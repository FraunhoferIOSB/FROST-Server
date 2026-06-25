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

import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreService.VERSION_STA_V10_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreService.VERSION_STA_V11_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.STA_GEOJSON_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.STA_MAP_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.STA_TIMEINTERVAL_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.STA_TIMEVALUE_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_BINARY_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_BOOLEAN_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DATETIMEOFFSET_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DATE_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DECIMAL_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DOUBLE_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_GEOMETRY_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_INT16_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_INT32_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_INT64_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_UNTYPED_NAME;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleSet;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Type type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Format format;

    private String description;

    @JsonProperty(value = "default")
    private String deflt;

    private OASchema items;

    private Map<String, OASchema> properties;

    private Boolean additionalProperties;

    private Boolean readOnly;

    public OASchema(Type type, Format format) {
        this.type = type;
        this.format = format;
    }

    public OASchema(String ref) {
        this.ref = ref;
    }

    public OASchema(Version version, PropertyType propertyType) {
        final String propertyTypeName = propertyType.getName();
        switch (propertyTypeName) {
            case STA_TIMEINTERVAL_NAME:
            case STA_TIMEVALUE_NAME:
                if (VERSION_STA_V10_NAME.equals(version.urlPart) || VERSION_STA_V11_NAME.equals(version.urlPart)) {
                    type = Type.STRING;
                } else {
                    type = Type.OBJECT;
                    addSubtypeComplex(version, (TypeComplex) propertyType);
                }
                break;

            case EDM_BINARY_NAME:
                type = Type.STRING;
                format = Format.BINARY;
                break;

            case EDM_BOOLEAN_NAME:
                type = Type.BOOLEAN;
                break;

            case EDM_INT16_NAME:
            case EDM_INT32_NAME:
                type = Type.INTEGER;
                format = Format.INT32;
                break;

            case EDM_INT64_NAME:
                type = Type.INTEGER;
                format = Format.INT64;
                break;

            case EDM_DOUBLE_NAME:
            case EDM_DECIMAL_NAME:
                type = Type.NUMBER;
                format = Format.DOUBLE;
                break;

            case EDM_DATETIMEOFFSET_NAME:
                type = Type.STRING;
                format = Format.DATETIME;
                break;

            case EDM_DATE_NAME:
                type = Type.STRING;
                format = Format.DATE;
                break;

            case STA_MAP_NAME:
                type = Type.OBJECT;
                break;

            case EDM_UNTYPED_NAME:
                type = null;
                break;

            case EDM_STRING_NAME:
                type = Type.STRING;
                break;

            case EDM_GEOMETRY_NAME:
            case STA_GEOJSON_NAME:
                type = Type.OBJECT;
                break;

            default:
                if (propertyType instanceof TypeComplex typeComplex) {
                    type = Type.OBJECT;
                    addSubtypeComplex(version, typeComplex);
                } else if (propertyType instanceof TypeSimpleSet typeSimpleSet) {
                    type = Type.ARRAY;
                    setItems(new OASchema(version, typeSimpleSet.getContaintedType()));
                } else {
                    type = Type.STRING;
                }
                break;
        }
    }

    private void addSubtypeComplex(Version version, TypeComplex typeComplex) {
        for (var subPropEntry : typeComplex.getPropertiesByName().entrySet()) {
            var subPropName = subPropEntry.getKey();
            var subProp = subPropEntry.getValue();
            addProperty(subPropName, new OASchema(version, subProp.getType()));
        }
    }

    public OASchema addProperty(String name, OASchema property) {
        if (properties == null) {
            properties = new TreeMap<>();
        }
        properties.put(name, property);
        return this;
    }

    /**
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     * @return this
     */
    public OASchema setRef(String ref) {
        this.ref = ref;
        return this;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     * @return this
     */
    public OASchema setType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * @return the format
     */
    public Format getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     * @return this
     */
    public OASchema setFormat(Format format) {
        this.format = format;
        return this;
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
    public OASchema setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return the deflt
     */
    public String getDeflt() {
        return deflt;
    }

    /**
     * @param deflt the deflt to set
     * @return this
     */
    public OASchema setDeflt(String deflt) {
        this.deflt = deflt;
        return this;
    }

    /**
     * @return the items
     */
    public OASchema getItems() {
        return items;
    }

    /**
     * @param items the items to set
     * @return this
     */
    public OASchema setItems(OASchema items) {
        this.items = items;
        return this;
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
     * @return this
     */
    public OASchema setAdditionalProperties(Boolean additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public OASchema setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

}
