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
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class MessageSerialisationTest {

    @Test
    public void serialiseMessageSimpleThing() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(EntityType.THING)
                .setId(new IdLong(123456))
                .setProperty(EntityPropertyMain.NAME, "testThing")
                .setProperty(EntityPropertyMain.DESCRIPTION, "A Thing for testing");
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        JsonReader parser = new JsonReader(IdLong.class);
        EntityChangedMessage deserialisedMessage = parser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageLocation() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(EntityType.LOCATION)
                .setId(new IdLong(123456))
                .setProperty(EntityPropertyMain.NAME, "testThing")
                .setProperty(EntityPropertyMain.DESCRIPTION, "A Thing for testing")
                .setProperty(EntityPropertyMain.ENCODINGTYPE, "application/geo+json")
                .setProperty(EntityPropertyMain.LOCATION, TestHelper.getPoint(-117.123, 54.123));
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        JsonReader parser = new JsonReader(IdLong.class);
        EntityChangedMessage deserialisedMessage = parser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageThingWithFields() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(EntityType.THING)
                .setId(new IdLong(123456))
                .setProperty(EntityPropertyMain.NAME, "testThing")
                .setProperty(EntityPropertyMain.DESCRIPTION, "A Thing for testing");
        message.setEntity(entity);
        message.addEpField(EntityPropertyMain.NAME);
        message.addEpField(EntityPropertyMain.DESCRIPTION);
        message.addField(NavigationPropertyMain.DATASTREAMS);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        JsonReader parser = new JsonReader(IdLong.class);
        EntityChangedMessage deserialisedMessage = parser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageSimpleObservation() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Entity entity = new DefaultEntity(EntityType.OBSERVATION)
                .setId(new IdLong(123456))
                .setProperty(EntityPropertyMain.RESULT, 12345)
                .setProperty(EntityPropertyMain.PARAMETERS, CollectionsHelper.propertiesBuilder()
                        .addProperty("param1", "value 1")
                        .build())
                .setProperty(NavigationPropertyMain.DATASTREAM, new DefaultEntity(EntityType.DATASTREAM, new IdLong(12)));
        entity.setProperty(EntityPropertyMain.RESULTTIME, null);
        message.setEntity(entity);

        ObjectMapper mapper = JsonWriter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        JsonReader parser = new JsonReader(IdLong.class);
        EntityChangedMessage deserialisedMessage = parser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

}
