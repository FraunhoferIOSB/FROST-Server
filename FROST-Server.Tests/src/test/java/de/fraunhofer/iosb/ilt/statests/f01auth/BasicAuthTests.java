package de.fraunhofer.iosb.ilt.statests.f01auth;

import de.fraunhofer.iosb.ilt.sta.NotAuthorizedException;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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
public class BasicAuthTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthTests.class);

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final Properties SERVER_PROPERTIES = new Properties();

    static {
        SERVER_PROPERTIES.put("auth_provider", "de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider");
        SERVER_PROPERTIES.put("auth_allowAnonymousRead", "false");
        SERVER_PROPERTIES.put("auth_autoUpdateDatabase", "true");
        SERVER_PROPERTIES.put("auth_db_driver", "org.postgresql.Driver");
        SERVER_PROPERTIES.put("auth_db_url", TestSuite.getInstance().getPgConnectUrl());
        SERVER_PROPERTIES.put("auth_db_username", TestSuite.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth_db_password", TestSuite.VAL_PG_PASS);
    }

    private static SensorThingsService serviceAdmin;
    private static SensorThingsService serviceWrite;
    private static SensorThingsService serviceRead;
    private static SensorThingsService serviceAnon;

    public BasicAuthTests(ServerVersion version) throws ServiceFailureException, IOException, URISyntaxException {
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
        int expected = 200;
        getDatabaseStatus(serviceAdmin, expected);
    }

    @Test
    public void test02WriteUpdateDb() throws IOException {
        LOGGER.info("  test02WriteUpdateDb");
        int expected = 401;
        getDatabaseStatus(serviceWrite, expected);
    }

    @Test
    public void test03ReadUpdateDb() throws IOException {
        LOGGER.info("  test03ReadUpdateDb");
        int expected = 401;
        getDatabaseStatus(serviceRead, expected);
    }

    @Test
    public void test04AnonUpdateDb() throws IOException {
        LOGGER.info("  test04AnonUpdateDb");
        int expected = 401;
        getDatabaseStatus(serviceAnon, expected);
    }

    private void getDatabaseStatus(SensorThingsService service, int expectedResponse) throws IOException {
        HttpGet getUpdateDb = new HttpGet(service.getEndpoint() + "../DatabaseStatus");
        CloseableHttpResponse response = service.getClient().execute(getUpdateDb);
        int code = response.getStatusLine().getStatusCode();
        Assert.assertEquals("Unexpected return code", expectedResponse, code);
    }

    private static final String ADMIN_SHOULD_BE_ABLE_TO_CREATE = "Admin should be able to create.";
    private static final String ADMIN_SHOULD_BE_ABLE_TO_UPDATE = "Admin should be able to update.";
    private static final String ADMIN_SHOULD_BE_ABLE_TO_DELETE = "Admin should be able to delete.";

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

    private static final String WRITE_SHOULD_BE_ABLE_TO_CREATE = "Write should be able to create.";
    private static final String WRITE_SHOULD_BE_ABLE_TO_UPDATE = "Write should be able to update.";
    private static final String WRITE_SHOULD_NOT_BE_ABLE_TO_DELETE = "Write should NOT be able to delete.";

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
        } catch (NotAuthorizedException ex) {
            LOGGER.trace("This should happen.", ex);
        } catch (ServiceFailureException ex) {
            Assert.fail("Expected NotAuthorizedException, got " + ex);
        }
        EntityUtils.filterAndCheck(serviceWrite.things(), "", THINGS);
    }

    private static final String READ_SHOULD_NOT_BE_ABLE_TO_CREATE = "read should NOT be able to create.";
    private static final String READ_SHOULD_NOT_BE_ABLE_TO_UPDATE = "read should NOT be able to update.";
    private static final String READ_SHOULD_NOT_BE_ABLE_TO_DELETE = "read should NOT be able to delete.";

    @Test
    public void test13ReadCreate() {
        LOGGER.info("  test13ReadCreate");
        Thing thing = new Thing("ReadThing", "The Thing made by read.");
        try {
            serviceRead.create(thing);
            Assert.fail(READ_SHOULD_NOT_BE_ABLE_TO_CREATE);
        } catch (NotAuthorizedException ex) {
            LOGGER.trace("This should happen.", ex);
        } catch (ServiceFailureException ex) {
            Assert.fail("Expected NotAuthorizedException, got " + ex);
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
        } catch (NotAuthorizedException ex) {
            LOGGER.trace("This should happen.", ex);
        } catch (ServiceFailureException ex) {
            Assert.fail("Expected NotAuthorizedException, got " + ex);
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
        } catch (NotAuthorizedException ex) {
            LOGGER.trace("This should happen.", ex);
        } catch (ServiceFailureException ex) {
            Assert.fail("Expected NotAuthorizedException, got " + ex);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }
    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_READ = "anon should NOT be able to read.";
    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_CREATE = "anon should NOT be able to create.";
    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_UPDATE = "anon should NOT be able to update.";
    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_DELETE = "anon should NOT be able to delete.";

    @Test
    public void test17AnonCreate() {
        LOGGER.info("  test17AnonCreate");
        Thing thing = new Thing("AnonThing", "The Thing made by anonymous.");
        try {
            serviceAnon.create(thing);
            Assert.fail(ANON_SHOULD_NOT_BE_ABLE_TO_CREATE);
        } catch (NotAuthorizedException ex) {
            LOGGER.trace("This should happen.", ex);
        } catch (ServiceFailureException ex) {
            Assert.fail("Expected NotAuthorizedException, got " + ex);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    @Test
    public void test18AnonRead() {
        LOGGER.info("  test18AnonRead");
        try {
            serviceAnon.things().query().list();
            Assert.fail(ANON_SHOULD_NOT_BE_ABLE_TO_READ);
        } catch (NotAuthorizedException ex) {
            LOGGER.trace("This should happen.", ex);
        } catch (ServiceFailureException ex) {
            Assert.fail("Expected NotAuthorizedException, got " + ex);
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
        } catch (NotAuthorizedException ex) {
            LOGGER.trace("This should happen.", ex);
        } catch (ServiceFailureException ex) {
            Assert.fail("Expected NotAuthorizedException, got " + ex);
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
        } catch (NotAuthorizedException ex) {
            LOGGER.trace("This should happen.", ex);
        } catch (ServiceFailureException ex) {
            Assert.fail("Expected NotAuthorizedException, got " + ex);
        }
        EntityUtils.filterAndCheck(serviceRead.things(), "", THINGS);
    }

    public static void setAuth(SensorThingsService service, String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        URL url = service.getEndpoint();

        credsProvider.setCredentials(
                new AuthScope(url.getHost(), url.getPort()),
                new UsernamePasswordCredentials(username, password));

        HttpClientBuilder clientBuilder = HttpClients.custom()
                .useSystemProperties()
                .setDefaultCredentialsProvider(credsProvider);

        CloseableHttpClient httpclient = clientBuilder.build();
        service.setClient(httpclient);
    }

}
