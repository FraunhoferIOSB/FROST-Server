/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import java.util.Collection;

/**
 * The interface that plugins must implement that want to make a service
 * available.
 *
 * @author scf
 */
public interface PluginService extends Plugin {

    /**
     * Get the URL paths that this service handles. These must start with a
     * slash (/).
     *
     * @return The URL paths that this service handles.
     */
    public Collection<String> getUrlPaths();

    /**
     * Get the request types this service handles.
     *
     * @return the request types this service handles.
     */
    public Collection<String> getRequestTypes();

    /**
     * Get the request method to be used for a request on the given path with
     * the given method.
     *
     * @param path The path of the request.
     * @param method The method of the request.
     * @return The requestType.
     */
    public String getRequestTypeFor(String path, HttpMethod method);

    /**
     * Execute the request, using the given main Service.
     *
     * @param mainService The main service for the request. Can supply the
     * PersistenceManager.
     * @param request The request to execute.
     * @param response The response object to put the response into.
     * @return The response to the request.
     */
    public ServiceResponse execute(Service mainService, ServiceRequest request, ServiceResponse response);

}
