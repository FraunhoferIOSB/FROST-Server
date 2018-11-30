/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.auth.keycloak;

import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.TAG_CORE_SETTINGS;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import de.fraunhofer.iosb.ilt.sta.util.AuthUtils;
import de.fraunhofer.iosb.ilt.sta.util.AuthUtils.Role;
import de.fraunhofer.iosb.ilt.sta.util.HttpMethod;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.AuthenticatedActionsHandler;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.NodesRegistrationManagement;
import org.keycloak.adapters.PreAuthActionsHandler;
import org.keycloak.adapters.servlet.FilterRequestAuthenticator;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore;
import org.keycloak.adapters.servlet.OIDCServletHttpFacade;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.InMemorySessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.UserSessionManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class KeycloakFilter implements Filter {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakFilter.class);

    private Map<String, Utils.MethodRoleMapper> roleMappersByPath = new HashMap<>();

    private Map<Role, String> roleMappings;
    private AdapterDeploymentContext deploymentContext;
    private NodesRegistrationManagement nodesRegistrationManagement;
    private UserSessionManagement sessionCleaner;
    private final SessionIdMapper idMapper = new InMemorySessionIdMapper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext context = filterConfig.getServletContext();
        Object attribute = context.getAttribute(TAG_CORE_SETTINGS);
        if (!(attribute instanceof CoreSettings)) {
            throw new IllegalArgumentException("Could not load core settings.");
        }
        CoreSettings coreSettings = (CoreSettings) attribute;
        Settings authSettings = coreSettings.getAuthSettings();
        roleMappings = AuthUtils.loadRoleMapping(authSettings);

        final boolean anonRead = authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class);
        roleMappersByPath.put("/Data", method -> Role.ADMIN);
        roleMappersByPath.put("/keyc", method -> Role.ADMIN);
        roleMappersByPath.put("/v1.0",
                (HttpMethod method) -> {
                    switch (method) {
                        case DELETE:
                            return Role.DELETE;

                        case GET:
                            if (anonRead) {
                                return Role.NONE;
                            }
                            return Role.READ;

                        case HEAD:
                            return Role.NONE;

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
                });
        try {
            deploymentContext = new AdapterDeploymentContext(Utils.resolveDeployment(coreSettings));
        } catch (RuntimeException exc) {
            LOGGER.error("Failed to initialise Keycloak. There is a problem with the configuration.");
            throw new IllegalArgumentException("Exception initialising keycloak.", exc);
        }
        nodesRegistrationManagement = new NodesRegistrationManagement();
        sessionCleaner = new UserSessionManagement() {
            @Override
            public void logoutAll() {
                idMapper.clear();
            }

            @Override
            public void logoutHttpSessions(List<String> ids) {
                LOGGER.debug("Logging out Http Sessions");
                for (String id : ids) {
                    LOGGER.debug("Removed session: {}", id);
                    idMapper.removeSession(id);
                }

            }
        };
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOGGER.trace("FROST Keycloak Filter - Filtering request...");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        Role requiredRole = findRequiredRoleForRequest(httpRequest);
        if (requiredRole == Role.NONE) {
            chain.doFilter(request, response);
            return;
        }
        if (requiredRole == Role.ERROR) {
            throwHttpError(400, httpResponse);
            return;
        }

        OIDCServletHttpFacade facade = new OIDCServletHttpFacade(httpRequest, httpResponse);
        KeycloakDeployment deployment = deploymentContext.resolveDeployment(facade);
        if (deployment == null || !deployment.isConfigured()) {
            LOGGER.error("Deployment not found");
            throwHttpError(403, httpResponse);
            return;
        }

        PreAuthActionsHandler preActions = new PreAuthActionsHandler(sessionCleaner, deploymentContext, facade);

        if (preActions.handleRequest()) {
            LOGGER.debug("Request handled by preActions.");
            return;
        }

        nodesRegistrationManagement.tryRegister(deployment);
        OIDCFilterSessionStore tokenStore = new OIDCFilterSessionStore(httpRequest, facade, 100000, deployment, idMapper);
        tokenStore.checkCurrentToken();

        FilterRequestAuthenticator authenticator = new FilterRequestAuthenticator(deployment, tokenStore, facade, httpRequest, 8443);
        AuthOutcome outcome = authenticator.authenticate();
        if (outcome == AuthOutcome.AUTHENTICATED) {
            LOGGER.debug("User is authenticated...");
            if (facade.isEnded()) {
                LOGGER.debug("Facade is ended.");
                return;
            }
            AuthenticatedActionsHandler actions = new AuthenticatedActionsHandler(deployment, facade);
            if (actions.handledRequest()) {
                LOGGER.debug("Request handled by authentication actions.");
                return;
            } else {
                HttpServletRequestWrapper wrapper = tokenStore.buildWrapper();
                if (wrapper.isUserInRole(roleMappings.get(requiredRole))) {
                    LOGGER.debug("User has correct role.");
                    chain.doFilter(wrapper, response);
                    return;
                }
            }
        }
        AuthChallenge challenge = authenticator.getChallenge();
        if (challenge != null) {
            LOGGER.debug("Challenge.");
            challenge.challenge(facade);
            return;
        }
        LOGGER.debug("User is not allowed.");
        throwHttpError(403, httpResponse);
    }

    @Override
    public void destroy() {
        // Nothing to clean up.
    }

    private Role findRequiredRoleForRequest(HttpServletRequest httpRequest) {
        final HttpMethod method;
        try {
            method = HttpMethod.valueOf(httpRequest.getMethod().toUpperCase());
        } catch (IllegalArgumentException exc) {
            LOGGER.debug("Rejecting request: Unknown method: {}.", httpRequest.getMethod());
            return Role.ERROR;
        }
        String servletPath = httpRequest.getServletPath();
        Utils.MethodRoleMapper mapper = roleMappersByPath.get(servletPath.substring(0, 5));
        if (mapper == null) {
            LOGGER.error("No role mapper for servletPath: {}", servletPath);
            return Role.ERROR;
        }
        Role requiredRole = mapper.findRole(method);
        LOGGER.debug("Role {} required for request {} {}", requiredRole, httpRequest.getMethod(), httpRequest.getRequestURI());
        return requiredRole;
    }

    private void throwHttpError(int code, HttpServletResponse response) {
        try {
            response.sendError(code);
        } catch (IOException exc) {
            LOGGER.error("Exception sending back error.", exc);
        }
    }

    private static String getInitParamWithDefault(FilterConfig filterConfig, String paramName, String defValue) {
        String value = filterConfig.getInitParameter(paramName);
        if (value == null) {
            LOGGER.info("Filter setting {}, using default value: {}", paramName, defValue);
            return defValue;
        }
        LOGGER.info("Filter setting {}, set to value: {}", paramName, value);
        return value;
    }

}
