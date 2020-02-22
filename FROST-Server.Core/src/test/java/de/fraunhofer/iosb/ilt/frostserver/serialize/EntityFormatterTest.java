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
package de.fraunhofer.iosb.ilt.frostserver.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.FeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.HistoricalLocationBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.LocationBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.MultiDatastreamBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.ObservationBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.ObservedPropertyBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.SensorBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.UnitOfMeasurementBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class EntityFormatterTest {

    @Test
    public void writeThingBasic() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        Thing entity = new ThingBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Things(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "Things(1)/Locations"))
                .setDatastreams(new EntitySetImpl(EntityType.DATASTREAM, "Things(1)/Datastreams"))
                .setHistoricalLocations(new EntitySetImpl(EntityType.THING, "Things(1)/HistoricalLocations"))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeThingSelect() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        Thing entity = new ThingBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Things(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "Things(1)/Locations"))
                .setDatastreams(new EntitySetImpl(EntityType.DATASTREAM, "Things(1)/Datastreams"))
                .setHistoricalLocations(new EntitySetImpl(EntityType.THING, "Things(1)/HistoricalLocations"))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .build();
        Set<Property> selectedProps = new HashSet<>();
        selectedProps.add(EntityProperty.ID);
        selectedProps.add(EntityProperty.NAME);
        entity.setSelectedProperties(selectedProps);
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeThingsBasic() throws IOException {
        String thing
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        String expResult
                = "{ \"value\":[\n"
                + thing + ",\n"
                + thing
                + "]}";
        Thing entity = new ThingBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Things(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "Things(1)/Locations"))
                .setDatastreams(new EntitySetImpl(EntityType.DATASTREAM, "Things(1)/Datastreams"))
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Things(1)/HistoricalLocations"))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .build();
        EntitySet<Thing> things = new EntitySetImpl<>(EntityType.THING);
        things.add(entity);
        things.add(entity);
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntityCollection(things)));
    }

    @Test
    public void writeThingCompletelyEmpty() throws IOException {
        String expResult
                = "{}";
        Thing entity = new ThingBuilder()
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeThingsWithExpandedDatastream() throws IOException {
        String thing
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams@iot.count\":1,\n"
                + "\"Datastreams\": [\n"
                + "{\n"
                + "	\"@iot.id\":1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(1)\",\n"
                + "	\"name\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"description\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"unitOfMeasurement\": {\n"
                + "		\"name\": \"degree Celsius\",\n"
                + "		\"symbol\": \"°C\",\n"
                + "		\"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\n"
                + "	},\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "	\"observedArea\": {\n"
                + "		\"type\": \"Polygon\",\n"
                + "		\"coordinates\": [[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]\n"
                + "	},\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\""
                + "}\n"
                + "],\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        String expResult
                = "{ "
                + "\"@iot.count\": 1,\n"
                + "\"value\":[\n"
                + thing
                + "]}";
        Thing entity = new ThingBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Things(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "Things(1)/Locations"))
                .addDatastream(new DatastreamBuilder()
                        .setId(new IdLong(1))
                        .setSelfLink("http://example.org/v1.0/Datastreams(1)")
                        .setName("This is a datastream measuring the temperature in an oven.")
                        .setDescription("This is a datastream measuring the temperature in an oven.")
                        .setUnitOfMeasurement(new UnitOfMeasurementBuilder()
                                .setName("degree Celsius")
                                .setSymbol("°C")
                                .setDefinition("http://unitsofmeasure.org/ucum.html#para-30")
                                .build())
                        .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                        .setObservedArea(TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                        .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                        .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                        .build())
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Things(1)/HistoricalLocations"))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .build();
        entity.getDatastreams().setCount(1);
        EntitySet<Thing> things = new EntitySetImpl<>(EntityType.THING);
        things.add(entity);
        things.setCount(1);
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntityCollection(things)));
    }

    @Test
    public void writeThingWithExpandedDatastream1() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123}],\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        Thing entity = new ThingBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Things(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "Things(1)/Locations"))
                .addDatastream(new DatastreamBuilder()
                        .setId(new IdLong(123))
                        .build())
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Things(1)/HistoricalLocations"))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeThingWithExpandedDatastream2() throws IOException {
        String expResult = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123}],\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        Thing entity = new ThingBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Things(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "Things(1)/Locations"))
                .addDatastream(new DatastreamBuilder()
                        .setId(new IdLong(123))
                        .build())
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Things(1)/HistoricalLocations"))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .build();
        entity.setSelectedPropertyNames(new HashSet<>(Arrays.asList(EntityProperty.ID.getJsonName(), "name", "Locations")));
        entity.getDatastreams().setExportObject(true);
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeThingWithExpandedDatastream3() throws IOException {
        String expResult = "{\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123}],\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        Thing entity = new ThingBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Things(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "Things(1)/Locations"))
                .addDatastream(new DatastreamBuilder()
                        .setId(new IdLong(123))
                        .build())
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Things(1)/HistoricalLocations"))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .build();
        entity.setSelectedPropertyNames(new HashSet<>(Arrays.asList(EntityProperty.SELFLINK.getJsonName(), "name", "Locations")));
        entity.getDatastreams().setExportObject(true);
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeThingWithExpandedDatastream4() throws IOException {
        String expResult = "{\n"
                + "  \"@iot.id\": 1,\n"
                + "  \"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "  \"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "  \"Datastreams\": [],\n"
                + "  \"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "  \"name\": \"This thing is an oven.\",\n"
                + "  \"description\": \"This thing is an oven.\",\n"
                + "  \"properties\": {\n"
                + "    \"owner\": \"John Doe\",\n"
                + "    \"color\": \"Silver\"\n"
                + "  }\n"
                + "}";
        Thing entity = new ThingBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Things(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "Things(1)/Locations"))
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Things(1)/HistoricalLocations"))
                .setName("This thing is an oven.")
                .setDescription("This thing is an oven.")
                .addProperty("owner", "John Doe")
                .addProperty("color", "Silver")
                .build();
        entity.getDatastreams().setExportObject(true);
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeLocationBasic() throws IOException {
        {
            String expResult
                    = "{\n"
                    + "	\"@iot.id\": 1,\n"
                    + "	\"@iot.selfLink\": \"http://example.org/v1.0/Locations(1)\",\n"
                    + "	\"Things@iot.navigationLink\": \"Locations(1)/Things\",\n"
                    + "	\"HistoricalLocations@iot.navigationLink\": \"Locations(1)/HistoricalLocations\",\n"
                    + "	\"encodingType\": \"application/vnd.geo+json\""
                    + "}";
            Entity entity = new LocationBuilder()
                    .setId(new IdLong(1))
                    .setSelfLink("http://example.org/v1.0/Locations(1)")
                    .setThings(new EntitySetImpl(EntityType.THING, "Locations(1)/Things"))
                    .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Locations(1)/HistoricalLocations"))
                    .setEncodingType("application/vnd.geo+json")
                    .build();
            Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
        }
        {
            String expResult
                    = "{\n"
                    + "	\"@iot.id\": 1,\n"
                    + "	\"@iot.selfLink\": \"http://example.org/v1.0/Locations(1)\",\n"
                    + "	\"Things@iot.navigationLink\": \"Locations(1)/Things\",\n"
                    + "	\"HistoricalLocations@iot.navigationLink\": \"Locations(1)/HistoricalLocations\",\n"
                    + "	\"encodingType\": \"application/geo+json\""
                    + "}";
            Entity entity = new LocationBuilder()
                    .setId(new IdLong(1))
                    .setSelfLink("http://example.org/v1.0/Locations(1)")
                    .setThings(new EntitySetImpl(EntityType.THING, "Locations(1)/Things"))
                    .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Locations(1)/HistoricalLocations"))
                    .setEncodingType("application/geo+json")
                    .build();
            Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
        }
    }

    @Test
    public void writeLocationWithGeoJsonLocation() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Locations(1)\",\n"
                + "	\"Things@iot.navigationLink\": \"Locations(1)/Things\",\n"
                + "	\"HistoricalLocations@iot.navigationLink\": \"Locations(1)/HistoricalLocations\",\n"
                + "	\"encodingType\": \"application/vnd.geo+json\""
                + ",\n"
                + "	\"location\": \n"
                + "	{\n"
                + "		\"type\": \"Feature\",\n"
                + "		\"geometry\":\n"
                + "		{\n"
                + "			\"type\": \"Point\",\n"
                + "			\"coordinates\": [-114.06,51.05]\n"
                + "		}\n"
                + "	}\n"
                + "}";
        Entity entity = new LocationBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Locations(1)")
                .setThings(new EntitySetImpl(EntityType.THING, "Locations(1)/Things"))
                .setHistoricalLocations(new EntitySetImpl(EntityType.HISTORICALLOCATION, "Locations(1)/HistoricalLocations"))
                .setEncodingType("application/vnd.geo+json")
                .setLocation(TestHelper.getFeatureWithPoint(-114.06, 51.05))
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeHistoricalLocationBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/HistoricalLocations(1)\",\n"
                + "	\"Locations@iot.navigationLink\": \"HistoricalLocations(1)/Locations\",\n"
                + "	\"Thing@iot.navigationLink\": \"HistoricalLocations(1)/Thing\",\n"
                + "	\"time\": \"2015-01-25T19:00:00.000Z\"\n"
                + "}";
        Entity entity = new HistoricalLocationBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/HistoricalLocations(1)")
                .setLocations(new EntitySetImpl(EntityType.LOCATION, "HistoricalLocations(1)/Locations"))
                .setThing(new ThingBuilder().setNavigationLink("HistoricalLocations(1)/Thing").build())
                .setTime(TestHelper.createTimeInstant(2015, 01, 25, 12, 0, 0, DateTimeZone.forOffsetHours(-7), DateTimeZone.UTC))
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeDatastreamBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"HistoricalLocations(1)/Thing\",\n"
                + "	\"Sensor@iot.navigationLink\": \"Datastreams(1)/Sensor\",\n"
                + "	\"ObservedProperty@iot.navigationLink\": \"Datastreams(1)/ObservedProperty\",\n"
                + "	\"Observations@iot.navigationLink\": \"Datastreams(1)/Observations\",\n"
                + "	\"name\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"description\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"name\": \"degree Celsius\",\n"
                + "		\"symbol\": \"°C\",\n"
                + "		\"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\n"
                + "	},\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement\",\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\"\n"
                + "}";
        Entity entity = new DatastreamBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Datastreams(1)")
                .setThing(new ThingBuilder().setNavigationLink("HistoricalLocations(1)/Thing").build())
                .setSensor(new SensorBuilder().setNavigationLink("Datastreams(1)/Sensor").build())
                .setObservedProperty(new ObservedPropertyBuilder().setNavigationLink("Datastreams(1)/ObservedProperty").build())
                .setObservations(new EntitySetImpl(EntityType.OBSERVATION, "Datastreams(1)/Observations"))
                .setName("This is a datastream measuring the temperature in an oven.")
                .setDescription("This is a datastream measuring the temperature in an oven.")
                .setUnitOfMeasurement(new UnitOfMeasurementBuilder()
                        .setName("degree Celsius")
                        .setSymbol("°C")
                        .setDefinition("http://unitsofmeasure.org/ucum.html#para-30")
                        .build())
                .setObservationType("http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeDatastreamWithEmptyUnitOfMeasurement() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"HistoricalLocations(1)/Thing\",\n"
                + "	\"Sensor@iot.navigationLink\": \"Datastreams(1)/Sensor\",\n"
                + "	\"ObservedProperty@iot.navigationLink\": \"Datastreams(1)/ObservedProperty\",\n"
                + "	\"Observations@iot.navigationLink\": \"Datastreams(1)/Observations\",\n"
                + "	\"name\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"description\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"name\": null,\n"
                + "		\"symbol\": null,\n"
                + "		\"definition\": null\n"
                + "	},\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement\",\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\"\n"
                + "}";
        Entity entity = new DatastreamBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Datastreams(1)")
                .setThing(new ThingBuilder().setNavigationLink("HistoricalLocations(1)/Thing").build())
                .setSensor(new SensorBuilder().setNavigationLink("Datastreams(1)/Sensor").build())
                .setObservedProperty(new ObservedPropertyBuilder().setNavigationLink("Datastreams(1)/ObservedProperty").build())
                .setObservations(new EntitySetImpl(EntityType.OBSERVATION, "Datastreams(1)/Observations"))
                .setUnitOfMeasurement(new UnitOfMeasurement())
                .setName("This is a datastream measuring the temperature in an oven.")
                .setDescription("This is a datastream measuring the temperature in an oven.")
                .setObservationType("http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeDatastreamWithObservedAreaGeoJsonPolygon() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"HistoricalLocations(1)/Thing\",\n"
                + "	\"Sensor@iot.navigationLink\": \"Datastreams(1)/Sensor\",\n"
                + "	\"ObservedProperty@iot.navigationLink\": \"Datastreams(1)/ObservedProperty\",\n"
                + "	\"Observations@iot.navigationLink\": \"Datastreams(1)/Observations\",\n"
                + "	\"name\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"description\": \"This is a datastream measuring the temperature in an oven.\",\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"name\": \"degree Celsius\",\n"
                + "		\"symbol\": \"°C\",\n"
                + "		\"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\n"
                + "	},\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement\",\n"
                + "	\"observedArea\": \n"
                + "	{\n"
                + "		\"type\": \"Polygon\",\n"
                + "		\"coordinates\": [[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]\n"
                + "	},\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\"\n"
                + "}";
        Entity entity = new DatastreamBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Datastreams(1)")
                .setThing(new ThingBuilder().setNavigationLink("HistoricalLocations(1)/Thing").build())
                .setSensor(new SensorBuilder().setNavigationLink("Datastreams(1)/Sensor").build())
                .setObservedProperty(new ObservedPropertyBuilder().setNavigationLink("Datastreams(1)/ObservedProperty").build())
                .setObservations(new EntitySetImpl(EntityType.OBSERVATION, "Datastreams(1)/Observations"))
                .setName("This is a datastream measuring the temperature in an oven.")
                .setDescription("This is a datastream measuring the temperature in an oven.")
                .setUnitOfMeasurement(new UnitOfMeasurementBuilder()
                        .setName("degree Celsius")
                        .setSymbol("°C")
                        .setDefinition("http://unitsofmeasure.org/ucum.html#para-30")
                        .build())
                .setObservationType("http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setObservedArea(TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeMultiDatastreamBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/MultiDatastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"HistoricalLocations(1)/Thing\",\n"
                + "	\"Sensor@iot.navigationLink\": \"MultiDatastreams(1)/Sensor\",\n"
                + "	\"ObservedProperties@iot.navigationLink\": \"MultiDatastreams(1)/ObservedProperties\",\n"
                + "	\"Observations@iot.navigationLink\": \"MultiDatastreams(1)/Observations\",\n"
                + "	\"name\": \"This is a datastream measuring the wind.\",\n"
                + "	\"description\": \"This is a datastream measuring wind direction and speed.\",\n"
                + " \"unitOfMeasurements\": [\n"
                + "  {\n"
                + "   \"name\": \"DegreeAngle\",\n"
                + "   \"symbol\": \"deg\",\n"
                + "   \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle\"\n"
                + "  },\n"
                + "  {\n"
                + "   \"name\": \"MeterPerSecond\",\n"
                + "   \"symbol\": \"m/s\",\n"
                + "   \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond\"\n"
                + "  }\n"
                + " ],\n"
                + "	\"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation\",\n"
                + " \"multiObservationDataTypes\": [\n"
                + "  \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\"\n"
                + " ],\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\"\n"
                + "}";
        Entity entity = new MultiDatastreamBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/MultiDatastreams(1)")
                .setThing(new ThingBuilder().setNavigationLink("HistoricalLocations(1)/Thing").build())
                .setSensor(new SensorBuilder().setNavigationLink("MultiDatastreams(1)/Sensor").build())
                .setObservations(new EntitySetImpl(EntityType.OBSERVATION, "MultiDatastreams(1)/Observations"))
                .setObservedProperties(new EntitySetImpl(EntityType.OBSERVEDPROPERTY, "MultiDatastreams(1)/ObservedProperties"))
                .setName("This is a datastream measuring the wind.")
                .setDescription("This is a datastream measuring wind direction and speed.")
                .addUnitOfMeasurement(new UnitOfMeasurementBuilder()
                        .setName("DegreeAngle")
                        .setSymbol("deg")
                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle")
                        .build())
                .addUnitOfMeasurement(new UnitOfMeasurementBuilder()
                        .setName("MeterPerSecond")
                        .setSymbol("m/s")
                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond")
                        .build())
                .addObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .addObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setPhenomenonTime(TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .setResultTime(TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeSensorBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Sensors(1)\",\n"
                + "	\"Datastreams@iot.navigationLink\": \"Sensors(1)/Datastreams\",\n"
                + "	\"name\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"description\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"encodingType\": \"application/pdf\",\n"
                + "	\"metadata\": \"http://example.org/TMP35_36_37.pdf\"\n"
                + "}";
        Entity entity = new SensorBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Sensors(1)")
                .setDatastreams(new EntitySetImpl(EntityType.DATASTREAM, "Sensors(1)/Datastreams"))
                .setName("TMP36 - Analog Temperature sensor")
                .setDescription("TMP36 - Analog Temperature sensor")
                .setEncodingType("application/pdf")
                .setMetadata("http://example.org/TMP35_36_37.pdf")
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeSensorEmptyDatastreamsCollection() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Sensors(1)\",\n"
                + "	\"name\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"description\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"encodingType\": \"application/pdf\",\n"
                + "	\"metadata\": \"http://example.org/TMP35_36_37.pdf\"\n"
                + "}";
        Entity entity = new SensorBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Sensors(1)")
                .setDatastreams(new EntitySetImpl(EntityType.DATASTREAM))
                .setName("TMP36 - Analog Temperature sensor")
                .setDescription("TMP36 - Analog Temperature sensor")
                .setEncodingType("application/pdf")
                .setMetadata("http://example.org/TMP35_36_37.pdf")
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeObservedPropertyBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/ObservedProperties(1)\",\n"
                + "	\"Datastreams@iot.navigationLink\": \"ObservedProperties(1)/Datastreams\",\n"
                + "	\"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\",\n"
                + "	\"name\": \"DewPoint Temperature\",\n"
                + "	\"definition\": \"http://dbpedia.org/page/Dew_point\"\n"
                + "}";
        Entity entity = new ObservedPropertyBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/ObservedProperties(1)")
                .setDatastreams(new EntitySetImpl(EntityType.DATASTREAM, "ObservedProperties(1)/Datastreams"))
                .setDescription("The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.")
                .setName("DewPoint Temperature")
                .setDefinition("http://dbpedia.org/page/Dew_point")
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeObservationBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"Datastream@iot.navigationLink\":\"Observations(1)/Datastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59.000Z\",\n"
                + "	\"resultTime\": \"2014-12-31T19:59:59.000Z\",\n"
                + "	\"result\": 70.40\n"
                + "}";
        Entity entity = new ObservationBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Observations(1)")
                .setFeatureOfInterest(new FeatureOfInterestBuilder().setNavigationLink("Observations(1)/FeatureOfInterest").build())
                .setDatastream(new DatastreamBuilder().setNavigationLink("Observations(1)/Datastream").build())
                .setPhenomenonTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
                .setResultTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setResult(new BigDecimal("70.40"))
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeObservationBasicWithNullResult() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"Datastream@iot.navigationLink\":\"Observations(1)/Datastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59.000Z\",\n"
                + "	\"resultTime\": \"2014-12-31T19:59:59.000Z\",\n"
                + "	\"result\": null\n"
                + "}";
        Entity entity = new ObservationBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Observations(1)")
                .setFeatureOfInterest(new FeatureOfInterestBuilder().setNavigationLink("Observations(1)/FeatureOfInterest").build())
                .setDatastream(new DatastreamBuilder().setNavigationLink("Observations(1)/Datastream").build())
                .setPhenomenonTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
                .setResultTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setResult(null)
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeObservationWithEmptyResultTime() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"Datastream@iot.navigationLink\":\"Observations(1)/Datastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59.000Z\",\n"
                + "	\"resultTime\": null,\n"
                + "	\"result\": \"70.4\"\n"
                + "}";
        Entity entity = new ObservationBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Observations(1)")
                .setFeatureOfInterest(new FeatureOfInterestBuilder().setNavigationLink("Observations(1)/FeatureOfInterest").build())
                .setDatastream(new DatastreamBuilder().setNavigationLink("Observations(1)/Datastream").build())
                .setPhenomenonTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
                .setResultTime(new TimeInstant(null))
                .setResult("70.4")
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeObservationWithEmptyDatastream() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59.000Z\",\n"
                + "	\"resultTime\": \"2014-12-31T19:59:59.000Z\",\n"
                + "	\"result\": \"70.4\"\n"
                + "}";
        Entity entity = new ObservationBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/Observations(1)")
                .setFeatureOfInterest(new FeatureOfInterestBuilder().setNavigationLink("Observations(1)/FeatureOfInterest").build())
                .setDatastream(new DatastreamBuilder().build())
                .setPhenomenonTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
                .setResultTime(TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setResult("70.4")
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeFeatureOfInterstBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/FeaturesOfInterest(1)\",\n"
                + "	\"Observations@iot.navigationLink\": \"FeaturesOfInterest(1)/Observations\",\n"
                + "	\"name\": \"This is a weather station.\",\n"
                + "	\"description\": \"This is a weather station.\",\n"
                + "	\"encodingType\": \"application/vnd.geo+json\""
                + "}";
        Entity entity = new FeatureOfInterestBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/FeaturesOfInterest(1)")
                .setObservations(new EntitySetImpl(EntityType.OBSERVATION, "FeaturesOfInterest(1)/Observations"))
                .setName("This is a weather station.")
                .setDescription("This is a weather station.")
                .setEncodingType("application/vnd.geo+json")
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    @Test
    public void writeFeatureOfInterstWithGeoJsonPointFeature() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/FeaturesOfInterest(1)\",\n"
                + "	\"Observations@iot.navigationLink\": \"FeaturesOfInterest(1)/Observations\",\n"
                + "	\"name\": \"This is a weather station.\",\n"
                + "	\"description\": \"This is a weather station.\",\n"
                + "	\"encodingType\": \"application/vnd.geo+json\""
                + ",\n"
                + "	\"feature\": \n"
                + "	{\n"
                + "		\"type\": \"Feature\",\n"
                + "		\"geometry\":\n"
                + "		{\n"
                + "			\"type\": \"Point\",\n"
                + "			\"coordinates\": [-114.06,51.05]\n"
                + "		}\n"
                + "	}\n"
                + "}";
        Entity entity = new FeatureOfInterestBuilder()
                .setId(new IdLong(1))
                .setSelfLink("http://example.org/v1.0/FeaturesOfInterest(1)")
                .setObservations(new EntitySetImpl(EntityType.OBSERVATION, "FeaturesOfInterest(1)/Observations"))
                .setName("This is a weather station.")
                .setDescription("This is a weather station.")
                .setEncodingType("application/vnd.geo+json")
                .setFeature(TestHelper.getFeatureWithPoint(-114.06, 51.05))
                .build();
        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeEntity(entity)));
    }

    private boolean jsonEqual(String string1, String string2) {
        ObjectMapper mapper = SimpleJsonMapper.getSimpleObjectMapper();
        try {
            JsonNode json1 = mapper.readTree(string1);
            JsonNode json2 = mapper.readTree(string2);
            return json1.equals(json2);
        } catch (IOException ex) {
            Logger.getLogger(EntityFormatterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
