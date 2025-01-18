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

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_MQTT;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PLUGINS;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_FILTER_DELETE_ENABLE;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11MultiDatastream;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Tasking;
import de.fraunhofer.iosb.ilt.frostserver.plugin.actuation.ActuationModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.MdsModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.AfterAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public abstract class AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestClass.class);

    /**
     * The server version to test. This is the JUnit parameter for this class.
     */
    protected static ServerVersion version;
    /**
     * The setting for the server.
     */
    protected static ServerSettings serverSettings;
    /**
     * A FROST-Client instance that can be used to access the server.
     */
    protected static StaService service;
    protected static SensorThingsService sSrvc;
    protected static SensorThingsV11Sensing sMdl;
    protected static SensorThingsV11MultiDatastream mMdl;
    protected static SensorThingsV11Tasking tMdl;

    private static final Map<String, String> defaultProperties = new TreeMap<>();

    static {
        defaultProperties.put(PREFIX_PLUGINS + ActuationModelSettings.TAG_ENABLE_ACTUATION, "true");
        defaultProperties.put(PREFIX_PLUGINS + MdsModelSettings.TAG_ENABLE_MDS_MODEL, "true");
        defaultProperties.put(TAG_FILTER_DELETE_ENABLE, "true");
        defaultProperties.put(PREFIX_MQTT + MqttSettings.TAG_MQTT_ALLOW_FILTER, "true");
        defaultProperties.put(PREFIX_MQTT + "session.timeout.seconds", "100");
    }

    public AbstractTestClass(ServerVersion serverVersion) {
        init(serverVersion, defaultProperties);
    }

    public AbstractTestClass(ServerVersion serverVersion, Map<String, String> properties) {
        init(serverVersion, Collections.unmodifiableMap(properties));
    }

    private void init(ServerVersion serverVersion, Map<String, String> properties) {
        try {
            LOGGER.trace("Init for version {} on {}.", serverVersion.urlPart, getClass());
            if (!serverVersion.equals(version)) {
                if (version != null) {
                    tearDownVersion();
                }
                version = serverVersion;
                LOGGER.trace("Setting up for version {}.", version.urlPart);
                TestSuite suite = TestSuite.getInstance();
                serverSettings = suite.getServerSettings(properties);
                try {

                    sSrvc = createService();
                    sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
                    mMdl = sSrvc.getModel(SensorThingsV11MultiDatastream.class);
                    tMdl = sSrvc.getModel(SensorThingsV11Tasking.class);
                    service = new StaService(sSrvc, sMdl, tMdl, mMdl);
                } catch (MalformedURLException ex) {
                    LOGGER.error("Failed to create URL", ex);
                }
                setUpVersion();
            }
        } catch (RuntimeException | ServiceFailureException | URISyntaxException | IOException | InterruptedException ex) {
            LOGGER.error("init failed.", ex);
        }
    }

    protected SensorThingsService createService() throws MalformedURLException, URISyntaxException {
        return new SensorThingsService(new SensorThingsV11Sensing(), new SensorThingsV11MultiDatastream(), new SensorThingsV11Tasking())
                .setBaseUrl(new URI(serverSettings.getServiceUrl(version)).toURL())
                .init();
    }

    protected abstract void setUpVersion() throws ServiceFailureException, URISyntaxException;

    protected void tearDownVersion() throws ServiceFailureException {
        // Empty by design.
    }

    @AfterAll
    public static final void cleanupAbstractClass() {
        version = null;
        serverSettings = null;
        service = null;
    }

    /**
     * The server version to test. This is the JUnit parameter for this class.
     *
     * @return the version
     */
    public static ServerVersion getVersion() {
        return version;
    }

    /**
     * The setting for the server.
     *
     * @return the serverSettings
     */
    public static ServerSettings getServerSettings() {
        return serverSettings;
    }

    /**
     * A FROST-Client instance that can be used to access the server.
     *
     * @return the service
     */
    public static StaService getService() {
        return service;
    }

}
