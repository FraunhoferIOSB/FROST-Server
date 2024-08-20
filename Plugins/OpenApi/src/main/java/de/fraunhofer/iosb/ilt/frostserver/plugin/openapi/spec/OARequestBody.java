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
import java.util.Map;
import java.util.TreeMap;

/**
 * An OpenAPI request body object.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OARequestBody {

    private String description;
    private Map<String, OAMediaType> content;
    private Boolean required;

    public OARequestBody addContent(String name, OAMediaType item) {
        if (content == null) {
            content = new TreeMap<>();
        }
        content.put(name, item);
        return this;
    }

    public boolean hasContent(String name) {
        if (content == null) {
            return false;
        }
        return content.containsKey(name);
    }

    public OAMediaType getContent(String name) {
        if (content == null) {
            return null;
        }
        return content.get(name);
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     * @return this
     */
    public OARequestBody setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return the content
     */
    public Map<String, OAMediaType> getContent() {
        return content;
    }

    /**
     * @return the required
     */
    public Boolean isRequired() {
        return required;
    }

    /**
     * @param required the required to set
     * @return this
     */
    public OARequestBody setRequired(Boolean required) {
        this.required = required;
        return this;
    }

}
