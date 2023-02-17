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
package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

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
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_AUTH_REALM_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_DELETE;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_GET;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_PATCH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_POST;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_PUT;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;

import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigUtils;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
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
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class BasicAuthFilter implements Filter {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_REQUIRED_HEADER = "WWW-Authenticate";
    private static final String BASIC_PREFIX = "Basic ";

    private final Map<HttpMethod, AuthChecker> methodCheckers = new EnumMap<>(HttpMethod.class);

    private DatabaseHandler databaseHandler;

    private String authHeaderValue;

    private String roleAdmin;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Turning on Basic authentication.");
        roleAdmin = getInitParamWithDefault(filterConfig, TAG_ROLE_ADMIN, BasicAuthProvider.class);
        String roleGet = getInitParamWithDefault(filterConfig, TAG_ROLE_GET, BasicAuthProvider.class);
        String rolePost = getInitParamWithDefault(filterConfig, TAG_ROLE_POST, BasicAuthProvider.class);
        String rolePatch = getInitParamWithDefault(filterConfig, TAG_ROLE_PATCH, BasicAuthProvider.class);
        String rolePut = getInitParamWithDefault(filterConfig, TAG_ROLE_PUT, BasicAuthProvider.class);
        String roleDelete = getInitParamWithDefault(filterConfig, TAG_ROLE_DELETE, BasicAuthProvider.class);
        String anonRead = getInitParamWithDefault(filterConfig, TAG_AUTH_ALLOW_ANON_READ, "F");

        ServletContext context = filterConfig.getServletContext();
        Object attribute = context.getAttribute(TAG_CORE_SETTINGS);
        if (!(attribute instanceof CoreSettings)) {
            throw new IllegalArgumentException("Could not load core settings.");
        }
        CoreSettings coreSettings = (CoreSettings) attribute;
        Settings authSettings = coreSettings.getAuthSettings();

        databaseHandler = DatabaseHandler.getInstance(coreSettings);
        String realmName = authSettings.get(TAG_AUTH_REALM_NAME, BasicAuthProvider.class);
        authHeaderValue = "Basic realm=\"" + realmName + "\", charset=\"UTF-8\"";

        final AuthChecker allAllowed = (userData, response) -> true;
        methodCheckers.put(HttpMethod.OPTIONS, allAllowed);
        methodCheckers.put(HttpMethod.HEAD, allAllowed);

        if ("T".equals(anonRead)) {
            methodCheckers.put(HttpMethod.GET, allAllowed);
        } else {
            methodCheckers.put(HttpMethod.GET, (userData, response) -> requireRole(roleGet, userData, response));
        }

        methodCheckers.put(HttpMethod.POST, (userData, response) -> requireRole(rolePost, userData, response));
        methodCheckers.put(HttpMethod.PATCH, (userData, response) -> requireRole(rolePatch, userData, response));
        methodCheckers.put(HttpMethod.PUT, (userData, response) -> requireRole(rolePut, userData, response));
        methodCheckers.put(HttpMethod.DELETE, (userData, response) -> requireRole(roleDelete, userData, response));
    }

    private UserData findCredentials(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            LOGGER.debug("No basic auth header.");
            return new UserData(null, null);
        }

        String userPassBase64 = authHeader.substring(BASIC_PREFIX.length());
        String userPassDecoded = new String(Base64.getDecoder().decode(userPassBase64), StringHelper.UTF8);
        if (!userPassDecoded.contains(":")) {
            LOGGER.debug("No username:password in basic auth header.");
            return new UserData(null, null);
        }

        String[] split = userPassDecoded.split(":", 2);
        final UserData userData = new UserData(split[0], split[1]);
        if (databaseHandler.isValidUser(userData)) {
            return userData;
        } else {
            return new UserData(null, null);
        }
    }

    private boolean requireRole(String roleName, UserData userData, HttpServletResponse response) {
        if (userData.isEmpty()) {
            LOGGER.debug("Rejecting request: No user data.");
            throwAuthRequired(response);
            return false;
        }

        if (!databaseHandler.userHasRole(userData.userName, roleName)) {
            LOGGER.debug("Rejecting request: User {} does not have role {}.", userData.userName, roleName);
            throwAuthRequired(response);
            return false;
        }
        LOGGER.debug("Accepting request: User {} has role {}.", userData.userName, roleName);
        return true;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) resp;

        final HttpMethod method;
        try {
            method = HttpMethod.valueOf(request.getMethod().toUpperCase());
        } catch (IllegalArgumentException exc) {
            LOGGER.debug("Rejecting request: Unknown method: {}.", request.getMethod());
            LOGGER.trace("", exc);
            throwAuthRequired(response);
            return;
        }

        UserData userData = findCredentials(request);

        AuthChecker checker = methodCheckers.get(method);
        if (checker == null) {
            LOGGER.debug("Rejecting request: No checker for method: {}.", request.getMethod());
            throwAuthRequired(response);
            return;
        }

        if (checker.isAllowed(userData, response)) {
            boolean admin = databaseHandler.userHasRole(userData.userName, roleAdmin);
            chain.doFilter(new RequestWrapper(request, new PrincipalExtended(userData.userName, admin, userData.roles)), response);
        }
    }

    @Override
    public void destroy() {
        // Nothing to destroy.
    }

    private void throwAuthRequired(HttpServletResponse response) {
        response.addHeader(AUTHORIZATION_REQUIRED_HEADER, authHeaderValue);
        try {
            response.sendError(401);
        } catch (IOException exc) {
            LOGGER.error("Exception sending back error.", exc);
        }
    }

    private static String getInitParamWithDefault(FilterConfig filterConfig, String paramName, Class<? extends ConfigDefaults> defaultsProvider) {
        return getInitParamWithDefault(filterConfig, paramName, ConfigUtils.getDefaultValue(defaultsProvider, paramName));
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

    /**
     * An interface for helper classes to check requests.
     */
    private static interface AuthChecker {

        /**
         * Check if the request is allowed.
         *
         * @param userData The request to check.
         * @param response The response to use for sending errors back.
         * @return False if the request is not allowed.
         */
        public boolean isAllowed(UserData userData, HttpServletResponse response);
    }

    public static class UserData {

        public final String userName;
        public final String userPass;
        public final List<String> roles = new ArrayList<>();

        public UserData(String userName, String userPass) {
            this.userName = userName;
            this.userPass = userPass;
        }

        public String getUserName() {
            return userName;
        }

        public boolean isEmpty() {
            return userName == null;
        }
    }

}
