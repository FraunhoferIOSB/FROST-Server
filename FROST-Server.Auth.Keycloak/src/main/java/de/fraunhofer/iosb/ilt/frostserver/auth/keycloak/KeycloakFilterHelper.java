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
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

/**
 *
 * @author scf
 */
public class KeycloakFilterHelper {

    private KeycloakFilterHelper() {
        // Utility class.
    }

    public static void createFilters(Object context) {
        if (!(context instanceof ServletContext)) {
            throw new IllegalArgumentException("Context must be a ServletContext to add Filters.");
        }
        ServletContext servletContext = (ServletContext) context;

        String filterClass = KeycloakFilter.class.getName();
        String filterName = "AuthFilterSta";
        FilterRegistration.Dynamic authFilterSta = servletContext.addFilter(filterName, filterClass);
        authFilterSta.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/keycloak/*", "/v1.0", "/v1.0/*", "/DatabaseStatus");
    }

}
