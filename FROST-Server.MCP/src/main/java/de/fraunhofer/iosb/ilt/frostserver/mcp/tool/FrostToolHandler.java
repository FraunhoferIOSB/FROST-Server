/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.mcp.tool;

import de.fraunhofer.iosb.ilt.frostserver.mcp.McpSettings;
import de.fraunhofer.iosb.ilt.frostserver.mcp.session.PrincipalExtractor;
import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.snapshots.Unit;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Shared execution skeleton for the FROST MCP tools: resolves the STA version,
 * applies the read gate, builds and validates the ServiceRequest, executes it
 * against the FROST Service and maps the outcome to an MCP CallToolResult,
 * including caps (paging, byte size) and metrics/audit logging.
 */
public class FrostToolHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrostToolHandler.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("de.fraunhofer.iosb.ilt.frostserver.mcp.audit");

    private static final String LABEL_TOOL = "tool";
    private static final String LABEL_OUTCOME = "outcome";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_CLIENT_ERROR = "clientError";
    private static final String OUTCOME_AUTH_ERROR = "authError";
    private static final String OUTCOME_SERVER_ERROR = "serverError";
    private static final String OUTCOME_TOO_LARGE = "tooLarge";

    private static final Histogram TOOL_DURATION = Histogram.builder()
            .name("frost_mcp_tool_duration_seconds")
            .help("MCP tool call duration in seconds")
            .unit(Unit.SECONDS)
            .labelNames(LABEL_TOOL)
            .register();
    private static final Counter TOOL_CALLS = Counter.builder()
            .name("frost_mcp_tool_calls_total")
            .help("MCP tool call counts per outcome")
            .labelNames(LABEL_TOOL, LABEL_OUTCOME)
            .register();

    private final CoreSettings coreSettings;
    private final McpSettings mcpSettings;
    private final ModelRegistry modelRegistry;
    private final String roleRead;
    private final String roleAdmin;

    public FrostToolHandler(CoreSettings coreSettings, McpSettings mcpSettings) {
        this.coreSettings = coreSettings;
        this.mcpSettings = mcpSettings;
        this.modelRegistry = coreSettings.getModelRegistry();
        final Map<AuthUtils.Role, String> roleMapping = AuthUtils.loadRoleMapping(coreSettings.getAuthSettings());
        this.roleRead = roleMapping.get(AuthUtils.Role.READ);
        this.roleAdmin = roleMapping.get(AuthUtils.Role.ADMIN);
    }

    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    public McpSettings getMcpSettings() {
        return mcpSettings;
    }

    public ModelRegistry getModelRegistry() {
        return modelRegistry;
    }

    /**
     * The entity-set names (plurals) the model exposes to non-admin users, in
     * registry order. Used for the frost_query enum and tool descriptions.
     *
     * @return the non-admin entity set names.
     */
    public List<String> entitySetNames() {
        return modelRegistry.getEntityTypes().stream()
                .map(type -> type.plural)
                .collect(Collectors.toList());
    }

    /**
     * A short cheat-sheet naming the available entity sets, embedded into the
     * tool descriptions.
     *
     * @return the entity set list as description fragment.
     */
    public String entitySetCheatSheet() {
        return "Available entity sets: " + String.join(", ", entitySetNames()) + ".";
    }

    /**
     * Guards a tool call against the mcp.allowTools configuration.
     *
     * @param toolName The tool being called.
     * @throws McpError (-32601) when the tool is not enabled.
     */
    public void requireToolAllowed(String toolName) {
        if (!mcpSettings.isToolAllowed(toolName)) {
            throw methodNotFound("Tool '" + toolName + "' is not enabled on this server (mcp.allowTools).");
        }
    }

    /**
     * Builds an isError tool result for a denied read gate, routed through the
     * normal result mapping so metrics and audit logging apply.
     *
     * @param toolName The tool name, for metrics/audit.
     * @param path The STA path, for audit logging.
     * @param principal The denied principal, for audit logging.
     * @param versionName The version name, for audit logging.
     * @param code 401 or 403.
     * @return the MCP tool result.
     */
    public McpSchema.CallToolResult readDeniedResult(String toolName, String path, PrincipalExtended principal, String versionName, int code) {
        final ServiceResponseDefault response = new ServiceResponseDefault(
                code, code == 401
                        ? "Anonymous access is not allowed."
                        : "Read permission denied.");
        return toCallToolResult(toolName, path, principal, versionName, response);
    }

    /**
     * Extracts the principal carried by the exchange's transport context.
     *
     * @param exchange The MCP server exchange. May be null.
     * @return the FROST principal, never null.
     */
    public PrincipalExtended principalFrom(McpSyncServerExchange exchange) {
        if (exchange == null) {
            return PrincipalExtended.ANONYMOUS_PRINCIPAL;
        }
        return principalFrom(exchange.transportContext());
    }

    /**
     * Extracts the principal carried by the transport context.
     *
     * @param context The MCP transport context. May be null.
     * @return the FROST principal, never null.
     */
    public PrincipalExtended principalFrom(McpTransportContext context) {
        return PrincipalExtractor.principalFrom(context);
    }

    /**
     * Resolves the STA version for a tool call: explicit name from args, else
     * the configured default, else the first registered version.
     *
     * @param versionName The version name from the tool arguments. May be null
     * or empty.
     * @return the resolved Version.
     * @throws McpError (-32602) if the name is not a registered version.
     */
    public Version resolveVersion(String versionName) {
        final PluginManager pluginManager = coreSettings.getPluginManager();
        if (!StringHelper.isNullOrEmpty(versionName)) {
            final Version version = pluginManager.getVersion(versionName);
            if (version == null) {
                throw invalidParams("Unknown STA version '" + versionName + "'. Known versions: "
                        + String.join(", ", pluginManager.getVersions().keySet()) + ".");
            }
            return version;
        }
        final String configuredDefault = mcpSettings.getDefaultVersionName();
        if (!StringHelper.isNullOrEmpty(configuredDefault)) {
            final Version version = pluginManager.getVersion(configuredDefault);
            if (version == null) {
                throw invalidParams("Configured default STA version '" + configuredDefault + "' is not a known version.");
            }
            return version;
        }
        return pluginManager.getVersions().values().iterator().next();
    }

    /**
     * The coarse read gate, mirroring the MQTT adapter: the principal must
     * have the read or admin role; anonymous principals are only allowed when
     * anonymous read is enabled in the settings.
     *
     * @param principal The principal making the call.
     * @return 0 when allowed, 401 when anonymous and not allowed, 403 when a
     * named user lacks the read role.
     */
    public int checkCanRead(PrincipalExtended principal) {
        if (principal.hasRole(roleRead) || principal.hasRole(roleAdmin) || principal.isAdmin()) {
            return 0;
        }
        if (isAnonymous(principal)
                && coreSettings.getAuthSettings().getBoolean(CoreSettings.TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class)) {
            return 0;
        }
        return isAnonymous(principal) ? 401 : 403;
    }

    private static boolean isAnonymous(PrincipalExtended principal) {
        return principal == PrincipalExtended.ANONYMOUS_PRINCIPAL
                || principal.getName() == null
                || principal.getName().isEmpty()
                || PrincipalExtended.USER_NAME_ANONYMOUS.equals(principal.getName());
    }

    /**
     * Builds a ServiceContext mirroring HttpRequestDecoder, with the version's
     * URL prefix generator.
     *
     * @param queryDefaults The per-request query defaults (a copy).
     * @param version The resolved version.
     * @return the ServiceContext.
     */
    public ServiceContext buildContext(QueryDefaults queryDefaults, Version version) {
        return new ServiceContext()
                .setFunctionRegistry(coreSettings.getFunctionRegistry())
                .setModelRegistry(modelRegistry)
                .setQueryDefaults(queryDefaults)
                .setPrefixGen(() -> queryDefaults.getServiceRootUrl() + '/' + version.urlPart + '/');
    }

    /**
     * Validates path and query with the STA parsers, applies the MCP paging and
     * count caps to entity-set reads. Parse failures surface as MCP protocol
     * errors (-32602). An empty path (index document) is not parsed at all —
     * the Service answers it before parsing — and returns null so callers pass
     * no query.
     *
     * @param context The service context.
     * @param version The resolved version.
     * @param principal The principal of the session, for admin-only paths.
     * @param staPath The STA path, relative to the version root. May be null
     * or empty.
     * @param decodedQuery The query string, without leading '?' and decoded.
     * May be null or empty.
     * @return the validated query with caps applied, or null for the index
     * document.
     * @throws McpError (-32602) on parse or validation failure.
     */
    public Query parseAndValidateQuery(ServiceContext context, Version version, PrincipalExtended principal, String staPath, String decodedQuery) {
        if (StringHelper.isNullOrEmpty(staPath) || staPath.equals("/")) {
            return null;
        }
        final ResourcePath path;
        try {
            path = PathParser.parsePath(context, version, staPath, principal);
        } catch (RuntimeException ex) {
            throw invalidParams("Invalid STA path: " + ex.getMessage());
        }
        Query query;
        try {
            query = QueryParser.parseQuery(decodedQuery, context, path, principal).validate();
        } catch (RuntimeException ex) {
            throw invalidParams("Invalid STA query: " + ex.getMessage());
        }
        applyReadCaps(query, path);
        return query;
    }

    /**
     * Applies the MCP caps to a read query: $top is clamped to mcp.maxTop
     * (implicit top for entity sets), and $count is forced on entity sets
     * unless already present, when configured.
     *
     * @param query The parsed query.
     * @param path The parsed path.
     */
    private void applyReadCaps(Query query, ResourcePath path) {
        final int maxTop = mcpSettings.getMaxTop();
        if (path.getMainElement() instanceof PathElementEntitySet) {
            final Optional<Integer> top = query.getTop();
            if (top.isEmpty()) {
                query.setTop(maxTop);
            } else if (top.get() > maxTop) {
                query.setTop(maxTop);
            }
            if (mcpSettings.isCount() && query.getCount().isEmpty()) {
                query.setCount(true);
            }
        }
    }

    /**
     * Re-joins the validated query into a decoded query string, via
     * Query.toUnencodedString: the string is later re-parsed by the Service
     * layer from the already-decoded urlQuery field (matching the HTTP layer's
     * urlDecode contract), so it must not be URL-encoded. Returns null when
     * the query carries no options.
     *
     * @param query The validated query with caps applied. May not be null.
     * @return the decoded query string, or null when empty.
     */
    public String queryStringFrom(Query query) {
        final String queryString = query.toUnencodedString(false);
        return queryString.isEmpty() ? null : queryString;
    }

    /**
     * Builds the parameter map for the query string, using UrlHelper's
     * split-contract (keys like $filter, $top, values not URL-encoded here —
     * they are already decoded).
     *
     * @param decodedQuery The decoded query string. May be null.
     * @return the parameter map, never null.
     */
    public Map<String, SequencedSet<String>> parameterMapFrom(String decodedQuery) {
        if (StringHelper.isNullOrEmpty(decodedQuery)) {
            return new LinkedHashMap<>();
        }
        final Map<String, SequencedSet<String>> map = new LinkedHashMap<>();
        for (final String part : decodedQuery.split("&")) {
            if (part.isBlank()) {
                continue;
            }
            final int idx = part.indexOf('=');
            final String key = idx > 0 ? part.substring(0, idx) : part;
            final String value = idx > 0 && part.length() > idx + 1 ? part.substring(idx + 1) : null;
            map.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(value);
        }
        return map;
    }

    /**
     * Executes a fully-built ServiceRequest against the FROST Service, in the
     * exact pattern of ServletMain.executeService. The local request (with its
     * principal) is installed for the duration of the call so the persistence
     * layer's security validators see it.
     *
     * @param serviceRequest The request to execute. May not be null.
     * @return the service response.
     */
    public ServiceResponseDefault execute(ServiceRequest serviceRequest) {
        try (Service service = new Service(coreSettings)) {
            ServiceRequest.setLocalRequest(serviceRequest);
            try {
                final ServiceResponseDefault response = new ServiceResponseDefault();
                service.distributeRequest(serviceRequest, response);
                return response;
            } finally {
                ServiceRequest.removeLocalRequest();
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to execute MCP service request.", ex);
            final ServiceResponseDefault response = new ServiceResponseDefault();
            response.setCode(500);
            response.setMessage("Internal error: " + ex.getMessage());
            return response;
        }
    }

    /**
     * Maps the service response and formatted result to an MCP CallToolResult:
     * success (2xx) becomes structuredContent + pretty text; 4xx/5xx becomes
     * isError with a statusCode/message object; over-size results become a 413
     * guidance error. Metrics and audit logging are applied here too.
     *
     * @param toolName The tool name, for metrics/audit.
     * @param path The STA path, for audit logging.
     * @param principal The principal, for audit logging.
     * @param versionName The version name, for audit logging.
     * @param response The executed service response.
     * @return the MCP tool result.
     */
    public McpSchema.CallToolResult toCallToolResult(
            String toolName, String path, PrincipalExtended principal, String versionName,
            ServiceResponseDefault response) {
        final int code = response.getCode();
        final String formatted = response.getFormattedResult();
        String outcome = OUTCOME_SUCCESS;
        try (Timer timer = TOOL_DURATION.labelValues(toolName).startTimer()) {
            if (response.isSuccessful()) {
                final var parsed = StaJsonHelper.parseOrNull(formatted);
                final Object structured = StaJsonHelper.toStructuredContent(parsed);
                if (formatted.getBytes(StandardCharsets.UTF_8).length > mcpSettings.getMaxResultSize()) {
                    outcome = OUTCOME_TOO_LARGE;
                    return errorResult(tooLargeObject(parsed));
                }
                outcome = OUTCOME_SUCCESS;
                return McpSchema.CallToolResult.builder()
                        .structuredContent(structured)
                        .addTextContent(StaJsonHelper.writeValueAsPrettyString(structured))
                        .isError(false)
                        .build();
            }
            if (code == 401 || code == 403) {
                outcome = OUTCOME_AUTH_ERROR;
            } else if (code >= 500) {
                outcome = OUTCOME_SERVER_ERROR;
            } else {
                outcome = OUTCOME_CLIENT_ERROR;
            }
            // For error responses, Service.errorResponse puts the STA error
            // JSON ({"type","code","message"}) in both the message and the
            // formatted result; surface it verbatim as structuredContent.
            final JsonNode parsed = StaJsonHelper.parseOrNull(formatted);
            if (parsed != null && parsed.isObject()) {
                return errorResult(StaJsonHelper.toStructuredContent(parsed));
            }
            final String message = StringHelper.isNullOrEmpty(response.getMessage())
                    ? "Request failed with status " + code + "."
                    : response.getMessage();
            return errorResult(errorObject(code, message));
        } finally {
            TOOL_CALLS.labelValues(toolName, outcome).inc();
            auditLog(toolName, versionName, path, principal, code);
        }
    }

    private McpSchema.CallToolResult errorResult(Object structured) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(structured)
                .addTextContent(StaJsonHelper.writeValueAsPrettyString(structured))
                .isError(true)
                .build();
    }

    /**
     * Builds the structured error object used for tool results, matching the
     * mapping table in the design document: statusCode plus message.
     *
     * @param code The (HTTP-like) status code.
     * @param message The error message.
     * @return the error object.
     */
    public static Map<String, Object> errorObject(int code, String message) {
        final Map<String, Object> error = new LinkedHashMap<>();
        error.put("statusCode", code);
        error.put("message", message);
        return error;
    }

    private Map<String, Object> tooLargeObject(JsonNode parsed) {
        final Map<String, Object> guidance = new LinkedHashMap<>();
        guidance.put("statusCode", 413);
        guidance.put("message", "Result too large for MCP; use $select/$expand to narrow the query and $top to page.");
        if (parsed != null && parsed.isObject() && parsed.has("@iot.count")) {
            guidance.put("@iot.count", parsed.get("@iot.count"));
        }
        return guidance;
    }

    /**
     * Creates an MCP protocol error for invalid parameters (-32602), carrying
     * the parser message.
     *
     * @param message The message for the client.
     * @return the McpError to throw from the handler.
     */
    public static McpError invalidParams(String message) {
        return McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
                .message(message)
                .build();
    }

    /**
     * Creates an MCP protocol error for an unknown tool (-32601).
     *
     * @param message The message for the client.
     * @return the McpError to throw from the handler.
     */
    public static McpError methodNotFound(String message) {
        return McpError.builder(McpSchema.ErrorCodes.METHOD_NOT_FOUND)
                .message(message)
                .build();
    }

    private void auditLog(String toolName, String versionName, String path, PrincipalExtended principal, int statusCode) {
        if (AUDIT_LOG.isInfoEnabled()) {
            AUDIT_LOG.info("tool={} version={} path={} user={} status={}",
                    toolName, versionName, StringHelper.cleanForLogging(Objects.toString(path, "")),
                    principal == null ? "anonymous" : Objects.toString(principal.getName(), "anonymous"),
                    statusCode);
        }
    }

}
