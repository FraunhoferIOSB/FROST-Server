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

import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TestHelper.createTimeInstantUTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.TextNode;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.model.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PrimaryKey;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class EntityParserTest {

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
    void readDatastreamBasic() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etDatastream)
                .setProperty(pluginCoreModel.getEpUnitOfMeasurement(),
                        new UnitOfMeasurement("Percentage", "%", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html"))
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.epName, "Temperature measurement")
                .setProperty(pluginCoreModel.epDescription, "Temperature measurement")
                .setProperty(
                        pluginCoreModel.npThingDatasteam,
                        new DefaultEntity(pluginCoreModel.etThing)
                                .setPrimaryKeyValues(PkValue.of(5394817L)))
                .setProperty(
                        pluginCoreModel.npObservedPropertyDatastream,
                        new DefaultEntity(pluginCoreModel.etObservedProperty)
                                .setPrimaryKeyValues(PkValue.of(5394816L)))
                .setProperty(
                        pluginCoreModel.npSensorDatastream,
                        new DefaultEntity(pluginCoreModel.etSensor)
                                .setPrimaryKeyValues(PkValue.of(Long.MAX_VALUE)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etDatastream, json));
    }

    @Test
    void readDatastreamWithAllValuesPresent() throws IOException {
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
        Entity result = entityParser.parseEntity(pluginCoreModel.etDatastream, json);
        assertTrue(result.isSetProperty(pluginCoreModel.getEpUnitOfMeasurement())
                && result.isSetProperty(pluginCoreModel.epObservationType)
                && result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.npThingDatasteam)
                && result.isSetProperty(pluginCoreModel.npObservedPropertyDatastream)
                && result.isSetProperty(pluginCoreModel.npSensorDatastream)
                && result.isSetProperty(pluginCoreModel.epObservedArea)
                && result.isSetProperty(pluginCoreModel.epPhenomenonTime)
                && result.isSetProperty(pluginCoreModel.epResultTime));
    }

    @Test
    void readDatastreamWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etDatastream, json);
        assertTrue(!result.isSetProperty(pluginCoreModel.getEpUnitOfMeasurement())
                && !result.isSetProperty(pluginCoreModel.epObservationType)
                && !result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.npThingDatasteam)
                && !result.isSetProperty(pluginCoreModel.npObservedPropertyDatastream)
                && !result.isSetProperty(pluginCoreModel.npSensorDatastream)
                && !result.isSetProperty(pluginCoreModel.epObservedArea)
                && !result.isSetProperty(pluginCoreModel.epPhenomenonTime)
                && !result.isSetProperty(pluginCoreModel.epResultTime));
    }

    @Test
    void readDatastreamWithObservedAreaGeoJsonPolygon() throws IOException {
        final String json = "{\n"
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
                + "		\"coordinates\": [[[100.0, 0.0],[101.0, 0.0],[101.0, 1.0],[100.0, 1.0],[100.0, 0.0]]]\n"
                + "	}\n"
                + "}";
        final Entity expectedResult = new DefaultEntity(pluginCoreModel.etDatastream)
                .setProperty(pluginCoreModel.getEpUnitOfMeasurement(),
                        new UnitOfMeasurement("Percentage", "%", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html"))
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.epName, "Temperature measurement")
                .setProperty(pluginCoreModel.epDescription, "Temperature measurement")
                .setProperty(
                        pluginCoreModel.npThingDatasteam,
                        new DefaultEntity(pluginCoreModel.etThing)
                                .setPrimaryKeyValues(PkValue.of(5394817L)))
                .setProperty(
                        pluginCoreModel.npObservedPropertyDatastream,
                        new DefaultEntity(pluginCoreModel.etObservedProperty)
                                .setPrimaryKeyValues(PkValue.of(5394816L)))
                .setProperty(
                        pluginCoreModel.npSensorDatastream,
                        new DefaultEntity(pluginCoreModel.etSensor)
                                .setPrimaryKeyValues(PkValue.of(5394815L)))
                .setProperty(pluginCoreModel.epObservedArea, TestHelper.jsonPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0));
        final Entity result = entityParser.parseEntity(pluginCoreModel.etDatastream, json);
        assertEquals(expectedResult, result);
    }

    @Test
    void readDatastreamWithObservedPropertyAndSensor() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etDatastream)
                .setProperty(pluginCoreModel.getEpUnitOfMeasurement(),
                        new UnitOfMeasurement("Celsius", "C", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius"))
                .setProperty(pluginCoreModel.epName, "Temperature measurement")
                .setProperty(pluginCoreModel.epDescription, "Temperature measurement")
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.npObservedPropertyDatastream,
                        new DefaultEntity(pluginCoreModel.etObservedProperty)
                                .setProperty(pluginCoreModel.epName, "Temperature")
                                .setProperty(pluginCoreModel.epDefinition, "http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                .setProperty(pluginCoreModel.epDescription, "Temperature of the camping site"))
                .setProperty(pluginCoreModel.npSensorDatastream,
                        new DefaultEntity(pluginCoreModel.etSensor)
                                .setProperty(pluginCoreModel.epDescription, "Sensor 101")
                                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  2011-11-11"));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etDatastream, json));
    }

    @Test
    void readFeatureOfInterstBasic() throws IOException {
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
            Entity expectedResult = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                    .setProperty(pluginCoreModel.epName, "Underground Air Quality in NYC train tunnels")
                    .setProperty(pluginCoreModel.epDescription, "Underground Air Quality in NYC train tunnels")
                    .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                    .setProperty(pluginCoreModel.epFeature, TestHelper.jsonPoint(51.08386, -114.13036));
            assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json));
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
            Entity expectedResult = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                    .setProperty(pluginCoreModel.epName, "Underground Air Quality in NYC train tunnels")
                    .setProperty(pluginCoreModel.epDescription, "Underground Air Quality in NYC train tunnels")
                    .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/geo+json")
                    .setProperty(pluginCoreModel.epFeature, TestHelper.jsonPoint(51.08386, -114.13036));
            assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json));
        }
    }

    @Test
    void readFeatureOfInterstWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"Underground Air Quality in NYC train tunnels\",\n"
                + "    \"description\": \"Underground Air Quality in NYC train tunnels\",\n"
                + "    \"encodingType\": \"application/vnd.geo+json\",\n"
                + "    \"feature\": {\n"
                + "        \"coordinates\": [51.08386,-114.13036],\n"
                + "        \"type\": \"Point\"\n"
                + "      }\n"
                + "}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json);
        assertTrue(result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && result.isSetProperty(pluginCoreModel.epFeature));
    }

    @Test
    void readFeatureOfInterstWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json);
        assertTrue(!result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && !result.isSetProperty(pluginCoreModel.epFeature));
    }

    @Test
    void readLocationBasic() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etLocation)
                .setProperty(pluginCoreModel.epName, "my backyard")
                .setProperty(pluginCoreModel.epDescription, "my backyard")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                .setProperty(pluginCoreModel.epLocation, TestHelper.jsonPoint(-117.123, 54.123));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etLocation, json));
    }

    @Test
    void readLocationWithAllValuesPresent() throws IOException {
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
        Entity result = entityParser.parseEntity(pluginCoreModel.etLocation, json);
        assertTrue(result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && result.isSetProperty(pluginCoreModel.epLocation));
    }

    @Test
    void readLocationWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etLocation, json);
        assertTrue(!result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && !result.isSetProperty(pluginCoreModel.epLocation));
    }

    @Test
    void readLocationWithLinkedThings() throws IOException {
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
        Entity thing = new DefaultEntity(pluginCoreModel.etThing)
                .setPrimaryKeyValues(PkValue.of(100L));
        EntitySet things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(thing);
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etLocation)
                .setProperty(pluginCoreModel.epName, "my backyard")
                .setProperty(pluginCoreModel.epDescription, "my backyard")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                .setProperty(pluginCoreModel.epLocation, TestHelper.jsonPoint(-117.123, 54.123))
                .setProperty(pluginCoreModel.npThingsLocation, things);
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etLocation, json));
    }

    @Test
    void readObservationWithAllValuesPresent() throws IOException {
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
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservation, json);
        assertTrue(result.isSetProperty(pluginCoreModel.epPhenomenonTime)
                && result.isSetProperty(pluginCoreModel.epResultTime)
                && result.isSetProperty(pluginCoreModel.epResult)
                && result.isSetProperty(pluginCoreModel.npDatastreamObservation)
                && result.isSetProperty(pluginCoreModel.npFeatureOfInterestObservation)
                && result.isSetProperty(pluginCoreModel.epParameters)
                && result.isSetProperty(pluginCoreModel.epPhenomenonTime)
                && result.isSetProperty(pluginCoreModel.epResultQuality)
                && result.isSetProperty(pluginCoreModel.epValidTime));
    }

    @Test
    void readObservationWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservation, json);
        assertTrue(!result.isSetProperty(pluginCoreModel.epPhenomenonTime)
                && !result.isSetProperty(pluginCoreModel.epResultTime)
                && !result.isSetProperty(pluginCoreModel.epResult)
                && !result.isSetProperty(pluginCoreModel.npDatastreamObservation)
                && !result.isSetProperty(pluginCoreModel.npFeatureOfInterestObservation)
                && !result.isSetProperty(pluginCoreModel.epParameters)
                && !result.isSetProperty(pluginCoreModel.epPhenomenonTime)
                && !result.isSetProperty(pluginCoreModel.epResultQuality)
                && !result.isSetProperty(pluginCoreModel.epValidTime));
    }

    @Test
    void readObservationWithLinks() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"Datastream\":{\"@iot.id\":100}\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(createTimeInstantUTC(2015, 04, 13, 0, 0, 0)))
                .setProperty(pluginCoreModel.epResultTime, createTimeInstantUTC(2015, 04, 13, 0, 0, 05))
                .setProperty(pluginCoreModel.epResult, 38)
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream)
                        .setPrimaryKeyValues(PkValue.of(100L)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));
    }

    @Test
    void readObservationWithIncorrectLink() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"Datastream\":[{\"@iot.id\":100}]\n"
                + "}";
        assertThrows(MismatchedInputException.class, () -> entityParser.parseEntity(pluginCoreModel.etObservation, json));
    }

    @Test
    void readObservationWithLinkedFeatureOfInterest() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"FeatureOfInterest\":{\"@iot.id\": 14269}\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(createTimeInstantUTC(2015, 04, 13, 0, 0, 0)))
                .setProperty(pluginCoreModel.epResultTime, createTimeInstantUTC(2015, 04, 13, 0, 0, 05))
                .setProperty(pluginCoreModel.epResult, 38)
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                        .setPrimaryKeyValues(PkValue.of(14269L)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));
    }

    @Test
    void readObservationWithFeatureOfInterest() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(createTimeInstantUTC(2015, 04, 13, 0, 0, 0)))
                .setProperty(pluginCoreModel.epResultTime, createTimeInstantUTC(2015, 04, 13, 0, 0, 05))
                .setProperty(pluginCoreModel.epResult, 99)
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                        .setProperty(pluginCoreModel.epName, "Turn 5, track surface temperature")
                        .setProperty(pluginCoreModel.epDescription, "Turn 5, track surface temperature")
                        .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://example.org/measurement_types#Measure")
                        .setProperty(pluginCoreModel.epFeature, new TextNode("tarmac")))
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream)
                        .setPrimaryKeyValues(PkValue.of(14314L)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));
    }

    @Test
    void readObservationPrecision() throws IOException {
        String json = "{\n"
                + "  \"result\" : 100.00\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epResult, new BigDecimal("100.00"));
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservation, json);
        assertEquals(expectedResult, result);

        json = "{\n"
                + "  \"result\" : 0.00\n"
                + "}";
        expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epResult, new BigDecimal("0.00"));
        result = entityParser.parseEntity(pluginCoreModel.etObservation, json);
        assertEquals(expectedResult, result);
    }

    @Test
    void readObservedPropertyBasic() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservedProperty)
                .setProperty(pluginCoreModel.epName, "ObservedPropertyUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "http://schema.org/description")
                .setProperty(pluginCoreModel.epDefinition, "Calibration date:  Jan 1, 2014");
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservedProperty, json));
    }

    @Test
    void readObservedPropertyWithLinks() throws IOException {
        String json = "{\n"
                + "    \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "    \"description\": \"http://schema.org/description\",\n"
                + "    \"definition\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservedProperty)
                .setProperty(pluginCoreModel.epName, "ObservedPropertyUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "http://schema.org/description")
                .setProperty(pluginCoreModel.epDefinition, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsObsProp,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setPrimaryKeyValues(PkValue.of(100L)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservedProperty, json));
    }

    @Test
    void readObservedPropertyWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservedProperty, json);
        assertTrue(result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.epDefinition));
    }

    @Test
    void readObservedPropertyWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservedProperty, json);
        assertTrue(!result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.epDefinition));
    }

    @Test
    void readSensorBasic() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etSensor)
                .setProperty(pluginCoreModel.epName, "SensorUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "SensorUp Tempomatic 2000")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  Jan 1, 2014");
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etSensor, json));
    }

    @Test
    void readSensorWithLinks() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etSensor)
                .setProperty(pluginCoreModel.epName, "SensorUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "SensorUp Tempomatic 2000")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsSensor,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setPrimaryKeyValues(PkValue.of(100L)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etSensor, json));

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
        expectedResult = new DefaultEntity(pluginCoreModel.etSensor)
                .setProperty(pluginCoreModel.epName, "SensorUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "SensorUp Tempomatic 2000")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsSensor,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setPrimaryKeyValues(PkValue.of(100L)))
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsSensor,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setPrimaryKeyValues(PkValue.of(101L)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etSensor, json));
    }

    @Test
    void readSensorWithBadLink() throws IOException {
        final String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": {\"@iot.id\":100}\n"
                + "}";
        assertThrows(MismatchedInputException.class, () -> entityParser.parseEntity(pluginCoreModel.etSensor, json));

        final String json2 = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"Datastreams\": [100]\n"
                + "}";
        assertThrows(MismatchedInputException.class, () -> entityParser.parseEntity(pluginCoreModel.etSensor, json2));
    }

    @Test
    void readSensorWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etSensor, json);
        assertTrue(result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && result.isSetProperty(pluginCoreModel.epMetadata));
    }

    @Test
    void readSensorWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etSensor, json);
        assertTrue(!result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && !result.isSetProperty(pluginCoreModel.epMetadata));
    }

    @Test
    void readThingBasic() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    }\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build());
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
    }

    @Test
    void readThingWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"camping lantern\",\n"
                + "    \"description\": \"camping lantern\",\n"
                + "    \"properties\": {\n"
                + "        \"property1\": \"it’s waterproof\",\n"
                + "        \"property2\": \"it glows in the dark\",\n"
                + "        \"property3\": \"it repels insects\"\n"
                + "    }\n"
                + "}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etThing, json);
        assertTrue(result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(ModelRegistry.EP_PROPERTIES));
    }

    @Test
    void readThingWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etThing, json);
        assertTrue(!result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(ModelRegistry.EP_PROPERTIES));
    }

    @Test
    void readThingWithNestedProperties() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", property3)
                        .build());
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
    }

    @Test
    void readThingWithLocation() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(
                        pluginCoreModel.npLocationsThing,
                        new DefaultEntity(pluginCoreModel.etLocation)
                                .setProperty(pluginCoreModel.epName, "my backyard")
                                .setProperty(pluginCoreModel.epDescription, "my backyard")
                                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                                .setProperty(pluginCoreModel.epLocation, TestHelper.jsonPoint(-117.123, 54.123)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
    }

    @Test
    void readThingWithLinks1() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(
                        pluginCoreModel.npLocationsThing,
                        new DefaultEntity(pluginCoreModel.etLocation)
                                .setPrimaryKeyValues(PkValue.of(100L)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
    }

    @Test
    void readThingWithLinks2() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsThing,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setPrimaryKeyValues(PkValue.of(100L)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
    }

    @Test
    void readThingWithLocationAndDatastream() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(
                        pluginCoreModel.npLocationsThing,
                        new DefaultEntity(pluginCoreModel.etLocation)
                                .setProperty(pluginCoreModel.epName, "my backyard")
                                .setProperty(pluginCoreModel.epDescription, "my backyard")
                                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                                .setProperty(pluginCoreModel.epLocation, TestHelper.jsonPoint(-117.123, 54.123)))
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsThing,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setProperty(pluginCoreModel.getEpUnitOfMeasurement(), new UnitOfMeasurement("Celsius", "C", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius"))
                                .setProperty(pluginCoreModel.epName, "Temperature measurement")
                                .setProperty(pluginCoreModel.epDescription, "Temperature measurement")
                                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                                .setProperty(
                                        pluginCoreModel.npObservedPropertyDatastream,
                                        new DefaultEntity(pluginCoreModel.etObservedProperty)
                                                .setProperty(pluginCoreModel.epName, "Temperature")
                                                .setProperty(pluginCoreModel.epDefinition, "http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                                .setProperty(pluginCoreModel.epDescription, "Temperature of the camping site"))
                                .setProperty(
                                        pluginCoreModel.npSensorDatastream,
                                        new DefaultEntity(pluginCoreModel.etSensor)
                                                .setProperty(pluginCoreModel.epName, "SensorUp Tempomatic 1000-b")
                                                .setProperty(pluginCoreModel.epDescription, "SensorUp Tempomatic 1000-b")
                                                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                                                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  Jan 11, 2015")));
        final Entity result = entityParser.parseEntity(pluginCoreModel.etThing, json);
        assertEquals(expectedResult, result);
    }

    @Test
    void readThingWithUnknownField() {
        assertThrows(UnrecognizedPropertyException.class, () -> {
            String json = "{ \"someField\": 123}";
            entityParser.parseEntity(pluginCoreModel.etThing, json);
        });
    }

    @Test
    void readSensorWithUnknownField() {
        assertThrows(UnrecognizedPropertyException.class, () -> {
            String json = "{ \"someField\": 123}";
            entityParser.parseEntity(pluginCoreModel.etSensor, json);
        });
    }

    @Test
    void readDatastreamWithUnknownField() {
        assertThrows(UnrecognizedPropertyException.class, () -> {
            String json = "{ \"someField\": 123}";
            entityParser.parseEntity(pluginCoreModel.etDatastream, json);
        });
    }

    @Test
    void readLocationWithUnknownField() {
        assertThrows(UnrecognizedPropertyException.class, () -> {
            String json = "{ \"someField\": 123}";
            entityParser.parseEntity(pluginCoreModel.etLocation, json);
        });
    }

    @Test
    void readFeatureOfInterestWithUnknownField() {
        assertThrows(UnrecognizedPropertyException.class, () -> {
            String json = "{ \"someField\": 123}";
            entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json);
        });
    }

    @Test
    void readHistoricalLocationWithUnknownField() {
        assertThrows(UnrecognizedPropertyException.class, () -> {
            String json = "{ \"someField\": 123}";
            entityParser.parseEntity(pluginCoreModel.etHistoricalLocation, json);
        });
    }

    @Test
    void readObservedPropertyWithUnknownField() {
        assertThrows(UnrecognizedPropertyException.class, () -> {
            String json = "{ \"someField\": 123}";
            entityParser.parseEntity(pluginCoreModel.etObservedProperty, json);
        });
    }

    @Test
    void readObservationWithUnknownField() {
        assertThrows(UnrecognizedPropertyException.class, () -> {
            String json = "{ \"someField\": 123}";
            entityParser.parseEntity(pluginCoreModel.etObservation, json);
        });
    }

    @Test
    void readEntityLongId() throws IOException {
        final PrimaryKey thingPrimaryKey = pluginCoreModel.etThing.getPrimaryKey();
        final EntityPropertyMain thingIdProperty = thingPrimaryKey.getKeyProperties().get(0);
        {
            long id = Long.MAX_VALUE;
            String json = "{\"@iot.id\": " + id + "}";
            Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing).setProperty(thingIdProperty, id);
            assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
        }
        {
            long id = Long.MIN_VALUE;
            String json = "{\"@iot.id\": " + id + "}";
            Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                    .setProperty(thingIdProperty, id);
            assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
        }
        {
            CoreSettings coreSettingsString = new CoreSettings();
            coreSettingsString.getPluginSettings().set("coreModel.idType", Constants.VALUE_ID_TYPE_STRING);
            ModelRegistry modelRegistryString = coreSettingsString.getModelRegistry();
            QueryDefaults queryDefaultsString = coreSettingsString.getQueryDefaults();
            queryDefaultsString.setUseAbsoluteNavigationLinks(false);
            PluginCoreModel pluginCoreModelString = new PluginCoreModel();
            pluginCoreModelString.init(coreSettingsString);
            coreSettingsString.getPluginManager().initPlugins(null);
            JsonReaderDefault entityParserString = new JsonReaderDefault(modelRegistryString);
            String id = UUID.randomUUID().toString();
            String json = "{\"@iot.id\": \"" + id + "\"}";
            Entity expectedResult = new DefaultEntity(pluginCoreModelString.etThing)
                    .setProperty(thingIdProperty, id);
            assertEquals(expectedResult, entityParserString.parseEntity(pluginCoreModelString.etThing, json));
        }
    }

}
