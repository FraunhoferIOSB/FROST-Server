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
package de.fraunhofer.iosb.ilt.statests.f01auth;

import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import java.net.URL;
import java.util.Properties;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for access rights checking with Basic Authentication.
 *
 * @author Hylke van der Schaaf
 */
public abstract class BasicAuthTests extends AbstractAuthTests {

    public static class Implementation10 extends BasicAuthTests {

        public Implementation10() {
            super(ServerVersion.v_1_0);
        }

    }

    public static class Implementation11 extends BasicAuthTests {

        public Implementation11() {
            super(ServerVersion.v_1_1);
        }

    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthTests.class);

    private static final Properties SERVER_PROPERTIES = new Properties();

    static {
        SERVER_PROPERTIES.put("auth_provider", "de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider");
        SERVER_PROPERTIES.put("auth_allowAnonymousRead", "false");
        SERVER_PROPERTIES.put("auth_autoUpdateDatabase", "true");
        SERVER_PROPERTIES.put("auth_db_driver", "org.postgresql.Driver");
        SERVER_PROPERTIES.put("auth_db_url", TestSuite.getInstance().getPgConnectUrl());
        SERVER_PROPERTIES.put("auth_db_username", TestSuite.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth_db_password", TestSuite.VAL_PG_PASS);
    }

    public BasicAuthTests(ServerVersion version) {
        super(version, SERVER_PROPERTIES, false);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        super.setUpVersion();
    }

    @Override
    public SensorThingsService getServiceAdmin() {
        return setAuth(createService(), "admin", "admin");
    }

    @Override
    public SensorThingsService getServiceWrite() {
        return setAuth(createService(), "write", "write");
    }

    @Override
    public SensorThingsService getServiceRead() {
        return setAuth(createService(), "read", "read");
    }

    @Override
    public SensorThingsService getServiceAnonymous() {
        return createService();
    }

    public static SensorThingsService setAuth(SensorThingsService service, String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        URL url = service.getEndpoint();

        credsProvider.setCredentials(
                new AuthScope(url.getHost(), url.getPort()),
                new UsernamePasswordCredentials(username, password));

        service.getClientBuilder()
                .setDefaultCredentialsProvider(credsProvider);

        service.rebuildHttpClient();
        return service;
    }

}
