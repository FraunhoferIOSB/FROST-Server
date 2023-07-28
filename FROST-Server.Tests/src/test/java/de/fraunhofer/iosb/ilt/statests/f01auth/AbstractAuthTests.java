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

import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.StatusCodeException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class AbstractAuthTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuthTests.class);

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

    private static SensorThingsService serviceAdmin;
    private static SensorThingsService serviceWrite;
    private static SensorThingsService serviceRead;
    private static SensorThingsService serviceAnon;

    private final boolean anonymousReadAllowed;

    public AbstractAuthTests(ServerVersion serverVersion, Properties properties, boolean anonymousReadAllowed) {
        super(serverVersion, properties);
        this.anonymousReadAllowed = anonymousReadAllowed;
    }

    @Override
    protected void setUpVersion() {
        serviceAdmin = getServiceAdmin();
        serviceWrite = getServiceWrite();
        serviceRead = getServiceRead();
        serviceAnon = getServiceAnonymous();
    }

    protected SensorThingsService createService() {
        try {
            return new SensorThingsService(new URL(serverSettings.getServiceUrl(version)));
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
        }
    }

    /**
     * Get a SensorThingsService that uses an Admin user.
     *
     * @return A SensorThingsService that uses an Admin user.
     */
    public abstract SensorThingsService getServiceAdmin();

    /**
     * Get a SensorThingsService that uses a user with Read and Write (but not
     * Delete) rights.
     *
     * @return A SensorThingsService that uses a user with Read and Write (but
     * not Delete) rights.
     */
    public abstract SensorThingsService getServiceWrite();

    /**
     * Get a SensorThingsService that uses a user with only Read rights.
     *
     * @return A SensorThingsService that a user with only Read rights.
     */
    public abstract SensorThingsService getServiceRead();

    /**
     * Get a SensorThingsService that uses an Anonymous user.
     *
     * @return A SensorThingsService that uses an Anonymous user.
     */
    public abstract SensorThingsService getServiceAnonymous();

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(version, serverSettings, serviceAdmin);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    @Test
    void test01AdminUpdateDb() throws IOException {
        LOGGER.info("  test01AdminUpdateDb");
        getDatabaseStatus(serviceAdmin, HTTP_CODE_200_OK);
    }

    @Test
    void test02WriteUpdateDb() throws IOException {
        LOGGER.info("  test02WriteUpdateDb");
        getDatabaseStatus(serviceWrite, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test03ReadUpdateDb() throws IOException {
        LOGGER.info("  test03ReadUpdateDb");
        getDatabaseStatus(serviceRead, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test04AnonUpdateDb() throws IOException {
        LOGGER.info("  test04AnonUpdateDb");
        getDatabaseStatusIndirect(serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        getDatabaseStatus(serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    private void getDatabaseStatusIndirect(SensorThingsService service, int... expectedResponse) throws IOException {
        getDatabaseStatus(service, service.getEndpoint() + "../DatabaseStatus", expectedResponse);
    }

    private void getDatabaseStatus(SensorThingsService service, int... expectedResponse) throws IOException {
        getDatabaseStatus(service, serverSettings.getServiceRootUrl() + "/DatabaseStatus", expectedResponse);
    }

    private void getDatabaseStatus(SensorThingsService service, String url, int... expectedResponse) throws IOException {
        HttpGet getUpdateDb = new HttpGet(url);
        CloseableHttpResponse response = service.execute(getUpdateDb);
        int code = response.getStatusLine().getStatusCode();
        for (int expected : expectedResponse) {
            if (expected == code) {
                return;
            }
        }
        LOGGER.info("Failed response: {}", org.apache.http.util.EntityUtils.toString(response.getEntity()));
        fail("Unexpected return code: " + code + ", expected one of " + Arrays.toString(expectedResponse));
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
        fail(failMessage);
    }

    @Test
    void test05AdminCreate() {
        LOGGER.info("  test05AdminCreate");
        Thing thing = new Thing("AdminThing", "The Thing made by admin.");
        THINGS.add(thing);
        try {
            serviceAdmin.create(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(ADMIN_SHOULD_BE_ABLE_TO_CREATE, ex);
            fail(ADMIN_SHOULD_BE_ABLE_TO_CREATE);
        }
    }

    @Test
    void test06AdminRead() {
        LOGGER.info("  test06AdminRead");
        EntityUtils.testFilterResults(serviceAdmin.things(), "", THINGS);
    }

    @Test
    void test07AdminUpdate() {
        LOGGER.info("  test07AdminUpdate");
        Thing thing = THINGS.get(0);
        thing.setDescription("Updated Thing made by admin.");
        try {
            serviceAdmin.update(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(ADMIN_SHOULD_BE_ABLE_TO_UPDATE, ex);
            fail(ADMIN_SHOULD_BE_ABLE_TO_UPDATE);
        }
        EntityUtils.testFilterResults(serviceAdmin.things(), "", THINGS);
    }

    @Test
    void test08AdminDelete() {
        LOGGER.info("  test08AdminDelete");
        Thing thing = THINGS.get(0);
        THINGS.remove(0);
        try {
            serviceAdmin.delete(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(ADMIN_SHOULD_BE_ABLE_TO_DELETE, ex);
            fail(ADMIN_SHOULD_BE_ABLE_TO_DELETE);
        }
        EntityUtils.testFilterResults(serviceAdmin.things(), "", THINGS);
    }

    @Test
    void test09WriteCreate() {
        LOGGER.info("  test09WriteCreate");
        Thing thing = new Thing("WriteThing", "The Thing made by write.");
        THINGS.add(thing);
        try {
            serviceWrite.create(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(WRITE_SHOULD_BE_ABLE_TO_CREATE, ex);
            fail(WRITE_SHOULD_BE_ABLE_TO_CREATE);
        }
    }

    @Test
    void test10WriteRead() {
        LOGGER.info("  test10WriteRead");
        EntityUtils.testFilterResults(serviceWrite.things(), "", THINGS);
    }

    @Test
    void test11WriteUpdate() {
        LOGGER.info("  test11WriteUpdate");
        Thing thing = THINGS.get(0);
        thing.setDescription("Updated Thing made by write.");
        try {
            serviceWrite.update(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error(WRITE_SHOULD_BE_ABLE_TO_UPDATE, ex);
            fail(WRITE_SHOULD_BE_ABLE_TO_UPDATE);
        }
        EntityUtils.testFilterResults(serviceWrite.things(), "", THINGS);
    }

    @Test
    void test12WriteDelete() {
        LOGGER.info("  test12WriteDelete");
        Thing thing = THINGS.get(0);
        try {
            serviceWrite.delete(thing);
            fail(WRITE_SHOULD_NOT_BE_ABLE_TO_DELETE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(WRITE_SHOULD_NOT_BE_ABLE_TO_DELETE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.testFilterResults(serviceWrite.things(), "", THINGS);
    }

    @Test
    void test13ReadCreate() {
        LOGGER.info("  test13ReadCreate");
        Thing thing = new Thing("ReadThing", "The Thing made by read.");
        try {
            serviceRead.create(thing);
            fail(READ_SHOULD_NOT_BE_ABLE_TO_CREATE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(READ_SHOULD_NOT_BE_ABLE_TO_CREATE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.testFilterResults(serviceRead.things(), "", THINGS);
    }

    @Test
    void test14ReadRead() {
        LOGGER.info("  test14ReadRead");
        // Make sure there is something to read.
        Thing thing = new Thing("WriteThing", "The Thing made by write.");
        THINGS.add(thing);
        try {
            serviceWrite.create(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to create test entity.");
        }

        EntityUtils.testFilterResults(serviceRead.things(), "", THINGS);
    }

    @Test
    void test15ReadUpdate() {
        LOGGER.info("  test15ReadUpdate");
        Thing thing = THINGS.get(0).withOnlyId();
        thing.setDescription("Read Updated Thing made by Admin.");
        try {
            serviceRead.update(thing);
            fail(READ_SHOULD_NOT_BE_ABLE_TO_UPDATE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(READ_SHOULD_NOT_BE_ABLE_TO_UPDATE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.testFilterResults(serviceRead.things(), "", THINGS);
    }

    @Test
    void test16ReadDelete() {
        LOGGER.info("  test16ReadDelete");
        Thing thing = THINGS.get(0);
        try {
            serviceRead.delete(thing);
            fail(READ_SHOULD_NOT_BE_ABLE_TO_DELETE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(READ_SHOULD_NOT_BE_ABLE_TO_DELETE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.testFilterResults(serviceRead.things(), "", THINGS);
    }

    @Test
    void test17AnonCreate() {
        LOGGER.info("  test17AnonCreate");
        Thing thing = new Thing("AnonThing", "The Thing made by anonymous.");
        try {
            serviceAnon.create(thing);
            fail(ANON_SHOULD_NOT_BE_ABLE_TO_CREATE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_CREATE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.testFilterResults(serviceRead.things(), "", THINGS);
    }

    @Test
    void test18AnonRead() {
        LOGGER.info("  test18AnonRead");
        if (anonymousReadAllowed) {
            EntityUtils.testFilterResults(serviceAnon.things(), "", THINGS);
        } else {
            try {
                serviceAnon.things().query().list();
                fail(ANON_SHOULD_NOT_BE_ABLE_TO_READ);
            } catch (ServiceFailureException ex) {
                expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_READ, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
            }
        }
    }

    @Test
    void test19AnonUpdate() {
        LOGGER.info("  test19AnonUpdate");
        Thing thing = THINGS.get(0).withOnlyId();
        thing.setDescription("Anon Updated Thing made by Admin.");
        try {
            serviceAnon.update(thing);
            fail(ANON_SHOULD_NOT_BE_ABLE_TO_UPDATE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_UPDATE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.testFilterResults(serviceRead.things(), "", THINGS);
    }

    @Test
    void test20AnonDelete() {
        LOGGER.info("  test20AnonDelete");
        Thing thing = THINGS.get(0);
        try {
            serviceAnon.delete(thing);
            fail(ANON_SHOULD_NOT_BE_ABLE_TO_DELETE);
        } catch (ServiceFailureException ex) {
            expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_DELETE, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        }
        EntityUtils.testFilterResults(serviceRead.things(), "", THINGS);
    }

}
