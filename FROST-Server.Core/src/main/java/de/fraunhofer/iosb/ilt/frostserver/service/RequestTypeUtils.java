/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.service;

/**
 *
 * @author jab
 * @author scf
 */
public class RequestTypeUtils {

    /**
     * The request type definition for the service root.
     */
    public static final String GET_CAPABILITIES = "getCapabilities";
    /**
     * The request type definition for create/POST.
     */
    public static final String CREATE = "create";
    /**
     * The request type definition for read/GET.
     */
    public static final String READ = "read";
    /**
     * The request type definition for update/PUT.
     */
    public static final String UPDATE_ALL = "updateAll";
    /**
     * The request type definition for update/PATCH.
     */
    public static final String UPDATE_CHANGES = "updateChanged";
    /**
     * The request type definition for update/JSON-Patch.
     */
    public static final String UPDATE_CHANGESET = "updateChangeset";
    /**
     * The request type definition for delete.
     */
    public static final String DELETE = "delete";

    private RequestTypeUtils() {
        // Not for instantiation.
    }

}
