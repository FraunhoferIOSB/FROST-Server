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
package de.fraunhofer.iosb.ilt.frostserver.auth.oidc;

import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_KEYCLOAK_CONFIG;
import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_KEYCLOAK_CONFIG_FILE;
import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_KEYCLOAK_CONFIG_SECRET;
import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_KEYCLOAK_CONFIG_URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils.Role;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

public class Utils {

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
        // Utility class.
    }

    /**
     * An interface for classes that map HTTP methods to a user Role.
     */
    public static interface MethodRoleMapper {

        /**
         * Map the given method to a user Role.
         *
         * @param method The method to map.
         * @return the Role the user must have to execute the method.
         */
        public Role findRole(HttpMethod method);
    }

    private static class WellKnownOpenidConfig {

    }

    private static class KeycloakConfig {

        public String realm;

        @JsonProperty("auth-server-url")
        public String authServerUrl;

        @JsonProperty("ssl-required")
        public String sslRequired;

        public String resource;

        @JsonProperty("public-client")
        public boolean publicClient;

        @JsonProperty("use-resource-role-mappings")
        public boolean useResourceRoleMappings;

        @JsonProperty("confidential-port")
        public int confidentialPort;

        public OidcConfiguration toOidcConfiguration() {
            return new OidcConfiguration()
                    .setClientId(resource)
                    .setAuthEndpoint(authServerUrl + "realms/" + realm + "/protocol/openid-connect/auth")
                    .setTokenEndpoint(authServerUrl + "realms/" + realm + "/protocol/openid-connect/token");
        }
    }

    /**
     * Load the keycloak config from the given CoreSettings. A keycloak config
     * looks like:
     *
     * <pre>
     * {
     *     "realm": "FROST-Test",
     *     "auth-server-url": "http://192.168.178.46:8180/",
     *     "ssl-required": "external",
     *     "resource": "frost-server",
     *     "public-client": true,
     *     "use-resource-role-mappings": true,
     *     "confidential-port": 0
     * }
     * </pre>
     *
     * @param authSettings the OidcSettings to load the keycloak config from.
     * @return the keycloak config.
     */
    public static String getKeycloakConfig(OidcSettings authSettings) {
        String keycloakConfig = authSettings.get(TAG_KEYCLOAK_CONFIG);
        if (!StringHelper.isNullOrEmpty(keycloakConfig)) {
            return keycloakConfig;
        }
        keycloakConfig = getKeycloakConfigFromFile(authSettings);
        if (!StringHelper.isNullOrEmpty(keycloakConfig)) {
            return keycloakConfig;
        }
        keycloakConfig = getKeycloakConfigFromServer(authSettings);
        return keycloakConfig;
    }

    /**
     * Load the contents of a keycloak config file, specified in the given
     * Settings object.
     *
     * @param authSettings The settings object to fetch the config file path
     * from.
     * @return the contents of the config file.
     */
    private static String getKeycloakConfigFromFile(OidcSettings authSettings) {
        String keycloakConfigFile = authSettings.get(TAG_KEYCLOAK_CONFIG_FILE);
        if (StringHelper.isNullOrEmpty(keycloakConfigFile)) {
            return "";
        }
        try {
            return FileUtils.readFileToString(FileUtils.getFile(keycloakConfigFile), StringHelper.UTF8);
        } catch (IOException exc) {
            LOGGER.error("Failed to read keycloak config file.", exc);
            return "";
        }
    }

    /**
     * Load the contents of a keycloak config file, specified in the given
     * Settings object.
     *
     * @param authSettings The settings object to fetch the config file path
     * from.
     * @return the contents of the config file.
     */
    private static String getKeycloakConfigFromServer(OidcSettings authSettings) {
        String keycloakConfigUrl = authSettings.get(TAG_KEYCLOAK_CONFIG_URL);
        if (StringHelper.isNullOrEmpty(keycloakConfigUrl)) {
            return "";
        }
        String keycloakConfigSecret = authSettings.get(TAG_KEYCLOAK_CONFIG_SECRET);

        LOGGER.info("Fetching Keycloak config from server: {}", keycloakConfigUrl);
        try (CloseableHttpClient client = HttpClients.createSystem()) {
            HttpGet httpGet = new HttpGet(keycloakConfigUrl);
            if (!StringHelper.isNullOrEmpty(keycloakConfigSecret)) {
                String clientId = keycloakConfigUrl.substring(keycloakConfigUrl.lastIndexOf('/') + 1);
                String encoded = clientId + ":" + keycloakConfigSecret;
                httpGet.addHeader("Authorization", "basic " + Base64.encodeBase64String(encoded.getBytes()));
            }
            HttpResponse httpResponse = client.execute(httpGet);
            String configString = EntityUtils.toString(httpResponse.getEntity(), StringHelper.UTF8);
            LOGGER.info("Fetched Keycloak config from server. Size {}", configString.length());
            return configString;
        } catch (IOException exc) {
            LOGGER.error("Failed to read keycloak config file.", exc);
            return "";
        }
    }

    private static OidcConfiguration configFromKeycloak(OidcSettings authSettings) {
        String keycloakConfig = getKeycloakConfig(authSettings);
        if (StringHelper.isNullOrEmpty(keycloakConfig)) {
            return null;
        }
        try {
            KeycloakConfig kcConfig = SimpleJsonMapper.getSimpleObjectMapper().readValue(keycloakConfig, KeycloakConfig.class);
            return kcConfig.toOidcConfiguration();
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to parse KeyCloak config.", ex);
            return null;
        }
    }

    /**
     * Create a new OidcConfiguration from settings loaded from the given
     * CoreSettings.
     *
     * @param coreSettings The CoreSettings to create an OidcConfiguration from.
     * @return the new OidcConfiguration.
     */
    public static OidcConfiguration createConfiguration(CoreSettings coreSettings) {
        OidcSettings settings = new OidcSettings().setSettings(coreSettings.getAuthSettings());
        OidcConfiguration configFromKeycloak = configFromKeycloak(settings);
        if (configFromKeycloak != null) {
            return configFromKeycloak;
        }
        return null;
    }

}
