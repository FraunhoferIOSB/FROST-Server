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

import de.fraunhofer.iosb.ilt.frostserver.FrostMqttServer;
import de.fraunhofer.iosb.ilt.frostserver.http.common.DatabaseStatus;
import de.fraunhofer.iosb.ilt.frostserver.http.common.ServletV1P0;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus;
import de.fraunhofer.iosb.ilt.frostserver.settings.BusSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
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
import de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamTests;
import de.fraunhofer.iosb.ilt.statests.c06dataarrays.DataArrayTests;
import de.fraunhofer.iosb.ilt.statests.c07mqttcreate.Capability7Tests;
import de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe.Capability8Tests;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthAnonReadTests;
import de.fraunhofer.iosb.ilt.statests.f01auth.BasicAuthTests;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

/**
 *
 * @author scf
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    Capability1Tests.class,
    Capability2Tests.class,
    AdditionalTests.class,
    DeleteFilterTests.class,
    JsonPatchTests.class,
    ResultTypesTests.class,
    Capability3Tests.class,
    DateTimeTests.class,
    FilterTests.class,
    GeoTests.class,
    JsonPropertiesTests.class,
    MultiDatastreamTests.class,
    DataArrayTests.class,
    Capability7Tests.class,
    Capability8Tests.class,
    BasicAuthTests.class,
    BasicAuthAnonReadTests.class
})
public class TestSuite {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class);
    public static final String KEY_HAS_MULTI_DATASTREAM = "hasMultiDatastream";
    public static final String KEY_HAS_ACTUATION = "hasActuation";

    // de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerLong
    // de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerUuid
    // de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerString
    public static final String VAL_PERSISTENCE_MANAGER = "de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerLong";
    public static final String VAL_PG_DB = "sensorthings";
    public static final String VAL_PG_USER = "sensorthings";
    public static final String VAL_PG_PASS = "ChangeMe";

    private static TestSuite instance;

    private final Map<Properties, Server> httpServers = new HashMap<>();
    private final Map<Properties, FrostMqttServer> mqttServers = new HashMap<>();
    private final Map<Properties, ServerSettings> serverSettings = new HashMap<>();
    private String pgConnectUrl;
    @Rule
    public GenericContainer pgServer = new GenericContainer<>("postgis/postgis:11-2.5-alpine")
            .withEnv("POSTGRES_DB", VAL_PG_DB)
            .withEnv("POSTGRES_USER", VAL_PG_USER)
            .withEnv("POSTGRES_PASSWORD", VAL_PG_PASS)
            .withExposedPorts(5432);

    @Rule
    public GenericContainer mqttBus = new GenericContainer<>("eclipse-mosquitto").withExposedPorts(1883);

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
        }
    }

    @BeforeClass
    public static void setUpClass() throws IOException, InterruptedException {
        LOGGER.info("Starting Servers...");
        TestSuite myInstance = getInstance();
        myInstance.startServers(null);
    }

    @AfterClass
    public static void tearDownClass() {
        LOGGER.info("Stopping Servers...");
        getInstance().stopAllServers();
    }

    public String getPgConnectUrl() {
        return pgConnectUrl;
    }

    public ServerSettings getServerSettings(Properties parameters) throws IOException, InterruptedException {
        maybeStartServers(parameters);
        return serverSettings.get(parameters);
    }

    public Server getServer(Properties parameters) throws IOException, InterruptedException {
        maybeStartServers(parameters);
        return httpServers.get(parameters);
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
        if (!pgServer.isRunning()) {
            pgServer.start();
            mqttBus.start();

            Container.ExecResult execResult = pgServer.execInContainer("psql", "-U" + VAL_PG_USER, "-d" + VAL_PG_DB, "-c CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");
            LOGGER.info("Installing extension uuid-ossp: {} {}", execResult.getStdout(), execResult.getStderr());
            pgConnectUrl = "jdbc:postgresql://" + pgServer.getContainerIpAddress() + ":" + pgServer.getFirstMappedPort() + "/" + VAL_PG_DB;
        }

        startHttpServer(parameters);
        startMqttServer(parameters);
    }

    private void startHttpServer(Properties parameters) {
        ServerSettings serverSetting = new ServerSettings();
        serverSettings.put(parameters, serverSetting);

        Map<String, String> paramsMap = new HashMap<>();
        if (parameters != null) {
            parameters.forEach((t, u) -> paramsMap.put(t.toString(), u.toString()));
        }

        Server myServer = new Server(0);
        HandlerCollection contextHandlerCollection = new HandlerCollection(true);
        myServer.setHandler(contextHandlerCollection);
        LOGGER.info("Server starting...");
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
        handler.setInitParameter(CoreSettings.TAG_SERVICE_ROOT_URL, serverSetting.getServiceRootUrl());
        handler.setInitParameter(CoreSettings.TAG_TEMP_PATH, System.getProperty("java.io.tmpdir"));

        handler.setInitParameter("persistence.persistenceManagerImplementationClass", VAL_PERSISTENCE_MANAGER);
        handler.setInitParameter("persistence.autoUpdateDatabase", "true");
        handler.setInitParameter("persistence.db.driver", "org.postgresql.Driver");
        handler.setInitParameter("persistence.db.url", pgConnectUrl);
        handler.setInitParameter("persistence.db.username", VAL_PG_USER);
        handler.setInitParameter("persistence.db.password", VAL_PG_PASS);

        handler.setInitParameter("bus." + BusSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus");
        handler.setInitParameter("bus." + MqttMessageBus.TAG_MQTT_BROKER, "tcp://" + mqttBus.getContainerIpAddress() + ":" + mqttBus.getFirstMappedPort());

        handler.getInitParams().putAll(paramsMap);

        handler.addEventListener(new HttpContextListener());
        handler.addServlet(DatabaseStatus.class, "/DatabaseStatus");
        handler.addServlet(ServletV1P0.class, "/v1.0/*");
        handler.addServlet(ServletV1P0.class, "/v1.1/*");
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

    private void startMqttServer(Properties parameters) {
        ServerSettings serverSetting = serverSettings.get(parameters);

        int mqttPort = findRandomPort();
        LOGGER.info("Generated random port {}", mqttPort);
        Properties properties = new Properties();
        properties.put(CoreSettings.TAG_SERVICE_ROOT_URL, serverSetting.getServiceRootUrl());
        properties.put(CoreSettings.TAG_ENABLE_ACTUATION, "false");
        properties.put(CoreSettings.TAG_ENABLE_MULTIDATASTREAM, "true");
        properties.put(CoreSettings.TAG_TEMP_PATH, System.getProperty("java.io.tmpdir"));

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
        properties.put("mqtt.WebsocketPort", "9876");

        properties.put("persistence.persistenceManagerImplementationClass", VAL_PERSISTENCE_MANAGER);
        properties.put("persistence.db.driver", "org.postgresql.Driver");
        properties.put("persistence.db.url", "jdbc:postgresql://" + pgServer.getContainerIpAddress() + ":" + pgServer.getFirstMappedPort() + "/" + VAL_PG_DB);
        properties.put("persistence.db.username", VAL_PG_USER);
        properties.put("persistence.db.password", VAL_PG_PASS);
        properties.put("bus." + BusSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus");
        properties.put("bus." + MqttMessageBus.TAG_MQTT_BROKER, "tcp://" + mqttBus.getContainerIpAddress() + ":" + mqttBus.getFirstMappedPort());
        if (parameters != null) {
            properties.putAll(parameters);
        }

        CoreSettings coreSettings = new CoreSettings(properties);
        FrostMqttServer server = new FrostMqttServer(coreSettings);
        server.start();
        serverSetting.setMqttUrl("tcp://localhost:" + mqttPort);
        mqttServers.put(parameters, server);
    }

    public synchronized void stopServer(Properties parameters) {
        if (!httpServers.containsKey(parameters)) {
            return;
        }
        Server httpServer = httpServers.get(parameters);
        try {
            httpServer.stop();
        } catch (Exception ex) {
            LOGGER.error("Exception stopping server!");
            throw new IllegalStateException(ex);
        }
        httpServers.remove(parameters);
        FrostMqttServer mqttServer = mqttServers.get(parameters);
        try {
            mqttServer.stop();
        } catch (Exception ex) {
            LOGGER.error("Exception stopping server!");
            throw new IllegalStateException(ex);
        }
        mqttServers.remove(parameters);
        serverSettings.remove(parameters);
    }

    public synchronized void stopAllServers() {
        for (Properties props : httpServers.keySet().toArray(new Properties[httpServers.size()])) {
            stopServer(props);
        }
        pgServer.stop();
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
            Assert.fail("Cannot fetch service root url from " + rootUri + ".");
            return;
        }

        JSONObject jsonResponse;
        JSONArray entities;
        try {
            jsonResponse = new JSONObject(response.response);
            entities = jsonResponse.getJSONArray("value");
        } catch (JSONException e) {
            LOGGER.error("The service response for the root URI '" + rootUri + "' is not JSON.", e);
            Assert.fail("The service response for the root URI '" + rootUri + "' is not JSON.");
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
                    Assert.fail("The name component of Service root URI response is not available.");
                    return;
                }
                name = entity.getString("name");
            } catch (JSONException e) {
                LOGGER.error("The service response for the root URI '" + rootUri + "' is not JSON.", e);
                Assert.fail("The service response for the root URI '" + rootUri + "' is not JSON.");
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
                Assert.fail("Server lists Actuation entities, but does not claim reqirement " + ServerSettings.TASKING_REQ.getName());
            }
            if (hasMultiDatastream && !serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ)) {
                Assert.fail("Server lists the MultiDatastream entity, but does not claim reqirement " + ServerSettings.MULTIDATA_REQ.getName());
            }
        }
    }

}
