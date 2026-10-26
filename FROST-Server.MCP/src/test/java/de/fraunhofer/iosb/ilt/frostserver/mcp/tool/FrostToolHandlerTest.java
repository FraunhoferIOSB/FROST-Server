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

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.mcp.McpSettings;
import de.fraunhofer.iosb.ilt.frostserver.mcp.TestModelMcp;
import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.DefaultFunctions;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for FrostToolHandler: version resolution, the read gate, tool
 * gating, query validation with MCP caps (maxTop clamping, implicit top,
 * forced count), query-string round-tripping and result mapping (success,
 * STA error re-keying, payload byte cap). The Service execution itself needs
 * persistence and stays covered by the Tests-module McpSmokeTest.
 */
class FrostToolHandlerTest {

    private static CoreSettings coreSettings;
    private static TestModelMcp testModel;
    private static FrostToolHandler handler;
    private static ServiceContext context;
    private static Version version;

    @BeforeAll
    static void beforeAll() {
        final Properties properties = new Properties();
        properties.put(McpSettings.PREFIX_MCP + McpSettings.TAG_MAX_TOP, "50");
        coreSettings = new CoreSettings();
        properties.forEach((key, value) -> coreSettings.getSettings().set(String.valueOf(key), String.valueOf(value)));
        DefaultFunctions.registerDefaultFunctions(coreSettings.getFunctionRegistry());
        coreSettings.getQueryDefaults()
                .setAlwaysOrder(false)
                .setUseAbsoluteNavigationLinks(false);
        testModel = new TestModelMcp();
        testModel.initModel(coreSettings.getModelRegistry(), VALUE_ID_TYPE_LONG);
        testModel.init(coreSettings);
        coreSettings.getModelRegistry().initFinalise();
        final McpSettings mcpSettings = new McpSettings(coreSettings.getSettings().getSubSettings(McpSettings.PREFIX_MCP));
        handler = new FrostToolHandler(coreSettings, mcpSettings);
        assertEquals(50, mcpSettings.getMaxTop());
        version = handler.resolveVersion(null);
        context = handler.buildContext(coreSettings.getQueryDefaults().copy(), version);
    }

    private static McpError expectInvalidParams(final Runnable call, final String nameFragment) {
        final McpError error = assertThrows(McpError.class, call::run, nameFragment);
        assertEquals(ErrorCodes.INVALID_PARAMS, error.getJsonRpcError().code(), nameFragment);
        return error;
    }

    @Test
    void testResolveVersion() {
        assertEquals(version, handler.resolveVersion(null), "Default resolves to the first/only registered version.");
        assertEquals(version, handler.resolveVersion(""));
        assertEquals(version, handler.resolveVersion(Version.INTERNAL.urlPart));
        expectInvalidParams(() -> handler.resolveVersion("v9.9"), "Unknown version must be -32602.");
    }

    @Test
    void testRequireToolAllowed() {
        handler.requireToolAllowed("frost_get");
        final McpError error = assertThrows(McpError.class, () -> handler.requireToolAllowed("frost_other"));
        assertEquals(ErrorCodes.METHOD_NOT_FOUND, error.getJsonRpcError().code());
    }

    @Test
    void testEntitySetNamesExcludeAdminOnly() {
        assertEquals(Set.of("Houses", "Rooms").size(), handler.entitySetNames().size());
        assertTrue(handler.entitySetNames().contains("Houses"));
        assertTrue(handler.entitySetNames().contains("Rooms"));
        assertFalse(handler.entitySetNames().contains("Safes"), "Admin-only entity sets must not be advertised.");
    }

    @Test
    void testCheckCanRead() {
        final PrincipalExtended anon = PrincipalExtended.ANONYMOUS_PRINCIPAL;
        final PrincipalExtended reader = new PrincipalExtended("reader", false, Set.of("read"));
        final PrincipalExtended named = new PrincipalExtended("named", false, Set.of());
        final PrincipalExtended admin = new PrincipalExtended("admin", true, Set.of());

        assertEquals(401, handler.checkCanRead(anon), "Anonymous read is off by default.");
        assertEquals(403, handler.checkCanRead(named), "Named user without the read role is denied.");
        assertEquals(0, handler.checkCanRead(reader));
        assertEquals(0, handler.checkCanRead(admin), "Admins can always read.");

        coreSettings.getAuthSettings().set(CoreSettings.TAG_AUTH_ALLOW_ANON_READ, true);
        assertEquals(0, handler.checkCanRead(anon), "Anonymous read allowed when configured.");
        coreSettings.getAuthSettings().set(CoreSettings.TAG_AUTH_ALLOW_ANON_READ, false);
    }

    @Test
    void testParseAndValidateQueryIndexPaths() {
        assertNull(handler.parseAndValidateQuery(context, version, PrincipalExtended.ANONYMOUS_PRINCIPAL, null, null));
        assertNull(handler.parseAndValidateQuery(context, version, PrincipalExtended.ANONYMOUS_PRINCIPAL, "", null));
        assertNull(handler.parseAndValidateQuery(context, version, PrincipalExtended.ANONYMOUS_PRINCIPAL, "/", null));
    }

    @Test
    void testParseAndValidateQueryCaps() {
        final PrincipalExtended principal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
        coreSettings.getAuthSettings().set(CoreSettings.TAG_AUTH_ALLOW_ANON_READ, true);

        // Entity sets get an implicit $top = maxTop ...
        Query query = handler.parseAndValidateQuery(context, version, principal, "Houses", null);
        assertEquals(50, query.getTop().orElse(-1));

        // ... $top above maxTop is clamped down ...
        query = handler.parseAndValidateQuery(context, version, principal, "Houses", "$top=500");
        assertEquals(50, query.getTop().orElse(-1));

        // ... $top below maxTop is kept ...
        query = handler.parseAndValidateQuery(context, version, principal, "Houses", "$top=5");
        assertEquals(5, query.getTop().orElse(-1));

        // ... and $count=true is forced on entity sets ...
        assertTrue(query.getCount().orElse(false), "mcp.count defaults to true and forces $count.");

        // ... unless the client sent one.
        query = handler.parseAndValidateQuery(context, version, principal, "Houses", "$count=false");
        assertFalse(query.getCount().orElse(true));

        // Single entities get no caps.
        query = handler.parseAndValidateQuery(context, version, principal, "Houses(1)", "$select=name");
        assertTrue(query.getTop().isEmpty(), "No implicit top on single entities.");
        assertTrue(query.getCount().isEmpty(), "No forced count on single entities.");

        coreSettings.getAuthSettings().set(CoreSettings.TAG_AUTH_ALLOW_ANON_READ, false);
    }

    @Test
    void testParseAndValidateQueryInvalids() {
        expectInvalidParams(
                () -> handler.parseAndValidateQuery(context, version, PrincipalExtended.ANONYMOUS_PRINCIPAL, "Nope", null),
                "Unknown entity set must be -32602.");
        expectInvalidParams(
                () -> handler.parseAndValidateQuery(context, version, PrincipalExtended.ANONYMOUS_PRINCIPAL, "Houses", "$filter=bad/syntax/("),
                "Invalid filter must be -32602.");
        expectInvalidParams(
                () -> handler.parseAndValidateQuery(context, version, PrincipalExtended.ANONYMOUS_PRINCIPAL, "Safes", null),
                "Admin-only paths are denied for anonymous principals.");
    }

    @Test
    void testQueryStringRoundTrip() {
        final PrincipalExtended principal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
        final Query query = handler.parseAndValidateQuery(
                context, version, principal, "Rooms", "$filter=name eq 'kitchen'&$select=name,value&$top=7");
        final String queryString = handler.queryStringFrom(query);
        assertNotNull(queryString);
        assertTrue(queryString.contains("$filter="), "Filter survives the round trip: " + queryString);
        assertTrue(queryString.contains("'kitchen'"), "Filter value survives the round trip: " + queryString);
        assertTrue(queryString.contains("$top=7"));
        assertTrue(queryString.contains("$select=name"), "Select survives the round trip: " + queryString);

        final Map<String, SequencedSet<String>> parameterMap = handler.parameterMapFrom(queryString);
        final String filterValue = parameterMap.get("$filter").iterator().next();
        assertTrue(filterValue.contains("name eq 'kitchen'"), "Filter value survives the round trip: " + filterValue);
        assertTrue(handler.parameterMapFrom(null).isEmpty());
        assertTrue(handler.parameterMapFrom("").isEmpty());
    }

    @Test
    void testToCallToolResultSuccess() {
        final ServiceResponseDefault response = new ServiceResponseDefault(200, "OK");
        response.setResult("rawFallback");
        final McpSchema.CallToolResult result = handler.toCallToolResult("frost_get", "Houses(1)", PrincipalExtended.ANONYMOUS_PRINCIPAL, version.urlPart, response);
        assertFalse(result.isError());
        assertNotNull(result.structuredContent());
        assertFalse(result.content().isEmpty());
    }

    @Test
    void testToCallToolResultStaErrorReKeying() {
        final ServiceResponseDefault response = new ServiceResponseDefault(404, "Not Found");
        response.getWriter().append("{\"type\":\"error\",\"code\":404,\"message\":\"No entity with id 42.\"}");
        final McpSchema.CallToolResult result = handler.toCallToolResult("frost_get", "Houses(42)", PrincipalExtended.ANONYMOUS_PRINCIPAL, version.urlPart, response);
        assertTrue(result.isError());
        @SuppressWarnings("unchecked")
        final Map<String, Object> structured = assertCast(Map.class, result.structuredContent());
        assertEquals(404, structured.get("statusCode"));
        assertNull(structured.get("code"), "The STA 'code' key must be re-keyed to statusCode.");
        assertEquals("No entity with id 42.", structured.get("message"));
        assertEquals("error", structured.get("type"));
    }

    @Test
    void testToCallToolResultNonJsonError() {
        final ServiceResponseDefault response = new ServiceResponseDefault(500, "Kaboom");
        final McpSchema.CallToolResult result = handler.toCallToolResult("frost_query", "Houses", PrincipalExtended.ANONYMOUS_PRINCIPAL, version.urlPart, response);
        assertTrue(result.isError());
        @SuppressWarnings("unchecked")
        final Map<String, Object> structured = assertCast(Map.class, result.structuredContent());
        assertEquals(500, structured.get("statusCode"));
        assertEquals("Kaboom", structured.get("message"));
    }

    @Test
    void testToCallToolResultByteCap() {
        final Properties properties = new Properties();
        properties.put(McpSettings.PREFIX_MCP + McpSettings.TAG_MAX_RESULT_SIZE, "4");
        final CoreSettings smallCore = new CoreSettings();
        properties.forEach((key, value) -> smallCore.getSettings().set(String.valueOf(key), String.valueOf(value)));
        final McpSettings smallMcp = new McpSettings(smallCore.getSettings().getSubSettings(McpSettings.PREFIX_MCP));
        final FrostToolHandler smallHandler = new FrostToolHandler(smallCore, smallMcp);
        assertEquals(4, smallMcp.getMaxResultSize());

        final ServiceResponseDefault response = new ServiceResponseDefault(200, "OK");
        response.getWriter().append("{\"value\":[\"much too large\"]}");
        final McpSchema.CallToolResult result = smallHandler.toCallToolResult("frost_get", "Houses", PrincipalExtended.ANONYMOUS_PRINCIPAL, version.urlPart, response);
        assertTrue(result.isError(), "Too-large payloads must become isError results.");
        @SuppressWarnings("unchecked")
        final Map<String, Object> structured = assertCast(Map.class, result.structuredContent());
        assertEquals(413, structured.get("statusCode"));
        assertTrue(String.valueOf(structured.get("message")).contains("narrow the query"));
    }

    @Test
    void testErrorObject() {
        final Map<String, Object> error = FrostToolHandler.errorObject(401, "denied");
        assertEquals(401, error.get("statusCode"));
        assertEquals("denied", error.get("message"));
        assertEquals(2, error.size());
    }

    @Test
    void testInvalidParamsHelper() {
        final McpError error = FrostToolHandler.invalidParams("oops");
        assertEquals(ErrorCodes.INVALID_PARAMS, error.getJsonRpcError().code());
    }

    @Test
    void testParseOrNullIntegration() {
        assertNotNull(StaJsonHelper.parseOrNull("{\"a\":1}"), "Sanity: helper used by the result mapping works.");
    }

    private static <T> T assertCast(Class<T> clazz, Object value) {
        assertTrue(clazz.isInstance(value), "Result must be a " + clazz.getSimpleName() + " but was " + (value == null ? "null" : value.getClass()));
        return clazz.cast(value);
    }

}
