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
package de.fraunhofer.iosb.ilt.frostserver.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.actuation.PluginActuation;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 * @author scf
 */
public class EntityFormatterTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;
    private static PluginActuation pluginActuation;

    @BeforeClass
    public static void initClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            modelRegistry = coreSettings.getModelRegistry();
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginActuation = new PluginActuation();
            pluginActuation.init(coreSettings);
            coreSettings.getPluginManager().registerPlugin(pluginActuation);
            coreSettings.getPluginManager().initPlugins(null);
        }
    }

    @Test
    public void writeThingBasicAbs() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/Datastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/HistoricalLocations\",\n"
                + "\"TaskingCapabilities@iot.navigationLink\": \"http://example.org/v1.0/Things(1)/TaskingCapabilities\",\n"
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
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingBasicRel() throws IOException {
        String expResult
                = "{\n"
                + "\"@iot.id\": 1,\n"
                + "\"@iot.selfLink\": \"http://example.org/v1.0/Things(1)\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"TaskingCapabilities@iot.navigationLink\": \"Things(1)/TaskingCapabilities\",\n"
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
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingSelect() throws IOException {
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
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
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
                + "\"TaskingCapabilities@iot.navigationLink\": \"Things(1)/TaskingCapabilities\",\n"
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
                .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
                        .addProperty("owner", "John Doe")
                        .addProperty("color", "Silver")
                        .build());
        EntitySet things = new EntitySetImpl(pluginCoreModel.etThing);
        things.add(entity);
        things.add(entity);
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntityCollection(things)));
    }

    @Test
    public void writeThingOnlyId() throws IOException {
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
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntity(entity)));
    }

    @Test
    public void writeThingsWithExpandedDatastream() throws IOException {
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
                + "	\"phenomenonTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\",\n"
                + "	\"resultTime\": \"2014-03-01T13:00:00.000Z/2015-05-11T15:30:00.000Z\""
                + "}\n"
                + "],\n"
                + "\"Datastreams@iot.navigationLink\": \"Things(1)/Datastreams\",\n"
                + "\"Locations@iot.navigationLink\": \"Things(1)/Locations\",\n"
                + "\"HistoricalLocations@iot.navigationLink\": \"Things(1)/HistoricalLocations\",\n"
                + "\"TaskingCapabilities@iot.navigationLink\": \"Things(1)/TaskingCapabilities\",\n"
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
                        .setProperty(pluginCoreModel.epUnitOfMeasurement, new UnitOfMeasurement()
                                .setName("degree Celsius")
                                .setSymbol("°C")
                                .setDefinition("http://unitsofmeasure.org/ucum.html#para-30")
                        )
                        .setProperty(pluginCoreModel.epObservationType, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                        .setProperty(pluginCoreModel.epObservedArea, TestHelper.getPolygon(2, 100, 0, 101, 0, 101, 1, 100, 1, 100, 0))
                        .setProperty(pluginCoreModel.epPhenomenonTime, TestHelper.createTimeInterval(2014, 03, 1, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
                        .setProperty(pluginCoreModel.epResultTime, TestHelper.createTimeInterval(2014, 03, 01, 13, 0, 0, 2015, 05, 11, 15, 30, 0, DateTimeZone.UTC))
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
        Assert.assertTrue(jsonEqual(expResult, JsonWriter.writeEntityCollection(things)));
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
