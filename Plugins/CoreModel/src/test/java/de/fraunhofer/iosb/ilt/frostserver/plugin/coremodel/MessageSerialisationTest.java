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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class MessageSerialisationTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;
    private static JsonReaderDefault entityParser;

    @BeforeAll
    public static void beforeClass() {
        coreSettings = new CoreSettings();
        modelRegistry = coreSettings.getModelRegistry();
        queryDefaults = coreSettings.getQueryDefaults();
        queryDefaults.setUseAbsoluteNavigationLinks(false);
        pluginCoreModel = new PluginCoreModel();
        pluginCoreModel.init(coreSettings);
        coreSettings.getPluginManager().initPlugins(null);
        entityParser = new JsonReaderDefault(modelRegistry);
    }

    @Test
    void serialiseMessageSimpleThing() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setPrimaryKeyValues(PkValue.of(123456L))
                .setProperty(pluginCoreModel.epName, "testThing")
                .setProperty(pluginCoreModel.epDescription, "A Thing for testing");
        entity.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityChangedMessage deserialisedMessage = entityParser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(deserialisedMessage, message);
    }

    @Test
    void serialiseMessageLocation() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(pluginCoreModel.etLocation)
                .setPrimaryKeyValues(PkValue.of(123456L))
                .setProperty(pluginCoreModel.epName, "testThing")
                .setProperty(pluginCoreModel.epDescription, "A Thing for testing")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/geo+json")
                .setProperty(pluginCoreModel.epLocation, TestHelper.jsonPoint(-117.123, 54.123));
        entity.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityChangedMessage deserialisedMessage = entityParser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(deserialisedMessage, message);
    }

    @Test
    void serialiseMessageThingWithFields() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setPrimaryKeyValues(PkValue.of(123456L))
                .setProperty(pluginCoreModel.epName, "testThing")
                .setProperty(pluginCoreModel.epDescription, "A Thing for testing");
        entity.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
        message.setEntity(entity);
        message.addEpField(pluginCoreModel.epName);
        message.addEpField(pluginCoreModel.epDescription);
        message.addField(pluginCoreModel.npDatastreamsThing);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityChangedMessage deserialisedMessage = entityParser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(deserialisedMessage, message);
    }

    @Test
    void serialiseMessageSimpleObservation() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(pluginCoreModel.etObservation)
                .setPrimaryKeyValues(PkValue.of(123456L))
                .setProperty(pluginCoreModel.epResult, 12345)
                .setProperty(pluginCoreModel.epParameters, CollectionsHelper.propertiesBuilder()
                        .addProperty("param1", "value 1")
                        .build())
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream, PkValue.of(12L)));
        entity.setProperty(pluginCoreModel.epResultTime, null);
        entity.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityChangedMessage deserialisedMessage = entityParser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(deserialisedMessage, message);
    }

}
