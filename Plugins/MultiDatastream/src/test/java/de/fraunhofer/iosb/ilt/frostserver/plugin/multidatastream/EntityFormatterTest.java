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
import static net.time4j.tz.ZonalOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 * @author scf
 */
class EntityFormatterTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;
    private static PluginMultiDatastream pluginMultiDatastream;
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
    public static void initClass() {
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
    void writeThingBasicAbs() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/MultiDatastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/HistoricalLocations\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things(1)");
        Query query = new Query(modelRegistry, new QueryDefaults(true, false, 100, 1000), path).validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(modelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingBasicRel() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"Things(1)/MultiDatastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "}\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things(1)");
        Query query = new Query(modelRegistry, new QueryDefaults(false, false, 100, 1000), path).validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(modelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingSelect() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things(1)");
        Query query = QueryParser.parseQuery("$select=id,name", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(modelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingsBasic() throws IOException {
        String thing
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"Things(1)/MultiDatastreams\",\n"
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
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things");
        Query query = new Query(modelRegistry, queryDefaults, path).validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(modelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        EntitySet things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(entity);
        things.add(entity);
        assertTrue(jsonEqual(expResult, JsonWriter.writeEntityCollection(things, query)));
    }

    @Test
    void writeThingOnlyId() throws IOException {
        String expResult = "{\"@iot.id\": 1}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things(1)");
        Query query = QueryParser.parseQuery("$select=id", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build())
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream, new IdLong(2)));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingsWithExpandedDatastream() throws IOException {
        String thing
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
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
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\""
                + "}\n"
                + "],\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"Things(1)/MultiDatastreams\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
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
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$expand=Datastreams", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream)
                        .setQuery(query.getExpand().get(0).getSubQuery())
                        .setId(new IdLong(1))
                        .setProperty(pluginCoreModel.epName, "This is a datastream measuring the temperature in an oven.")
                        .setProperty(pluginCoreModel.epDescription, "This is a datastream measuring the temperature in an oven.")
                        .setProperty(pluginCoreModel.getEpUnitOfMeasurement(), new UnitOfMeasurement()
                                .setName("degree Celsius")
                                .setSymbol("°C")
                                .setDefinition("http://unitsofmeasure.org/ucum.html#para-30")
                        )
                        .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                        .setProperty(pluginCoreModel.epObservedArea, TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                        .setProperty(pluginCoreModel.epPhenomenonTimeDs, TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC))
                        .setProperty(pluginCoreModel.epResultTimeDs, TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC))
                )
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        ((EntitySet) entity.getProperty(pluginCoreModel.npDatastreamsThing)).setCount(1);
        EntitySet things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(entity);
        things.setCount(1);
        compareJson(expResult, JsonWriter.writeEntityCollection(things, query));
    }

    @Test
    void writeThingWithExpandedDatastream1() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123}],\n"
                + "\"name\": \"This thing is an oven.\",\n"
                + "\"description\": \"This thing is an oven.\",\n"
                + "\"properties\": {\n"
                + "\"owner\": \"John Doe\",\n"
                + "\"color\": \"Silver\"\n"
                + "},\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"MultiDatastreams@iot.navigationLink\": \"Things(1)/MultiDatastreams\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$expand=Datastreams($select=id)", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.npLocationsThing, new EntitySetImpl(pluginCoreModel.etLocation))
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream)
                        .setQuery(query.getExpand().get(0).getSubQuery())
                        .setId(new IdLong(123))
                )
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingWithExpandedDatastream2() throws IOException {
        String expResult = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123}],\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=id,name,Locations&$expand=Datastreams($select=id)", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream)
                        .setQuery(query.getExpand().get(0).getSubQuery())
                        .setId(new IdLong(123))
                )
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingWithExpandedDatastream3() throws IOException {
        String expResult = "{\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams\": [{\"@iot.id\":123, \"@iot.selfLink\": \"http://example.org/v1.0/Datastreams(123)\"}],\n"
                + "\"name\": \"This thing is an oven.\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=@iot.selfLink,name,Locations&$expand=Datastreams($select=@iot.selfLink,id)", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.npLocationsThing, new EntitySetImpl(pluginCoreModel.etLocation))
                .addNavigationEntity(new DefaultEntity(pluginCoreModel.etDatastream)
                        .setQuery(query.getExpand().get(0).getSubQuery())
                        .setId(new IdLong(123))
                )
                .setProperty(pluginCoreModel.npHistoricalLocationsThing, new EntitySetImpl(pluginCoreModel.etHistoricalLocation))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingWithExpandedDatastream4() throws IOException {
        String expResult = "{\n"
                + "  \"@iot.id\": 1,\n"
                + "  \"Datastreams\": [],\n"
                + "  \"name\": \"This thing is an oven.\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=id,name&$expand=Datastreams", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.npLocationsThing, new EntitySetImpl(pluginCoreModel.etLocation))
                .setProperty(pluginCoreModel.npHistoricalLocationsThing, new EntitySetImpl(pluginCoreModel.etHistoricalLocation))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeMultiDatastreamBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/MultiDatastreams(1)\",\n"
                + "	\"Thing@iot.navigationLink\": \"MultiDatastreams(1)/Thing\",\n"
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
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/MultiDatastreams(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(etMultiDatastream)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(npThingMds, new DefaultEntity(pluginCoreModel.etThing, new IdLong(1)))
                .setProperty(npSensorMds, new DefaultEntity(pluginCoreModel.etSensor, new IdLong(1)))
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")
                .setProperty(pluginCoreModel.epName, "This is a datastream measuring the wind.")
                .setProperty(pluginCoreModel.epDescription, "This is a datastream measuring wind direction and speed.")
                .setProperty(epUnitOfMeasurements, Arrays.asList(
                        new UnitOfMeasurement("DegreeAngle", "deg", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeAngle"),
                        new UnitOfMeasurement("MeterPerSecond", "m/s", "http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#MeterPerSecond")
                ))
                .setProperty(epMultiObservationDataTypes, Arrays.asList(
                        "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
                        "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement"
                ))
                .setProperty(pluginCoreModel.epPhenomenonTimeDs, TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC))
                .setProperty(pluginCoreModel.epResultTimeDs, TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeSensorBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Sensors(1)\",\n"
                + "	\"Datastreams@iot.navigationLink\": \"Sensors(1)/Datastreams\",\n"
                + "	\"MultiDatastreams@iot.navigationLink\": \"Sensors(1)/MultiDatastreams\",\n"
                + "	\"name\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"description\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"encodingType\": \"application/pdf\",\n"
                + "	\"metadata\": \"http://example.org/TMP35_36_37.pdf\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Sensors(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etSensor)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "TMP36 - Analog Temperature sensor")
                .setProperty(pluginCoreModel.epDescription, "TMP36 - Analog Temperature sensor")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/pdf")
                .setProperty(pluginCoreModel.epMetadata, "http://example.org/TMP35_36_37.pdf");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeSensorEmptyDatastreamsCollection() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"name\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"description\": \"TMP36 - Analog Temperature sensor\",\n"
                + "	\"encodingType\": \"application/pdf\",\n"
                + "	\"metadata\": \"http://example.org/TMP35_36_37.pdf\"\n,"
                + " \"Datastreams\": []"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Sensors(1)");
        Query query = QueryParser.parseQuery("$select=id,name,description,encodingType,metadata&$expand=Datastreams", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etSensor)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "TMP36 - Analog Temperature sensor")
                .setProperty(pluginCoreModel.epDescription, "TMP36 - Analog Temperature sensor")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/pdf")
                .setProperty(pluginCoreModel.epMetadata, "http://example.org/TMP35_36_37.pdf");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeObservedPropertyBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/ObservedProperties(1)\",\n"
                + "	\"Datastreams@iot.navigationLink\": \"ObservedProperties(1)/Datastreams\",\n"
                + "	\"MultiDatastreams@iot.navigationLink\": \"ObservedProperties(1)/MultiDatastreams\",\n"
                + "	\"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\",\n"
                + "	\"name\": \"DewPoint Temperature\",\n"
                + "	\"definition\": \"http://dbpedia.org/page/Dew_point\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/ObservedProperties(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservedProperty)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epDescription, "The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.")
                .setProperty(pluginCoreModel.epName, "DewPoint Temperature")
                .setProperty(pluginCoreModel.epDefinition, "http://dbpedia.org/page/Dew_point");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeObservationBasicDs() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"Datastream@iot.navigationLink\":\"Observations(1)/Datastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59Z\",\n"
                + "	\"resultTime\": \"2014-12-31T19:59:59Z\",\n"
                + "	\"result\": 70.40\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservation)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream, new IdLong(1)))
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest, new IdLong(1)))
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59)))
                .setProperty(pluginCoreModel.epResultTime, TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setProperty(pluginCoreModel.epResult, new BigDecimal("70.40"));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeObservationBasicMds() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"MultiDatastream@iot.navigationLink\":\"Observations(1)/MultiDatastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59Z\",\n"
                + "	\"resultTime\": \"2014-12-31T19:59:59Z\",\n"
                + "	\"result\": 70.40\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservation)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(npMultiDatastreamObservation, new DefaultEntity(etMultiDatastream, new IdLong(1)))
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest, new IdLong(1)))
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59)))
                .setProperty(pluginCoreModel.epResultTime, TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setProperty(pluginCoreModel.epResult, new BigDecimal("70.40"));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeObservationBasicWithNullResult() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"MultiDatastream@iot.navigationLink\":\"Observations(1)/MultiDatastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59Z\",\n"
                + "	\"resultTime\": \"2014-12-31T19:59:59Z\",\n"
                + "	\"result\": null\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservation)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(npMultiDatastreamObservation, new DefaultEntity(etMultiDatastream, new IdLong(1)))
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest, new IdLong(1)))
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59)))
                .setProperty(pluginCoreModel.epResultTime, TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setProperty(pluginCoreModel.epResult, null);
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeObservationWithEmptyResultTime() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/Observations(1)\",\n"
                + "	\"FeatureOfInterest@iot.navigationLink\": \"Observations(1)/FeatureOfInterest\",\n"
                + "	\"MultiDatastream@iot.navigationLink\":\"Observations(1)/MultiDatastream\",\n"
                + "	\"phenomenonTime\": \"2014-12-31T11:59:59Z\",\n"
                + "	\"resultTime\": null,\n"
                + "	\"result\": \"70.4\"\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservation)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(npMultiDatastreamObservation, new DefaultEntity(etMultiDatastream, new IdLong(1)))
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest, new IdLong(1)))
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59)))
                .setProperty(pluginCoreModel.epResultTime, new TimeInstant(null))
                .setProperty(pluginCoreModel.epResult, "70.4");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeFeatureOfInterstBasic() throws IOException {
        String expResult
                = "{\n"
                + "	\"@iot.id\": 1,\n"
                + "	\"@iot.selfLink\": \"http://example.org/v1.0/FeaturesOfInterest(1)\",\n"
                + "	\"Observations@iot.navigationLink\": \"FeaturesOfInterest(1)/Observations\",\n"
                + "	\"name\": \"This is a weather station.\",\n"
                + "	\"description\": \"This is a weather station.\",\n"
                + "	\"encodingType\": \"application/geo+json\""
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/FeaturesOfInterest(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "This is a weather station.")
                .setProperty(pluginCoreModel.epDescription, "This is a weather station.")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/geo+json");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeFeatureOfInterstWithGeoJsonPointFeature() throws IOException {
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
                + "		\"properties\": {},\n"
                + "		\"geometry\":\n"
                + "		{\n"
                + "			\"type\": \"Point\",\n"
                + "			\"coordinates\": [-114.06,51.05]\n"
                + "		}\n"
                + "	}\n"
                + "}";
        ResourcePath path = PathParser.parsePath(modelRegistry, "http://example.org", Version.V_1_0, "/FeaturesOfInterest(1)");
        Query query = QueryParser.parseQuery("", coreSettings, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                .setQuery(query)
                .setId(new IdLong(1))
                .setProperty(pluginCoreModel.epName, "This is a weather station.")
                .setProperty(pluginCoreModel.epDescription, "This is a weather station.")
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, "application/vnd.geo+json")
                .setProperty(pluginCoreModel.epFeature, TestHelper.getFeatureWithPoint(-114.06, 51.05));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    private void compareJson(String expected, String result) {
        assertTrue(jsonEqual(expected, result), () -> "Expected:\n" + expected + "\nReceived:\n" + result + "\n");
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
