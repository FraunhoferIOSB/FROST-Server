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
package de.fraunhofer.iosb.ilt.frostserver.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import tools.jackson.core.type.TypeReference;

public class ComplexValueImplOpen implements ComplexValue<ComplexValueImplOpen> {

    public static final TypeReference<ComplexValueImpl> TYPE_REFERENCE = new TypeReference<ComplexValueImpl>() {
        // Empty by design.
    };

    private final TypeComplex type;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public ComplexValueImplOpen(TypeComplex type) {
        this.type = type;
    }

    @Override
    public TypeComplex getType() {
        return type;
    }

    @Override
    public <P> P getProperty(Property<P> property) {
        return (P) properties.get(property.getJsonName());
    }

    @Override
    public <P> ComplexValueImplOpen setProperty(Property<P> property, P value) {
        properties.put(property.getJsonName(), value);
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAllProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @JsonAnySetter
    public void setAnyProperty(String name, Object value) {
        properties.put(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public ComplexValueImplOpen setProperty(String name, Object value) {
        if (!type.isOpenType()) {
            throw new IllegalArgumentException("Can not set custom properties on non-openType " + type);
        }
        properties.put(name, value);
        return this;
    }

    @Override
    public boolean isSetProperty(Property property) {
        return properties.containsKey(property.getJsonName());
    }

    public static TypeComplex.Instantiator createFor(TypeComplex type) {
        return t -> new ComplexValueImpl(t);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.properties);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ComplexValueImplOpen other = (ComplexValueImplOpen) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return Objects.equals(this.properties, other.properties);
    }

}
