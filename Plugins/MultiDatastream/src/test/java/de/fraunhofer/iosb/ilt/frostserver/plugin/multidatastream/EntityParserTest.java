/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.MdsModelSettings.TAG_ENABLE_MDS_MODEL;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.TestHelper.createTimeInstantUTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static PluginMultiDatastream pluginMultiDatastream;
    private static JsonReader entityParser;
    private static EntityType etMultiDatastream;
    private static EntityPropertyMain epMultiObservationDataTypes;
    private static EntityPropertyMain epUnitOfMeasurements;
    private static NavigationPropertyEntity npMultiDatastreamObservation;
    private static NavigationPropertyEntity npThingMds;
    private static NavigationPropertyEntity npSensorMds;
    private static NavigationPropertyEntitySet npObservedPropertiesMds;
    private static NavigationPropertyEntitySet npMultiDatastreamsSensor;
    private static NavigationPropertyEntitySet npMultiDatastreamsObsProp;
    private static NavigationPropertyEntitySet npMultiDatastreamsThing;

    @BeforeAll
    public static void beforeClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            coreSettings.getSettings().getProperties().put("plugins." + TAG_ENABLE_MDS_MODEL, "true");
            modelRegistry = coreSettings.getModelRegistry();
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginMultiDatastream = new PluginMultiDatastream();
            pluginMultiDatastream.init(coreSettings);
            coreSettings.getPluginManager().initPlugins(null);
            entityParser = new JsonReader(modelRegistry);
            etMultiDatastream = modelRegistry.getEntityTypeForName("MultiDatastream");
            epMultiObservationDataTypes = etMultiDatastream.getEntityProperty("multiObservationDataTypes");
            epUnitOfMeasurements = etMultiDatastream.getEntityProperty("unitOfMeasurements");

            npThingMds = (NavigationPropertyEntity) etMultiDatastream.getNavigationProperty("Thing");
            npSensorMds = (NavigationPropertyEntity) etMultiDatastream.getNavigationProperty("Sensor");
            npObservedPropertiesMds = (NavigationPropertyEntitySet) etMultiDatastream.getNavigationProperty("ObservedProperties");

            npMultiDatastreamObservation = (NavigationPropertyEntity) pluginCoreModel.etObservation.getNavigationProperty("MultiDatastream");
            npMultiDatastreamsThing = (NavigationPropertyEntitySet) pluginCoreModel.etThing.getNavigationProperty("MultiDatastreams");
            npMultiDatastreamsSensor = (NavigationPropertyEntitySet) pluginCoreModel.etSensor.getNavigationProperty("MultiDatastreams");
            npMultiDatastreamsObsProp = (NavigationPropertyEntitySet) pluginCoreModel.etObservedProperty.getNavigationProperty("MultiDatastreams");
        }
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
                .setProperty(pluginCoreModel.npThingDatasteam, new DefaultEntity(pluginCoreModel.etThing).setProperty(pluginCoreModel.etThing.getPrimaryKey(), new IdLong(5394817)))
                .setProperty(pluginCoreModel.npObservedPropertyDatastream, new DefaultEntity(pluginCoreModel.etObservedProperty).setProperty(pluginCoreModel.etObservedProperty.getPrimaryKey(), new IdLong(5394816)))
                .setProperty(pluginCoreModel.npSensorDatastream, new DefaultEntity(pluginCoreModel.etSensor).setProperty(pluginCoreModel.etSensor.getPrimaryKey(), new IdLong(Long.MAX_VALUE)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etDatastream, json));
    }

    @Test
    void readMultiDatastreamWithObservedPropertyAndSensor() throws IOException {
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
        Entity expectedResult = new DefaultEntity(etMultiDatastream)
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")
                .setProperty(epUnitOfMeasurements, unitsOfMeasurement)
                .setProperty(pluginCoreModel.epName, "Wind")
                .setProperty(pluginCoreModel.epDescription, "Wind direction and speed")
                .setProperty(epMultiObservationDataTypes, observationTypes)
                .addNavigationEntity(npObservedPropertiesMds, new DefaultEntity(pluginCoreModel.etObservedProperty)
                        .setProperty(pluginCoreModel.epName, "Wind Direction")
                        .setProperty(pluginCoreModel.epDefinition, "SomeDefinition")
                        .setProperty(pluginCoreModel.epDescription, "Direction the wind blows, 0=North, 90=East."))
                .addNavigationEntity(npObservedPropertiesMds, new DefaultEntity(pluginCoreModel.etObservedProperty)
                        .setProperty(pluginCoreModel.epName, "Wind Speed")
                        .setProperty(pluginCoreModel.epDefinition, "SomeDefinition")
                        .setProperty(pluginCoreModel.epDescription, "Wind Speed"))
                .setProperty(npSensorMds, new DefaultEntity(pluginCoreModel.etSensor)
                        .setProperty(pluginCoreModel.epDescription, "Wind Sensor 101")
                        .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                        .setProperty(pluginCoreModel.epMetadata, "Calibration date:  2011-11-11"));
        assertEquals(expectedResult, entityParser.parseEntity(etMultiDatastream, json));
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
                        .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));

        json = "{\n"
                + "  \"phenomenonTime\": \"2015-04-13T00:00:00Z\",\n"
                + "  \"resultTime\" : \"2015-04-13T00:00:05Z\",\n"
                + "  \"result\" : 38,\n"
                + "  \"MultiDatastream\":{\"@iot.id\":100}\n"
                + "}";
        expectedResult = new DefaultEntity(pluginCoreModel.etObservation)
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(createTimeInstantUTC(2015, 04, 13, 0, 0, 0)))
                .setProperty(pluginCoreModel.epResultTime, createTimeInstantUTC(2015, 04, 13, 0, 0, 05))
                .setProperty(pluginCoreModel.epResult, 38)
                .setProperty(npMultiDatastreamObservation, new DefaultEntity(etMultiDatastream)
                        .setProperty(pluginMultiDatastream.etMultiDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));
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
                        .setProperty(pluginCoreModel.etFeatureOfInterest.getPrimaryKey(), new IdLong(14269)));
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
                        .setProperty(pluginCoreModel.epFeature, "tarmac"))
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream).setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(14314)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservation, json));
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
                .addNavigationEntity(pluginCoreModel.npDatastreamsObsProp, new DefaultEntity(pluginCoreModel.etDatastream)
                        .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservedProperty, json));

        json = "{\n"
                + "    \"name\": \"ObservedPropertyUp Tempomatic 2000\",\n"
                + "    \"description\": \"http://schema.org/description\",\n"
                + "    \"definition\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new DefaultEntity(pluginCoreModel.etObservedProperty)
                .setProperty(pluginCoreModel.epName, "ObservedPropertyUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "http://schema.org/description")
                .setProperty(pluginCoreModel.epDefinition, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(npMultiDatastreamsObsProp, new DefaultEntity(etMultiDatastream)
                        .setProperty(pluginMultiDatastream.etMultiDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etObservedProperty, json));

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
        expectedResult = new DefaultEntity(pluginCoreModel.etObservedProperty)
                .setProperty(pluginCoreModel.epName, "ObservedPropertyUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "http://schema.org/description")
                .setProperty(pluginCoreModel.epDefinition, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(pluginCoreModel.npDatastreamsObsProp, new DefaultEntity(pluginCoreModel.etDatastream)
                        .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(100)))
                .addNavigationEntity(npMultiDatastreamsObsProp, new DefaultEntity(etMultiDatastream)
                        .setProperty(pluginMultiDatastream.etMultiDatastream.getPrimaryKey(), new IdLong(100)));
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
                                .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(100)));
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
                                .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(100)))
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsSensor,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(101)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etSensor, json));

        json = "{\n"
                + "    \"name\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"description\": \"SensorUp Tempomatic 2000\",\n"
                + "    \"encodingType\": \"http://schema.org/description\",\n"
                + "    \"metadata\": \"Calibration date:  Jan 1, 2014\",\n"
                + "    \"MultiDatastreams\": [ \n"
                + "        {\"@iot.id\":100}\n"
                + "    ]\n"
                + "}";
        expectedResult = new DefaultEntity(pluginCoreModel.etSensor)
                .setProperty(pluginCoreModel.epName, "SensorUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "SensorUp Tempomatic 2000")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(
                        npMultiDatastreamsSensor,
                        new DefaultEntity(etMultiDatastream)
                                .setProperty(pluginMultiDatastream.etMultiDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etSensor, json));

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
        expectedResult = new DefaultEntity(pluginCoreModel.etSensor)
                .setProperty(pluginCoreModel.epName, "SensorUp Tempomatic 2000")
                .setProperty(pluginCoreModel.epDescription, "SensorUp Tempomatic 2000")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "http://schema.org/description")
                .setProperty(pluginCoreModel.epMetadata, "Calibration date:  Jan 1, 2014")
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsSensor,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(100)))
                .addNavigationEntity(
                        npMultiDatastreamsSensor,
                        new DefaultEntity(etMultiDatastream)
                                .setProperty(pluginMultiDatastream.etMultiDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etSensor, json));
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
                                .setProperty(pluginCoreModel.epLocation, TestHelper.getPoint(-117.123, 54.123)));
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
                                .setProperty(pluginCoreModel.etLocation.getPrimaryKey(), new IdLong(100)));
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
                                .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
    }

    @Test
    void readThingWithLinks3() throws IOException {
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
        Entity expectedResult = new DefaultEntity(pluginCoreModel.etThing)
                .setProperty(pluginCoreModel.epName, "camping lantern")
                .setProperty(pluginCoreModel.epDescription, "camping lantern")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("property1", "it’s waterproof")
                        .addProperty("property2", "it glows in the dark")
                        .addProperty("property3", "it repels insects")
                        .build())
                .addNavigationEntity(
                        npMultiDatastreamsThing,
                        new DefaultEntity(etMultiDatastream)
                                .setProperty(pluginMultiDatastream.etMultiDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
    }

    @Test
    void readThingWithLinks4() throws IOException {
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
                                .setProperty(pluginCoreModel.etDatastream.getPrimaryKey(), new IdLong(100)))
                .addNavigationEntity(
                        npMultiDatastreamsThing,
                        new DefaultEntity(etMultiDatastream)
                                .setProperty(pluginMultiDatastream.etMultiDatastream.getPrimaryKey(), new IdLong(100)));
        assertEquals(expectedResult, entityParser.parseEntity(pluginCoreModel.etThing, json));
    }

}
