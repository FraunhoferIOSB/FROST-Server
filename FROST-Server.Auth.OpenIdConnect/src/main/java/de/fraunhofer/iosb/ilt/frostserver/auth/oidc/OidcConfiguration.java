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
import java.net.URI;
import java.net.URISyntaxException;
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

    /**
     * The name of the client in the auth server.
     */
    private String clientId;

    /**
     * The secret of the client in the auth server.
     */
    private String clientSecret;

    private String tokenEndpoint;
    private String userinfoEndpoint;

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

    public static URI createUri(String uriString) {
        try {
            return new URI(uriString);
        } catch (URISyntaxException ex) {
            LOGGER.error("Not a valid URI: {}", uriString);
            return null;
        }
    }
}
