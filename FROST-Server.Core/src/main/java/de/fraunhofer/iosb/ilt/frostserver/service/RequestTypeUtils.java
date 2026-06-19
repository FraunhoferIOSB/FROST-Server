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
package de.fraunhofer.iosb.ilt.frostserver.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants and helper functions for dealing with request types.
 */
public class RequestTypeUtils {

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

    /**
     * Request Types as defined in OGC 23-019: SensorThings API version 2.0
     * https://hylkevds.github.io/23-019/23-019.html#request_types
     *
     */
    public enum Type_23019 {
        READ(RequestTypeUtils.READ, "Read data from the resource path."),
        CREATE(RequestTypeUtils.CREATE, "Create a new Entity, or new Entities, at the resource path."),
        UPDATE(RequestTypeUtils.UPDATE_CHANGES, "Change the Entity or Entities at the resource path. Only the attributes present in the request are updated."),
        REPLACE(RequestTypeUtils.UPDATE_ALL, "Replace all the data of the resource with different data."),
        DELETE(RequestTypeUtils.DELETE, "Remove the resource."),
        EXECUTE("execute", "Execute an function on a resource.");

        private static final Map<String, Type_23019> map = new HashMap<>();

        static {
            for (var type : Type_23019.values()) {
                map.put(type.name().toUpperCase(), type);
            }
        }

        public final String requestType;
        public final String description;

        private Type_23019(String requetsType, String description) {
            this.requestType = requetsType;
            this.description = description;
        }

        public static Type_23019 of(String type) {
            return map.get(type.toUpperCase());
        }
    }

    private RequestTypeUtils() {
        // Not for instantiation.
    }

}
