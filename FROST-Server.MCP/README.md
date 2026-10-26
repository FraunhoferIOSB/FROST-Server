# FROST-Server.MCP

A [Model Context Protocol](https://modelcontextprotocol.io) (MCP) server for the
OGC SensorThings API. It exposes a FROST-Server instance — any FROST model, not just
Sensing — to LLM agents through six generic tools and a small set of `frost://`
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

All tools accept an optional `version` arg (defaults to `mcp.defaultVersion`) so one
server can serve multiple STA API versions if the deployed model defines them.
Results are plain data in `structuredContent` — no `outputSchema` is advertised.

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
| `mcp.enable` | `true` | Start the MCP server. |
| `mcp.host` | `0.0.0.0` | Jetty bind address. |
| `mcp.port` | `8080` | Jetty port for `/mcp`. |
| `mcp.defaultVersion` | (none → model's first) | STA version used when a tool call omits `version`. |
| `mcp.maxTop` | `100` | `$top` clamp; implicit top when a query specifies none. |
| `mcp.maxResultSize` | `1048576` | Result byte cap. Over-size answers return a structured `statusCode: 413` with guidance to narrow the query. |
| `mcp.count` | `true` | Force `$count=true` on all queries. |
| `mcp.allowTools` | `get,query,create,update,replace,delete` | Tool ACL (subset/expand as needed; e.g. read-only deployments: `allowTools=get,query`). |
| `mcp.auth.requireAuthenticated` | `false` | Fail boot if no known auth provider could be resolved for `/mcp`. |
| `mcp.startBus` | `false` | Start an internal message bus in this process (needed when writes are enabled on a stand-alone MCP server; see below). |

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
mcp.startBus=true
```

For **write-capable** stand-alone deployments set `mcp.startBus=true` (starts the
in-JVM `InternalMessageBus` the persistence layer needs to fire entity-change events).

Then run:

```bash
java -jar FROST-Server.MCP.jar
```

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

In-module unit tests (35) cover settings, JSON handling, tool handling (version
resolution, ACLs, permissions, query validation, result formation, byte cap),
query assembly, and the resource generators — against a compact local fake model
(`TestModelMcp`). Integration tests live in `FROST-Server.Tests` (`McpSmokeTest`).

```bash
mvn -pl FROST-Server.MCP test
```

## License

LGPL-3.0-or-later (see the headers of the individual files).
