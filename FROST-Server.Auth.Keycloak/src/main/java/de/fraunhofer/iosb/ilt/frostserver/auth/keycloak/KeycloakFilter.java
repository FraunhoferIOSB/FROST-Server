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
package de.fraunhofer.iosb.ilt.frostserver.auth.keycloak;

import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_REGISTER_USER_LOCALLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTHENTICATE_ONLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;

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
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.keycloak.adapters.spi.KeycloakAccount;
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

    private final Map<String, Utils.MethodRoleMapper> roleMappersByPath = new HashMap<>();

    private Map<Role, String> roleMappings;
    private String roleAdmin;
    private boolean authenticateOnly;
    private boolean registerUserLocally;
    private DatabaseHandler databaseHandler;

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
        roleAdmin = authSettings.get(TAG_AUTH_ROLE_ADMIN, CoreSettings.class);
        authenticateOnly = authSettings.getBoolean(TAG_AUTHENTICATE_ONLY, CoreSettings.class);
        registerUserLocally = authSettings.getBoolean(TAG_REGISTER_USER_LOCALLY, KeycloakSettings.class);
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
        if (!authenticateOnly && requiredRole == Role.NONE) {
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
                final KeycloakAccount account = findKeycloakAccount(httpRequest);
                final Principal principalBasic = account.getPrincipal();
                final String userName = principalBasic.getName();
                final Set<String> roles = account.getRoles();
                final PrincipalExtended pe = new PrincipalExtended(userName, roles.contains(roleAdmin), roles);
                if (registerUserLocally) {
                    databaseHandler.enureUserInUsertable(userName, roles);
                }
                if (authenticateOnly) {
                    chain.doFilter(new RequestWrapper(httpRequest, pe), response);
                    return;
                }
                if (roles.contains(roleMappings.get(requiredRole))) {
                    LOGGER.debug("User has correct role.");
                    chain.doFilter(new RequestWrapper(httpRequest, pe), response);
                    return;
                } else {
                    LOGGER.debug("User is not allowed.");
                    throwHttpError(403, httpResponse);
                    return;
                }
            }
        }

        if (requiredRole == Role.NONE) {
            chain.doFilter(request, response);
            return;
        }

        AuthChallenge challenge = authenticator.getChallenge();
        if (challenge != null) {
            LOGGER.debug("Challenge.");
            try {
                challenge.challenge(facade);
                return;
            } catch (IllegalStateException ex) {
                LOGGER.debug("Challenge failed.", ex);
            }
        }
        LOGGER.debug("User is not allowed.");
        throwHttpError(401, httpResponse);
    }

    private KeycloakAccount findKeycloakAccount(HttpServletRequest httpRequest) {
        final HttpSession session = httpRequest.getSession(false);
        KeycloakAccount account = null;
        if (session != null) {
            account = (KeycloakAccount) session.getAttribute(KeycloakAccount.class.getName());
            if (account == null) {
                account = (KeycloakAccount) httpRequest.getAttribute(KeycloakAccount.class.getName());
            }
        }
        if (account == null) {
            account = (KeycloakAccount) httpRequest.getAttribute(KeycloakAccount.class.getName());
        }
        return account;
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
        Utils.MethodRoleMapper mapper = roleMappersByPath.get(pathInfo.substring(0, 5));
        if (mapper == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("No role mapper for servletPath: {}", StringHelper.cleanForLogging(servletPath));
            }
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

}
