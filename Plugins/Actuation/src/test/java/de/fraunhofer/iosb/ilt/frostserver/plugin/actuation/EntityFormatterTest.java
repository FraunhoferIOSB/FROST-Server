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
package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import static net.time4j.tz.ZonalOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreService;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class EntityFormatterTest {

    private static CoreSettings coreSettings;
    private static ServiceContext context;
    private static PluginCoreModel pluginCoreModel;
    private static PluginActuation pluginActuation;

    @BeforeAll
    static void initClass() {
        if (context == null) {
            coreSettings = new CoreSettings();
            coreSettings.getSettings().getProperties().put("plugins." + ActuationModelSettings.TAG_ENABLE_ACTUATION, "true");
            coreSettings.getQueryDefaults()
                    .setUseAbsoluteNavigationLinks(false);
            context = new ServiceContext()
                    .setModelRegistry(coreSettings.getModelRegistry())
                    .setFunctionRegistry(coreSettings.getFunctionRegistry())
                    .setQueryDefaults(coreSettings.getQueryDefaults())
                    .setPrefixGen(() -> "http://example.org/v1.0/");
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginActuation = new PluginActuation();
            pluginActuation.init(coreSettings);
            coreSettings.getPluginManager().initPlugins(null);
        }
    }

    @Test
    void writeThingBasicAbs() throws IOException {
        String expResult = """
                {
                "@iot.id": 1,
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Locations@iot.navigationLink": "http://example.org/v1.0/Things(1)/Locations",
                "Datastreams@iot.navigationLink": "http://example.org/v1.0/Things(1)/Datastreams",
                "HistoricalLocations@iot.navigationLink": "http://example.org/v1.0/Things(1)/HistoricalLocations",
                "TaskingCapabilities@iot.navigationLink": "http://example.org/v1.0/Things(1)/TaskingCapabilities",
                "name": "This thing is an oven.",
                "description": "This thing is an oven.",
                "properties": {
                "owner": "John Doe",
                "color": "Silver"
                }
                }""";
        ServiceContext contextAbs = context.copy();
        contextAbs.setQueryDefaults(contextAbs.getQueryDefaults().copy().setUseAbsoluteNavigationLinks(true));
        ResourcePath path = PathParser.parsePath(contextAbs, PluginCoreService.V_1_0, "/Things(1)");
        Query query = new Query(contextAbs, path).validate();
        DefaultEntity entity = new DefaultEntity(pluginCoreModel.etThing)
                .setQuery(query)
                .setPrimaryKeyValues(PkValue.of(1L))
                .setProperty(pluginCoreModel.epName, "This thing is an oven.")
                .setProperty(pluginCoreModel.epDescription, "This thing is an oven.")
                .setProperty(StandardProperties.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .buildTreeNode());
        assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    void writeThingBasicRel() throws IOException {
        String expResult = """
                {
                "@iot.id": 1,
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Locations@iot.navigationLink": "Things(1)/Locations",
                "Datastreams@iot.navigationLink": "Things(1)/Datastreams",
                "HistoricalLocations@iot.navigationLink": "Things(1)/HistoricalLocations",
                "TaskingCapabilities@iot.navigationLink": "Things(1)/TaskingCapabilities",
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
        assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    void writeThingSelect() throws IOException {
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
        assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    void writeThingsBasic() throws IOException {
        String thing = """
                {
                "@iot.id": 1,
                "@iot.selfLink": "http://example.org/v1.0/Things(1)",
                "Locations@iot.navigationLink": "Things(1)/Locations",
                "Datastreams@iot.navigationLink": "Things(1)/Datastreams",
                "HistoricalLocations@iot.navigationLink": "Things(1)/HistoricalLocations",
                "TaskingCapabilities@iot.navigationLink": "Things(1)/TaskingCapabilities",
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
    void writeThingOnlyId() throws IOException {
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
        assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    void writeThingsWithExpandedDatastream() throws IOException {
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
                "TaskingCapabilities@iot.navigationLink": "Things(1)/TaskingCapabilities",
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
        assertTrue(jsonEqual(expResult, JsonWriter.writeEntityCollection(things, query)));
    }

    private boolean jsonEqual(String string1, String string2) {
        ObjectMapper mapper = SimpleJsonMapper.getSimpleObjectMapper();
        try {
            JsonNode json1 = mapper.readTree(string1);
            JsonNode json2 = mapper.readTree(string2);
            return json1.equals(json2);
        } catch (JacksonException ex) {
            Logger.getLogger(EntityFormatterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
