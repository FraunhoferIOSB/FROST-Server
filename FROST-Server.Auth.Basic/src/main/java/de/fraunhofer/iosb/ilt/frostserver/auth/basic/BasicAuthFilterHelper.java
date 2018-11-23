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
package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_DELETE;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_GET;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_PATCH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_POST;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_PUT;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.DEF_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import de.fraunhofer.iosb.ilt.sta.util.AuthUtils;
import de.fraunhofer.iosb.ilt.sta.util.AuthUtils.Role;
import java.util.EnumSet;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

/**
 *
 * @author scf
 */
public class BasicAuthFilterHelper {

    public static void createFilters(Object context, CoreSettings coreSettings) throws IllegalArgumentException {
        if (!(context instanceof ServletContext)) {
            throw new IllegalArgumentException("Context must be a ServletContext to add Filters.");
        }
        ServletContext servletContext = (ServletContext) context;
        Settings authSettings = coreSettings.getAuthSettings();
        Map<AuthUtils.Role, String> roleMapping = AuthUtils.loadRoleMapping(authSettings);
        String filterClass = BasicAuthFilter.class.getName();
        String filterName = "AuthFilterSta";
        FilterRegistration.Dynamic authFilterSta = servletContext.addFilter(filterName, filterClass);
        authFilterSta.setInitParameter(TAG_AUTH_ALLOW_ANON_READ, authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, DEF_AUTH_ALLOW_ANON_READ) ? "T" : "F");
        authFilterSta.setInitParameter(TAG_ROLE_GET, roleMapping.get(Role.READ));
        authFilterSta.setInitParameter(TAG_ROLE_PATCH, roleMapping.get(Role.UPDATE));
        authFilterSta.setInitParameter(TAG_ROLE_POST, roleMapping.get(Role.CREATE));
        authFilterSta.setInitParameter(TAG_ROLE_PUT, roleMapping.get(Role.UPDATE));
        authFilterSta.setInitParameter(TAG_ROLE_DELETE, roleMapping.get(Role.DELETE));
        authFilterSta.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/v1.0", "/v1.0/*");

        filterName = "AuthFilterAdmin";
        FilterRegistration.Dynamic authFilterAdmin = servletContext.addFilter(filterName, filterClass);
        authFilterSta.setInitParameter(TAG_AUTH_ALLOW_ANON_READ, "F");
        String adminRoleString = roleMapping.get(Role.ADMIN);
        authFilterAdmin.setInitParameter(TAG_ROLE_GET, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_PATCH, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_POST, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_PUT, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_DELETE, adminRoleString);
        authFilterAdmin.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/DatabaseStatus");
    }
}
