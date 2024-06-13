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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An OpenAPI parameter object.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OAParameter {

    public enum In {
        @JsonProperty("query")
        QUERY,
        @JsonProperty("header")
        HEADER,
        @JsonProperty("path")
        PATH,
        @JsonProperty("cookie")
        COOKIE
    }

    @JsonProperty(value = "$ref")
    private String ref;
    private String name;
    private In in = In.PATH;
    private String description;
    private Boolean required = false;
    private OASchema schema;

    public OAParameter(String refName) {
        ref = "#/components/parameters/" + refName;
        in = null;
        required = null;
    }

    public OAParameter(String name, String description, OASchema schema) {
        this.name = name;
        this.description = description;
        this.schema = schema;
        this.required = true;
    }

    public OAParameter(String name, In in, String description, OASchema schema) {
        this.name = name;
        this.description = description;
        this.schema = schema;
        this.in = in;
    }

    /**
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the in
     */
    public In getIn() {
        return in;
    }

    /**
     * @param in the in to set
     */
    public void setIn(In in) {
        this.in = in;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the required
     */
    public Boolean getRequired() {
        return required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @return the schema
     */
    public OASchema getSchema() {
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    public void setSchema(OASchema schema) {
        this.schema = schema;
    }

}
