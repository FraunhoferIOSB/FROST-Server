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
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.TreeNode;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Utility class for (de)serialising.
 */
public class ParserUtils {

    private static TreeNodeSerializer treeNodeSerializer;
    private static TreeNodeDeserializer treeNodeDeserializer;

    private ParserUtils() {
        // Utility class
    }

    public static ValueSerializer<Object> getDefaultSerializer() {
        return new ValueSerializer<Object>() {

            @Override
            public void serialize(Object t, JsonGenerator jg, SerializationContext ctxt) {
                jg.writePOJO(t);
            }
        };
    }

    public static <T> ValueDeserializer<T> getDefaultDeserializer(TypeReference<T> tr) {
        return new ValueDeserializer<T>() {
            @Override
            public T deserialize(JsonParser jp, DeserializationContext dc) throws JacksonException {
                return jp.readValueAs(tr);
            }
        };
    }

    public static ValueDeserializer<Object> getLocationDeserializer() {
        return new ValueDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser jp, DeserializationContext dc) throws JacksonException {
                return jp.readValueAsTree();
            }
        };
    }

    public static TreeNodeSerializer getTreeNodeSerializer() {
        if (treeNodeSerializer == null) {
            treeNodeSerializer = new TreeNodeSerializer();
        }
        return treeNodeSerializer;
    }

    public static TreeNodeDeserializer getTreeNodeDeserializer() {
        if (treeNodeDeserializer == null) {
            treeNodeDeserializer = new TreeNodeDeserializer();
        }
        return treeNodeDeserializer;
    }

    public static class TreeNodeSerializer extends ValueSerializer<TreeNode> {

        @Override
        public void serialize(TreeNode value, JsonGenerator jg, SerializationContext sc) throws JacksonException {
            jg.writeTree(value);
        }
    }

    public static class TreeNodeDeserializer extends ValueDeserializer<TreeNode> {

        @Override
        public TreeNode deserialize(JsonParser jp, DeserializationContext dc) throws JacksonException {
            return jp.readValueAsTree();
        }
    }

    /**
     * @param type the type of the complex type.
     * @return The deserialiser.
     */
    public static ValueDeserializer<List<ComplexValue>> getComplexTypeListDeserializer(TypeComplex type) {
        return new ComplexTypeListDeserializer(type);
    }

    private static class ComplexTypeListDeserializer extends ValueDeserializer<List<ComplexValue>> {

        private final TypeComplex type;
        private final ComplexTypeDeserializer itemDeser;

        public ComplexTypeListDeserializer(TypeComplex type) {
            this.type = type;
            this.itemDeser = new ComplexTypeDeserializer(type);
        }

        @Override
        public List<ComplexValue> deserialize(JsonParser parser, DeserializationContext ctxt) {
            List<ComplexValue> result = null;
            JsonToken currentToken = parser.currentToken();
            if (currentToken == null) {
                currentToken = parser.nextToken();
            }
            if (currentToken == JsonToken.VALUE_NULL) {
                // Value is an explicit JSON null, return null.
                return result;
            }
            if (currentToken != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("Expected " + JsonToken.START_ARRAY + " got " + currentToken);
            }
            result = new ArrayList<>();
            currentToken = parser.nextToken();
            while (currentToken != JsonToken.END_ARRAY) {
                result.add(itemDeser.deserialize(parser, ctxt));
                currentToken = parser.nextToken();
            }
            return result;
        }

    }

    public static ValueDeserializer<ComplexValue> getComplexTypeDeserializer(TypeComplex type) {
        return new ComplexTypeDeserializer(type);
    }

    private static class ComplexTypeDeserializer extends ValueDeserializer<ComplexValue> {

        private final TypeComplex type;

        public ComplexTypeDeserializer(TypeComplex type) {
            this.type = type;
        }

        @Override
        public ComplexValue deserialize(JsonParser parser, DeserializationContext ctxt) {
            ComplexValue result = null;
            JsonToken currentToken = parser.currentToken();
            if (currentToken == JsonToken.VALUE_NULL) {
                // Value is an explicit JSON null, return null.
                return result;
            }
            result = type.instantiate();
            currentToken = parser.nextToken();
            while (currentToken == JsonToken.PROPERTY_NAME) {
                String fieldName = parser.currentName();
                parser.nextValue();
                Property property = type.getEntityProperty(fieldName);
                if (property == null) {
                    if (!type.isOpenType()) {
                        final String message = "Unknown field: " + fieldName + " on " + type.getName() + " expected one of: " + type.getPropertiesByName().keySet();
                        throw new UnrecognizedPropertyException(parser, message, parser.currentLocation(), TypeComplex.class, fieldName, null);
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

        private void deserializeProperty(JsonParser parser, DeserializationContext ctxt, Property property, ComplexValue result) {
            if (property instanceof EntityPropertyMain epm) {
                deserializeEntityProperty(parser, ctxt, epm, result);
            } else if (property instanceof NavigationProperty) {
                throw new IllegalArgumentException("NavigationProperties not supported on ComplexTypes.");
            }
        }

        private void deserializeEntityProperty(JsonParser parser, DeserializationContext ctxt, EntityPropertyMain property, ComplexValue result) {
            final ValueDeserializer deserializer = property.getType().getDeserializer();
            if (deserializer == null) {
                Object value = parser.readValueAs(Object.class);
                result.setProperty(property, value);
            } else {
                Object value = deserializer.deserialize(parser, ctxt);
                result.setProperty(property, value);
            }
        }
    }

    public static ComplexValue parseComplexValue(ObjectMapper mapper, TypeComplex type, String value) {
        try (final JsonParser parser = mapper.createParser(value)) {
            DeserializationContext dsc = mapper._deserializationContext();
            return ParserUtils.getComplexTypeDeserializer(type)
                    .deserialize(parser, dsc);
        } catch (StackOverflowError err) {
            throw new StreamReadException("Json is too deeply nested.");
        }
    }

    public static List<ComplexValue> parseComplexValueList(ObjectMapper mapper, TypeComplex type, String value) {
        try (final JsonParser parser = mapper.createParser(value)) {
            DeserializationContext dsc = mapper._deserializationContext();
            return ParserUtils.getComplexTypeListDeserializer(type)
                    .deserialize(parser, dsc);
        } catch (StackOverflowError err) {
            throw new StreamReadException("Json is too deeply nested.");
        }
    }

}
