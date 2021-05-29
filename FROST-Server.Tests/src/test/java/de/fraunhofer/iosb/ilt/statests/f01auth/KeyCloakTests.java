package de.fraunhofer.iosb.ilt.statests.f01auth;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.StatusCodeException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.sta.service.TokenManagerOpenIDConnect;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the getting and filtering JSON properties.
 *
 * @author Hylke van der Schaaf
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KeyCloakTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyCloakTests.class);

    private static final String ADMIN_SHOULD_BE_ABLE_TO_CREATE = "Admin should be able to create.";
    private static final String ADMIN_SHOULD_BE_ABLE_TO_UPDATE = "Admin should be able to update.";
    private static final String ADMIN_SHOULD_BE_ABLE_TO_DELETE = "Admin should be able to delete.";
    private static final String WRITE_SHOULD_BE_ABLE_TO_CREATE = "Write should be able to create.";
    private static final String WRITE_SHOULD_BE_ABLE_TO_UPDATE = "Write should be able to update.";
    private static final String WRITE_SHOULD_NOT_BE_ABLE_TO_DELETE = "Write should NOT be able to delete.";
    private static final String READ_SHOULD_NOT_BE_ABLE_TO_CREATE = "read should NOT be able to create.";
    private static final String READ_SHOULD_NOT_BE_ABLE_TO_UPDATE = "read should NOT be able to update.";
    private static final String READ_SHOULD_NOT_BE_ABLE_TO_DELETE = "read should NOT be able to delete.";
    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_READ = "anon should NOT be able to read.";
    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_CREATE = "anon should NOT be able to create.";
    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_UPDATE = "anon should NOT be able to update.";
    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_DELETE = "anon should NOT be able to delete.";

    private static final int HTTP_CODE_200_OK = 200;
    private static final int HTTP_CODE_401_UNAUTHORIZED = 401;
    private static final int HTTP_CODE_403_FORBIDDEN = 403;

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final Properties SERVER_PROPERTIES = new Properties();

    public static final String KEYCLOAK_FROST_CLIENT_ID = "frost-server";
    public static final String KEYCLOAK_FROST_CONFIG_SECRET = "5aa9087d-817f-47b6-92a1-2b5f7caac967";
    //public static final String KEYCLOAK_SERVER_URL = "http://localhost:8081/auth/realms/FROST-Test";
    public static final String KEYCLOAK_TOKEN_PATH = "/realms/FROST-Test/protocol/openid-connect/token";

    static {
        SERVER_PROPERTIES.put("auth_provider", "de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider");
        SERVER_PROPERTIES.put("auth_keycloakConfigUrl", TestSuite.getInstance().getKeycloak().getAuthServerUrl() + "/realms/FROST-Test/clients-registrations/install/" + KEYCLOAK_FROST_CLIENT_ID);
        SERVER_PROPERTIES.put("auth_keycloakConfigSecret", KEYCLOAK_FROST_CONFIG_SECRET);
        SERVER_PROPERTIES.put("auth_allowAnonymousRead", "false");
    }

    private static SensorThingsService serviceAdmin;
    private static SensorThingsService serviceWrite;
    private static SensorThingsService serviceRead;
    private static SensorThingsService serviceAnon;

    public KeyCloakTests(ServerVersion version) throws ServiceFailureException, IOException, URISyntaxException {
        super(version, SERVER_PROPERTIES);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        try {
            serviceAdmin = new SensorThingsService(new URL(serverSettings.getServiceUrl(version)));
            setAuth(serviceAdmin, "admin", "admin");
            serviceWrite = new SensorThingsService(new URL(serverSettings.getServiceUrl(version)));
            setAuth(serviceWrite, "write", "write");
            serviceRead = new SensorThingsService(new URL(serverSettings.getServiceUrl(version)));
            setAuth(serviceRead, "read", "read");
            serviceAnon = new SensorThingsService(new URL(serverSettings.getServiceUrl(version)));
        } catch (MalformedURLException ex) {
            LOGGER.error("Failed to create URL", ex);
        }
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    @AfterClass
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(serviceAdmin);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    @Test
    public void test01AdminUpdateDb() throws IOException {
        LOGGER.info("  test01AdminUpdateDb");
        //LOGGER.info("Admin Token: {}",getAccessToken("admin", "admin"));
        getDatabaseStatus(serviceAdmin, HTTP_CODE_200_OK);
    }

    @Test
    public void test02WriteUpdateDb() throws IOException {
        LOGGER.info("  test02WriteUpdateDb");
        getDatabaseStatus(serviceWrite, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    public void test03ReadUpdateDb() throws IOException {
        LOGGER.info("  test03ReadUpdateDb");
        getDatabaseStatus(serviceRead, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    public void test04AnonUpdateDb() throws IOException {
        LOGGER.info("  test04AnonUpdateDb");
        getDatabaseStatus(serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    private void getDatabaseStatus(SensorThingsService service, int... expectedResponse) throws IOException {
        HttpGet getUpdateDb = new HttpGet(service.getEndpoint() + "../DatabaseStatus");
        CloseableHttpResponse response = service.execute(getUpdateDb);
        int code = response.getStatusLine().getStatusCode();
        for (int expected : expectedResponse) {
            if (expected == code) {
                return;
            }
        }
        Assert.fail("Unexpected return code: " + code + ", expected one of " + Arrays.toString(expectedResponse));
    }

    private void expectStatusCodeException(String failMessage, Exception ex, int... expected) {
        if (ex instanceof StatusCodeException) {
            StatusCodeException scex = (StatusCodeException) ex;
            int got = scex.getStatusCode();
            for (int want : expected) {
                if (got == want) {
                    return;
                }
            }
        }
        LOGGER.error(failMessage, ex);
        Assert.fail(failMessage);
    }

    @Test
    public void test05AdminCreate() {
        LOGGER.info("  test05AdminCreate");
        Thing thing = new Thing("AdminThing", "The Thing made by admin.");
        THINGS.add(thing);
        try {
            serviceAdmin.create(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(ADMIN_SHOULD_BE_ABLE_TO_CREATE, ex);
            Assert.fail(ADMIN_SHOULD_BE_ABLE_TO_CREATE);
        }
    }

    @Test
    public void test06AdminRead() {
        LOGGER.info("  test06AdminRead");
        EntityUtils.filterAndCheck(serviceAdmin.things(), "", THINGS);
    }

    @Test
    public void test07AdminUpdate() {
        LOGGER.info("  test07AdminUpdate");
        Thing thing = THINGS.get(0);
        thing.setDescription("Updated Thing made by admin.");
        try {
            serviceAdmin.update(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(ADMIN_SHOULD_BE_ABLE_TO_UPDATE, ex);
            Assert.fail(ADMIN_SHOULD_BE_ABLE_TO_UPDATE);
        }
        EntityUtils.filterAndCheck(serviceAdmin.things(), "", THINGS);
    }

    @Test
    public void test08AdminDelete() {
        LOGGER.info("  test08AdminDelete");
        Thing thing = THINGS.get(0);
        THINGS.remove(0);
        try {
            serviceAdmin.delete(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(ADMIN_SHOULD_BE_ABLE_TO_DELETE, ex);
            Assert.fail(ADMIN_SHOULD_BE_ABLE_TO_DELETE);
        }
        EntityUtils.filterAndCheck(serviceAdmin.things(), "", THINGS);
    }

    @Test
    public void test09WriteCreate() {
        LOGGER.info("  test09WriteCreate");
        Thing thing = new Thing("WriteThing", "The Thing made by write.");
        THINGS.add(thing);
        try {
            serviceWrite.create(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(WRITE_SHOULD_BE_ABLE_TO_CREATE, ex);
            Assert.fail(WRITE_SHOULD_BE_ABLE_TO_CREATE);
        }
    }

    @Test
    public void test10WriteRead() {
        LOGGER.info("  test10WriteRead");
        EntityUtils.filterAndCheck(serviceWrite.things(), "", THINGS);
    }

    @Test
    public void test11WriteUpdate() {
        LOGGER.info("  test11WriteUpdate");
        Thing thing = THINGS.get(0);
        thing.setDescription("Updated Thing made by write.");
        try {
            serviceWrite.update(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(WRITE_SHOULD_BE_ABLE_TO_UPDATE, ex);
            Assert.fail(WRITE_SHOULD_BE_ABLE_TO_UPDATE);
        }
        EntityUtils.filterAndCheck(serviceWrite.things(), "", THINGS);
    }

    @Test
    public void test12WriteDelete() {
        LOGGER.info("  test12WriteDelete");
        Thing thing = THINGS.get(0);
        try {
            serviceWrite.delete(thing);
            Assert.fail(WRITE_SHOULD_NOT_BE_ABLE_TO_DELETE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(WRITE_SHOULD_NOT_BE_ABLE_TO_DELETE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.filterAndCheck(serviceWrite.things(), "", THINGS);
    }

    @Test
    public void test13ReadCreate() {
        LOGGER.info("  test13ReadCreate");
        Thing thing = new Thing("ReadThing", "The Thing made by read.");
        try {
            serviceRead.create(thing);
            Assert.fail(READ_SHOULD_NOT_BE_ABLE_TO_CREATE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(READ_SHOULD_NOT_BE_ABLE_TO_CREATE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    @Test
    public void test14ReadRead() {
        LOGGER.info("  test14ReadRead");
        // Make sure there is something to read.
        test09WriteCreate();
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    @Test
    public void test15ReadUpdate() {
        LOGGER.info("  test15ReadUpdate");
        Thing thing = THINGS.get(0).withOnlyId();
        thing.setDescription("Read Updated Thing made by Admin.");
        try {
            serviceRead.update(thing);
            Assert.fail(READ_SHOULD_NOT_BE_ABLE_TO_UPDATE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(READ_SHOULD_NOT_BE_ABLE_TO_UPDATE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    @Test
    public void test16ReadDelete() {
        LOGGER.info("  test16ReadDelete");
        Thing thing = THINGS.get(0);
        try {
            serviceRead.delete(thing);
            Assert.fail(READ_SHOULD_NOT_BE_ABLE_TO_DELETE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(READ_SHOULD_NOT_BE_ABLE_TO_DELETE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    @Test
    public void test17AnonCreate() {
        LOGGER.info("  test17AnonCreate");
        Thing thing = new Thing("AnonThing", "The Thing made by anonymous.");
        try {
            serviceAnon.create(thing);
            Assert.fail(ANON_SHOULD_NOT_BE_ABLE_TO_CREATE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_CREATE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    @Test
    public void test18AnonRead() {
        LOGGER.info("  test18AnonRead");
        try {
            serviceAnon.things().query().list();
            Assert.fail(ANON_SHOULD_NOT_BE_ABLE_TO_READ);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_READ, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
    }

    @Test
    public void test19AnonUpdate() {
        LOGGER.info("  test19AnonUpdate");
        Thing thing = THINGS.get(0).withOnlyId();
        thing.setDescription("Anon Updated Thing made by Admin.");
        try {
            serviceAnon.update(thing);
            Assert.fail(ANON_SHOULD_NOT_BE_ABLE_TO_UPDATE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_UPDATE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    @Test
    public void test20AnonDelete() {
        LOGGER.info("  test20AnonDelete");
        Thing thing = THINGS.get(0);
        try {
            serviceAnon.delete(thing);
            Assert.fail(ANON_SHOULD_NOT_BE_ABLE_TO_DELETE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_DELETE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    public static void setAuth(SensorThingsService service, String username, String password) {
        KeycloakContainer keycloak = TestSuite.getInstance().getKeycloak();
        service.setTokenManager(
                new TokenManagerOpenIDConnect()
                        .setTokenServerUrl(keycloak.getAuthServerUrl() + KEYCLOAK_TOKEN_PATH)
                        .setClientId(KEYCLOAK_FROST_CLIENT_ID)
                        .setUserName(username)
                        .setPassword(password)
        );
    }

}
