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

import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_AUTHENTICATE_ONLY;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_DELETE;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_GET;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_PATCH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_POST;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_ROLE_PUT;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils.Role;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

/**
 *
 * @author scf
 */
public class BasicAuthFilterHelper {

    private BasicAuthFilterHelper() {
        // Utility class.
    }

    public static void createFilters(Object context, CoreSettings coreSettings) {
        if (!(context instanceof ServletContext)) {
            throw new IllegalArgumentException("Context must be a ServletContext to add Filters.");
        }
        final ServletContext servletContext = (ServletContext) context;
        final Settings authSettings = coreSettings.getAuthSettings();
        final boolean authOnly = authSettings.getBoolean(TAG_AUTHENTICATE_ONLY, BasicAuthProvider.class);

        final Map<AuthUtils.Role, String> roleMapping = AuthUtils.loadRoleMapping(authSettings);
        final String filterClass = BasicAuthFilter.class.getName();

        final Map<String, Version> versions = coreSettings.getPluginManager().getVersions();
        final List<String> urlPatterns = new ArrayList<>();
        for (Version version : versions.values()) {
            urlPatterns.add("/" + version.urlPart);
            urlPatterns.add("/" + version.urlPart + "/*");
        }

        String filterName = "AuthFilterSta";
        FilterRegistration.Dynamic authFilterSta = servletContext.addFilter(filterName, filterClass);
        final boolean anonRead = authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class);
        authFilterSta.setInitParameter(TAG_AUTHENTICATE_ONLY, authOnly ? "T" : "F");
        authFilterSta.setInitParameter(TAG_AUTH_ALLOW_ANON_READ, anonRead ? "T" : "F");
        authFilterSta.setInitParameter(TAG_ROLE_GET, roleMapping.get(Role.READ));
        authFilterSta.setInitParameter(TAG_ROLE_PATCH, roleMapping.get(Role.UPDATE));
        authFilterSta.setInitParameter(TAG_ROLE_POST, roleMapping.get(Role.CREATE));
        authFilterSta.setInitParameter(TAG_ROLE_PUT, roleMapping.get(Role.UPDATE));
        authFilterSta.setInitParameter(TAG_ROLE_DELETE, roleMapping.get(Role.DELETE));
        authFilterSta.setInitParameter(TAG_ROLE_ADMIN, roleMapping.get(Role.ADMIN));

        authFilterSta.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, urlPatterns.toArray(String[]::new));

        filterName = "AuthFilterAdmin";
        FilterRegistration.Dynamic authFilterAdmin = servletContext.addFilter(filterName, filterClass);
        authFilterAdmin.setInitParameter(TAG_AUTH_ALLOW_ANON_READ, "F");
        String adminRoleString = roleMapping.get(Role.ADMIN);
        authFilterAdmin.setInitParameter(TAG_ROLE_GET, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_PATCH, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_POST, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_PUT, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_DELETE, adminRoleString);
        authFilterAdmin.setInitParameter(TAG_ROLE_ADMIN, roleMapping.get(Role.ADMIN));
        authFilterAdmin.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/DatabaseStatus");
    }
}
