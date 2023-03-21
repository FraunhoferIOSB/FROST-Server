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

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.sta.service.TokenManagerOpenIDConnect;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for access rights checking with KeyCloak Authentication.
 *
 * @author Hylke van der Schaaf
 */
public abstract class KeyCloakTests extends AbstractAuthTests {

    public static class Implementation10 extends KeyCloakTests {

        public Implementation10() {
            super(ServerVersion.v_1_0);
        }

    }

    public static class Implementation11 extends KeyCloakTests {

        public Implementation11() {
            super(ServerVersion.v_1_1);
        }

    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyCloakTests.class);

    private static final Properties SERVER_PROPERTIES = new Properties();

    public static final String KEYCLOAK_FROST_CLIENT_ID = "frost-server";
    public static final String KEYCLOAK_FROST_CONFIG_SECRET = "5aa9087d-817f-47b6-92a1-2b5f7caac967";
    public static final String KEYCLOAK_TOKEN_PATH = "/realms/FROST-Test/protocol/openid-connect/token";

    static {
        SERVER_PROPERTIES.put("auth_provider", "de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider");
        SERVER_PROPERTIES.put("auth_keycloakConfigUrl", TestSuite.getInstance().getKeycloak().getAuthServerUrl() + "/realms/FROST-Test/clients-registrations/install/" + KEYCLOAK_FROST_CLIENT_ID);
        SERVER_PROPERTIES.put("auth_keycloakConfigSecret", KEYCLOAK_FROST_CONFIG_SECRET);
        SERVER_PROPERTIES.put("auth_allowAnonymousRead", "false");
    }

    public KeyCloakTests(ServerVersion version) {
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
        KeycloakContainer keycloak = TestSuite.getInstance().getKeycloak();
        service.setTokenManager(
                new TokenManagerOpenIDConnect()
                        .setTokenServerUrl(keycloak.getAuthServerUrl() + KEYCLOAK_TOKEN_PATH)
                        .setClientId(KEYCLOAK_FROST_CLIENT_ID)
                        .setUserName(username)
                        .setPassword(password)
        );
        return service;
    }

}
