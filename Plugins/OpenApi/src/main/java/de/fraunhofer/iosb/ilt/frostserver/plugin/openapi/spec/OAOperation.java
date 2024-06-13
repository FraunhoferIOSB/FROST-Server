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
package de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An OpenAPI operation object.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OAOperation {

    private List<OAParameter> parameters;
    private OARequestBody requestBody;
    private final Map<String, OAResponse> responses = new TreeMap<>();

    public OAOperation addParameter(OAParameter parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        parameters.add(parameter);
        return this;
    }

    /**
     * @return the parameters
     */
    public List<OAParameter> getParameters() {
        return parameters;
    }

    /**
     * @return the requestBody
     */
    public OARequestBody getRequestBody() {
        return requestBody;
    }

    /**
     * @param requestBody the requestBody to set
     * @return this
     */
    public OAOperation setRequestBody(OARequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * @return the responses
     */
    public Map<String, OAResponse> getResponses() {
        return responses;
    }

    public OAOperation addResponse(String code, OAResponse response) {
        responses.put(code, response);
        return this;
    }

    /**
     * @param parameters the parameters to set
     * @return this
     */
    public OAOperation setParameters(List<OAParameter> parameters) {
        this.parameters = parameters;
        return this;
    }

}
