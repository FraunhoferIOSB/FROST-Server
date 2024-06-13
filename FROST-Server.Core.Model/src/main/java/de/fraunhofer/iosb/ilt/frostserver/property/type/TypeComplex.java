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

import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DATETIMEOFFSET;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValueImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class TypeComplex extends PropertyType {

    public static final String STA_MAP_NAME = "Object";
    public static final String STA_OBJECT_NAME = "ANY";
    public static final String STA_TIMEINTERVAL_NAME = "TM_Period";
    public static final String STA_TIMEINTERVAL_ALIAS = "TimeInterval";
    public static final String STA_TIMEVALUE_NAME = "TM_Object";
    public static final String STA_TIMEVALUE_ALIAS = "TimeValue";
    public static final String STA_TIMEVALUE_ALIAS2 = "TimeObject";
    public static final String NAME_INTERVAL_START = "start";
    public static final String NAME_INTERVAL_END = "end";

    public static final EntityPropertyMain<TimeInstant> EP_START_TIME = new EntityPropertyMain<TimeInstant>(NAME_INTERVAL_START, EDM_DATETIMEOFFSET)
            .setNullable(false);
    public static final EntityPropertyMain<TimeInstant> EP_INTERVAL_END_TIME = new EntityPropertyMain<TimeInstant>(NAME_INTERVAL_END, EDM_DATETIMEOFFSET)
            .setNullable(false);
    public static final EntityPropertyMain<TimeInstant> EP_VALUE_END_TIME = new EntityPropertyMain<TimeInstant>(NAME_INTERVAL_END, EDM_DATETIMEOFFSET)
            .setNullable(true);

    public static final TypeComplex STA_MAP = new TypeComplex(STA_MAP_NAME, "A free object that can contain anything", true, MapValue::new, TypeReferencesHelper.TYPE_REFERENCE_MAP);
    public static final TypeComplex STA_OBJECT = new TypeComplex(STA_OBJECT_NAME, "A free type, can be anything", true, null, TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeComplex STA_OBJECT_UNTYPED = new TypeComplex(STA_OBJECT_NAME, "A free type, can be anything", true, null, null, null);

    public static final TypeComplex STA_TIMEINTERVAL = new TypeComplex(STA_TIMEINTERVAL_NAME, "An ISO time interval.", false, TimeInterval::new, TypeReferencesHelper.TYPE_REFERENCE_TIMEINTERVAL)
            .registerProperty(EP_START_TIME)
            .registerProperty(EP_INTERVAL_END_TIME);
    public static final TypeComplex STA_TIMEVALUE = new TypeComplex(STA_TIMEVALUE_NAME, "An ISO time instant or time interval.", false, TimeValue::new, TypeReferencesHelper.TYPE_REFERENCE_TIMEVALUE)
            .registerProperty(EP_START_TIME)
            .registerProperty(EP_VALUE_END_TIME);

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeComplex.class.getName());
    private static final Map<String, TypeComplex> TYPES = new HashMap<>();

    static {
        for (Field field : FieldUtils.getAllFields(TypeComplex.class)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                final TypeComplex type = (TypeComplex) FieldUtils.readStaticField(field, false);
                final String name = type.getName();
                TYPES.put(name, type);
                LOGGER.debug("Registered type: {}", name);
            } catch (IllegalArgumentException ex) {
                LOGGER.error("Failed to initialise: {}", field, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.trace("Failed to initialise: {}", field, ex);
            } catch (ClassCastException ex) {
                // It's not a TypeSimplePrimitive
            }
        }
        TYPES.put(STA_TIMEINTERVAL_ALIAS, TYPES.get(STA_TIMEINTERVAL_NAME));
        TYPES.put(STA_TIMEVALUE_ALIAS, TYPES.get(STA_TIMEVALUE_NAME));
        TYPES.put(STA_TIMEVALUE_ALIAS2, TYPES.get(STA_TIMEVALUE_NAME));

    }

    public static TypeComplex getType(String name) {
        return TYPES.get(name);
    }

    /**
     * The Set of PROPERTIES that Elements of this type have.
     */
    private final Set<Property> properties = new LinkedHashSet<>();

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
    }

    public TypeComplex(String name, String description, boolean openType, Instantiator instantiator, TypeReference tr) {
        super(name, description, ParserUtils.getDefaultDeserializer(tr), null);
        this.openType = openType;
        this.instantiator = instantiator;
    }

    public TypeComplex(String name, String description, boolean openType, Instantiator instantiator, JsonDeserializer jd, JsonSerializer js) {
        super(name, description, jd, js);
        this.openType = openType;
        this.instantiator = instantiator;
    }

    public boolean isOpenType() {
        return openType;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public Property getProperty(String name) {
        return propertiesByName.get(name);
    }

    public Map<String, Property> getPropertiesByName() {
        return propertiesByName;
    }

    public TypeComplex registerProperty(Property property) {
        properties.add(property);
        propertiesByName.put(property.getName(), property);
        return this;
    }

    @Override
    public String toString() {
        return "TypeComplex: " + getName();
    }

    @Override
    public JsonSerializer getSerializer() {
        JsonSerializer serializer = super.getSerializer();
        if (serializer == null) {
            serializer = ParserUtils.getDefaultSerializer();
            setSerializer(serializer);
        }
        return serializer;
    }

    @Override
    public JsonDeserializer getDeserializer() {
        JsonDeserializer deserializer = super.getDeserializer();
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
        return instantiator.instantiate();
    }

    public static interface Instantiator {

        public ComplexValue instantiate();
    }

}
