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

import static net.time4j.tz.OffsetSign.BEHIND_UTC;
import static net.time4j.tz.ZonalOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.math.BigDecimal;
import net.time4j.tz.ZonalOffset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class EntityFormatterTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntityFormatterTest.class.getName());

    private static CoreSettings coreSettings;
    private static ServiceContext context;
    private static PluginCoreModel pluginCoreModel;

    @BeforeAll
    static void initClass() {
        coreSettings = new CoreSettings();
        coreSettings.getQueryDefaults()
                .setServiceRootUrl("http://example.org/v1.0/")
                .setUseAbsoluteNavigationLinks(false);
        context = new ServiceContext()
                .setModelRegistry(coreSettings.getModelRegistry())
                .setFunctionRegistry(coreSettings.getFunctionRegistry())
                .setQueryDefaults(coreSettings.getQueryDefaults())
                .setPrefixGen(() -> "http://example.org/v1.0/");
        pluginCoreModel = new PluginCoreModel();
        pluginCoreModel.init(coreSettings);
        coreSettings.getPluginManager().initPlugins(null);
    }

    @Test
    void writeThingBasicAbs() {
        String expResult = """
                {
                "@iot.id": 1,
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Locations@iot.navigationLink": "http://example.org/v1.0/Things(1)/Locations",
                "Datastreams@iot.navigationLink": "http://example.org/v1.0/Things(1)/Datastreams",
                "HistoricalLocations@iot.navigationLink": "http://example.org/v1.0/Things(1)/HistoricalLocations",
                "name": "This thing is an oven.",
                "description": "This thing is an oven.",
                "properties": {
                "owner": "John Doe",
                "color": "Silver"
                }
                }""";
        context.getQueryDefaults().setUseAbsoluteNavigationLinks(true);
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things(1)");
        Query query = new Query(context, path).validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        compareJson(expResult, JsonWriter.writeEntity(entity));
        context.getQueryDefaults().setUseAbsoluteNavigationLinks(false);
    }

    @Test
    void writeThingBasicRel() {
        String expResult = """
                {
                "@iot.id": 1,
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Locations@iot.navigationLink": "Things(1)/Locations",
                "Datastreams@iot.navigationLink": "Things(1)/Datastreams",
                "HistoricalLocations@iot.navigationLink": "Things(1)/HistoricalLocations",
                "name": "This thing is an oven.",
                "description": "This thing is an oven.",
                "properties": {
                "owner": "John Doe",
                "color": "Silver"
                }
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things(1)");
        Query query = new Query(context, path).validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingSelect() {
        String expResult = """
                {
                "@iot.id": 1,
                "name": "This thing is an oven."
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things(1)");
        Query query = QueryParser.parseQuery("$select=id,name", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingsBasic() {
        String thing = """
                {
                "@iot.id": 1,
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Locations@iot.navigationLink": "Things(1)/Locations",
                "Datastreams@iot.navigationLink": "Things(1)/Datastreams",
                "HistoricalLocations@iot.navigationLink": "Things(1)/HistoricalLocations",
                "name": "This thing is an oven.",
                "description": "This thing is an oven.",
                "properties": {
                "owner": "John Doe",
                "color": "Silver"
                }
                }""";
        String expResult = "{ \"value\":[\n"
                + thing + ",\n"
                + thing
                + "]}";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things");
        Query query = new Query(context, path).validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        EntitySet things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(entity);
        things.add(entity);
        assertTrue(jsonEqual(expResult, JsonWriter.writeEntityCollection(things, query)));
    }

    @Test
    void writeThingOnlyId() {
        String expResult = "{\"@iot.id\": 1}";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things(1)");
        Query query = QueryParser.parseQuery("$select=id", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode())
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsThing,
                        new DefaultEntity(pluginCoreModel.etDatastream, PkValue.of(2L)));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingsWithExpandedDatastream() {
        String thing = """
                {
                "@iot.id": 1,
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Datastreams@iot.count":1,
                "Datastreams": [
                {
                    "@iot.id":1,
                    "@iot.selfLink": "http://example.org/v1.0/Datastreams(1)",
                    "name": "This is a datastream measuring the temperature in an oven.",
                    "description": "This is a datastream measuring the temperature in an oven.",
                    "unitOfMeasurement": {
                        "name": "degree Celsius",
                        "symbol": "\u00b0C",
                        "definition": "http://unitsofmeasure.org/ucum.html#para-30"
                    },
                    "observationType": "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
                    "observedArea": {
                        "type": "Polygon",
                        "coordinates": [[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]
                    },
                    "phenomenonTime": "2014-03-01T13:00:00Z/2015-05-11T15:30:00Z",
                    "resultTime": "2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"}
                ],
                "Datastreams@iot.navigationLink": "Things(1)/Datastreams",
                "Locations@iot.navigationLink": "Things(1)/Locations",
                "HistoricalLocations@iot.navigationLink": "Things(1)/HistoricalLocations",
                "name": "This thing is an oven.",
                "description": "This thing is an oven.",
                "properties": {
                "owner": "John Doe",
                "color": "Silver"
                }
                }""";
        String expResult = "{ "
                + "\"@iot.count\": 1,\n"
                + "\"value\":[\n"
                + thing
                + "]}";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$expand=Datastreams", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsThing,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setQuery(query.getExpand().get(0).getSubQuery())
                                .setPrimaryKeyValues(PkValue.of(1L))
                                .setProperty(pluginCoreModel.epName, "This is a datastream measuring the temperature in an oven.")
                                .setProperty(pluginCoreModel.epDescription, "This is a datastream measuring the temperature in an oven.")
                                .setProperty(pluginCoreModel.epUnitOfMeasurement, UnitOfMeasurement.create("degree Celsius", "°C", "http://unitsofmeasure.org/ucum.html#para-30"))
                                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                                .setProperty(pluginCoreModel.epObservedArea, TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                                .setProperty(pluginCoreModel.epPhenomenonTimeDs, TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC))
                                .setProperty(pluginCoreModel.epResultTimeDs, TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC)))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        ((EntitySet) entity.getProperty(pluginCoreModel.npDatastreamsThing)).setCount(1);
        EntitySet things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(entity);
        things.setCount(1);
        compareJson(expResult, JsonWriter.writeEntityCollection(things, query));
    }

    @Test
    void writeThingWithExpandedDatastream1() {
        String expResult = """
                {
                "@iot.id": 1,
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Datastreams": [{"@iot.id":123}],
                "name": "This thing is an oven.",
                "description": "This thing is an oven.",
                "properties": {
                "owner": "John Doe",
                "color": "Silver"
                },
                "Datastreams@iot.navigationLink": "Things(1)/Datastreams",
                "Locations@iot.navigationLink": "Things(1)/Locations",
                "HistoricalLocations@iot.navigationLink": "Things(1)/HistoricalLocations"
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$expand=Datastreams($select=id)", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npLocationsThing, new EntitySetImpl(pluginCoreModel.etLocation))
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsThing,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setQuery(query.getExpand().get(0).getSubQuery())
                                .setPrimaryKeyValues(PkValue.of(123)))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingWithExpandedDatastream2() {
        String expResult = """
                {
                "@iot.id": 1,
                "Locations@iot.navigationLink": "Things(1)/Locations",
                "Datastreams": [{"@iot.id":123}],
                "name": "This thing is an oven."
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=id,name,Locations&$expand=Datastreams($select=id)", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsThing,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setQuery(query.getExpand().get(0).getSubQuery())
                                .setPrimaryKeyValues(PkValue.of(123)))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingWithExpandedDatastream3() {
        String expResult = """
                {
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Locations@iot.navigationLink": "Things(1)/Locations",
                "Datastreams": [{"@iot.id":123, "@iot.selfLink": "http://example.org/v1.0/Datastreams(123)"}],
                "name": "This thing is an oven."
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=@iot.selfLink,name,Locations&$expand=Datastreams($select=@iot.selfLink,id)", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npLocationsThing, new EntitySetImpl(pluginCoreModel.etLocation))
                .addNavigationEntity(
                        pluginCoreModel.npDatastreamsThing,
                        new DefaultEntity(pluginCoreModel.etDatastream)
                                .setQuery(query.getExpand().get(0).getSubQuery())
                                .setPrimaryKeyValues(PkValue.of(123)))
                .setProperty(pluginCoreModel.npHistoricalLocationsThing, new EntitySetImpl(pluginCoreModel.etHistoricalLocation))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeThingWithExpandedDatastream4() {
        String expResult = """
                {
                  "@iot.id": 1,
                  "Datastreams": [],
                  "name": "This thing is an oven."
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Things");
        Query query = QueryParser.parseQuery("$select=id,name&$expand=Datastreams", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npLocationsThing, new EntitySetImpl(pluginCoreModel.etLocation))
                .setProperty(pluginCoreModel.npHistoricalLocationsThing, new EntitySetImpl(pluginCoreModel.etHistoricalLocation))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeLocationBasic() {
        {
            String expResult = """
                    {
                        "@iot.id": 1,
                        "@iot.selfLink": "http://example.org/v1.0/Locations(1)",
                        "Things@iot.navigationLink": "Locations(1)/Things",
                        "HistoricalLocations@iot.navigationLink": "Locations(1)/HistoricalLocations",
                        "encodingType": "application/vnd.geo+json"}""";
            ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Locations(1)");
            Query query = QueryParser.parseQuery("$select=id,@iot.selfLink,encodingType,Things,HistoricalLocations", context, path)
                    .validate();
            DefaultEntity entity = new DefaultEntity(pluginCoreModel.etLocation)
                    .setQuery(query)
                    .setPrimaryKeyValues(PkValue.of(1L))
                    .setProperty(pluginCoreModel.npThingsLocation, new EntitySetImpl(pluginCoreModel.etThing))
                    .setProperty(pluginCoreModel.npHistoricalLocationsLocation, new EntitySetImpl(pluginCoreModel.etHistoricalLocation))
                    .setProperty(StandardProperties.EP_ENCODINGTYPE, "application/vnd.geo+json");
            compareJson(expResult, JsonWriter.writeEntity(entity));
        }
        {
            String expResult = """
                    {
                        "@iot.id": 1,
                        "@iot.selfLink": "http://example.org/v1.0/Locations(1)",
                        "Things@iot.navigationLink": "Locations(1)/Things",
                        "HistoricalLocations@iot.navigationLink": "Locations(1)/HistoricalLocations",
                        "encodingType": "application/geo+json"}""";
            ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Locations(1)");
            Query query = QueryParser.parseQuery("", context, path)
                    .validate();
            DefaultEntity entity = new DefaultEntity(pluginCoreModel.etLocation)
                    .setQuery(query)
                    .setPrimaryKeyValues(PkValue.of(1L))
                    .setProperty(StandardProperties.EP_ENCODINGTYPE, "application/geo+json");
            compareJson(expResult, JsonWriter.writeEntity(entity));
        }
    }

    @Test
    void writeLocationWithGeoJsonLocation() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/Locations(1)",
                    "Things@iot.navigationLink": "Locations(1)/Things",
                    "HistoricalLocations@iot.navigationLink": "Locations(1)/HistoricalLocations",
                    "encodingType": "application/geo+json",
                    "location":
                    {
                        "type": "Feature",
                        "properties": {},
                        "geometry":
                        {
                            "type": "Point",
                            "coordinates": [-114.06,51.05]
                        }
                    }
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Locations(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etLocation)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(StandardProperties.EP_ENCODINGTYPE, "application/geo+json")
                .setProperty(pluginCoreModel.epLocation, TestHelper.getFeatureWithPoint(-114.06, 51.05));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeHistoricalLocationBasic() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/HistoricalLocations(1)",
                    "Locations@iot.navigationLink": "HistoricalLocations(1)/Locations",
                    "Thing@iot.navigationLink": "HistoricalLocations(1)/Thing",
                    "time": "2015-01-25T19:00:00Z"
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/HistoricalLocations(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etHistoricalLocation)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npThingHistLoc, new DefaultEntity(pluginCoreModel.etThing, PkValue.of(1)))
                .setProperty(pluginCoreModel.epTime, TestHelper.createTimeInstant(2015, 01, 25, 12, 0, 0, ZonalOffset.ofHours(BEHIND_UTC, 7)));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeDatastreamBasic() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/Datastreams(1)",
                    "Thing@iot.navigationLink": "Datastreams(1)/Thing",
                    "Sensor@iot.navigationLink": "Datastreams(1)/Sensor",
                    "ObservedProperty@iot.navigationLink": "Datastreams(1)/ObservedProperty",
                    "Observations@iot.navigationLink": "Datastreams(1)/Observations",
                    "name": "This is a datastream measuring the temperature in an oven.",
                    "description": "This is a datastream measuring the temperature in an oven.",
                    "unitOfMeasurement":
                    {
                        "name": "degree Celsius",
                        "symbol": "\u00b0C",
                        "definition": "http://unitsofmeasure.org/ucum.html#para-30"
                    },
                    "observationType": "http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement",
                    "phenomenonTime": "2014-03-01T13:00:00Z/2015-05-11T15:30:00Z",
                    "resultTime": "2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Datastreams(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etDatastream)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npThingDatasteam, new DefaultEntity(pluginCoreModel.etThing, PkValue.of(1)))
                .setProperty(pluginCoreModel.npSensorDatastream, new DefaultEntity(pluginCoreModel.etSensor, PkValue.of(1)))
                .setProperty(pluginCoreModel.npObservedPropertyDatastream, new DefaultEntity(pluginCoreModel.etObservedProperty, PkValue.of(1)))
                .setProperty(pluginCoreModel.epName, "This is a datastream measuring the temperature in an oven.")
                .setProperty(pluginCoreModel.epDescription, "This is a datastream measuring the temperature in an oven.")
                .setProperty(pluginCoreModel.epUnitOfMeasurement, UnitOfMeasurement.create("degree Celsius", "°C", "http://unitsofmeasure.org/ucum.html#para-30"))
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.epPhenomenonTimeDs, TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC))
                .setProperty(pluginCoreModel.epResultTimeDs, TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeDatastreamWithEmptyUnitOfMeasurement() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/Datastreams(1)",
                    "Thing@iot.navigationLink": "Datastreams(1)/Thing",
                    "Sensor@iot.navigationLink": "Datastreams(1)/Sensor",
                    "ObservedProperty@iot.navigationLink": "Datastreams(1)/ObservedProperty",
                    "Observations@iot.navigationLink": "Datastreams(1)/Observations",
                    "name": "This is a datastream measuring the temperature in an oven.",
                    "description": "This is a datastream measuring the temperature in an oven.",
                    "unitOfMeasurement":
                    {
                        "name": null,
                        "symbol": null,
                        "definition": null
                    },
                    "observationType": "http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement",
                    "phenomenonTime": "2014-03-01T13:00:00Z/2015-05-11T15:30:00Z",
                    "resultTime": "2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Datastreams(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etDatastream)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npThingDatasteam, new DefaultEntity(pluginCoreModel.etThing, PkValue.of(1)))
                .setProperty(pluginCoreModel.npSensorDatastream, new DefaultEntity(pluginCoreModel.etSensor, PkValue.of(1)))
                .setProperty(pluginCoreModel.npObservedPropertyDatastream, new DefaultEntity(pluginCoreModel.etObservedProperty, PkValue.of(1)))
                .setProperty(pluginCoreModel.epUnitOfMeasurement, UnitOfMeasurement.create(null, null, null))
                .setProperty(pluginCoreModel.epName, "This is a datastream measuring the temperature in an oven.")
                .setProperty(pluginCoreModel.epDescription, "This is a datastream measuring the temperature in an oven.")
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.epPhenomenonTimeDs, TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC))
                .setProperty(pluginCoreModel.epResultTimeDs, TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeDatastreamWithObservedAreaGeoJsonPolygon() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/Datastreams(1)",
                    "Thing@iot.navigationLink": "Datastreams(1)/Thing",
                    "Sensor@iot.navigationLink": "Datastreams(1)/Sensor",
                    "ObservedProperty@iot.navigationLink": "Datastreams(1)/ObservedProperty",
                    "Observations@iot.navigationLink": "Datastreams(1)/Observations",
                    "name": "This is a datastream measuring the temperature in an oven.",
                    "description": "This is a datastream measuring the temperature in an oven.",
                    "unitOfMeasurement":
                    {
                        "name": "degree Celsius",
                        "symbol": "\u00b0C",
                        "definition": "http://unitsofmeasure.org/ucum.html#para-30"
                    },
                    "observationType": "http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement",
                    "observedArea":
                    {
                        "type": "Polygon",
                        "coordinates": [[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]
                    },
                    "phenomenonTime": "2014-03-01T13:00:00Z/2015-05-11T15:30:00Z",
                    "resultTime": "2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Datastreams(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etDatastream)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npThingDatasteam, new DefaultEntity(pluginCoreModel.etThing, PkValue.of(1)))
                .setProperty(pluginCoreModel.npSensorDatastream, new DefaultEntity(pluginCoreModel.etSensor, PkValue.of(1)))
                .setProperty(pluginCoreModel.npObservedPropertyDatastream, new DefaultEntity(pluginCoreModel.etObservedProperty, PkValue.of(1)))
                .setProperty(pluginCoreModel.epName, "This is a datastream measuring the temperature in an oven.")
                .setProperty(pluginCoreModel.epDescription, "This is a datastream measuring the temperature in an oven.")
                .setProperty(pluginCoreModel.epUnitOfMeasurement, UnitOfMeasurement.create("degree Celsius", "°C", "http://unitsofmeasure.org/ucum.html#para-30"))
                .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement")
                .setProperty(pluginCoreModel.epObservedArea, TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                .setProperty(pluginCoreModel.epPhenomenonTimeDs, TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC))
                .setProperty(pluginCoreModel.epResultTimeDs, TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, UTC));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeSensorBasic() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/Sensors(1)",
                    "Datastreams@iot.navigationLink": "Sensors(1)/Datastreams",
                    "name": "TMP36 - Analog Temperature sensor",
                    "description": "TMP36 - Analog Temperature sensor",
                    "encodingType": "application/pdf",
                    "metadata": "http://example.org/TMP35_36_37.pdf"
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Sensors(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etSensor)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "TMP36 - Analog Temperature sensor")
                .setProperty(pluginCoreModel.epDescription, "TMP36 - Analog Temperature sensor")
                .setProperty(StandardProperties.EP_ENCODINGTYPE, "application/pdf")
                .setProperty(pluginCoreModel.epMetadata, "http://example.org/TMP35_36_37.pdf");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeSensorEmptyDatastreamsCollection() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "name": "TMP36 - Analog Temperature sensor",
                    "description": "TMP36 - Analog Temperature sensor",
                    "encodingType": "application/pdf",
                    "metadata": "http://example.org/TMP35_36_37.pdf"
                , "Datastreams": []}""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Sensors(1)");
        Query query = QueryParser.parseQuery("$select=id,name,description,encodingType,metadata&$expand=Datastreams", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etSensor)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "TMP36 - Analog Temperature sensor")
                .setProperty(pluginCoreModel.epDescription, "TMP36 - Analog Temperature sensor")
                .setProperty(StandardProperties.EP_ENCODINGTYPE, "application/pdf")
                .setProperty(pluginCoreModel.epMetadata, "http://example.org/TMP35_36_37.pdf");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeObservedPropertyBasic() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/ObservedProperties(1)",
                    "Datastreams@iot.navigationLink": "ObservedProperties(1)/Datastreams",
                    "description": "The dewpoint temperature bla bla",
                    "name": "DewPoint Temperature",
                    "definition": "http://dbpedia.org/page/Dew_point"
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/ObservedProperties(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservedProperty)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epDescription, "The dewpoint temperature bla bla")
                .setProperty(pluginCoreModel.epName, "DewPoint Temperature")
                .setProperty(pluginCoreModel.epDefinition, "http://dbpedia.org/page/Dew_point");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeObservationBasic() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/Observations(1)",
                    "FeatureOfInterest@iot.navigationLink": "Observations(1)/FeatureOfInterest",
                    "Datastream@iot.navigationLink":"Observations(1)/Datastream",
                    "phenomenonTime": "2014-12-31T11:59:59Z",
                    "resultTime": "2014-12-31T19:59:59Z",
                    "result": 70.40
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservation)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59)))
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream, PkValue.of(1)))
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest, PkValue.of(1)))
                .setProperty(pluginCoreModel.epResultTime, TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setProperty(pluginCoreModel.epResult, new BigDecimal("70.40"));
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeObservationBasicWithNullResult() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/Observations(1)",
                    "FeatureOfInterest@iot.navigationLink": "Observations(1)/FeatureOfInterest",
                    "Datastream@iot.navigationLink":"Observations(1)/Datastream",
                    "phenomenonTime": "2014-12-31T11:59:59Z",
                    "resultTime": "2014-12-31T19:59:59Z",
                    "result": null
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservation)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream, PkValue.of(1)))
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest, PkValue.of(1)))
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59)))
                .setProperty(pluginCoreModel.epResultTime, TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
                .setProperty(pluginCoreModel.epResult, null);
        final String value = JsonWriter.writeEntity(entity);
        compareJson(expResult, value);
    }

    @Test
    void writeObservationWithEmptyResultTime() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/Observations(1)",
                    "FeatureOfInterest@iot.navigationLink": "Observations(1)/FeatureOfInterest",
                    "Datastream@iot.navigationLink":"Observations(1)/Datastream",
                    "phenomenonTime": "2014-12-31T11:59:59Z",
                    "resultTime": null,
                    "result": "70.4"
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/Observations(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etObservation)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.npDatastreamObservation, new DefaultEntity(pluginCoreModel.etDatastream, PkValue.of(1)))
                .setProperty(pluginCoreModel.npFeatureOfInterestObservation, new DefaultEntity(pluginCoreModel.etFeatureOfInterest, PkValue.of(1)))
                .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59)))
                .setProperty(pluginCoreModel.epResultTime, new TimeInstant(null))
                .setProperty(pluginCoreModel.epResult, "70.4");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeFeatureOfInterstBasic() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/FeaturesOfInterest(1)",
                    "Observations@iot.navigationLink": "FeaturesOfInterest(1)/Observations",
                    "name": "This is a weather station.",
                    "description": "This is a weather station.",
                    "encodingType": "application/geo+json"}""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/FeaturesOfInterest(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "This is a weather station.")
                .setProperty(pluginCoreModel.epDescription, "This is a weather station.")
                .setProperty(StandardProperties.EP_ENCODINGTYPE, "application/geo+json");
        compareJson(expResult, JsonWriter.writeEntity(entity));
    }

    @Test
    void writeFeatureOfInterstWithGeoJsonPointFeature() {
        String expResult = """
                {
                    "@iot.id": 1,
                    "@iot.selfLink": "http://example.org/v1.0/FeaturesOfInterest(1)",
                    "Observations@iot.navigationLink": "FeaturesOfInterest(1)/Observations",
                    "name": "This is a weather station.",
                    "description": "This is a weather station.",
                    "encodingType": "application/vnd.geo+json",
                    "feature":
                    {
                        "type": "Feature",
                        "properties": {},
                        "geometry":
                        {
                            "type": "Point",
                            "coordinates": [-114.06,51.05]
                        }
                    }
                }""";
        ResourcePath path = PathParser.parsePath(context, PluginCoreService.V_1_0, "/FeaturesOfInterest(1)");
        Query query = QueryParser.parseQuery("", context, path)
                .validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "This is a weather station.")
                .setProperty(pluginCoreModel.epDescription, "This is a weather station.")
                .setProperty(StandardProperties.EP_ENCODINGTYPE, "application/vnd.geo+json")
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
        } catch (JacksonException ex) {
            LOGGER.error("Failed", ex);
        }
        return false;
    }

}
