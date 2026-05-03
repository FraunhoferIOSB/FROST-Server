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
package de.fraunhofer.iosb.ilt.frostserver.property.type;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValueImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.ContainerType;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

/**
 * The complex type definition.
 */
public class TypeComplex extends PropertyType implements ContainerType<TypeComplex> {

    /**
     * The Set of PROPERTIES that Elements of this type have.
     */
    private final Set<EntityPropertyMain> properties = new LinkedHashSet<>();

    /**
     * The Set of PROPERTIES that Entities of this type have, mapped by their
     * name.
     */
    private final Map<String, Property> propertiesByName = new LinkedHashMap<>();
    private final boolean openType;
    private Instantiator instantiator;

    public TypeComplex(String name, String description, boolean openType) {
        super(name, description, null, null);
        this.openType = openType;
        setMediaType(Constants.CONTENT_TYPE_APPLICATION_JSON);
    }

    public TypeComplex(String name, String description, boolean openType, Instantiator instantiator, TypeReference tr) {
        super(name, description, ParserUtils.getDefaultDeserializer(tr), null);
        this.openType = openType;
        this.instantiator = instantiator;
        setMediaType(Constants.CONTENT_TYPE_APPLICATION_JSON);
    }

    public TypeComplex(String name, String description, boolean openType, Instantiator instantiator, ValueDeserializer jd, ValueSerializer js) {
        super(name, description, jd, js);
        this.openType = openType;
        this.instantiator = instantiator;
        setMediaType(Constants.CONTENT_TYPE_APPLICATION_JSON);
    }

    @Override
    public boolean isOpenType() {
        return openType;
    }

    @Override
    public Set<EntityPropertyMain> getEntityProperties() {
        return properties;
    }

    @Override
    public EntityPropertyMain getEntityProperty(String name) {
        return (EntityPropertyMain) propertiesByName.get(name);
    }

    @Override
    public Map<String, Property> getPropertiesByName() {
        return propertiesByName;
    }

    @Override
    public TypeComplex registerProperty(Property property) {
        if (property == null) {
            return this;
        }
        if (property instanceof EntityPropertyMain epm) {
            properties.add(epm);
            propertiesByName.put(epm.getName(), epm);
        } else {
            throw new IllegalArgumentException("Complex types can only have entity properties, not " + property.getClass().getName());
        }
        return this;
    }

    @Override
    public String toString() {
        return "TypeComplex: " + getName();
    }

    @Override
    public ValueSerializer getSerializer() {
        ValueSerializer serializer = super.getSerializer();
        if (serializer == null) {
            serializer = ParserUtils.getDefaultSerializer();
            setSerializer(serializer);
        }
        return serializer;
    }

    @Override
    public ValueDeserializer getDeserializer() {
        ValueDeserializer deserializer = super.getDeserializer();
        if (deserializer == null) {
            deserializer = ParserUtils.getComplexTypeDeserializer(this);
            setDeserializer(deserializer);
        }
        return deserializer;
    }

    public void setInstantiator(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    public ComplexValue instantiate() {
        if (instantiator == null) {
            this.instantiator = ComplexValueImpl.createFor(this);
        }
        return instantiator.instantiate(this);
    }

    public static interface Instantiator {

        public ComplexValue instantiate(TypeComplex type);
    }

}
