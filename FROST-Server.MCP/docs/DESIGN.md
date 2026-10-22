# FROST-Server MCP Interface — Design Document

*Status: Draft for discussion
*Module: `FROST-Server.MCP`
*Protocol: [Model Context Protocol](https://modelcontextprotocol.io) (MCP), Streamable HTTP transport
*Target SDK: `io.modelcontextprotocol.sdk` (official Java SDK) `mcp-core` **2.1.0** (verified on Maven
Central; in the 2.x line the servlet transports live *inside* `mcp-core`; the old
`server-servlet` artifact does not exist for 2.x)
*Interface version: v1 (query, create, update, delete — no notifications, no batch, no generated per-entity tools)
*Deployment: **stand-alone runnable jar** with embedded Jetty, exactly like `FROST-Server.MQTT`
  (own `main()`, `FrostMcp.properties` config file, same database/`CoreSettings`). Not a
  servlet embedded in the HTTP/MQTTP wars.

---

## 1. Goals and Non-Goals

### Goals

1. Expose a FROST-Server instance to LLM clients (agents, IDE assistants, chat clients) via MCP.
2. **Work with any data model.** FROST's model is pluggable and not fixed at compile time
   (CoreModel, CoreModelV2, MultiDatastream, Actuation, Projects, …). The MCP interface must
   therefore be **model-agnostic**: no tool, schema or description may hardcode SensorThings
   entity types.
3. Reuse the transport-agnostic core (`Service`, `ServiceRequest`, `ServiceResponse`) so that
   MCP is a *frontend* beside HTTP and MQTT, not a parallel API.
4. Reuse the existing security model unchanged (servlet filter chain, roles, fine-grained
   authorisation in `Service`/`PersistenceManager`).
5. Keep results small and LLM-context friendly by applying server-side result caps with
   standard STA continuation (`@iot.nextLink`).

### Non-Goals (for v1)

* **No generated per-entity-type tools** ("Layer 2"). Model knowledge is offered through
  tool *descriptions*, input *enums* and *resources* that are generated at runtime from the
  `ModelRegistry` — see §5.2. Revisit later.
* **No batch processing tool.** The STA batch/`$batch` machinery
  (`Plugins/BatchProcessing`) is not mapped to MCP in v1.
* **No server→client notifications / subscriptions.** Mapping `MessageBus`/
  MQTT-style entity-change events to MCP notifications is future work; v1 is
  strictly request→response.
* **No custom streaming JSON-RPC layer.** We use the official MCP Java SDK, which
  buffers tool results in memory. This is acceptable **because** results are capped
  (§8). The server-side streaming goals tracked in `FROST-Server.MCP/issues.txt`
  (Readers/Iterators instead of Strings/Lists) are an orthogonal, longer-term
  improvement; when they land, MCP serialization can switch to bounded streams
  *without contract changes*.
* **No MCP-native OAuth authorization server.** v1 relies on the existing servlet
  filter authentication (Basic / Keycloak / OIDC). MCP's OAuth 2.1 resource-server
  profile is future work (§14).

---

## 2. FROST Architecture Facts the Design Relies On

| Fact | Code reference | Consequence for MCP |
|---|---|---|
| `Service` is a transport-agnostic request executor: `execute(ServiceRequest, ServiceResponse)` dispatches `READ`, `CREATE`, `UPDATE_*`, `DELETE` against the `PersistenceManager`, with auth, validation, transactions and formatting | `FROST-Server.Core/.../service/Service.java:188` | MCP tools are thin adapters: build a `ServiceRequest`, execute, read the `ServiceResponse`. All security (roles, fine-grained filter rules in the PM) applies automatically. |
| Requests are fully generic: path (STA addressing language) + query (STA `$filter`/`$select`/`$expand`/`$orderby`/`$top`/`$skip`/`$count`…) | `PathParser`, `QueryParser` | One generic tool set covers **any** entity type, including model extensions and custom properties. The path/query language *is* the schema-agnostic API. |
| URL path excludes the version part; the version is a separate attribute | `HttpRequestDecoder.serviceRequestFromHttpRequest` (strips `v1.1` into `Version`), `PluginManager.getVersion(versionPart)` | Tools take `path` *relative to the version* plus an explicit `version` argument; no URL juggling for the LLM. |
| The model is fully introspectable at runtime and frozen after startup | `ModelRegistry` (`getEntityTypes()`, `getEntityTypeForName()`, `initFinalise()`), `EntityType` (`getProperties()`, `getEntityProperties()`, `getNavigationProperties()`, navigation properties know entity vs entity-set; `getPrimaryKey()`), `Property` (`isRequired()`, `isNullable()`, `isReadOnly()`, typed via `PropertyType`) | Tool descriptions, enums and resources are **generated at startup** from the registry — never compiled in. |
| Free-form JSON is a first-class type in the model | `TypeSimplePrimitive.EDM_UNTYPED` — "Can be any valid JSON" (`FROST-Server.Core.Model/.../type/TypeSimplePrimitive.java:110`); e.g. `Thing/properties`, `Datastream/unitOfMeasurement`, `Observation/parameters` | MCP `structuredContent` can carry these; we avoid rigid `outputSchema`s (§7). |
| STA has built-in paging with continuation links | `EntitySetResultSerializer` emits `@iot.nextLink` when the server applies `$top` | MCP result capping reuses standard paging: the LLM "turns the page" with `frost_get(nextLinkPath)`. |
| FROST frontends are either wars (HTTP) or stand-alone processes (MQTT); both boot from `CoreSettings`; auth/CORS are installed as servlet filters via `AuthProvider` against the running `ServletContext` | `ServletMain`, `AbstractContextListener` (wars), `FrostMqttServer` (stand-alone) | The MCP server is a **stand-alone process like MQTT**, embedding Jetty so the *same* filter installation code secures `/mcp`. |
| Auth is a servlet filter chain installed by pluggable `AuthProvider`s | `AbstractContextListener.setupAuthFilter`, `BasicAuthFilterHelper.addFilter` | Mapping the FROST auth filters to `/*` (or explicitly `/mcp` too) secures the MCP endpoint with zero MCP code; the authenticated `Principal` is picked up per request. |
| Per-request `Service` instances, `ServiceRequest` ThreadLocal | `ServletMain.executeService` (try-with-resources + `ServiceRequest.setLocalRequest`/`removeLocalRequest`) | MCP tool handlers follow the same pattern; `Service` is documented as not thread-safe, so one instance per tool call. |
| Server sizing knobs already exist | `CoreSettings`: `defaultTop=100`, `maxTop=10000`, `maxDataSize=25 MB` | MCP adds its own, stricter caps under `mcp.*` (§9) and clamps `$top` before execution. |

The MCP Java SDK further aligns well:

* Server transport is a **Jakarta Servlet** implementation shipped inside `mcp-core`
  (`HttpServletStreamableServerTransportProvider`) — hosted by our embedded Jetty.
  It matches the servlet API generation FROST already uses (jakarta.servlet 6.1).
* JSON binding is `tools.jackson` (Jackson 3) via `mcp-json-jackson3` — the **same Jackson
  generation FROST already depends on** (root `pom.xml`, `version.jackson = 3.2.0`), so there
  is no JSON library conflict and `McpJsonMapper`-style concerns stay in-family.
* MIT licensed (LGPL-compatible).
* Latest stable release at time of writing: **2.1.0** (`2.2.0-M1` is a milestone); to be
  pinned via a `version.mcp` property + `mcp-bom`.
* The SDK supports MCP protocol revision **2025-06-25 and later**, which includes Streamable
  HTTP and structured tool output (`structuredContent`) — the features this design uses.

---

## 3. System Overview

```
MCP Host (LLM agent, IDE, ...)
   │  JSON-RPC 2.0 over Streamable HTTP (POST /mcp, optional GET SSE stream)
   ▼
┌────────────────── Stand-alone process: FROST-Server.MCP (like FROST-Server.MQTT) ──────────────────┐
│  Embedded Jetty (jetty-ee10-servlet)                                                               │
│  │  Auth filter chain (AuthProvider: Basic / Keycloak / OIDC), CORS filter, Remote-IP filter        │
│  │        │                                                                                        │
│  │        ▼                                                                                        │
│  │  HttpServletStreamableServerTransportProvider  (MCP Java SDK, mcp-core 2.1.0)                    │
│  │        │  ── delegates to ──▶  McpSyncServer: tools (frost_get/query/create/update/delete)       │
│  │        │                         resources (frost://index, frost://model + templates)           │
│  │        ▼                                                                                        │
│  │  FrostToolHandler: builds ServiceRequest (path, query, version, PrincipalExtended)              │
│  ▼                    ──▶  Service.execute  ──▶  PersistenceManager ──▶ same Database as FROST-HTTP │
│                                      ModelRegistry  ──▶ Model (pluggable, any model)              │
└────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

Key property: **everything between the Jetty servlet and `Service` is model-agnostic.** Only
descriptions, enums and resources are generated from the live `ModelRegistry`.

---

## 4. Endpoint, Protocol and Lifecycle

* **Deployment model: stand-alone process**, mirroring `FROST-Server.MQTT`
  (`FrostMqttServer`): entry point `main(String[] args)` reads a properties file
  (default `FrostMcp.properties`, overridable as `args[0]`) into a `CoreSettings`
  instance — in stand-alone mode the MCP server is a **sidecar to the same database**:
  it talks to the same DB via the `PersistenceManager`, is configured from the same
  settings, and does *not* require the FROST HTTP server to run in the same process.
* **Endpoint inside the process:** embedded Jetty (Jakarta EE 10 servlets, artefact
  `jetty-ee10-servlet` — already version-managed by the root pom) hosts a
  `ServletContextHandler` with one servlet: the SDK's
  `HttpServletStreamableServerTransportProvider` (the SDK transport *is* an
  `HttpServlet`), mapped at `/mcp`. `POST /mcp` handles JSON-RPC; `GET /mcp` optionally
  streams SSE; `DELETE /mcp` ends the session.
* **Auth, Mongo-style:** the stand-alone MCP server reuses the existing filter
  machinery unchanged by hosting them *inside its own Jetty* (mirroring how
  `AbstractContextListener.setupAuthFilter` installs filters into a servlet context):
  * `IpFilterHelper.setupRemoteIpFilter(context, coreSettings)`
  * `AuthProvider`-installed filters (Basic / Keycloak / OIDC) via
    `authProvider.init(coreSettings)` + `addFilter(servletContext, coreSettings)`.
  * CORS filter (Catalina if on classpath, else Jetty `CrossOriginFilter`) — same
    detection logic as `AbstractContextListener.setUpCorsFilter`.
  The authenticated `Principal` from `HttpServletRequest.getUserPrincipal()` is then
  captured per request through the transport's `contextExtractor` hook and converted
  with `PrincipalExtended.fromPrincipal(...)`.
* **Protocol revision:** SDK 2.x implements MCP protocol revision **2025-06-25** and
  negotiates per client; Streamable HTTP only (no legacy 2024-11-05 SSE transport).
* **Server metadata:** name `FROST-Server`, version from `GitVersionInfo`;
  capabilities declared by registering tools/resources only (the SDK derives
  `ServerCapabilities` automatically: `tools`, `resources`, `resourceTemplates`).
  No prompts, no sampling, no elicitation in v1.
* **Sessions:** the SDK transport manages `Mcp-Session-Id` handling. FROST-side, tool
  calls are **stateless** (one `Service` + `PersistenceManager` per call, identical to
  `ServletMain`), so MCP sessions need no FROST-side state: the current request's
  principal travels with the transport context (§10).
* **Sync facade:** tools are registered on the SDK's *synchronous* server facade
  (`McpServer.sync(streamableTransportProvider)` → `McpSyncServer`); each tool call is
  short-lived and blocking, matching `Service`'s one-transaction-per-call semantics.
  (The SDK's reactive plumbing stays an internal detail.)
* **Shut-down:** shutdown hook mirrors `FrostMqttServer.stop()`: stop Jetty → stop
  metrics endpoint → `MessageBusFactory` bus stop (only if the MCP process also owns
  local services — see §9 `mcp.startBus`).

---

## 5. Tools

Design rule: **fixed, small, path/query-addressed tool set** ("Layer 1"). The STA path
language already encodes entity sets, entities, navigation links, property access (`/name`,
`/$value`), so six generic tools are complete for any model.

### 5.1 Tool List

| Tool | Purpose | Maps to (`Service`) | MCP annotations |
|---|---|---|---|
| `frost_get` | GET any STA path, with optional raw query options | `READ` | `readOnlyHint: true`, `idempotentHint: true`, `openWorldHint: false` |
| `frost_query` | Structured, LLM-friendly entity-set query (typed args instead of hand-built query strings) | `READ` | `readOnlyHint: true`, `idempotentHint: true` |
| `frost_create` | POST a new entity to an entity set | `CREATE` | `destructiveHint: false`, `idempotentHint: false` |
| `frost_update` | PATCH (patch/changeset) an existing entity | `UPDATE_CHANGES` / `UPDATE_CHANGESET` | `destructiveHint: false`, `idempotentHint: true` |
| `frost_replace` | PUT an existing entity — full replace | `UPDATE_ALL` | `destructiveHint: true`, `idempotentHint: true` |
| `frost_delete` | DELETE an entity | `DELETE` | `destructiveHint: true`, `idempotentHint: true` |

Rationale for having both `frost_get` and `frost_query`: LLMs make fewer mistakes with
structured, typed arguments (arrays for `$select`, explicit integers for `$top`), but the
full STA expressiveness (deep paths, `$value`, `$count`, `$expand` paths, function calls)
needs the raw string form. `frost_query` covers the 90% case; `frost_get` is the escape
hatch. Both share the same execution path.

Rationale for splitting `frost_update` and `frost_replace`: MCP tool annotations are
static per tool registration, so "destructive for `put` only" cannot be expressed on one
tool. Separate tools let clients auto-allow patches while gating full replaces behind an
approval flow — exactly what `destructiveHint` exists for; the tool name itself also
signals the danger to the LLM.

`frost_update` keeps `destructiveHint: false` even though changeset (RFC 6902) documents
can contain `remove`/`replace` ops: destructive ops in a JSON Patch are explicit in the
request (one does not accidentally add a `remove` op), whereas the PUT hazard is precisely
the *omitted* properties being wiped — so only `frost_replace` needs the hint.

### 5.2 Model-Generated Bits (within the fixed tool set)

Although the *tools* are fixed, two inputs are **generated at server startup** from the
live `ModelRegistry` and injected into `tools/list` responses:

1. `frost_query.entitySet` — a JSON Schema `enum` of all non-admin entity-set names
   (`modelRegistry.getEntityTypes()` → `plural`), plus the `namespace` variants the
   registry resolves. This is the only model-derived *schema* element and it is cheap,
   stable (registry is frozen after `initFinalise()`) and enormously improves LLM accuracy.
2. **Tool descriptions** — each description embeds a dynamically assembled cheat-sheet:
   the available entity sets, the nav-link syntax (`Things(1)/Datastreams`), the
   filter-expression examples, and a pointer to the `frost://model` resource for details.
   Descriptions are regenerated on server start; if a future FROST version ever allows
   model hot-swap, expose `notifications/tools/list_changed`.

Admin-only users may additionally see admin-only types in the enum when their session
principal is admin (`PrincipalExtended.isWrittenAdmin()` uses the same registry logic as
`Service`).

### 5.3 Tool Contracts

All tools take an optional `version` argument (default: first configured/default version,
see §9). Paths are **relative to the version root** — i.e. the same string the STA HTTP
interface would place after `/v1.1/`.

#### frost_get

```jsonc
{
  "name": "frost_get",
  "inputSchema": {
    "type": "object",
    "required": ["path"],
    "properties": {
      "path": {
        "type": "string",
        "description": "STA path, relative to the version root. Examples: 'Things', 'Things(1)', 'Things(1)/Datastreams', 'Things(1)/name', 'Things(1)/name/$value', 'Datastreams(7)/Observations'. An empty path fetches the service index document."
      },
      "query": {
        "type": "string",
        "description": "Raw STA query options, without leading '?'. Example: '$filter=properties/temperature gt 20 and phenomenonTime gt 2026-01-01T00:00:00Z&$orderby=phenomenonTime desc&$top=5&$select=id,result,phenomenonTime'. Validated by the STA query parser.",
        "default": ""
      },
      "version": { "type": "string", "description": "STA version part, e.g. 'v1.1'. Default: the server's default version." }
    },
    "additionalProperties": false
  }
}
```

Returns the STA JSON response (entity, entity set with `@iot.count`/`@iot.nextLink`,
plain value, or index document). Errors (404, 400, 401, 403, …) are returned as tool
*results* with `isError: true` and a machine-readable `statusCode` — see §11.

#### frost_query

```jsonc
{
  "name": "frost_query",
  "inputSchema": {
    "type": "object",
    "required": ["entitySet"],
    "properties": {
      "entitySet": { "type": "string", "enum": ["__GENERATED_FROM_MODELREGISTRY__"] },
      "filter":  { "type": "string",  "description": "STA $filter expression, without the '$filter=' prefix." },
      "select":  { "type": "array",   "items": { "type": "string" }, "description": "Properties for $select; '@id' allowed." },
      "expand":  { "type": "array",   "items": { "type": "string" }, "description": "$expand paths, e.g. 'Datastream', 'Things($select=id,name)'." },
      "orderby": { "type": "string",  "description": "STA $orderby expression, e.g. 'phenomenonTime desc'." },
      "top":     { "type": "integer", "minimum": 1, "description": "Page size. Clamped to the MCP maximum (see mcp.maxTop)." },
      "skip":    { "type": "integer", "minimum": 0 },
      "count":   { "type": "boolean", "description": "Request @iot.count total." },
      "version": { "type": "string" }
    },
    "additionalProperties": false
  }
}
```

The MCP layer assembles the query string from the typed args and executes the same code
path as `frost_get`. Follow-up pages: read `@iot.nextLink` from the result and issue
`frost_get` with the link's path+query (or `frost_query` with an adjusted `skip`).

#### frost_create

```jsonc
{
  "name": "frost_create",
  "inputSchema": {
    "type": "object",
    "required": ["path", "entity"],
    "properties": {
      "path":   { "type": "string", "description": "Target entity set, optionally nested under an entity. Examples: 'Things', 'Things(1)/HistoricalLocations', 'Datastreams(3)/Observations'." },
      "entity": { "type": "object", "description": "The entity to create, as STA JSON. Required/nullable/read-only rules and navigation by @id link are validated by the server (EntityType.validateCreate)." },
      "prefer": { "type": "string", "enum": ["representation", "minimal"], "default": "representation", "description": "'minimal' returns only the new @iot.id; 'representation' returns the full created entity." },
      "version": { "type": "string" }
    },
    "additionalProperties": false
  }
}
```

`frost_create` sends the STA `Prefer` header equivalent of its `prefer` arg. Deep inserts
(inline related entities in `entity`) work because the server core already handles them.

#### frost_update

```jsonc
{
  "name": "frost_update",
  "inputSchema": {
    "type": "object",
    "required": ["path", "changes"],
    "properties": {
      "path":    { "type": "string", "description": "Path to the entity (or property) to update, e.g. 'Things(1)' or 'Things(1)/name'." },
      "changes": { "description": "'patch': an STA JSON object (entity or single property value). 'changeset': a JSON Patch array of operations." },
      "mode":    { "type": "string", "enum": ["patch", "changeset"], "default": "patch", "description": "patch: PATCH semantics, only listed properties change (UPDATE_CHANGES). changeset: RFC 6902 JSON Patch (UPDATE_CHANGESET)." },
      "version": { "type": "string" }
    },
    "additionalProperties": false
  }
}
```

#### frost_replace

```jsonc
{
  "name": "frost_replace",
  "inputSchema": {
    "type": "object",
    "required": ["path", "entity"],
    "properties": {
      "path":    { "type": "string", "description": "Path to the entity to replace, e.g. 'Things(1)'." },
      "entity":  { "type": "object", "description": "The complete new entity, as STA JSON. Every property not present is overwritten with its default value (PUT semantics — full replace)." },
      "version": { "type": "string" }
    },
    "additionalProperties": false
  }
}
```

#### frost_delete

```jsonc
{
  "name": "frost_delete",
  "inputSchema": {
    "type": "object",
    "required": ["path"],
    "properties": {
      "path":    { "type": "string", "description": "Path to the entity to delete, e.g. 'Observations(4711)'." },
      "version": { "type": "string" }
    },
    "additionalProperties": false
  }
}
```

### 5.4 Worked Example

```jsonc
// tools/call: frost_query
{ "name": "frost_query",
  "arguments": { "entitySet": "Observations",
                 "filter": "phenomenenTime gt now() sub duration'P1D' and Datastream/id eq 7",
                 "select": ["id", "result", "phenomenonTime", "Datastreams"],
                 "orderby": "phenomenonTime desc",
                 "top": 20 } }

// CallToolResult (success):
{ "content": [ { "type": "text", "text": "<pretty-printed STA JSON, see below>" } ],
  "structuredContent": {
      "@iot.count": 2,
      "value": [
        { "@iot.id": 4712, "phenomenonTime": "2026-10-22T09:41:00.000Z", "result": 21.4,
          "Datastream@iot.navigationLink": "Datastreams(7)" },
        { "@iot.id": 4710, "phenomenonTime": "2026-10-22T09:40:00.000Z", "result": 21.1,
          "Datastream@iot.navigationLink": "Datastreams(7)" }
      ],
      "@iot.nextLink": ".../Observations?$top=20&$skip=20&..."
  } }
```

---

## 6. Structured Content and Free-Form Properties

MCP clients that can, display `structuredContent` directly; it is a plain JSON object.
Many STA properties are intentionally free-form JSON objects (`Thing/properties`,
`Datastream/unitOfMeasurement`, `Observation/parameters`, …), typed in FROST as
`EDM_UNTYPED` ("Can be any valid JSON"). Additionally, the *shape* of any response depends
on the request's `$select`/`$expand` and on the (pluggable) model.

Consequences:

1. **`structuredContent` can carry free-form properties without problems** — anything that
   is valid STA JSON is a valid MCP tool result: `CallToolResult.structuredContent` is not
   restricted to a pre-declared shape.
2. **We do not declare `outputSchema` on entity-returning tools in v1.** An `outputSchema`
   is optional; when declared, spec-conforming clients enforce it, and no static schema can
   describe "a set of entities with model-pluggable types and request-dependent
   `$select`/`$expand`". Omitting `outputSchema` keeps the generic tools honest. (If per-
   entity constraints are ever wanted, generate loose per-type schemas later — none are
   needed for validation on the server side: the *server* already validates via
   `EntityType.validateCreate/validateUpdate`.)
3. Every result contains **both**:

   * `structuredContent`: the parsed STA JSON — machine-usable, preserves free-form
     objects verbatim;
   * a single `text` content block with the same JSON pretty-printed — for clients that
     only render text (cheap duplication because results are capped).
4. Free-form objects from the model appear in the generated `frost://model` resource as
   properties typed `"Edm.Untyped"` with the JSON-Schema-equivalent annotation
   `{"type": "object", "additionalProperties": true}` for guidance only.

---

## 7. Resources and Resource Templates

Resources give the LLM orientation without blobbing tool schemas ("blobbing" avoided by
design; the model resource is fetched on demand).

| URI | Type | MIME | Contents |
|---|---|---|---|
| `frost://index` | static | `application/json` | The STA root/index document (same map `PluginRootDocument`s produce via `Service.handleGetCapabilities`: entity-set URLs, `serverSettings`, `conformance`, `functions`). |
| `frost://model` | static | `application/json` | Generated model overview: for each `EntityType` (non-admin): singular/plural names, entity properties (name, type, required/nullable/read-only, JSON name), navigation properties (→ entity vs entity set, target type, inverse), primary key type. Generated from the live `ModelRegistry` at first read; cached. |
| `frost://model/{entitySet}` | template | `application/json` | Per-entity-type detail, same generator. |
| `frost://sta/{path}` | template | `application/json` | Live read of any STA path, with MCP caps applied. The handler simply runs the same code as `frost_get` with default query options. Lets agents attach stable resource links (e.g. as output references) without new tools. |

Resource reads execute with the *session principal's* rights, exactly like tools.

---

## 8. Result Size Policy ("context-window friendly")

LLM context is the scarce resource; MCP results must be bounded independently of
`maxTop` (10000) / `maxDataSize` (25 MB). Policy (all enforced in the MCP layer, before
`Service` parses the query):

1. **Paging cap:** any `$top` (including implicit ones) is clamped to `mcp.maxTop`
   (default **100**). Entity sets then automatically carry `@iot.nextLink` because that
   is precisely the condition under which the core emits next links — the agent pages
   with follow-up calls. No bespoke truncation protocol needed.
2. **Byte cap:** after formatting, if the response exceeds `mcp.maxResultSize` (default
   **1 MiB**), the tool returns a structured error (result, not protocol error) with
   the message "Result too large for MCP; use $select/$expand to narrow the query and
   $top to page" plus the count. For entity sets the byte cap practically never triggers
   below `$top` 100 unless individual entities are huge (large free-form properties);
   single large reads must be narrowed.
3. **Count default:** `mcp.count` (default **true**): MCP always asks for `@iot.count`
   on entity sets so the agent immediately learns the total size and can plan paging.
   (STA's default is false, `defaultCount`.)
4. **Writes minimal by option:** `frost_create.prefer = "minimal"` suppresses the full
   entity echo for very large creates.

Because of these caps, the SDK's buffered result model is fine for v1. The streaming
goals from `issues.txt` become relevant if `mcp.maxResultSize` is raised far above the
defaults or for high-throughput servers; the contract will not change when we switch to
bounded stream serialization.

---

## 9. Configuration (`mcp.*` settings)

Loaded via the standard `Settings`/`ConfigProvider` machinery, prefix `mcp.`
(complete annotated `McpSettings` class, analogous to `MqttSettings`; names as served by
the `settings-annotation` processor so they appear in generated docs):

| Property | Type | Default | Meaning |
|---|---|---|---|
| `mcp.enable` | boolean | **true** (stand-alone module; setting exists for symmetry/wiring guards) | Serves MCP only when true. |
| `mcp.host` | string | `0.0.0.0` | Jetty bind address. |
| `mcp.port` | int | 8080 | Jetty port for `/mcp`. |
| `mcp.defaultVersion` | string | *first registered default version, usually `v1.1`* | Used when a tool call omits `version`. Resolved via `PluginManager.getVersion(...)`. |
| `mcp.maxTop` | int | 100 | Paging cap (§8.1). |
| `mcp.maxResultSize` | int (bytes) | 1048576 | Byte cap (§8.2). |
| `mcp.count` | boolean | true | Force `@iot.count` on entity sets (§8.3). |
| `mcp.allowTools` | — | `get,query,create,update,replace,delete` | Comma-separated list of enabled tools; lets deployments expose a read-only MCP ("get,query") or a write MCP. |
| `mcp.auth.requireAuthenticated` | boolean | false | When true, anonymous MCP sessions (unauthenticated principal) are rejected; default follows the container outcome like STA does. |
| `mcp.startBus` | boolean | false | Start a `MessageBus` in this process (needed only if a plugin requires an active bus; the core tool path does not). |

Config file (`FrostMcp.properties`) follows `FrostMqtt.properties`: the same
`bus.*` / `persistence.*` / `auth.*` / `plugins.*` keys drive `CoreSettings`, plus the
`mcp.*` block above.

---

## 10. Security Model

HTTP filters can't be reused 1:1 for `/mcp`: the standard filter registrations
(`BasicAuthFilterHelper`/`KeycloakFilterHelper` `AuthFilterSta`) are mapped only to
`/{version}` and `/{version}/*`, and their per-HTTP-method role checks cannot map onto
POST-only JSON-RPC (a read-only user would need the create-role for every `frost_get`;
`KeycloakFilter.findRequiredRoleForRequest` even crashes on the 4-char `/mcp`
servlet path — `pathInfo.substring(0, 5)`). Therefore the MCP server installs its own
authentication as described below, while authorization stays where it already lives:
in the persistence-layer security validators.

* **Second filter registration for `/mcp` (Basic).** The MCP boot registers its own
  instance of `BasicAuthFilter` (name `AuthFilterMcp`, mapped to `/mcp`) with init-param
  `authenticateOnly=T`. In that mode (`BasicAuthFilter.doFilter`) the filter only
  authenticates the `Authorization` header and wraps the request with a
  `PrincipalExtended` (name + roles + admin flag from the user table) — no per-method
  role check, which is exactly right for JSON-RPC. The init-param
  `allowAnonymousRead` is derived from `!mcp.auth.requireAuthenticated` (§9):
  * `mcp.auth.requireAuthenticated=T` → `allowAnonymousRead=F` → requests without
    valid credentials get 401 before JSON-RPC processing.
  * `mcp.auth.requireAuthenticated=F` → `allowAnonymousRead=T` → anonymous requests
    pass; the filter wraps a null-name principal, which the `PrincipalExtractor`
    normalizes to `PrincipalExtended.ANONYMOUS_PRINCIPAL`.
* **Second filter registration for `/mcp` (Keycloak).** A small backward-compatible
  patch in `FROST-Server.Auth.Keycloak` `KeycloakFilter` (safe substring key
  extraction + `"/mcp"` entry in `roleMappersByPath`) allows the MCP boot to register a
  `KeycloakFilter` for `/mcp`. Deployments combine this with
  `auth.authenticateOnly=T` in `FrostMcp.properties` — correct for the stand-alone
  MCP process (per-process settings), where the HTTP layer only authenticates
  (Bearer tokens, sessions) and the persistence layer authorizes. Existing
  deployments are unaffected (none of them map `/mcp`).
* **Mandatory persistence-layer authorization at boot.** Independent of the global
  `auth.authenticateOnly`, the MCP boot ensures the PM-side default security validators
  are installed (`jpm.getTableCollection().addSecurityValidator("", null)` — the same
  idiom `BasicAuthProvider.init`/`KeycloakAuthProvider.init` use), so
  `TableCollection` installs `CheckUserHasRoles` hooks for insert/update/delete on all
  tables (`ROLE_ADMIN` or `roleCreate`/`typeName+create` etc., `Type.ANY`). These
  validators read `PrincipalExtended.getLocalPrincipal()` (the ThreadLocal that
  `ServiceRequest.setLocalRequest` sets — the same call the tool handlers make), so
  writes via MCP are role-checked at the data layer even when the HTTP-authenticated
  identity carries only "authenticated" semantics.
* **Read gate mirroring the MQTT adapter.** The persistence layer does *not* check the
  read role (HTTP filters do that per-method; MQTT checks it explicitly in
  `AuthWrapper.canRead`). Since the MCP filter runs in authenticate-only mode, the
  `FrostToolHandler` applies one coarse check per call, mirroring
  `AuthWrapper.canRead`: the principal must have the read role or the admin role, and
  anonymous principals are only allowed when `auth.allowAnonymousRead` is true (the
  `mcp.auth.requireAuthenticated` setting independently controls whether anonymous
  sessions are accepted at all). This keeps role semantics identical to MQTT, which
  faces the same "authenticate-then-authorize" situation.
* **Boot fail-safe on unknown providers.** If `auth.provider` names a class the MCP
  boot cannot map onto `/mcp` (anything other than `BasicAuthProvider` /
  `KeycloakAuthProvider`, or empty), and `mcp.auth.requireAuthenticated=T`, the MCP
  server **refuses to start** with a clear error message: the endpoint would otherwise
  silently be unprotected. With `mcp.auth.requireAuthenticated=F` (explicitly
  acknowledging anonymous use) the server starts, bordered by a loud warning in the
  log when an auth provider is configured.
* **Principal propagation.** The transport's `contextExtractor(HttpServletRequest)`
  hook captures `HttpServletRequest.getUserPrincipal()` on every request (it is called
  per HTTP request, including `initialize`), converts it with
  `PrincipalExtended.fromPrincipal(...)` — null/empty → `ANONYMOUS_PRINCIPAL` — and
  passes it via `McpTransportContext` to the tool/resource handler (the exchange
  exposes `exchange.transportContext()`). Each handler sets
  `ServiceRequest.setUserPrincipal(...)`. Unauthenticated sessions are impossible once
  the transport accepted the request (session-bound), an anonymous principal only
  occurs when the security settings permit it.
* **Authorization granularity.** Because tools speak the plain STA language, fine-grained
  auth (`SecurityModel`/filter rules per user, entity and property) works without any
  MCP-specific code path.
* **Tool surface control:** `mcp.allowTools` gives deployments a read-only MCP if desired
  (§9).
* **Data exposure parity:** results are exactly the STA JSON the same principal would
  receive over HTTP — no extra fields, no relaxed defaults. Note `serviceRootUrl` appears in
  links (`@iot.selfLink`), as usual.
* **Auditability/oversight:** tool calls are logged (method, path shape, outcome — *not*
  full payloads, honouring `logSensitiveData`) with a distinct logger name
  (`...mcp.audit`) and metrics counters (§13).

---

## 11. Request Flow and Error Mapping

### 11.1 Happy-path flow (per `tools/call`)

1. SDK transport receives JSON-RPC request, authenticates session principal per §10.
2. Tool handler (e.g. `FrostToolGet`) validates args (`version` resolution, `path` non-empty).
3. Build `ServiceRequest`:
   `.setContext(new ServiceContext()…)` (mirroring `HttpRequestDecoder`),
   `.setVersion(resolvedVersion)`, `.setRequestType(READ|CREATE|UPDATE_*|DELETE)` via
   `PluginManager.decodeRequestType(serviceForPath, version, path, method, contentType)`,
   `.setUrlPath(path)`, `.setUrlQuery(assembledQuery)`,
   `.setContent(entityJson)`, `.setContentType("application/json"` or
   `"application/json-patch+json"` for changeset mode`)`,
   `.setUserPrincipal(sessionPrincipal)`.
4. `try (Service service = new Service(coreSettings))` +
   `ServiceRequest.setLocalRequest(req)` … `service.execute(req, response)` with a
   `ServiceResponseDefault` (in-memory writer), then `removeLocalRequest()` — the exact
   `ServletMain.executeService` pattern. The core formats the result JSON (default
   ResultFormatter) into the response writer.
5. Cap checks (§8: clamping happened in step 3; byte cap checked here on the formatted
   result).
6. Build `CallToolResult`: `content=[text(prettyJson)]`, `structuredContent=parsed`,
   `isError=false`.

`PATCH`-semantics detail for updates: `Service.executeGet` shows the mapping of request
types; for consistency with the STA HTTP interface the MCP layer uses
`UPDATE_CHANGES` (PATCH), `UPDATE_CHANGESET` (JSON-Patch) and `UPDATE_ALL` (PUT) — the
same request types the STA plugin decodes for the same HTTP methods/content types
(see `PluginCoreService.getRequestTypeFor`).

### 11.2 Mapping table

| Situation | Result |
|---|---|
| Path/query parse failure, unknown entity set, bad enum, invalid `$filter` (from `PathParser`/`QueryParser`, or `IllegalArgumentException`) | JSON-RPC **protocol error** `-32602 Invalid params`, message carries the parser message. (Fails fast before touching the DB; distinguishes malformed MCP calls from data-level errors.) |
| Everything executed by `Service` returning 2xx | Success result (above). |
| `404 Not Found` (entity, path) | Tool result, `isError: true`, `structuredContent = { "statusCode": 404, "message": "…" }`. |
| `400 BadRequest` (semantic violations caught inside `Service`, e.g. incomplete entity) | Tool result, `isError: true`, `{ "statusCode": 400, "message": … }`. |
| `401 Unauthorized` / `403 Forbidden` (from `UnauthorizedException`/`ForbiddenException`) | Tool result, `isError: true`, `{ "statusCode": 401|403, "message": … }` — the *tool* protocol layer stays 200 so the agent sees and can react to the auth problem. |
| `409/4xx` duplicates etc. | as 4xx tool result. |
| `500` internal (`errorResponse` 500, unexpected exception) | Tool result, `isError: true`, generic message (details in server log, honour existing "See logs for details." behaviour). |
| `tools/call` with disabled tool name (mcp.allowTools) | JSON-RPC `-32601 Method not found`. |
| Byte cap exceeded (§8.2) | Tool result, `isError: true`, `{ "statusCode": 499-ish "tooLarge" code?}` — defined as `"statusCode": 413, "message": "…" }` guidance text (see §8.2). |

STANDARD `initialize`, `tools/list`, `resources/list`, `resources/templates/list`,
`ping` are answered by the SDK directly (our registrations feed them).

---

## 12. Component / Module Structure

### 12.1 Maven

* New artifact `de.fraunhofer.iosb.ilt.FROST-Server:FROST-Server.MCP` (jar), directory
  `FROST-Server.MCP/`, **mirroring `FROST-Server.MQTT`**:
  * `pom.xml` inherits from the root parent; `mainClass` property;
    `maven-assembly-plugin` `jar-with-dependencies` execution bound to `package`
    (same block as `FROST-Server.MQTT/pom.xml`), `skipPublishing` for central publishing.
  * Dependencies on the plugin set the same way the MQTT module pulls them in
    (`Auth.Basic`, `Auth.Keycloak`, `Core`, `Plugin.CoreModel`, `Plugin.CoreModelV2`,
    `Plugin.MultiDatastream`, `Plugin.ModelLoader`, `Plugin.OpenApi`, `Plugin.OData`,
    SQLjooq persistence, postgres + mariadb drivers, logback, prometheus, …) so the
    stand-alone jar is a complete FROST frontend for any installed model.
  * MCP SDK via the `mcp-bom` (pinned `2.1.0` with a `version.mcp` property in the root
    pom): brings in `mcp-core` (contains servlet transports + `McpSyncServer`) and
    `mcp-json-jackson3` (Jackson-3 `McpJsonMapper`, matching FROST's `tools.jackson`).
  * `org.eclipse.jetty.ee10:jetty-ee10-servlet` for the embedded server (version already
    managed by the root pom).
  * `provided`-scope: none (the jar bundles everything; wars no longer host MCP).
* Root `pom.xml`: add `<module>FROST-Server.MCP</module>`, add `version.mcp` property
  and the `mcp-bom` import in `dependencyManagement`.
* The wars (`FROST-Server.HTTP`, `FROST-Server.MQTTP`) do **not** change: MCP is a
  stand-alone sibling process, like MQTT.

### 12.2 Classes (package `de.fraunhofer.iosb.ilt.frostserver.mcp`)

```
de.fraunhofer.iosb.ilt.frostserver        // main() entry, mirrors FrostMqttServer
└── FrostMcpServer                        // boot: CoreSettings → filters → Jetty → SDK server
de.fraunhofer.iosb.ilt.frostserver.mcp
├── McpSettings                           // ConfigProvider for mcp.* (@DefaultValue…)
├── session/
│   └── PrincipalExtractor                // contextExtractor(HttpServletRequest): extracts
│                                         //   PrincipalExtended from the request
├── tool/
│   ├── FrostToolHandler                  // shared execution skeleton: validate → ServiceRequest
│   │                                     //   → Service.execute → cap → CallToolResult (§11.1)
│   ├── FrostToolGet                      // frost_get
│   ├── FrostToolQuery                    // frost_query (assembles STA query string)
│   ├── FrostToolCreate                   // frost_create (Prefer handling)
│   ├── FrostToolUpdate                   // frost_update (patch / changeset)
│   ├── FrostToolReplace                  // frost_replace (put: full replace)
│   └── FrostToolDelete                   // frost_delete
├── resource/
│   ├── ModelResourceGenerator            // ModelRegistry → frost://model JSON (also feeds
│   │                                     //   frost_query enum + tool descriptions)
│   └── FrostResourceHandler              // frost://index, frost://model, templates → STA calls
└── util/
    └── StaJsonHelper                     // bounded pretty-print + parse + byte-cap check
```

Class-level notes:

* `FrostMcpServer.main` mirrors `FrostMqttServer.main` (config file arg, shutdown hook,
  `WaitForEnter`), and additionally: `PersistenceManagerFactory.init`,
  `IpFilterHelper.setupRemoteIpFilter`, auth filters via `AuthProvider` calls, CORS,
  then Jetty `Server` + `ServletContextHandler` + SDK transport servlet + `McpSyncServer`
  construction, then `server.start()`; finish on `waitForEnter` / shutdown hook.
* The separation of `FrostToolHandler` from the Jetty/SDK plumbing keeps the *tool core*
  transport-agnostic — a future *stdio* server binary (next bullet) can reuse it verbatim.
* `ModelResourceGenerator` is the single introspection point: it *never* mentions STA types;
  it walks `ModelRegistry`/`EntityType`/`Property`/`PropertyType` only (`Edm.*` names
  surface verbatim; `EDM_UNTYPED` → free-form object annotation, §6).

---

## 13. Observability, Metrics, Logging

* Prometheus counters/histograms, following the existing `Registry`-free counter style used
  in `ServletMain`/`Service`:
  * `frost_mcp_tool_calls_total{tool, outcome}` (outcome: `success | clientError | authError | serverError | tooLarge`)
  * `frost_mcp_tool_duration_seconds{tool}` (Histogram)
  * `frost_mcp_active_sessions` (Gauge)
* Audit log `de.fraunhofer.iosb.ilt.frostserver.mcp.audit`: one INFO line per tool call
  (tool, version, path *shape*, principal name, statusCode). No payload logging unless
  `logSensitiveData` is set (existing core tag honoured).
* All internal logging via SLF4J like the rest of the server; the SDK's SLF4J usage
  merges naturally.

---

## 14. Future Work (explicitly out of v1, revisit order)

1. **Layer 2 — generated per-entity-type tools** (`get_things`, `create_observation`, …) with
   full JSON-Schemas derived from the model. Trigger to revisit: evidence that generic
   tools + `frost://model` are not sufficient for agent accuracy, or per-type output
   schemas are required by clients.
2. **Batch tool** mapping to `Plugins/BatchProcessing` (deep-insert bundling, transactions
   over many operations).
3. **Notifications/subscriptions:** map `MessageBus`/`SubscriptionManager` (MQTT-style
   entity-change events) to MCP `notifications/*` + a `frost_subscribe` tool — turns MCP
   sessions into lightweight MQTT clients and pairs naturally with §8 (push instead of
   poll). Requires the SDK's notification plumbing (session-bound GET SSE streams).
4. **OAuth 2.1 resource-server support** per the MCP authorization spec (the SDK provides
   hooks; complements rather than replaces the filter chain).
5. **Streaming serialization** per `FROST-Server.MCP/issues.txt` — Readers/Iterators accepted
   by `Content`/`FormatWriter`-level plumbing, letting `StaJsonHelper` serialize with a
   `JsonGenerator` into *bounded* buffers instead of materializing strings (helps when
   `mcp.maxResultSize` is raised or on very large single entities).
6. **stdio server binary** reusing `FrostToolHandler` for local agents.
7. **MCP prompts** (pre-baked STA query recipes, e.g. "latest observations for a thing").
8. **Model hot-swapping + `notifications/tools/list_changed`** if the model ever stops
   being frozen at startup.

---

## 15. Testing Strategy

1. **Unit tests** (JUnit, in-module): query-string assembly for `FrostToolQuery`; version
   resolution; `mcp.maxTop`/byte-cap behaviour with a mocked `Service` (reuse the existing
   `TestModel` builder from `FROST-Server.Core` tests for a compact fake model — proves
   model-agnosticism incl. free-form properties); `ModelResourceGenerator` output on fake
   registries.
2. **Integration tests** (`FROST-Server.Tests`, Testcontainers Postgres as used today for
   `SQLjooq`): boot `FrostMcpServer` (with an in-memory or test DB) in the test JVM,
   drive it with the SDK's **MCP Java client** (`HttpClientStreamableHttpTransport`):
   initialize handshake, `tools/list` (assert generated enum for the deployed model),
   full CRUD round-trip against the standard test entities (also exercising deep inserts
   and `$expand`), paging via `@iot.nextLink` beyond `mcp.maxTop`, error mapping
   (404 → isError result), auth on/off (anonymous vs. filter-secured), free-form property
   round-trip.
3. **Conformance:** optionally run the official MCP conformance suite
   (`npx @modelcontextprotocol/conformance server --url http://…/mcp --suite active`; the
   SDK itself documents 40/40 server conformance) in CI against a running `FrostMcpServer`.
4. **Manual smoke:** an MCP host (e.g. Claude Desktop or an IDE MCP client) against a
   stock FROST deployment, checking description quality/LLM ergonomics — the ultimate
   usability gauge for tool descriptions and resources.

---

## 16. Open Points (settled during implementation unless noted)

| # | Question | Status / Decision |
|---|---|---|
| 1 | Expose `/mcp` under the *version* paths too (`/v1.1/mcp`)? | **Settled: No** — one unversioned `/mcp`; the STA version is chosen per call via the `version` arg. |
| 2 | Should `frost_get` on `/` return the index document, or should the index be a resource only? | **Settled: both** — tool support *and* resource; cheap, and agents naturally probe it. |
| 3 | Byte-cap error code choice (413 vs. custom structured marker). | **Settled:** `statusCode: 413` + guidance text (no HTTP involved, it's an MCP-internal code). |
| 4 | Include `frost://sta/{path}` resource template in v1 or defer? | **Settled: Include** — near-zero code, good agent ergonomics. |
| 5 | Pin SDK `2.1.0` vs. milestone `2.2.0-M1`. | **Settled:** `2.1.0` stable; bump opportunistically. |
| 6 | `mcp.maxTop` default (100) vs. aligning with STA `defaultTop` (100). | **Settled: keep independent settings.** |
| 7 | How to secure `/mcp` given `AuthFilterSta` is version-path-mapped with per-method roles (can't gate POST-only JSON-RPC)? | **Settled (user decision):** Basic → MCP boot registers its own `AuthFilterMcp` (`BasicAuthFilter`, `authenticateOnly=T`) for `/mcp`; Keycloak → small backward-compatible `KeycloakFilter` patch (safe path key + `"/mcp"` mapper entry) + `auth.authenticateOnly=T` in `FrostMcp.properties`; unknown provider + `mcp.auth.requireAuthenticated=T` → **boot fails**; MCP boot always installs PM-side default security validators (§10). |

---

## 17. References

* MCP specification & Streamable HTTP transport: <https://modelcontextprotocol.io>
* MCP Java SDK (used server-side and for tests): <https://github.com/modelcontextprotocol/java-sdk>
* OGC SensorThings API Part 1: Sensing (the query language this interface exposes):
  <https://docs.ogc.org/is/18-088/18-088.html>
* Internal: `FROST-Server.MCP/issues.txt` (streaming/efficiency goals — §14.5),
  `FROST-Server.Core/.../service/Service.java` (execution core),
  `FROST-Server.Core.Model/.../model/ModelRegistry.java` (introspection source),
  `FROST-Server.Core.Model/.../type/TypeSimplePrimitive.java` (`EDM_UNTYPED`).
```
