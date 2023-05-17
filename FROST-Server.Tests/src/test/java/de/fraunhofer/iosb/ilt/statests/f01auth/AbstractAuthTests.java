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

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_200_OK;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_401_UNAUTHORIZED;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_403_FORBIDDEN;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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

    private static final String ANON_SHOULD_NOT_BE_ABLE_TO_READ = "anon should NOT be able to read.";
    private static final String ADMIN = "admin";
    private static final String WRITE = "write";
    private static final String READ = "read";
    private static final String ANONYMOUS = "anonymous";

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();

    private static SensorThingsService serviceAdmin;
    private static SensorThingsService serviceWrite;
    private static SensorThingsService serviceRead;
    private static SensorThingsService serviceAnon;

    private final boolean anonymousReadAllowed;
    private final AuthTestHelper ath;

    public AbstractAuthTests(ServerVersion serverVersion, Properties properties, boolean anonymousReadAllowed) {
        super(serverVersion, properties);
        this.anonymousReadAllowed = anonymousReadAllowed;
        this.ath = new AuthTestHelper(serverSettings);
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
            return new SensorThingsService(sSrvc.getModelRegistry(), new URL(serverSettings.getServiceUrl(version)));
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
        EntityUtils.deleteAll(serverSettings.hasTasking(version), serviceAdmin, sMdl, tMdl);
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
    void test05AdminCreate() {
        LOGGER.info("  test05AdminCreate");
        Entity thing = sMdl.newThing("AdminThing", "The Thing made by admin.");
        THINGS.add(thing);
        ath.createForOk(ADMIN, serviceAdmin, thing, serviceAdmin.dao(sMdl.etThing), THINGS);
    }

    @Test
    void test06AdminRead() {
        LOGGER.info("  test06AdminRead");
        EntityUtils.testFilterResults(serviceAdmin.dao(sMdl.etThing), "", THINGS);
    }

    @Test
    void test07AdminUpdate() {
        LOGGER.info("  test07AdminUpdate");
        Entity thing = THINGS.get(0);
        thing.setProperty(EP_DESCRIPTION, "Updated Thing made by admin.");
        ath.updateForOk(ADMIN, serviceAdmin, thing, serviceAdmin.dao(sMdl.etThing), THINGS);
    }

    @Test
    void test08AdminDelete() {
        LOGGER.info("  test08AdminDelete");
        Entity thing = THINGS.get(0);
        THINGS.remove(0);
        ath.deleteForOk(ADMIN, serviceAdmin, thing, serviceAdmin.dao(sMdl.etThing), THINGS);
    }

    @Test
    void test09WriteCreate() {
        LOGGER.info("  test09WriteCreate");
        Entity thing = sMdl.newThing("WriteThing", "The Thing made by write.");
        THINGS.add(thing);
        ath.createForOk(WRITE, serviceWrite, thing, serviceWrite.dao(sMdl.etThing), THINGS);
    }

    @Test
    void test10WriteRead() {
        LOGGER.info("  test10WriteRead");
        EntityUtils.testFilterResults(serviceWrite.dao(sMdl.etThing), "", THINGS);
    }

    @Test
    void test11WriteUpdate() {
        LOGGER.info("  test11WriteUpdate");
        Entity thing = THINGS.get(0);
        thing.setProperty(EP_DESCRIPTION, "Updated Thing made by write.");
        ath.updateForOk(WRITE, serviceWrite, thing, serviceWrite.dao(sMdl.etThing), THINGS);
    }

    @Test
    void test12WriteDelete() {
        LOGGER.info("  test12WriteDelete");
        Entity thing = THINGS.get(0);
        ath.deleteForFail(
                WRITE, serviceWrite, thing,
                serviceWrite.dao(sMdl.etThing), THINGS, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test13ReadCreate() {
        LOGGER.info("  test13ReadCreate");
        Entity thing = sMdl.newThing("ReadThing", "The Thing made by read.");
        ath.createForFail(
                READ, serviceRead, thing,
                serviceRead.dao(sMdl.etThing), THINGS,
                HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test14ReadRead() {
        LOGGER.info("  test14ReadRead");
        // Make sure there is something to read.
        Entity thing = sMdl.newThing("WriteThing", "The Thing made by write.");
        THINGS.add(thing);
        try {
            serviceWrite.create(thing);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to create test entity.");
        }

        EntityUtils.testFilterResults(serviceRead.dao(sMdl.etThing), "", THINGS);
    }

    @Test
    void test15ReadUpdate() {
        LOGGER.info("  test15ReadUpdate");
        Entity thing = THINGS.get(0).withOnlyId();
        thing.setProperty(EP_DESCRIPTION, "Read Updated Thing made by Admin.");
        ath.updateForFail(READ, serviceRead, thing,
                serviceRead.dao(sMdl.etThing), THINGS,
                HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test16ReadDelete() {
        LOGGER.info("  test16ReadDelete");
        Entity thing = THINGS.get(0);
        ath.deleteForFail(READ, serviceRead, thing,
                serviceRead.dao(sMdl.etThing), THINGS,
                HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test17AnonCreate() {
        LOGGER.info("  test17AnonCreate");
        Entity thing = sMdl.newThing("AnonThing", "The Thing made by anonymous.");
        ath.createForFail(
                ANONYMOUS, serviceAnon, thing,
                serviceRead.dao(sMdl.etThing), THINGS,
                HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test18AnonRead() {
        LOGGER.info("  test18AnonRead");
        if (anonymousReadAllowed) {
            EntityUtils.testFilterResults(serviceAnon.dao(sMdl.etThing), "", THINGS);
        } else {
            try {
                serviceAnon.dao(sMdl.etThing).query().list();
                fail(ANON_SHOULD_NOT_BE_ABLE_TO_READ);
            } catch (ServiceFailureException ex) {
                ath.expectStatusCodeException(ANON_SHOULD_NOT_BE_ABLE_TO_READ, ex, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
            }
        }
    }

    @Test
    void test19AnonUpdate() {
        LOGGER.info("  test19AnonUpdate");
        Entity thing = THINGS.get(0).withOnlyId();
        thing.setProperty(EP_DESCRIPTION, "Anon Updated Thing made by Admin.");
        ath.updateForFail(ANONYMOUS, serviceAnon, thing,
                serviceRead.dao(sMdl.etThing), THINGS,
                HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test20AnonDelete() {
        LOGGER.info("  test20AnonDelete");
        Entity thing = THINGS.get(0);
        ath.deleteForFail(ANONYMOUS, serviceAnon, thing,
                serviceRead.dao(sMdl.etThing), THINGS,
                HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

}
