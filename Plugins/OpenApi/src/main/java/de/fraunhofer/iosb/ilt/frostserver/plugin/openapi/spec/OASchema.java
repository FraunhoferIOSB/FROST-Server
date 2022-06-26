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
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import java.util.Map;
import java.util.TreeMap;

/**
 * An OpenAPI schema object.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OASchema {

    public enum Type {
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

    public enum Format {
        @JsonProperty(value = "binary")
        BINARY,
        @JsonProperty(value = "int32")
        INT32,
        @JsonProperty(value = "int64")
        INT64,
        @JsonProperty(value = "float")
        FLOAT,
        @JsonProperty(value = "double")
        DOUBLE,
        @JsonProperty(value = "date")
        DATE,
        @JsonProperty(value = "date-time")
        DATETIME
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
        switch (property.getType().getName()) {
            case TypeSimplePrimitive.EDM_BINARY_NAME:
                type = Type.STRING;
                format = Format.BINARY;
                break;

            case TypeSimplePrimitive.EDM_BOOLEAN_NAME:
                type = Type.BOOLEAN;
                break;

            case TypeSimplePrimitive.EDM_INT16_NAME:
            case TypeSimplePrimitive.EDM_INT32_NAME:
                type = Type.INTEGER;
                format = Format.INT32;
                break;

            case TypeSimplePrimitive.EDM_INT64_NAME:
                type = Type.INTEGER;
                format = Format.INT64;
                break;

            case TypeSimplePrimitive.EDM_DOUBLE_NAME:
            case TypeSimplePrimitive.EDM_DECIMAL_NAME:
                type = Type.NUMBER;
                format = Format.DOUBLE;
                break;

            case TypeSimplePrimitive.EDM_DATETIMEOFFSET_NAME:
                type = Type.STRING;
                format = Format.DATETIME;
                break;

            case TypeSimplePrimitive.EDM_DATE_NAME:
                type = Type.STRING;
                format = Format.DATE;
                break;

            case TypeComplex.STA_MAP_NAME:
                type = Type.OBJECT;
                break;

            case TypeSimplePrimitive.EDM_UNTYPED_NAME:
            case TypeComplex.STA_OBJECT_NAME:
                type = null;
                break;

            default:
                type = Type.STRING;
                break;
        }
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
