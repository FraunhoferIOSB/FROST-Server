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

import static de.fraunhofer.iosb.ilt.statests.TestSuite.KEY_DB_NAME;
import static de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakTests.KEYCLOAK_FROST_CLIENT_ID;
import static de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakTests.KEYCLOAK_FROST_CONFIG_SECRET;
import static de.fraunhofer.iosb.ilt.statests.f01auth.KeyCloakTests.KEYCLOAK_TOKEN_PATH;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.utils.TokenManagerOpenIDConnect;
import de.fraunhofer.iosb.ilt.frostserver.plugin.projects.ProjectRoleDecoder;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the FineGrained Auth Tests using Keycloak Auth.
 */
public class FineGrainedAuthTestsKeycloak extends FineGrainedAuthTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(FineGrainedAuthTestsKeycloak.class.getName());
    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();
    private static final Map<String, String> SERVER_PROPERTIES_ANON = new LinkedHashMap<>();

    static {
        FineGrainedAuthTests.addCommonProperties(SERVER_PROPERTIES);
        final String dbName = "fineGrainedAuthKeycloak";
        final String dbDriver = "org.postgresql.Driver";
        SERVER_PROPERTIES.put("auth.db.url", TestSuite.createDbUrl(dbDriver, dbName));
        SERVER_PROPERTIES.put("auth.db.driver", dbDriver);
        SERVER_PROPERTIES.put("auth.db.username", TestSuite.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth.db.password", TestSuite.VAL_PG_PASS);
        SERVER_PROPERTIES.put("auth.plainTextPassword", "false");
        SERVER_PROPERTIES.put("auth.autoUpdateDatabase", "true");
        SERVER_PROPERTIES.put(KEY_DB_NAME, dbName);

        SERVER_PROPERTIES.put("auth.provider", "de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider");
        SERVER_PROPERTIES.put("auth.keycloakConfigUrl", TestSuite.getInstance().getKeycloak().getAuthServerUrl() + "/realms/FROST-Test/clients-registrations/install/" + KEYCLOAK_FROST_CLIENT_ID);
        SERVER_PROPERTIES.put("auth.keycloakConfigSecret", KEYCLOAK_FROST_CONFIG_SECRET);
        SERVER_PROPERTIES.put("auth.allowAnonymousRead", "false");
        SERVER_PROPERTIES.put("auth.authenticateOnly", "true");
        SERVER_PROPERTIES.put("auth.registerUserLocally", "true");
        SERVER_PROPERTIES.put("auth.userRoleDecoderClass", ProjectRoleDecoder.class.getName());

        final String dbNameAnon = "fineGrainedAuthKeycloakAnon";
        SERVER_PROPERTIES_ANON.putAll(SERVER_PROPERTIES);
        SERVER_PROPERTIES_ANON.put("auth.db.url", TestSuite.createDbUrl(dbDriver, dbNameAnon));
        SERVER_PROPERTIES_ANON.put(KEY_DB_NAME, dbNameAnon);
        SERVER_PROPERTIES_ANON.put("auth_allowAnonymousRead", "true");
    }

    public FineGrainedAuthTestsKeycloak(ServerVersion version, boolean anonymousReadAllowed) {
        super(version,
                anonymousReadAllowed ? SERVER_PROPERTIES_ANON : SERVER_PROPERTIES,
                anonymousReadAllowed);
    }

    @BeforeEach
    void checkEntities() {
        if (USER_PROJECT_ROLES.isEmpty()) {
            USERS.clear();
            try {
                LOGGER.info("Fetching UserProjectRoles");
                USER_PROJECT_ROLES.addAll(serviceAdmin.query(mdlUsers.etUserProjectRole).top(100).orderBy("id").list().toList());
            } catch (ServiceFailureException ex) {
                LOGGER.error("Failed to fetch reference entities", ex);
            }
        }
        if (USERS.isEmpty()) {
            try {
                LOGGER.info("Fetching Users");
                USERS.addAll(serviceAdmin.query(mdlUsers.etUser).top(100).orderBy("username").list().toList());
            } catch (ServiceFailureException ex) {
                LOGGER.error("Failed to fetch reference entities", ex);
            }
        }

    }

    @Override
    public String getBatchPostData() throws IOException {
        String batchPostData = IOUtils.resourceToString("finegrainedsecurity/dataBatchPostKeyCloak.json", StandardCharsets.UTF_8, FineGrainedAuthTests.class.getClassLoader());
        return batchPostData;
    }

    @Override
    public void createServices() {
        serviceAdmin = setAuth(createService(), "admin", "admin");
        serviceWrite = setAuth(createService(), "write", "write");
        serviceRead = setAuth(createService(), "read", "read");
        serviceAnon = createService();
        serviceAdminProject1 = setAuth(createService(), "AdminProject1", "AdminProject1");
        serviceAdminProject2 = setAuth(createService(), "AdminProject2", "AdminProject2");
        serviceObsCreaterProject1 = setAuth(createService(), "ObsCreaterProject1", "ObsCreaterProject1");
        serviceObsCreaterProject2 = setAuth(createService(), "ObsCreaterProject2", "ObsCreaterProject2");
    }

    protected SensorThingsService createService() {
        if (!baseService.isBaseUrlSet()) {
            try {
                baseService.setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                        .init();
            } catch (MalformedURLException | URISyntaxException ex) {
                throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
            }
        }
        try {
            return new SensorThingsService(baseService.getModelRegistry())
                    .setBaseUrl(new URI(serverSettings.getServiceUrl(version)))
                    .init();
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new IllegalArgumentException("Serversettings contains malformed URL.", ex);
        }
    }

    public static SensorThingsService setAuth(SensorThingsService service, String username, String password) {
        KeycloakContainer keycloak = TestSuite.getInstance().getKeycloak();
        service.setTokenManager(
                new TokenManagerOpenIDConnect()
                        .setTokenServerUrl(keycloak.getAuthServerUrl() + KEYCLOAK_TOKEN_PATH)
                        .setClientId(KEYCLOAK_FROST_CLIENT_ID)
                        .setUserName(username)
                        .setPassword(password));
        service.getOrCreateMqttConfig().setAuth(username, password);
        return service;
    }

}
