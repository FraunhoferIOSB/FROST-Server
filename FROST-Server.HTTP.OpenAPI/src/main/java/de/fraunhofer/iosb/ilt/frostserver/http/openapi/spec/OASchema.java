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
package de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import java.util.Map;
import java.util.TreeMap;

/**
 * An OpenAPI schema object.
 *
 * @author scf
 */
public final class OASchema {

    public static enum Type {
        string, number, integer, object, array, @JsonProperty(value = "boolean")
        bool
    }

    public static enum Format {
        int32, int64, @JsonProperty(value = "float")
        flt, @JsonProperty(value = "double")
        dble
    }
    @JsonProperty(value = "$ref")
    public String ref;
    public Type type;
    public Format format;
    public String description;
    @JsonProperty(value = "default")
    public String deflt;
    public OASchema items;
    public Map<String, OASchema> properties;
    public Boolean additionalProperties;

    public OASchema(Type type, Format format) {
        this.type = type;
        this.format = format;
    }

    public OASchema(String ref) {
        this.ref = ref;
    }

    public OASchema(Property property) {
        type = Type.string;
    }

    public void addProperty(String name, OASchema property) {
        if (properties == null) {
            properties = new TreeMap<>();
        }
        properties.put(name, property);
    }

}
