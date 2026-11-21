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
package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_AUTH_REALM_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_MAX_AUTH_HEADER_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_MAX_PASSWORD_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_MAX_USERNAME_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_PASSWORD_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_USERNAME_LENGTH;

import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigUtils;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserData;
import de.fraunhofer.iosb.ilt.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.settings.Settings;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Tomcat filter for Basic Authentication.
 */
public class BasicAuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_REQUIRED_HEADER = "WWW-Authenticate";
    private static final String BASIC_PREFIX = "Basic ";
    private static final UserData USER_DATA_NO_USER = new UserData(null, null);

    private boolean allowAnonymous;
    private int maxPassLength = MAX_PASSWORD_LENGTH;
    private int maxNameLength = MAX_USERNAME_LENGTH;
    private int maxAuthHeaderLength = MAX_PASSWORD_LENGTH;

    private DatabaseHandler databaseHandler;
    private String authHeaderValue;
    private String roleAdmin;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Turning on Basic authentication.");
        roleAdmin = getInitParamWithDefault(filterConfig, TAG_AUTH_ROLE_ADMIN, CoreSettings.class);

        ServletContext context = filterConfig.getServletContext();
        Object attribute = context.getAttribute(TAG_CORE_SETTINGS);
        if (!(attribute instanceof CoreSettings)) {
            throw new IllegalArgumentException("Could not load core settings.");
        }
        CoreSettings coreSettings = (CoreSettings) attribute;
        Settings authSettings = coreSettings.getAuthSettings();

        allowAnonymous = authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class);
        roleAdmin = authSettings.get(TAG_AUTH_ROLE_ADMIN, CoreSettings.class);

        databaseHandler = DatabaseHandler.getInstance(coreSettings);
        String realmName = authSettings.get(TAG_AUTH_REALM_NAME, BasicAuthProvider.class);
        authHeaderValue = "Basic realm=\"" + realmName + "\", charset=\"UTF-8\"";

        maxPassLength = authSettings.getInt(TAG_MAX_PASSWORD_LENGTH, BasicAuthProvider.class);
        maxNameLength = authSettings.getInt(TAG_MAX_USERNAME_LENGTH, BasicAuthProvider.class);
        maxAuthHeaderLength = authSettings.getInt(TAG_MAX_AUTH_HEADER_LENGTH, BasicAuthProvider.class);
    }

    private UserData findCredentials(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            LOGGER.debug("No basic auth header.");
            return USER_DATA_NO_USER;
        }
        if (authHeader.length() > maxAuthHeaderLength) {
            LOGGER.debug("Basic auth header too long.");
            return USER_DATA_NO_USER;
        }

        String userPassBase64 = authHeader.substring(BASIC_PREFIX.length());
        String userPassDecoded = new String(Base64.getDecoder().decode(userPassBase64), StringHelper.UTF8);
        if (!userPassDecoded.contains(":")) {
            LOGGER.debug("No username:password in basic auth header.");
            return USER_DATA_NO_USER;
        }
        String[] split = StringUtils.split(userPassDecoded, ":", 2);
        if (split.length != 2) {
            LOGGER.debug("Empty username or password in basic auth header.");
            return USER_DATA_NO_USER;
        }
        final UserData userData = new UserData(split[0], maxNameLength, split[1], maxPassLength);
        if (databaseHandler.isValidUser(userData)) {
            LOGGER.debug("User {}", userData.userName);
            return userData;
        } else {
            return USER_DATA_NO_USER;
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) resp;

        UserData userData = findCredentials(request);

        if (userData == USER_DATA_NO_USER) {
            if (allowAnonymous) {
                chain.doFilter(new RequestWrapper(request, PrincipalExtended.ANONYMOUS_PRINCIPAL), response);
            } else {
                throwAuthRequired(response);
            }
        } else {
            boolean admin = userData.roles.contains(roleAdmin);
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

    private void throwInsufficientRights(HttpServletResponse response) {
        response.addHeader(AUTHORIZATION_REQUIRED_HEADER, authHeaderValue);
        try {
            response.sendError(403);
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

}
