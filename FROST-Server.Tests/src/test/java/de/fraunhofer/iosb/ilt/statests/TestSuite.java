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
import de.fraunhofer.iosb.ilt.frostserver.http.common.ServletV1P0;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus;
import de.fraunhofer.iosb.ilt.frostserver.settings.BusSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Properties;
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
    de.fraunhofer.iosb.ilt.statests.c01sensingcore.Capability1Tests.class,
    de.fraunhofer.iosb.ilt.statests.c02cud.Capability2Tests.class,
    de.fraunhofer.iosb.ilt.statests.c02cud.AdditionalTests.class,
    de.fraunhofer.iosb.ilt.statests.c02cud.DeleteFilterTests.class,
    de.fraunhofer.iosb.ilt.statests.c02cud.JsonPatchTests.class,
    de.fraunhofer.iosb.ilt.statests.c02cud.ResultTypesTests.class,
    de.fraunhofer.iosb.ilt.statests.c03filtering.Capability3Tests.class,
    de.fraunhofer.iosb.ilt.statests.c03filtering.DateTimeTests.class,
    de.fraunhofer.iosb.ilt.statests.c03filtering.FilterTests.class,
    de.fraunhofer.iosb.ilt.statests.c03filtering.GeoTests.class,
    de.fraunhofer.iosb.ilt.statests.c03filtering.JsonPropertiesTests.class,
    de.fraunhofer.iosb.ilt.statests.c05multidatastream.MultiDatastreamTests.class,
    de.fraunhofer.iosb.ilt.statests.c06dataarrays.DataArrayTests.class,
    de.fraunhofer.iosb.ilt.statests.c07mqttcreate.Capability7Tests.class,
    de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe.Capability8Tests.class
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

    private Server httpServer;
    private FrostMqttServer mqttServer;
    private final ServerSettings serverSettings = new ServerSettings();

    @Rule
    public GenericContainer pgServer = new GenericContainer<>("mdillon/postgis:latest")
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
            } catch (Exception ex) {
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
    public static void setUpClass() throws Exception {
        LOGGER.info("Starting Servers...");
        TestSuite myInstance = getInstance();
        myInstance.startServers();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        LOGGER.info("Stopping Servers...");
        getInstance().stopServer();
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public Server getServer() throws Exception {
        if (httpServer == null) {
            startServers();
        }
        return httpServer;
    }

    private synchronized void startServers() throws Exception {
        if (httpServer != null) {
            return;
        }
        pgServer.start();
        mqttBus.start();

        Container.ExecResult execResult = pgServer.execInContainer("psql", "-U" + VAL_PG_USER, "-d" + VAL_PG_DB, "-c CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");
        LOGGER.info("Installing extension uuid-ossp: {} {}", execResult.getStdout(), execResult.getStderr());

        startHttpServer();
        startMqttServer();
    }

    private void startHttpServer() throws Exception {
        Server myServer = new Server(0);
        HandlerCollection contextHandlerCollection = new HandlerCollection(true);
        myServer.setHandler(contextHandlerCollection);
        LOGGER.info("Server starting...");
        myServer.start();

        Connector[] connectors = myServer.getConnectors();
        ServerConnector connecor = (ServerConnector) connectors[0];

        serverSettings.setServiceRootUrl("http://localhost:" + connecor.getLocalPort() + "");

        ServletContextHandler handler = new ServletContextHandler();
        handler.getServletContext().setExtendedListenerTypes(true);
        handler.setInitParameter(CoreSettings.TAG_SERVICE_ROOT_URL, serverSettings.serviceRootUrl);
        handler.setInitParameter(CoreSettings.TAG_TEMP_PATH, System.getProperty("java.io.tmpdir"));

        handler.setInitParameter("persistence.persistenceManagerImplementationClass", VAL_PERSISTENCE_MANAGER);
        handler.setInitParameter("persistence.autoUpdateDatabase", "true");
        handler.setInitParameter("persistence.db.driver", "org.postgresql.Driver");
        handler.setInitParameter("persistence.db.url", "jdbc:postgresql://" + pgServer.getContainerIpAddress() + ":" + pgServer.getFirstMappedPort() + "/" + VAL_PG_DB);
        handler.setInitParameter("persistence.db.username", VAL_PG_USER);
        handler.setInitParameter("persistence.db.password", VAL_PG_PASS);

        handler.setInitParameter("bus." + BusSettings.TAG_IMPLEMENTATION_CLASS, "de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus");
        handler.setInitParameter("bus." + MqttMessageBus.TAG_MQTT_BROKER, "tcp://" + mqttBus.getContainerIpAddress() + ":" + mqttBus.getFirstMappedPort());

        handler.addEventListener(new HttpContextListener());
        handler.addServlet(ServletV1P0.class, "/v1.0/*");
        contextHandlerCollection.addHandler(handler);
        handler.start();

        LOGGER.info("Server started.");
        httpServer = myServer;

        checkServiceRootUri(serverSettings);
        serverSettings.initExtensionsAndTypes();
    }

    private void startMqttServer() {
        int mqttPort = findRandomPort();
        LOGGER.info("Generated random port {}", mqttPort);
        Properties properties = new Properties();
        properties.put(CoreSettings.TAG_SERVICE_ROOT_URL, serverSettings.serviceRootUrl);
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

        CoreSettings coreSettings = new CoreSettings(properties);
        FrostMqttServer server = new FrostMqttServer(coreSettings);
        server.start();
        serverSettings.mqttUrl = "tcp://localhost:" + mqttPort;
        mqttServer = server;
    }

    public synchronized void stopServer() throws Exception {
        if (httpServer == null) {
            return;
        }
        httpServer.stop();
        httpServer = null;
        mqttServer.stop();
        mqttServer = null;
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

    /**
     * Checking the service root URL to be compliant with SensorThings API
     *
     * @param serverSettings the settings for the httpServer.
     */
    public void checkServiceRootUri(ServerSettings serverSettings) {
        String rootUri = serverSettings.serviceUrl;
        if (rootUri.endsWith("/")) {
            rootUri = rootUri.substring(0, rootUri.length() - 1);
        }
        HttpURLConnection connection;
        String response;
        URL url;
        try {
            url = new URL(rootUri);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Get Response
            InputStream is = connection.getInputStream();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    responseBuilder.append(line);
                    responseBuilder.append('\r');
                }
                response = responseBuilder.toString();
            }
        } catch (IOException e) {
            LOGGER.error("Cannot connect to " + rootUri + ".", e);
            Assert.fail("Cannot connect to " + rootUri + ".");
            return;
        }
        JSONObject jsonResponse;
        JSONArray entities;
        try {
            jsonResponse = new JSONObject(response);
            entities = jsonResponse.getJSONArray("value");
        } catch (JSONException e) {
            LOGGER.error("The service response for the root URI \"" + rootUri + "\" is not JSON.", e);
            Assert.fail("The service response for the root URI \"" + rootUri + "\" is not JSON.");
            return;
        }
        serverSettings.hasActuation = false;
        serverSettings.hasMultiDatastream = false;
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
                LOGGER.error("The service response for the root URI \"" + rootUri + "\" is not JSON.", e);
                Assert.fail("The service response for the root URI \"" + rootUri + "\" is not JSON.");
                return;
            }
            switch (name) {
                case "Actuators":
                case "Tasks":
                case "TaskingCapabilities":
                    serverSettings.hasActuation = true;
                    break;

                case "MultiDatastreams":
                    serverSettings.hasMultiDatastream = true;
                    break;

                default:
                // Nothing special...
            }
        }
    }

}
