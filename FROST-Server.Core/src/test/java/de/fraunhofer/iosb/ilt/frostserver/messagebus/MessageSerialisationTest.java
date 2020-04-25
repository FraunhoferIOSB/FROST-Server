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
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class MessageSerialisationTest {

    private <T extends Entity> T setExports(T entity) {
        for (NavigationPropertyMain property : entity.getEntityType().getNavigationEntities()) {
            Object parentObject = entity.getProperty(property);
            if (parentObject instanceof Entity) {
                Entity parentEntity = (Entity) parentObject;
                parentEntity.setExportObject(true);
            }
        }
        return entity;
    }

    @Test
    public void serialiseMessageSimpleThing() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Thing entity = new Thing()
                .setId(new IdLong(123456))
                .setName("testThing")
                .setDescription("A Thing for testing");
        message.setEntity(entity);
        setExports(entity);

        ObjectMapper mapper = EntityFormatter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityParser parser = new EntityParser(IdLong.class);
        EntityChangedMessage deserialisedMessage = parser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageLocation() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Location entity = new Location()
                .setId(new IdLong(123456))
                .setName("testThing")
                .setDescription("A Thing for testing")
                .setEncodingType("application/geo+json")
                .setLocation(TestHelper.getPoint(-117.123, 54.123));
        message.setEntity(entity);
        setExports(entity);

        ObjectMapper mapper = EntityFormatter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityParser parser = new EntityParser(IdLong.class);
        EntityChangedMessage deserialisedMessage = parser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageThingWithFields() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Thing entity = new Thing()
                .setId(new IdLong(123456))
                .setName("testThing")
                .setDescription("A Thing for testing");
        message.setEntity(entity);
        message.addEpField(EntityProperty.NAME);
        message.addEpField(EntityProperty.DESCRIPTION);
        message.addField(NavigationPropertyMain.DATASTREAMS);
        setExports(entity);

        ObjectMapper mapper = EntityFormatter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityParser parser = new EntityParser(IdLong.class);
        EntityChangedMessage deserialisedMessage = parser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

    @Test
    public void serialiseMessageSimpleObservation() throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        Observation entity = new Observation()
                .setId(new IdLong(123456))
                .setResult(12345)
                .addParameter("param1", "value 1")
                .setDatastream(new Datastream().setId(new IdLong(12)));
        entity.setResultTime(new TimeInstant(null));
        message.setEntity(entity);
        setExports(entity);

        ObjectMapper mapper = EntityFormatter.getObjectMapper();
        String serialisedMessage = mapper.writeValueAsString(message);

        EntityParser parser = new EntityParser(IdLong.class);
        EntityChangedMessage deserialisedMessage = parser.parseObject(EntityChangedMessage.class, serialisedMessage);

        assertEquals(message, deserialisedMessage);
    }

}
