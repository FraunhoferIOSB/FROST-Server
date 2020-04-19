/*
 * Copyright (C) 2019 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OpenAPI schema object.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OASchema {

    private static final Logger LOGGER = LoggerFactory.getLogger(OASchema.class.getName());

    public static enum Type {
        @JsonProperty(value = "string")
        STRING,
        @JsonProperty(value = "number")
        NUMBER,
        @JsonProperty(value = "integer")
        INTEGER,
        @JsonProperty(value = "object")
        OBJECT,
        @JsonProperty(value = "array")
        ARRAY,
        @JsonProperty(value = "boolean")
        BOOLEAN
    }

    public static enum Format {
        @JsonProperty(value = "int32")
        INT32,
        @JsonProperty(value = "int64")
        INT64,
        @JsonProperty(value = "float")
        FLOAT,
        @JsonProperty(value = "double")
        DOUBLE
    }

    @JsonProperty(value = "$ref")
    private String ref;
    private Type type;
    private Format format;
    private String description;
    @JsonProperty(value = "default")
    private String deflt;
    private OASchema items;
    private Map<String, OASchema> properties;
    private Boolean additionalProperties;

    public OASchema(Type type, Format format) {
        this.type = type;
        this.format = format;
    }

    public OASchema(String ref) {
        this.ref = ref;
    }

    public OASchema(Property property) {
        type = Type.STRING;
        LOGGER.trace("TODO: sniff type from property: {}.", property);
    }

    public void addProperty(String name, OASchema property) {
        if (properties == null) {
            properties = new TreeMap<>();
        }
        properties.put(name, property);
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
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the format
     */
    public Format getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(Format format) {
        this.format = format;
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
     * @return the deflt
     */
    public String getDeflt() {
        return deflt;
    }

    /**
     * @param deflt the deflt to set
     */
    public void setDeflt(String deflt) {
        this.deflt = deflt;
    }

    /**
     * @return the items
     */
    public OASchema getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(OASchema items) {
        this.items = items;
    }

    /**
     * @return the properties
     */
    public Map<String, OASchema> getProperties() {
        return properties;
    }

    /**
     * @return the additionalProperties
     */
    public Boolean getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * @param additionalProperties the additionalProperties to set
     */
    public void setAdditionalProperties(Boolean additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

}
