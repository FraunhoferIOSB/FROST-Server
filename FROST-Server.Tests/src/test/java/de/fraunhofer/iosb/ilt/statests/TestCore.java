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
package de.fraunhofer.iosb.ilt.statests;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_DRIVER;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_PASSWRD;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_URL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_USERNAME;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_AUTH;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PERSISTENCE;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PLUGINS;
import static de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings.TAG_AUTO_UPDATE_DATABASE;
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
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
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
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

@Testcontainers
public class TestCore {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCore.class);

    public static final String KEY_HAS_MULTI_DATASTREAM = "hasMultiDatastream";
    public static final String KEY_HAS_ACTUATION = "hasActuation";
    public static final String KEY_DB_NAME = "dbName";

    public static final String VAL_PERSISTENCE_MANAGER = "de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager";
    public static final String VAL_ID_TYPE_DEFAULT = Constants.VALUE_ID_TYPE_LONG;
    public static final String VAL_ID_TYPE_LOCATIONS = Constants.VALUE_ID_TYPE_UUID;
    public static final String VAL_ID_TYPE_OBSERVATIONS = Constants.VALUE_ID_TYPE_LONG;
    public static final String VAL_ID_TYPE_OBSERVEDPROPERTIES = Constants.VALUE_ID_TYPE_LONG;
    public static final String VAL_ID_TYPE_HIST_LOCATIONS = Constants.VALUE_ID_TYPE_LONG;
    public static final String VAL_PG_DB = "sensorthings";
    public static final String VAL_PG_USER = "sensorthings";
    public static final String VAL_PG_PASS = "ChangeMe";

    private static final String DB_PG_CONNECT_URL_BASE = "jdbc:tc:postgis:14-3.2-alpine:///";
    private static final String DB_PG_CONNECT_URL_POSTFIX = "?TC_DAEMON=true&TC_INITSCRIPT=file:test-classes/pgInit.sql";
    private static final String DB_MARIADB_CONNECT_URL_BASE = "jdbc:tc:mariadb:11.5.2:///";
    private static final String DB_MARIADB_CONNECT_URL_POSTFIX = "?TC_DAEMON=true";

    private static TestCore instance;

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
    private final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak")
            .withRealmImportFile("keycloak/FROST-Test.json");

    public static TestCore getInstance() {
        // Create a new instance if none exists. This only happens when running
        // tests outside of the test suite.
        if (instance == null) {
            instance = new TestCore();
            try {
                LOGGER.info("Starting Servers...");
            } catch (RuntimeException ex) {
                LOGGER.error("Failed to initialise.", ex);
            }
        }
        return instance;
    }

    public TestCore() {
        if (instance == null) {
            instance = this;
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }
    }

    public ServerSettings getServerSettings(Map<String, String> parameters) throws IOException {
        int key = maybeStartServers(parameters);
        return serverSettings.get(key);
    }

    public Server getServer(Map<String, String> parameters) throws IOException {
        int key = maybeStartServers(parameters);
        return httpServers.get(key);
    }

    public KeycloakContainer getKeycloak() {
        if (!keycloak.isRunning()) {
            keycloak.start();
        }
        return keycloak;
    }

    private synchronized void maybeStartMessagebus() {
        if (!mqttBus.isRunning()) {
            mqttBus.start();
        }
    }

    private synchronized int maybeStartServers(Map<String, String> parameters) throws IOException {
        int key = keyFromProperties(parameters);
        LOGGER.debug("Checking for parameters key {}", key);
        if (!serverSettings.containsKey(key)) {
            startServers(key, new HashMap<>(parameters));
        }
        return key;
    }

    private int keyFromProperties(Map<String, String> props) {
        return Objects.hashCode(props);
    }

    private synchronized void startServers(int key, Map<String, String> parameters) throws IOException {
        if (serverSettings.containsKey(key)) {
            return;
        }
        maybeStartMessagebus();
        parameters.computeIfAbsent(KEY_DB_NAME, t -> "db" + nextDbId.incrementAndGet());
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
        paramsMap.putAll(parameters);

        Server myServer = new Server(0);
        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection(true);
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
        handler.setInitParameter(CoreSettings.TAG_LOG_SENSITIVE_DATA, Boolean.TRUE.toString());
        handler.setInitParameter(CoreSettings.TAG_SERVICE_ROOT_URL, serverSetting.getServiceRootUrl());
        handler.setInitParameter(CoreSettings.TAG_TEMP_PATH, System.getProperty("java.io.tmpdir"));

        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_DEFAULT, VAL_ID_TYPE_DEFAULT);
        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_LOCATION, VAL_ID_TYPE_LOCATIONS);
        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_OBSERVATION, VAL_ID_TYPE_OBSERVATIONS);
        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_OBS_PROPERTY, VAL_ID_TYPE_OBSERVEDPROPERTIES);
        handler.setInitParameter(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_HIST_LOCATION, VAL_ID_TYPE_HIST_LOCATIONS);

        String dbDriver = paramsMap.computeIfAbsent(PREFIX_PERSISTENCE + TAG_DB_DRIVER, t -> "org.postgresql.Driver");
        handler.setInitParameter(PREFIX_PERSISTENCE + PersistenceSettings.TAG_IMPLEMENTATION_CLASS, VAL_PERSISTENCE_MANAGER);
        handler.setInitParameter(PREFIX_PERSISTENCE + TAG_AUTO_UPDATE_DATABASE, "true");
        handler.setInitParameter(PREFIX_PERSISTENCE + TAG_DB_DRIVER, dbDriver);
        handler.setInitParameter(PREFIX_PERSISTENCE + TAG_DB_URL, createDbUrl(dbDriver, parameters.get(KEY_DB_NAME)));
        handler.setInitParameter(PREFIX_PERSISTENCE + TAG_DB_USERNAME, VAL_PG_USER);
        handler.setInitParameter(PREFIX_PERSISTENCE + TAG_DB_PASSWRD, VAL_PG_PASS);

        handler.setInitParameter("bus." + BusSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus");
        handler.setInitParameter("bus." + MqttMessageBus.TAG_MQTT_BROKER, "tcp://" + mqttBus.getHost() + ":" + mqttBus.getFirstMappedPort());
        handler.setInitParameter("bus.sendWorkerPoolSize", Integer.toString(20));
        handler.setInitParameter("bus.sendQueueSize", Integer.toString(10000));
        handler.setInitParameter("bus.maxInFlight", Integer.toString(10000));

        handler.getInitParams().putAll(paramsMap);

        handler.addEventListener(new HttpContextListener());
        handler.addServlet(DatabaseStatus.class, "/DatabaseStatus");
        handler.addServlet(ServletMain.class, "/");
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

    public static String createDbUrl(String type, String dbName) {
        if (type.toLowerCase().contains("mariadb")) {
            return DB_MARIADB_CONNECT_URL_BASE + dbName + DB_MARIADB_CONNECT_URL_POSTFIX;
        } else {
            return DB_PG_CONNECT_URL_BASE + dbName + DB_PG_CONNECT_URL_POSTFIX;
        }
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
        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_LOCATION, VAL_ID_TYPE_LOCATIONS);
        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_OBSERVATION, VAL_ID_TYPE_OBSERVATIONS);
        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_OBS_PROPERTY, VAL_ID_TYPE_OBSERVEDPROPERTIES);
        properties.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_HIST_LOCATION, VAL_ID_TYPE_HIST_LOCATIONS);

        String dbDriver = parameters.getOrDefault(PREFIX_PERSISTENCE + TAG_DB_DRIVER, "org.postgresql.Driver");
        properties.put(PREFIX_PERSISTENCE + PersistenceSettings.TAG_IMPLEMENTATION_CLASS, VAL_PERSISTENCE_MANAGER);
        properties.put(PREFIX_PERSISTENCE + TAG_DB_DRIVER, dbDriver);
        properties.put(PREFIX_PERSISTENCE + TAG_DB_URL, createDbUrl(dbDriver, parameters.get(KEY_DB_NAME)));
        properties.put(PREFIX_PERSISTENCE + TAG_DB_USERNAME, VAL_PG_USER);
        properties.put(PREFIX_PERSISTENCE + TAG_DB_PASSWRD, VAL_PG_PASS);
        properties.put("bus." + BusSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus");
        properties.put("bus." + MqttMessageBus.TAG_MQTT_BROKER, "tcp://" + mqttBus.getHost() + ":" + mqttBus.getFirstMappedPort());
        properties.putAll(parameters);
        properties.put(PREFIX_PERSISTENCE + TAG_AUTO_UPDATE_DATABASE, "false");
        properties.put(PREFIX_AUTH + TAG_AUTO_UPDATE_DATABASE, "false");

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
    }

    public int findRandomPort() {
        int port;
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        } catch (IOException ex) {
            LOGGER.error("Failed to find a port. Using default 11883", ex);
            return 11883;
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
            LOGGER.debug("Version {} {} implemented.", version.urlPart, implemented);
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

        JsonNode jsonResponse;
        JsonNode entities;
        try {
            jsonResponse = Utils.MAPPER.readTree(response.response);
            entities = jsonResponse.get("value");
        } catch (JacksonException | NullPointerException e) {
            LOGGER.error("The service response for the root URI '" + rootUri + "' is not JSON.", e);
            fail("The service response for the root URI '" + rootUri + "' is not JSON.");
            return;
        }
        boolean hasActuation = false;
        boolean hasMultiDatastream = false;
        for (int i = 0; i < entities.size(); i++) {
            JsonNode entity;
            String name;
            try {
                entity = entities.get(i);
                if (!entity.has("name")) {
                    fail("The name component of Service root URI response is not available.");
                    return;
                }
                name = entity.get("name").stringValue();
            } catch (NullPointerException e) {
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
        if (version == ServerVersion.V_1_0) {
            if (hasMultiDatastream) {
                serverSettings.addImplementedRequirement(version, ServerSettings.MULTIDATA_REQ);
            }
            if (hasActuation) {
                serverSettings.addImplementedRequirement(version, ServerSettings.TASKING_REQ);
            }
        }
        if (version == ServerVersion.V_1_1) {
            JsonNode serverSettingsObject = jsonResponse.get("serverSettings");
            JsonNode conformanceArray = serverSettingsObject.get("conformance");
            for (JsonNode reqItem : conformanceArray) {
                Set<Requirement> allMatching = Requirement.getAllMatching(reqItem.stringValue());
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
