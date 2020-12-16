/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
@RunWith(Parameterized.class)
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
    protected static SensorThingsService service;

    private static final Properties defaultProperties = new Properties();

    static {
        defaultProperties.put("plugins.actuation.enable", "true");
        defaultProperties.put("plugins.multiDatastream.enable", "true");
    }

    public AbstractTestClass(ServerVersion serverVersion) {
        init(serverVersion, defaultProperties);
    }

    public AbstractTestClass(ServerVersion serverVersion, Properties properties) {
        init(serverVersion, properties);
    }

    private void init(ServerVersion serverVersion, Properties properties) {
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
                    service = new SensorThingsService(new URL(serverSettings.getServiceUrl(version)));
                } catch (MalformedURLException ex) {
                    LOGGER.error("Failed to create URL", ex);
                }
                setUpVersion();
            }
        } catch (RuntimeException | ServiceFailureException | URISyntaxException | IOException | InterruptedException ex) {
            LOGGER.error("init failed.", ex);
        }
    }

    protected abstract void setUpVersion() throws ServiceFailureException, URISyntaxException;

    protected void tearDownVersion() throws ServiceFailureException {
        // Empty by design.
    }

    /**
     * The collection of parameters for testing.
     *
     * @return The collection of parameters for testing.
     */
    @Parameterized.Parameters
    public static Collection versions() {
        return Arrays.asList(ServerVersion.values());
    }

    @AfterClass
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
    public static SensorThingsService getService() {
        return service;
    }

}
