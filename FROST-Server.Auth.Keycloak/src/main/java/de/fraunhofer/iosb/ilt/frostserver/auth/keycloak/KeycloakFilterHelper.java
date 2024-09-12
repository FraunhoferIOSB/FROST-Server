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

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author scf
 */
public class KeycloakFilterHelper {

    private KeycloakFilterHelper() {
        // Utility class.
    }

    public static void createFilters(Object context, CoreSettings coreSettings) {
        if (!(context instanceof ServletContext)) {
            throw new IllegalArgumentException("Context must be a ServletContext to add Filters.");
        }
        ServletContext servletContext = (ServletContext) context;

        Map<String, Version> versions = coreSettings.getPluginManager().getVersions();
        List<String> urlPatterns = new ArrayList<>();
        for (Version version : versions.values()) {
            urlPatterns.add("/" + version.urlPart);
            urlPatterns.add("/" + version.urlPart + "/*");
        }
        urlPatterns.add("/DatabaseStatus");
        urlPatterns.add("/keycloak/*");

        String filterClass = KeycloakFilter.class.getName();
        String filterName = "AuthFilterSta";
        FilterRegistration.Dynamic authFilterSta = servletContext.addFilter(filterName, filterClass);
        authFilterSta.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, urlPatterns.toArray(String[]::new));
    }

}
