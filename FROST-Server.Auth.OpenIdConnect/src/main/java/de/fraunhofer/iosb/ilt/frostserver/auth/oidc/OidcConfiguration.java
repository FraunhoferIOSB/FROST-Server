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

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserData;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The final configuration of the OID-C module.
 */
public class OidcConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcConfiguration.class.getName());

    /**
     * The URL on the auth server to redirect users to.
     * http://192.168.178.46:8180/realms/FROST-Test/protocol/openid-connect/auth
     */
    private String authEndpoint;

    private String bearerTokenAudience;

    /**
     * The name of the client in the auth server.
     */
    private String clientId;

    /**
     * The secret of the client in the auth server.
     */
    private String clientSecret;

    private String issuer;
    private String jwksUri;
    private String roleAdmin;
    private List<List<String>> rolePaths = new ArrayList<>();
    private int maxPassLength = UserData.MAX_PASSWORD_LENGTH;
    private int maxNameLength = UserData.MAX_USERNAME_LENGTH;

    private String tokenEndpoint;
    private String userinfoEndpoint;
    private List<String> usernamePath;

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public URI getAuthEndpointUri() {
        return createUri(authEndpoint);
    }

    public OidcConfiguration setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
        return this;
    }

    public String getBearerTokenAudience() {
        return bearerTokenAudience;
    }

    public OidcConfiguration setBearerTokenAudience(String bearerTokenAudience) {
        this.bearerTokenAudience = bearerTokenAudience;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public OidcConfiguration setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public boolean hasClientSecret() {
        return !StringHelper.isNullOrEmpty(clientSecret);
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public OidcConfiguration setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getIssuer() {
        return issuer;
    }

    public OidcConfiguration setIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public URL getJwksUrl() {
        try {
            return new URI(jwksUri).toURL();
        } catch (URISyntaxException | MalformedURLException ex) {
            LOGGER.error("Failed to create jwks URL", ex);
        }
        return null;
    }

    public OidcConfiguration setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
        return this;
    }

    public int getMaxNameLength() {
        return maxNameLength;
    }

    public OidcConfiguration setMaxNameLength(int maxNameLength) {
        this.maxNameLength = maxNameLength;
        return this;
    }

    public int getMaxPassLength() {
        return maxPassLength;
    }

    public OidcConfiguration setMaxPassLength(int maxPassLength) {
        this.maxPassLength = maxPassLength;
        return this;
    }

    public String getRoleAdmin() {
        return roleAdmin;
    }

    public OidcConfiguration setRoleAdmin(String roleAdmin) {
        this.roleAdmin = roleAdmin;
        return this;
    }

    public List<List<String>> getRolePaths() {
        return rolePaths;
    }

    public OidcConfiguration addRolePath(List<String> rolePath) {
        this.rolePaths.add(rolePath);
        return this;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public URI getTokenEndpointUri() {
        return createUri(tokenEndpoint);
    }

    public OidcConfiguration setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
        return this;
    }

    public String getUserinfoEndpoint() {
        return userinfoEndpoint;
    }

    public URI getUserinfoEndpointUri() {
        return createUri(userinfoEndpoint);
    }

    public OidcConfiguration setUserinfoEndpoint(String userinfoEndpoint) {
        this.userinfoEndpoint = userinfoEndpoint;
        return this;
    }

    public List<String> getUsernamePath() {
        return usernamePath;
    }

    public OidcConfiguration setUsernamePath(List<String> usernamePath) {
        this.usernamePath = usernamePath;
        return this;
    }

    public static URI createUri(String uriString) {
        try {
            return new URI(uriString);
        } catch (URISyntaxException ex) {
            LOGGER.error("Not a valid URI: {}", uriString);
            return null;
        }
    }

}
