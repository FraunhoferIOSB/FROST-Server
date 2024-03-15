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
package de.fraunhofer.iosb.ilt.frostserver.model.ext;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The complex value for an open type.
 */
public class MapValue implements ComplexValue<MapValue> {

    private final Map<String, Object> content;

    public MapValue() {
        this.content = new LinkedHashMap<>();
    }

    public MapValue(Map<String, Object> content) {
        this.content = content;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @JsonAnyGetter
    public Map<String, Object> getContent() {
        return content;
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return content.entrySet();
    }

    @JsonIgnore
    public Object get(String name) {
        return content.get(name);
    }

    public boolean containsKey(String name) {
        return content.containsKey(name);
    }

    @JsonAnySetter
    public MapValue put(String name, Object value) {
        content.put(name, value);
        return this;
    }

    @JsonIgnore
    @Override
    public <P> P getProperty(Property<P> property) {
        return (P) content.get(property.getJsonName());
    }

    @JsonIgnore
    @Override
    public <P> MapValue setProperty(Property<P> property, P value) {
        content.put(property.getJsonName(), value);
        return this;
    }

    @Override
    public Object getProperty(String name) {
        return content.get(name);
    }

    @Override
    public MapValue setProperty(String name, Object value) {
        content.put(name, value);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MapValue o) {
            return content.equals(o.content);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return 79 * hash + Objects.hashCode(content);
    }

}
