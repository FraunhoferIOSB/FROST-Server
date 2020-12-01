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
package de.fraunhofer.iosb.ilt.frostserver.messagebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class MessageSerialisationTest {

    private static ModelRegistry modelRegistry;
    private static JsonReader entityParser;

    @BeforeClass
    public static void beforeClass() {
        modelRegistry = new ModelRegistry();
        modelRegistry.initDefaultTypes();
        modelRegistry.initFinalise();
        modelRegistry.setIdClass(IdLong.class);
        entityParser = new JsonReader(modelRegistry);
    }

    @Test
    public void serialiseMessageSimpleThing() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(modelRegistry.THING)
                .setId(new IdLong(123456))
                .setProperty(modelRegistry.EP_NAME, "testThing")
                .setProperty(modelRegistry.EP_DESCRIPTION, "A Thing for testing");
        entity.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityChangedMessage deserialisedMessage = entityParser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageLocation() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(modelRegistry.LOCATION)
                .setId(new IdLong(123456))
                .setProperty(modelRegistry.EP_NAME, "testThing")
                .setProperty(modelRegistry.EP_DESCRIPTION, "A Thing for testing")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/geo+json")
                .setProperty(modelRegistry.EP_LOCATION, TestHelper.getPoint(-117.123, 54.123));
        entity.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityChangedMessage deserialisedMessage = entityParser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageThingWithFields() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(modelRegistry.THING)
                .setId(new IdLong(123456))
                .setProperty(modelRegistry.EP_NAME, "testThing")
                .setProperty(modelRegistry.EP_DESCRIPTION, "A Thing for testing");
        entity.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
        message.setEntity(entity);
        message.addEpField(modelRegistry.EP_NAME);
        message.addEpField(modelRegistry.EP_DESCRIPTION);
        message.addField(modelRegistry.NP_DATASTREAMS);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityChangedMessage deserialisedMessage = entityParser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageSimpleObservation() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(modelRegistry.OBSERVATION)
                .setId(new IdLong(123456))
                .setProperty(modelRegistry.EP_RESULT, 12345)
                .setProperty(modelRegistry.EP_PARAMETERS, CollectionsHelper.propertiesBuilder()
                        .addProperty("param1", "value 1")
                        .build())
                .setProperty(modelRegistry.NP_DATASTREAM, new DefaultEntity(modelRegistry.DATASTREAM, new IdLong(12)));
        entity.setProperty(modelRegistry.EP_RESULTTIME, null);
        entity.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityChangedMessage deserialisedMessage = entityParser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

}
