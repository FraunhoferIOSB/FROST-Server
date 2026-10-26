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
package de.fraunhofer.iosb.ilt.frostserver.mcp.resource;

import de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler;
import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The frost:// resources: the STA index document, the generated model
 * overview, per-entity-type model details and live STA reads. Resource reads
 * execute with the session principal's rights, exactly like tools; error
 * outcomes and over-size results surface as JSON error documents in the
 * resource text.
 */
public class FrostResourceHandler {

    /**
     * URI of the index resource.
     */
    public static final String URI_INDEX = "frost://index";
    /**
     * URI of the model resource.
     */
    public static final String URI_MODEL = "frost://model";
    /**
     * Template URI of the per-entity-type model resource.
     */
    public static final String URI_TEMPLATE_MODEL_SET = "frost://model/{entitySet}";
    /**
     * Template URI of the live STA read resource.
     */
    public static final String URI_TEMPLATE_STA = "frost://sta/{path}";
    /**
     * Prefix of the live STA read resource URIs.
     */
    public static final String URI_PREFIX_STA = "frost://sta/";

    private static final String MIME_JSON = "application/json";

    private final FrostToolHandler handler;
    private final ModelResourceGenerator modelGenerator;
    private final Map<String, String> modelCache = new ConcurrentHashMap<>();

    public FrostResourceHandler(FrostToolHandler handler) {
        this.handler = handler;
        this.modelGenerator = new ModelResourceGenerator(handler.getModelRegistry());
    }

    /**
     * Builds the static frost://index registration.
     *
     * @return the sync resource specification.
     */
    public McpServerFeatures.SyncResourceSpecification indexSpecification() {
        final McpSchema.Resource resource = McpSchema.Resource.builder()
                .uri(URI_INDEX)
                .name("STA index document")
                .description("The SensorThings API index document: entity set URLs, server settings, conformance list.")
                .mimeType(MIME_JSON)
                .build();
        return new McpServerFeatures.SyncResourceSpecification(resource, this::readIndex);
    }

    /**
     * Builds the static frost://model registration.
     *
     * @return the sync resource specification.
     */
    public McpServerFeatures.SyncResourceSpecification modelSpecification() {
        final McpSchema.Resource resource = McpSchema.Resource.builder()
                .uri(URI_MODEL)
                .name("STA model overview")
                .description("The generated model overview: entity types with properties and navigation properties.")
                .mimeType(MIME_JSON)
                .build();
        return new McpServerFeatures.SyncResourceSpecification(resource, this::readModel);
    }

    /**
     * Builds the frost://model/{entitySet} template registration.
     *
     * @return the sync resource template specification.
     */
    public McpServerFeatures.SyncResourceTemplateSpecification modelSetSpecification() {
        final McpSchema.ResourceTemplate template = McpSchema.ResourceTemplate.builder()
                .uriTemplate(URI_TEMPLATE_MODEL_SET)
                .name("STA entity model")
                .description("Model detail for one entity set: properties, navigation properties, key.")
                .mimeType(MIME_JSON)
                .build();
        return new McpServerFeatures.SyncResourceTemplateSpecification(template, this::readModelSet);
    }

    /**
     * Builds the frost://sta/{path} template registration.
     *
     * @return the sync resource template specification.
     */
    public McpServerFeatures.SyncResourceTemplateSpecification staSpecification() {
        final McpSchema.ResourceTemplate template = McpSchema.ResourceTemplate.builder()
                .uriTemplate(URI_TEMPLATE_STA)
                .name("Live STA resource")
                .description("Live read of a STA path with the MCP paging caps applied, e.g. frost://sta/Things(1).")
                .mimeType(MIME_JSON)
                .build();
        return new McpServerFeatures.SyncResourceTemplateSpecification(template, this::readSta);
    }

    private McpSchema.ReadResourceResult readIndex(McpSyncServerExchange exchange, McpSchema.ReadResourceRequest request) {
        final String json = executeStaRead(exchange, "", null);
        return textResult(request.uri(), json);
    }

    private McpSchema.ReadResourceResult readModel(McpSyncServerExchange exchange, McpSchema.ReadResourceRequest request) {
        final PrincipalExtended principal = handler.principalFrom(exchange);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return textResult(request.uri(), errorJson(readGate,
                    readGate == 401 ? "Anonymous access is not allowed." : "Read permission denied."));
        }
        final String json = modelCache.computeIfAbsent(URI_MODEL, key -> modelGenerator.generateModel());
        return textResult(request.uri(), json);
    }

    private McpSchema.ReadResourceResult readModelSet(McpSyncServerExchange exchange, McpSchema.ReadResourceRequest request) {
        final String entitySet = extractEntitySet(request.uri());
        if (entitySet == null) {
            throw McpError.RESOURCE_NOT_FOUND.apply(request.uri());
        }
        final PrincipalExtended principal = handler.principalFrom(exchange);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return textResult(request.uri(), errorJson(readGate,
                    readGate == 401 ? "Anonymous access is not allowed." : "Read permission denied."));
        }
        final String json = modelGenerator.generateEntityType(entitySet);
        if (json == null) {
            throw McpError.RESOURCE_NOT_FOUND.apply(request.uri());
        }
        return textResult(request.uri(), json);
    }

    private McpSchema.ReadResourceResult readSta(McpSyncServerExchange exchange, McpSchema.ReadResourceRequest request) {
        final String staPath = extractStaPath(request.uri());
        if (staPath == null) {
            throw McpError.RESOURCE_NOT_FOUND.apply(request.uri());
        }
        final String[] pathAndQuery = splitStaTarget(staPath);
        final String json = executeStaRead(exchange, pathAndQuery[0], pathAndQuery[1]);
        return textResult(request.uri(), json);
    }

    /**
     * Splits a frost://sta/ target into its STA path and query parts. The
     * template URI may carry a query part, e.g. frost://sta/Things?$top=1 —
     * it is split at the first '?'. Package-private for unit testing.
     *
     * @param staPath The target under frost://sta/, after the prefix.
     * @return a two-element array {path, query}; the query is null when
     * absent.
     */
    static String[] splitStaTarget(String staPath) {
        final int queryStart = staPath.indexOf('?');
        if (queryStart < 0) {
            return new String[]{staPath, null};
        }
        return new String[]{staPath.substring(0, queryStart), staPath.substring(queryStart + 1)};
    }

    /**
     * Extracts the {path} value from a frost://sta/{...} URI. The SDK already
     * resolved the template into a concrete URI; the path may contain further
     * slashes.
     *
     * @param uri The concrete URI read.
     * @return the STA path, or null when the URI is not under frost://sta/.
     */
    private String extractStaPath(String uri) {
        if (uri == null || !uri.startsWith(URI_PREFIX_STA)) {
            return null;
        }
        return uri.substring(URI_PREFIX_STA.length());
    }

    /**
     * Extracts the {entitySet} value from a frost://model/{...} URI.
     *
     * @param uri The concrete URI read.
     * @return the entity set name, or null when the URI is not under
     * frost://model/ or the name carries further slashes.
     */
    private String extractEntitySet(String uri) {
        if (uri == null || !uri.startsWith(URI_MODEL + '/')) {
            return null;
        }
        final String value = uri.substring(URI_MODEL.length() + 1);
        if (value.isEmpty() || value.contains("/")) {
            return null;
        }
        return value;
    }

    /**
     * Runs a STA read with the session principal's rights and the MCP caps
     * applied, exactly like frost_get. Non-2xx outcomes and over-size results
     * become JSON error documents instead of a raw STA payload.
     *
     * @param exchange The MCP server exchange carrying the principal.
     * @param staPath The STA path, relative to the version root. Empty string
     * reads the index document.
     * @param decodedQuery Optional raw query options, without leading '?' and
     * not URL-encoded. May be null.
     * @return the STA JSON (or error JSON) text.
     */
    private String executeStaRead(McpSyncServerExchange exchange, String staPath, String decodedQuery) {
        final PrincipalExtended principal = handler.principalFrom(exchange);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return errorJson(readGate, readGate == 401 ? "Anonymous access is not allowed." : "Read permission denied.");
        }
        final Version version = handler.resolveVersion(null);
        final var queryDefaults = handler.getCoreSettings().getQueryDefaults().copy();
        final var context = handler.buildContext(queryDefaults, version);
        final var query = handler.parseAndValidateQuery(context, version, principal, staPath, decodedQuery);
        final String urlQuery = query == null ? null : handler.queryStringFrom(query);
        final ServiceRequest serviceRequest = new ServiceRequest()
                .setContext(context)
                .setVersion(version)
                .setRequestType(RequestTypeUtils.READ)
                .setUrlPath(staPath == null ? "" : staPath)
                .setUrlQuery(urlQuery)
                .setParameterMap(handler.parameterMapFrom(urlQuery))
                .setUserPrincipal(principal);
        final ServiceResponseDefault response = handler.execute(serviceRequest);
        final String formatted = response.getFormattedResult();
        if (!response.isSuccessful()) {
            final var parsed = StaJsonHelper.parseOrNull(formatted);
            if (parsed != null && parsed.isObject()) {
                return formatted;
            }
            return errorJson(response.getCode(), StringHelper.isNullOrEmpty(response.getMessage())
                    ? "Request failed with status " + response.getCode() + "."
                    : response.getMessage());
        }
        if (formatted.getBytes(StandardCharsets.UTF_8).length > handler.getMcpSettings().getMaxResultSize()) {
            return errorJson(413, "Result too large for MCP; use $select/$expand to narrow the query and $top to page.");
        }
        return formatted;
    }

    private String errorJson(int statusCode, String message) {
        return StaJsonHelper.writeValueAsString(FrostToolHandler.errorObject(statusCode, message));
    }

    private McpSchema.ReadResourceResult textResult(String uri, String text) {
        return new McpSchema.ReadResourceResult(List.of(
                new TextResourceContents(uri, MIME_JSON, text)));
    }

}
