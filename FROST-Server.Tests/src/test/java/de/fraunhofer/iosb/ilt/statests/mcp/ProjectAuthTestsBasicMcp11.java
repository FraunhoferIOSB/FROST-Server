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
package de.fraunhofer.iosb.ilt.statests.mcp;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_NAME;
import static de.fraunhofer.iosb.ilt.frostclient.utils.Constants.CONTENT_TYPE_APPLICATION_GEOJSON;
import static de.fraunhofer.iosb.ilt.statests.TestCore.KEY_DB_NAME;
import static de.fraunhofer.iosb.ilt.statests.TestCore.KEY_MCP_ENABLED;
import static de.fraunhofer.iosb.ilt.statests.f01auth.SensorThingsUserModel.EP_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Projects;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.utils.ParserUtils;
import de.fraunhofer.iosb.ilt.frostclient.utils.StringHelper;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestCore;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchResponseJson;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the FROST MCP server: uses the MCP tools to run the fine-grained
 * project authorization matrix of the f01auth ProjectAuthTests with basic auth,
 * with anonymous reads disallowed.
 *
 * Instead of one HTTP service per user, every user gets an MCP session
 * authenticated with a Basic-Authorization request customizer. The server
 * requires authenticated MCP sessions; since the anonymous client cannot even
 * complete the initialize handshake, every anonymous row of the original matrix
 * is expressed as "client must be refused".
 *
 * Seeding (DatabaseStatus upgrade + the projects $batch) intentionally stays on
 * an HTTP admin service, so the HTTP request statistics remain small and
 * deterministic; all per-user access runs through the MCP tools.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ProjectAuthTestsBasicMcp11 extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectAuthTestsBasicMcp11.class.getName());

    private static final int H200 = 200;
    private static final int H401 = 401;
    private static final int H403 = 403;
    private static final int H404 = 404;

    private static final String V1_1 = "v1.1";

    private static final String ADMIN = "admin";
    private static final String WRITE = "write";
    private static final String READ = "read";
    private static final String GLOBAL_OBS_CREATE = "GlobalObsCreater";
    private static final String GLOBAL_OBSPROP_CREATE = "GlobalObsPropCreater";
    private static final String ANONYMOUS = "anonymous";
    private static final String ADMIN_P1 = "AdminProject1";
    private static final String ADMIN_P2 = "AdminProject2";
    private static final String OBS_CREATE_P1 = "ObsCreaterProject1";
    private static final String OBS_CREATE_P2 = "ObsCreaterProject2";

    /**
     * The users of the original test. The password is always the user name.
     */
    private static final String[] USERS_CLIENT = new String[]{
        ADMIN,
        WRITE,
        READ,
        GLOBAL_OBS_CREATE,
        GLOBAL_OBSPROP_CREATE,
        ADMIN_P1,
        ADMIN_P2,
        OBS_CREATE_P1,
        OBS_CREATE_P2};

    private static final String DB_NAME = "projectAuthBasicMcp";

    protected static final SensorThingsV11Sensing mdlSensing = new SensorThingsV11Sensing();
    protected static final SensorThingsV11Projects mdlProjects = new SensorThingsV11Projects();
    protected static final SensorThingsService baseService = new SensorThingsService(mdlSensing, mdlProjects);

    protected static final List<Entity> USERS = new ArrayList<>();
    protected static final List<Entity> ROLES = new ArrayList<>();
    protected static final List<Entity> O_PROPS = new ArrayList<>();
    protected static final List<Entity> SENSORS = new ArrayList<>();
    protected static final List<Entity> THINGS = new ArrayList<>();
    protected static final List<Entity> LOCATIONS = new ArrayList<>();
    protected static final List<Entity> DATASTREAMS = new ArrayList<>();
    protected static final List<Entity> OBSERVATIONS = new ArrayList<>();
    protected static final List<Entity> PROJECTS = new ArrayList<>();
    protected static final List<Entity> USER_PROJECT_ROLES = new ArrayList<>();

    private static SensorThingsService serviceAdmin;
    private static final Map<String, McpSyncClient> MCP_CLIENTS = new LinkedHashMap<>();
    private static boolean anonConnected = false;
    private static final Map<String, String> FOI_IDS = new LinkedHashMap<>();

    public static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        SERVER_PROPERTIES.put("plugins.coreModel.idType", "LONG");
        SERVER_PROPERTIES.put("plugins.projects.enable", "true");
        SERVER_PROPERTIES.put("plugins.projects.enableDefaultRules", "true");
        SERVER_PROPERTIES.put("plugins.modelLoader.enable", "true");
        SERVER_PROPERTIES.put("plugins.modelLoader.idType.Role", "STRING");
        SERVER_PROPERTIES.put("plugins.modelLoader.idType.User", "STRING");
        SERVER_PROPERTIES.put("persistence.idGenerationMode.Role", "ClientGeneratedOnly");
        SERVER_PROPERTIES.put("persistence.idGenerationMode.User", "ClientGeneratedOnly");
        SERVER_PROPERTIES.put("auth.mqtt.fineGrainedAuth", "true");
        SERVER_PROPERTIES.put("auth.mqtt.topicAllowList", "^(/Things)|(/[a-zA-Z0-9_-]+\\((('[^']+')|([0-9]+))\\)/[a-zA-Z0-9_-]+)$");
        SERVER_PROPERTIES.put("auth.provider", "de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider");
        SERVER_PROPERTIES.put("auth.authenticateOnly", "true");
        SERVER_PROPERTIES.put("auth.allowAnonymousRead", "false");
        SERVER_PROPERTIES.put("auth.autoUpdateDatabase", "true");
        SERVER_PROPERTIES.put("auth.db.driver", "org.postgresql.Driver");
        SERVER_PROPERTIES.put("auth.db.url", TestCore.createDbUrl("org.postgresql.Driver", DB_NAME));
        SERVER_PROPERTIES.put("auth.db.username", TestCore.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth.db.password", TestCore.VAL_PG_PASS);
        SERVER_PROPERTIES.put("auth.plainTextPassword", "false");
        SERVER_PROPERTIES.put(KEY_DB_NAME, DB_NAME);
        SERVER_PROPERTIES.put(KEY_MCP_ENABLED, "true");
        SERVER_PROPERTIES.put("mcp.auth.requireAuthenticated", "true");
    }

    public ProjectAuthTestsBasicMcp11() {
        super(ServerVersion.V_1_1, SERVER_PROPERTIES);
    }

    @Override
    protected SensorThingsService createService() {
        try {
            if (!baseService.isBaseUrlSet()) {
                baseService.setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                        .addHook(HTTPMethods.getCountHook())
                        .init();
            }
            return new SensorThingsService(baseService.getModelRegistry())
                    .setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                    .setVersion(baseService.getVersion())
                    .addHook(HTTPMethods.getCountHook())
                    .init();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
        }
    }

    @Override
    protected void setUpVersion() {
        // HTTP admin service, only used for seeding; all test access runs via MCP.
        serviceAdmin = setAuthBasic(createService(), ADMIN, ADMIN);
        createEntities();
        connectMcpClients();
    }

    @AfterAll
    static void stats() {
        cleanup();
        HTTPMethods.expectStats("ProjectAuthTestsBasicMcp11", 0, 33, 0, 4, 0);
    }

    private static SensorThingsService setAuthBasic(SensorThingsService service, String username, String password) {
        final String auth = username + ":" + password;
        final byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        final String authHeader = "Basic " + new String(encodedAuth);

        service.getClientBuilder()
                .setDefaultHeaders(Arrays.asList(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader)));

        service.rebuildHttpClient();
        return service;
    }

    private static void createEntities() {
        try {
            THINGS.clear();
            LOCATIONS.clear();
            SENSORS.clear();
            O_PROPS.clear();
            DATASTREAMS.clear();
            OBSERVATIONS.clear();
            PROJECTS.clear();
            USERS.clear();
            ROLES.clear();
            USER_PROJECT_ROLES.clear();

            USERS.add(mdlProjects.newUser("read", "read"));
            USERS.add(mdlProjects.newUser("write", "write"));
            USERS.add(mdlProjects.newUser(ADMIN, ADMIN));
            ROLES.add(mdlProjects.buildRole().setRolename("read").build());
            ROLES.add(mdlProjects.buildRole().setRolename("create").build());
            ROLES.add(mdlProjects.buildRole().setRolename("update").build());
            ROLES.add(mdlProjects.buildRole().setRolename("delete").build());
            ROLES.add(mdlProjects.buildRole().setRolename("admin").build());
            ROLES.add(mdlProjects.buildRole().setRolename("obscreate").build());
            ROLES.add(mdlProjects.buildRole().setRolename("obsupdate").build());
            ROLES.add(mdlProjects.buildRole().setRolename("obsdelete").build());
            ROLES.add(mdlProjects.buildRole().setRolename("obspropcreate").build());
            ROLES.add(mdlProjects.buildRole().setRolename("obspropupdate").build());
            ROLES.add(mdlProjects.buildRole().setRolename("obspropdelete").build());

            try {
                HTTPMethods.doPost(serviceAdmin, serverSettings.getServiceRootUrl() + "/DatabaseStatus", "", "");

                final String response = postBatch(getBatchPostData());
                final BatchResponseJson result = Utils.MAPPER.readValue(response, BatchResponseJson.class);
                LOGGER.info("  Posted Batch with {} results.", result.getResponses().size());
                for (BatchResponseJson.ResponsePart part : result.getResponses()) {
                    final String location = part.getLocation();
                    final String type = Utils.typeFromSelfLink(location);
                    final PkValue pk = pkFromSelfLink(location);
                    switch (type) {
                        case "things" ->
                            THINGS.add(serviceAdmin.dao(mdlSensing.etThing).find(pk));
                        case "locations" ->
                            LOCATIONS.add(serviceAdmin.dao(mdlSensing.etLocation).find(pk));
                        case "sensors" ->
                            SENSORS.add(serviceAdmin.dao(mdlSensing.etSensor).find(pk));
                        case "observedproperties" ->
                            O_PROPS.add(serviceAdmin.dao(mdlSensing.etObservedProperty).find(pk));
                        case "observations" ->
                            OBSERVATIONS.add(serviceAdmin.dao(mdlSensing.etObservation).find(pk));
                        case "datastreams" ->
                            DATASTREAMS.add(serviceAdmin.dao(mdlSensing.etDatastream).find(pk));
                        case "users" ->
                            USERS.add(serviceAdmin.dao(mdlProjects.etUser).find(pk));
                        case "projects" ->
                            PROJECTS.add(serviceAdmin.dao(mdlProjects.etProject).find(pk));
                        case "roles" ->
                            ROLES.add(serviceAdmin.dao(mdlProjects.etRole).find(pk));
                        case "userprojectroles" ->
                            USER_PROJECT_ROLES.add(serviceAdmin.dao(mdlProjects.etUserProjectRole).find(pk));
                        default ->
                            throw new IllegalArgumentException("Type " + type + " should not have been created.");
                    }
                }
                Collections.sort(USERS, Comparator.comparing(
                        user -> String.valueOf(user.getProperty(EP_USERNAME)).toLowerCase()));
            } catch (RuntimeException ex) {
                LOGGER.error("Exception in createEntities.", ex);
                fail("Exception in createEntities: " + ex.getMessage());
            }
            OBSERVATIONS.addAll(serviceAdmin.query(mdlSensing.etObservation).top(100).orderBy("id").list().toList());
        } catch (Exception ex) {
            LOGGER.error("Exception in createEntities.", ex);
            fail("Exception in createEntities: " + ex.getMessage());
        }
    }

    private static PkValue pkFromSelfLink(String selfLink) {
        String idString = selfLink.substring(selfLink.indexOf('(') + 1, selfLink.indexOf(')'));
        return ParserUtils.tryToParse(idString);
    }

    private static String getBatchPostData() {
        try {
            return IOUtils.resourceToString(
                    "projects/dataBatchPost.json",
                    StandardCharsets.UTF_8,
                    ProjectAuthTestsBasicMcp11.class.getClassLoader());
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read projects/dataBatchPost.json", ex);
        }
    }

    private static String postBatch(String body) {
        String urlString = serverSettings.getServiceUrl(version) + "/$batch";
        HTTPMethods.HttpResponse httpResponse = HTTPMethods.doPost(serviceAdmin, urlString, body, "application/json");
        assertEquals(200, httpResponse.code, "Batch response should be 200");
        return httpResponse.response;
    }

    private static void connectMcpClients() {
        MCP_CLIENTS.clear();
        anonConnected = false;
        for (String user : USERS_CLIENT) {
            MCP_CLIENTS.put(user, connectClient(user, user));
        }
        try {
            McpSyncClient anon = McpClient.sync(
                    HttpClientStreamableHttpTransport.builder(serverSettings.getMcpUrl()).build())
                    .clientInfo(McpSchema.Implementation.builder("FROST-MCP-ProjectAuth-anon", "1.0.0").build())
                    .build();
            anon.initialize();
            anon.close();
            anonConnected = true;
            LOGGER.warn("Anonymous MCP client could connect, although the server requires authentication!");
        } catch (Exception ex) {
            LOGGER.debug("Anonymous MCP client was refused, as expected: {}", ex.getMessage());
        }
        fetchFoiIds();
    }

    /**
     * Fetches the id of the FeatureOfInterest of every observation, once,
     * through the admin MCP session. MCP traffic is not counted by the HTTP
     * statistics hook, so this prefetch keeps the frostclient lazy-loading
     * (which would be counted) out of the per-user checks.
     */
    private static void fetchFoiIds() {
        FOI_IDS.clear();
        final Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("entitySet", mdlSensing.etObservation.mainSet);
        arguments.put("select", List.of("id"));
        arguments.put("expand", List.of("FeatureOfInterest($select=id)"));
        arguments.put("version", V1_1);
        final CallToolResult result = callTool(MCP_CLIENTS.get(ADMIN), "frost_query", arguments);
        if (isError(result)) {
            LOGGER.warn("Could not fetch FeatureOfInterest ids: {}", textOf(result));
            return;
        }
        for (Map observation : valueOf(result)) {
            final Object foi = observation.get("FeatureOfInterest");
            if (foi instanceof Map) {
                FOI_IDS.put(canonPk(observation.get("@iot.id")), canonPk(((Map) foi).get("@iot.id")));
            }
        }
    }

    private static McpSyncClient connectClient(String user, String password) {
        final String mcpUrl = serverSettings.getMcpUrl();
        LOGGER.debug("Connecting MCP client for user {} to {}", user, mcpUrl);
        final String authHeader = basicAuthHeader(user, password);
        final McpSyncClient client = McpClient.sync(
                HttpClientStreamableHttpTransport.builder(mcpUrl)
                        .addHttpRequestCustomizer((requestBuilder, toolName, uri, sessionId, context) -> requestBuilder.header("Authorization", authHeader))
                        .build())
                .clientInfo(McpSchema.Implementation.builder("FROST-MCP-ProjectAuth-" + user, "1.0.0").build())
                .build();
        client.initialize();
        return client;
    }

    private static String basicAuthHeader(String username, String password) {
        final String auth = username + ":" + password;
        final byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    //
    // MCP tool helpers. All helpers access the server through the MCP tools
    // of the session of the given user; validation queries use the admin MCP
    // session. MCP traffic is not counted by the HTTP statistics hook.
    //
    private CallToolResult callTool(String user, String name, Map<String, Object> arguments) {
        final McpSyncClient client = MCP_CLIENTS.get(user);
        assertNotNull(client, "No MCP session for user " + user);
        return callTool(client, name, arguments);
    }

    private static CallToolResult callTool(McpSyncClient client, String name, Map<String, Object> arguments) {
        return client.callTool(CallToolRequest.builder(name).arguments(arguments).build());
    }

    private static boolean isError(CallToolResult result) {
        return Boolean.TRUE.equals(result.isError());
    }

    private static Map structuredMap(CallToolResult result) {
        final Object structured = result.structuredContent();
        assertTrue(structured instanceof Map, "Tool result must carry structured content.");
        return (Map) structured;
    }

    private static String textOf(CallToolResult result) {
        if (result.content() == null || result.content().isEmpty()) {
            return "";
        }
        return String.valueOf(result.content().get(0));
    }

    private static int statusCodeOf(CallToolResult result) {
        final Object code = structuredMap(result).get("statusCode");
        assertTrue(code instanceof Number, "Error result must carry a numeric statusCode: " + textOf(result));
        return ((Number) code).intValue();
    }

    private static long idOf(CallToolResult result) {
        final Object id = structuredMap(result).get("@iot.id");
        assertTrue(id instanceof Number, "Create result must carry a numeric @iot.id key: " + textOf(result));
        return ((Number) id).longValue();
    }

    private static boolean codeIn(int got, int... codes) {
        for (int code : codes) {
            if (code == got) {
                return true;
            }
        }
        return false;
    }

    private static String canonPk(Object pk) {
        if (pk == null) {
            return "null";
        }
        if (pk instanceof PkValue pkv) {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object value : pkv) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(String.valueOf(value));
                first = false;
            }
            return sb.toString();
        }
        if (pk instanceof List<?> list) {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object value : list) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(String.valueOf(value));
                first = false;
            }
            return sb.toString();
        }
        return String.valueOf(pk);
    }

    private static String canon(Entity entity) {
        return canonPk(entity.getPrimaryKeyValues());
    }

    private static Set<String> canonSet(List<Entity> entities) {
        final Set<String> result = new LinkedHashSet<>();
        for (Entity entity : entities) {
            result.add(canon(entity));
        }
        return result;
    }

    private static Map<String, Object> args(String entitySet, String filter, boolean count) {
        final Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("entitySet", entitySet);
        if (filter != null && !filter.isEmpty()) {
            arguments.put("filter", filter);
        }
        if (count) {
            arguments.put("count", true);
            // Users and Roles have client-generated String ids; for those
            // entity types FROST rejects "@iot.id" in $select, so fetch the
            // full (small) entities instead.
            if (!entitySet.equals(mdlProjects.etUser.mainSet) && !entitySet.equals(mdlProjects.etRole.mainSet)) {
                arguments.put("select", List.of("@iot.id"));
            }
        }
        arguments.put("version", V1_1);
        return arguments;
    }

    @SuppressWarnings("unchecked")
    private static List<Map> valueOf(CallToolResult result) {
        final Object value = structuredMap(result).get("value");
        assertTrue(value instanceof List, "Query result must carry a value array: " + textOf(result));
        return (List<Map>) value;
    }

    private static Set<String> idsOf(List<Map> items) {
        final Set<String> ids = new LinkedHashSet<>();
        for (Map item : items) {
            ids.add(idOf(item));
        }
        return ids;
    }

    /**
     * Extracts the id of an entity from its JSON representation. Core model
     * entity types carry "@iot.id", but entity types with client-generated
     * String primary keys (Users, Roles) do not serialise "@iot.id"; those
     * carry the id only inside "@iot.selfLink", as ".../Users('read')".
     */
    private static String idOf(Map item) {
        final Object id = item.get("@iot.id");
        if (id != null) {
            return canonPk(id);
        }
        final Object selfLink = item.get("@iot.selfLink");
        if (selfLink instanceof String link && link.endsWith(")")) {
            final int open = link.lastIndexOf('(');
            if (open > 0) {
                String raw = link.substring(open + 1, link.length() - 1);
                if (raw.length() > 1 && raw.startsWith("'") && raw.endsWith("'")) {
                    raw = raw.substring(1, raw.length() - 1).replace("''", "'");
                }
                return raw;
            }
        }
        return null;
    }

    /**
     * Builds the STA path of an entity from its JSON representation, for use in
     * frost_delete. For entities without "@iot.id" the path is taken directly
     * from the "@iot.selfLink", keeping its id quoting intact.
     */
    private static String deletePathOf(String set, Map item) {
        final Object id = item.get("@iot.id");
        if (id != null) {
            String idString;
            if (id instanceof String text) {
                idString = "'" + text + "'";
            } else {
                idString = String.valueOf(id);
            }
            return set + "(" + idString + ")";
        }
        final Object selfLink = item.get("@iot.selfLink");
        if (selfLink instanceof String link && link.endsWith(")")) {
            final int hostEnd = link.indexOf('/', link.indexOf("//") + 2);
            if (hostEnd > 0) {
                final int versionEnd = link.indexOf('/', hostEnd + 1);
                if (versionEnd > hostEnd && versionEnd < link.length() - 1) {
                    return link.substring(versionEnd + 1);
                }
            }
        }
        return null;
    }

    private static int countOf(CallToolResult result, int fallback) {
        final Object count = structuredMap(result).get("@iot.count");
        return count instanceof Number number ? number.intValue() : fallback;
    }

    private static long adminCount(String entitySet) {
        final CallToolResult result = callTool(MCP_CLIENTS.get(ADMIN), "frost_query", args(entitySet, null, true));
        assertFalse(isError(result), "Admin count query failed: " + textOf(result));
        return countOf(result, valueOf(result).size());
    }

    /**
     * The MCP analogue of the original testFilterResults: the user queries an
     * entity set (optionally filtered) and must see exactly the expected
     * entities, both in the value array and in @iot.count.
     */
    private void assertVisibility(String user, String entitySet, String filter, List<Entity> expected) {
        final CallToolResult result = callTool(user, "frost_query", args(entitySet, filter, true));
        assertFalse(isError(result), user + " should be able to read " + entitySet + ": " + textOf(result));
        final Set<String> expectedIds = canonSet(expected);
        final Set<String> foundIds = idsOf(valueOf(result));
        assertEquals(expectedIds, foundIds, "Wrong set of " + entitySet + " visible to " + user);
        assertEquals(expected.size(), countOf(result, foundIds.size()),
                "Wrong count of " + entitySet + " visible to " + user);
    }

    /**
     * With mcp.auth.requireAuthenticated the anonymous client cannot complete
     * the initialize handshake; that refusal is the connection to all anonymous
     * rows of the original matrix (HTTP 401 analogue).
     */
    private void expectAnonRefused(String what) {
        assertFalse(anonConnected,
                ANONYMOUS + " must not be able to connect; refusing the session is the 401 analogue for " + what);
    }

    private void createForOk(String user, String entitySet, Map<String, Object> entity,
            List<Entity> entityList, Entity created) {
        final long before = adminCount(entitySet);
        final CallToolResult result = callTool(user, "frost_create", Map.of(
                "path", entitySet,
                "entity", entity,
                "version", V1_1));
        assertFalse(isError(result), user + " should be able to create in " + entitySet + ": " + textOf(result));
        final long id = idOf(result);
        assertTrue(id > 0, user + "'s created entity must carry a positive @iot.id.");
        assertEquals(before + 1, adminCount(entitySet), user + "'s create must raise the " + entitySet + " count.");
        created.setPrimaryKeyValues(PkValue.of(id));
        entityList.add(created);
    }

    private void createForFail(String user, String entitySet, Map<String, Object> entity, int... expectedCodes) {
        final long before = adminCount(entitySet);
        final CallToolResult result = callTool(user, "frost_create", Map.of(
                "path", entitySet,
                "entity", entity,
                "version", V1_1));
        assertTrue(isError(result), user + " should NOT be able to create in " + entitySet + ": " + textOf(result));
        final int got = statusCodeOf(result);
        assertTrue(codeIn(got, expectedCodes),
                "Expected one of " + Arrays.toString(expectedCodes) + " but got " + got + " when " + user + " created in " + entitySet);
        assertEquals(before, adminCount(entitySet), user + "'s failed create must not change the " + entitySet + " count.");
    }

    private void updateForOk(String user, String path, Map<String, Object> changes, String property, Object expectedValue) {
        final CallToolResult result = callTool(user, "frost_update", Map.of(
                "path", path,
                "mode", "patch",
                "changes", changes,
                "version", V1_1));
        assertFalse(isError(result), user + " should be able to update " + path + ": " + textOf(result));
        assertEquals(canonPk(expectedValue), canonPk(fetchCheckValue(user, path, property)),
                "Wrong value of " + property + " after update by " + user);
    }

    private void updateForFail(String user, String path, Map<String, Object> changes,
            String checkPath, String checkProperty, Object unchangedValue, int... expectedCodes) {
        final CallToolResult result = callTool(user, "frost_update", Map.of(
                "path", path,
                "mode", "patch",
                "changes", changes,
                "version", V1_1));
        assertTrue(isError(result), user + " should NOT be able to update " + path + ": " + textOf(result));
        final int got = statusCodeOf(result);
        assertTrue(codeIn(got, expectedCodes),
                "Expected one of " + Arrays.toString(expectedCodes) + " but got " + got + " when " + user + " updated " + path);
        assertEquals(canonPk(unchangedValue), canonPk(fetchCheckValue(ADMIN, checkPath, checkProperty)),
                "Property " + checkProperty + " must be unchanged after failed update by " + user);
    }

    /**
     * Fetches the value of the given property of the entity at path, through
     * the MCP session of the given user. Relation-id properties like
     * "Thing@iot.id" are read from an expanded child entity, since plain
     * serialisations do not contain them.
     */
    private Object fetchCheckValue(String user, String path, String property) {
        final Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("path", path);
        final int atIndex = property.indexOf('@');
        if (atIndex > 0) {
            arguments.put("query", "$expand=" + property.substring(0, atIndex) + "($select=id)");
        }
        arguments.put("version", V1_1);
        final CallToolResult fetched = callTool(user, "frost_get", arguments);
        assertFalse(isError(fetched), user + " should be able to read " + path + ": " + textOf(fetched));
        final Map entityMap = structuredMap(fetched);
        if (atIndex > 0) {
            final Object child = entityMap.get(property.substring(0, atIndex));
            assertTrue(child instanceof Map,
                    "Fetched " + path + " must contain the expanded " + property.substring(0, atIndex) + ": " + textOf(fetched));
            return ((Map) child).get("@iot.id");
        }
        return entityMap.get(property);
    }

    private void deleteForOk(String user, String entitySet, Entity target, List<Entity> entityList) {
        final long before = adminCount(entitySet);
        final String path = entitySet + "(" + target.getPrimaryKeyValues().get(0) + ")";
        final CallToolResult result = callTool(user, "frost_delete", Map.of(
                "path", path,
                "version", V1_1));
        assertFalse(isError(result), user + " should be able to delete " + path + ": " + textOf(result));
        assertEquals(before - 1, adminCount(entitySet), user + "'s delete must lower the " + entitySet + " count.");
        entityList.removeIf(entity -> canon(entity).equals(canon(target)));
    }

    private void deleteForFail(String user, String entitySet, Entity target, int... expectedCodes) {
        final long before = adminCount(entitySet);
        final String path = entitySet + "(" + target.getPrimaryKeyValues().get(0) + ")";
        final CallToolResult result = callTool(user, "frost_delete", Map.of(
                "path", path,
                "version", V1_1));
        assertTrue(isError(result), user + " should NOT be able to delete " + path + ": " + textOf(result));
        final int got = statusCodeOf(result);
        assertTrue(codeIn(got, expectedCodes),
                "Expected one of " + Arrays.toString(expectedCodes) + " but got " + got + " when " + user + " deleted " + path);
        assertEquals(before, adminCount(entitySet), user + "'s failed delete must not change the " + entitySet + " count.");
    }

    /**
     * The MCP analogue of fetchForCode: the user reads a navigation path and
     * the result must be a success (200) or an error carrying one of the
     * expected status codes.
     */
    private void fetchForCode(String user, String path, int... expectedCodes) {
        final CallToolResult result = callTool(user, "frost_get", Map.of(
                "path", path,
                "version", V1_1));
        if (isError(result)) {
            final int got = statusCodeOf(result);
            assertTrue(codeIn(got, expectedCodes),
                    "Expected one of " + Arrays.toString(expectedCodes) + " but got " + got + " when " + user + " fetched " + path);
            return;
        }
        assertTrue(codeIn(H200, expectedCodes),
                "Expected an error (" + Arrays.toString(expectedCodes) + ") but got a success" + " when " + user + " fetched " + path);
    }

    /**
     * The MCP analogue of linkForOk/linkForFail: the user POSTs an @id
     * reference to the $ref path of the navigation property.
     */
    private CallToolResult link(String user, Entity source, String navSet, Entity target) {
        final String sourcePath = mdlSensing.etThing.mainSet + "(" + source.getPrimaryKeyValues().get(0) + ")";
        final String targetPath = navSet + "(" + target.getPrimaryKeyValues().get(0) + ")";
        return callTool(user, "frost_create", Map.of(
                "path", sourcePath + "/" + navSet + "/$ref",
                "entity", Map.of("@id", serverSettings.getServiceRootUrl() + "/" + targetPath),
                "version", V1_1));
    }

    private void linkForOk(String user, Entity source, String navSet, Entity target) {
        final CallToolResult result = link(user, source, navSet, target);
        assertFalse(isError(result), user + " should be able to link: " + textOf(result));
        assertNavLink(ADMIN, source, navSet, target);
    }

    private void linkForFail(String user, Entity source, String navSet, Entity target, int... expectedCodes) {
        final CallToolResult result = link(user, source, navSet, target);
        assertTrue(isError(result), user + " should NOT be able to link: " + textOf(result));
        final int got = statusCodeOf(result);
        assertTrue(codeIn(got, expectedCodes),
                "Expected one of " + Arrays.toString(expectedCodes) + " but got " + got + " when " + user + " linked a " + navSet);
    }

    private void assertNavLink(String checkUser, Entity source, String navSet, Entity expectedTarget) {
        final String sourcePath = mdlSensing.etThing.mainSet + "(" + source.getPrimaryKeyValues().get(0) + ")";
        final CallToolResult result = callTool(checkUser, "frost_get", Map.of(
                "path", sourcePath + "/" + navSet,
                "version", V1_1));
        assertFalse(isError(result), checkUser + " should be able to read " + sourcePath + "/" + navSet + ": " + textOf(result));
        final Set<String> gotIds = idsOf(valueOf(result));
        assertTrue(gotIds.contains(canon(expectedTarget)),
                checkUser + " should see the target of the link in " + navSet);
    }

    private static String userPath(String username) {
        return "Users('" + username + "')";
    }

    private static Map<String, Object> newPatchingMap(String property, Object value) {
        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(property, value);
        return changes;
    }

    //
    // The test matrix.
    //
    @Test
    void test_02a_ReadProjects() {
        LOGGER.info("  test_02a_ReadProjects");
        final String set = mdlProjects.etProject.mainSet;
        assertVisibility(ADMIN, set, "", PROJECTS);
        assertVisibility(WRITE, set, "", PROJECTS);
        assertVisibility(READ, set, "", PROJECTS);
        expectAnonRefused(set);
        assertVisibility(ADMIN_P1, set, "", Utils.getFromList(PROJECTS, 0));
        assertVisibility(ADMIN_P2, set, "", Utils.getFromList(PROJECTS, 0, 1));
        assertVisibility(OBS_CREATE_P1, set, "", Utils.getFromList(PROJECTS, 0));
        assertVisibility(OBS_CREATE_P2, set, "", Utils.getFromList(PROJECTS, 0, 1));
    }

    @Test
    void test_02b_ReadUserProjectRoles() {
        LOGGER.info("  test_02b_ReadUserProjectRoles");
        final String set = mdlProjects.etUserProjectRole.mainSet;
        assertVisibility(ADMIN, set, "", USER_PROJECT_ROLES);
        assertVisibility(WRITE, set, "", Collections.emptyList());
        assertVisibility(READ, set, "", Collections.emptyList());
        expectAnonRefused(set);
        assertVisibility(ADMIN_P1, set, "", Utils.getFromList(USER_PROJECT_ROLES, 0, 1));
        assertVisibility(ADMIN_P2, set, "", Utils.getFromList(USER_PROJECT_ROLES, 2, 3));
        assertVisibility(OBS_CREATE_P1, set, "", Collections.emptyList());
        assertVisibility(OBS_CREATE_P2, set, "", Collections.emptyList());
    }

    @Test
    void test_02c_ReadUser() {
        LOGGER.info("  test_02c_ReadUser");
        final String set = mdlProjects.etUser.mainSet;
        assertVisibility(ADMIN, set, "", USERS);
        assertVisibility(WRITE, set, "", Utils.getFromList(USERS, 8));
        assertVisibility(READ, set, "", Utils.getFromList(USERS, 7));
        expectAnonRefused(set);
        assertVisibility(ADMIN_P1, set, "", USERS);
        assertVisibility(ADMIN_P2, set, "", USERS);
        assertVisibility(OBS_CREATE_P1, set, "", Utils.getFromList(USERS, 5));
        assertVisibility(OBS_CREATE_P2, set, "", Utils.getFromList(USERS, 6));
    }

    @Test
    void test_02d_ReadRole() {
        LOGGER.info("  test_02d_ReadRole");
        final String set = mdlProjects.etRole.mainSet;
        assertVisibility(ADMIN, set, "", ROLES);
        assertVisibility(WRITE, set, "", Collections.emptyList());
        assertVisibility(READ, set, "", Collections.emptyList());
        expectAnonRefused(set);
        assertVisibility(ADMIN_P1, set, "", ROLES);
        assertVisibility(ADMIN_P2, set, "", ROLES);
        assertVisibility(OBS_CREATE_P1, set, "", Collections.emptyList());
        assertVisibility(OBS_CREATE_P2, set, "", Collections.emptyList());
    }

    @Test
    void test_02e_ReadThings() {
        LOGGER.info("  test_02e_ReadThings");
        final String set = mdlSensing.etThing.mainSet;
        assertVisibility(ADMIN, set, "", THINGS);
        assertVisibility(WRITE, set, "", THINGS);
        assertVisibility(READ, set, "", THINGS);
        expectAnonRefused(set);
        assertVisibility(ADMIN_P1, set, "", Utils.getFromList(THINGS, 0, 2));
        assertVisibility(ADMIN_P2, set, "", Utils.getFromList(THINGS, 0, 1, 2));
        assertVisibility(OBS_CREATE_P1, set, "", Utils.getFromList(THINGS, 0, 2));
        assertVisibility(OBS_CREATE_P2, set, "", Utils.getFromList(THINGS, 0, 1, 2));
    }

    @Test
    void test_02f_ReadDatastreams() {
        LOGGER.info("  test_02f_ReadDatastreams");
        final String set = mdlSensing.etDatastream.mainSet;
        assertVisibility(ADMIN, set, "", DATASTREAMS);
        assertVisibility(WRITE, set, "", DATASTREAMS);
        assertVisibility(READ, set, "", DATASTREAMS);
        expectAnonRefused(set);
        assertVisibility(ADMIN_P1, set, "", Utils.getFromList(DATASTREAMS, 0, 1, 4, 5));
        assertVisibility(ADMIN_P2, set, "", Utils.getFromList(DATASTREAMS, 0, 2, 3, 4, 5));
        assertVisibility(OBS_CREATE_P1, set, "", Utils.getFromList(DATASTREAMS, 0, 1, 4, 5));
        assertVisibility(OBS_CREATE_P2, set, "", Utils.getFromList(DATASTREAMS, 0, 2, 3, 4, 5));
    }

    @Test
    void test_03a_ProjectCreate() {
        LOGGER.info("  test_03a_ProjectCreate");
        final String set = mdlProjects.etProject.mainSet;
        assertVisibility(ADMIN, set, "", PROJECTS);
        for (String user : new String[]{
            READ,
            GLOBAL_OBS_CREATE,
            GLOBAL_OBSPROP_CREATE,
            ADMIN_P1,
            ADMIN_P2,
            OBS_CREATE_P1,
            OBS_CREATE_P2}) {
            createForFail(user, set, projectEntity(user), H403);
        }
        expectAnonRefused(set);
        createForOk(WRITE, set, projectEntity(WRITE), PROJECTS,
                newProjectEntity(WRITE));
    }

    private static Map<String, Object> projectEntity(String user) {
        final Map<String, Object> entity = new LinkedHashMap<>();
        entity.put("name", user + "-Project");
        entity.put("description", "A Project made by " + user);
        return entity;
    }

    private Entity newProjectEntity(String user) {
        return mdlProjects.buildProject()
                .setName(user + "-Project")
                .setDescription("A Project made by " + user)
                .build();
    }

    @Test
    void test_03b_UpdateProject() {
        LOGGER.info("  test_03b_UpdateProject");
        final Entity original = PROJECTS.get(1);
        final String path = mdlProjects.etProject.mainSet + "(" + original.getPrimaryKeyValues().get(0) + ")";
        final String originalName = String.valueOf(original.getProperty(EP_NAME));
        final Map<String, Object> changes = newPatchingMap("name", "x-Edited");

        updateForFail(READ, path, changes, path, "name", originalName, H403);
        updateForFail(GLOBAL_OBS_CREATE, path, changes, path, "name", originalName, H403);
        updateForFail(GLOBAL_OBSPROP_CREATE, path, changes, path, "name", originalName, H403);
        expectAnonRefused(path);
        updateForFail(ADMIN_P1, path, changes, path, "name", originalName, H404);
        updateForFail(OBS_CREATE_P2, path, changes, path, "name", originalName, H403);
        updateForFail(OBS_CREATE_P1, path, changes, path, "name", originalName, H404);
        updateForOk(ADMIN_P2, path, newPatchingMap("name", ADMIN_P2 + "-Edited"), "name", ADMIN_P2 + "-Edited");
        updateForOk(WRITE, path, newPatchingMap("name", WRITE + "-Edited"), "name", WRITE + "-Edited");
        updateForOk(ADMIN, path, newPatchingMap("name", originalName), "name", originalName);
    }

    @Test
    void test_03c_UserCreate() {
        LOGGER.info("  test_03c_UserCreate");
        final String set = mdlProjects.etUser.mainSet;
        createForFail(READ, set, userEntity(READ), H403);
        expectAnonRefused(set);
        createForFail(ADMIN_P1, set, userEntity(ADMIN_P1), H403);
        createForFail(ADMIN_P2, set, userEntity(ADMIN_P2), H403);
        createForFail(OBS_CREATE_P1, set, userEntity(OBS_CREATE_P1), H403);
        createForFail(OBS_CREATE_P2, set, userEntity(OBS_CREATE_P2), H403);
    }

    private static Map<String, Object> userEntity(String user) {
        final Map<String, Object> entity = new LinkedHashMap<>();
        entity.put("username", user + "-User");
        entity.put("userpass", user + "-password");
        return entity;
    }

    @Test
    void test_04a_ThingCreate() {
        LOGGER.info("  test_04a_ThingCreate");
        final String set = mdlSensing.etThing.mainSet;
        createForOk(WRITE, set, thingEntity(WRITE), THINGS, newThingEntity(WRITE));
        createForFail(READ, set, thingEntity(READ), H403);
        expectAnonRefused(set);
        createForFail(ADMIN_P1, set, thingEntity(ADMIN_P1), H403);
        createForFail(ADMIN_P2, set, thingEntity(ADMIN_P2), H403);
        createForFail(OBS_CREATE_P1, set, thingEntity(OBS_CREATE_P1), H403);
        createForFail(OBS_CREATE_P2, set, thingEntity(OBS_CREATE_P2), H403);
    }

    private static Map<String, Object> thingEntity(String user) {
        final Map<String, Object> entity = new LinkedHashMap<>();
        entity.put("name", user + "Thing");
        entity.put("description", "A Thing made by " + user);
        return entity;
    }

    private Entity newThingEntity(String user) {
        return mdlSensing.buildThing()
                .setName(user + "Thing")
                .setDescription("A Thing made by " + user)
                .build();
    }

    @Test
    void test_04b_ThingCreateForProject1() {
        LOGGER.info("  test_04b_ThingCreateForProject1");
        final String set = mdlSensing.etThing.mainSet;
        final long projectPk = (Long) PROJECTS.get(0).getPrimaryKeyValues().get(0);
        createForOk(WRITE, set, thingEntityWithProject(WRITE, projectPk), THINGS, newThingEntity(WRITE));
        createForFail(READ, set, thingEntityWithProject(READ, projectPk), H403);
        expectAnonRefused(set);
        createForOk(ADMIN_P1, set, thingEntityWithProject(ADMIN_P1, projectPk), THINGS, newThingEntity(ADMIN_P1));
        createForFail(ADMIN_P2, set, thingEntityWithProject(ADMIN_P2, projectPk), H403);
        createForFail(OBS_CREATE_P1, set, thingEntityWithProject(OBS_CREATE_P1, projectPk), H403);
        createForFail(OBS_CREATE_P2, set, thingEntityWithProject(OBS_CREATE_P2, projectPk), H403);
    }

    private static Map<String, Object> thingEntityWithProject(String user, long projectPk) {
        final Map<String, Object> entity = thingEntity(user);
        entity.put("Projects", List.of(Map.of("@iot.id", projectPk)));
        return entity;
    }

    @Test
    void test_04c_ThingDeepInsert() {
        LOGGER.info("  test_04c_ThingDeepInsert");
        final String set = mdlSensing.etThing.mainSet;
        final long projectPk = (Long) PROJECTS.get(0).getPrimaryKeyValues().get(0);
        final Map<String, Object> entity = thingEntity(WRITE);
        entity.put("Datastreams", List.of(datastreamTemplate()));
        entity.put("Projects", List.of(Map.of("@iot.id", projectPk)));

        createForOk(WRITE, set, entity, THINGS, newThingEntity(WRITE));
        createForFail(READ, set, entity, H403);
        expectAnonRefused(set);
        createForOk(ADMIN_P1, set, entity, THINGS, newThingEntity(ADMIN_P1));
        createForFail(ADMIN_P2, set, entity, H403);
        createForFail(OBS_CREATE_P1, set, entity, H403);
        createForFail(OBS_CREATE_P2, set, entity, H403);
    }

    private static Map<String, Object> datastreamTemplate() {
        final Map<String, Object> datastream = new LinkedHashMap<>();
        datastream.put("name", "DeepInsertDs");
        datastream.put("description", "Ds created by deep insert");
        datastream.put("observationType", "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        datastream.put("unitOfMeasurement", Map.of("name", "%", "definition", "%", "symbol", "%"));
        datastream.put("Sensor", Map.of("@iot.id", SENSORS.get(0).getPrimaryKeyValues().get(0)));
        datastream.put("ObservedProperty", Map.of("@iot.id", O_PROPS.get(0).getPrimaryKeyValues().get(0)));
        return datastream;
    }

    @Test
    void test_05a_DatastreamRelinkToThing2() {
        LOGGER.info("  test_05a_DatastreamRelinkToThing2");
        final Entity datastream = DATASTREAMS.get(0);
        final long thing1Pk = (Long) THINGS.get(1).getPrimaryKeyValues().get(0);
        final long thing0Pk = (Long) THINGS.get(0).getPrimaryKeyValues().get(0);
        final String path = mdlSensing.etDatastream.mainSet + "(" + datastream.getPrimaryKeyValues().get(0) + ")";
        final Map<String, Object> relinkChanges = Map.of("Thing", Map.of("@iot.id", thing1Pk));
        final Map<String, Object> resetChanges = Map.of("Thing", Map.of("@iot.id", thing0Pk));

        updateForFail(READ, path, relinkChanges, path, "Thing@iot.id", thing0Pk, H403);
        expectAnonRefused(path);
        updateForFail(ADMIN_P1, path, relinkChanges, path, "Thing@iot.id", thing0Pk, H403);
        updateForFail(ADMIN_P2, path, relinkChanges, path, "Thing@iot.id", thing0Pk, H403);
        updateForFail(OBS_CREATE_P1, path, relinkChanges, path, "Thing@iot.id", thing0Pk, H403);
        updateForFail(OBS_CREATE_P2, path, relinkChanges, path, "Thing@iot.id", thing0Pk, H403);
        updateForOk(WRITE, path, relinkChanges, "Thing@iot.id", thing1Pk);
        updateForOk(ADMIN, path, resetChanges, "Thing@iot.id", thing0Pk);
    }

    @Test
    void test_08a_ReadObservations() {
        LOGGER.info("  test_08a_ReadObservations");
        final String set = mdlSensing.etObservation.mainSet;
        assertVisibility(ADMIN, set, "", OBSERVATIONS);
        assertVisibility(WRITE, set, "", OBSERVATIONS);
        assertVisibility(READ, set, "", OBSERVATIONS);
        expectAnonRefused(set);
        assertVisibility(ADMIN_P1, set, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23));
        assertVisibility(ADMIN_P2, set, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
        assertVisibility(OBS_CREATE_P1, set, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23));
        assertVisibility(OBS_CREATE_P2, set, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
    }

    @Test
    void test_08b_ReadObsPropWithFilter() {
        LOGGER.info("  test_08b_ReadObsPropWithFilter");
        final String set = mdlSensing.etObservedProperty.mainSet;
        final String filter = "Datastreams/Observations/id eq "
                + StringHelper.quoteForUrl(OBSERVATIONS.get(8).getPrimaryKeyValues().get(0));
        assertVisibility(ADMIN, set, filter, Utils.getFromList(O_PROPS, 0));
        assertVisibility(WRITE, set, filter, Utils.getFromList(O_PROPS, 0));
        assertVisibility(READ, set, filter, Utils.getFromList(O_PROPS, 0));
        expectAnonRefused(set);
        assertVisibility(ADMIN_P1, set, filter, Collections.emptyList());
        assertVisibility(ADMIN_P2, set, filter, Utils.getFromList(O_PROPS, 0));
        assertVisibility(OBS_CREATE_P1, set, filter, Collections.emptyList());
        assertVisibility(OBS_CREATE_P2, set, filter, Utils.getFromList(O_PROPS, 0));
    }

    @Test
    void test_08c_ReadDatastreamOfObservation() {
        LOGGER.info("  test_08c_ReadDatastreamOfObservation");
        final String path = mdlSensing.etObservation.mainSet + "("
                + OBSERVATIONS.get(8).getPrimaryKeyValues().get(0) + ")/Datastream";
        fetchForCode(ADMIN, path, H200);
        fetchForCode(WRITE, path, H200);
        fetchForCode(READ, path, H200);
        expectAnonRefused(path);
        fetchForCode(ADMIN_P1, path, H404);
        fetchForCode(OBS_CREATE_P1, path, H404);
        fetchForCode(ADMIN_P2, path, H200);
        fetchForCode(OBS_CREATE_P2, path, H200);
    }

    @Test
    void test_08d_ReadObservationsOfDatastream() {
        LOGGER.info("  test_08d_ReadObservationsOfDatastream");
        final String path = mdlSensing.etDatastream.mainSet + "("
                + DATASTREAMS.get(1).getPrimaryKeyValues().get(0) + ")/Observations";
        fetchForCode(ADMIN, path, H200);
        fetchForCode(WRITE, path, H200);
        fetchForCode(READ, path, H200);
        expectAnonRefused(path);
        fetchForCode(ADMIN_P1, path, H200);
        fetchForCode(OBS_CREATE_P1, path, H200);
        fetchForCode(ADMIN_P2, path, H404);
        fetchForCode(OBS_CREATE_P2, path, H404);
    }

    @Test
    void test_08e_ObservationReadExpandPlus() {
        LOGGER.info("  test_08e_ObservationReadExpandPlus");
        final String filter = "id eq "
                + StringHelper.quoteForUrl(OBSERVATIONS.get(16).getPrimaryKeyValues().get(0));
        // obs16: ds4 -> thing[2] -> projects [0,1]; FoI -> projects [0,1].
        // P1 sees the same but only project[0] in the expanded navigation.
        checkExpanded(ADMIN, filter, 16, 4, 2, Utils.getFromList(PROJECTS, 0, 1), true, Utils.getFromList(PROJECTS, 0, 1));
        checkExpanded(WRITE, filter, 16, 4, 2, Utils.getFromList(PROJECTS, 0, 1), true, Utils.getFromList(PROJECTS, 0, 1));
        checkExpanded(READ, filter, 16, 4, 2, Utils.getFromList(PROJECTS, 0, 1), true, Utils.getFromList(PROJECTS, 0, 1));
        expectAnonRefused(mdlSensing.etObservation.mainSet);
        checkExpanded(ADMIN_P1, filter, 16, 4, 2, Utils.getFromList(PROJECTS, 0), true, Utils.getFromList(PROJECTS, 0));
        checkExpanded(OBS_CREATE_P1, filter, 16, 4, 2, Utils.getFromList(PROJECTS, 0), true, Utils.getFromList(PROJECTS, 0));
        checkExpanded(ADMIN_P2, filter, 16, 4, 2, Utils.getFromList(PROJECTS, 0, 1), true, Utils.getFromList(PROJECTS, 0, 1));
        checkExpanded(OBS_CREATE_P2, filter, 16, 4, 2, Utils.getFromList(PROJECTS, 0, 1), true, Utils.getFromList(PROJECTS, 0, 1));
    }

    @Test
    void test_08f_ObservationReadExpandMinus() {
        LOGGER.info("  test_08f_ObservationReadExpandMinus");
        final String filter = "id eq "
                + StringHelper.quoteForUrl(OBSERVATIONS.get(8).getPrimaryKeyValues().get(0));
        // obs8: ds2 -> thing[1] -> project[1]; FoI -> project[1].
        checkExpanded(ADMIN, filter, 8, 2, 1, Utils.getFromList(PROJECTS, 1), true, Utils.getFromList(PROJECTS, 1));
        checkExpanded(WRITE, filter, 8, 2, 1, Utils.getFromList(PROJECTS, 1), true, Utils.getFromList(PROJECTS, 1));
        checkExpanded(READ, filter, 8, 2, 1, Utils.getFromList(PROJECTS, 1), true, Utils.getFromList(PROJECTS, 1));
        expectAnonRefused(mdlSensing.etObservation.mainSet);
        checkExpanded(ADMIN_P1, filter, 8, 2, 1, Collections.emptyList(), false, Collections.emptyList());
        checkExpanded(OBS_CREATE_P1, filter, 8, 2, 1, Collections.emptyList(), false, Collections.emptyList());
        checkExpanded(ADMIN_P2, filter, 8, 2, 1, Utils.getFromList(PROJECTS, 1), true, Utils.getFromList(PROJECTS, 1));
        checkExpanded(OBS_CREATE_P2, filter, 8, 2, 1, Utils.getFromList(PROJECTS, 1), true, Utils.getFromList(PROJECTS, 1));
    }

    @Test
    void test_08g_ObservationReadExpandFoi() {
        LOGGER.info("  test_08g_ObservationReadExpandFoi");
        final String filter = "id eq "
                + StringHelper.quoteForUrl(OBSERVATIONS.get(2).getPrimaryKeyValues().get(0));
        // obs2: ds0 -> thing[0] -> project[0]; FoI -> project[0].
        // P2/OP2 see the observation but its FoI (project 0 only) is hidden.
        checkExpanded(ADMIN, filter, 2, 0, 0, Utils.getFromList(PROJECTS, 0), true, Utils.getFromList(PROJECTS, 0));
        checkExpanded(WRITE, filter, 2, 0, 0, Utils.getFromList(PROJECTS, 0), true, Utils.getFromList(PROJECTS, 0));
        checkExpanded(READ, filter, 2, 0, 0, Utils.getFromList(PROJECTS, 0), true, Utils.getFromList(PROJECTS, 0));
        expectAnonRefused(mdlSensing.etObservation.mainSet);
        checkExpanded(ADMIN_P1, filter, 2, 0, 0, Utils.getFromList(PROJECTS, 0), true, Utils.getFromList(PROJECTS, 0));
        checkExpanded(OBS_CREATE_P1, filter, 2, 0, 0, Utils.getFromList(PROJECTS, 0), true, Utils.getFromList(PROJECTS, 0));
        checkExpanded(ADMIN_P2, filter, 2, 0, 0, Utils.getFromList(PROJECTS, 0), false, Collections.emptyList());
        checkExpanded(OBS_CREATE_P2, filter, 2, 0, 0, Utils.getFromList(PROJECTS, 0), false, Collections.emptyList());
    }

    private static final String EXPAND_DS_THING_PROJECTS_FOI = "Datastream($select=id;$expand=Thing($select=id;$expand=Projects($select=id))),"
            + "FeatureOfInterest($select=id;$expand=Projects($select=id))";

    /**
     * The MCP analogue of testFilterResultsExpanded: the user reads one
     * observation (by id filter) with the Datastream-Thing-Projects and
     * FeatureOfInterest-Projects expansions and must see the expected graph.
     */
    private void checkExpanded(String user, String filter, int obsIndex, int dsIndex, int thingIndex,
            List<Entity> thingProjects, boolean foiVisible, List<Entity> foiProjects) {
        final Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("entitySet", mdlSensing.etObservation.mainSet);
        arguments.put("filter", filter);
        arguments.put("expand", List.of(EXPAND_DS_THING_PROJECTS_FOI));
        arguments.put("version", V1_1);
        final CallToolResult result = callTool(user, "frost_query", arguments);
        assertFalse(isError(result), user + " must be able to query the expansion: " + textOf(result));
        final List<Map> value = valueOf(result);
        if (thingProjects.isEmpty() && !foiVisible) {
            // The observation itself is invisible to this user.
            assertTrue(value.isEmpty(), user + " must not see the observation.");
            return;
        }
        assertEquals(1, value.size(), user + " must see exactly the one filtered observation.");
        final Map observation = value.get(0);
        assertEquals(canon(OBSERVATIONS.get(obsIndex)), canonPk(observation.get("@iot.id")),
                "Wrong observation visible to " + user);

        final Map datastream = (Map) observation.get("Datastream");
        assertNotNull(datastream, "The observation must carry its Datastream.");
        assertEquals(canon(DATASTREAMS.get(dsIndex)), canonPk(datastream.get("@iot.id")),
                "Wrong Datastream visible to " + user);
        final Map thing = (Map) datastream.get("Thing");
        assertNotNull(thing, "The Datastream must carry its Thing.");
        assertEquals(canon(THINGS.get(thingIndex)), canonPk(thing.get("@iot.id")),
                "Wrong Thing visible to " + user);
        assertEquals(canonSet(thingProjects), idsOf(valueList(thing.get("Projects"))),
                "Wrong Projects visible on the Thing to " + user);

        final Object foi = observation.get("FeatureOfInterest");
        if (foiVisible) {
            assertNotNull(foi, "The observation must carry its FeatureOfInterest for " + user);
            final Map foiMap = (Map) foi;
            assertEquals(
                    FOI_IDS.get(canon(OBSERVATIONS.get(obsIndex))),
                    canonPk(foiMap.get("@iot.id")),
                    "Wrong FeatureOfInterest visible to " + user);
            assertEquals(canonSet(foiProjects), idsOf(valueList(foiMap.get("Projects"))),
                    "Wrong Projects visible on the FeatureOfInterest to " + user);
        } else {
            assertTrue(foi == null || !(foi instanceof Map) || ((Map) foi).get("Projects") == null,
                    user + " must not see the FoI projects.");
        }
    }

    private static List<Map> valueList(Object raw) {
        assertTrue(raw instanceof List, "Expected an array value: " + raw);
        return (List<Map>) raw;
    }

    @Test
    void test_18a_ObservationCreate() {
        LOGGER.info("  test_18a_ObservationCreate");
        final String set = mdlSensing.etObservation.mainSet;
        final long ds0Pk = (Long) DATASTREAMS.get(0).getPrimaryKeyValues().get(0);
        createForFail(OBS_CREATE_P2, set, observationEntity(OBS_CREATE_P2, ds0Pk), H403);
        createForFail(GLOBAL_OBSPROP_CREATE, set, observationEntity(GLOBAL_OBSPROP_CREATE, ds0Pk), H403);
        createForOk(OBS_CREATE_P1, set, observationEntity(OBS_CREATE_P1, ds0Pk), OBSERVATIONS,
                newObservationEntity(OBS_CREATE_P1));
        createForOk(GLOBAL_OBS_CREATE, set, observationEntity(GLOBAL_OBS_CREATE, ds0Pk), OBSERVATIONS,
                newObservationEntity(GLOBAL_OBS_CREATE));
    }

    private static Map<String, Object> observationEntity(String user, long datastreamPk) {
        final Map<String, Object> entity = new LinkedHashMap<>();
        entity.put("result", user + " Observation");
        entity.put("Datastream", Map.of("@iot.id", datastreamPk));
        return entity;
    }

    private Entity newObservationEntity(String user) {
        final Entity entity = mdlSensing.buildObservation()
                .setResult(user + " Observation")
                .setDatastream(DATASTREAMS.get(0))
                .build();
        return entity;
    }

    @Test
    void test_18b_ObservationCreateNewFoi() {
        LOGGER.info("  test_18b_ObservationCreateNewFoi");
        // Give the Thing of Datastream 0 an extra location, so new FoIs are
        // generated for new observations.
        final long thing0Pk = (Long) THINGS.get(0).getPrimaryKeyValues().get(0);
        final Map<String, Object> location = new LinkedHashMap<>();
        location.put("name", "testFoiGeneration");
        location.put("description", "Testing if FoI generation works");
        location.put("encodingType", CONTENT_TYPE_APPLICATION_GEOJSON);
        location.put("location", Map.of("type", "Point", "coordinates", List.of(10.0, 49.0)));
        location.put("Things", List.of(Map.of("@iot.id", thing0Pk)));
        final CallToolResult created = callTool(ADMIN, "frost_create", Map.of(
                "path", mdlSensing.etLocation.mainSet,
                "entity", location,
                "version", V1_1));
        assertFalse(isError(created), "Admin should be able to create the Location: " + textOf(created));

        final String set = mdlSensing.etObservation.mainSet;
        final long ds0Pk = (Long) DATASTREAMS.get(0).getPrimaryKeyValues().get(0);
        createForFail(OBS_CREATE_P2, set, observationEntity(OBS_CREATE_P2, ds0Pk), H403);
        createForOk(OBS_CREATE_P1, set, observationEntity(OBS_CREATE_P1, ds0Pk), OBSERVATIONS,
                newObservationEntity(OBS_CREATE_P1));
    }

    @Test
    void test_18c_ObsPropCreate() {
        LOGGER.info("  test_18c_ObsPropCreate");
        final String set = mdlSensing.etObservedProperty.mainSet;
        createForOk(WRITE, set, obsPropEntity(WRITE), O_PROPS, newObsPropEntity(WRITE));
        createForFail(READ, set, obsPropEntity(READ), H403);
        createForFail(GLOBAL_OBS_CREATE, set, obsPropEntity(GLOBAL_OBS_CREATE), H403);
        expectAnonRefused(set);
        createForOk(GLOBAL_OBSPROP_CREATE, set, obsPropEntity(GLOBAL_OBSPROP_CREATE), O_PROPS,
                newObsPropEntity(GLOBAL_OBSPROP_CREATE));
        createForFail(ADMIN_P1, set, obsPropEntity(ADMIN_P1), H403);
        createForFail(ADMIN_P2, set, obsPropEntity(ADMIN_P2), H403);
        createForFail(OBS_CREATE_P1, set, obsPropEntity(OBS_CREATE_P1), H403);
        createForFail(OBS_CREATE_P2, set, obsPropEntity(OBS_CREATE_P2), H403);
    }

    private static Map<String, Object> obsPropEntity(String user) {
        final Map<String, Object> entity = new LinkedHashMap<>();
        entity.put("name", user + " ObservedProperty");
        entity.put("definition", "http://example.org");
        entity.put("description", "An ObservedProperty made by " + user);
        return entity;
    }

    private Entity newObsPropEntity(String user) {
        return mdlSensing.buildObservedProperty()
                .setName(user + " ObservedProperty")
                .setDefinition("http://example.org")
                .setDescription("An ObservedProperty made by " + user)
                .build();
    }

    @Test
    void test_19a_LinkThingLocation() {
        LOGGER.info("  test_19a_LinkThingLocation");
        final Entity sourceThing = THINGS.get(0);
        final Entity targetLocation = LOCATIONS.get(0);
        final String navSet = mdlSensing.etLocation.mainSet;

        expectAnonRefused(navSet);
        linkForFail(READ, sourceThing, navSet, targetLocation, H403);
        linkForOk(WRITE, sourceThing, navSet, targetLocation);
        linkForOk(ADMIN, sourceThing, navSet, targetLocation);
        linkForOk(ADMIN_P1, sourceThing, navSet, targetLocation);
        linkForFail(ADMIN_P2, sourceThing, navSet, targetLocation, H404);
        linkForFail(OBS_CREATE_P1, sourceThing, navSet, targetLocation, H403);
        linkForFail(OBS_CREATE_P2, sourceThing, navSet, targetLocation, H404);
    }

    @Test
    void test_20a_ThingDelete() {
        LOGGER.info("  test_20a_ThingDelete");
        final String set = mdlSensing.etThing.mainSet;
        final Entity thing = THINGS.get(0);

        expectAnonRefused(set);
        deleteForFail(READ, set, thing, H403);
        deleteForFail(WRITE, set, thing, H403);
        deleteForFail(OBS_CREATE_P1, set, thing, H403);
        deleteForFail(OBS_CREATE_P2, set, thing, H403);
        deleteForFail(ADMIN_P2, set, thing, H403);
        deleteForOk(ADMIN_P1, set, thing, THINGS);
    }

    @Test
    void test_99_ChangePassword() {
        LOGGER.info("  test_99_ChangePassword");
        testChangePassword(READ, "read", Utils.getFromList(USERS, 7));
        testChangePassword(WRITE, "write", Utils.getFromList(USERS, 8));
        testChangePassword(ADMIN_P1, ADMIN_P1, USERS);
        testChangePassword(ADMIN_P2, ADMIN_P2, USERS);
        testChangePassword(OBS_CREATE_P1, OBS_CREATE_P1, Utils.getFromList(USERS, 5));
        testChangePassword(OBS_CREATE_P2, OBS_CREATE_P2, Utils.getFromList(USERS, 6));

        testChangePasswordFail(WRITE, READ, READ + "2");
        testChangePasswordFail(ADMIN_P1, OBS_CREATE_P1, OBS_CREATE_P1 + "2");
    }

    private void testChangePassword(String user, String pathUser, List<Entity> entityList) {
        final String newPath = pathUser + "2";
        final CallToolResult result = callTool(user, "frost_update", Map.of(
                "path", userPath(pathUser),
                "mode", "patch",
                "changes", newPatchingMap("userpass", newPath),
                "version", V1_1));
        assertFalse(isError(result), "User " + user + " should be able to update password: " + textOf(result));

        // The old session is stale now; reconnect with the new password and
        // check the still-visible user list with the fresh session.
        final McpSyncClient oldClient = MCP_CLIENTS.get(user);
        oldClient.close();
        MCP_CLIENTS.put(user, connectClient(user, newPath));
        assertVisibility(user, mdlProjects.etUser.mainSet, "", entityList);
    }

    private void testChangePasswordFail(String user, String targetUser, String newPassword) {
        final CallToolResult result = callTool(user, "frost_update", Map.of(
                "path", userPath(targetUser),
                "mode", "patch",
                "changes", newPatchingMap("userpass", newPassword),
                "version", V1_1));
        assertTrue(isError(result),
                "User " + user + " should NOT be able to update password for user " + targetUser + ": " + textOf(result));
    }

    public static void cleanup() {
        // Delete everything except the Users, using the admin MCP session;
        // MCP traffic is not part of the HTTP statistics.
        if (!MCP_CLIENTS.isEmpty()) {
            final McpSyncClient admin = MCP_CLIENTS.get(ADMIN);
            if (admin != null) {
                final List<String> sets = List.of(
                        mdlSensing.etThing.mainSet,
                        mdlProjects.etUserProjectRole.mainSet,
                        mdlSensing.etObservation.mainSet,
                        mdlSensing.etDatastream.mainSet,
                        mdlSensing.etObservedProperty.mainSet,
                        mdlSensing.etSensor.mainSet,
                        mdlSensing.etFeatureOfInterest.mainSet,
                        mdlSensing.etHistoricalLocation.mainSet,
                        mdlSensing.etLocation.mainSet,
                        mdlProjects.etProject.mainSet,
                        mdlProjects.etRole.mainSet);
                for (String set : sets) {
                    int lastSize = Integer.MAX_VALUE;
                    while (true) {
                        final CallToolResult queryResult = callTool(admin, "frost_query", args(set, null, true));
                        if (isError(queryResult)) {
                            LOGGER.warn("Could not query {} during cleanup: {}", set, textOf(queryResult));
                            break;
                        }
                        final List<Map> value = valueOf(queryResult);
                        if (value.isEmpty()) {
                            break;
                        }
                        if (value.size() >= lastSize) {
                            LOGGER.warn("Cleanup of {} made no progress; leaving {} entities: {}", set, value.size(), value);
                            break;
                        }
                        lastSize = value.size();
                        for (Map item : value) {
                            final Object id = item.get("@iot.id");
                            final String path = deletePathOf(set, item);
                            if (path == null) {
                                LOGGER.warn("Could not find id in {} during cleanup; item was {}", set, item);
                                continue;
                            }
                            final CallToolResult deleteResult = callTool(admin, "frost_delete", Map.of(
                                    "path", path,
                                    "version", V1_1));
                            if (isError(deleteResult)) {
                                LOGGER.warn("Could not delete {}({}) during cleanup; item was {} : {}", set, id, item, textOf(deleteResult));
                            }
                        }
                    }
                }
            }
        }
        for (McpSyncClient client : MCP_CLIENTS.values()) {
            try {
                client.close();
            } catch (Exception ex) {
                LOGGER.warn("Failed to close MCP client.", ex);
            }
        }
        MCP_CLIENTS.clear();
        anonConnected = false;
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
        PROJECTS.clear();
        USERS.clear();
        ROLES.clear();
        USER_PROJECT_ROLES.clear();
        serviceAdmin = null;
        AbstractTestClass.cleanup();
    }

}
