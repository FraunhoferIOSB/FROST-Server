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
package de.fraunhofer.iosb.ilt.frostserver.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import java.io.IOException;

/**
 *
 * @author hylke
 */
public class ParserUtils {

    private ParserUtils() {
        // Utility class
    }

    public static JsonSerializer<Object> getDefaultSerializer() {
        return new JsonSerializer<Object>() {
            @Override
            public void serialize(Object t, JsonGenerator jg, SerializerProvider sp) throws IOException {
                jg.writePOJO(t);
            }
        };
    }

    public static <T> JsonDeserializer<T> getDefaultDeserializer(TypeReference<T> tr) {
        return new JsonDeserializer<T>() {
            @Override
            public T deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JacksonException {
                return jp.readValueAs(tr);
            }
        };
    }

    public static JsonDeserializer<Object> getLocationDeserializer() {
        return new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
                final TreeNode valueTree = jp.readValueAsTree();
                return valueTree;
            }
        };
    }

    /**
     * @param type the type of the complex type.
     * @return The deserialiser.
     */
    public static JsonDeserializer<ComplexValue> getComplexTypeDeserializer(TypeComplex type) {
        return new ComplexTypeDeserializer(type);
    }

    private static class ComplexTypeDeserializer extends JsonDeserializer<ComplexValue> {

        private final TypeComplex type;

        public ComplexTypeDeserializer(TypeComplex type) {
            this.type = type;
        }

        @Override
        public ComplexValue deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            ComplexValue result = type.instantiate();
            JsonToken currentToken = parser.currentToken();
            if (currentToken == JsonToken.VALUE_NULL) {
                return null;
            }
            currentToken = parser.nextToken();
            while (currentToken == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();
                parser.nextValue();
                Property property = type.getProperty(fieldName);
                if (property == null) {
                    if (!type.isOpenType()) {
                        final String message = "Unknown field: " + fieldName + " on " + type.getName() + " expected one of: " + type.getPropertiesByName().keySet();
                        throw new UnrecognizedPropertyException(parser, message, parser.getCurrentLocation(), TypeComplex.class, fieldName, null);
                    } else {
                        result.setProperty(fieldName, parser.readValueAsTree());
                    }
                } else {
                    deserializeProperty(parser, ctxt, property, result);
                }
                currentToken = parser.nextToken();
            }

            return result;
        }

        private void deserializeProperty(JsonParser parser, DeserializationContext ctxt, Property property, ComplexValue result) throws IOException {
            if (property instanceof EntityPropertyMain epm) {
                deserializeEntityProperty(parser, ctxt, epm, result);
            } else if (property instanceof NavigationProperty) {
                throw new IllegalArgumentException("NavigationProperties not supported on ComplexTypes.");
            }
        }

        private void deserializeEntityProperty(JsonParser parser, DeserializationContext ctxt, EntityPropertyMain property, ComplexValue result) throws IOException {
            final JsonDeserializer deserializer = property.getType().getDeserializer();
            if (deserializer == null) {
                Object value = parser.readValueAs(Object.class);
                result.setProperty(property, value);
            } else {
                Object value = deserializer.deserialize(parser, ctxt);
                result.setProperty(property, value);
            }
        }
    }
}
