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
package de.fraunhofer.iosb.ilt.statests;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PLUGINS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.fraunhofer.iosb.ilt.frostserver.FrostMqttServer;
import de.fraunhofer.iosb.ilt.frostserver.http.common.DatabaseStatus;
import de.fraunhofer.iosb.ilt.frostserver.http.common.ServletMain;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.BusSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1CoreOnlyTests10;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1CoreOnlyTests11;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1Tests10;
import de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1Tests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.AdditionalTests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.AdditionalTests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.Capability2Tests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.Capability2Tests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.DeleteFilterTests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.DeleteFilterTests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.JsonPatchTests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.JsonPatchTests11;
import de.fraunhofer.iosb.ilt.statests.c02cud.ResultTypesTests10;
import de.fraunhofer.iosb.ilt.statests.c02cud.ResultTypesTests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.Capability3Tests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.Capability3Tests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.DateTimeTests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.DateTimeTests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.FilterTests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.FilterTests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.GeoTests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.GeoTests11;
import de.fraunhofer.iosb.ilt.statests.c03filtering.JsonPropertiesTests10;
import de.fraunhofer.iosb.ilt.statests.c03filtering.JsonPropertiesTests11;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchTests10;
import de.fraunhofer.iosb.ilt.statests.c04batch.BatchTests11;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MdDateTimeTests10;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MdDateTimeTests11;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamObsPropTests10;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamObsPropTests11;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamTests10;
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamTests11;
import de.fraunhofer.iosb.ilt.statests.c06dataarrays.DataArrayTests10;
import de.fraunhofer.iosb.ilt.statests.c06dataarrays.DataArrayTests11;
import de.fraunhofer.iosb.ilt.statests.c07mqttcreate.Capability7Tests10;
import de.fraunhofer.iosb.ilt.statests.c07mqttcreate.Capability7Tests11;
import de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe.Capability8Tests10;
import de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe.Capability8Tests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthAnonReadTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthAnonReadTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthCryptPwTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthCryptPwTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.FineGrainedAuthTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakAnonReadTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakAnonReadTests11;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakTests10;
import de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakTests11;
import de.fraunhofer.iosb.ilt.statests.f02customlinks.CustomLinksTests10;
import de.fraunhofer.iosb.ilt.statests.f02customlinks.CustomLinksTests11;
import de.fraunhofer.iosb.ilt.statests.f03metadata.MetadataTests10;
import de.fraunhofer.iosb.ilt.statests.f03metadata.MetadataTests11;
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
import java.util.Objects;
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
    Capability1CoreOnlyTests10.class,
    Capability1CoreOnlyTests11.class,
    Capability1Tests10.class,
    Capability1Tests11.class,
    Capability2Tests10.class,
    Capability2Tests11.class,
    AdditionalTests10.class,
    AdditionalTests11.class,
    DeleteFilterTests10.class,
    DeleteFilterTests11.class,
    JsonPatchTests10.class,
    JsonPatchTests11.class,
    ResultTypesTests10.class,
    ResultTypesTests11.class,
    Capability3Tests10.class,
    Capability3Tests11.class,
    DateTimeTests10.class,
    DateTimeTests11.class,
    FilterTests10.class,
    FilterTests11.class,
    GeoTests10.class,
    GeoTests11.class,
    JsonPropertiesTests10.class,
    JsonPropertiesTests11.class,
    BatchTests10.class,
    BatchTests11.class,
    MultiDatastreamTests10.class,
    MultiDatastreamTests11.class,
    MultiDatastreamObsPropTests10.class,
    MultiDatastreamObsPropTests11.class,
    MdDateTimeTests10.class,
    MdDateTimeTests11.class,
    DataArrayTests10.class,
    DataArrayTests11.class,
    Capability7Tests10.class,
    Capability7Tests11.class,
    Capability8Tests10.class,
    Capability8Tests11.class,
    BasicAuthTests10.class,
    BasicAuthTests11.class,
    BasicAuthAnonReadTests10.class,
    BasicAuthAnonReadTests11.class,
    BasicAuthCryptPwTests10.class,
    BasicAuthCryptPwTests11.class,
    FineGrainedAuthTests11.class,
    KeyCloakTests10.class,
    KeyCloakTests11.class,
    KeyCloakAnonReadTests10.class,
    KeyCloakAnonReadTests11.class,
    CustomLinksTests10.class,
    CustomLinksTests11.class,
    MetadataTests10.class,
    MetadataTests11.class,
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
    public static final String VAL_ID_TYPE_OBSERVEDPROPERTIES = Constants.VALUE_ID_TYPE_LONG;
    public static final String VAL_ID_TYPE_HIST_LOCATIONS = Constants.VALUE_ID_TYPE_LONG;
    public static final String VAL_PG_DB = "sensorthings";
    public static final String VAL_PG_USER = "sensorthings";
    public static final String VAL_PG_PASS = "ChangeMe";

    private static final String DATABASE_CONNECT_URL_BASE = "jdbc:tc:postgis:14-3.2:///";
    private static final String DATABASE_CONNECT_URL_POSTFIX = "?TC_DAEMON=true&TC_INITSCRIPT=file:src/test/resources/pgInit.sql";

    private static TestSuite instance;

    private final Map<Integer, Server> httpServers = new HashMap<>();
    private final Map<Integer, FrostMqttServer> mqttServers = new HashMap<>();
    private final Map<Integer, ServerSettings> serverSettings = new HashMap<>();

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

    public ServerSettings getServerSettings(Map<String, String> parameters) throws IOException, InterruptedException {
        int key = maybeStartServers(parameters);
        return serverSettings.get(key);
    }

    public Server getServer(Map<String, String> parameters) throws IOException, InterruptedException {
        int key = maybeStartServers(parameters);
        return httpServers.get(key);
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

    private synchronized int maybeStartServers(Map<String, String> parameters) throws IOException, InterruptedException {
        int key = keyFromProperties(parameters);
        LOGGER.warn("Checking for parameters key {}", key);
        if (!serverSettings.containsKey(key)) {
            startServers(key, new HashMap<>(parameters));
        }
        return key;
    }

    private int keyFromProperties(Map<String, String> props) {
        return Objects.hashCode(props);
    }

    private synchronized void startServers(int key, Map<String, String> parameters) throws IOException, InterruptedException {
        if (serverSettings.containsKey(key)) {
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

        startHttpServer(key, parameters);
        startMqttServer(key, parameters);
    }

    private void startHttpServer(int key, Map<String, String> parameters) {
        // Set common properties shared by HTTP and MQTT
        parameters.put("bus." + MqttMessageBus.TAG_TOPIC_NAME, "FROST-BUS-" + nextBusId.getAndIncrement());

        LOGGER.info("HTTP Server starting...");
        ServerSettings serverSetting = new ServerSettings();
        serverSettings.put(key, serverSetting);

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
        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_OBS_PROPERTY, VAL_ID_TYPE_OBSERVEDPROPERTIES);
        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_HIST_LOCATION, VAL_ID_TYPE_HIST_LOCATIONS);

        handler.setInitParameter("persistence.persistenceManagerImplementationClass", VAL_PERSISTENCE_MANAGER);
        handler.setInitParameter("persistence.autoUpdateDatabase", "true");
        handler.setInitParameter("persistence.db.driver", "org.postgresql.Driver");
        handler.setInitParameter("persistence.db.url", createDbUrl(parameters.get(KEY_DB_NAME)));
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
        httpServers.put(key, myServer);

        findImplementedVersions(serverSetting);
        checkServiceRootUri(serverSetting);
        serverSetting.initExtensionsAndTypes();
    }

    public static String createDbUrl(String dbName) {
        return DATABASE_CONNECT_URL_BASE + dbName + DATABASE_CONNECT_URL_POSTFIX;
    }

    private void startMqttServer(int key, Map<String, String> parameters) throws IOException {
        LOGGER.info("MQTT Server starting...");
        ServerSettings serverSetting = serverSettings.get(key);

        int mqttPort = findRandomPort();
        int mqttWsPort = findRandomPort();
        LOGGER.info("Generated random ports {}, {}", mqttPort, mqttWsPort);
        Properties properties = new Properties();
        properties.put(CoreSettings.TAG_LOG_SENSITIVE_DATA, Boolean.TRUE.toString());
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
        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_OBS_PROPERTY, VAL_ID_TYPE_OBSERVEDPROPERTIES);
        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_HIST_LOCATION, VAL_ID_TYPE_HIST_LOCATIONS);

        properties.put("persistence.persistenceManagerImplementationClass", VAL_PERSISTENCE_MANAGER);
        properties.put("persistence.db.driver", "org.postgresql.Driver");
        properties.put("persistence.db.url", createDbUrl(parameters.get(KEY_DB_NAME)));
        properties.put("persistence.db.username", VAL_PG_USER);
        properties.put("persistence.db.password", VAL_PG_PASS);
        properties.put("bus." + BusSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus");
        properties.put("bus." + MqttMessageBus.TAG_MQTT_BROKER, "tcp://" + mqttBus.getHost() + ":" + mqttBus.getFirstMappedPort());
        properties.putAll(parameters);

        CoreSettings coreSettings = new CoreSettings(properties);
        FrostMqttServer server = new FrostMqttServer(coreSettings);
        server.start();
        serverSetting.setMqttUrl("tcp://localhost:" + mqttPort);
        LOGGER.info("MQTT Server started on port {}", mqttPort);
        mqttServers.put(key, server);
    }

    public void stopServer(int key) {
        if (!httpServers.containsKey(key)) {
            return;
        }
        Server httpServer = httpServers.get(key);
        if (httpServer != null) {
            try {
                httpServer.stop();
            } catch (Exception ex) {
                LOGGER.error("Exception stopping server!");
                throw new IllegalStateException(ex);
            }
        }
        httpServers.remove(key);
        FrostMqttServer mqttServer = mqttServers.get(key);
        if (mqttServer != null) {
            try {
                mqttServer.stop();
            } catch (Exception ex) {
                LOGGER.error("Exception stopping server!");
                throw new IllegalStateException(ex);
            }
        }
        mqttServers.remove(key);
        serverSettings.remove(key);
    }

    public synchronized void stopAllServers() {
        List<Thread> shutdownThreads = new ArrayList<>();
        // we copy the keys since the set is changed during shutdown.
        for (Integer key : httpServers.keySet().toArray(new Integer[httpServers.size()])) {
            Thread t = new Thread(() -> {
                stopServer(key);
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
                    LOGGER.trace("Tasking entity: {}", name);
                    hasActuation = true;
                    break;

                case "MultiDatastreams":
                    LOGGER.trace("MultiDatastreams entity: {}", name);
                    hasMultiDatastream = true;
                    break;

                default:
                    LOGGER.trace("Normal Entity: {}", name);
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
