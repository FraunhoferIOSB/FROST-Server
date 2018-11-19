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
package de.fraunhofer.iosb.ilt.sta.messagebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.LocationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ObservationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.IdLong;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.util.TestHelper;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author jab
 */
public class MessageSerialisationTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private <T extends Entity> T setExports(T entity) {
        for (NavigationProperty property : entity.getEntityType().getNavigationEntities()) {
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
        Thing entity = new ThingBuilder()
                .setId(new IdLong(123456))
                .setName("testThing")
                .setDescription("A Thing for testing")
                .build();
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
        Location entity = new LocationBuilder()
                .setId(new IdLong(123456))
                .setName("testThing")
                .setDescription("A Thing for testing")
                .setEncodingType("application/geo+json")
                .setLocation(TestHelper.getPoint(-117.123, 54.123))
                .build();
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
        Thing entity = new ThingBuilder()
                .setId(new IdLong(123456))
                .setName("testThing")
                .setDescription("A Thing for testing")
                .build();
        message.setEntity(entity);
        message.addEpField(EntityProperty.NAME);
        message.addEpField(EntityProperty.DESCRIPTION);
        message.addField(NavigationProperty.DATASTREAMS);
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
        Observation entity = new ObservationBuilder()
                .setId(new IdLong(123456))
                .setResult(12345)
                .addParameter("param1", "value 1")
                .setDatastream(new DatastreamBuilder().setId(new IdLong(12)).build())
                .build();
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
