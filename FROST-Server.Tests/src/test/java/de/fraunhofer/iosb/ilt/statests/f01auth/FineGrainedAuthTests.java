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
package de.fraunhofer.iosb.ilt.statests.f01auth;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_NAME;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_200_OK;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_401_UNAUTHORIZED;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_403_FORBIDDEN;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_404_NOT_FOUND;
import static de.fraunhofer.iosb.ilt.statests.f01auth.SensorThingsUserModel.EP_USERNAME;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.filterForException;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostclient.utils.ParserUtils;
import de.fraunhofer.iosb.ilt.frostclient.utils.StringHelper;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchResponseJson;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper2;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.geojson.Point;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for access rights checking with Basic Authentication.
 *
 * @author Hylke van der Schaaf
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class FineGrainedAuthTests extends AbstractTestClass {

    private final int H200 = HTTP_CODE_200_OK;
    private final int H401 = HTTP_CODE_401_UNAUTHORIZED;
    private final int H403 = HTTP_CODE_403_FORBIDDEN;
    private final int H404 = HTTP_CODE_404_NOT_FOUND;
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FineGrainedAuthTests.class);

    private static String modelUrl(String name) {
        return resourceUrl("finegrainedsecurity/model/", name);
    }

    private static String metaUrl(String name) {
        return resourceUrl("finegrainedsecurity/", name);
    }

    private static String resourceUrl(String path, String name) {
        try {
            return IOUtils.resourceToURL(path + name, FineGrainedAuthTests.class.getClassLoader()).getFile();
        } catch (IOException ex) {
            LOGGER.error("Failed", ex);
            return "";
        }
    }

    protected static final SensorThingsV11Sensing mdlSensing = new SensorThingsV11Sensing();
    protected static final SensorThingsUserModel mdlUsers = new SensorThingsUserModel();
    protected static final SensorThingsService baseService = new SensorThingsService(mdlSensing, mdlUsers);

    protected static final List<Entity> THINGS = new ArrayList<>();
    protected static final List<Entity> LOCATIONS = new ArrayList<>();
    protected static final List<Entity> SENSORS = new ArrayList<>();
    protected static final List<Entity> O_PROPS = new ArrayList<>();
    protected static final List<Entity> DATASTREAMS = new ArrayList<>();
    protected static final List<Entity> OBSERVATIONS = new ArrayList<>();
    protected static final List<Entity> PROJECTS = new ArrayList<>();
    protected static final List<Entity> USERS = new ArrayList<>();
    protected static final List<Entity> ROLES = new ArrayList<>();
    protected static final List<Entity> USER_PROJECT_ROLES = new ArrayList<>();

    protected static final String ADMIN = "admin";
    protected static final String WRITE = "write";
    protected static final String READ = "read";
    protected static final String ANONYMOUS = "anonymous";
    protected static final String ADMIN_P1 = "AdminProject1";
    protected static final String ADMIN_P2 = "AdminProject2";
    protected static final String OBS_CREATE_P1 = "ObsCreaterProject1";
    protected static final String OBS_CREATE_P2 = "ObsCreaterProject2";

    protected static SensorThingsService serviceAdmin;
    protected static SensorThingsService serviceWrite;
    protected static SensorThingsService serviceRead;
    protected static SensorThingsService serviceAnon;
    protected static SensorThingsService serviceAdminProject1;
    protected static SensorThingsService serviceAdminProject2;
    protected static SensorThingsService serviceObsCreaterProject1;
    protected static SensorThingsService serviceObsCreaterProject2;

    protected static EntityHelper2 ehAdmin;
    protected static EntityHelper2 ehAdminProject1;
    protected static EntityHelper2 ehAdminProject2;

    private static MqttHelper2 mqttHelperAdmin;
    private static MqttHelper2 mqttHelperAdminProject1;
    private static MqttHelper2 mqttHelperAdminProject2;

    private final boolean anonymousReadAllowed;
    private final AuthTestHelper ath;

    protected static void addCommonProperties(Map<String, String> properties) {
        properties.put("plugins.coreModel.idType", "LONG");
        properties.put("plugins.projects.enable", "false");
        properties.put("plugins.projects.enableDefaultRules", "true");
        properties.put("plugins.modelLoader.enable", "true");
        properties.put("plugins.modelLoader.modelPath", "");
        properties.put("plugins.modelLoader.modelFiles", modelUrl("Project.json") + ", " + modelUrl("Role.json") + ", " + modelUrl("User.json") + ", " + modelUrl("UserProjectRole.json"));
        properties.put("plugins.modelLoader.liquibasePath", "target/test-classes/finegrainedsecurity/liquibase");
        properties.put("plugins.modelLoader.liquibaseFiles", "tablesSecurityUPR.xml");
        properties.put("plugins.modelLoader.securityPath", "");
        properties.put("plugins.modelLoader.securityFiles", modelUrl("secUsers.json") + ", " + modelUrl("secDatastreams.json") + ", " + modelUrl("secObservations.json") + ", " + modelUrl("secProjects.json") + ", " + modelUrl("secThings.json"));
        properties.put("plugins.modelLoader.metadataData", "{\"conformance\": [\"testModel\"],\"testModel\": 4}");
        properties.put("plugins.modelLoader.metadataPath", "");
        properties.put("plugins.modelLoader.metadataFiles", metaUrl("metadata_1.json") + ", " + metaUrl("metadata_2.json"));
        properties.put("plugins.modelLoader.idType.Role", "STRING");
        properties.put("plugins.modelLoader.idType.User", "STRING");
        properties.put("persistence.idGenerationMode.Role", "ClientGeneratedOnly");
        properties.put("persistence.idGenerationMode.User", "ClientGeneratedOnly");
        properties.put("auth.mqtt.topicAllowList", "^/[a-zA-Z0-9_-]+\\((('[^']+')|([0-9]+))\\)/[a-zA-Z0-9_-]+$");
    }

    public FineGrainedAuthTests(ServerVersion version, Map<String, String> properties, boolean anonymousReadAllowed) {
        super(version, properties);
        this.anonymousReadAllowed = anonymousReadAllowed;
        ath = new AuthTestHelper(serverSettings);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        createServices();
        ehAdmin = setCaches(new EntityHelper2(serviceAdmin));
        ehAdminProject1 = setCaches(new EntityHelper2(serviceAdminProject1));
        ehAdminProject2 = setCaches(new EntityHelper2(serviceAdminProject2));

        mqttHelperAdmin = new MqttHelper2(serviceAdmin, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        mqttHelperAdminProject1 = new MqttHelper2(serviceAdminProject1, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        mqttHelperAdminProject2 = new MqttHelper2(serviceAdminProject2, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        createEntities();
    }

    protected EntityHelper2 setCaches(EntityHelper2 eh) {
        return eh.setCache(mdlSensing.etThing, THINGS)
                .setCache(mdlSensing.etDatastream, DATASTREAMS)
                .setCache(mdlSensing.etObservation, OBSERVATIONS)
                .setCache(mdlUsers.etProject, PROJECTS);
    }

    public abstract void createServices();

    public void createEntities() throws ServiceFailureException {
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

        USERS.add(mdlUsers.newUser("read", "read"));
        USERS.add(mdlUsers.newUser("write", "write"));
        USERS.add(mdlUsers.newUser("admin", "admin"));
        ROLES.add(mdlUsers.newRole("read", ""));
        ROLES.add(mdlUsers.newRole("create", ""));
        ROLES.add(mdlUsers.newRole("update", ""));
        ROLES.add(mdlUsers.newRole("delete", ""));
        ROLES.add(mdlUsers.newRole("admin", ""));
        try {
            HTTPMethods.doPost(serviceAdmin, serverSettings.getServiceRootUrl() + "/DatabaseStatus", "", "");

            String batchPostData = getBatchPostData();
            String response = postBatch(batchPostData);
            BatchResponseJson result = Utils.MAPPER.readValue(response, BatchResponseJson.class);
            LOGGER.info("  Posted Batch with {} results.", result.getResponses().size());
            for (BatchResponseJson.ResponsePart part : result.getResponses()) {
                final String location = part.getLocation();
                PkValue pk = pkFromSelfLink(location);
                final String type = typeFromSelfLink(location);
                switch (type) {
                    case "things":
                        THINGS.add(serviceAdmin.dao(mdlSensing.etThing).find(pk));
                        break;
                    case "locations":
                        LOCATIONS.add(serviceAdmin.dao(mdlSensing.etLocation).find(pk));
                        break;
                    case "sensors":
                        SENSORS.add(serviceAdmin.dao(mdlSensing.etSensor).find(pk));
                        break;
                    case "observedproperties":
                        O_PROPS.add(serviceAdmin.dao(mdlSensing.etObservedProperty).find(pk));
                        break;
                    case "observations":
                        OBSERVATIONS.add(serviceAdmin.dao(mdlSensing.etObservation).find(pk));
                        break;
                    case "datastreams":
                        DATASTREAMS.add(serviceAdmin.dao(mdlSensing.etDatastream).find(pk));
                        break;
                    case "users":
                        USERS.add(serviceAdmin.dao(mdlUsers.etUser).find(pk));
                        break;
                    case "projects":
                        PROJECTS.add(serviceAdmin.dao(mdlUsers.etProject).find(pk));
                        break;
                    case "roles":
                        ROLES.add(serviceAdmin.dao(mdlUsers.etRole).find(pk));
                        break;
                    case "userprojectroles":
                        USER_PROJECT_ROLES.add(serviceAdmin.dao(mdlUsers.etUserProjectRole).find(pk));
                        break;
                    default:
                        LOGGER.error("Type {} should not have been created.", type);
                        throw new IllegalArgumentException("Type " + type + " should not have been created.");

                }
            }
            Collections.sort(USERS, (o1, o2) -> o1.getProperty(EP_USERNAME).toLowerCase().compareTo(o2.getProperty(EP_USERNAME).toLowerCase()));
            OBSERVATIONS.addAll(serviceAdmin.query(mdlSensing.etObservation).top(100).orderBy("id").list().toList());
        } catch (IOException | RuntimeException ex) {
            LOGGER.error("Failed to read resource", ex);
        }
    }

    public abstract String getBatchPostData() throws IOException;

    public static PkValue pkFromSelfLink(String selfLink) {
        String idString = selfLink.substring(selfLink.indexOf('(') + 1, selfLink.indexOf(')'));
        return ParserUtils.tryToParse(idString);
    }

    private String typeFromSelfLink(String selfLink) {
        Pattern typePattern = Pattern.compile(".*\\/([^/(]+)\\(.*");
        Matcher matcher = typePattern.matcher(selfLink);
        if (!matcher.matches()) {
            LOGGER.error("Unknown entity type for: {}", selfLink);
        }
        return matcher.group(1).toLowerCase();
    }

    private String postBatch(String body) {
        String urlString = serverSettings.getServiceUrl(version) + "/$batch";
        HTTPMethods.HttpResponse httpResponse = HTTPMethods.doPost(serviceAdmin, urlString, body, "application/json");
        assertEquals(200, httpResponse.code, "Batch response should be 200");
        return httpResponse.response;
    }

    @Test
    void test_01_UpdateDb() throws IOException {
        LOGGER.info("  test_01_UpdateDb");
        ath.getDatabaseStatus(ADMIN, serviceAdmin, HTTP_CODE_200_OK);
        ath.getDatabaseStatus(WRITE, serviceWrite, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(READ, serviceRead, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceAnon, HTTP_CODE_401_UNAUTHORIZED);
        ath.getDatabaseStatus(ANONYMOUS, serviceAnon, HTTP_CODE_401_UNAUTHORIZED);
        ath.getDatabaseStatusIndirect(serviceAdminProject1, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(ADMIN_P1, serviceAdminProject1, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceAdminProject2, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(ADMIN_P2, serviceAdminProject2, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceObsCreaterProject1, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(OBS_CREATE_P1, serviceObsCreaterProject1, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceObsCreaterProject2, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(OBS_CREATE_P2, serviceObsCreaterProject2, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test_02a_ReadProjects() {
        LOGGER.info("  test_02a_ReadProjects");
        testFilterResults(ADMIN, serviceAdmin, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(WRITE, serviceWrite, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(READ, serviceRead, mdlUsers.etProject, "", PROJECTS);
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlUsers.etProject, "", PROJECTS);
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlUsers.etProject, "", H401);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlUsers.etProject, "", PROJECTS);
    }

    @Test
    void test_02b_CreateProject() {
        LOGGER.info("  test_02b_CreateProject");
        EntityCreator creator = (user) -> mdlUsers.newProject(user + "-Project", "A Project made by " + user);

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlUsers.etProject), PROJECTS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlUsers.etProject), PROJECTS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlUsers.etProject), PROJECTS, H401);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlUsers.etProject), PROJECTS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlUsers.etProject), PROJECTS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlUsers.etProject), PROJECTS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlUsers.etProject), PROJECTS, H403);
    }

    @Test
    void test_02c_UpdateProject() {
        LOGGER.info("  test_02c_UpdateProject");
        final Entity original = PROJECTS.get(0);
        EntityCreator creator = (user) -> original.withOnlyPk().setProperty(EP_NAME, user + "-Edited");
        EntityCreator reset = (user) -> original.withOnlyPk().setProperty(EP_NAME, original.getProperty(EP_NAME));

        updateForFail(READ, serviceRead, creator, original, H403);
        updateForFail(ANONYMOUS, serviceAnon, creator, original, H401, H403);
        updateForFail(ADMIN_P2, serviceAdminProject2, creator, original, H403);
        updateForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, original, H403);
        updateForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, original, H403);
        updateForOk(ADMIN_P1, serviceAdminProject1, creator, EP_NAME);
        updateForOk(WRITE, serviceWrite, creator, EP_NAME);
        updateForOk(ADMIN, serviceAdmin, reset, EP_NAME);
    }

    @Test
    void test_03_ReadUserProjectRole() {
        LOGGER.info("  test_03_ReadUserProjectRole");
        testFilterResults(serviceAdmin, mdlUsers.etUserProjectRole, "", USER_PROJECT_ROLES);
        filterForException(WRITE, serviceWrite, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(READ, serviceRead, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(ANONYMOUS, serviceAnon, mdlUsers.etUserProjectRole, "", anonymousReadAllowed ? new int[]{H404} : new int[]{H401, H403});
        filterForException(ADMIN_P1, serviceAdminProject1, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(ADMIN_P2, serviceAdminProject2, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(OBS_CREATE_P1, serviceObsCreaterProject1, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(OBS_CREATE_P2, serviceObsCreaterProject2, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
    }

    @Test
    void test_04a_ReadUser() {
        LOGGER.info("  test_04a_ReadUser");
        testFilterResults(serviceAdmin, mdlUsers.etUser, "", USERS);
        testFilterResults(serviceWrite, mdlUsers.etUser, "", Utils.getFromList(USERS, 6));
        testFilterResults(serviceRead, mdlUsers.etUser, "", Utils.getFromList(USERS, 5));
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlUsers.etUser, "", Collections.emptyList());
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlUsers.etUser, "", H401, H403);
        }
        testFilterResults(serviceAdminProject1, mdlUsers.etUser, "", USERS);
        testFilterResults(serviceAdminProject2, mdlUsers.etUser, "", USERS);
        testFilterResults(serviceObsCreaterProject1, mdlUsers.etUser, "", Utils.getFromList(USERS, 3));
        testFilterResults(serviceObsCreaterProject2, mdlUsers.etUser, "", Utils.getFromList(USERS, 4));
    }

    @Test
    void test_04b_CreateUser() {
        LOGGER.info("  test_04b_CreateUser");
        EntityCreator creator = (user) -> mdlUsers.newUser(user + "-User", user + "-password");

        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H401, H403);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
    }

    @Test
    void test_05_ReadRole() {
        LOGGER.info("  test_05_ReadRole");
        testFilterResults(ADMIN, serviceAdmin, mdlUsers.etRole, "", ROLES);
        filterForException(WRITE, serviceWrite, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(READ, serviceRead, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(ANONYMOUS, serviceAnon, mdlUsers.etRole, "", anonymousReadAllowed ? new int[]{H404} : new int[]{H401, H403});
        filterForException(ADMIN_P1, serviceAdminProject1, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(ADMIN_P2, serviceAdminProject2, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(OBS_CREATE_P1, serviceObsCreaterProject1, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(OBS_CREATE_P2, serviceObsCreaterProject2, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
    }

    @Test
    void test_06a_PlainThingCreate() {
        LOGGER.info("  test_06a_PlainThingCreate");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + "Thing", "A Thing made by " + user);

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H401);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
    }

    @Test
    void test_06b_ThingCreateForProject1() {
        LOGGER.info("  test_06b_ThingCreateForProject1");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + "Thing", "A Thing made by " + user)
                .addNavigationEntity(mdlUsers.npThingProjects, PROJECTS.get(0).withOnlyPk());

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H401);
        createForOk(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
    }

    @Test
    void test_06c_ThingCreateForProject1WithDatastream() {
        LOGGER.info("  test_06c_ThingCreateForProject1WithDatastream");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + "Thing", "A Thing made by " + user)
                .addNavigationEntity(mdlUsers.npThingProjects, PROJECTS.get(0).withOnlyPk())
                .addNavigationEntity(
                        mdlSensing.npThingDatastreams,
                        mdlSensing.newDatastream("DeepInsertDs", "Ds created by deep insert", new UnitOfMeasurement("%", "%", "%"))
                                .setProperty(mdlSensing.npDatastreamSensor, SENSORS.get(0).withOnlyPk())
                                .setProperty(mdlSensing.npDatastreamObservedproperty, O_PROPS.get(0).withOnlyPk()));

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H401);
        createForOk(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
    }

    @Test
    void test_07a_DatastreamRelinkToThing2() {
        LOGGER.info("  test_07a_DatastreamRelinkToThing2");
        EntityCreator creator = (user) -> DATASTREAMS.get(0).withOnlyPk()
                .setProperty(mdlSensing.npDatastreamThing, THINGS.get(1).withOnlyPk());
        EntityCreator reset = (user) -> DATASTREAMS.get(0).withOnlyPk()
                .setProperty(mdlSensing.npDatastreamThing, THINGS.get(0).withOnlyPk());
        Entity original = DATASTREAMS.get(0);

        updateForFail(READ, serviceRead, creator, original, H403);
        updateForFail(ANONYMOUS, serviceAnon, creator, original, H401);
        updateForFail(ADMIN_P1, serviceAdminProject1, creator, original, H403);
        updateForFail(ADMIN_P2, serviceAdminProject2, creator, original, H404);
        updateForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, original, H403);
        updateForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, original, H404);
        updateForOk(WRITE, serviceWrite, creator, mdlSensing.npDatastreamThing);
        updateForOk(ADMIN, serviceAdmin, reset, mdlSensing.npDatastreamThing);
    }

    @Test
    void test_08a_ObservationRead() {
        LOGGER.info("  test_08a_ObservationRead");
        testFilterResults(ADMIN, serviceAdmin, mdlSensing.etObservation, "", OBSERVATIONS);
        testFilterResults(WRITE, serviceWrite, mdlSensing.etObservation, "", OBSERVATIONS);
        testFilterResults(READ, serviceRead, mdlSensing.etObservation, "", OBSERVATIONS);
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlSensing.etObservation, "", Collections.emptyList());
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlSensing.etObservation, "", HTTP_CODE_401_UNAUTHORIZED);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
    }

    @Test
    void test_08b_ObservationReadFilter() {
        LOGGER.info("  test_08b_ObservationReadFilter");
        final String filter = "Datastreams/Observations/id eq " + StringHelper.quoteForUrl(OBSERVATIONS.get(0).getPrimaryKeyValues().get(0));
        testFilterResults(ADMIN, serviceAdmin, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(WRITE, serviceWrite, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(READ, serviceRead, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlSensing.etObservedProperty, filter, Collections.emptyList());
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlSensing.etObservedProperty, filter, H401);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlSensing.etObservedProperty, filter, Collections.emptyList());
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlSensing.etObservedProperty, filter, Collections.emptyList());
    }

    @Test
    void test_08c_DatastreamFromObservationRead() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("  test_08c_DatastreamFromObservationRead");

        URL link = serviceAdmin.getFullPath(OBSERVATIONS.get(0), mdlSensing.npObservationDatastream);
        fetchForCode(ADMIN, serviceAdmin, link, H200);
        fetchForCode(WRITE, serviceWrite, link, H200);
        fetchForCode(READ, serviceRead, link, H200);
        fetchForCode(ANONYMOUS, serviceAnon, link, anonymousReadAllowed ? H404 : H401);
        fetchForCode(ADMIN_P1, serviceAdminProject1, link, H200);
        fetchForCode(ADMIN_P2, serviceAdminProject2, link, H404);
        fetchForCode(OBS_CREATE_P1, serviceObsCreaterProject1, link, H200);
        fetchForCode(OBS_CREATE_P2, serviceObsCreaterProject2, link, H404);
    }

    @Test
    void test_08d_ObservationsFromDatastreamRead() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("  test_08d_ObservationsFromDatastreamRead");

        URL link = serviceAdmin.getFullPath(DATASTREAMS.get(0), mdlSensing.npDatastreamObservations);
        fetchForCode(ADMIN, serviceAdmin, link, H200);
        fetchForCode(WRITE, serviceWrite, link, H200);
        fetchForCode(READ, serviceRead, link, H200);
        fetchForCode(ANONYMOUS, serviceAnon, link, anonymousReadAllowed ? H404 : H401);
        fetchForCode(ADMIN_P1, serviceAdminProject1, link, H200);
        fetchForCode(ADMIN_P2, serviceAdminProject2, link, H404);
        fetchForCode(OBS_CREATE_P1, serviceObsCreaterProject1, link, H200);
        fetchForCode(OBS_CREATE_P2, serviceObsCreaterProject2, link, H404);
    }

    @Test
    void test_09a_SubscribeObservationDirect() {
        LOGGER.info("  test_09a_SubscribeObservationDirect");
        final CompletableFuture<Entity> obsFuture = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity obs = EntityUtils.createObservation(
                    serviceAdmin,
                    ehAdmin.getCache(mdlSensing.etDatastream, 0),
                    0,
                    ZonedDateTime.parse("2024-01-01T01:00:00.000Z"),
                    ehAdmin.getCache(mdlSensing.etObservation));
            LOGGER.debug("Created {}", obs);
            obsFuture.complete(obs);
            return null;
        };

        Entity ds0 = ehAdmin.getCache(mdlSensing.etDatastream, 0);
        String relationPath = ParserUtils.relationPath(ds0, mdlSensing.npDatastreamObservations);
        String dsTopic = "v1.1/" + relationPath;
        final MqttHelper2.TestSubscription testSubAdmin = new MqttHelper2.TestSubscription(mqttHelperAdmin, "v1.1/Observations")
                .addExpectedEntity(obsFuture)
                .createReceivedListener(mdlSensing.etObservation);

        final MqttHelper2.TestSubscription testSubDsAdmin = new MqttHelper2.TestSubscription(mqttHelperAdmin, dsTopic)
                .addExpectedEntity(obsFuture)
                .createReceivedListener(mdlSensing.etObservation);

        final MqttHelper2.TestSubscription testSubAdminP1 = new MqttHelper2.TestSubscription(mqttHelperAdminProject1, "v1.1/Observations")
                .addExpectedError("Failed to subscribe to")
                .createReceivedListener(mdlSensing.etObservation);

        final MqttHelper2.TestSubscription testSubDsAdminP1 = new MqttHelper2.TestSubscription(mqttHelperAdminProject1, dsTopic)
                .addExpectedEntity(obsFuture)
                .createReceivedListener(mdlSensing.etObservation);

        final MqttHelper2.TestSubscription testSubAdminP2 = new MqttHelper2.TestSubscription(mqttHelperAdminProject2, "v1.1/Observations")
                .addExpectedError("Failed to subscribe to")
                .createReceivedListener(mdlSensing.etObservation);

        final MqttHelper2.TestSubscription testSubDsAdminP2 = new MqttHelper2.TestSubscription(mqttHelperAdminProject2, dsTopic)
                .addExpectedError("Failed to subscribe to")
                .createReceivedListener(mdlSensing.etObservation);

        MqttHelper2.MqttAction mqttAction = new MqttHelper2.MqttAction(insertAction)
                .add(testSubAdmin)
                .add(testSubDsAdmin)
                .add(testSubAdminP1)
                .add(testSubDsAdminP1)
                .add(testSubAdminP2)
                .add(testSubDsAdminP2);
        mqttHelperAdmin.executeRequest(mqttAction);
    }

    @Test
    void test_18a_ObservationCreate() {
        LOGGER.info("  test_08e_ObservationCreate");
        EntityCreator creator = (user) -> mdlSensing.newObservation(user + " Observation", DATASTREAMS.get(0));

        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS, H403);
        createForOk(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS);
    }

    @Test
    void test_18b_ObservationCreateNewFoi() throws ServiceFailureException {
        LOGGER.info("  test_08f_ObservationCreateNewFoi");
        // Create a new Location for Thing 1, so a new FoI must be generated.
        Entity newLocation = mdlSensing.newLocation("testFoiGeneration", "Testing if FoI generation works", new Point(10.0, 49.0))
                .addNavigationEntity(mdlSensing.npLocationThings, THINGS.get(0));
        serviceAdmin.create(newLocation);

        EntityCreator creator = (user) -> mdlSensing.newObservation(user + " Observation", DATASTREAMS.get(0));

        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS, H403);
        createForOk(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS);
    }

    @Test
    void test_18c_ObservedPropertyCreate() {
        LOGGER.info("  test_09_ObservedPropertyCreate");
        EntityCreator creator = (user) -> mdlSensing.newObservedProperty(user + " ObservedProperty", "http://example.org", "An ObservedProperty made by " + user);

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H401);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
    }

    @Test
    void test_19a_ThingDelete() {
        LOGGER.info("  test_10a_ThingDelete");
        EntityCreator creator = (user) -> THINGS.get(0);

        deleteForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H401);
        deleteForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForFail(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForOk(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
    }

    @Test
    void test_20_TestLandingPage() {
        LOGGER.info("  test_11_TestLandingPage");
        try {
            JsonNode data = getRootUrl();
            JsonNode settings = data.get("serverSettings");
            JsonNode conformance = settings.get("conformance");
            Set<String> confItems = new HashSet<>();
            for (var item : conformance) {
                confItems.add(item.textValue());
            }
            assertTrue(confItems.contains("testModel"), "Conformance should contain 'testModel'");
            assertTrue(confItems.contains("testModel1"), "Conformance should contain 'testModel1'");
            assertTrue(confItems.contains("testModel2"), "Conformance should contain 'testModel2'");
            assertEquals(4, settings.findPath("testModel").intValue());
            assertEquals(5, settings.findPath("testModel1").intValue());
            assertEquals(6, settings.findPath("testModel2").findPath("value").intValue());
        } catch (NullPointerException | ParseException | IOException ex) {
            fail("Unexpected exception during test.", ex);
        }
    }

    private JsonNode getRootUrl() throws JsonProcessingException, ParseException, IOException {
        String urlString = serverSettings.getServiceUrl(version);
        HTTPMethods.HttpResponse responseMap = HTTPMethods.doGet(serviceAdmin, urlString);
        assertEquals(200, responseMap.code, () -> "Error fetching root URI: " + urlString);
        return Utils.MAPPER.readTree(responseMap.response);
    }

    private void fetchForCode(String user, SensorThingsService service, URL link, int... codesWant) throws URISyntaxException {
        HttpGet get = new HttpGet(link.toURI());
        try (CloseableHttpResponse response = service.execute(get)) {
            int codeGot = response.getStatusLine().getStatusCode();
            for (int codeWant : codesWant) {
                if (codeWant == codeGot) {
                    return;
                }
            }
            Assertions.fail("Expected one of " + Arrays.toString(codesWant) + " but got " + codeGot + " when " + user + " fetched " + link);
        } catch (IOException ex) {
            Assertions.fail(ex);
        }
    }

    private void createForOk(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList) {
        final Entity entity = creator.create(user);
        entityList.add(entity);
        ath.createForOk(user, service, entity, validateDoa, entityList);
    }

    private void createForFail(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList, int... expectedCodes) {
        ath.createForFail(user, service, creator.create(user), validateDoa, entityList, expectedCodes);
    }

    private void updateForOk(String user, SensorThingsService service, EntityCreator creator, NavigationPropertyEntity property) {
        final Entity entity = creator.create(user);
        ath.updateForOk(user, service, entity, property);
    }

    private void updateForOk(String user, SensorThingsService service, EntityCreator creator, EntityPropertyMain... properties) {
        final Entity entity = creator.create(user);
        ath.updateForOk(user, service, entity, properties);
    }

    private void updateForFail(String user, SensorThingsService service, EntityCreator creator, Entity original, int... expectedCodes) {
        ath.updateForFail(user, service, creator.create(user), serviceAdmin, original, expectedCodes);
    }

    private void deleteForOk(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList) {
        final Entity toDelete = creator.create(user);
        entityList.remove(EntityUtils.findEntityIn(toDelete, entityList));
        ath.deleteForOk(user, service, toDelete, validateDoa, entityList);
    }

    private void deleteForFail(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList, int... expectedCodes) {
        ath.deleteForFail(user, service, creator.create(user), validateDoa, entityList, expectedCodes);
    }

    public static interface EntityCreator {

        public Entity create(String user);
    }
}
