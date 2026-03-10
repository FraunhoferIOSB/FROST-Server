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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.AbstractSWE;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.AbstractSWEIdentifiable;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.constraint.AbstractConstraint;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.TypeIdResolver;

/**
 * Resolves Swe types based on their "type" property.
 */
public class SweTypeIdResolver implements TypeIdResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweTypeIdResolver.class.getName());

    private static final Map<String, Class<?>> annnotatedClasses;

    static {
        final Reflections reflections = new Reflections(AbstractSWE.class.getPackageName());
        annnotatedClasses = reflections
                .getSubTypesOf(AbstractSWEIdentifiable.class)
                .stream()
                .collect(Collectors.toMap(
                        x -> idFromClass(x),
                        x -> x));
        annnotatedClasses.putAll(
                reflections
                        .getSubTypesOf(AbstractConstraint.class)
                        .stream()
                        .collect(Collectors.toMap(
                                x -> idFromClass(x),
                                x -> x)));
    }

    private JavaType superType;

    @Override
    public void init(JavaType baseType) {
        superType = baseType;
    }

    @Override
    public String idFromValue(DatabindContext context, Object value) {
        return idFromClass(value.getClass());
    }

    public static String idFromClass(Class clazz) {
        final String className = clazz.getName();
        String name = className.substring(1 + className.lastIndexOf('.'));
        try {
            name = FieldUtils.readStaticField(clazz, "SWE_NAME").toString();
        } catch (NullPointerException | IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.trace("Class {} has no SWE_NAME field.", className);
        }
        LOGGER.trace("{} -> {}", clazz.getName(), name);
        return name;
    }

    @Override
    public String idFromValueAndType(DatabindContext context, Object value, Class<?> suggestedType) {
        return idFromClass(value.getClass());
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws JacksonException {
        if (!annnotatedClasses.containsKey(id)) {
            throw new RuntimeException(String.format("unkown type '%s'", id));
        }
        return context.constructSpecializedType(superType, annnotatedClasses.get(id));
    }

    @Override
    public String idFromBaseType(DatabindContext ctxt) {
        return idFromClass(superType.getRawClass());
    }

    @Override
    public String getDescForKnownTypeIds() {
        return annnotatedClasses.toString();
    }

}
