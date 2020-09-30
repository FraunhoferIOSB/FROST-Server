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
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
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
        Entity expectedResult = new DefaultEntity(EntityType.DATASTREAM)
                .setProperty(EntityPropertyMain.UNITOFMEASUREMENT,
                        new UnitOfMeasurement("Percentage", "%", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html"))
                .setProperty(EntityPropertyMain.OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(EntityPropertyMain.NAME, "Temperature measurement")
                .setProperty(EntityPropertyMain.DESCRIPTION, "Temperature measurement")
                .setProperty(NavigationPropertyMain.THING, new DefaultEntity(EntityType.THING).setProperty(EntityPropertyMain.ID, new IdLong(5394817)))
                .setProperty(NavigationPropertyMain.OBSERVEDPROPERTY, new DefaultEntity(EntityType.OBSERVED_PROPERTY).setProperty(EntityPropertyMain.ID, new IdLong(5394816)))
                .setProperty(NavigationPropertyMain.SENSOR, new DefaultEntity(EntityType.SENSOR).setProperty(EntityPropertyMain.ID, new IdLong(Long.MAX_VALUE)));
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.DATASTREAM, json));
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
        Entity result = entityParser.parseEntity(EntityType.DATASTREAM, json);
        Assert.assertTrue(result.isSetProperty(EntityPropertyMain.UNITOFMEASUREMENT)
                && result.isSetProperty(EntityPropertyMain.OBSERVATIONTYPE)
                && result.isSetProperty(EntityPropertyMain.NAME)
                && result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && result.isSetProperty(NavigationPropertyMain.THING)
                && result.isSetProperty(NavigationPropertyMain.OBSERVEDPROPERTY)
                && result.isSetProperty(NavigationPropertyMain.SENSOR)
                && result.isSetProperty(EntityPropertyMain.OBSERVEDAREA)
                && result.isSetProperty(EntityPropertyMain.PHENOMENONTIME)
                && result.isSetProperty(EntityPropertyMain.RESULTTIME));
    }

    @Test
    public void readDatastreamWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(EntityType.DATASTREAM, json);
        Assert.assertTrue(!result.isSetProperty(EntityPropertyMain.UNITOFMEASUREMENT)
                && !result.isSetProperty(EntityPropertyMain.OBSERVATIONTYPE)
                && !result.isSetProperty(EntityPropertyMain.NAME)
                && !result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && !result.isSetProperty(NavigationPropertyMain.THING)
                && !result.isSetProperty(NavigationPropertyMain.OBSERVEDPROPERTY)
                && !result.isSetProperty(NavigationPropertyMain.SENSOR)
                && !result.isSetProperty(EntityPropertyMain.OBSERVEDAREA)
                && !result.isSetProperty(EntityPropertyMain.PHENOMENONTIME)
                && !result.isSetProperty(EntityPropertyMain.RESULTTIME));
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
        Entity expectedResult = new DefaultEntity(EntityType.DATASTREAM)
                .setProperty(EntityPropertyMain.UNITOFMEASUREMENT,
                        new UnitOfMeasurement("Percentage", "%", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html"))
                .setProperty(EntityPropertyMain.OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(EntityPropertyMain.NAME, "Temperature measurement")
                .setProperty(EntityPropertyMain.DESCRIPTION, "Temperature measurement")
                .setProperty(NavigationPropertyMain.THING, new DefaultEntity(EntityType.THING).setProperty(EntityPropertyMain.ID, new IdLong(5394817)))
                .setProperty(NavigationPropertyMain.OBSERVEDPROPERTY, new DefaultEntity(EntityType.OBSERVED_PROPERTY).setProperty(EntityPropertyMain.ID, new IdLong(5394816)))
                .setProperty(NavigationPropertyMain.SENSOR, new DefaultEntity(EntityType.SENSOR).setProperty(EntityPropertyMain.ID, new IdLong(5394815)))
                .setProperty(EntityPropertyMain.OBSERVEDAREA, TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0));
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.DATASTREAM, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.DATASTREAM)
                .setProperty(EntityPropertyMain.UNITOFMEASUREMENT,
                        new UnitOfMeasurement("Celsius", "C", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius"))
                .setProperty(EntityPropertyMain.NAME, "Temperature measurement")
                .setProperty(EntityPropertyMain.DESCRIPTION, "Temperature measurement")
                .setProperty(EntityPropertyMain.OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(NavigationPropertyMain.OBSERVEDPROPERTY,
                        new DefaultEntity(EntityType.OBSERVED_PROPERTY)
                                .setProperty(EntityPropertyMain.NAME, "Temperature")
                                .setProperty(EntityPropertyMain.DEFINITION, "http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                .setProperty(EntityPropertyMain.DESCRIPTION, "Temperature of the camping site")
                )
                .setProperty(NavigationPropertyMain.SENSOR,
                        new DefaultEntity(EntityType.SENSOR)
                                .setProperty(EntityPropertyMain.DESCRIPTION, "Sensor 101")
                                .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://schema.org/description")
                                .setProperty(EntityPropertyMain.METADATA, "Calibration date:  2011-11-11")
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.DATASTREAM, json));
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
        unitsOfMeasurement.add(new UnitOfMeasurement("DegreeAngle", "deg", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle"));
        unitsOfMeasurement.add(new UnitOfMeasurement("MeterPerSecond", "m/s", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond"));
        List<String> observationTypes = new ArrayList<>();
        observationTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        observationTypes.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        Entity expectedResult = new DefaultEntity(EntityType.MULTI_DATASTREAM)
                .setProperty(EntityPropertyMain.OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")
                .setProperty(EntityPropertyMain.UNITOFMEASUREMENTS, unitsOfMeasurement)
                .setProperty(EntityPropertyMain.NAME, "Wind")
                .setProperty(EntityPropertyMain.DESCRIPTION, "Wind direction and speed")
                .setProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, observationTypes)
                .addNavigationEntity(new DefaultEntity(EntityType.OBSERVED_PROPERTY)
                        .setProperty(EntityPropertyMain.NAME, "Wind Direction")
                        .setProperty(EntityPropertyMain.DEFINITION, "SomeDefinition")
                        .setProperty(EntityPropertyMain.DESCRIPTION, "Direction the wind blows, 0=North, 90=East.")
                )
                .addNavigationEntity(new DefaultEntity(EntityType.OBSERVED_PROPERTY)
                        .setProperty(EntityPropertyMain.NAME, "Wind Speed")
                        .setProperty(EntityPropertyMain.DEFINITION, "SomeDefinition")
                        .setProperty(EntityPropertyMain.DESCRIPTION, "Wind Speed")
                )
                .setProperty(NavigationPropertyMain.SENSOR, new DefaultEntity(EntityType.SENSOR)
                        .setProperty(EntityPropertyMain.DESCRIPTION, "Wind Sensor 101")
                        .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://schema.org/description")
                        .setProperty(EntityPropertyMain.METADATA, "Calibration date:  2011-11-11")
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.MULTI_DATASTREAM, json));
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
            Entity expectedResult = new DefaultEntity(EntityType.FEATURE_OF_INTEREST)
                    .setProperty(EntityPropertyMain.NAME, "Underground Air Quality in NYC train tunnels")
                    .setProperty(EntityPropertyMain.DESCRIPTION, "Underground Air Quality in NYC train tunnels")
                    .setProperty(EntityPropertyMain.ENCODINGTYPE, "application/vnd.geo+json")
                    .setProperty(EntityPropertyMain.FEATURE, TestHelper.getPoint(51.08386, -114.13036));
            assertEquals(expectedResult, entityParser.parseEntity(EntityType.FEATURE_OF_INTEREST, json));
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
            Entity expectedResult = new DefaultEntity(EntityType.FEATURE_OF_INTEREST)
                    .setProperty(EntityPropertyMain.NAME, "Underground Air Quality in NYC train tunnels")
                    .setProperty(EntityPropertyMain.DESCRIPTION, "Underground Air Quality in NYC train tunnels")
                    .setProperty(EntityPropertyMain.ENCODINGTYPE, "application/geo+json")
                    .setProperty(EntityPropertyMain.FEATURE, TestHelper.getPoint(51.08386, -114.13036));
            assertEquals(expectedResult, entityParser.parseEntity(EntityType.FEATURE_OF_INTEREST, json));
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
        Entity result = entityParser.parseEntity(EntityType.FEATURE_OF_INTEREST, json);
        Assert.assertTrue(result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && result.isSetProperty(EntityPropertyMain.NAME)
                && result.isSetProperty(EntityPropertyMain.ENCODINGTYPE)
                && result.isSetProperty(EntityPropertyMain.FEATURE));
    }

    @Test
    public void readFeatureOfInterstWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(EntityType.FEATURE_OF_INTEREST, json);
        Assert.assertTrue(!result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && !result.isSetProperty(EntityPropertyMain.NAME)
                && !result.isSetProperty(EntityPropertyMain.ENCODINGTYPE)
                && !result.isSetProperty(NavigationPropertyMain.FEATUREOFINTEREST));
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
        Entity expectedResult = new DefaultEntity(EntityType.LOCATION)
                .setProperty(EntityPropertyMain.NAME, "my backyard")
                .setProperty(EntityPropertyMain.DESCRIPTION, "my backyard")
                .setProperty(EntityPropertyMain.ENCODINGTYPE, "application/vnd.geo+json")
                .setProperty(EntityPropertyMain.LOCATION, TestHelper.getPoint(-117.123, 54.123));
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.LOCATION, json));
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
        Entity result = entityParser.parseEntity(EntityType.LOCATION, json);
        Assert.assertTrue(result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && result.isSetProperty(EntityPropertyMain.NAME)
                && result.isSetProperty(EntityPropertyMain.ENCODINGTYPE)
                && result.isSetProperty(EntityPropertyMain.LOCATION));
    }

    @Test
    public void readLocationWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(EntityType.LOCATION, json);
        Assert.assertTrue(!result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && !result.isSetProperty(EntityPropertyMain.NAME)
                && !result.isSetProperty(EntityPropertyMain.ENCODINGTYPE)
                && !result.isSetProperty(EntityPropertyMain.LOCATION));
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
        Entity thing = new DefaultEntity(EntityType.THING).setProperty(EntityPropertyMain.ID, new IdLong(100));
        EntitySet things = new EntitySetImpl<>(EntityType.THING);
        things.add(thing);
        Entity expectedResult = new DefaultEntity(EntityType.LOCATION)
                .setProperty(EntityPropertyMain.NAME, "my backyard")
                .setProperty(EntityPropertyMain.DESCRIPTION, "my backyard")
                .setProperty(EntityPropertyMain.ENCODINGTYPE, "application/vnd.geo+json")
                .setProperty(EntityPropertyMain.LOCATION, TestHelper.getPoint(-117.123, 54.123))
                .setProperty(NavigationPropertyMain.THINGS, things);
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.LOCATION, json));
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
        Entity result = entityParser.parseEntity(EntityType.OBSERVATION, json);
        Assert.assertTrue(result.isSetProperty(EntityPropertyMain.PHENOMENONTIME)
                && result.isSetProperty(EntityPropertyMain.RESULTTIME)
                && result.isSetProperty(EntityPropertyMain.RESULT)
                && result.isSetProperty(NavigationPropertyMain.DATASTREAM)
                && result.isSetProperty(NavigationPropertyMain.FEATUREOFINTEREST)
                && result.isSetProperty(EntityPropertyMain.PARAMETERS)
                && result.isSetProperty(EntityPropertyMain.PHENOMENONTIME)
                && result.isSetProperty(EntityPropertyMain.RESULTQUALITY)
                && result.isSetProperty(EntityPropertyMain.VALIDTIME));
    }

    @Test
    public void readObservationWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(EntityType.OBSERVATION, json);
        Assert.assertTrue(!result.isSetProperty(EntityPropertyMain.PHENOMENONTIME)
                && !result.isSetProperty(EntityPropertyMain.RESULTTIME)
                && !result.isSetProperty(EntityPropertyMain.RESULT)
                && !result.isSetProperty(NavigationPropertyMain.DATASTREAM)
                && !result.isSetProperty(NavigationPropertyMain.FEATUREOFINTEREST)
                && !result.isSetProperty(EntityPropertyMain.PARAMETERS)
                && !result.isSetProperty(EntityPropertyMain.PHENOMENONTIME)
                && !result.isSetProperty(EntityPropertyMain.RESULTQUALITY)
                && !result.isSetProperty(EntityPropertyMain.VALIDTIME));
    }

    @Test
    public void readObservationWithLinks() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"Datastream\":{\"@iot.id\":100}\n"
                + "}";
        Entity expectedResult = new DefaultEntity(EntityType.OBSERVATION)
                .setProperty(EntityPropertyMain.PHENOMENONTIME, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setProperty(EntityPropertyMain.RESULTTIME, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setProperty(EntityPropertyMain.RESULT, 38)
                .setProperty(NavigationPropertyMain.DATASTREAM, new DefaultEntity(EntityType.DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.OBSERVATION, json));

        json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"MultiDatastream\":{\"@iot.id\":100}\n"
                + "}";
        expectedResult = new DefaultEntity(EntityType.OBSERVATION)
                .setProperty(EntityPropertyMain.PHENOMENONTIME, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setProperty(EntityPropertyMain.RESULTTIME, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setProperty(EntityPropertyMain.RESULT, 38)
                .setProperty(NavigationPropertyMain.MULTIDATASTREAM, new DefaultEntity(EntityType.MULTI_DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.OBSERVATION, json));
    }

    @Test
    public void readObservationWithLinkedFeatureOfInterest() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"FeatureOfInterest\":{\"@iot.id\": 14269}\n"
                + "}";
        Entity expectedResult = new DefaultEntity(EntityType.OBSERVATION)
                .setProperty(EntityPropertyMain.PHENOMENONTIME, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setProperty(EntityPropertyMain.RESULTTIME, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setProperty(EntityPropertyMain.RESULT, 38)
                .setProperty(NavigationPropertyMain.FEATUREOFINTEREST, new DefaultEntity(EntityType.FEATURE_OF_INTEREST)
                        .setProperty(EntityPropertyMain.ID, new IdLong(14269)));
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.OBSERVATION, json));
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
                + "    \"feature\": \"tarmac\"\n"
                + "  },\n"
                + "  \"Datastream\":{\"@iot.id\": 14314}\n"
                + "}";
        Entity expectedResult = new DefaultEntity(EntityType.OBSERVATION)
                .setProperty(EntityPropertyMain.PHENOMENONTIME, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setProperty(EntityPropertyMain.RESULTTIME, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setProperty(EntityPropertyMain.RESULT, 99)
                .setProperty(NavigationPropertyMain.FEATUREOFINTEREST, new DefaultEntity(EntityType.FEATURE_OF_INTEREST)
                        .setProperty(EntityPropertyMain.NAME, "Turn 5, track surface temperature")
                        .setProperty(EntityPropertyMain.DESCRIPTION, "Turn 5, track surface temperature")
                        .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://example.org/measurement_types#Measure")
                        .setProperty(EntityPropertyMain.FEATURE, "tarmac")
                )
                .setProperty(NavigationPropertyMain.DATASTREAM, new DefaultEntity(EntityType.DATASTREAM).setProperty(EntityPropertyMain.ID, new IdLong(14314)));
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.OBSERVATION, json));
    }

    @Test
    public void readObservationPrecision() throws IOException {
        String json = "{\n"
                + "  \"result\" : 100.00\n"
                + "}";
        Entity expectedResult = new DefaultEntity(EntityType.OBSERVATION)
                .setProperty(EntityPropertyMain.RESULT, new BigDecimal("100.00"));
        Entity result = entityParser.parseEntity(EntityType.OBSERVATION, json);
        assertEquals(expectedResult, result);

        json = "{\n"
                + "  \"result\" : 0.00\n"
                + "}";
        expectedResult = new DefaultEntity(EntityType.OBSERVATION)
                .setProperty(EntityPropertyMain.RESULT, new BigDecimal("0.00"));
        result = entityParser.parseEntity(EntityType.OBSERVATION, json);
        assertEquals(expectedResult, result);
    }

    @Test
    public void readObservedPropertyBasic() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity expectedResult = new DefaultEntity(EntityType.OBSERVED_PROPERTY)
                .setProperty(EntityPropertyMain.NAME, "ObservedPropertyUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "http://schema.org/description")
                .setProperty(EntityPropertyMain.DEFINITION, "Calibration date:  Jan 1, 2014");
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.OBSERVED_PROPERTY, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.OBSERVED_PROPERTY)
                .setProperty(EntityPropertyMain.NAME, "ObservedPropertyUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "http://schema.org/description")
                .setProperty(EntityPropertyMain.DEFINITION, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(new DefaultEntity(EntityType.DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.OBSERVED_PROPERTY, json));

        json = "{\n"
                + "    \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "    \"description\": \"http://schema.org/description\",\n"
                + "    \"definition\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new DefaultEntity(EntityType.OBSERVED_PROPERTY)
                .setProperty(EntityPropertyMain.NAME, "ObservedPropertyUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "http://schema.org/description")
                .setProperty(EntityPropertyMain.DEFINITION, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(new DefaultEntity(EntityType.MULTI_DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.OBSERVED_PROPERTY, json));

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
        expectedResult = new DefaultEntity(EntityType.OBSERVED_PROPERTY)
                .setProperty(EntityPropertyMain.NAME, "ObservedPropertyUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "http://schema.org/description")
                .setProperty(EntityPropertyMain.DEFINITION, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(new DefaultEntity(EntityType.DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                )
                .addNavigationEntity(new DefaultEntity(EntityType.MULTI_DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.OBSERVED_PROPERTY, json));
    }

    @Test
    public void readObservedPropertyWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity result = entityParser.parseEntity(EntityType.OBSERVED_PROPERTY, json);
        Assert.assertTrue(result.isSetProperty(EntityPropertyMain.NAME)
                && result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && result.isSetProperty(EntityPropertyMain.DEFINITION));
    }

    @Test
    public void readObservedPropertyWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(EntityType.OBSERVED_PROPERTY, json);
        Assert.assertTrue(!result.isSetProperty(EntityPropertyMain.NAME)
                && !result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && !result.isSetProperty(EntityPropertyMain.DEFINITION));
    }

    @Test
    public void readSensorBasic() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity expectedResult = new DefaultEntity(EntityType.SENSOR)
                .setProperty(EntityPropertyMain.NAME, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://schema.org/description")
                .setProperty(EntityPropertyMain.METADATA, "Calibration date:  Jan 1, 2014");
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.SENSOR, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.SENSOR)
                .setProperty(EntityPropertyMain.NAME, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://schema.org/description")
                .setProperty(EntityPropertyMain.METADATA, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(new DefaultEntity(EntityType.DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.SENSOR, json));

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
        expectedResult = new DefaultEntity(EntityType.SENSOR)
                .setProperty(EntityPropertyMain.NAME, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://schema.org/description")
                .setProperty(EntityPropertyMain.METADATA, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(
                        new DefaultEntity(EntityType.DATASTREAM)
                                .setProperty(EntityPropertyMain.ID, new IdLong(100)))
                .addNavigationEntity(
                        new DefaultEntity(EntityType.DATASTREAM)
                                .setProperty(EntityPropertyMain.ID, new IdLong(101)));
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.SENSOR, json));

        json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new DefaultEntity(EntityType.SENSOR)
                .setProperty(EntityPropertyMain.NAME, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://schema.org/description")
                .setProperty(EntityPropertyMain.METADATA, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(new DefaultEntity(EntityType.MULTI_DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.SENSOR, json));

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
        expectedResult = new DefaultEntity(EntityType.SENSOR)
                .setProperty(EntityPropertyMain.NAME, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.DESCRIPTION, "SensorUp Tempomatic 2000")
                .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://schema.org/description")
                .setProperty(EntityPropertyMain.METADATA, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(new DefaultEntity(EntityType.DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                )
                .addNavigationEntity(new DefaultEntity(EntityType.MULTI_DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.SENSOR, json));
    }

    @Test
    public void readSensorWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity result = entityParser.parseEntity(EntityType.SENSOR, json);
        Assert.assertTrue(result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && result.isSetProperty(EntityPropertyMain.NAME)
                && result.isSetProperty(EntityPropertyMain.ENCODINGTYPE)
                && result.isSetProperty(EntityPropertyMain.METADATA));
    }

    @Test
    public void readSensorWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(EntityType.SENSOR, json);
        Assert.assertTrue(!result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && !result.isSetProperty(EntityPropertyMain.NAME)
                && !result.isSetProperty(EntityPropertyMain.ENCODINGTYPE)
                && !result.isSetProperty(EntityPropertyMain.METADATA));
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
        Entity expectedResult = new DefaultEntity(EntityType.THING)
                .setProperty(EntityPropertyMain.NAME, "camping lantern")
                .setProperty(EntityPropertyMain.DESCRIPTION, "camping lantern")
                .setProperty(EntityPropertyMain.PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build());
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
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
        Entity result = entityParser.parseEntity(EntityType.THING, json);
        Assert.assertTrue(result.isSetProperty(EntityPropertyMain.NAME)
                && result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && result.isSetProperty(EntityPropertyMain.PROPERTIES));
    }

    @Test
    public void readThingWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(EntityType.THING, json);
        Assert.assertTrue(!result.isSetProperty(EntityPropertyMain.NAME)
                && !result.isSetProperty(EntityPropertyMain.DESCRIPTION)
                && !result.isSetProperty(EntityPropertyMain.PROPERTIES));
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
        Entity expectedResult = new DefaultEntity(EntityType.THING)
                .setProperty(EntityPropertyMain.NAME, "camping lantern")
                .setProperty(EntityPropertyMain.DESCRIPTION, "camping lantern")
                .setProperty(EntityPropertyMain.PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", property3)
                        .build());
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.THING)
                .setProperty(EntityPropertyMain.NAME, "camping lantern")
                .setProperty(EntityPropertyMain.DESCRIPTION, "camping lantern")
                .setProperty(EntityPropertyMain.PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(EntityType.LOCATION)
                        .setProperty(EntityPropertyMain.NAME, "my backyard")
                        .setProperty(EntityPropertyMain.DESCRIPTION, "my backyard")
                        .setProperty(EntityPropertyMain.ENCODINGTYPE, "application/vnd.geo+json")
                        .setProperty(EntityPropertyMain.LOCATION, TestHelper.getPoint(-117.123, 54.123))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.THING)
                .setProperty(EntityPropertyMain.NAME, "camping lantern")
                .setProperty(EntityPropertyMain.DESCRIPTION, "camping lantern")
                .setProperty(EntityPropertyMain.PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(EntityType.LOCATION)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.THING)
                .setProperty(EntityPropertyMain.NAME, "camping lantern")
                .setProperty(EntityPropertyMain.DESCRIPTION, "camping lantern")
                .setProperty(EntityPropertyMain.PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(EntityType.DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.THING)
                .setProperty(EntityPropertyMain.NAME, "camping lantern")
                .setProperty(EntityPropertyMain.DESCRIPTION, "camping lantern")
                .setProperty(EntityPropertyMain.PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(EntityType.MULTI_DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.THING)
                .setProperty(EntityPropertyMain.NAME, "camping lantern")
                .setProperty(EntityPropertyMain.DESCRIPTION, "camping lantern")
                .setProperty(EntityPropertyMain.PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(EntityType.DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                )
                .addNavigationEntity(new DefaultEntity(EntityType.MULTI_DATASTREAM)
                        .setProperty(EntityPropertyMain.ID, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
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
        Entity expectedResult = new DefaultEntity(EntityType.THING)
                .setProperty(EntityPropertyMain.NAME, "camping lantern")
                .setProperty(EntityPropertyMain.DESCRIPTION, "camping lantern")
                .setProperty(EntityPropertyMain.PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(EntityType.LOCATION)
                        .setProperty(EntityPropertyMain.NAME, "my backyard")
                        .setProperty(EntityPropertyMain.DESCRIPTION, "my backyard")
                        .setProperty(EntityPropertyMain.ENCODINGTYPE, "application/vnd.geo+json")
                        .setProperty(EntityPropertyMain.LOCATION, TestHelper.getPoint(-117.123, 54.123))
                )
                .addNavigationEntity(new DefaultEntity(EntityType.DATASTREAM)
                        .setProperty(EntityPropertyMain.UNITOFMEASUREMENT,
                                new UnitOfMeasurement("Celsius", "C", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius"))
                        .setProperty(EntityPropertyMain.NAME, "Temperature measurement")
                        .setProperty(EntityPropertyMain.DESCRIPTION, "Temperature measurement")
                        .setProperty(EntityPropertyMain.OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                        .setProperty(NavigationPropertyMain.OBSERVEDPROPERTY,
                                new DefaultEntity(EntityType.OBSERVED_PROPERTY)
                                        .setProperty(EntityPropertyMain.NAME, "Temperature")
                                        .setProperty(EntityPropertyMain.DEFINITION, "http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                        .setProperty(EntityPropertyMain.DESCRIPTION, "Temperature of the camping site")
                        )
                        .setProperty(NavigationPropertyMain.SENSOR,
                                new DefaultEntity(EntityType.SENSOR)
                                        .setProperty(EntityPropertyMain.NAME, "SensorUp Tempomatic 1000-b")
                                        .setProperty(EntityPropertyMain.DESCRIPTION, "SensorUp Tempomatic 1000-b")
                                        .setProperty(EntityPropertyMain.ENCODINGTYPE, "http://schema.org/description")
                                        .setProperty(EntityPropertyMain.METADATA, "Calibration date:  Jan 11, 2015")
                        )
                );
        final Entity result = entityParser.parseEntity(EntityType.THING, json);
        assertEquals(expectedResult, result);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readThingWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(EntityType.THING, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readSensorWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(EntityType.SENSOR, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readDatastreamWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(EntityType.DATASTREAM, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readLocationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(EntityType.LOCATION, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readFeatureOfInterestWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(EntityType.FEATURE_OF_INTEREST, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readHistoricalLocationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(EntityType.HISTORICAL_LOCATION, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readObservedPropertyWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(EntityType.OBSERVED_PROPERTY, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readObservationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(EntityType.OBSERVATION, json);
    }

    @Test
    public void readEntityLongId() throws IOException {
        {
            long id = Long.MAX_VALUE;
            String json = "{\"@iot.id\": " + id + "}";
            Entity expectedResult = new DefaultEntity(EntityType.THING).setProperty(EntityPropertyMain.ID, new IdLong(id));
            assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
        }
        {
            long id = Long.MIN_VALUE;
            String json = "{\"@iot.id\": " + id + "}";
            Entity expectedResult = new DefaultEntity(EntityType.THING).setProperty(EntityPropertyMain.ID, new IdLong(id));
            assertEquals(expectedResult, entityParser.parseEntity(EntityType.THING, json));
        }
        {
            String id = UUID.randomUUID().toString();
            String json = "{\"@iot.id\": \"" + id + "\"}";
            Entity expectedResult = new DefaultEntity(EntityType.THING).setProperty(EntityPropertyMain.ID, new IdString(id));
            assertEquals(expectedResult, new JsonReader(IdString.class).parseEntity(EntityType.THING, json));
        }
    }

}
