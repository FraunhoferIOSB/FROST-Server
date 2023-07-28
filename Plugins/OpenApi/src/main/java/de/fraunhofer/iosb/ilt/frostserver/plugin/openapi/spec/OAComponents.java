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
package de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.TreeMap;

/**
 * An OpenAPI components object.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OAComponents {

    private Map<String, OASchema> schemas;
    private Map<String, OAParameter> parameters;
    private Map<String, OAResponse> responses;
    private Map<String, OAHeader> headers;

    public Map<String, OASchema> getSchemas() {
        return schemas;
    }

    public void addSchema(String name, OASchema schema) {
        if (schemas == null) {
            schemas = new TreeMap<>();
        }
        schemas.put(name, schema);
    }

    public boolean hasSchema(String name) {
        if (schemas == null) {
            return false;
        }
        return schemas.containsKey(name);
    }

    public OASchema getSchema(String name) {
        if (schemas == null) {
            return null;
        }
        return schemas.get(name);
    }

    public Map<String, OAParameter> getParameters() {
        return parameters;
    }

    public void addParameter(String name, OAParameter param) {
        if (parameters == null) {
            parameters = new TreeMap<>();
        }
        parameters.put(name, param);
    }

    public boolean hasParameter(String name) {
        if (parameters == null) {
            return false;
        }
        return parameters.containsKey(name);
    }

    public OAParameter getParameter(String name) {
        if (parameters == null) {
            return null;
        }
        return parameters.get(name);
    }

    public Map<String, OAResponse> getResponses() {
        return responses;
    }

    public void addResponse(String name, OAResponse value) {
        if (responses == null) {
            responses = new TreeMap<>();
        }
        responses.put(name, value);
    }

    public boolean hasResponse(String name) {
        if (responses == null) {
            return false;
        }
        return responses.containsKey(name);
    }

    public OAResponse getResponse(String name) {
        if (responses == null) {
            return null;
        }
        return responses.get(name);
    }

    public Map<String, OAHeader> getHeaders() {
        return headers;
    }

    public void addHeader(String name, OAHeader value) {
        if (headers == null) {
            headers = new TreeMap<>();
        }
        headers.put(name, value);
    }

    public boolean hasHeader(String name) {
        if (headers == null) {
            return false;
        }
        return headers.containsKey(name);
    }

    public OAHeader getHeader(String name) {
        if (headers == null) {
            return null;
        }
        return headers.get(name);
    }

}
