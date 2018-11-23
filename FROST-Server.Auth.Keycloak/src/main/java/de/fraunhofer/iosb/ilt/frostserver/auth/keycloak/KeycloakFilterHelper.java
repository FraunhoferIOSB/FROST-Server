package de.fraunhofer.iosb.ilt.frostserver.auth.keycloak;

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
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.DEF_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class KeycloakFilterHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakFilterHelper.class);

    public static CoreSettings coreSettings;



    public static void createFilters(Object context, CoreSettings settings) throws IllegalArgumentException {
        if (!(context instanceof ServletContext)) {
            throw new IllegalArgumentException("Context must be a ServletContext to add Filters.");
        }
        coreSettings = settings;
        ServletContext servletContext = (ServletContext) context;
        Settings authSettings = settings.getAuthSettings();

        String filterClass = KeycloakFilter.class.getName();
        String filterName = "AuthFilterSta";
        FilterRegistration.Dynamic authFilterSta = servletContext.addFilter(filterName, filterClass);
        authFilterSta.setInitParameter(TAG_AUTH_ALLOW_ANON_READ, authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, DEF_AUTH_ALLOW_ANON_READ) ? "T" : "F");
        authFilterSta.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/keycloak/*", "/v1.0", "/v1.0/*", "/DatabaseStatus");

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
