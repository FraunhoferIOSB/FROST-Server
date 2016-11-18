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
package de.fraunhofer.iosb.ilt.sta.deserialize;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import de.fraunhofer.iosb.ilt.sta.formatter.DataArrayValue;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.FeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.LocationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.MultiDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ObservationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ObservedPropertyBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.SensorBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.UnitOfMeasurementBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.model.id.LongId;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.util.TestHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
public class EntityParserTest {

    private EntityParser entityParser;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        entityParser = new EntityParser(LongId.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void readDatastream_Basic_Success() throws IOException {
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
                + "	\"Sensor\": {\"@iot.id\": 5394815}\n"
                + "}";
        Datastream expectedResult = new DatastreamBuilder()
                .setUnitOfMeasurement(
                        new UnitOfMeasurementBuilder()
                        .setSymbol("%")
                        .setName("Percentage")
                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html")
                        .build()
                )
                .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setName("Temperature measurement")
                .setDescription("Temperature measurement")
                .setThing(new ThingBuilder().setId(new LongId(5394817)).build())
                .setObservedProperty(new ObservedPropertyBuilder().setId(new LongId(5394816)).build())
                .setSensor(new SensorBuilder().setId(new LongId(5394815)).build())
                .build();
        assertEquals(expectedResult, entityParser.parseDatastream(json));
    }

    @Test
    public void readDatastream_WithAllValuesPresent_Success() throws IOException {
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
        assert (result.isSetUnitOfMeasurement()
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
    public void readDatastream_WithAllValuesMissing_Success() throws IOException {
        String json = "{}";
        Datastream result = entityParser.parseDatastream(json);
        assert (!result.isSetUnitOfMeasurement()
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
    public void readDatastream_WithObservedAreaGeoJsonPolygon_Success() throws IOException {
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
        Datastream expectedResult = new DatastreamBuilder()
                .setUnitOfMeasurement(
                        new UnitOfMeasurementBuilder()
                        .setSymbol("%")
                        .setName("Percentage")
                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html")
                        .build()
                )
                .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setName("Temperature measurement")
                .setDescription("Temperature measurement")
                .setThing(new ThingBuilder().setId(new LongId(5394817)).build())
                .setObservedProperty(new ObservedPropertyBuilder().setId(new LongId(5394816)).build())
                .setSensor(new SensorBuilder().setId(new LongId(5394815)).build())
                .setObservedArea(TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                .build();
        assertEquals(expectedResult, entityParser.parseDatastream(json));
    }

    @Test
    public void readDatastream_WithObservedPropertyAndSensor_Success() throws IOException {
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
        Datastream expectedResult = new DatastreamBuilder()
                .setUnitOfMeasurement(
                        new UnitOfMeasurementBuilder()
                        .setName("Celsius")
                        .setSymbol("C")
                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius")
                        .build())
                .setName("Temperature measurement")
                .setDescription("Temperature measurement")
                .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setObservedProperty(
                        new ObservedPropertyBuilder()
                        .setName("Temperature")
                        .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                        .setDescription("Temperature of the camping site")
                        .build())
                .setSensor(
                        new SensorBuilder()
                        .setDescription("Sensor 101")
                        .setEncodingType("http://schema.org/description")
                        .setMetadata("Calibration date:  2011-11-11")
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseDatastream(json));
    }

    @Test
    public void readMultiDatastream_WithObservedPropertyAndSensor_Success() throws IOException {
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
        unitsOfMeasurement.add(new UnitOfMeasurementBuilder()
                .setName("DegreeAngle")
                .setSymbol("deg")
                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle")
                .build());
        unitsOfMeasurement.add(new UnitOfMeasurementBuilder()
                .setName("MeterPerSecond")
                .setSymbol("m/s")
                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond")
                .build());
        List<String> observationTypes = new ArrayList<>();
        observationTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        observationTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        MultiDatastream expectedResult = new MultiDatastreamBuilder()
                .setUnitOfMeasurements(unitsOfMeasurement)
                .setName("Wind")
                .setDescription("Wind direction and speed")
                .setMultiObservationDataTypes(observationTypes)
                .addObservedProperty(
                        new ObservedPropertyBuilder()
                        .setName("Wind Direction")
                        .setDefinition("SomeDefinition")
                        .setDescription("Direction the wind blows, 0=North, 90=East.")
                        .build())
                .addObservedProperty(
                        new ObservedPropertyBuilder()
                        .setName("Wind Speed")
                        .setDefinition("SomeDefinition")
                        .setDescription("Wind Speed")
                        .build())
                .setSensor(
                        new SensorBuilder()
                        .setDescription("Wind Sensor 101")
                        .setEncodingType("http://schema.org/description")
                        .setMetadata("Calibration date:  2011-11-11")
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseMultiDatastream(json));
    }

    @Test
    public void readFeatureOfInterst_Basic_Success() throws IOException {
        String json = "{\n"
                + "    \"name\": \"Underground Air Quality in NYC train tunnels\",\n"
                + "    \"description\": \"Underground Air Quality in NYC train tunnels\",\n"
                + "    \"encodingType\": \"application/vnd.geo+json\",\n"
                + "    \"feature\": {\n"
                + "        \"coordinates\": [51.08386,-114.13036],\n"
                + "        \"type\": \"Point\"\n"
                + "      }\n"
                + "}";
        FeatureOfInterest expectedResult = new FeatureOfInterestBuilder()
                .setName("Underground Air Quality in NYC train tunnels")
                .setDescription("Underground Air Quality in NYC train tunnels")
                .setEncodingType("application/vnd.geo+json")
                .setFeature(TestHelper.getPoint(51.08386, -114.13036))
                .build();
        assertEquals(expectedResult, entityParser.parseFeatureOfInterest(json));
    }

    @Test
    public void readFeatureOfInterst_WithAllValuesPresent_Success() throws IOException {
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
        assert (result.isSetDescription()
                && result.isSetName()
                && result.isSetEncodingType()
                && result.isSetFeature());
    }

    @Test
    public void readFeatureOfInterst_WithAllValuesMissing_Success() throws IOException {
        String json = "{}";
        FeatureOfInterest result = entityParser.parseFeatureOfInterest(json);
        assert (!result.isSetDescription()
                && !result.isSetName()
                && !result.isSetEncodingType()
                && !result.isSetFeature());
    }

    @Test
    public void readLocation_Basic_Success() throws IOException {
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
        Location expectedResult = new LocationBuilder()
                .setName("my backyard")
                .setDescription("my backyard")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(TestHelper.getPoint(-117.123, 54.123))
                .build();
        assertEquals(expectedResult, entityParser.parseLocation(json));
    }

    @Test
    public void readLocation_WithAllValuesPresent_Success() throws IOException {
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
        assert (result.isSetDescription()
                && result.isSetName()
                && result.isSetEncodingType()
                && result.isSetLocation());
    }

    @Test
    public void readLocation_WithAllValuesMissing_Success() throws IOException {
        String json = "{}";
        Location result = entityParser.parseLocation(json);
        assert (!result.isSetDescription()
                && !result.isSetName()
                && !result.isSetEncodingType()
                && !result.isSetLocation());
    }

    @Test
    public void readLocation_WithLinkedThings_Success() throws IOException {
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
        Thing thing = new ThingBuilder().setId(new LongId(100)).build();
        EntitySet<Thing> things = new EntitySetImpl<>(EntityType.Thing);
        things.add(thing);
        Location expectedResult = new LocationBuilder()
                .setName("my backyard")
                .setDescription("my backyard")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(TestHelper.getPoint(-117.123, 54.123))
                .setThings(things)
                .build();
        assertEquals(expectedResult, entityParser.parseLocation(json));
    }

    @Test
    public void readObservation_WithAllValuesPresent_Success() throws IOException {
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
        assert (result.isSetPhenomenonTime()
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
    public void readObservation_WithAllValuesMissing_Success() throws IOException {
        String json = "{}";
        Observation result = entityParser.parseObservation(json);
        assert (!result.isSetPhenomenonTime()
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
    public void readObservation_WithLinks_Success() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"Datastream\":{\"@iot.id\":100}\n"
                + "}";
        Observation expectedResult = new ObservationBuilder()
                .setPhenomenonTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setResultTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setResult(38)
                .setDatastream(new DatastreamBuilder().setId(new LongId(100)).build())
                .build();
        assertEquals(expectedResult, entityParser.parseObservation(json));

        json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"MultiDatastream\":{\"@iot.id\":100}\n"
                + "}";
        expectedResult = new ObservationBuilder()
                .setPhenomenonTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setResultTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setResult(38)
                .setMultiDatastream(new MultiDatastreamBuilder().setId(new LongId(100)).build())
                .build();
        assertEquals(expectedResult, entityParser.parseObservation(json));
    }

    @Test
    public void readObservation_WithLinkedFeatureOfInterest_Success() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"FeatureOfInterest\":{\"@iot.id\": 14269}\n"
                + "}";
        Observation expectedResult = new ObservationBuilder()
                .setPhenomenonTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setResultTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setResult(38)
                .setFeatureOfInterest(new FeatureOfInterestBuilder().setId(new LongId(14269)).build())
                .build();
        assertEquals(expectedResult, entityParser.parseObservation(json));
    }

    @Test
    public void readObservation_WithFeatureOfInterest_Success() throws IOException {
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
        Observation expectedResult = new ObservationBuilder()
                .setPhenomenonTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setResultTime(TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setResult(99)
                .setFeatureOfInterest(new FeatureOfInterestBuilder()
                        .setName("Turn 5, track surface temperature")
                        .setDescription("Turn 5, track surface temperature")
                        .setEncodingType("http://example.org/measurement_types#Measure")
                        .setFeature("tarmac temperature")
                        .build())
                .setDatastream(new DatastreamBuilder().setId(new LongId(14314)).build())
                .build();
        assertEquals(expectedResult, entityParser.parseObservation(json));
    }

    @Test
    public void readObservation_Precision_Success() throws IOException {
        String json = "{\n"
                + "  \"result\" : 100.00\n"
                + "}";
        Observation expectedResult = new ObservationBuilder()
                .setResult(new BigDecimal("100.00"))
                .build();
        Observation result = entityParser.parseObservation(json);
        assertEquals(expectedResult, result);
    }

    @Test
    public void readObservation_DataArray() throws IOException {
        String json = "[\n"
                + "  {\n"
                + "    \"Datastream\": {\n"
                + "      \"@iot.id\": 1\n"
                + "    },\n"
                + "    \"components\": [\n"
                + "      \"phenomenonTime\",\n"
                + "      \"result\",\n"
                + "      \"FeatureOfInterest/id\"\n"
                + "    ],\n"
                + "    \"dataArray@iot.count\":2,\n"
                + "    \"dataArray\": [\n"
                + "      [\n"
                + "        \"2010-12-23T10:20:00-0700\",\n"
                + "        20,\n"
                + "        1\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        30,\n"
                + "        1\n"
                + "      ]\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"Datastream\": {\n"
                + "      \"@iot.id\": 2\n"
                + "    },\n"
                + "    \"components\": [\n"
                + "      \"phenomenonTime\",\n"
                + "      \"result\",\n"
                + "      \"FeatureOfInterest/id\"\n"
                + "    ],\n"
                + "    \"dataArray@iot.count\":2,\n"
                + "    \"dataArray\": [\n"
                + "      [\n"
                + "        \"2010-12-23T10:20:00-0700\",\n"
                + "        65,\n"
                + "        1\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        60,\n"
                + "        1\n"
                + "      ]\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"MultiDatastream\": {\n"
                + "      \"@iot.id\": 2\n"
                + "    },\n"
                + "    \"components\": [\n"
                + "      \"phenomenonTime\",\n"
                + "      \"result\",\n"
                + "      \"FeatureOfInterest/id\"\n"
                + "    ],\n"
                + "    \"dataArray@iot.count\":2,\n"
                + "    \"dataArray\": [\n"
                + "      [\n"
                + "        \"2010-12-23T10:20:00-0700\",\n"
                + "        65,\n"
                + "        1\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        60,\n"
                + "        1\n"
                + "      ]\n"
                + "    ]\n"
                + "  }\n"
                + "]";
        List<DataArrayValue> expectedResult = new ArrayList<>();

        List<String> components = new ArrayList<>();
        components.add("phenomenonTime");
        components.add("result");
        components.add("FeatureOfInterest/id");

        Datastream ds1 = new DatastreamBuilder().setId(new LongId(1L)).build();

        DataArrayValue dav1 = new DataArrayValue(ds1, components);
        dav1.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 20, 1}));
        dav1.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 30, 1}));

        Datastream ds2 = new DatastreamBuilder().setId(new LongId(2L)).build();

        DataArrayValue dav2 = new DataArrayValue(ds2, components);
        dav2.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 65, 1}));
        dav2.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 60, 1}));

        MultiDatastream mds1 = new MultiDatastreamBuilder().setId(new LongId(2L)).build();

        DataArrayValue dav3 = new DataArrayValue(mds1, components);
        dav3.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 65, 1}));
        dav3.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 60, 1}));

        expectedResult.add(dav1);
        expectedResult.add(dav2);
        expectedResult.add(dav3);
        List<DataArrayValue> result = entityParser.parseObservationDataArray(json);
        assertEquals(expectedResult, result);
    }

    @Test
    public void readObservedProperty_Basic_Success() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        ObservedProperty expectedResult = new ObservedPropertyBuilder()
                .setName("ObservedPropertyUp Tempomatic 2000")
                .setDescription("http://schema.org/description")
                .setDefinition("Calibration date:  Jan 1, 2014")
                .build();
        assertEquals(expectedResult, entityParser.parseObservedProperty(json));
    }

    @Test
    public void readObservedProperty_WithLinks_Success() throws IOException {
        String json = "{\n"
                + "    \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "    \"description\": \"http://schema.org/description\",\n"
                + "    \"definition\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        ObservedProperty expectedResult = new ObservedPropertyBuilder()
                .setName("ObservedPropertyUp Tempomatic 2000")
                .setDescription("http://schema.org/description")
                .setDefinition("Calibration date:  Jan 1, 2014")
                .addDatastream(new DatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseObservedProperty(json));

        json = "{\n"
                + "    \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "    \"description\": \"http://schema.org/description\",\n"
                + "    \"definition\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new ObservedPropertyBuilder()
                .setName("ObservedPropertyUp Tempomatic 2000")
                .setDescription("http://schema.org/description")
                .setDefinition("Calibration date:  Jan 1, 2014")
                .addMultiDatastream(new MultiDatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
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
        expectedResult = new ObservedPropertyBuilder()
                .setName("ObservedPropertyUp Tempomatic 2000")
                .setDescription("http://schema.org/description")
                .setDefinition("Calibration date:  Jan 1, 2014")
                .addDatastream(new DatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .addMultiDatastream(new MultiDatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseObservedProperty(json));
    }

    @Test
    public void readObservedProperty_WithAllValuesPresent_Success() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        ObservedProperty result = entityParser.parseObservedProperty(json);
        assert (result.isSetName()
                && result.isSetDescription()
                && result.isSetDefinition());
    }

    @Test
    public void readObservedProperty_WithAllValuesMissing_Success() throws IOException {
        String json = "{}";
        ObservedProperty result = entityParser.parseObservedProperty(json);
        assert (!result.isSetName()
                && !result.isSetDescription()
                && !result.isSetDefinition());
    }

    @Test
    public void readSensor_Basic_Success() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Sensor expectedResult = new SensorBuilder()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014")
                .build();
        assertEquals(expectedResult, entityParser.parseSensor(json));
    }

    @Test
    public void readSensor_WithLinks_Success() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        Sensor expectedResult = new SensorBuilder()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014")
                .addDatastream(new DatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
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
        expectedResult = new SensorBuilder()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014")
                .addMultiDatastream(new MultiDatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
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
        expectedResult = new SensorBuilder()
                .setName("SensorUp Tempomatic 2000")
                .setDescription("SensorUp Tempomatic 2000")
                .setEncodingType("http://schema.org/description")
                .setMetadata("Calibration date:  Jan 1, 2014")
                .addDatastream(new DatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .addMultiDatastream(new MultiDatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseSensor(json));

    }

    @Test
    public void readSensor_WithAllValuesPresent_Success() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Sensor result = entityParser.parseSensor(json);
        assert (result.isSetDescription()
                && result.isSetName()
                && result.isSetEncodingType()
                && result.isSetMetadata());
    }

    @Test
    public void readSensor_WithAllValuesMissing_Success() throws IOException {
        String json = "{}";
        Sensor result = entityParser.parseSensor(json);
        assert (!result.isSetDescription()
                && !result.isSetName()
                && !result.isSetEncodingType()
                && !result.isSetMetadata());
    }

    @Test
    public void readThing_Basic_Success() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    }\n"
                + "}";
        Thing expectedResult = new ThingBuilder()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .build();
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThing_WithAllValuesPresent_Success() throws IOException {
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
        assert (result.isSetName()
                && result.isSetDescription()
                && result.isSetProperties());
    }

    @Test
    public void readThing_WithAllValuesMissing_Success() throws IOException {
        String json = "{}";
        Thing result = entityParser.parseThing(json);
        assert (!result.isSetName()
                && !result.isSetDescription()
                && !result.isSetProperties());
    }

    @Test
    public void readThing_WithNestedProperties_Success() throws IOException {
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
        Thing expectedResult = new ThingBuilder()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", property3)
                .build();
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThing_WithLocation_Success() throws IOException {
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
        Thing expectedResult = new ThingBuilder()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addLocation(new LocationBuilder()
                        .setName("my backyard")
                        .setDescription("my backyard")
                        .setEncodingType("application/vnd.geo+json")
                        .setLocation(TestHelper.getPoint(-117.123, 54.123))
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThing_WithLinks_Success() throws IOException {
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
        Thing expectedResult = new ThingBuilder()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addLocation(new LocationBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseThing(json));

        json = "{\n"
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
        expectedResult = new ThingBuilder()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addDatastream(new DatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseThing(json));

        json = "{\n"
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
        expectedResult = new ThingBuilder()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addMultiDatastream(new MultiDatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseThing(json));

        json = "{\n"
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
        expectedResult = new ThingBuilder()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addDatastream(new DatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .addMultiDatastream(new MultiDatastreamBuilder()
                        .setId(new LongId(100))
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThing_WithLocationAndDatastream_Success() throws IOException {
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
        Thing expectedResult = new ThingBuilder()
                .setName("camping lantern")
                .setDescription("camping lantern")
                .addProperty("property1", "it’s waterproof")
                .addProperty("property2", "it glows in the dark")
                .addProperty("property3", "it repels insects")
                .addLocation(new LocationBuilder()
                        .setName("my backyard")
                        .setDescription("my backyard")
                        .setEncodingType("application/vnd.geo+json")
                        .setLocation(TestHelper.getPoint(-117.123, 54.123))
                        .build()
                )
                .addDatastream(new DatastreamBuilder()
                        .setUnitOfMeasurement(
                                new UnitOfMeasurementBuilder()
                                .setName("Celsius")
                                .setSymbol("C")
                                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius")
                                .build())
                        .setName("Temperature measurement")
                        .setDescription("Temperature measurement")
                        .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                        .setObservedProperty(
                                new ObservedPropertyBuilder()
                                .setName("Temperature")
                                .setDefinition("http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                .setDescription("Temperature of the camping site")
                                .build())
                        .setSensor(
                                new SensorBuilder()
                                .setName("SensorUp Tempomatic 1000-b")
                                .setDescription("SensorUp Tempomatic 1000-b")
                                .setEncodingType("http://schema.org/description")
                                .setMetadata("Calibration date:  Jan 11, 2015")
                                .build())
                        .build())
                .build();
        assertEquals(expectedResult, entityParser.parseThing(json));
    }

    @Test
    public void readThing_WithUnknownField_Exception() throws IOException {
        String json = "{ \"someField\": 123}";
        exception.expect(UnrecognizedPropertyException.class);
        entityParser.parseThing(json);
    }

    @Test
    public void readSensor_WithUnknownField_Exception() throws IOException {
        String json = "{ \"someField\": 123}";
        exception.expect(UnrecognizedPropertyException.class);
        entityParser.parseSensor(json);
    }

    @Test
    public void readDatastream_WithUnknownField_Exception() throws IOException {
        String json = "{ \"someField\": 123}";
        exception.expect(UnrecognizedPropertyException.class);
        entityParser.parseDatastream(json);
    }

    @Test
    public void readLocation_WithUnknownField_Exception() throws IOException {
        String json = "{ \"someField\": 123}";
        exception.expect(UnrecognizedPropertyException.class);
        entityParser.parseLocation(json);
    }

    @Test
    public void readFeatureOfInterest_WithUnknownField_Exception() throws IOException {
        String json = "{ \"someField\": 123}";
        exception.expect(UnrecognizedPropertyException.class);
        entityParser.parseFeatureOfInterest(json);
    }

    @Test
    public void readHistoricalLocation_WithUnknownField_Exception() throws IOException {
        String json = "{ \"someField\": 123}";
        exception.expect(UnrecognizedPropertyException.class);
        entityParser.parseHistoricalLocation(json);
    }

    @Test
    public void readObservedProperty_WithUnknownField_Exception() throws IOException {
        String json = "{ \"someField\": 123}";
        exception.expect(UnrecognizedPropertyException.class);
        entityParser.parseObservedProperty(json);
    }

    @Test
    public void readObservation_WithUnknownField_Exception() throws IOException {
        String json = "{ \"someField\": 123}";
        exception.expect(UnrecognizedPropertyException.class);
        entityParser.parseObservation(json);
    }
}
