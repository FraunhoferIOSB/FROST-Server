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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.jsonarray;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomEntityDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.request.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationContextExt;

/**
 * DeSerializer for DataArray JSON documents.
 */
public class JsonArrayValueDeserializer extends ValueDeserializer<List<JsonArrayValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonArrayValueDeserializer.class.getName());
    private static final TypeReference<List<List<Object>>> LIST_LIST_OBJECT = new TypeReference<List<List<Object>>>() {
        // empty by design
    };

    private final ModelRegistry modelRegistry;
    private final EntityType etDatastream;
    private final Version version;

    public JsonArrayValueDeserializer(CoreSettings settings, Version version) {
        this.version = version;
        modelRegistry = settings.getModelRegistry();
        etDatastream = modelRegistry.getEntityTypeForName("Datastream");
    }

    public static List<JsonArrayValue> deserialize(String value, JsonReader reader, CoreSettings settings) throws JacksonException {
        ObjectMapper mapper = reader.getMapper();
        try (final JsonParser parser = mapper.createParser(value)) {
            DeserializationContextExt dsc = mapper._deserializationContext();
            return new JsonArrayValueDeserializer(settings, reader.getVersion()).deserialize(parser, dsc);
        }
    }

    public static List<JsonArrayValue> deserialize(Reader value, JsonReader reader, CoreSettings settings) throws JacksonException {
        ObjectMapper mapper = reader.getMapper();
        try (final JsonParser parser = mapper.createParser(value)) {
            DeserializationContextExt dsc = mapper._deserializationContext();
            return new JsonArrayValueDeserializer(settings, reader.getVersion()).deserialize(parser, dsc);
        }
    }

    @Override
    public List<JsonArrayValue> deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        List<JsonArrayValue> result = new ArrayList<>();
        JsonToken currentToken = parser.nextToken();
        expect(JsonToken.START_ARRAY, currentToken);
        currentToken = parser.nextToken();
        while (currentToken == JsonToken.START_OBJECT) {
            result.add(deserializeJsonArrayValue(parser, ctxt));
            currentToken = parser.nextToken();
        }
        return result;
    }

    private JsonArrayValue deserializeJsonArrayValue(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        JsonArrayValue result = new JsonArrayValue();
        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.PROPERTY_NAME) {
            String fieldName = parser.currentName();
            switch (fieldName) {
                case "Datastream":
                    parser.nextToken();
                    result.setDatastream(
                            CustomEntityDeserializer.getInstance(modelRegistry, etDatastream, version)
                                    .deserialize(parser, ctxt));
                    break;

                case "components":
                    currentToken = parser.nextToken();
                    expect(JsonToken.START_ARRAY, currentToken);
                    currentToken = parser.nextToken();
                    while (currentToken == JsonToken.VALUE_STRING) {
                        String componentName = parser.getValueAsString();
                        result.getComponents().add(componentName);
                        currentToken = parser.nextToken();
                    }
                    break;

                case "dataArray":
                    parser.nextToken();
                    result.setDataArray(parser.readValueAs(LIST_LIST_OBJECT));
                    break;

                default:
                    LOGGER.warn("Unknown field in message: {}", fieldName);
                    break;
            }
            currentToken = parser.nextToken();
        }
        return result;
    }

    private static void expect(JsonToken expected, JsonToken value) {
        if (value != expected) {
            throw new IllegalArgumentException("Expected token " + expected + ". Found: " + value);
        }
    }
}
