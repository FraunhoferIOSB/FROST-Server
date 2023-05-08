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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchResponseJson;
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

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final List<Id> PROJECTS = new ArrayList<>();
    private static final List<Id> USERS = new ArrayList<>();
    private static final List<Id> ROLES = new ArrayList<>();
    private static final List<Id> USER_PROJECT_ROLES = new ArrayList<>();

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
        try {
            HTTPMethods.doPost(serviceAdmin.getHttpClient(), serverSettings.getServiceRootUrl() + "/DatabaseStatus", "", "");

            String batchPostData = IOUtils.resourceToString("finegrainedsecurity/dataBatchPost.json", StandardCharsets.UTF_8, FineGrainedAuthTests.class.getClassLoader());
            String response = postBatch(batchPostData);
            BatchResponseJson result = new ObjectMapper().readValue(response, BatchResponseJson.class);
            for (BatchResponseJson.ResponsePart part : result.getResponses()) {
                final String location = part.getLocation();
                final Id id = idFromSelfLink(location);
                final String type = typeFromSelfLink(location);
                switch (type) {
                    case "things":
                        THINGS.add(serviceAdmin.things().find(id));
                        break;
                    case "locations":
                        LOCATIONS.add(serviceAdmin.locations().find(id));
                        break;
                    case "sensors":
                        SENSORS.add(serviceAdmin.sensors().find(id));
                        break;
                    case "observedproperties":
                        O_PROPS.add(serviceAdmin.observedProperties().find(id));
                        break;
                    case "observations":
                        OBSERVATIONS.add(serviceAdmin.observations().find(id));
                        break;
                    case "datastreams":
                        DATASTREAMS.add(serviceAdmin.datastreams().find(id));
                        break;
                    case "users":
                        USERS.add(id);
                        break;
                    case "projects":
                        PROJECTS.add(id);
                        break;
                    case "roles":
                        ROLES.add(id);
                        break;
                    case "userprojectprojects":
                        USER_PROJECT_ROLES.add(id);
                        break;
                    default:
                        LOGGER.error("Type {} should not have been created.", type);
                        throw new IllegalArgumentException("Type " + type + " should not have been created.");

                }
            }

            OBSERVATIONS.addAll(serviceAdmin.observations().query().top(100).list());
        } catch (IOException ex) {
            LOGGER.error("Failed to read resource", ex);
        }
    }

    public static Id idFromSelfLink(String selfLink) {
        String idString = selfLink.substring(selfLink.indexOf('(') + 1, selfLink.indexOf(')'));
        return Id.tryToParse(idString);
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
            return new SensorThingsService(new URL(serverSettings.getServiceUrl(version)));
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

}
