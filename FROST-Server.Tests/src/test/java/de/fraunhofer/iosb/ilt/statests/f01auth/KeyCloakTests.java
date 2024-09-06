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
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.filterForException;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.utils.TokenManagerOpenIDConnect;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for access rights checking with KeyCloak Authentication.
 *
 * @author Hylke van der Schaaf
 */
public abstract class KeyCloakTests extends AbstractAuthTests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyCloakTests.class);

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    public static final String KEYCLOAK_FROST_CLIENT_ID = "frost-server";
    public static final String KEYCLOAK_FROST_CONFIG_SECRET = "5aa9087d-817f-47b6-92a1-2b5f7caac967";
    public static final String KEYCLOAK_TOKEN_PATH = "/realms/FROST-Test/protocol/openid-connect/token";

    private static final SensorThingsV11Sensing mdlSensing = new SensorThingsV11Sensing();
    private static final SensorThingsUserModel mdlUsers = new SensorThingsUserModel();
    private static final SensorThingsService baseService = new SensorThingsService(mdlSensing, mdlUsers);
    private static final List<Entity> USERS = new ArrayList<>();

    private static String modelUrl(String name) {
        return resourceUrl("finegrainedsecurity/model/", name);
    }

    private static String resourceUrl(String path, String name) {
        try {
            return IOUtils.resourceToURL(path + "/" + name, KeyCloakTests.class.getClassLoader()).getFile();
        } catch (IOException ex) {
            LOGGER.error("Failed", ex);
            return "";
        }
    }

    static {
        final String dbName = "keycloakauth";
        SERVER_PROPERTIES.put("auth.db.url", TestSuite.createDbUrl(dbName));
        SERVER_PROPERTIES.put("auth.db.driver", "org.postgresql.Driver");
        SERVER_PROPERTIES.put("auth.db.username", TestSuite.VAL_PG_USER);
        SERVER_PROPERTIES.put("auth.db.password", TestSuite.VAL_PG_PASS);
        SERVER_PROPERTIES.put(KEY_DB_NAME, dbName);

        SERVER_PROPERTIES.put("auth_provider", "de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider");
        SERVER_PROPERTIES.put("auth_keycloakConfigUrl", TestSuite.getInstance().getKeycloak().getAuthServerUrl() + "/realms/FROST-Test/clients-registrations/install/" + KEYCLOAK_FROST_CLIENT_ID);
        SERVER_PROPERTIES.put("auth_keycloakConfigSecret", KEYCLOAK_FROST_CONFIG_SECRET);
        SERVER_PROPERTIES.put("auth_allowAnonymousRead", "false");
        SERVER_PROPERTIES.put("auth_registerUserLocally", "true");
        SERVER_PROPERTIES.put("plugins.coreModel.idType", "LONG");
        SERVER_PROPERTIES.put("plugins.modelLoader.enable", "true");
        SERVER_PROPERTIES.put("plugins.modelLoader.modelPath", "");
        SERVER_PROPERTIES.put("plugins.modelLoader.modelFiles", modelUrl("Role.json") + ", " + modelUrl("UserNoPass.json"));
        SERVER_PROPERTIES.put("plugins.modelLoader.liquibasePath", "target/test-classes/finegrainedsecurity/liquibase");
        SERVER_PROPERTIES.put("plugins.modelLoader.liquibaseFiles", "tablesSecurityUPR.xml");
        SERVER_PROPERTIES.put("plugins.modelLoader.idType.Role", "STRING");
        SERVER_PROPERTIES.put("plugins.modelLoader.idType.User", "STRING");
        SERVER_PROPERTIES.put("persistence.idGenerationMode.Role", "ClientGeneratedOnly");
        SERVER_PROPERTIES.put("persistence.idGenerationMode.User", "ClientGeneratedOnly");
    }

    public KeyCloakTests(ServerVersion version) {
        super(version, SERVER_PROPERTIES, false);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = mdlSensing;
        super.setUpVersion();
        USERS.clear();
        USERS.add(mdlUsers.newUser("admin", null));
        USERS.add(mdlUsers.newUser("read", null));
        USERS.add(mdlUsers.newUser("write", null));
    }

    @Test
    void test_100_ReadUser() {
        LOGGER.info("  test_100_ReadUser");
        testFilterResults(ADMIN, serviceAdmin, mdlUsers.etUser, "", USERS);
        filterForException(ANONYMOUS, serviceAnon, mdlUsers.etUser, "", AuthTestHelper.HTTP_CODE_403_FORBIDDEN);
    }

    @Override
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
                        .setPassword(password));
        return service;
    }

}
