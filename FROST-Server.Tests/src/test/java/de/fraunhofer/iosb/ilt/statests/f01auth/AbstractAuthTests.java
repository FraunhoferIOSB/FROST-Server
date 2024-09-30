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

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_200_OK;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_401_UNAUTHORIZED;
import static de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper.HTTP_CODE_403_FORBIDDEN;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper2;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.MqttAction;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.TestSubscription;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
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
    public static final String ADMIN = "admin";
    public static final String WRITE = "write";
    public static final String READ = "read";
    public static final String ANONYMOUS = "anonymous";

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();

    protected static SensorThingsService serviceAdmin;
    protected static SensorThingsService serviceWrite;
    protected static SensorThingsService serviceRead;
    protected static SensorThingsService serviceAnon;
    private static EntityHelper2 ehAdmin;
    private static EntityHelper2 ehWrite;
    private static EntityHelper2 ehRead;
    private static EntityHelper2 ehAnon;

    private final boolean anonymousReadAllowed;
    private final AuthTestHelper ath;
    private static MqttHelper2 mqttHelperAdmin;
    private static MqttHelper2 mqttHelperWrite;
    private static MqttHelper2 mqttHelperRead;
    private static MqttHelper2 mqttHelperAnon;

    public AbstractAuthTests(ServerVersion serverVersion, Map<String, String> properties, boolean anonymousReadAllowed) {
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
        ehAdmin = new EntityHelper2(serviceAdmin);
        ehWrite = new EntityHelper2(serviceWrite);
        ehRead = new EntityHelper2(serviceRead);
        ehAnon = new EntityHelper2(serviceAnon);
        mqttHelperAdmin = new MqttHelper2(serviceAdmin, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        mqttHelperWrite = new MqttHelper2(serviceWrite, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        mqttHelperRead = new MqttHelper2(serviceRead, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        mqttHelperAnon = new MqttHelper2(serviceAnon, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
    }

    protected SensorThingsService createService() {
        if (!sSrvc.isBaseUrlSet()) {
            try {
                sSrvc.setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                        .init();
            } catch (MalformedURLException | URISyntaxException ex) {
                throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
            }
        }
        try {
            return new SensorThingsService(sSrvc.getModelRegistry())
                    .setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                    .init();
        } catch (MalformedURLException | URISyntaxException ex) {
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
        EntityUtils.deleteAll(serviceAdmin);
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
        ath.getDatabaseStatus("admin", serviceAdmin, HTTP_CODE_200_OK);
    }

    @Test
    void test02WriteUpdateDb() throws IOException {
        LOGGER.info("  test02WriteUpdateDb");
        ath.getDatabaseStatus("write", serviceWrite, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test03ReadUpdateDb() throws IOException {
        LOGGER.info("  test03ReadUpdateDb");
        ath.getDatabaseStatus("read", serviceRead, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
    }

    @Test
    void test04AnonUpdateDb() throws IOException {
        LOGGER.info("  test04AnonUpdateDb");
        ath.getDatabaseStatusIndirect(serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
        ath.getDatabaseStatus("anonymous", serviceAnon, HTTP_CODE_401_UNAUTHORIZED, HTTP_CODE_403_FORBIDDEN);
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
        EntityUtils.testFilterResults(ADMIN, serviceAdmin.dao(sMdl.etThing), "", THINGS);
    }

    @Test
    void test07AdminUpdate() {
        LOGGER.info("  test07AdminUpdate");
        final Entity original = THINGS.get(0);
        final Entity thing = original.withOnlyPk()
                .setProperty(EP_DESCRIPTION, "Thing Updated by admin.");
        ath.updateForOk(ADMIN, serviceAdmin, thing, EP_DESCRIPTION);
        original.setProperty(EP_DESCRIPTION, thing.getProperty(EP_DESCRIPTION));
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
        EntityUtils.testFilterResults(WRITE, serviceWrite.dao(sMdl.etThing), "", THINGS);
    }

    @Test
    void test11WriteUpdate() {
        LOGGER.info("  test11WriteUpdate");
        final Entity original = THINGS.get(0);
        final Entity thing = original.withOnlyPk()
                .setProperty(EP_DESCRIPTION, "Thing Updated by write.");
        ath.updateForOk(WRITE, serviceWrite, thing, EP_DESCRIPTION);
        original.setProperty(EP_DESCRIPTION, thing.getProperty(EP_DESCRIPTION));
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

        EntityUtils.testFilterResults(READ, serviceRead.dao(sMdl.etThing), "", THINGS);
    }

    @Test
    void test15ReadUpdate() {
        LOGGER.info("  test15ReadUpdate");
        final Entity original = THINGS.get(0);
        Entity thing = original.withOnlyPk()
                .setProperty(EP_DESCRIPTION, "Thing Updated by Read.");
        ath.updateForFail(READ, serviceRead, thing,
                serviceRead, original,
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
            EntityUtils.testFilterResults(ANONYMOUS, serviceAnon.dao(sMdl.etThing), "", THINGS);
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
        final Entity original = THINGS.get(0);
        final Entity thing = original.withOnlyPk();
        thing.setProperty(EP_DESCRIPTION, "Thing Updated by Anon.");
        ath.updateForFail(ANONYMOUS, serviceAnon, thing,
                serviceRead, original,
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

    @Test
    void test30SubscribeThingAdmin() {
        LOGGER.info("  test30SubscribeThingAdmin");
        final CompletableFuture<Entity> obsFuture = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity thing = EntityUtils.createThing(
                    serviceAdmin,
                    "newThing",
                    "A new Thing for Testing",
                    ehAdmin.getCache(sMdl.etThing));
            obsFuture.complete(thing);
            return null;
        };
        final TestSubscription testSubscription = new TestSubscription(mqttHelperAdmin, "v1.1/Things")
                .addExpectedEntity(obsFuture)
                .createReceivedListener(sMdl.etThing);
        MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription);
        mqttHelperAdmin.executeRequest(mqttAction);
    }

    @Test
    void test31SubscribeThingWrite() {
        LOGGER.info("  test31SubscribeThingWrite");
        final CompletableFuture<Entity> obsFuture = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity thing = EntityUtils.createThing(
                    serviceAdmin,
                    "newThing",
                    "A new Thing for Testing",
                    ehAdmin.getCache(sMdl.etThing));
            obsFuture.complete(thing);
            return null;
        };
        final TestSubscription testSubscription = new TestSubscription(mqttHelperWrite, "v1.1/Things")
                .addExpectedEntity(obsFuture)
                .createReceivedListener(sMdl.etThing);
        MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription);
        mqttHelperAdmin.executeRequest(mqttAction);
    }

    @Test
    void test32SubscribeThingRead() {
        LOGGER.info("  test32SubscribeThingRead");
        final CompletableFuture<Entity> obsFuture = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity thing = EntityUtils.createThing(
                    serviceAdmin,
                    "newThing",
                    "A new Thing for Testing",
                    ehAdmin.getCache(sMdl.etThing));
            obsFuture.complete(thing);
            return null;
        };
        final TestSubscription testSubscription = new TestSubscription(mqttHelperRead, "v1.1/Things")
                .addExpectedEntity(obsFuture)
                .createReceivedListener(sMdl.etThing);
        MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription);
        mqttHelperAdmin.executeRequest(mqttAction);
    }

    @Test
    void test33SubscribeThingAnon() {
        LOGGER.info("  test33SubscribeThingAnon");
        final CompletableFuture<Entity> obsFuture = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity thing = EntityUtils.createThing(
                    serviceAdmin,
                    "newThing",
                    "A new Thing for Testing",
                    ehAdmin.getCache(sMdl.etThing));
            obsFuture.complete(thing);
            return null;
        };
        final TestSubscription testSubscription = new TestSubscription(mqttHelperAnon, "v1.1/Things")
                .createReceivedListener(sMdl.etThing);
        if (anonymousReadAllowed) {
            testSubscription.addExpectedEntity(obsFuture);
        } else {
            testSubscription.addExpectedError("Failed to subscribe to v1.1/Things");
        }
        MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription);
        mqttHelperAdmin.executeRequest(mqttAction);
    }

}
