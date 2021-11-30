/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.property.type;

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class TypeComplex extends PropertyType {

    public static final TypeComplex STA_MAP = new TypeComplex("Object", "A free object that can contain anything", TypeReferencesHelper.TYPE_REFERENCE_MAP, true);
    public static final TypeComplex STA_OBJECT = new TypeComplex("ANY", "A free type, can be anything", TypeReferencesHelper.TYPE_REFERENCE_OBJECT, true);
    public static final TypeComplex STA_OBJECT_UNTYPED = new TypeComplex("ANY", "A free type, can be anything", null, true);
    public static final TypeComplex STA_TIMEINTERVAL = new TypeComplex("TimeInterval", "An ISO time interval.", TypeReferencesHelper.TYPE_REFERENCE_TIMEINTERVAL)
            .addProperty("start", TypeSimplePrimitive.EDM_DATETIMEOFFSET)
            .addProperty("end", TypeSimplePrimitive.EDM_DATETIMEOFFSET);
    public static final TypeComplex STA_TIMEVALUE = new TypeComplex("TimeValue", "An ISO time instant or time interval.", TypeReferencesHelper.TYPE_REFERENCE_TIMEVALUE)
            .addProperty("start", TypeSimplePrimitive.EDM_DATETIMEOFFSET)
            .addProperty("end", TypeSimplePrimitive.EDM_DATETIMEOFFSET);

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
    }

    public static TypeComplex getType(String name) {
        return TYPES.get(name);
    }

    private final boolean openType;
    private final Map<String, PropertyType> properties = new LinkedHashMap<>();

    public TypeComplex(String name, String description, TypeReference typeReference) {
        this(name, description, typeReference, false);
    }

    public TypeComplex(String name, String description, TypeReference typeReference, boolean openType) {
        super(name, description, typeReference);
        this.openType = openType;
    }

    public boolean isOpenType() {
        return openType;
    }

    public Map<String, PropertyType> getProperties() {
        return properties;
    }

    public TypeComplex addProperty(String name, PropertyType property) {
        properties.put(name, property);
        return this;
    }

}
