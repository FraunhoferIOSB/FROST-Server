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

import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_REGISTER_USER_LOCALLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTHENTICATE_ONLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCClaimsRequest;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils.Role;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Filter implementing OpenID-Connect for FROST.
 *
 * Client: FROST-Server
 *
 * User: External user/software accessing FROST-Server
 *
 * Resource Owner Password Credentials Grant: Direct username/password provided
 * by the user to the client.
 *
 * Authorization Code Flow: Ping-pong between FROST and KeyCloak.
 *
 *
 *
 */
public class OidcFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcFilter.class);
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_START_BEARER = "Bearer ";
    private static final String SESSION_KEY_STATE = "OidcFilter.state";

    private final Map<String, Utils.MethodRoleMapper> roleMappersByPath = new HashMap<>();

    private Map<Role, String> roleMappings;
    private boolean authenticateOnly;
    private boolean registerUserLocally;
    private DatabaseHandler databaseHandler;
    private OidcConfiguration config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        CoreSettings coreSettings = getCoreSettings(filterConfig);
        Settings authSettings = coreSettings.getAuthSettings();
        roleMappings = AuthUtils.loadRoleMapping(authSettings);
        authenticateOnly = authSettings.getBoolean(TAG_AUTHENTICATE_ONLY, CoreSettings.class);
        registerUserLocally = authSettings.getBoolean(TAG_REGISTER_USER_LOCALLY, OidcSettings.class);
        if (registerUserLocally) {
            databaseHandler = DatabaseHandler.getInstance(coreSettings);
        }

        final boolean anonRead = authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class);
        roleMappersByPath.put("/Data", method -> Role.ADMIN);
        roleMappersByPath.put("/keyc", method -> Role.ADMIN);
        final Utils.MethodRoleMapper roleMapperSta = (HttpMethod method) -> {
            switch (method) {
                case DELETE:
                    return Role.DELETE;

                case GET:
                case HEAD:
                    if (anonRead) {
                        return Role.NONE;
                    }
                    return Role.READ;

                case PATCH:
                    return Role.UPDATE;

                case POST:
                    return Role.CREATE;

                case PUT:
                    return Role.UPDATE;

                case OPTIONS:
                    return Role.NONE;

                default:
                    LOGGER.error("Unknown method: {}", method);
                    return Role.ERROR;
            }
        };
        for (String version : coreSettings.getPluginManager().getVersions().keySet()) {
            roleMappersByPath.put("/" + version, roleMapperSta);
        }
        config = Utils.createConfiguration(coreSettings);
        if (config == null) {
            LOGGER.error("Failed to load OpenID-Connect configuration.");
        }
    }

    private CoreSettings getCoreSettings(FilterConfig filterConfig) {
        ServletContext context = filterConfig.getServletContext();
        Object attribute = context.getAttribute(TAG_CORE_SETTINGS);
        if (attribute instanceof CoreSettings cs) {
            return cs;
        } else {
            throw new IllegalArgumentException("Could not load core settings.");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOGGER.trace("FROST OpenID-Connect Filter - Filtering request...");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (config == null) {
            LOGGER.error("No configuration.");
            throwHttpError(500, httpResponse);
        }
        String pathInfo = findPathInfo(httpRequest);
        Role requiredRole = findRequiredRoleForRequest(httpRequest, pathInfo);
        if (!authenticateOnly && requiredRole == Role.NONE) {
            chain.doFilter(request, response);
            return;
        }
        if (requiredRole == Role.ERROR) {
            throwHttpError(400, httpResponse);
            return;
        }

        PrincipalExtended pe = findUserAccount(httpRequest);
        if (pe == null) {
            // Check for bearer token
            String authHeader = httpRequest.getHeader(HEADER_AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith(HEADER_START_BEARER)) {
                String tokenString = authHeader.strip().substring(HEADER_START_BEARER.length());
                pe = Utils.checkLogin(config, tokenString);
                if (pe == null) {
                    LOGGER.debug("Failed to extract token from header.");
                    throwHttpError(httpResponse, 403, "Authentication failed");
                    return;
                } else {
                    if (registerUserLocally) {
                        databaseHandler.enureUserInUsertable(pe.getName(), pe.getRoles());
                    }
                    httpRequest.getSession(true).setAttribute(PrincipalExtended.class.getName(), pe);
                    // TODO: Add some caching machanism so the full check is not done every time.
                }

            } else if (hasStateAndCode(httpRequest)) {
                // We have a redirect back from the OIDC Server.
                final HttpSession session = httpRequest.getSession(false);
                if (session == null) {
                    throwHttpError(httpResponse, 400, "Session not found.");
                    return;
                }
                URI requestUri = createFullRequestUri(httpRequest);
                AuthenticationResponse authResponse;
                try {
                    authResponse = AuthenticationResponseParser.parse(requestUri);
                } catch (ParseException ex) {
                    LOGGER.error("Failed to parse callback", ex);
                    throwHttpError(500, httpResponse);
                    return;
                }
                if (response instanceof AuthenticationErrorResponse) {
                    // The OpenID provider returned an error
                    throwHttpError(httpResponse, 403, "Authentication failed");
                    return;
                }
                if (!authResponse.getState().toString().equals(session.getAttribute(SESSION_KEY_STATE))) {
                    throwHttpError(httpResponse, 400, "Session state not correct.");
                    return;
                }
                AuthorizationCode code = authResponse.toSuccessResponse().getAuthorizationCode();

                // Construct the code grant from the code obtained from the authz endpoint
                // and the original callback URI used at the authz endpoint
                URI origUri = recreateOriginalUri(httpRequest);
                LOGGER.info("Original URI: {}", origUri);
                AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, origUri);

                // The credentials to authenticate the client at the token endpoint
                ClientID clientID = new ClientID(config.getClientId());
                TokenRequest tokenRequest;
                if (config.hasClientSecret()) {
                    Secret clientSecret = new Secret(config.getClientSecret());
                    ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
                    // Make the token request
                    tokenRequest = new TokenRequest(config.getTokenEndpointUri(), clientAuth, codeGrant, new Scope("roles"));
                } else {
                    tokenRequest = new TokenRequest(config.getTokenEndpointUri(), clientID, codeGrant, new Scope("roles"));
                }

                TokenResponse tokenResponse;
                try {
                    tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());
                } catch (ParseException ex) {
                    LOGGER.info("Failed to fetch token", ex);
                    throwHttpError(500, httpResponse);
                    return;
                }

                if (!tokenResponse.indicatesSuccess()) {
                    // We got an error response...
                    TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
                    LOGGER.info("Failed to fetch token: {}", errorResponse.getErrorObject().getDescription());
                    // The OpenID provider returned an error
                    throwHttpError(httpResponse, 403, "Authentication failed");
                    return;
                }

                OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
                // Get the ID and access token, the server may also return a refresh token
                JWT idToken = successResponse.getOIDCTokens().getIDToken();
                AccessToken accessToken = successResponse.getOIDCTokens().getAccessToken();
                pe = Utils.extractPrincipalFromToken(config, idToken);
                if (pe == null) {
                    LOGGER.debug("Failed to extract token from response.");
                    throwHttpError(httpResponse, 403, "Authentication failed");
                    return;
                } else {
                    if (registerUserLocally) {
                        databaseHandler.enureUserInUsertable(pe.getName(), pe.getRoles());
                    }
                    httpRequest.getSession(true).setAttribute(PrincipalExtended.class.getName(), pe);
                    httpResponse.sendRedirect(origUri.toString());
                    return;
                }

            } else {
                // No bearer token, no login-in-progress: redirect to OIDC Server
                final HttpSession session = httpRequest.getSession(true);
                ClientID clientID = new ClientID(config.getClientId());
                // Generate random state string to securely pair the callback to this request
                State state = new State();
                // Store the state in the session.
                session.setAttribute(SESSION_KEY_STATE, state.toString());
                // Get the callback URL, wich is the full request url in our case.
                URI requestUri = createFullRequestUri(httpRequest);
                // Generate nonce for the ID token
                Nonce nonce = new Nonce();
                OIDCClaimsRequest claims = new OIDCClaimsRequest()
                        .withIDTokenClaimsRequest(
                                new ClaimsSetRequest()
                                        .add("roles"));
                // Compose the OpenID authentication request (for the code flow)
                AuthenticationRequest authRequest = new AuthenticationRequest.Builder(new ResponseType("code"), new Scope("openid", "roles"), clientID, requestUri)
                        .endpointURI(config.getAuthEndpointUri())
                        .state(state)
                        .nonce(nonce)
                        .claims(claims)
                        .build();
                httpResponse.sendRedirect(authRequest.toURI().toString());
                return;
            }
        }

        if (authenticateOnly) {
            chain.doFilter(new RequestWrapper(httpRequest, pe), response);
            return;
        }
        if (pe.getRoles().contains(roleMappings.get(requiredRole))) {
            LOGGER.debug("User has correct role.");
            chain.doFilter(new RequestWrapper(httpRequest, pe), response);
            return;
        }
        LOGGER.debug("User is not allowed.");
        throwHttpError(403, httpResponse);
    }

    private boolean hasStateAndCode(HttpServletRequest httpRequest) {
        return !StringHelper.isNullOrEmpty(httpRequest.getParameter("state")) && !StringHelper.isNullOrEmpty(httpRequest.getParameter("code"));
    }

    private PrincipalExtended findUserAccount(HttpServletRequest httpRequest) {
        final HttpSession session = httpRequest.getSession(false);
        PrincipalExtended principal = null;
        if (session != null) {
            principal = (PrincipalExtended) session.getAttribute(PrincipalExtended.class.getName());
        }
        return principal;
    }

    @Override
    public void destroy() {
        // Nothing to clean up.
    }

    private URI recreateOriginalUri(HttpServletRequest httpRequest) {
        StringBuilder requestURL = new StringBuilder(httpRequest.getRequestURL());
        String queryString = httpRequest.getQueryString();
        if (!StringHelper.isNullOrEmpty(queryString)) {
            String[] split = StringUtils.split(queryString, '&');
            char join = '?';
            for (String part : split) {
                if (!Strings.CS.startsWithAny(part, "state=", "session_state=", "iss=", "code=")) {
                    requestURL.append(join).append(part);
                    join = '&';
                }
            }
        }
        try {
            return new URI(requestURL.toString());
        } catch (URISyntaxException ex) {
            LOGGER.error("Failed to build callback URI: {}", requestURL.toString());
            return null;
        }
    }

    private URI createFullRequestUri(HttpServletRequest httpRequest) {
        StringBuilder requestURL = new StringBuilder(httpRequest.getRequestURL());
        String queryString = httpRequest.getQueryString();
        if (!StringHelper.isNullOrEmpty(queryString)) {
            requestURL.append('?').append(queryString);
        }
        try {
            return new URI(requestURL.toString());
        } catch (URISyntaxException ex) {
            LOGGER.error("Failed to build callback URI: {}", requestURL.toString());
            return null;
        }
    }

    private String findPathInfo(HttpServletRequest httpRequest) throws IllegalArgumentException {
        final String requestURI = httpRequest.getRequestURI();
        final String contextPath = httpRequest.getContextPath();
        final String servletPath = httpRequest.getServletPath();
        final String preVersionPath = contextPath + servletPath;
        String pathInfo;
        if (requestURI.startsWith(preVersionPath)) {
            pathInfo = StringHelper.urlDecode(requestURI.substring(preVersionPath.length()));
        } else if (!servletPath.isEmpty()) {
            pathInfo = servletPath;
        } else {
            throw new IllegalArgumentException("Path oddness!");
        }
        if (pathInfo.isEmpty()) {
            pathInfo = servletPath;
        }
        LOGGER.trace("\nrequestURI: {}\ncontextPath: {}\nservletPath: {}\nfullPath: {}\npathInfo: {}", requestURI, contextPath, servletPath, preVersionPath, pathInfo);
        return pathInfo;
    }

    private Role findRequiredRoleForRequest(HttpServletRequest httpRequest, String pathInfo) {
        final HttpMethod method;
        try {
            method = HttpMethod.valueOf(httpRequest.getMethod().toUpperCase());
        } catch (IllegalArgumentException exc) {
            LOGGER.debug("Rejecting request: Unknown method: {}.", httpRequest.getMethod());
            return Role.ERROR;
        }

        Utils.MethodRoleMapper mapper = roleMappersByPath.get(pathInfo.substring(0, 5));
        if (mapper == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("No role mapper for servletPath: {}", StringHelper.cleanForLogging(pathInfo));
            }
            return Role.ERROR;
        }
        Role requiredRole = mapper.findRole(method);
        LOGGER.debug("Role {} required for request {} {}", requiredRole, httpRequest.getMethod(), httpRequest.getRequestURI());
        return requiredRole;
    }

    private void throwHttpError(int code, HttpServletResponse response) {
        throwHttpError(response, code, null);
    }

    private void throwHttpError(HttpServletResponse response, int code, String msg) {
        try {
            if (StringHelper.isNullOrEmpty(msg)) {
                response.sendError(code);
            } else {
                response.sendError(code, msg);
            }
        } catch (IOException exc) {
            LOGGER.error("Exception sending back error.", exc);
        }
    }

}
