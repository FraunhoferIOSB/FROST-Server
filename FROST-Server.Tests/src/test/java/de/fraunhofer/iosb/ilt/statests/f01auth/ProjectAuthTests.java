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
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResultsExpanded;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.JOIN_TIMEOUT;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.WAIT_AFTER_INSERT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Projects;
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
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.EntityCreator;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.MqttCreateTester;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.StringCreator;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.TestSubscription;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class ProjectAuthTests extends AbstractTestClass {

    private final int H200 = HTTP_CODE_200_OK;
    private final int H401 = HTTP_CODE_401_UNAUTHORIZED;
    private final int H403 = HTTP_CODE_403_FORBIDDEN;
    private final int H404 = HTTP_CODE_404_NOT_FOUND;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectAuthTests.class);

    protected static final SensorThingsV11Sensing mdlSensing = new SensorThingsV11Sensing();
    protected static final SensorThingsV11Projects mdlProjects = new SensorThingsV11Projects();
    protected static final SensorThingsService baseService = new SensorThingsService(mdlSensing, mdlProjects);

    protected static final List<Entity> THINGS = new ArrayList<>();
    protected static final List<Entity> LOCATIONS = new ArrayList<>();
    protected static final List<Entity> SENSORS = new ArrayList<>();
    protected static final List<Entity> O_PROPS = new ArrayList<>();
    protected static final List<Entity> DATASTREAMS = new ArrayList<>();
    protected static final List<Entity> OBSERVATIONS = new ArrayList<>();
    protected static final List<Entity> PROJECTS = new ArrayList<>();

    /**
     * Users: admin, AdminProject1, AdminProject2, GlobalObsCreater,
     * GlobalObsPropCreater, ObsCreaterProject1, ObsCreaterProject2, read, write
     */
    protected static final List<Entity> USERS = new ArrayList<>();
    protected static final List<Entity> ROLES = new ArrayList<>();
    protected static final List<Entity> USER_PROJECT_ROLES = new ArrayList<>();

    protected static final String ADMIN = "admin";
    protected static final String WRITE = "write";
    protected static final String READ = "read";
    protected static final String GLOBAL_OBS_CREATE = "GlobalObsCreater";
    protected static final String GLOBAL_OBSPROP_CREATE = "GlobalObsPropCreater";
    protected static final String ANONYMOUS = "anonymous";
    protected static final String ADMIN_P1 = "AdminProject1";
    protected static final String ADMIN_P2 = "AdminProject2";
    protected static final String OBS_CREATE_P1 = "ObsCreaterProject1";
    protected static final String OBS_CREATE_P2 = "ObsCreaterProject2";

    protected static SensorThingsService serviceAdmin;
    protected static SensorThingsService serviceWrite;
    protected static SensorThingsService serviceRead;
    protected static SensorThingsService serviceGlObsCr;
    protected static SensorThingsService serviceGlObsPropCr;
    protected static SensorThingsService serviceAnon;
    protected static SensorThingsService serviceAdminProject1;
    protected static SensorThingsService serviceAdminProject2;
    protected static SensorThingsService serviceObsCreaterProject1;
    protected static SensorThingsService serviceObsCreaterProject2;

    protected static EntityHelper2 ehAdmin;
    protected static EntityHelper2 ehAdminProject1;
    protected static EntityHelper2 ehAdminProject2;

    private static MqttHelper2 mqttHelperAdmin;
    private static MqttHelper2 mqttHelperWrite;
    private static MqttHelper2 mqttHelperRead;
    private static MqttHelper2 mqttHelperAnon;
    private static MqttHelper2 mqttHelperAdminProject1;
    private static MqttHelper2 mqttHelperAdminProject2;
    private static MqttHelper2 mqttHelperObsCreaterProject1;
    private static MqttHelper2 mqttHelperObsCreaterProject2;

    private final boolean anonymousReadAllowed;
    private final AuthTestHelper ath;

    protected static void addCommonProperties(Map<String, String> properties) {
        properties.put("plugins.coreModel.idType", "LONG");
        properties.put("plugins.projects.enable", "true");
        properties.put("plugins.projects.enableDefaultRules", "true");
        properties.put("plugins.modelLoader.enable", "true");
        properties.put("plugins.modelLoader.idType.Role", "STRING");
        properties.put("plugins.modelLoader.idType.User", "STRING");
        properties.put("persistence.idGenerationMode.Role", "ClientGeneratedOnly");
        properties.put("persistence.idGenerationMode.User", "ClientGeneratedOnly");
        properties.put("auth.mqtt.topicAllowList", "^/[a-zA-Z0-9_-]+\\((('[^']+')|([0-9]+))\\)/[a-zA-Z0-9_-]+$");
    }

    public ProjectAuthTests(ServerVersion version, Map<String, String> properties, boolean anonymousReadAllowed) {
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

        mqttHelperAdmin = new MqttHelper2(serviceAdmin, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs(), "TC-" + ADMIN);
        mqttHelperWrite = new MqttHelper2(serviceWrite, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs(), "TC-" + WRITE);
        mqttHelperRead = new MqttHelper2(serviceRead, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs(), "TC-" + READ);
        mqttHelperAnon = new MqttHelper2(serviceAnon, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs(), "TC-" + ANONYMOUS);
        mqttHelperAdminProject1 = new MqttHelper2(serviceAdminProject1, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs(), "TC-" + ADMIN_P1);
        mqttHelperAdminProject2 = new MqttHelper2(serviceAdminProject2, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs(), "TC-" + ADMIN_P2);
        mqttHelperObsCreaterProject1 = new MqttHelper2(serviceObsCreaterProject1, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs(), "TC-" + OBS_CREATE_P1);
        mqttHelperObsCreaterProject2 = new MqttHelper2(serviceObsCreaterProject2, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs(), "TC-" + OBS_CREATE_P2);
        createEntities();
    }

    protected EntityHelper2 setCaches(EntityHelper2 eh) {
        return eh.setCache(mdlSensing.etThing, THINGS)
                .setCache(mdlSensing.etDatastream, DATASTREAMS)
                .setCache(mdlSensing.etObservation, OBSERVATIONS)
                .setCache(mdlProjects.etProject, PROJECTS);
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

        USERS.add(mdlProjects.newUser("read", "read"));
        USERS.add(mdlProjects.newUser("write", "write"));
        USERS.add(mdlProjects.newUser("admin", "admin"));
        ROLES.add(mdlProjects.newRole("read", ""));
        ROLES.add(mdlProjects.newRole("create", ""));
        ROLES.add(mdlProjects.newRole("update", ""));
        ROLES.add(mdlProjects.newRole("delete", ""));
        ROLES.add(mdlProjects.newRole("admin", ""));
        ROLES.add(mdlProjects.newRole("obscreate", ""));
        ROLES.add(mdlProjects.newRole("obsupdate", ""));
        ROLES.add(mdlProjects.newRole("obsdelete", ""));
        ROLES.add(mdlProjects.newRole("obspropcreate", ""));
        ROLES.add(mdlProjects.newRole("obspropupdate", ""));
        ROLES.add(mdlProjects.newRole("obspropdelete", ""));
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
                        USERS.add(serviceAdmin.dao(mdlProjects.etUser).find(pk));
                        break;
                    case "projects":
                        PROJECTS.add(serviceAdmin.dao(mdlProjects.etProject).find(pk));
                        break;
                    case "roles":
                        ROLES.add(serviceAdmin.dao(mdlProjects.etRole).find(pk));
                        break;
                    case "userprojectroles":
                        USER_PROJECT_ROLES.add(serviceAdmin.dao(mdlProjects.etUserProjectRole).find(pk));
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
    void test_00_TriggerInit() throws IOException {
        LOGGER.info("  test_00_TriggerInit");
        EntityCreator creator = (user) -> mdlSensing.newSensor(
                user + " MQTT-Sensor",
                "A Sensor made by " + user + " using MQTT",
                "encodingType", "metadata");
        StringCreator filterCreator = (user) -> "name eq " + StringHelper.quoteForUrl(user + " MQTT-Sensor");
        String topic = version.urlPart + '/' + mdlSensing.etSensor.mainSet;

        List<MqttCreateTester> testers = new ArrayList<>();
        testers.add(new MqttCreateTester(mqttHelperAdmin, ehAdmin, ADMIN + "-0", creator, filterCreator, topic, mdlSensing.etSensor, true)
                .setReadRetries(100));

        for (var tester : testers) {
            tester.start();
        }
        for (var tester : testers) {
            tester.join(20_000);
            if (tester.hasCreatedEntity()) {
                LOGGER.info("Found Entity for {}: {}", tester.name, tester.getCreatedEntity());
                SENSORS.add(tester.getCreatedEntity());
            }
        }
        for (var tester : testers) {
            LOGGER.info("  User {}, {}, Message: {}", tester.name, tester.isSuccess(), tester.getMessage());
            assertTrue(tester.isSuccess(), tester.getMessage());
        }
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
        testFilterResults(ADMIN, serviceAdmin, mdlProjects.etProject, "", PROJECTS);
        testFilterResults(WRITE, serviceWrite, mdlProjects.etProject, "", PROJECTS);
        testFilterResults(READ, serviceRead, mdlProjects.etProject, "", PROJECTS);
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlProjects.etProject, "", Utils.getFromList(PROJECTS, 0));
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlProjects.etProject, "", H401);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlProjects.etProject, "", Utils.getFromList(PROJECTS, 0));
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlProjects.etProject, "", Utils.getFromList(PROJECTS, 0, 1));
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlProjects.etProject, "", Utils.getFromList(PROJECTS, 0));
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlProjects.etProject, "", Utils.getFromList(PROJECTS, 0, 1));
    }

    @Test
    void test_02b_ReadUserProjectRole() {
        LOGGER.info("  test_02b_ReadUserProjectRole");
        testFilterResults(serviceAdmin, mdlProjects.etUserProjectRole, "", USER_PROJECT_ROLES);
        testFilterResults(WRITE, serviceWrite, mdlProjects.etUserProjectRole, "", Collections.emptyList());
        testFilterResults(READ, serviceRead, mdlProjects.etUserProjectRole, "", Collections.emptyList());
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlProjects.etUserProjectRole, "", Collections.emptyList());
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlProjects.etUserProjectRole, "", H401, H403);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlProjects.etUserProjectRole, "", Utils.getFromList(USER_PROJECT_ROLES, 0, 1));
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlProjects.etUserProjectRole, "", Utils.getFromList(USER_PROJECT_ROLES, 2, 3));
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlProjects.etUserProjectRole, "", Collections.emptyList());
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlProjects.etUserProjectRole, "", Collections.emptyList());
    }

    @Test
    void test_02c_ReadUser() {
        LOGGER.info("  test_02c_ReadUser");
        testFilterResults(serviceAdmin, mdlProjects.etUser, "", USERS);
        testFilterResults(serviceWrite, mdlProjects.etUser, "", Utils.getFromList(USERS, 8));
        testFilterResults(serviceRead, mdlProjects.etUser, "", Utils.getFromList(USERS, 7));
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlProjects.etUser, "", Collections.emptyList());
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlProjects.etUser, "", H401, H403);
        }
        testFilterResults(serviceAdminProject1, mdlProjects.etUser, "", USERS);
        testFilterResults(serviceAdminProject2, mdlProjects.etUser, "", USERS);
        testFilterResults(serviceObsCreaterProject1, mdlProjects.etUser, "", Utils.getFromList(USERS, 5));
        testFilterResults(serviceObsCreaterProject2, mdlProjects.etUser, "", Utils.getFromList(USERS, 6));
    }

    @Test
    void test_02d_ReadRole() {
        LOGGER.info("  test_02d_ReadRole");
        testFilterResults(ADMIN, serviceAdmin, mdlProjects.etRole, "", ROLES);
        testFilterResults(WRITE, serviceWrite, mdlProjects.etRole, "", Collections.emptyList());
        testFilterResults(READ, serviceRead, mdlProjects.etRole, "", Collections.emptyList());
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlProjects.etRole, "", Collections.emptyList());
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlProjects.etRole, "", H401, H403);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlProjects.etRole, "", ROLES);
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlProjects.etRole, "", ROLES);
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlProjects.etRole, "", Collections.emptyList());
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlProjects.etRole, "", Collections.emptyList());
    }

    @Test
    void test_02e_ReadThings() {
        LOGGER.info("  test_02e_ReadThings");
        testFilterResults(ADMIN, serviceAdmin, mdlSensing.etThing, "", THINGS);
        testFilterResults(WRITE, serviceWrite, mdlSensing.etThing, "", THINGS);
        testFilterResults(READ, serviceRead, mdlSensing.etThing, "", THINGS);
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlSensing.etThing, "", Utils.getFromList(THINGS, 0, 2));
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlSensing.etThing, "", H401);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlSensing.etThing, "", Utils.getFromList(THINGS, 0, 2));
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlSensing.etThing, "", Utils.getFromList(THINGS, 0, 1, 2));
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlSensing.etThing, "", Utils.getFromList(THINGS, 0, 2));
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlSensing.etThing, "", Utils.getFromList(THINGS, 0, 1, 2));
    }

    @Test
    void test_02f_ReadDatastreams() {
        LOGGER.info("  test_02f_ReadDatastreams");
        testFilterResults(ADMIN, serviceAdmin, mdlSensing.etDatastream, "", DATASTREAMS);
        testFilterResults(WRITE, serviceWrite, mdlSensing.etDatastream, "", DATASTREAMS);
        testFilterResults(READ, serviceRead, mdlSensing.etDatastream, "", DATASTREAMS);
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlSensing.etDatastream, "", Utils.getFromList(DATASTREAMS, 0, 4));
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlSensing.etDatastream, "", H401);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlSensing.etDatastream, "", Utils.getFromList(DATASTREAMS, 0, 1, 4, 5));
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlSensing.etDatastream, "", Utils.getFromList(DATASTREAMS, 0, 2, 3, 4, 5));
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlSensing.etDatastream, "", Utils.getFromList(DATASTREAMS, 0, 1, 4, 5));
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlSensing.etDatastream, "", Utils.getFromList(DATASTREAMS, 0, 2, 3, 4, 5));
    }

    @Test
    void test_03a_CreateProject() {
        LOGGER.info("  test_03a_CreateProject");
        EntityCreator creator = (user) -> mdlProjects.newProject(user + "-Project", "A Project made by " + user);

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS, H403);
        createForFail(GLOBAL_OBS_CREATE, serviceGlObsCr, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS, H403);
        createForFail(GLOBAL_OBSPROP_CREATE, serviceGlObsPropCr, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS, anonymousReadAllowed ? H403 : H401);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlProjects.etProject), PROJECTS, H403);
    }

    @Test
    void test_03b_UpdateProject() {
        LOGGER.info("  test_03b_UpdateProject");
        final Entity original = PROJECTS.get(1);
        EntityCreator creator = (user) -> original.withOnlyPk().setProperty(EP_NAME, user + "-Edited");
        EntityCreator reset = (user) -> original.withOnlyPk().setProperty(EP_NAME, original.getProperty(EP_NAME));

        updateForFail(READ, serviceRead, creator, original, H403);
        updateForFail(GLOBAL_OBS_CREATE, serviceGlObsCr, creator, original, H403);
        updateForFail(GLOBAL_OBSPROP_CREATE, serviceGlObsPropCr, creator, original, H403);
        updateForFail(ANONYMOUS, serviceAnon, creator, original, anonymousReadAllowed ? H404 : H401);
        updateForFail(ADMIN_P1, serviceAdminProject1, creator, original, H404);
        updateForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, original, H403);
        updateForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, original, H404);
        updateForOk(ADMIN_P2, serviceAdminProject2, creator, EP_NAME);
        updateForOk(WRITE, serviceWrite, creator, EP_NAME);
        updateForOk(ADMIN, serviceAdmin, reset, EP_NAME);
    }

    @Test
    void test_03c_CreateUser() {
        LOGGER.info("  test_03c_CreateUser");
        EntityCreator creator = (user) -> mdlProjects.newUser(user + "-User", user + "-password");

        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlProjects.etUser), USERS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlProjects.etUser), USERS, H401, H403);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlProjects.etUser), USERS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlProjects.etUser), USERS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlProjects.etUser), USERS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlProjects.etUser), USERS, H403);
    }

    @Test
    void test_04a_PlainThingCreate() {
        LOGGER.info("  test_04a_PlainThingCreate");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + "Thing", "A Thing made by " + user);

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, anonymousReadAllowed ? H403 : H401);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
    }

    @Test
    void test_04b_ThingCreateForProject1() {
        LOGGER.info("  test_04b_ThingCreateForProject1");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + "Thing", "A Thing made by " + user)
                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(0).withOnlyPk());

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, anonymousReadAllowed ? H403 : H401);
        createForOk(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
    }

    @Test
    void test_04c_ThingCreateForProject1WithDatastream() {
        LOGGER.info("  test_04c_ThingCreateForProject1WithDatastream");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + "Thing", "A Thing made by " + user)
                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(0).withOnlyPk())
                .addNavigationEntity(
                        mdlSensing.npThingDatastreams,
                        mdlSensing.newDatastream("DeepInsertDs", "Ds created by deep insert", new UnitOfMeasurement("%", "%", "%"))
                                .setProperty(mdlSensing.npDatastreamSensor, SENSORS.get(0).withOnlyPk())
                                .setProperty(mdlSensing.npDatastreamObservedproperty, O_PROPS.get(0).withOnlyPk()));

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, anonymousReadAllowed ? H403 : H401);
        createForOk(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
    }

    @Test
    void test_04d_ThingCreateForProject1Mqtt() throws JsonProcessingException {
        LOGGER.info("  test_04d_ThingCreateForProject1Mqtt");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + " MQTT-Thing", "A Thing made by " + user + " using MQTT")
                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(0).withOnlyPk());
        StringCreator filterCreator = (user) -> "name eq " + StringHelper.quoteForUrl(user + " MQTT-Thing");
        String topic = version.urlPart + '/' + mdlSensing.etThing.mainSet;

        List<MqttCreateTester> testers = new ArrayList<>();
        if (anonymousReadAllowed) {
            testers.add(new MqttCreateTester(mqttHelperAnon, ehAdmin, ANONYMOUS, creator, filterCreator, topic, mdlSensing.etThing, false));
        } else {
            testers.add(new MqttCreateTester(mqttHelperAnon, ehAdmin, ANONYMOUS, creator, filterCreator, topic, mdlSensing.etThing, false)
                    .addExpectedException(org.eclipse.paho.client.mqttv3.MqttSecurityException.class));
        }
        testers.add(new MqttCreateTester(mqttHelperRead, ehAdmin, READ, creator, filterCreator, topic, mdlSensing.etThing, false));
        testers.add(new MqttCreateTester(mqttHelperWrite, ehAdmin, WRITE, creator, filterCreator, topic, mdlSensing.etThing, true));
        testers.add(new MqttCreateTester(mqttHelperAdminProject1, ehAdmin, ADMIN_P1, creator, filterCreator, topic, mdlSensing.etThing, true));
        testers.add(new MqttCreateTester(mqttHelperAdminProject2, ehAdmin, ADMIN_P2, creator, filterCreator, topic, mdlSensing.etThing, false));
        testers.add(new MqttCreateTester(mqttHelperObsCreaterProject1, ehAdmin, OBS_CREATE_P1, creator, filterCreator, topic, mdlSensing.etThing, false));
        testers.add(new MqttCreateTester(mqttHelperObsCreaterProject2, ehAdmin, OBS_CREATE_P2, creator, filterCreator, topic, mdlSensing.etThing, false));

        for (var tester : testers) {
            tester.start();
        }
        MqttHelper2.waitMillis(WAIT_AFTER_INSERT);
        for (var tester : testers) {
            tester.join(JOIN_TIMEOUT);
            if (tester.hasCreatedEntity()) {
                LOGGER.info("Found Entity for {}: {}", tester.name, tester.getCreatedEntity());
                THINGS.add(tester.getCreatedEntity());
            }
        }
        for (var tester : testers) {
            LOGGER.info("  User {}, {}, Message: {}", tester.name, tester.isSuccess(), tester.getMessage());
            assertTrue(tester.isSuccess(), tester.getMessage());
        }
    }

    @Test
    void test_05a_DatastreamRelinkToThing2() {
        LOGGER.info("  test_05a_DatastreamRelinkToThing2");
        EntityCreator creator = (user) -> DATASTREAMS.get(0).withOnlyPk()
                .setProperty(mdlSensing.npDatastreamThing, THINGS.get(1).withOnlyPk());
        EntityCreator reset = (user) -> DATASTREAMS.get(0).withOnlyPk()
                .setProperty(mdlSensing.npDatastreamThing, THINGS.get(0).withOnlyPk());
        Entity original = DATASTREAMS.get(0);

        updateForFail(READ, serviceRead, creator, original, H403);
        updateForFail(ANONYMOUS, serviceAnon, creator, original, anonymousReadAllowed ? H403 : H401);
        updateForFail(ADMIN_P1, serviceAdminProject1, creator, original, H403);
        updateForFail(ADMIN_P2, serviceAdminProject2, creator, original, H403);
        updateForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, original, H403);
        updateForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, original, H403);
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
            testFilterResults(ANONYMOUS, serviceAnon, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 16, 17, 18, 19));
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlSensing.etObservation, "", HTTP_CODE_401_UNAUTHORIZED);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
    }

    @Test
    void test_08b_ObservationReadFilter() {
        LOGGER.info("  test_08b_ObservationReadFilter");
        final String filter = "Datastreams/Observations/id eq " + StringHelper.quoteForUrl(OBSERVATIONS.get(8).getPrimaryKeyValues().get(0));
        testFilterResults(ADMIN, serviceAdmin, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(WRITE, serviceWrite, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(READ, serviceRead, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        if (anonymousReadAllowed) {
            testFilterResults(ANONYMOUS, serviceAnon, mdlSensing.etObservedProperty, filter, Collections.emptyList());
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlSensing.etObservedProperty, filter, H401);
        }
        testFilterResults(ADMIN_P1, serviceAdminProject1, mdlSensing.etObservedProperty, filter, Collections.emptyList());
        testFilterResults(ADMIN_P2, serviceAdminProject2, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(OBS_CREATE_P1, serviceObsCreaterProject1, mdlSensing.etObservedProperty, filter, Collections.emptyList());
        testFilterResults(OBS_CREATE_P2, serviceObsCreaterProject2, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
    }

    @Test
    void test_08c_DatastreamFromObservationRead() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("  test_08c_DatastreamFromObservationRead");

        URL link = serviceAdmin.getFullPath(OBSERVATIONS.get(8), mdlSensing.npObservationDatastream);
        fetchForCode(ADMIN, serviceAdmin, link, H200);
        fetchForCode(WRITE, serviceWrite, link, H200);
        fetchForCode(READ, serviceRead, link, H200);
        fetchForCode(ANONYMOUS, serviceAnon, link, anonymousReadAllowed ? H404 : H401);
        fetchForCode(ADMIN_P1, serviceAdminProject1, link, H404);
        fetchForCode(ADMIN_P2, serviceAdminProject2, link, H200);
        fetchForCode(OBS_CREATE_P1, serviceObsCreaterProject1, link, H404);
        fetchForCode(OBS_CREATE_P2, serviceObsCreaterProject2, link, H200);
    }

    @Test
    void test_08d_ObservationsFromDatastreamRead() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("  test_08d_ObservationsFromDatastreamRead");

        URL link = serviceAdmin.getFullPath(DATASTREAMS.get(1), mdlSensing.npDatastreamObservations);
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
    void test_08e_ObservationReadExpand_1() throws ServiceFailureException {
        LOGGER.info("  test_08e_ObservationReadExpand_1");
        final String filter = "id eq " + StringHelper.quoteForUrl(OBSERVATIONS.get(16).getPrimaryKeyValues().get(0));
        final String expand = "Datastream($select=id;$expand=Thing($select=id;$expand=Projects($select=id))),FeatureOfInterest($select=id;$expand=Projects($select=id))";

        Entity expectedAdmin = OBSERVATIONS.get(16)
                .withOnlyPk()
                .setProperty(
                        mdlSensing.npObservationDatastream,
                        DATASTREAMS.get(4).withOnlyPk()
                                .setProperty(
                                        mdlSensing.npDatastreamThing,
                                        THINGS.get(2).withOnlyPk()
                                                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(0))
                                                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(1))))
                .setProperty(
                        mdlSensing.npObservationFeatureofinterest,
                        OBSERVATIONS.get(16).getProperty(mdlSensing.npObservationFeatureofinterest).withOnlyPk()
                                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(0))
                                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(1)));
        Entity expectedP1 = OBSERVATIONS.get(16)
                .withOnlyPk()
                .setProperty(
                        mdlSensing.npObservationDatastream,
                        DATASTREAMS.get(4).withOnlyPk()
                                .setProperty(
                                        mdlSensing.npDatastreamThing,
                                        THINGS.get(2).withOnlyPk()
                                                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(0))))
                .setProperty(
                        mdlSensing.npObservationFeatureofinterest,
                        OBSERVATIONS.get(16).getProperty(mdlSensing.npObservationFeatureofinterest).withOnlyPk()
                                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(0)));
        testFilterResultsExpanded(ADMIN, serviceAdmin, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
        testFilterResultsExpanded(WRITE, serviceWrite, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
        testFilterResultsExpanded(READ, serviceRead, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
        if (anonymousReadAllowed) {
            testFilterResultsExpanded(ANONYMOUS, serviceAnon, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedP1));
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlSensing.etObservedProperty, filter, H401);
        }

        testFilterResultsExpanded(ADMIN_P1, serviceAdminProject1, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedP1));
        testFilterResultsExpanded(OBS_CREATE_P1, serviceObsCreaterProject1, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedP1));
        testFilterResultsExpanded(ADMIN_P2, serviceAdminProject2, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
        testFilterResultsExpanded(OBS_CREATE_P2, serviceObsCreaterProject2, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
    }

    @Test
    void test_08f_ObservationReadExpand_2() throws ServiceFailureException {
        LOGGER.info("  test_08f_ObservationReadExpand_2");
        final String filter = "id eq " + StringHelper.quoteForUrl(OBSERVATIONS.get(8).getPrimaryKeyValues().get(0));
        final String expand = "Datastream($select=id;$expand=Thing($select=id;$expand=Projects($select=id))),FeatureOfInterest($select=id;$expand=Projects($select=id))";

        Entity expectedAdmin = OBSERVATIONS.get(8)
                .withOnlyPk()
                .setProperty(
                        mdlSensing.npObservationDatastream,
                        DATASTREAMS.get(2).withOnlyPk()
                                .setProperty(
                                        mdlSensing.npDatastreamThing,
                                        THINGS.get(1).withOnlyPk()
                                                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(1))))
                .setProperty(
                        mdlSensing.npObservationFeatureofinterest,
                        OBSERVATIONS.get(8).getProperty(mdlSensing.npObservationFeatureofinterest).withOnlyPk()
                                .addNavigationEntity(mdlProjects.npThingProjects, PROJECTS.get(1)));

        testFilterResultsExpanded(ADMIN, serviceAdmin, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
        testFilterResultsExpanded(WRITE, serviceWrite, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
        testFilterResultsExpanded(READ, serviceRead, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
        if (anonymousReadAllowed) {
            testFilterResultsExpanded(ANONYMOUS, serviceAnon, mdlSensing.etObservation, filter, expand, Collections.emptyList());
        } else {
            filterForException(ANONYMOUS, serviceAnon, mdlSensing.etObservation, filter, H401);
        }
        testFilterResultsExpanded(ADMIN_P1, serviceAdminProject1, mdlSensing.etObservation, filter, expand, Collections.emptyList());
        testFilterResultsExpanded(OBS_CREATE_P1, serviceObsCreaterProject1, mdlSensing.etObservation, filter, expand, Collections.emptyList());
        testFilterResultsExpanded(ADMIN_P2, serviceAdminProject2, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
        testFilterResultsExpanded(OBS_CREATE_P2, serviceObsCreaterProject2, mdlSensing.etObservation, filter, expand, Arrays.asList(expectedAdmin));
    }

    @Test
    void test_09a_SubscribeObservations() {
        LOGGER.info("  test_09a_SubscribeObservations");
        final CompletableFuture<Entity> obsFuture0 = new CompletableFuture<>();
        final CompletableFuture<Entity> obsFuture1 = new CompletableFuture<>();
        final CompletableFuture<Entity> obsFuture2 = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity obs0 = EntityUtils.createObservation(
                    serviceAdmin,
                    ehAdmin.getCache(mdlSensing.etDatastream, 0),
                    0,
                    ZonedDateTime.parse("2024-01-01T00:00:00.000Z"),
                    ehAdmin.getCache(mdlSensing.etObservation));
            LOGGER.debug("Created {}", obs0);
            obsFuture0.complete(obs0);

            Entity obs1 = EntityUtils.createObservation(
                    serviceAdmin,
                    ehAdmin.getCache(mdlSensing.etDatastream, 1),
                    0,
                    ZonedDateTime.parse("2024-01-01T01:00:00.000Z"),
                    ehAdmin.getCache(mdlSensing.etObservation));
            LOGGER.debug("Created {}", obs1);
            obsFuture1.complete(obs1);

            Entity obs2 = EntityUtils.createObservation(
                    serviceAdmin,
                    ehAdmin.getCache(mdlSensing.etDatastream, 2),
                    0,
                    ZonedDateTime.parse("2024-02-02T02:00:00.000Z"),
                    ehAdmin.getCache(mdlSensing.etObservation));
            LOGGER.debug("Created {}", obs2);
            obsFuture2.complete(obs2);
            return null;
        };

        Entity ds0 = ehAdmin.getCache(mdlSensing.etDatastream, 0);
        String relationPathDs0 = ParserUtils.relationPath(ds0, mdlSensing.npDatastreamObservations);
        String dsTopic0 = "v1.1/" + relationPathDs0;

        Entity ds1 = ehAdmin.getCache(mdlSensing.etDatastream, 1);
        String relationPathDs1 = ParserUtils.relationPath(ds1, mdlSensing.npDatastreamObservations);
        String dsTopic1 = "v1.1/" + relationPathDs1;

        Entity ds2 = ehAdmin.getCache(mdlSensing.etDatastream, 2);
        String relationPathDs2 = ParserUtils.relationPath(ds2, mdlSensing.npDatastreamObservations);
        String dsTopic2 = "v1.1/" + relationPathDs2;

        final TestSubscription test1SubAdmin = new MqttHelper2.TestSubscription(mqttHelperAdmin, "v1.1/Observations")
                .setName(ADMIN + "-1")
                .addExpectedEntity(obsFuture0)
                .addExpectedEntity(obsFuture1)
                .addExpectedEntity(obsFuture2)
                .createReceivedListener(mdlSensing.etObservation);

        final TestSubscription test0SubDsAdmin = new MqttHelper2.TestSubscription(mqttHelperAdmin, dsTopic0)
                .setName(ADMIN + "-2")
                .addExpectedEntity(obsFuture0)
                .createReceivedListener(mdlSensing.etObservation);
        final TestSubscription test1SubDsAdmin = new MqttHelper2.TestSubscription(mqttHelperAdmin, dsTopic1)
                .setName(ADMIN + "-3")
                .addExpectedEntity(obsFuture1)
                .createReceivedListener(mdlSensing.etObservation);
        final TestSubscription test2SubDsAdmin = new MqttHelper2.TestSubscription(mqttHelperAdmin, dsTopic2)
                .setName(ADMIN + "-4")
                .addExpectedEntity(obsFuture2)
                .createReceivedListener(mdlSensing.etObservation);

        final TestSubscription test1SubAdminP1 = new MqttHelper2.TestSubscription(mqttHelperAdminProject1, "v1.1/Observations")
                .setName(ADMIN_P1 + "-1")
                .addExpectedError("Failed to subscribe to")
                .createReceivedListener(mdlSensing.etObservation);

        final TestSubscription test0SubDsAdminP1 = new MqttHelper2.TestSubscription(mqttHelperAdminProject1, dsTopic0)
                .setName(ADMIN_P1 + "-2")
                .addExpectedEntity(obsFuture0)
                .createReceivedListener(mdlSensing.etObservation);
        final TestSubscription test1SubDsAdminP1 = new MqttHelper2.TestSubscription(mqttHelperAdminProject1, dsTopic1)
                .setName(ADMIN_P1 + "-3")
                .addExpectedEntity(obsFuture1)
                .createReceivedListener(mdlSensing.etObservation);
        final TestSubscription test2SubDsAdminP1 = new MqttHelper2.TestSubscription(mqttHelperAdminProject1, dsTopic2)
                .setName(ADMIN_P1 + "-4")
                .addExpectedError("Failed to subscribe to")
                .createReceivedListener(mdlSensing.etObservation);

        final TestSubscription test1SubAdminP2 = new MqttHelper2.TestSubscription(mqttHelperAdminProject2, "v1.1/Observations")
                .setName(ADMIN_P2 + "-1")
                .addExpectedError("Failed to subscribe to")
                .createReceivedListener(mdlSensing.etObservation);

        final TestSubscription test0SubDsAdminP2 = new MqttHelper2.TestSubscription(mqttHelperAdminProject2, dsTopic0)
                .setName(ADMIN_P2 + "-2")
                .addExpectedEntity(obsFuture0)
                .createReceivedListener(mdlSensing.etObservation);
        final TestSubscription test1SubDsAdminP2 = new MqttHelper2.TestSubscription(mqttHelperAdminProject2, dsTopic1)
                .setName(ADMIN_P2 + "-3")
                .addExpectedError("Failed to subscribe to")
                .createReceivedListener(mdlSensing.etObservation);
        final TestSubscription test2SubDsAdminP2 = new MqttHelper2.TestSubscription(mqttHelperAdminProject2, dsTopic2)
                .setName(ADMIN_P2 + "-4")
                .addExpectedEntity(obsFuture2)
                .createReceivedListener(mdlSensing.etObservation);

        final TestSubscription test0SubDsAnon;
        final TestSubscription test1SubDsAnon;
        final TestSubscription test2SubDsAnon;
        if (anonymousReadAllowed) {
            test0SubDsAnon = new MqttHelper2.TestSubscription(mqttHelperAnon, dsTopic0)
                    .setName(ANONYMOUS + "-1")
                    .addExpectedEntity(obsFuture0)
                    .createReceivedListener(mdlSensing.etObservation);
            test1SubDsAnon = new MqttHelper2.TestSubscription(mqttHelperAnon, dsTopic1)
                    .setName(ANONYMOUS + "-2")
                    .addExpectedError("Failed to subscribe to")
                    .createReceivedListener(mdlSensing.etObservation);
            test2SubDsAnon = new MqttHelper2.TestSubscription(mqttHelperAnon, dsTopic2)
                    .setName(ANONYMOUS + "-3")
                    .addExpectedError("Failed to subscribe to")
                    .createReceivedListener(mdlSensing.etObservation);
        } else {
            test0SubDsAnon = new MqttHelper2.TestSubscription(mqttHelperAnon, dsTopic0)
                    .setName(ANONYMOUS + "-4")
                    .addExpectedError("MQTT connect failed")
                    .createReceivedListener(mdlSensing.etObservation);
            test1SubDsAnon = new MqttHelper2.TestSubscription(mqttHelperAnon, dsTopic1)
                    .setName(ANONYMOUS + "-5")
                    .addExpectedError("MQTT connect failed")
                    .createReceivedListener(mdlSensing.etObservation);
            test2SubDsAnon = new MqttHelper2.TestSubscription(mqttHelperAnon, dsTopic2)
                    .setName(ANONYMOUS + "-6")
                    .addExpectedError("MQTT connect failed")
                    .createReceivedListener(mdlSensing.etObservation);
        }

        MqttHelper2.MqttAction mqttAction = new MqttHelper2.MqttAction(insertAction)
                .add(test0SubDsAdmin)
                .add(test0SubDsAdminP1)
                .add(test0SubDsAdminP2)
                .add(test0SubDsAnon)
                .add(test1SubAdmin)
                .add(test1SubDsAdmin)
                .add(test1SubAdminP1)
                .add(test1SubDsAdminP1)
                .add(test1SubAdminP2)
                .add(test1SubDsAdminP2)
                .add(test1SubDsAnon)
                .add(test2SubDsAdmin)
                .add(test2SubDsAdminP1)
                .add(test2SubDsAdminP2)
                .add(test2SubDsAnon);
        mqttHelperAdmin.executeRequest(mqttAction);
    }

    @Test
    void test_18a_ObservationCreate() {
        LOGGER.info("  test_08e_ObservationCreate");
        EntityCreator creator = (user) -> mdlSensing.newObservation(user + " Observation", DATASTREAMS.get(0));

        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS, H403);
        createForFail(GLOBAL_OBSPROP_CREATE, serviceGlObsPropCr, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS, H403);
        createForOk(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS);
        createForOk(GLOBAL_OBS_CREATE, serviceGlObsCr, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS);
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
        createForFail(GLOBAL_OBS_CREATE, serviceGlObsCr, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
        createForOk(GLOBAL_OBSPROP_CREATE, serviceGlObsPropCr, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, anonymousReadAllowed ? H403 : H401);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etObservedProperty), O_PROPS, H403);
    }

    @Test
    void test_19a_ThingDelete() {
        LOGGER.info("  test_10a_ThingDelete");
        EntityCreator creator = (user) -> THINGS.get(0);

        deleteForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, anonymousReadAllowed ? H403 : H401);
        deleteForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForFail(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS, H403);
        deleteForOk(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
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

}
