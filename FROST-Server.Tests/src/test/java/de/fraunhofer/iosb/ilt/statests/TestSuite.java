/*
 * Copyright (C) 2019 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.statests;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.fraunhofer.iosb.ilt.frostserver.FrostMqttServer;
import de.fraunhofer.iosb.ilt.frostserver.http.common.DatabaseStatus;
import de.fraunhofer.iosb.ilt.frostserver.http.common.ServletMain;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.BusSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PLUGINS;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1CoreOnlyTests;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1Tests;
import de.fraunhofer.iosb.ilt.statests.c02cud.AdditionalTests;
import de.fraunhofer.iosb.ilt.statests.c02cud.Capability2Tests;
import de.fraunhofer.iosb.ilt.statests.c02cud.DeleteFilterTests;
import de.fraunhofer.iosb.ilt.statests.c02cud.JsonPatchTests;
import de.fraunhofer.iosb.ilt.statests.c02cud.ResultTypesTests;
import de.fraunhofer.iosb.ilt.statests.c03filtering.Capability3Tests;
import de.fraunhofer.iosb.ilt.statests.c03filtering.DateTimeTests;
import de.fraunhofer.iosb.ilt.statests.c03filtering.FilterTests;
import de.fraunhofer.iosb.ilt.statests.c03filtering.GeoTests;
import de.fraunhofer.iosb.ilt.statests.c03filtering.JsonPropertiesTests;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchTests;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MdDateTimeTests;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamTests;
import de.fraunhofer.iosb.ilt.statests.c06dataarrays.DataArrayTests;
import de.fraunhofer.iosb.ilt.statests.c07mqttcreate.Capability7Tests;
import de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe.Capability8Tests;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthAnonReadTests;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthCryptPwTests;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthTests;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakAnonReadTests;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakTests;
import de.fraunhofer.iosb.ilt.statests.f02customlinks.CustomLinksTests;
import de.fraunhofer.iosb.ilt.statests.f03metadata.MetadataTests;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 *
 * @author scf
 */
@SelectClasses({
    Capability1CoreOnlyTests.Implementation10.class,
    Capability1CoreOnlyTests.Implementation11.class,
    Capability1Tests.Implementation10.class,
    Capability1Tests.Implementation11.class,
    Capability2Tests.Implementation10.class,
    Capability2Tests.Implementation11.class,
    AdditionalTests.Implementation10.class,
    AdditionalTests.Implementation11.class,
    DeleteFilterTests.Implementation10.class,
    DeleteFilterTests.Implementation11.class,
    JsonPatchTests.Implementation10.class,
    JsonPatchTests.Implementation11.class,
    ResultTypesTests.Implementation10.class,
    ResultTypesTests.Implementation11.class,
    Capability3Tests.Implementation10.class,
    Capability3Tests.Implementation11.class,
    DateTimeTests.Implementation10.class,
    DateTimeTests.Implementation11.class,
    FilterTests.Implementation10.class,
    FilterTests.Implementation11.class,
    GeoTests.Implementation10.class,
    GeoTests.Implementation11.class,
    JsonPropertiesTests.Implementation10.class,
    JsonPropertiesTests.Implementation11.class,
    BatchTests.Implementation10.class,
    BatchTests.Implementation11.class,
    MultiDatastreamTests.Implementation10.class,
    MultiDatastreamTests.Implementation11.class,
    MdDateTimeTests.Implementation10.class,
    MdDateTimeTests.Implementation11.class,
    DataArrayTests.Implementation10.class,
    DataArrayTests.Implementation11.class,
    Capability7Tests.Implementation10.class,
    Capability7Tests.Implementation11.class,
    Capability8Tests.Implementation10.class,
    Capability8Tests.Implementation11.class,
    BasicAuthTests.Implementation10.class,
    BasicAuthTests.Implementation11.class,
    BasicAuthAnonReadTests.Implementation10.class,
    BasicAuthAnonReadTests.Implementation11.class,
    BasicAuthCryptPwTests.Implementation10.class,
    BasicAuthCryptPwTests.Implementation11.class,
    KeyCloakTests.Implementation10.class,
    KeyCloakTests.Implementation11.class,
    KeyCloakAnonReadTests.Implementation10.class,
    KeyCloakAnonReadTests.Implementation11.class,
    CustomLinksTests.Implementation10.class,
    CustomLinksTests.Implementation11.class,
    MetadataTests.Implementation10.class,
    MetadataTests.Implementation11.class,
    TestSuite.SuiteFinaliser.class
})
@Suite
@Testcontainers
public class TestSuite {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class);
    public static final String KEY_HAS_MULTI_DATASTREAM = "hasMultiDatastream";
    public static final String KEY_HAS_ACTUATION = "hasActuation";
    public static final String KEY_DB_NAME = "dbName";

    public static final String VAL_PERSISTENCE_MANAGER = "de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager";
    public static final String VAL_ID_TYPE_DEFAULT = Constants.VALUE_ID_TYPE_UUID;
    public static final String VAL_ID_TYPE_OBSERVATIONS = Constants.VALUE_ID_TYPE_LONG;
    public static final String VAL_ID_TYPE_HIST_LOCATIONS = Constants.VALUE_ID_TYPE_LONG;
    public static final String VAL_PG_DB = "sensorthings";
    public static final String VAL_PG_USER = "sensorthings";
    public static final String VAL_PG_PASS = "ChangeMe";

    private static final String DATABASE_CONNECT_URL_BASE = "jdbc:tc:postgis:14-3.2:///";
    private static final String DATABASE_CONNECT_URL_POSTFIX = "?TC_DAEMON=true&TC_INITSCRIPT=file:src/test/resources/pgInit.sql";

    private static TestSuite instance;

    private final Map<Properties, Server> httpServers = new HashMap<>();
    private final Map<Properties, FrostMqttServer> mqttServers = new HashMap<>();
    private final Map<Properties, ServerSettings> serverSettings = new HashMap<>();

    private final AtomicInteger nextBusId = new AtomicInteger(1);
    private final AtomicInteger nextDbId = new AtomicInteger(1);

    @Container
    private final GenericContainer mqttBus = new GenericContainer<>("eclipse-mosquitto")
            .withExposedPorts(1883)
            .withClasspathResourceMapping("mosquitto.conf", "/mosquitto/config/mosquitto.conf", BindMode.READ_ONLY);

    @Container
    private final KeycloakContainer keycloak = new KeycloakContainer()
            .withRealmImportFile("keycloak/FROST-Test.json");

    static class SuiteFinaliser {

        @Test
        void finalTest() {
            LOGGER.info("Stopping Servers...");
            assertDoesNotThrow(() -> {
                getInstance().stopAllServers();
            });
        }
    }

    public static TestSuite getInstance() {
        // Create a new instance if none exists. This only happens when running
        // tests outside of the test suite.
        if (instance == null) {
            instance = new TestSuite();
            try {
                setUpClass();
            } catch (RuntimeException | IOException | InterruptedException ex) {
                LOGGER.error("Failed to initialise.", ex);
            }
        }
        return instance;
    }

    public TestSuite() {
        if (instance == null) {
            instance = this;
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }
    }

    @BeforeAll
    public static void setUpClass() throws IOException, InterruptedException {
        LOGGER.info("Starting Servers...");
    }

    @AfterAll
    public static void tearDownClass() {
        HTTPMethods.logStats();
        LOGGER.info("Stopping Servers...");
        getInstance().stopAllServers();
    }

    public ServerSettings getServerSettings(Properties parameters) throws IOException, InterruptedException {
        maybeStartServers(parameters);
        return serverSettings.get(parameters);
    }

    public Server getServer(Properties parameters) throws IOException, InterruptedException {
        maybeStartServers(parameters);
        return httpServers.get(parameters);
    }

    public KeycloakContainer getKeycloak() {
        if (!keycloak.isRunning()) {
            keycloak.start();
        }
        return keycloak;
    }

    private synchronized void maybeStartMessagebus() throws InterruptedException, UnsupportedOperationException, IOException {
        if (!mqttBus.isRunning()) {
            mqttBus.start();
        }
    }

    private synchronized void maybeStartServers(Properties parameters) throws IOException, InterruptedException {
        if (!serverSettings.containsKey(parameters)) {
            startServers(parameters);
        }
    }

    private synchronized void startServers(Properties parameters) throws IOException, InterruptedException {
        if (serverSettings.containsKey(parameters)) {
            return;
        }
        maybeStartMessagebus();
        parameters.computeIfAbsent(KEY_DB_NAME, (t) -> "db" + nextDbId.incrementAndGet());
        try {
            LOGGER.info("Testing if Mosquitto works...");
            MqttClient client = new MqttClient(
                    "tcp://127.0.0.1:" + mqttBus.getFirstMappedPort(),
                    MqttClient.generateClientId(),
                    new MemoryPersistence());
            client.connect();
            client.disconnect();
            LOGGER.info("Mosquitto works.");
        } catch (MqttException ex) {
            throw new RuntimeException("Failed to connect to bus!", ex);
        }

        startHttpServer(parameters);
        startMqttServer(parameters);
    }

    private void startHttpServer(Properties parameters) {
        // Set common properties shared by HTTP and MQTT
        parameters.put("bus." + MqttMessageBus.TAG_TOPIC_NAME, "FROST-BUS-" + nextBusId.getAndIncrement());

        LOGGER.info("HTTP Server starting...");
        ServerSettings serverSetting = new ServerSettings();
        serverSettings.put(parameters, serverSetting);

        Map<String, String> paramsMap = new HashMap<>();
        parameters.forEach((t, u) -> paramsMap.put(t.toString(), u.toString()));

        Server myServer = new Server(0);
        HandlerCollection contextHandlerCollection = new HandlerCollection(true);
        myServer.setHandler(contextHandlerCollection);
        try {
            myServer.start();
        } catch (Exception ex) {
            LOGGER.error("Exception starting server!");
            throw new IllegalStateException(ex);
        }

        Connector[] connectors = myServer.getConnectors();
        ServerConnector connecor = (ServerConnector) connectors[0];

        serverSetting.setServiceRootUrl("http://localhost:" + connecor.getLocalPort() + "");

        ServletContextHandler handler = new ServletContextHandler();
        handler.getServletContext().setExtendedListenerTypes(true);
        handler.setInitParameter(CoreSettings.TAG_LOG_SENSITIVE_DATA, Boolean.TRUE.toString());
        handler.setInitParameter(CoreSettings.TAG_SERVICE_ROOT_URL, serverSetting.getServiceRootUrl());
        handler.setInitParameter(CoreSettings.TAG_TEMP_PATH, System.getProperty("java.io.tmpdir"));

        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_DEFAULT, VAL_ID_TYPE_DEFAULT);
        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_OBSERVATION, VAL_ID_TYPE_OBSERVATIONS);
        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_HIST_LOCATION, VAL_ID_TYPE_HIST_LOCATIONS);

        handler.setInitParameter("persistence.persistenceManagerImplementationClass", VAL_PERSISTENCE_MANAGER);
        handler.setInitParameter("persistence.autoUpdateDatabase", "true");
        handler.setInitParameter("persistence.db.driver", "org.postgresql.Driver");
        handler.setInitParameter("persistence.db.url", createDbUrl(parameters.getProperty(KEY_DB_NAME)));
        handler.setInitParameter("persistence.db.username", VAL_PG_USER);
        handler.setInitParameter("persistence.db.password", VAL_PG_PASS);

        handler.setInitParameter("bus." + BusSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus");
        handler.setInitParameter("bus." + MqttMessageBus.TAG_MQTT_BROKER, "tcp://" + mqttBus.getHost() + ":" + mqttBus.getFirstMappedPort());
        handler.setInitParameter("bus.sendWorkerPoolSize", Integer.toString(20));
        handler.setInitParameter("bus.sendQueueSize", Integer.toString(10000));
        handler.setInitParameter("bus.maxInFlight", Integer.toString(10000));

        handler.getInitParams().putAll(paramsMap);

        handler.addEventListener(new HttpContextListener());
        handler.addServlet(DatabaseStatus.class, "/DatabaseStatus");
        handler.addServlet(ServletMain.class, "/*");
        contextHandlerCollection.addHandler(handler);
        try {
            handler.start();
        } catch (Exception ex) {
            LOGGER.error("Exception starting server!");
            throw new IllegalStateException(ex);
        }

        LOGGER.info("Server started.");
        httpServers.put(parameters, myServer);

        findImplementedVersions(serverSetting);
        checkServiceRootUri(serverSetting);
        serverSetting.initExtensionsAndTypes();
    }

    public static String createDbUrl(String dbName) {
        return DATABASE_CONNECT_URL_BASE + dbName + DATABASE_CONNECT_URL_POSTFIX;
    }

    private void startMqttServer(Properties parameters) throws IOException {
        LOGGER.info("MQTT Server starting...");
        ServerSettings serverSetting = serverSettings.get(parameters);

        int mqttPort = findRandomPort();
        int mqttWsPort = findRandomPort();
        LOGGER.info("Generated random ports {}, {}", mqttPort, mqttWsPort);
        Properties properties = new Properties();
        properties.put(CoreSettings.TAG_SERVICE_ROOT_URL, serverSetting.getServiceRootUrl());
        Path tempDir = Files.createTempDirectory("FROST-Tests");
        properties.put(CoreSettings.TAG_TEMP_PATH, tempDir.toString());

        properties.put("mqtt." + MqttSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette.MoquetteMqttServer");
        properties.put("mqtt." + MqttSettings.TAG_ENABLED, "true");
        properties.put("mqtt." + MqttSettings.TAG_PORT, "" + mqttPort);
        properties.put("mqtt." + MqttSettings.TAG_QOS, "2");
        properties.put("mqtt.SubscribeMessageQueueSize", "100");
        properties.put("mqtt.SubscribeThreadPoolSize", "20");
        properties.put("mqtt.CreateMessageQueueSize", "100");
        properties.put("mqtt.CreateThreadPoolSize", "10");
        properties.put("mqtt.Host", "0.0.0.0");
        properties.put("mqtt.internalHost", "localhost");
        properties.put("mqtt.WebsocketPort", "" + mqttWsPort);

        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_DEFAULT, VAL_ID_TYPE_DEFAULT);
        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_OBSERVATION, VAL_ID_TYPE_OBSERVATIONS);
        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_HIST_LOCATION, VAL_ID_TYPE_HIST_LOCATIONS);

        properties.put("persistence.persistenceManagerImplementationClass", VAL_PERSISTENCE_MANAGER);
        properties.put("persistence.db.driver", "org.postgresql.Driver");
        properties.put("persistence.db.url", createDbUrl(parameters.getProperty(KEY_DB_NAME)));
        properties.put("persistence.db.username", VAL_PG_USER);
        properties.put("persistence.db.password", VAL_PG_PASS);
        properties.put("bus." + BusSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus");
        properties.put("bus." + MqttMessageBus.TAG_MQTT_BROKER, "tcp://" + mqttBus.getHost() + ":" + mqttBus.getFirstMappedPort());
        if (parameters != null) {
            properties.putAll(parameters);
        }

        CoreSettings coreSettings = new CoreSettings(properties);
        FrostMqttServer server = new FrostMqttServer(coreSettings);
        server.start();
        serverSetting.setMqttUrl("tcp://localhost:" + mqttPort);
        LOGGER.info("MQTT Server started on port {}", mqttPort);
        mqttServers.put(parameters, server);
    }

    public void stopServer(Properties parameters) {
        if (!httpServers.containsKey(parameters)) {
            return;
        }
        Server httpServer = httpServers.get(parameters);
        if (httpServer != null) {
            try {
                httpServer.stop();
            } catch (Exception ex) {
                LOGGER.error("Exception stopping server!");
                throw new IllegalStateException(ex);
            }
        }
        httpServers.remove(parameters);
        FrostMqttServer mqttServer = mqttServers.get(parameters);
        if (mqttServer != null) {
            try {
                mqttServer.stop();
            } catch (Exception ex) {
                LOGGER.error("Exception stopping server!");
                throw new IllegalStateException(ex);
            }
        }
        mqttServers.remove(parameters);
        serverSettings.remove(parameters);
    }

    public synchronized void stopAllServers() {
        List<Thread> shutdownThreads = new ArrayList<>();
        for (Properties props : httpServers.keySet().toArray(new Properties[httpServers.size()])) {
            Thread t = new Thread(() -> {
                stopServer(props);
            });
            shutdownThreads.add(t);
            t.start();
        }
        for (Thread t : shutdownThreads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                LOGGER.error("Interrupted!", ex);
            }
        }
        keycloak.stop();
        mqttBus.stop();
    }

    public int findRandomPort() {
        int port;
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
            s = null;
        } catch (IOException ex) {
            LOGGER.error("Failed to find a port. Using default 11883", ex);
            return 11883;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException ex) {
                    LOGGER.error("Failed to close port.", ex);
                }
            }
        }
        return port;
    }

    public void findImplementedVersions(ServerSettings serverSettings) {
        for (ServerVersion version : ServerVersion.values()) {
            String rootUri = serverSettings.getServiceUrl(version);
            HTTPMethods.HttpResponse response = HTTPMethods.doGet(rootUri);
            String implemented;
            if (response.code == 200) {
                serverSettings.addImplementedVersion(version);
                implemented = "is";
            } else {
                implemented = "not";
            }
            LOGGER.info("Version {} {} implemented.", version.urlPart, implemented);
        }
    }

    public void checkServiceRootUri(ServerSettings serverSettings) {
        for (ServerVersion version : serverSettings.getImplementedVersions()) {
            checkServiceRootUri(serverSettings, version);
        }
    }

    /**
     * Checking the service root URL to be compliant with SensorThings API
     *
     * @param serverSettings the settings for the httpServer.
     * @param version the version to check the serviceRootUri for. This must be
     * a version actually implemented by the server.
     */
    public void checkServiceRootUri(ServerSettings serverSettings, ServerVersion version) {
        String rootUri = serverSettings.getServiceUrl(version);
        HttpResponse response = HTTPMethods.doGet(rootUri);

        if (response == null || response.code != 200) {
            fail("Cannot fetch service root url from " + rootUri + ".");
            return;
        }

        JSONObject jsonResponse;
        JSONArray entities;
        try {
            jsonResponse = new JSONObject(response.response);
            entities = jsonResponse.getJSONArray("value");
        } catch (JSONException e) {
            LOGGER.error("The service response for the root URI '" + rootUri + "' is not JSON.", e);
            fail("The service response for the root URI '" + rootUri + "' is not JSON.");
            return;
        }
        boolean hasActuation = false;
        boolean hasMultiDatastream = false;
        for (int i = 0; i < entities.length(); i++) {
            JSONObject entity;
            String name;
            try {
                entity = entities.getJSONObject(i);
                if (!entity.has("name")) {
                    fail("The name component of Service root URI response is not available.");
                    return;
                }
                name = entity.getString("name");
            } catch (JSONException e) {
                LOGGER.error("The service response for the root URI '" + rootUri + "' is not JSON.", e);
                fail("The service response for the root URI '" + rootUri + "' is not JSON.");
                return;
            }
            switch (name) {
                case "Actuators":
                case "Tasks":
                case "TaskingCapabilities":
                    hasActuation = true;
                    break;

                case "MultiDatastreams":
                    hasMultiDatastream = true;
                    break;

                default:
                // Nothing special...
            }
        }
        if (version == ServerVersion.v_1_0) {
            if (hasMultiDatastream) {
                serverSettings.addImplementedRequirement(version, ServerSettings.MULTIDATA_REQ);
            }
            if (hasActuation) {
                serverSettings.addImplementedRequirement(version, ServerSettings.TASKING_REQ);
            }
        }
        if (version == ServerVersion.v_1_1) {
            JSONObject serverSettingsObject = jsonResponse.getJSONObject("serverSettings");
            JSONArray conformanceArray = serverSettingsObject.getJSONArray("conformance");
            for (Object reqItem : conformanceArray.toList()) {
                Set<Requirement> allMatching = Requirement.getAllMatching(reqItem.toString());
                if (allMatching.isEmpty()) {
                    LOGGER.info("Server implements unknown requirement: {}", reqItem);
                }
                serverSettings.addImplementedRequirements(version, allMatching);
            }
            if (hasActuation && !serverSettings.implementsRequirement(version, ServerSettings.TASKING_REQ)) {
                fail("Server lists Actuation entities, but does not claim reqirement " + ServerSettings.TASKING_REQ.getName());
            }
            if (hasMultiDatastream && !serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ)) {
                fail("Server lists the MultiDatastream entity, but does not claim reqirement " + ServerSettings.MULTIDATA_REQ.getName());
            }
        }
    }

}
