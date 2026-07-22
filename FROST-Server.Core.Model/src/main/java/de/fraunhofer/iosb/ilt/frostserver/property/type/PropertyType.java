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

import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotatable;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.Exceptions;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

/**
 * Class for defining property types.
 */
public class PropertyType implements Annotatable {

    private String namespace;
    private final String name;
    private final String description;
    private String mediaType = Constants.CONTENT_TYPE_TEXT_PLAIN;
    private ValueDeserializer deserializer;
    private ValueSerializer serializer;
    private final List<Annotation> annotations = new ArrayList<>();

    public PropertyType(String name, String description, ValueDeserializer deserializer, ValueSerializer serializer) {
        this.name = name;
        this.description = description;
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    public PropertyType(String name, String description, ValueDeserializer deserializer) {
        this(name, description, deserializer, ParserUtils.getDefaultSerializer());
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getNamespace() {
        return namespace;
    }

    public PropertyType setNamespace(String namespace) {
        Exceptions.illegalArgumentIf(this.namespace != null, "Changing namespace on PropertyType {} is not allowed", name);
        this.namespace = namespace;
        return this;
    }

    public ValueDeserializer getDeserializer() {
        return deserializer;
    }

    public PropertyType setDeserializer(ValueDeserializer deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public String getMediaType() {
        return mediaType;
    }

    public PropertyType setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public ValueSerializer getSerializer() {
        return serializer;
    }

    public PropertyType setSerializer(ValueSerializer serializer) {
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
