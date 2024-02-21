/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_NAME;
import static de.fraunhofer.iosb.ilt.statests.TestSuite.KEY_DB_NAME;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_200_OK;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_401_UNAUTHORIZED;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_403_FORBIDDEN;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_404_NOT_FOUND;
import static de.fraunhofer.iosb.ilt.statests.f01auth.SensorThingsUserModel.EP_USERNAME;
import static de.fraunhofer.iosb.ilt.statests.f01auth.SensorThingsUserModel.EP_USERPASS;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.filterForException;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostclient.utils.ParserUtils;
import de.fraunhofer.iosb.ilt.frostclient.utils.StringHelper;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchResponseJson;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.geojson.Point;
import org.json.JSONException;
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

    private final int H401 = HTTP_CODE_401_UNAUTHORIZED;
    private final int H403 = HTTP_CODE_403_FORBIDDEN;
    private final int H404 = HTTP_CODE_404_NOT_FOUND;
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FineGrainedAuthTests.class);

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    private static String modelUrl(String name) {
        return resourceUrl("finegrainedsecurity/model/", name);
    }

    private static String resourceUrl(String path, String name) {
        try {
            return IOUtils.resourceToURL(path + "/" + name, FineGrainedAuthTests.class.getClassLoader()).getFile();
        } catch (IOException ex) {
            LOGGER.error("Failed", ex);
            return "";
        }
    }

    static {
        SERVER_PROPERTIES.put("auth.provider", "de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider");
        SERVER_PROPERTIES.put("auth.authenticateOnly", "true");
        SERVER_PROPERTIES.put("auth.allowAnonymousRead", "false");
        SERVER_PROPERTIES.put("auth.autoUpdateDatabase", "true");
        final String dbName = "fineGrainedAuth";
        SERVER_PROPERTIES.put("auth.db.url", TestSuite.createDbUrl(dbName));
        SERVER_PROPERTIES.put("auth.db.driver", "org.postgresql.Driver");
        SERVER_PROPERTIES.put("auth.db.username", TestSuite.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth.db.password", TestSuite.VAL_PG_PASS);
        SERVER_PROPERTIES.put("auth.plainTextPassword", "false");
        SERVER_PROPERTIES.put(KEY_DB_NAME, dbName);

        SERVER_PROPERTIES.put("plugins.coreModel.idType", "LONG");
        SERVER_PROPERTIES.put("plugins.modelLoader.enable", "true");
        SERVER_PROPERTIES.put("plugins.modelLoader.modelPath", "");
        SERVER_PROPERTIES.put("plugins.modelLoader.modelFiles", modelUrl("Project.json") + ", " + modelUrl("Role.json") + ", " + modelUrl("User.json") + ", " + modelUrl("UserProjectRole.json"));
        SERVER_PROPERTIES.put("plugins.modelLoader.liquibasePath", "target/test-classes/finegrainedsecurity/liquibase");
        SERVER_PROPERTIES.put("plugins.modelLoader.liquibaseFiles", "tablesSecurityUPR.xml");
        SERVER_PROPERTIES.put("plugins.modelLoader.securityPath", "");
        SERVER_PROPERTIES.put("plugins.modelLoader.securityFiles", modelUrl("secUsers.json") + ", " + modelUrl("secDatastreams.json") + ", " + modelUrl("secObservations.json") + ", " + modelUrl("secProjects.json") + ", " + modelUrl("secThings.json"));
        SERVER_PROPERTIES.put("plugins.modelLoader.idType.Role", "STRING");
        SERVER_PROPERTIES.put("plugins.modelLoader.idType.User", "STRING");
        SERVER_PROPERTIES.put("persistence.idGenerationMode.Role", "ClientGeneratedOnly");
        SERVER_PROPERTIES.put("persistence.idGenerationMode.User", "ClientGeneratedOnly");
    }

    private static final SensorThingsSensingV11 mdlSensing = new SensorThingsSensingV11();
    private static final SensorThingsUserModel mdlUsers = new SensorThingsUserModel();
    private static final SensorThingsService baseService = new SensorThingsService(mdlSensing, mdlUsers);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> PROJECTS = new ArrayList<>();
    private static final List<Entity> USERS = new ArrayList<>();
    private static final List<Entity> ROLES = new ArrayList<>();
    private static final List<Entity> USER_PROJECT_ROLES = new ArrayList<>();

    private static final String ADMIN = "admin";
    private static final String WRITE = "write";
    private static final String READ = "read";
    private static final String ANONYMOUS = "anonymous";
    private static final String ADMIN_P1 = "AdminProject1";
    private static final String ADMIN_P2 = "AdminProject2";
    private static final String OBS_CREATE_P1 = "ObsCreaterProject1";
    private static final String OBS_CREATE_P2 = "ObsCreaterProject2";

    private static SensorThingsService serviceAdmin;
    private static SensorThingsService serviceWrite;
    private static SensorThingsService serviceRead;
    private static SensorThingsService serviceAnon;
    private static SensorThingsService serviceAdminProject1;
    private static SensorThingsService serviceAdminProject2;
    private static SensorThingsService serviceObsCreaterProject1;
    private static SensorThingsService serviceObsCreaterProject2;

    private final AuthTestHelper ath;

    public FineGrainedAuthTests(ServerVersion version) {
        super(version, SERVER_PROPERTIES);
        ath = new AuthTestHelper(serverSettings);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);

        serviceAdmin = AuthTestHelper.setAuthBasic(createService(), "admin", "admin");
        serviceWrite = AuthTestHelper.setAuthBasic(createService(), "write", "write");
        serviceRead = AuthTestHelper.setAuthBasic(createService(), "read", "read");
        serviceAnon = createService();
        serviceAdminProject1 = AuthTestHelper.setAuthBasic(createService(), "AdminProject1", "AdminProject1");
        serviceAdminProject2 = AuthTestHelper.setAuthBasic(createService(), "AdminProject2", "AdminProject2");
        serviceObsCreaterProject1 = AuthTestHelper.setAuthBasic(createService(), "ObsCreaterProject1", "ObsCreaterProject1");
        serviceObsCreaterProject2 = AuthTestHelper.setAuthBasic(createService(), "ObsCreaterProject2", "ObsCreaterProject2");
        createEntities();
    }

    public void createEntities() throws ServiceFailureException {
        USERS.add(mdlUsers.newUser("read", "read"));
        USERS.add(mdlUsers.newUser("write", "write"));
        USERS.add(mdlUsers.newUser("admin", "admin"));
        ROLES.add(mdlUsers.newRole("read", ""));
        ROLES.add(mdlUsers.newRole("create", ""));
        ROLES.add(mdlUsers.newRole("update", ""));
        ROLES.add(mdlUsers.newRole("delete", ""));
        ROLES.add(mdlUsers.newRole("admin", ""));
        try {
            HTTPMethods.doPost(serviceAdmin.getHttpClient(), serverSettings.getServiceRootUrl() + "/DatabaseStatus", "", "");

            String batchPostData = IOUtils.resourceToString("finegrainedsecurity/dataBatchPost.json", StandardCharsets.UTF_8, FineGrainedAuthTests.class.getClassLoader());
            String response = postBatch(batchPostData);
            BatchResponseJson result = new ObjectMapper().readValue(response, BatchResponseJson.class);
            for (BatchResponseJson.ResponsePart part : result.getResponses()) {
                final String location = part.getLocation();
                Object[] pk = pkFromSelfLink(location);
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

            OBSERVATIONS.addAll(serviceAdmin.query(mdlSensing.etObservation).top(100).list().toList());
        } catch (IOException ex) {
            LOGGER.error("Failed to read resource", ex);
        }
    }

    public static Object[] pkFromSelfLink(String selfLink) {
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

    private SensorThingsService createService() {
        if (!baseService.isEndpointSet()) {
            try {
                baseService.setEndpoint(new URL(serverSettings.getServiceUrl(version)));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
            }
        }
        try {
            return new SensorThingsService(baseService.getModelRegistry())
                    .setEndpoint(new URL(serverSettings.getServiceUrl(version)));
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
        }
    }

    private String postBatch(String body) {
        String urlString = serverSettings.getServiceUrl(version) + "/$batch";
        try {
            HTTPMethods.HttpResponse httpResponse = HTTPMethods.doPost(serviceAdmin.getHttpClient(), urlString, body, "application/json");
            assertEquals(200, httpResponse.code, "Batch response should be 200");
            return httpResponse.response;
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            fail("An Exception occurred during testing: " + e.getMessage());
            return null;
        }
    }

    @Test
    void test_01_UpdateDb() throws IOException {
        LOGGER.info("  test_01_UpdateDb");
        ath.getDatabaseStatus(serviceAdmin, HTTP_CODE_200_OK);
        ath.getDatabaseStatus(serviceWrite, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceRead, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceAdminProject1, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceAdminProject1, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceAdminProject2, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceAdminProject2, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceObsCreaterProject1, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceObsCreaterProject1, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatusIndirect(serviceObsCreaterProject2, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceObsCreaterProject2, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test_02a_ReadProjects() {
        LOGGER.info("  test_02a_ReadProjects");
        testFilterResults(serviceAdmin, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(serviceWrite, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(serviceRead, mdlUsers.etProject, "", PROJECTS);
        filterForException(serviceAnon, mdlUsers.etProject, "", HTTP_CODE_401_UNAUTHORIZED);
        testFilterResults(serviceAdminProject1, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(serviceAdminProject2, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(serviceObsCreaterProject1, mdlUsers.etProject, "", PROJECTS);
        testFilterResults(serviceObsCreaterProject2, mdlUsers.etProject, "", PROJECTS);
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
        updateForFail(ANONYMOUS, serviceAnon, creator, original, H401);
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
        filterForException(serviceWrite, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceRead, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceAnon, mdlUsers.etUserProjectRole, "", HTTP_CODE_401_UNAUTHORIZED);
        filterForException(serviceAdminProject1, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceAdminProject2, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceObsCreaterProject1, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceObsCreaterProject2, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
    }

    @Test
    void test_04a_ReadUser() {
        LOGGER.info("  test_04a_ReadUser");
        testFilterResults(serviceAdmin, mdlUsers.etUser, "", USERS);
        testFilterResults(serviceWrite, mdlUsers.etUser, "", Utils.getFromList(USERS, 1));
        testFilterResults(serviceRead, mdlUsers.etUser, "", Utils.getFromList(USERS, 0));
        filterForException(serviceAnon, mdlUsers.etUser, "", HTTP_CODE_401_UNAUTHORIZED);
        testFilterResults(serviceAdminProject1, mdlUsers.etUser, "", USERS);
        testFilterResults(serviceAdminProject2, mdlUsers.etUser, "", USERS);
        testFilterResults(serviceObsCreaterProject1, mdlUsers.etUser, "", Utils.getFromList(USERS, 4));
        testFilterResults(serviceObsCreaterProject2, mdlUsers.etUser, "", Utils.getFromList(USERS, 6));
    }

    @Test
    void test_04b_CreateUser() {
        LOGGER.info("  test_04b_CreateUser");
        EntityCreator creator = (user) -> mdlUsers.newUser(user + "-User", user + "-password");

        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H401);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlUsers.etUser), USERS, H403);
    }

    @Test
    void test_04c_ChangePassword() {
        LOGGER.info("  test_04c_ChangePassword");
        EntityCreator changed = (user) -> USERS.stream().filter(t -> t.getProperty(EP_USERNAME).equals(user)).findFirst().get()
                .setProperty(EP_USERPASS, user + "2");
        EntityCreator changedCopy = (user) -> USERS.stream().filter(t -> t.getProperty(EP_USERNAME).equals(user)).findFirst().get()
                .withOnlyPk()
                .setProperty(EP_USERPASS, user + "2");

        serviceRead = testChangePassword(READ, serviceRead, changed, Utils.getFromList(USERS, 0));
        serviceWrite = testChangePassword(WRITE, serviceWrite, changed, Utils.getFromList(USERS, 1));
        serviceAdminProject1 = testChangePassword(ADMIN_P1, serviceAdminProject1, changed, USERS);
        serviceAdminProject2 = testChangePassword(ADMIN_P2, serviceAdminProject2, changed, USERS);
        serviceObsCreaterProject1 = testChangePassword(OBS_CREATE_P1, serviceObsCreaterProject1, changed, Utils.getFromList(USERS, 4));
        serviceObsCreaterProject2 = testChangePassword(OBS_CREATE_P2, serviceObsCreaterProject2, changed, Utils.getFromList(USERS, 6));

        testChangePasswordFail(WRITE, serviceWrite, changedCopy, READ);
        testChangePasswordFail(ADMIN_P1, serviceAdminProject1, changedCopy, OBS_CREATE_P1);
    }

    private void testChangePasswordFail(String user, SensorThingsService service, EntityCreator creator, String user2) {
        LOGGER.debug("    {}", user);
        try {
            service.update(creator.create(user2));
            String failMessage = "User " + user + " should NOT be able to update password for user " + user2 + ".";
            LOGGER.error(failMessage);
            fail(failMessage);
        } catch (ServiceFailureException ex) {
            // Good!
        }
    }

    private SensorThingsService testChangePassword(String user, SensorThingsService service, EntityCreator creator, List<Entity> entityList) {
        LOGGER.debug("    {}", user);
        final Entity userEntity = creator.create(user);
        try {
            service.update(userEntity);
        } catch (ServiceFailureException ex) {
            String failMessage = "User " + user + " should be able to update password. Got " + ex.getMessage();
            LOGGER.error(failMessage, ex);
            fail(failMessage);
        }
        SensorThingsService newService = AuthTestHelper.setAuthBasic(createService(), user, userEntity.getProperty(EP_USERPASS));
        testFilterResults(newService, mdlUsers.etUser, "", entityList);
        return newService;
    }

    @Test
    void test_05_ReadRole() {
        LOGGER.info("  test_05_ReadRole");
        testFilterResults(serviceAdmin, mdlUsers.etRole, "", ROLES);
        filterForException(serviceWrite, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceRead, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceAnon, mdlUsers.etRole, "", HTTP_CODE_401_UNAUTHORIZED);
        filterForException(serviceAdminProject1, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceAdminProject2, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceObsCreaterProject1, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        filterForException(serviceObsCreaterProject2, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
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
        testFilterResults(serviceAdmin, mdlSensing.etObservation, "", OBSERVATIONS);
        testFilterResults(serviceWrite, mdlSensing.etObservation, "", OBSERVATIONS);
        testFilterResults(serviceRead, mdlSensing.etObservation, "", OBSERVATIONS);
        filterForException(serviceAnon, mdlSensing.etObservation, "", HTTP_CODE_401_UNAUTHORIZED);
        testFilterResults(serviceAdminProject1, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(serviceAdminProject2, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(serviceObsCreaterProject1, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23));
        testFilterResults(serviceObsCreaterProject2, mdlSensing.etObservation, "", Utils.getFromList(OBSERVATIONS, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
    }

    @Test
    void test_08b_ObservationReadFilter() {
        LOGGER.info("  test_08b_ObservationReadFilter");
        final String filter = "Datastreams/Observations/id eq " + StringHelper.quoteForUrl(OBSERVATIONS.get(0).getPrimaryKeyValues()[0]);
        testFilterResults(serviceAdmin, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(serviceWrite, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(serviceRead, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        filterForException(serviceAnon, mdlSensing.etObservedProperty, filter, HTTP_CODE_401_UNAUTHORIZED);
        testFilterResults(serviceAdminProject1, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(serviceAdminProject2, mdlSensing.etObservedProperty, filter, Collections.emptyList());
        testFilterResults(serviceObsCreaterProject1, mdlSensing.etObservedProperty, filter, Utils.getFromList(O_PROPS, 0));
        testFilterResults(serviceObsCreaterProject2, mdlSensing.etObservedProperty, filter, Collections.emptyList());
    }

    @Test
    void test_08c_DatastreamFromObservationRead() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("  test_08c_DatastreamFromObservationRead");

        URL link = serviceAdmin.getFullPath(OBSERVATIONS.get(0), mdlSensing.npObservationDatastream);
        fetchForCode(ADMIN, serviceAdmin, link, 200);
        fetchForCode(WRITE, serviceWrite, link, 200);
        fetchForCode(READ, serviceRead, link, 200);
        fetchForCode(ANONYMOUS, serviceAnon, link, 401);
        fetchForCode(ADMIN_P1, serviceAdminProject1, link, 200);
        fetchForCode(ADMIN_P2, serviceAdminProject2, link, 404);
        fetchForCode(OBS_CREATE_P1, serviceObsCreaterProject1, link, 200);
        fetchForCode(OBS_CREATE_P2, serviceObsCreaterProject2, link, 404);
    }

    @Test
    void test_08d_ObservationsFromDatastreamRead() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("  test_08d_ObservationsFromDatastreamRead");

        URL link = serviceAdmin.getFullPath(DATASTREAMS.get(0), mdlSensing.npDatastreamObservations);
        fetchForCode(ADMIN, serviceAdmin, link, 200);
        fetchForCode(WRITE, serviceWrite, link, 200);
        fetchForCode(READ, serviceRead, link, 200);
        fetchForCode(ANONYMOUS, serviceAnon, link, 401);
        fetchForCode(ADMIN_P1, serviceAdminProject1, link, 200);
        fetchForCode(ADMIN_P2, serviceAdminProject2, link, 404);
        fetchForCode(OBS_CREATE_P1, serviceObsCreaterProject1, link, 200);
        fetchForCode(OBS_CREATE_P2, serviceObsCreaterProject2, link, 404);
    }

    @Test
    void test_08e_ObservationCreate() {
        LOGGER.info("  test_08e_ObservationCreate");
        EntityCreator creator = (user) -> mdlSensing.newObservation(user + " Observation", DATASTREAMS.get(0));

        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS, H403);
        createForOk(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etObservation), OBSERVATIONS);
    }

    @Test
    void test_08f_ObservationCreateNewFoi() throws ServiceFailureException {
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
    void test_09_ObservedPropertyCreate() {
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
    void test_10a_ThingDelete() {
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

    private void fetchForCode(String user, SensorThingsService service, URL link, int... codesWant) throws URISyntaxException {
        HttpGet get = new HttpGet(link.toURI());
        try (CloseableHttpResponse response = service.getHttpClient().execute(get)) {
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

    private void createForFail(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList, int expectedCodes) {
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

    private void deleteForFail(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList, int expectedCodes) {
        ath.deleteForFail(user, service, creator.create(user), validateDoa, entityList, expectedCodes);
    }

    private static interface EntityCreator {

        public Entity create(String user);
    }
}
