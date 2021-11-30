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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class TypeSimpleCustom extends TypeSimple {

    public static TypeSimpleCustom STA_GEOJSON = new TypeSimpleCustom("GeoJson", "A GeoJSON Object", TypeSimplePrimitive.EDM_GEOMETRY, null);

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeSimplePrimitive.class.getName());
    private static final Map<String, TypeSimpleCustom> TYPES = new HashMap<>();

    static {
        for (Field field : FieldUtils.getAllFields(TypeSimpleCustom.class)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                final TypeSimpleCustom type = (TypeSimpleCustom) FieldUtils.readStaticField(field, false);
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

    public static TypeSimpleCustom getType(String name) {
        return TYPES.get(name);
    }

    public TypeSimpleCustom(String name, String description, TypeSimplePrimitive underlyingType) {
        super(name, description, underlyingType, underlyingType.getTypeReference());
    }

    public TypeSimpleCustom(String name, String description, TypeSimplePrimitive underlyingType, TypeReference typeReference) {
        super(name, description, underlyingType, typeReference);
    }

}
