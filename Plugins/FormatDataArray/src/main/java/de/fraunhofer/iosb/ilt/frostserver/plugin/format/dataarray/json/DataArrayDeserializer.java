/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomEntityDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.DataArrayValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class DataArrayDeserializer extends JsonDeserializer<List<DataArrayValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataArrayDeserializer.class.getName());
    private static final TypeReference<List<List<Object>>> LIST_LIST_OBJECT = new TypeReference<List<List<Object>>>() {
        // empty by design
    };

    private final ModelRegistry modelRegistry;
    private final PluginCoreModel pluginCoreModel;
    private final EntityType etMultiDatastream;

    public DataArrayDeserializer(CoreSettings settings) {
        modelRegistry = settings.getModelRegistry();
        pluginCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        etMultiDatastream = modelRegistry.getEntityTypeForName("MultiDatastream");
    }

    public static List<DataArrayValue> deserialize(String value, JsonReader reader, CoreSettings settings) throws IOException {
        ObjectMapper mapper = reader.getMapper();
        try (final JsonParser parser = mapper.createParser(value)) {
            DefaultDeserializationContext dsc = (DefaultDeserializationContext) mapper.getDeserializationContext();
            dsc = dsc.createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
            return new DataArrayDeserializer(settings).deserialize(parser, dsc);
        }
    }

    public static List<DataArrayValue> deserialize(Reader value, JsonReader reader, CoreSettings settings) throws IOException {
        ObjectMapper mapper = reader.getMapper();
        try (final JsonParser parser = mapper.createParser(value)) {
            DefaultDeserializationContext dsc = (DefaultDeserializationContext) mapper.getDeserializationContext();
            dsc = dsc.createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
            return new DataArrayDeserializer(settings).deserialize(parser, dsc);
        }
    }

    @Override
    public List<DataArrayValue> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        List<DataArrayValue> result = new ArrayList<>();
        JsonToken currentToken = parser.nextToken();
        expect(JsonToken.START_ARRAY, currentToken);
        currentToken = parser.nextToken();
        while (currentToken == JsonToken.START_OBJECT) {
            result.add(deserializeDataArrayValue(parser, ctxt));
            currentToken = parser.nextToken();
        }
        return result;
    }

    private DataArrayValue deserializeDataArrayValue(JsonParser parser, DeserializationContext ctxt) throws IOException {
        DataArrayValue result = new DataArrayValue();
        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            switch (fieldName) {
                case "Datastream":
                    parser.nextToken();
                    result.setDatastream(
                            CustomEntityDeserializer.getInstance(modelRegistry, pluginCoreModel.etDatastream)
                                    .deserialize(parser, ctxt));
                    break;

                case "MultiDatastream":
                    if (etMultiDatastream == null) {
                        throw new IllegalArgumentException("MultiDatastream plugin not enabled.");
                    }
                    parser.nextToken();
                    result.setMultiDatastream(
                            CustomEntityDeserializer.getInstance(modelRegistry, etMultiDatastream)
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

                case "dataArray@iot.count":
                    // ignore it, bad artifact of v1.0.
                    parser.nextToken();
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
