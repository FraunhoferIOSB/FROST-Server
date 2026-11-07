---
layout: default
title: MCP Server
category: extensions
order: 20
---

# MCP Interface

The MCP package adds a [Model Context Protocol](https://modelcontextprotocol.io) (MCP)
server to the FROST-Server. MCP is the protocol used by LLM agents (AI assistants,
IDE agents, chat clients) to interact with external systems. The MCP interface exposes
a FROST-Server instance — any FROST data model, not just SensorThings Sensing — to
such agents, so they can query and manage sensor data in natural language.

Since the MCP interface is a server on its own, it makes no changes to the
SensorThings API or OData interfaces: agents build plain STA requests through the
MCP tools, and the MCP layer maps those onto the same core `Service` as the HTTP
interface.

The interface is not part of the SensorThings API standard, nor of the OData standard.

## Running the MCP server

The MCP server is a **stand-alone package**, like the MQTT package: a runnable jar with
embedded Jetty, and a docker image. It reads its settings from `FrostMcp.properties` or environment variables
(see [settings](../settings/settings.md)), boots the FROST core and a message bus
exactly like the other front-ends, and serves MCP at `http://host:port/mcp`
(Streamable HTTP transport).

Docker image: [fraunhoferiosb/frost-server-mcp](https://hub.docker.com/r/fraunhoferiosb/frost-server-mcp/)

```bash
docker run -d --name frost-mcp \
    -p 8080:8080 \
    -v $(pwd)/FrostMcp.properties:/usr/local/FROST/FrostMcp.properties \
    fraunhoferiosb/frost-server-mcp:latest
```

A minimal `FrostMcp.properties`:

```properties
mcp.port=8080
persistence.persistenceManagerImplementationClass=de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager
persistence.db.url=jdbc:postgresql://database:5432/sensorthings
persistence.db.username=sensorthings
persistence.db.password=changeMe
```

The MCP server can be added next to an existing instance sharing the same database.

## Tools

Agents interact with the server through six tools, which cover the full SensorThings
API path and query language:

| Tool | Purpose |
|---|---|
| `frost_get` | Read any resource by path (entity, entity set, index, property, `$value`). |
| `frost_query` | Query an entity set with the full STA query language (`$filter`, `$select`, `$expand`, `$orderby`, `$top`, `$skip`, `$count`), passing the query parts as arguments. |
| `frost_create` | Create entities (deep inserts supported). |
| `frost_update` | Patch entities: `mode=patch` sends the changed properties, `mode=changeset` sends an RFC 6902 JSON Patch. |
| `frost_replace` | Replace an entity (PUT semantics). |
| `frost_delete` | Delete an entity by id path, e.g. `Things(1)`. |

All tools accept an optional `version` argument for servers that serve multiple API
versions, so one MCP server can serve all versions at once.
Results are STA JSON, delivered both as pretty-printed text and as structured content,
so both simple and capable clients can use them.

## Resources

MCP resources give the agent orientation about the server and its data model:

| URI | Content |
|---|---|
| `frost://index` | The STA index document, as served on `/v1.1`. |
| `frost://model` | The complete model: all entity types, with key, property and navigation summaries. |
| `frost://model/{entitySet}` | Details for one entity set: properties, navigation properties, key. |
| `frost://sta/{path}` | Any STA resource, e.g. `frost://sta/Things(1)`, with the MCP limits applied. |

Since the FROST data model is pluggable, the model resources are generated at server
start-up from the actual model in the database, and are not limited to the standard
Sensing entities.

## Security

The MCP server uses the same authentication providers as the HTTP server: it can run
with Basic authentication or Keycloak/OIDC. Sessions are authenticated on the HTTP
request, and the MCP server authorises each tool call with the same roles and
fine-grained rules (per user, entity type and property) as the HTTP interface.
Read-only and write roles apply as usual, and a read-only MCP server can be
configured with the `mcp.allowTools` setting.

## Settings

The MCP server has the following settings, in the `mcp.` group:

* **mcp.host:** Default: `0.0.0.0`
  The address the MCP server binds on.
* **mcp.port:** Default: `8080`
  The port the MCP server listens on. The endpoint is served at `http://host:port/mcp`.
* **mcp.defaultVersion:** Default: *first available version*
  The API version used when a tool call does not specify a version.
* **mcp.maxTop:** Default: `100`
  The maximum page size, applied to all queries. Larger results are paged with
  `@iot.nextLink`, so agents can fetch the rest with follow-up calls.
* **mcp.maxResultSize:** Default: `1048576` (1 MB)
  The maximum result size, applied after paging. Larger answers are refused with a
  message instructing the agent to narrow the query.
* **mcp.allowTools:** Default: `get,query,create,update,replace,delete`
  Comma-separated list of enabled tools. A read-only MCP server, for example, uses
  `get,query`. Tools not in the list are not advertised to clients.
* **mcp.auth.requireAuthenticated:** Default: `false`
  When true, anonymous MCP sessions are rejected: clients must authenticate with the
  configured authentication provider.
* **mcp.exposedEndpoints:** Default: *empty*
  Comma-separated list of MCP endpoint URLs to advertise on the index document
  (landing page) of the HTTP server, in the `serverSettings` next to the MQTT
  endpoints. Since the MCP server usually runs as a separate process with its own
  address, this list must be configured explicitly; when it is empty the MCP
  endpoint is not advertised.
* **mcp.maxRequestSize:** Default: `16777216` (16 MB)
  The maximum size in bytes of a single MCP request (JSON-RPC message) the server
  accepts; larger requests are rejected.
* **mcp.keepAliveIntervalSec:** Default: `1800` (30 minutes)
  The interval in seconds of the transport keep-alive signal, relevant for
  long-lived listening streams behind proxies.
* **mcp.sessionSweepIntervalSec:** Default: `1800` (30 minutes)
  The interval in seconds at which the transport removes stale MCP sessions.
