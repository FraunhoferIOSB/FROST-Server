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
package de.fraunhofer.iosb.ilt.frostserver.deserialize;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class EntityParserTest {

    private JsonReader entityParser;

    @Before
    public void setUp() {
        entityParser = new JsonReader(IdLong.class);
    }

    @Test
    public void readDatastreamBasic() throws IOException {
        String json = "{\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"symbol\": \"%\",\n"
                + "		\"name\": \"Percentage\",\n"
                + "		\"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html\"\n"
                + "	},\n"
                + "	\"observationType\":\"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "	\"name\": \"Temperature measurement\",\n"
                + "	\"description\": \"Temperature measurement\",\n"
                + "	\"Thing\": {\"@iot.id\": 5394817},\n"
                + "	\"ObservedProperty\": {\"@iot.id\": 5394816},\n"
                + "	\"Sensor\": {\"@iot.id\": " + Long.MAX_VALUE + "}\n"
                + "}";
        Datastream expectedResult = new Datastream()
                .setUnitOfMeasurement(
                        new UnitOfMeasurement()
                                .setSymbol("%")
                                .setName("Percentage")
                                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html")
                )
                .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setName("Temperature measurement")
                .setDescription("Temperature measurement")
                .setThing(new Thing().setId(new IdLong(5394817)))
                .setObservedProperty(new ObservedProperty().setId(new IdLong(5394816)))
                .setSensor(new Sensor().setId(new IdLong(Long.MAX_VALUE)));
        assertEquals(expectedResult, entityParser.parseDatastream(json));
    }

    @Test
    public void readDatastreamWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"symbol\": \"%\",\n"
                + "		\"name\": \"Percentage\",\n"
                + "		\"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html\"\n"
                + "	},\n"
                + "	\"observationType\":\"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "	\"name\": \"Temperature measurement\",\n"
                + "	\"description\": \"Temperature measurement\",\n"
                + "	\"Thing\": {\"@iot.id\": 5394817},\n"
                + "	\"ObservedProperty\": {\"@iot.id\": 5394816},\n"
                + "	\"Sensor\": {\"@iot.id\": 5394815},\n"
                + "	\"observedArea\": {\n"
                + "		\"type\": \"Polygon\",\n"
                + "		\"coordinates\": [[[100,0],[101,0],[101,1],[100,1],[100,0]]]\n"
                + "	},\n"
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\"\n"
                + "}";
        Datastream result = entityParser.parseDatastream(json);
        Assert.assertTrue(result.isSetUnitOfMeasurement()
                && result.isSetObservationType()
                && result.isSetName()
                && result.isSetDescription()
                && result.isSetThing()
                && result.isSetObservedProperty()
                && result.isSetSensor()
                && result.isSetObservedArea()
                && result.isSetPhenomenonTime()
                && result.isSetResultTime());
    }

    @Test
    public void readDatastreamWithAllValuesMissing() throws IOException {
        String json = "{}";
        Datastream result = entityParser.parseDatastream(json);
        Assert.assertTrue(!result.isSetUnitOfMeasurement()
                && !result.isSetObservationType()
                && !result.isSetName()
                && !result.isSetDescription()
                && !result.isSetThing()
                && !result.isSetObservedProperty()
                && !result.isSetSensor()
                && !result.isSetObservedArea()
                && !result.isSetPhenomenonTime()
                && !result.isSetResultTime());
    }

    @Test
    public void readDatastreamWithObservedAreaGeoJsonPolygon() throws IOException {
        String json = "{\n"
                + "	\"unitOfMeasurement\": \n"
                + "	{\n"
                + "		\"symbol\": \"%\",\n"
                + "		\"name\": \"Percentage\",\n"
                + "		\"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html\"\n"
                + "	},\n"
                + "	\"observationType\":\"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "	\"name\": \"Temperature measurement\",\n"
                + "	\"description\": \"Temperature measurement\",\n"
                + "	\"Thing\": {\"@iot.id\": 5394817},\n"
                + "	\"ObservedProperty\": {\"@iot.id\": 5394816},\n"
                + "	\"Sensor\": {\"@iot.id\": 5394815},\n"
                + "	\"observedArea\": {\n"
                + "		\"type\": \"Polygon\",\n"
                + "		\"coordinates\": [[[100,0],[101,0],[101,1],[100,1],[100,0]]]\n"
                + "	}\n"
                + "}";
        Datastream expectedResult = new Datastream()
                .setUnitOfMeasurement(
                        new UnitOfMeasurement()
                                .setSymbol("%")
                                .setName("Percentage")
                                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html")
                )
                .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setName("Temperature measurement")
                .setDescription("Temperature measurement")
                .setThing(new Thing().setId(new IdLong(5394817)))
                .setObservedProperty(new ObservedProperty().setId(new IdLong(5394816)))
                .setSensor(new Sensor().setId(new IdLong(5394815)))
                .setObservedArea(TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0));
        assertEquals(expectedResult, entityParser.parseDatastream(json));
    }

    @Test
    public void readDatastreamWithObservedPropertyAndSensor() throws IOException {
        String json = "{\n"
                + "    \"unitOfMeasurement\": {\n"
                + "        \"name\": \"Celsius\",\n"
                + "        \"symbol\": \"C\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius\"\n"
                + "    },\n"
                + "    \"name\": \"Temperature measurement\",\n"
                + "    \"description\": \"Temperature measurement\",\n"
                + "    \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "    \"ObservedProperty\": {\n"
                + "        \"name\": \"Temperature\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature\",\n"
                + "        \"description\": \"Temperature of the camping site\"\n"
                + "    },\n"
                + "    \"Sensor\": {\n"
                + "        \"description\": \"Sensor 101\",\n"
                + "        \"encodingType\": \"http://schema.org/description\",\n"
                + "        \"metadata\": \"Calibration date:  2011-11-11\"\n"
                + "    }\n"
                + "}";
        Datastream expectedResult = new Datastream()
                .setUnitOfMeasurement(
                        new UnitOfMeasurement()
                                .setName("Celsius")
                                .setSymbol("C")
                                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius")
                )
                .setName("Temperature measurement")
                .setDescription("Temperature measurement")
                .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setObservedProperty(
                        new ObservedProperty()
                                .setName("Temperature")
                                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                .setDescription("Temperature of the camping site")
                )
                .setSensor(
                        new Sensor()
                                .setDescription("Sensor 101")
                                .setEncodingType("http://schema.org/description")
                                .setMetadata("Calibration date:  2011-11-11")
                );
        assertEquals(expectedResult, entityParser.parseDatastream(json));
    }

    @Test
    public void readMultiDatastreamWithObservedPropertyAndSensor() throws IOException {
        String json = "{\n"
                + "    \"unitOfMeasurements\": [\n"
                + "        {\n"
                + "            \"name\": \"DegreeAngle\",\n"
                + "            \"symbol\": \"deg\",\n"
                + "            \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"MeterPerSecond\",\n"
                + "            \"symbol\": \"m/s\",\n"
                + "            \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"name\": \"Wind\",\n"
                + "    \"description\": \"Wind direction and speed\",\n"
                + "    \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation\",\n"
                + "    \"multiObservationDataTypes\": [\n"
                + "        \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "        \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\"\n"
                + "    ],\n"
                + "    \"ObservedProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"Wind Direction\",\n"
                + "            \"definition\": \"SomeDefinition\",\n"
                + "            \"description\": \"Direction the wind blows, 0=North, 90=East.\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"Wind Speed\",\n"
                + "            \"definition\": \"SomeDefinition\",\n"
                + "            \"description\": \"Wind Speed\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"Sensor\": {\n"
                + "        \"description\": \"Wind Sensor 101\",\n"
                + "        \"encodingType\": \"http://schema.org/description\",\n"
                + "        \"metadata\": \"Calibration date:  2011-11-11\"\n"
                + "    }\n"
                + "}";
        List<UnitOfMeasurement> unitsOfMeasurement = new ArrayList<>();
        unitsOfMeasurement.add(new UnitOfMeasurement()
                .setName("DegreeAngle")
                .setSymbol("deg")
                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle")
        );
        unitsOfMeasurement.add(new UnitOfMeasurement()
                .setName("MeterPerSecond")
                .setSymbol("m/s")
                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond")
        );
        List<String> observationTypes = new ArrayList<>();
        observationTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        observationTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        MultiDatastream expectedResult = new MultiDatastream()
                .setUnitOfMeasurements(unitsOfMeasurement)
                .setName("Wind")
                .setDescription("Wind direction and speed")
                .setMultiObservationDataTypes(observationTypes)
                .addObservedProperty(
                        new ObservedProperty()
                                .setName("Wind Direction")
                                .setDefinition("SomeDefinition")
                                .setDescription("Direction the wind blows, 0=North, 90=East.")
                )
                .addObservedProperty(
                        new ObservedProperty()
                                .setName("Wind Speed")
                                .setDefinition("SomeDefinition")
                                .setDescription("Wind Speed")
                )
                .setSensor(
                        new Sensor()
                                .setDescription("Wind Sensor 101")
                                .setEncodingType("http://schema.org/description")
                                .setMetadata("Calibration date:  2011-11-11")
                );
        assertEquals(expectedResult, entityParser.parseMultiDatastream(json));
    }

    @Test
    public void readFeatureOfInterstBasic() throws IOException {
        {
            String json = "{\n"
                    + "    \"name\": \"Underground Air Quality in NYC train tunnels\",\n"
                    + "    \"description\": \"Underground Air Quality in NYC train tunnels\",\n"
                    + "    \"encodingType\": \"application/vnd.geo+json\",\n"
                    + "    \"feature\": {\n"
                    + "        \"coordinates\": [51.08386,-114.13036],\n"
                    + "        \"type\": \"Point\"\n"
                    + "      }\n"
                    + "}";
            FeatureOfInterest expectedResult = new FeatureOfInterest()
                    .setName("Underground Air Quality in NYC train tunnels")
                    .setDescription("Underground Air Quality in NYC train tunnels")
                    .setEncodingType("application/vnd.geo+json")
                    .setFeature(TestHelper.getPoint(51.08386, -114.13036));
            assertEquals(expectedResult, entityParser.parseFeatureOfInterest(json));
        }
        {
            String json = "{\n"
                    + "    \"name\": \"Underground Air Quality in NYC train tunnels\",\n"
                    + "    \"description\": \"Underground Air Quality in NYC train tunnels\",\n"
                    + "    \"encodingType\": \"application/geo+json\",\n"
                    + "    \"feature\": {\n"
                    + "        \"coordinates\": [51.08386,-114.13036],\n"
                    + "        \"type\": \"Point\"\n"
                    + "      }\n"
                    + "}";
            FeatureOfInterest expectedResult = new FeatureOfInterest()
                    .setName("Underground Air Quality in NYC train tunnels")
                    .setDescription("Underground Air Quality in NYC train tunnels")
                    .setEncodingType("application/geo+json")
                    .setFeature(TestHelper.getPoint(51.08386, -114.13036));
            assertEquals(expectedResult, entityParser.parseFeatureOfInterest(json));
        }
    }

    @Test
    public void readFeatureOfInterstWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"Underground Air Quality in NYC train tunnels\",\n"
                + "    \"description\": \"Underground Air Quality in NYC train tunnels\",\n"
                + "    \"encodingType\": \"application/vnd.geo+json\",\n"
                + "    \"feature\": {\n"
                + "        \"coordinates\": [51.08386,-114.13036],\n"
                + "        \"type\": \"Point\"\n"
                + "      }\n"
                + "}";
        FeatureOfInterest result = entityParser.parseFeatureOfInterest(json);
        Assert.assertTrue(result.isSetDescription()
                && result.isSetName()
                && result.isSetEncodingType()
                && result.isSetFeature());
    }

    @Test
    public void readFeatureOfInterstWithAllValuesMissing() throws IOException {
        String json = "{}";
        FeatureOfInterest result = entityParser.parseFeatureOfInterest(json);
        Assert.assertTrue(!result.isSetDescription()
                && !result.isSetName()
                && !result.isSetEncodingType()
                && !result.isSetFeature());
    }

    @Test
    public void readLocationBasic() throws IOException {
        String json = "{\n"
                + "    \"name\": \"my backyard\",\n"
                + "    \"description\": \"my backyard\",\n"
                + "    \"encodingType\": \"application/vnd.geo+json\",\n"
                + "    \"location\": {\n"
                + "        \"type\": \"Point\",\n"
                + "        \"coordinates\": [-117.123,\n"
                + "        54.123]\n"
                + "    }\n"
                + "}";
        Location expectedResult = new Location()
                .setName("my backyard")
                .setDescription("my backyard")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(TestHelper.getPoint(-117.123, 54.123));
        assertEquals(expectedResult, entityParser.parseLocation(json));
    }

    @Test
    public void readLocationWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"my backyard\",\n"
                + "    \"description\": \"my backyard\",\n"
                + "    \"encodingType\": \"application/vnd.geo+json\",\n"
                + "    \"location\": {\n"
                + "        \"type\": \"Point\",\n"
                + "        \"coordinates\": [-117.123,\n"
                + "        54.123]\n"
                + "    }\n"
                + "}";
        Location result = entityParser.parseLocation(json);
        Assert.assertTrue(result.isSetDescription()
                && result.isSetName()
                && result.isSetEncodingType()
                && result.isSetLocation());
    }

    @Test
    public void readLocationWithAllValuesMissing() throws IOException {
        String json = "{}";
        Location result = entityParser.parseLocation(json);
        Assert.assertTrue(!result.isSetDescription()
                && !result.isSetName()
                && !result.isSetEncodingType()
                && !result.isSetLocation());
    }

    @Test
    public void readLocationWithLinkedThings() throws IOException {
        String json = "{\n"
                + "    \"name\": \"my backyard\",\n"
                + "    \"description\": \"my backyard\",\n"
                + "    \"encodingType\": \"application/vnd.geo+json\",\n"
                + "    \"location\": {\n"
                + "        \"type\": \"Point\",\n"
                + "        \"coordinates\": [-117.123,\n"
                + "        54.123]\n"
                + "    },"
                + "    \"Things\":[{\"@iot.id\":100}]\n"
                + "}";
        Thing thing = new Thing().setId(new IdLong(100));
        EntitySet<Thing> things = new EntitySetImpl<>(EntityType.THING);
        things.add(thing);
        Location expectedResult = new Location()
                .setName("my backyard")
                .setDescription("my backyard")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(TestHelper.getPoint(-117.123, 54.123))
                .setThings(things);
        assertEquals(expectedResult, entityParser.parseLocation(json));
    }

    @Test
    public void readObservationWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"Datastream\":{\"@iot.id\":100},\n"
                + "  \"FeatureOfInterest\":{\"@iot.id\": 14269},\n"
                + "  \"parameters\":{\"param1\": \"some value1\", \"param2\": \"some value2\"},\n"
                + "  \"phenomenonTime\": \"2015-09-01T14:22:05.149Z\",\n"
                + "  \"resultQuality\": \"none\",\n"
                + "  \"validTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\"\n"
                + "}";
        Observation result = entityParser.parseObservation(json);
        Assert.assertTrue(result.isSetPhenomenonTime()
                && result.isSetResultTime()
                && result.isSetResult()
                && result.isSetDatastream()
                && result.isSetFeatureOfInterest()
                && result.isSetParameters()
                && result.isSetPhenomenonTime()
                && result.isSetResultQuality()
                && result.isSetValidTime());
    }

    @Test
    public void readObservationWithAllValuesMissing() throws IOException {
        String json = "{}";
        Observation result = entityParser.parseObservation(json);
        Assert.assertTrue(!result.isSetPhenomenonTime()
                && !result.isSetResultTime()
                && !result.isSetResult()
                && !result.isSetDatastream()
                && !result.isSetFeatureOfInterest()
                && !result.isSetParameters()
                && !result.isSetPhenomenonTime()
                && !result.isSetResultQuality()
                && !result.isSetValidTime());
    }

    @Test
    public void readObservationWithLinks() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"Datastream\":{\"@iot.id\":100}\n"
                + "}";
        Observation expectedResult = new Observation()
                .setPhenomenonTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setResultTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setResult(38)
                .setDatastream(new Datastream().setId(new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseObservation(json));

        json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"MultiDatastream\":{\"@iot.id\":100}\n"
                + "}";
        expectedResult = new Observation()
                .setPhenomenonTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setResultTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setResult(38)
                .setMultiDatastream(new MultiDatastream().setId(new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseObservation(json));
    }

    @Test
    public void readObservationWithLinkedFeatureOfInterest() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"FeatureOfInterest\":{\"@iot.id\": 14269}\n"
                + "}";
        Observation expectedResult = new Observation()
                .setPhenomenonTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setResultTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setResult(38)
                .setFeatureOfInterest(new FeatureOfInterest().setId(new IdLong(14269)));
        assertEquals(expectedResult, entityParser.parseObservation(json));
    }

    @Test
    public void readObservationWithFeatureOfInterest() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                //                + "  \"resultQuality\" : \"Very Good\",\n"
                + "  \"result\" : 99,\n"
                + "  \"FeatureOfInterest\": {\n"
                + "    \"name\": \"Turn 5, track surface temperature\",\n"
                + "    \"description\": \"Turn 5, track surface temperature\",\n"
                + "    \"encodingType\": \"http://example.org/measurement_types#Measure\",\n"
                + "    \"feature\": \"tarmac temperature\"\n"
                + "  },\n"
                + "  \"Datastream\":{\"@iot.id\": 14314}\n"
                + "}";
        Observation expectedResult = new Observation()
                .setPhenomenonTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setResultTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setResult(99)
                .setFeatureOfInterest(new FeatureOfInterest()
                        .setName("Turn 5, track surface temperature")
                        .setDescription("Turn 5, track surface temperature")
                        .setEncodingType("http://example.org/measurement_types#Measure")
                        .setFeature("tarmac temperature")
                )
                .setDatastream(new Datastream().setId(new IdLong(14314)));
        assertEquals(expectedResult, entityParser.parseObservation(json));
    }

    @Test
    public void readObservationPrecision() throws IOException {
        String json = "{\n"
                + "  \"result\" : 100.00\n"
                + "}";
        Observation expectedResult = new Observation()
                .setResult(new BigDecimal("100.00"));
        Observation result = entityParser.parseObservation(json);
        assertEquals(expectedResult, result);

        json = "{\n"
                + "  \"result\" : 0.00\n"
                + "}";
        expectedResult = new Observation()
                .setResult(new BigDecimal("0.00"));
        result = entityParser.parseObservation(json);
        assertEquals(expectedResult, result);
    }

    @Test
    public void readObservedPropertyBasic() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        ObservedProperty expectedResult = new ObservedProperty()
                .setName("ObservedPropertyUp Tempomatic 2000")
                .setDescription("http://schema.org/description")
                .setDefinition("Calibration date:  Jan 1, 2014");
        assertEquals(expectedResult, entityParser.parseObservedProperty(json));
    }

    @Test
    public void readObservedPropertyWithLinks() throws IOException {
        String json = "{\n"
                + "    \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "    \"description\": \"http://schema.org/description\",\n"
                + "    \"definition\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        ObservedProperty expectedResult = new ObservedProperty()
                .setName("ObservedPropertyUp Tempomatic 2000")
                .setDescription("http://schema.org/description")
                .setDefinition("Calibration date:  Jan 1, 2014")
                .addDatastream(new Datastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseObservedProperty(json));

        json = "{\n"
                + "    \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "    \"description\": \"http://schema.org/description\",\n"
                + "    \"definition\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new ObservedProperty()
                .setName("ObservedPropertyUp Tempomatic 2000")
                .setDescription("http://schema.org/description")
                .setDefinition("Calibration date:  Jan 1, 2014")
                .addMultiDatastream(new MultiDatastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseObservedProperty(json));

        json = "{\n"
                + "    \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "    \"description\": \"http://schema.org/description\",\n"
                + "    \"definition\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ],\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new ObservedProperty()
                .setName("ObservedPropertyUp Tempomatic 2000")
                .setDescription("http://schema.org/description")
                .setDefinition("Calibration date:  Jan 1, 2014")
                .addDatastream(new Datastream()
                        .setId(new IdLong(100))
                )
                .addMultiDatastream(new MultiDatastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseObservedProperty(json));
    }

    @Test
    public void readObservedPropertyWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        ObservedProperty result = entityParser.parseObservedProperty(json);
        Assert.assertTrue(result.isSetName()
                && result.isSetDescription()
                && result.isSetDefinition());
    }

    @Test
    public void readObservedPropertyWithAllValuesMissing() throws IOException {
        String json = "{}";
        ObservedProperty result = entityParser.parseObservedProperty(json);
        Assert.assertTrue(!result.isSetName()
                && !result.isSetDescription()
                && !result.isSetDefinition());
    }

    @Test
    public void readSensorBasic() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Sensor expectedResult = new Sensor()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014");
        assertEquals(expectedResult, entityParser.parseSensor(json));
    }

    @Test
    public void readSensorWithLinks() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        Sensor expectedResult = new Sensor()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014")
                .addDatastream(new Datastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseSensor(json));

        json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100},\n"
                + "        {\"@iot.id\":101}\n"
                + "    ]\n"
                + "}";
        expectedResult = new Sensor()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014")
                .addDatastream(
                        new Datastream()
                                .setId(new IdLong(100)))
                .addDatastream(
                        new Datastream()
                                .setId(new IdLong(101)));
        assertEquals(expectedResult, entityParser.parseSensor(json));

        json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new Sensor()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014")
                .addMultiDatastream(new MultiDatastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseSensor(json));

        json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ],\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new Sensor()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014")
                .addDatastream(new Datastream()
                        .setId(new IdLong(100))
                )
                .addMultiDatastream(new MultiDatastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseSensor(json));
    }

    @Test
    public void readSensorWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Sensor result = entityParser.parseSensor(json);
        Assert.assertTrue(result.isSetDescription()
                && result.isSetName()
                && result.isSetEncodingType()
                && result.isSetMetadata());
    }

    @Test
    public void readSensorWithAllValuesMissing() throws IOException {
        String json = "{}";
        Sensor result = entityParser.parseSensor(json);
        Assert.assertTrue(!result.isSetDescription()
                && !result.isSetName()
                && !result.isSetEncodingType()
                && !result.isSetMetadata());
    }

    @Test
    public void readThingBasic() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    }\n"
                + "}";
        Thing expectedResult = new Thing()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects");
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThingWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    }\n"
                + "}";
        Thing result = entityParser.parseThing(json);
        Assert.assertTrue(result.isSetName()
                && result.isSetDescription()
                && result.isSetProperties());
    }

    @Test
    public void readThingWithAllValuesMissing() throws IOException {
        String json = "{}";
        Thing result = entityParser.parseThing(json);
        Assert.assertTrue(!result.isSetName()
                && !result.isSetDescription()
                && !result.isSetProperties());
    }

    @Test
    public void readThingWithNestedProperties() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": {\n"
                + "			\"someNestedProperty\": 10,\n"
                + "			\"someOtherNestedProperty\": \"someValue\"\n"
                + "		} 		\n"
                + "    }\n"
                + "}";
        Map<String, Object> property3 = new HashMap<>();
        property3.put("someNestedProperty", 10);
        property3.put("someOtherNestedProperty", "someValue");
        Thing expectedResult = new Thing()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", property3);
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThingWithLocation() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    },\n"
                + "    \"Locations\": [\n"
                + "        {\n"
                + "            \"name\": \"my backyard\",\n"
                + "            \"description\": \"my backyard\",\n"
                + "            \"encodingType\": \"application/vnd.geo+json\",\n"
                + "            \"location\": {\n"
                + "                \"type\": \"Point\",\n"
                + "                \"coordinates\": [-117.123,\n"
                + "                54.123]\n"
                + "            }\n"
                + "        }\n"
                + "    ]\n"
                + "}";
        Thing expectedResult = new Thing()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addLocation(new Location()
                        .setName("my backyard")
                        .setDescription("my backyard")
                        .setEncodingType("application/vnd.geo+json")
                        .setLocation(TestHelper.getPoint(-117.123, 54.123))
                );
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThingWithLinks1() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    },\n"
                + "    \"Locations\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        Thing expectedResult = new Thing()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addLocation(new Location()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThingWithLinks2() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    },\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        Thing expectedResult = new Thing()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addDatastream(new Datastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThingWithLinks3() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    },\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        Thing expectedResult = new Thing()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addMultiDatastream(new MultiDatastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThingWithLinks4() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    },\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ],\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        Thing expectedResult = new Thing()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addDatastream(new Datastream()
                        .setId(new IdLong(100))
                )
                .addMultiDatastream(new MultiDatastream()
                        .setId(new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThingWithLocationAndDatastream() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    },\n"
                + "    \"Locations\": [{\n"
                + "        \"name\": \"my backyard\",\n"
                + "        \"description\": \"my backyard\",\n"
                + "        \"encodingType\": \"application/vnd.geo+json\",\n"
                + "        \"location\": {\n"
                + "            \"type\": \"Point\",\n"
                + "            \"coordinates\": [-117.123,\n"
                + "            54.123]\n"
                + "        }\n"
                + "    }],\n"
                + "    \"Datastreams\": [{\n"
                + "        \"unitOfMeasurement\": {\n"
                + "            \"name\": \"Celsius\",\n"
                + "            \"symbol\": \"C\",\n"
                + "            \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius\"\n"
                + "        },\n"
                + "        \"name\": \"Temperature measurement\",\n"
                + "        \"description\": \"Temperature measurement\",\n"
                + "        \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "        \"ObservedProperty\": {\n"
                + "            \"name\": \"Temperature\",\n"
                + "            \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature\",\n"
                + "            \"description\": \"Temperature of the camping site\"\n"
                + "        },\n"
                + "        \"Sensor\": {\n"
                + "            \"name\": \"SensorUp Tempomatic 1000-b\",\n"
                + "            \"description\": \"SensorUp Tempomatic 1000-b\",\n"
                + "            \"encodingType\": \"http://schema.org/description\",\n"
                + "            \"metadata\": \"Calibration date:  Jan 11, 2015\"\n"
                + "        }\n"
                + "    }]\n"
                + "}";
        Thing expectedResult = new Thing()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addLocation(new Location()
                        .setName("my backyard")
                        .setDescription("my backyard")
                        .setEncodingType("application/vnd.geo+json")
                        .setLocation(TestHelper.getPoint(-117.123, 54.123))
                )
                .addDatastream(new Datastream()
                        .setUnitOfMeasurement(
                                new UnitOfMeasurement()
                                        .setName("Celsius")
                                        .setSymbol("C")
                                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius")
                        )
                        .setName("Temperature measurement")
                        .setDescription("Temperature measurement")
                        .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                        .setObservedProperty(
                                new ObservedProperty()
                                        .setName("Temperature")
                                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                        .setDescription("Temperature of the camping site")
                        )
                        .setSensor(
                                new Sensor()
                                        .setName("SensorUp Tempomatic 1000-b")
                                        .setDescription("SensorUp Tempomatic 1000-b")
                                        .setEncodingType("http://schema.org/description")
                                        .setMetadata("Calibration date:  Jan 11, 2015")
                        )
                );
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readThingWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseThing(json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readSensorWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseSensor(json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readDatastreamWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseDatastream(json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readLocationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseLocation(json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readFeatureOfInterestWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseFeatureOfInterest(json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readHistoricalLocationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseHistoricalLocation(json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readObservedPropertyWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseObservedProperty(json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readObservationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseObservation(json);
    }

    @Test
    public void readEntityLongId() throws IOException {
        {
            long id = Long.MAX_VALUE;
            String json = "{\"@iot.id\": " + id + "}";
            Thing expectedResult = new Thing().setId(new IdLong(id));
            assertEquals(expectedResult, entityParser.parseThing(json));
        }
        {
            long id = Long.MIN_VALUE;
            String json = "{\"@iot.id\": " + id + "}";
            Thing expectedResult = new Thing().setId(new IdLong(id));
            assertEquals(expectedResult, entityParser.parseThing(json));
        }
        {
            String id = UUID.randomUUID().toString();
            String json = "{\"@iot.id\": \"" + id + "\"}";
            Thing expectedResult = new Thing().setId(new IdString(id));
            assertEquals(expectedResult, new JsonReader(IdString.class).parseThing(json));
        }
    }

}
