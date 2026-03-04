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
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_ADMIN;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import de.fraunhofer.iosb.ilt.frostserver.model.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils.Role;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserData;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * A Well-Known OpenID config. Usually loaded from a URL like:
     * http://keycloak.example.com/realms/[realm]/.well-known/openid-configuration
     */
    private static class WellKnownOpenidConfig {

        public String issuer;

        @JsonProperty("authorization_endpoint")
        public String authorizationEndpoint;

        @JsonProperty("token_endpoint")
        public String tokenEndpoint;

        @JsonProperty("introspection_endpoint")
        public String introspectionEndpoint;

        @JsonProperty("userinfo_endpoint")
        public String userinfoEndpoint;

        @JsonProperty("end_session_endpoint")
        public String endSessionEndpoint;

        @JsonProperty("jwks_uri")
        public String jwksUri;

        @JsonIgnore
        public Map<String, Object> otherAttributes;

        @JsonAnySetter
        public void setAttribute(String key, Object value) {
            if (otherAttributes == null) {
                otherAttributes = new HashMap<>();
            }
            otherAttributes.put(key, value);
        }

        public OidcConfiguration toOidcConfiguration(OidcConfiguration target) {
            return target.setAuthEndpoint(authorizationEndpoint)
                    .setIssuer(issuer)
                    .setJwksUri(jwksUri)
                    .setTokenEndpoint(tokenEndpoint)
                    .setUserinfoEndpoint(userinfoEndpoint);
        }
    }

    /**
     * A KeyCloak config. Usually loaded from a URL like:
     * https://keycloak.example.com/auth/realms/[realm]/clients-registrations/install/[clientId]
     */
    private static class KeycloakConfig {

        private static class Credentials {

            public String secret;

            public Credentials setSecret(String secret) {
                this.secret = secret;
                return this;
            }
        }

        @JsonProperty("auth-server-url")
        public String authServerUrl;

        @JsonProperty("confidential-port")
        public int confidentialPort;

        public Credentials credentials;

        @JsonProperty("public-client")
        public boolean publicClient;

        public String realm;
        public String resource;

        @JsonProperty("ssl-required")
        public String sslRequired;

        @JsonProperty("use-resource-role-mappings")
        public boolean useResourceRoleMappings;

        @JsonIgnore
        public Map<String, Object> otherAttributes;

        public OidcConfiguration toOidcConfiguration(OidcConfiguration target) {
            if (target == null) {
                target = new OidcConfiguration();
            }
            String wellKnownUrl = authServerUrl + "realms/" + realm + "/.well-known/openid-configuration";
            WellKnownOpenidConfig wellKnownConfig = getWellKnownConfig(wellKnownUrl);
            if (wellKnownConfig != null) {
                wellKnownConfig.toOidcConfiguration(target);
            } else {
                LOGGER.warn("Failed to load Well-Known OpenID config from KeyCloak at {}", wellKnownUrl);
                target.setAuthEndpoint(authServerUrl + "realms/" + realm + "/protocol/openid-connect/auth")
                        .setTokenEndpoint(authServerUrl + "realms/" + realm + "/protocol/openid-connect/token");
            }
            if (hasSecret()) {
                target.setClientSecret(getSecret());
            }
            return target.setClientId(resource);
        }

        @JsonAnySetter
        public void setAttribute(String key, Object value) {
            if (otherAttributes == null) {
                otherAttributes = new HashMap<>();
            }
            otherAttributes.put(key, value);
        }

        public String getSecret() {
            if (credentials != null) {
                return credentials.secret;
            }
            return "";
        }

        public boolean hasSecret() {
            return credentials != null && !StringHelper.isNullOrEmpty(credentials.secret);
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
    private static String findKeycloakConfig(OidcSettings authSettings) {
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
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                LOGGER.error("Failed to fetch KeyCloak config: {} {}", statusCode, keycloakConfigUrl);
                return "";
            }
            String configString = EntityUtils.toString(httpResponse.getEntity(), StringHelper.UTF8);
            LOGGER.info("Fetched Keycloak config from server. Size {}", configString.length());
            return configString;
        } catch (IOException exc) {
            LOGGER.error("Failed to read keycloak config file.", exc);
            return "";
        }
    }

    private static KeycloakConfig getKeycloakConfig(OidcSettings authSettings) {
        String keycloakConfig = findKeycloakConfig(authSettings);
        if (StringHelper.isNullOrEmpty(keycloakConfig)) {
            return null;
        }
        try {
            final KeycloakConfig kcConfig = SimpleJsonMapper.getSimpleObjectMapper().readValue(keycloakConfig, KeycloakConfig.class);
            String keycloakConfigSecret = authSettings.get(TAG_KEYCLOAK_CONFIG_SECRET);
            if (!StringHelper.isNullOrEmpty(keycloakConfigSecret)) {
                kcConfig.credentials = new KeycloakConfig.Credentials().setSecret(keycloakConfigSecret);
            }
            return kcConfig;
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to parse KeyCloak config.", ex);
            return null;
        }
    }

    /**
     * Load the contents of a Well-Known OpenID-Connect configuration file,
     * specified in the given Settings object.
     *
     * @param authSettings The settings object to fetch the config file path
     * from.
     * @return the contents of the config file.
     */
    private static String getWellKnownConfigFromServer(String configUrl) {
        LOGGER.info("Fetching Well-Known OpenID config from server: {}", configUrl);
        try (CloseableHttpClient client = HttpClients.createSystem()) {
            HttpGet httpGet = new HttpGet(configUrl);
            HttpResponse httpResponse = client.execute(httpGet);
            String configString = EntityUtils.toString(httpResponse.getEntity(), StringHelper.UTF8);
            LOGGER.info("Fetched Keycloak config from server. Size {}", configString.length());
            return configString;
        } catch (IOException exc) {
            LOGGER.error("Failed to read keycloak config file.", exc);
            return "";
        }
    }

    private static WellKnownOpenidConfig getWellKnownConfig(String configUrl) {
        String wellKnownConfigString = getWellKnownConfigFromServer(configUrl);
        try {
            return SimpleJsonMapper.getSimpleObjectMapper().readValue(wellKnownConfigString, WellKnownOpenidConfig.class);
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
        final Settings authSettings = coreSettings.getAuthSettings();
        final OidcSettings settings = new OidcSettings().setSettings(authSettings);

        String bearerTokenAudience = settings.get(OidcSettings.TAG_OIDC_BEARER_TOKEN_AUDIENCE);
        String roleAdmin = authSettings.get(TAG_AUTH_ROLE_ADMIN, CoreSettings.class);
        String wellKnownUrl = settings.get(OidcSettings.TAG_OIDC_WELL_KNOWN_CONFIGURATION_URL);
        String clientSecret = settings.get(OidcSettings.TAG_OIDC_CLIENT_SECRET);
        String usernamePath = settings.get(OidcSettings.TAG_OIDC_USERNAME_PATH);

        OidcConfiguration config = new OidcConfiguration()
                .setClientSecret(clientSecret)
                .setRoleAdmin(roleAdmin)
                .setUsernamePath(Arrays.asList(StringUtils.split(usernamePath, '/')))
                .setMaxNameLength(settings.getInt(OidcSettings.TAG_MAX_USERNAME_LENGTH))
                .setMaxPassLength(settings.getInt(OidcSettings.TAG_MAX_PASSWORD_LENGTH));
        KeycloakConfig keycloakConfig = getKeycloakConfig(settings);
        if (keycloakConfig != null) {
            keycloakConfig.toOidcConfiguration(config);
        } else if (!StringHelper.isNullOrEmpty(wellKnownUrl)) {
            String clientId = settings.get(OidcSettings.TAG_OIDC_CLIENT_ID);
            WellKnownOpenidConfig wellKnownConfig = getWellKnownConfig(wellKnownUrl);
            if (wellKnownConfig != null) {
                wellKnownConfig.toOidcConfiguration(config);
            }
            config.setClientId(clientId);
        } else {
            LOGGER.error("Failed to create configuration, no WellKnow OpenID-Connect url and no KeyCloak configuration found.");
            return null;
        }

        if (StringHelper.isNullOrEmpty(bearerTokenAudience)) {
            config.setBearerTokenAudience(config.getClientId());
        } else {
            config.setBearerTokenAudience(bearerTokenAudience);
        }

        String rolePathString = settings.get(OidcSettings.TAG_OIDC_ROLES_PATHS);
        String[] pathStrings = StringUtils.split(rolePathString, ',');
        for (String pathString : pathStrings) {
            String[] path = StringUtils.split(pathString, '/');
            config.addRolePath(Arrays.asList(path));
        }
        return config;
    }

    public static PrincipalExtended checkLogin(OidcConfiguration config, String token) {
        try {
            JWT idToken = JWTParser.parse(token);

            // The required parameters
            Issuer iss = new Issuer(config.getIssuer());
            ClientID clientID = new ClientID(config.getBearerTokenAudience());
            JWSAlgorithm jwsAlg = JWSAlgorithm.RS256;
            URL jwkSetURL = config.getJwksUrl();

            // Create validator for signed ID tokens
            IDTokenValidator validator = new IDTokenValidator(iss, clientID, jwsAlg, jwkSetURL);
            try {
                validator.validate(idToken, null);
            } catch (BadJOSEException ex) {
                LOGGER.error("Invalid token", ex);
                return null;
            } catch (JOSEException ex) {
                LOGGER.error("Failed to process token", ex);
                return null;
            }
            return Utils.extractPrincipalFromToken(config, idToken);
        } catch (RuntimeException | ParseException ex) {
            LOGGER.info("Failed to parse token", ex);
        }
        return null;
    }

    public static PrincipalExtended checkLogin(OidcConfiguration config, String username, String password) {
        UserData.validate(username, config.getMaxNameLength(), password, config.getMaxPassLength());
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(username, new Secret(password));

        // The credentials to authenticate the client at the token endpoint
        ClientID clientID = new ClientID(config.getClientId());
        Secret clientSecret = new Secret(config.getClientSecret());
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        // The request scope for the token
        Scope scope = new Scope(OIDCScopeValue.OPENID);
        // Make the token request
        TokenRequest request = new TokenRequest(config.getTokenEndpointUri(), clientAuth, passwordGrant, scope);

        TokenResponse tokenResponse;
        try {
            tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send());
        } catch (IOException | com.nimbusds.oauth2.sdk.ParseException ex) {
            LOGGER.error("Failed to parse response from token endpoint", ex);
            return null;
        }

        if (!tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            LOGGER.info("Failed to fetch token: {}", errorResponse.getErrorObject().getDescription());
            return null;
        }

        OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        // Get the ID and access token, the server may also return a refresh token
        JWT idToken = successResponse.getOIDCTokens().getIDToken();
        return Utils.extractPrincipalFromToken(config, idToken);
    }

    public static PrincipalExtended extractPrincipalFromToken(OidcConfiguration config, JWT idToken) {
        if (idToken instanceof JWSObject jwso && jwso.getPayload() != null) {
            Map<String, Object> payload = jwso.getPayload().toJSONObject();

            final String userName = Objects.toString(CollectionsHelper.getFrom(payload, config.getUsernamePath()), null);
            if (StringHelper.isNullOrEmpty(userName)) {
                LOGGER.warn("No username found at path {}", config.getUsernamePath());
                return null;
            }

            final Set<String> roles = new HashSet<>();
            for (List<String> path : config.getRolePaths()) {
                Object rolesObj = CollectionsHelper.getFrom(payload, path);
                if (rolesObj instanceof Collection rolesCol) {
                    roles.addAll(rolesCol);
                }
            }
            return new PrincipalExtended(userName, roles.contains(config.getRoleAdmin()), roles);
        }
        return null;
    }

    public static void getUserInfo(OidcConfiguration config, AccessToken token) {
        URI userInfoEndpoint = config.getUserinfoEndpointUri();

        // Make the request
        HTTPResponse httpResponse;
        try {
            httpResponse = new UserInfoRequest(userInfoEndpoint, token)
                    .toHTTPRequest()
                    .send();
        } catch (IOException ex) {
            LOGGER.error("Failed to fetch userinfo", ex);
            return;
        }

        // Parse the response
        UserInfoResponse userInfoResponse;
        try {
            userInfoResponse = UserInfoResponse.parse(httpResponse);
        } catch (com.nimbusds.oauth2.sdk.ParseException ex) {
            LOGGER.error("Failed to parse userinfo", ex);
            return;
        }

        if (!userInfoResponse.indicatesSuccess()) {
            // The request failed, e.g. due to invalid or expired token
            LOGGER.debug("Failed to fetch user info: {} - {}",
                    userInfoResponse.toErrorResponse().getErrorObject().getCode(),
                    userInfoResponse.toErrorResponse().getErrorObject().getDescription());
            return;
        }

        // Extract the claims
        UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
        System.out.println("Subject: " + userInfo.getSubject());
        System.out.println("Email: " + userInfo.getEmailAddress());
        System.out.println("Name: " + userInfo.getName());
    }
}
