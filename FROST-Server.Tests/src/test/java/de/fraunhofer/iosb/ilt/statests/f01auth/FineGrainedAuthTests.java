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

import static de.fraunhofer.iosb.ilt.statests.TestSuite.KEY_DB_NAME;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_200_OK;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_401_UNAUTHORIZED;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_403_FORBIDDEN;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_404_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11;
import de.fraunhofer.iosb.ilt.frostclient.utils.ParserUtils;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchResponseJson;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
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

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FineGrainedAuthTests.class);

    private static final Properties SERVER_PROPERTIES = new Properties();

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
        SERVER_PROPERTIES.put(KEY_DB_NAME, dbName);

        SERVER_PROPERTIES.put("plugins.coreModel.idType", "LONG");
        SERVER_PROPERTIES.put("plugins.modelLoader.enable", "true");
        SERVER_PROPERTIES.put("plugins.modelLoader.modelPath", "");
        SERVER_PROPERTIES.put("plugins.modelLoader.modelFiles", modelUrl("Project.json") + ", " + modelUrl("Role.json") + ", " + modelUrl("User.json") + ", " + modelUrl("UserProjectRole.json"));
        SERVER_PROPERTIES.put("plugins.modelLoader.liquibasePath", "finegrainedsecurity/liquibase");
        SERVER_PROPERTIES.put("plugins.modelLoader.liquibaseFiles", "tablesSecurityUPR.xml");
        SERVER_PROPERTIES.put("plugins.modelLoader.securityPath", "");
        SERVER_PROPERTIES.put("plugins.modelLoader.securityFiles", modelUrl("secDatastreams.json") + ", " + modelUrl("secObservations.json") + ", " + modelUrl("secProjects.json") + ", " + modelUrl("secThings.json"));
        SERVER_PROPERTIES.put("plugins.modelLoader.idType.Role", "STRING");
        SERVER_PROPERTIES.put("plugins.modelLoader.idType.User", "STRING");
        SERVER_PROPERTIES.put("persistence.idGenerationMode.Role", "ClientGeneratedOnly");
        SERVER_PROPERTIES.put("persistence.idGenerationMode.User", "ClientGeneratedOnly");
    }

    private static final SensorThingsSensingV11 mdlSensing = new SensorThingsSensingV11();
    private static final SensorThingsUserModel mdlUsers = new SensorThingsUserModel(mdlSensing);

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
        try {
            return new SensorThingsService(mdlUsers.getModelRegistry(), new URL(serverSettings.getServiceUrl(version)));
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
    void test01AdminUpdateDb() throws IOException {
        LOGGER.info("  test01AdminUpdateDb");
        ath.getDatabaseStatus(serviceAdmin, HTTP_CODE_200_OK);
    }

    @Test
    void test02WriteUpdateDb() throws IOException {
        LOGGER.info("  test02WriteUpdateDb");
        ath.getDatabaseStatus(serviceWrite, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test03ReadUpdateDb() throws IOException {
        LOGGER.info("  test03ReadUpdateDb");
        ath.getDatabaseStatus(serviceRead, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test04AnonUpdateDb() throws IOException {
        LOGGER.info("  test04AnonUpdateDb");
        ath.getDatabaseStatusIndirect(serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test05AdminProject1UpdateDb() throws IOException {
        LOGGER.info("  test05AdminProject1UpdateDb");
        ath.getDatabaseStatusIndirect(serviceAdminProject1, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceAdminProject1, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test06AdminProject2UpdateDb() throws IOException {
        LOGGER.info("  test06AdminProject2UpdateDb");
        ath.getDatabaseStatusIndirect(serviceAdminProject2, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceAdminProject2, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test07ObsCreaterProject1UpdateDb() throws IOException {
        LOGGER.info("  test07ObsCreaterProject1UpdateDb");
        ath.getDatabaseStatusIndirect(serviceObsCreaterProject1, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceObsCreaterProject1, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test08ObsCreaterProject2UpdateDb() throws IOException {
        LOGGER.info("  test08ObsCreaterProject2UpdateDb");
        ath.getDatabaseStatusIndirect(serviceObsCreaterProject2, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus(serviceObsCreaterProject2, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test09ReadProjects() {
        EntityUtils.testFilterResults(serviceAdmin, mdlUsers.etProject, "", PROJECTS);
        EntityUtils.testFilterResults(serviceWrite, mdlUsers.etProject, "", PROJECTS);
        EntityUtils.testFilterResults(serviceRead, mdlUsers.etProject, "", PROJECTS);
        EntityUtils.filterForException(serviceAnon, mdlUsers.etProject, "", HTTP_CODE_401_UNAUTHORIZED);
        EntityUtils.testFilterResults(serviceAdminProject1, mdlUsers.etProject, "", PROJECTS);
        EntityUtils.testFilterResults(serviceAdminProject2, mdlUsers.etProject, "", PROJECTS);
        EntityUtils.testFilterResults(serviceObsCreaterProject1, mdlUsers.etProject, "", PROJECTS);
        EntityUtils.testFilterResults(serviceObsCreaterProject2, mdlUsers.etProject, "", PROJECTS);
    }

    @Test
    void test10ReadUserProjectRole() {
        EntityUtils.testFilterResults(serviceAdmin, mdlUsers.etUserProjectRole, "", USER_PROJECT_ROLES);
        EntityUtils.filterForException(serviceWrite, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceRead, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceAnon, mdlUsers.etUserProjectRole, "", HTTP_CODE_401_UNAUTHORIZED);
        EntityUtils.filterForException(serviceAdminProject1, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceAdminProject2, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceObsCreaterProject1, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceObsCreaterProject2, mdlUsers.etUserProjectRole, "", HTTP_CODE_404_NOT_FOUND);
    }

    @Test
    void test11ReadUser() {
        EntityUtils.testFilterResults(serviceAdmin, mdlUsers.etUser, "", USERS);
        EntityUtils.filterForException(serviceWrite, mdlUsers.etUser, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceRead, mdlUsers.etUser, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceAnon, mdlUsers.etUser, "", HTTP_CODE_401_UNAUTHORIZED);
        EntityUtils.filterForException(serviceAdminProject1, mdlUsers.etUser, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceAdminProject2, mdlUsers.etUser, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceObsCreaterProject1, mdlUsers.etUser, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceObsCreaterProject2, mdlUsers.etUser, "", HTTP_CODE_404_NOT_FOUND);
    }

    @Test
    void test12ReadRole() {
        EntityUtils.testFilterResults(serviceAdmin, mdlUsers.etRole, "", ROLES);
        EntityUtils.filterForException(serviceWrite, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceRead, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceAnon, mdlUsers.etRole, "", HTTP_CODE_401_UNAUTHORIZED);
        EntityUtils.filterForException(serviceAdminProject1, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceAdminProject2, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceObsCreaterProject1, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
        EntityUtils.filterForException(serviceObsCreaterProject2, mdlUsers.etRole, "", HTTP_CODE_404_NOT_FOUND);
    }

    @Test
    void test13PlainThingCreate() {
        LOGGER.info("  test09PlainThingCreate");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + "Thing", "A Thing made by " + user);

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
    }

    @Test
    void test14ThingCreateForProject1() {
        LOGGER.info("  test10Project1ThingCreate");
        EntityCreator creator = (user) -> mdlSensing.newThing(user + "Thing", "A Thing made by " + user)
                .addNavigationEntity(mdlUsers.npThingProjects, PROJECTS.get(0).withOnlyId());

        createForOk(WRITE, serviceWrite, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(READ, serviceRead, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ANONYMOUS, serviceAnon, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForOk(ADMIN_P1, serviceAdminProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(ADMIN_P2, serviceAdminProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(OBS_CREATE_P1, serviceObsCreaterProject1, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
        createForFail(OBS_CREATE_P2, serviceObsCreaterProject2, creator, serviceAdmin.dao(mdlSensing.etThing), THINGS);
    }

    private void createForOk(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList) {
        final Entity entity = creator.create(user);
        entityList.add(entity);
        ath.createForOk(user, service, entity, validateDoa, entityList);
    }

    private void createForFail(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList) {
        ath.createForFail(user, service, creator.create(user), validateDoa, entityList, HTTP_CODE_401_UNAUTHORIZED);
    }

    private void updateForOk(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList) {
        final Entity entity = creator.create(user);
        ath.updateForOk(user, service, entity, validateDoa, entityList);
    }

    private void updateForFail(String user, SensorThingsService service, EntityCreator creator, Dao validateDoa, List<Entity> entityList) {
        ath.updateForFail(user, service, creator.create(user), validateDoa, entityList, HTTP_CODE_401_UNAUTHORIZED);
    }

    private static interface EntityCreator {

        public Entity create(String user);
    }
}
