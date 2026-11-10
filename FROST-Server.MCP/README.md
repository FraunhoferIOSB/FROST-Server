# FROST-Server.MCP

A [Model Context Protocol](https://modelcontextprotocol.io) (MCP) server for the
OGC SensorThings API. It exposes a FROST-Server instance — any FROST model, not just
Sensing — to LLM agents through eight generic tools and a small set of `frost://`
resources. Agents never see raw URLs or STA syntax; the tool descriptions guide them,
and the server validates, clamps, and maps everything onto the internal `Service`.

The module is a **stand-alone process like the MQTT frontend** (`FrostMcpServer`,
embedding Jetty) and boots from the same properties files as the other frontends.

Design document: [docs/DESIGN.md](docs/DESIGN.md).

## Tools

| Tool | Purpose |
|---|---|
| `frost_get` | Read any STA resource by path (entity, entity set, index). |
| `frost_query` | Query an entity set with the full STA query language (`$filter`, `$select`, `$expand`, `$orderby`, `$top`, `$skip`, `$count`) — passes the parts as arguments, no query-string building. |
| `frost_create` | Create entities (deep inserts supported). |
| `frost_update` | Patch entities; `mode=changeset` wraps a multi-property change in one changeset. |
| `frost_replace` | Replace an entity. |
| `frost_delete` | Delete by fully-qualified id path (`Things(1)`). |
| `frost_subscribe` | Subscribe the session to entity-change notifications — an entity set (optionally with `$filter`/`$select`) or a single entity (also via navigation, e.g. `Things(1)/Datastreams`). Notifications arrive as MCP logging notifications (logger `frost`, level INFO) carrying `subscriptionId`, `eventType`, `entityType` and the entity. |
| `frost_unsubscribe` | Remove one subscription (by `subscriptionId`) or all subscriptions of the session. |

All tools accept an optional `version` arg (defaults to `mcp.defaultVersion`) so one
server can serve multiple STA API versions if the deployed model defines them.
Results are plain data in `structuredContent` — no `outputSchema` is advertised.
Subscriptions survive per session only; identical repeated `frost_subscribe` calls
return the existing subscription id, and sessions are limited to
`mcp.maxSubscriptionsPerSession` (default 16) active subscriptions.

## Resources (frost://…

| URI | Content |
|---|---|
| `frost://index` | The STA index document. |
| `frost://model` | Model overview: entity types with key, property and navigation summaries. |
| `frost://model/{entitySet}` | Per-entity-type detail document (properties, navigations, `Edm.Untyped` free-form annotations). |
| `frost://sta/{path}` | Any STA resource, e.g. `frost://sta/Things(1)`. |

## Settings

| Property | Default | Meaning |
|---|---|---|
| `mcp.host` | `0.0.0.0` | Jetty bind address. |
| `mcp.port` | `8080` | Jetty port for `/mcp`. |
| `mcp.defaultVersion` | (none → model's first) | STA version used when a tool call omits `version`. |
| `mcp.maxTop` | `100` | `$top` clamp; implicit top when a query specifies none. |
| `mcp.maxResultSize` | `1048576` | Result byte cap. Over-size answers return a structured `statusCode: 413` with guidance to narrow the query. |
| `mcp.allowTools` | `get,query,create,update,replace,delete,subscribe,unsubscribe` | Tool ACL (subset/expand as needed; e.g. read-only deployments: `allowTools=get,query`). |
| `mcp.auth.requireAuthenticated` | `false` | Fail boot if no known auth provider could be resolved for `/mcp`. |
| `mcp.exposedEndpoints` | (empty) | Comma-separated MCP endpoint URLs advertised in the index document `serverSettings` (next to the MQTT endpoints). Empty: not advertised. |
| `mcp.maxRequestSize` | `16777216` | Byte cap on a single MCP request (JSON-RPC message); larger requests are rejected. |
| `mcp.keepAliveIntervalSec` | `1800` | Transport keep-alive interval (seconds) for long-lived listening streams. |
| `mcp.sessionSweepIntervalSec` | `1800` | Interval (seconds) at which stale MCP sessions are swept. |
| `mcp.maxSubscriptionsPerSession` | `16` | Per-session cap on active subscriptions; beyond it `frost_subscribe` answers `429`. |

## Deployment

Create a config file (default `FrostMcp.properties`) next to a `FrostMcpServer`
distribution, e.g.:

```properties
mcp.port=8090
persistence.persistenceManagerImplementationClass=de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager
persistence.db.url=jdbc:postgresql://localhost:5432/sensorthings
persistence.db.username=postgres
persistence.db.password=changeMe
auth.provider=de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthFilter
auth.allowAnonymousRead=false
```

A `MessageBus` (default: the in-JVM `InternalMessageBus`) is always started, since
the persistence layer fires entity-change events on every commit.

Then run:

```bash
java -jar FROST-Server.MCP.jar
```

### Docker image and release jar

CI publishes the module like the other frontends: the runnable
`FROST-Server.MCP-<version>-jar-with-dependencies.jar` is attached to GitHub releases
(and uploaded as a workflow artifact), and a Docker image is pushed to DockerHub as
`fraunhoferiosb/frost-server-mcp` (tags `latest` + version on the `v2.x` branch,
`develop-<branch>-<version>` on develop branches, full/short version tags on releases).

```bash
docker run -d --name frost-mcp \
    -p 8080:8080 \
    -v $(pwd)/FrostMcp.properties:/usr/local/FROST/FrostMcp.properties \
    fraunhoferiosb/frost-server-mcp:latest
```

The image exposes `mcp.port` (8080) and, when enabled with
`metrics.useInternalHttpServer`, the internal Prometheus endpoint (9400).
Mount the config file next to the jar as shown; since the image has no nested
configuration, all settings come from `FrostMcp.properties` (path is optionally the
first program argument — adjust the CMD/entrypoint if you put it elsewhere).

## Client example

Any MCP host (Claude Desktop, IDEs, etc. via Streamable HTTP at
`http://host:8080/mcp`), or programmatically with the [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
(as used by `McpSmokeTest`):

```java
McpSyncClient client = McpClient.sync(
        HttpClientStreamableHttpTransport.builder("http://localhost:8080/mcp")
                .clientInfo(new McpSchema.Implementation("my-client", "1.0.0"))
                .build())
        .build();

client.initialize();

CallToolResult result = client.callTool(
        new CallToolRequest("frost_query", Map.of(
                "entitySet", "Things",
                "filter", "name eq 'Kitchen'",
                "top", 3)));
System.out.println(result.structuredContent());
```

## Tests

In-module unit tests (52) cover settings, JSON handling, tool handling (version
resolution, ACLs, permissions, query validation, result formation, byte cap),
query assembly, the resource generators, and the subscription registry — against a
compact local fake model (`TestModelMcp`). The version-dependent selfLink name in
`$select` (`@iot.selfLink`, `@id`, `@odata.id`) is pinned by round-trip tests over
all four API versions. Integration tests live in
`FROST-Server.Tests` (`McpSmokeTest`, including a subscribe → notify → unsubscribe
round-trip).

```bash
mvn -pl FROST-Server.MCP test
```

## License

LGPL-3.0-or-later (see the headers of the individual files).
