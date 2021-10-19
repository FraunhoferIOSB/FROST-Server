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
package de.fraunhofer.iosb.ilt.frostserver.coremodel;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class EntityParserTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;
    private static JsonReader entityParser;

    @BeforeClass
    public static void beforeClass() {
        coreSettings = new CoreSettings();
        modelRegistry = coreSettings.getModelRegistry();
        queryDefaults = coreSettings.getQueryDefaults();
        queryDefaults.setUseAbsoluteNavigationLinks(false);
        pluginCoreModel = new PluginCoreModel();
        pluginCoreModel.init(coreSettings);
        coreSettings.getPluginManager().initPlugins(null);
        entityParser = new JsonReader(modelRegistry);
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etDatastream)
                .setProperty(pluginCoreModel.epUnitOfMeasurement,
                        new UnitOfMeasurement("Percentage", "%", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html"))
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.epName, "Temperature measurement")
                .setProperty(pluginCoreModel.epDescription, "Temperature measurement")
                .setProperty(pluginCoreModel.npThingDatasteam, new DefaultEntity(pluginCoreModel.etThing).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(5394817)))
                .setProperty(pluginCoreModel.npObservedPropertyDatastream, new DefaultEntity(pluginCoreModel.etObservedProperty).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(5394816)))
                .setProperty(pluginCoreModel.npSensorDatastream, new DefaultEntity(pluginCoreModel.etSensor).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(Long.MAX_VALUE)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etDatastream, json));
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
        Entity result = entityParser.parseEntity(pluginCoreModel.etDatastream, json);
        Assert.assertTrue(result.isSetProperty(pluginCoreModel.epUnitOfMeasurement)
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
    public void readDatastreamWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etDatastream, json);
        Assert.assertTrue(!result.isSetProperty(pluginCoreModel.epUnitOfMeasurement)
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etDatastream)
                .setProperty(pluginCoreModel.epUnitOfMeasurement,
                        new UnitOfMeasurement("Percentage", "%", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html"))
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.epName, "Temperature measurement")
                .setProperty(pluginCoreModel.epDescription, "Temperature measurement")
                .setProperty(pluginCoreModel.npThingDatasteam, new DefaultEntity(pluginCoreModel.etThing).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(5394817)))
                .setProperty(pluginCoreModel.npObservedPropertyDatastream, new DefaultEntity(pluginCoreModel.etObservedProperty).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(5394816)))
                .setProperty(pluginCoreModel.npSensorDatastream, new DefaultEntity(pluginCoreModel.etSensor).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(5394815)))
                .setProperty(pluginCoreModel.epObservedArea, TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etDatastream, json));
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etDatastream)
                .setProperty(pluginCoreModel.epUnitOfMeasurement,
                        new UnitOfMeasurement("Celsius", "C", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius"))
                .setProperty(pluginCoreModel.epName, "Temperature measurement")
                .setProperty(pluginCoreModel.epDescription, "Temperature measurement")
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.npObservedPropertyDatastream,
                        new DefaultEntity(pluginCoreModel.etObservedProperty)
                                .setProperty(pluginCoreModel.epName, "Temperature")
                                .setProperty(pluginCoreModel.epDefinition, "http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                .setProperty(pluginCoreModel.epDescription, "Temperature of the camping site")
                )
                .setProperty(pluginCoreModel.npSensorDatastream,
                        new DefaultEntity(pluginCoreModel.etSensor)
                                .setProperty(pluginCoreModel.epDescription, "Sensor 101")
                                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  2011-11-11")
                );
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etDatastream, json));
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
            Entity expectedResult = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                    .setProperty(pluginCoreModel.epName, "Underground Air Quality in NYC train tunnels")
                    .setProperty(pluginCoreModel.epDescription, "Underground Air Quality in NYC train tunnels")
                    .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                    .setProperty(pluginCoreModel.epFeature, TestHelper.getPoint(51.08386, -114.13036));
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
                    .setProperty(pluginCoreModel.epFeature, TestHelper.getPoint(51.08386, -114.13036));
            assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json));
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
        Entity result = entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json);
        Assert.assertTrue(result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && result.isSetProperty(pluginCoreModel.epFeature));
    }

    @Test
    public void readFeatureOfInterstWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json);
        Assert.assertTrue(!result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && !result.isSetProperty(pluginCoreModel.epFeature));
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etLocation)
                .setProperty(pluginCoreModel.epName, "my backyard")
                .setProperty(pluginCoreModel.epDescription, "my backyard")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                .setProperty(pluginCoreModel.epLocation, TestHelper.getPoint(-117.123, 54.123));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etLocation, json));
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
        Entity result = entityParser.parseEntity(pluginCoreModel.etLocation, json);
        Assert.assertTrue(result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && result.isSetProperty(pluginCoreModel.epLocation));
    }

    @Test
    public void readLocationWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etLocation, json);
        Assert.assertTrue(!result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && !result.isSetProperty(pluginCoreModel.epLocation));
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
        Entity thing = new DefaultEntity(pluginCoreModel.etThing).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(100));
        EntitySet things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(thing);
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etLocation)
                .setProperty(pluginCoreModel.epName, "my backyard")
                .setProperty(pluginCoreModel.epDescription, "my backyard")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                .setProperty(pluginCoreModel.epLocation, TestHelper.getPoint(-117.123, 54.123))
                .setProperty(pluginCoreModel.npThingsLocation, things);
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etLocation, json));
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
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservation, json);
        Assert.assertTrue(result.isSetProperty(pluginCoreModel.epPhenomenonTime)
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
    public void readObservationWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservation, json);
        Assert.assertTrue(!result.isSetProperty(pluginCoreModel.epPhenomenonTime)
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
    public void readObservationWithLinks() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"Datastream\":{\"@iot.id\":100}\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epPhenomenonTime, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setProperty(pluginCoreModel.epResultTime, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setProperty(pluginCoreModel.epResult, 38)
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream)
                        .setProperty(ModelRegistry.EP_ID_LONG, new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));
    }

    @Test
    public void readObservationWithLinkedFeatureOfInterest() throws IOException {
        String json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"FeatureOfInterest\":{\"@iot.id\": 14269}\n"
                + "}";
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epPhenomenonTime, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setProperty(pluginCoreModel.epResultTime, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setProperty(pluginCoreModel.epResult, 38)
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                        .setProperty(ModelRegistry.EP_ID_LONG, new IdLong(14269)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epPhenomenonTime, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 0, DateTimeZone.UTC).getMillis()))
                .setProperty(pluginCoreModel.epResultTime, TimeInstant.create(new DateTime(2015, 04, 13, 0, 0, 05, DateTimeZone.UTC).getMillis()))
                .setProperty(pluginCoreModel.epResult, 99)
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                        .setProperty(pluginCoreModel.epName, "Turn 5, track surface temperature")
                        .setProperty(pluginCoreModel.epDescription, "Turn 5, track surface temperature")
                        .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://example.org/measurement_types#Measure")
                        .setProperty(pluginCoreModel.epFeature, "tarmac")
                )
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(14314)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));
    }

    @Test
    public void readObservationPrecision() throws IOException {
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
    public void readObservedPropertyBasic() throws IOException {
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
    public void readObservedPropertyWithLinks() throws IOException {
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
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream)
                        .setProperty(ModelRegistry.EP_ID_LONG, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservedProperty, json));
    }

    @Test
    public void readObservedPropertyWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "  \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "  \"description\": \"http://schema.org/description\",\n"
                + "  \"definition\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservedProperty, json);
        Assert.assertTrue(result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.epDefinition));
    }

    @Test
    public void readObservedPropertyWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etObservedProperty, json);
        Assert.assertTrue(!result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.epDefinition));
    }

    @Test
    public void readSensorBasic() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etSensor)
                .setProperty(pluginCoreModel.epName, "SensorUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "SensorUp Tempomatic 2000")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream)
                        .setProperty(ModelRegistry.EP_ID_LONG, new IdLong(100))
                );
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
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setProperty(ModelRegistry.EP_ID_LONG, new IdLong(100)))
                .addNavigationEntity(
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setProperty(ModelRegistry.EP_ID_LONG, new IdLong(101)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etSensor, json));
    }

    @Test
    public void readSensorWithAllValuesPresent() throws IOException {
        String json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\"\n"
                + "}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etSensor, json);
        Assert.assertTrue(result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && result.isSetProperty(pluginCoreModel.epMetadata));
    }

    @Test
    public void readSensorWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etSensor, json);
        Assert.assertTrue(!result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(ModelRegistry.EP_ENCODINGTYPE)
                && !result.isSetProperty(pluginCoreModel.epMetadata));
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
        Entity result = entityParser.parseEntity(pluginCoreModel.etThing, json);
        Assert.assertTrue(result.isSetProperty(pluginCoreModel.epName)
                && result.isSetProperty(pluginCoreModel.epDescription)
                && result.isSetProperty(ModelRegistry.EP_PROPERTIES));
    }

    @Test
    public void readThingWithAllValuesMissing() throws IOException {
        String json = "{}";
        Entity result = entityParser.parseEntity(pluginCoreModel.etThing, json);
        Assert.assertTrue(!result.isSetProperty(pluginCoreModel.epName)
                && !result.isSetProperty(pluginCoreModel.epDescription)
                && !result.isSetProperty(ModelRegistry.EP_PROPERTIES));
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etLocation)
                        .setProperty(pluginCoreModel.epName, "my backyard")
                        .setProperty(pluginCoreModel.epDescription, "my backyard")
                        .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                        .setProperty(pluginCoreModel.epLocation, TestHelper.getPoint(-117.123, 54.123))
                );
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etLocation)
                        .setProperty(ModelRegistry.EP_ID_LONG, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream)
                        .setProperty(ModelRegistry.EP_ID_LONG, new IdLong(100))
                );
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etLocation)
                        .setProperty(pluginCoreModel.epName, "my backyard")
                        .setProperty(pluginCoreModel.epDescription, "my backyard")
                        .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                        .setProperty(pluginCoreModel.epLocation, TestHelper.getPoint(-117.123, 54.123))
                )
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream)
                        .setProperty(pluginCoreModel.epUnitOfMeasurement,
                                new UnitOfMeasurement("Celsius", "C", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Celsius"))
                        .setProperty(pluginCoreModel.epName, "Temperature measurement")
                        .setProperty(pluginCoreModel.epDescription, "Temperature measurement")
                        .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                        .setProperty(pluginCoreModel.npObservedPropertyDatastream,
                                new DefaultEntity(pluginCoreModel.etObservedProperty)
                                        .setProperty(pluginCoreModel.epName, "Temperature")
                                        .setProperty(pluginCoreModel.epDefinition, "http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Temperature")
                                        .setProperty(pluginCoreModel.epDescription, "Temperature of the camping site")
                        )
                        .setProperty(pluginCoreModel.npSensorDatastream,
                                new DefaultEntity(pluginCoreModel.etSensor)
                                        .setProperty(pluginCoreModel.epName, "SensorUp Tempomatic 1000-b")
                                        .setProperty(pluginCoreModel.epDescription, "SensorUp Tempomatic 1000-b")
                                        .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                                        .setProperty(pluginCoreModel.epMetadata, "Calibration date:  Jan 11, 2015")
                        )
                );
        final Entity result = entityParser.parseEntity(pluginCoreModel.etThing, json);
        assertEquals(expectedResult, result);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readThingWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(pluginCoreModel.etThing, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readSensorWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(pluginCoreModel.etSensor, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readDatastreamWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(pluginCoreModel.etDatastream, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readLocationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(pluginCoreModel.etLocation, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readFeatureOfInterestWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(pluginCoreModel.etFeatureOfInterest, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readHistoricalLocationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(pluginCoreModel.etHistoricalLocation, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readObservedPropertyWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(pluginCoreModel.etObservedProperty, json);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void readObservationWithUnknownField() throws IOException {
        String json = "{ \"someField\": 123}";
        entityParser.parseEntity(pluginCoreModel.etObservation, json);
    }

    @Test
    public void readEntityLongId() throws IOException {
        {
            long id = Long.MAX_VALUE;
            String json = "{\"@iot.id\": " + id + "}";
            Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(id));
            assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
        }
        {
            long id = Long.MIN_VALUE;
            String json = "{\"@iot.id\": " + id + "}";
            Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing).setProperty(ModelRegistry.EP_ID_LONG, new IdLong(id));
            assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
        }
        {
            CoreSettings coreSettingsString = new CoreSettings();
            ModelRegistry modelRegistryString = coreSettingsString.getModelRegistry();
            QueryDefaults queryDefaultsString = coreSettingsString.getQueryDefaults();
            queryDefaultsString.setUseAbsoluteNavigationLinks(false);
            PluginCoreModel pluginCoreModelString = new PluginCoreModel();
            pluginCoreModelString.init(coreSettingsString);
            coreSettingsString.getPluginManager().initPlugins(null);
            JsonReader entityParserString = new JsonReader(modelRegistryString);
            String id = UUID.randomUUID().toString();
            String json = "{\"@iot.id\": \"" + id + "\"}";
            Entity expectedResult = new DefaultEntity(pluginCoreModelString.etThing).setProperty(modelRegistryString.EP_ID_STRING, new IdString(id));
            assertEquals(expectedResult, entityParserString.parseEntity(pluginCoreModelString.etThing, json));
        }
    }

}
