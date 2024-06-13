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
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_GEOMETRY;

import de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils;
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

    public static final String STA_GEOJSON_NAME = "Geometry";
    public static final String STA_TM_INSTANT_NAME = "TM_Instant";
    public static final String STA_TM_INSTANT_ALIAS = "TimeInstant";
    public static final PropertyType STA_LOCATION = new TypeSimpleCustom(STA_GEOJSON_NAME, "A Free Location object", EDM_GEOMETRY)
            .setDeserializer(ParserUtils.getLocationDeserializer());
    public static final PropertyType STA_TM_INSTANT = new TypeSimpleCustom(STA_TM_INSTANT_NAME, "A Time Instant", EDM_DATETIMEOFFSET);

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeSimpleCustom.class.getName());
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
        TYPES.put(STA_TM_INSTANT_ALIAS, TYPES.get(STA_TM_INSTANT_NAME));
    }

    public static TypeSimpleCustom getType(String name) {
        return TYPES.get(name);
    }

    public TypeSimpleCustom(String name, String description, TypeSimplePrimitive underlyingType) {
        super(name, description, underlyingType);
    }

}
