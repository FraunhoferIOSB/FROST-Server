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

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotatable;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for defining property types.
 */
public class PropertyType implements Annotatable {

    private final String name;
    private final String description;
    private JsonDeserializer deserializer;
    private JsonSerializer serializer;
    private final List<Annotation> annotations = new ArrayList<>();

    public PropertyType(String name, String description, JsonDeserializer deserializer, JsonSerializer serializer) {
        this.name = name;
        this.description = description;
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    public PropertyType(String name, String description, JsonDeserializer deserializer) {
        this(name, description, deserializer, ParserUtils.getDefaultSerializer());
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public JsonDeserializer getDeserializer() {
        return deserializer;
    }

    public PropertyType setDeserializer(JsonDeserializer deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public JsonSerializer getSerializer() {
        return serializer;
    }

    public PropertyType setSerializer(JsonSerializer serializer) {
        this.serializer = serializer;
        return this;
    }

    public Object parseFromUrl(String input) {
        throw new IllegalArgumentException("Don't know how to parse for " + name);
    }

    public boolean isCollection() {
        return false;
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public PropertyType addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public PropertyType addAnnotations(List<Annotation> annotationsToAdd) {
        annotations.addAll(annotationsToAdd);
        return this;
    }

}
