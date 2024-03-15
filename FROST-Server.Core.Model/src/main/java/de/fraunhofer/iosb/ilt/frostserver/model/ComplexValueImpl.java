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
package de.fraunhofer.iosb.ilt.frostserver.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ComplexValueImpl implements ComplexValue<ComplexValueImpl> {

    public static final TypeReference<ComplexValueImpl> TYPE_REFERENCE = new TypeReference<ComplexValueImpl>() {
        // Empty by design.
    };

    private final TypeComplex type;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public ComplexValueImpl(TypeComplex type) {
        this.type = type;
    }

    @JsonIgnore
    public TypeComplex getType() {
        return type;
    }

    @Override
    public <P> P getProperty(Property<P> property) {
        return (P) properties.get(property.getJsonName());
    }

    @Override
    public <P> ComplexValueImpl setProperty(Property<P> property, P value) {
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
    public ComplexValueImpl setProperty(String name, Object value) {
        if (!type.isOpenType()) {
            throw new IllegalArgumentException("Can not set custom properties on non-openType " + type);
        }
        properties.put(name, value);
        return this;
    }

    public static TypeComplex.Instantiator createFor(TypeComplex type) {
        return () -> new ComplexValueImpl(type);
    }

}
