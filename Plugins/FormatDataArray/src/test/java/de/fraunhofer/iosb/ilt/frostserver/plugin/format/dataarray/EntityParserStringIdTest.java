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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.json.DataArrayDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class EntityParserStringIdTest {

    private static CoreSettings coreSettings;
    private static QueryDefaults queryDefaults;
    private static ModelRegistry modelRegistry;
    private static PluginCoreModel pluginCoreModel;
    private static PluginMultiDatastream pluginMultiDatastream;

    @BeforeClass
    public static void initClass() {
        if (queryDefaults == null) {
            coreSettings = new CoreSettings();
            modelRegistry = coreSettings.getModelRegistry();
            modelRegistry.setIdClass(IdString.class);
            queryDefaults = coreSettings.getQueryDefaults();
            queryDefaults.setUseAbsoluteNavigationLinks(false);
            pluginCoreModel = new PluginCoreModel();
            pluginCoreModel.init(coreSettings);
            pluginMultiDatastream = new PluginMultiDatastream();
            pluginMultiDatastream.init(coreSettings);
            coreSettings.getPluginManager().registerPlugin(pluginMultiDatastream);
            coreSettings.getPluginManager().initPlugins(null);
        }
    }

    private JsonReader entityParser;

    @Before
    public void setUp() {
        entityParser = new JsonReader(modelRegistry);
    }

    @Test
    public void readObservationDataArray() throws IOException {
        String json = createDataJson();
        List<DataArrayValue> expectedResult = new ArrayList<>();

        List<String> components = new ArrayList<>();
        components.add("phenomenonTime");
        components.add("result");
        components.add("FeatureOfInterest/id");

        Entity ds1 = new DefaultEntity(pluginCoreModel.DATASTREAM).setId(new IdString("A"));

        DataArrayValue dav1 = new DataArrayValue(ds1, components, pluginCoreModel.DATASTREAM);
        dav1.newItemList()
                .addItemToTail("2010-12-23T10:20:00-0700")
                .addItemToTail(20)
                .addItemToTail("A");
        dav1.newItemList()
                .addItemToTail("2010-12-23T10:21:00-0700")
                .addItemToTail(30)
                .addItemToTail("B");

        Entity ds2 = new DefaultEntity(pluginCoreModel.DATASTREAM).setId(new IdString("B"));

        DataArrayValue dav2 = new DataArrayValue(ds2, components, pluginCoreModel.DATASTREAM);
        dav2.newItemList()
                .addItemToTail("2010-12-23T10:20:00-0700")
                .addItemToTail(65)
                .addItemToTail("C");
        dav2.newItemList()
                .addItemToTail("2010-12-23T10:21:00-0700")
                .addItemToTail(60)
                .addItemToTail("D");

        Entity mds1 = new DefaultEntity(pluginMultiDatastream.MULTI_DATASTREAM).setId(new IdString("A"));

        DataArrayValue dav3 = new DataArrayValue(mds1, components, pluginCoreModel.DATASTREAM);
        dav3.newItemList()
                .addItemToTail("2010-12-23T10:20:00-0700")
                .addItemToTail(65)
                .addItemToTail("A");
        dav3.newItemList()
                .addItemToTail("2010-12-23T10:21:00-0700")
                .addItemToTail(60)
                .addItemToTail("A");

        expectedResult.add(dav1);
        expectedResult.add(dav2);
        expectedResult.add(dav3);
        List<DataArrayValue> result = DataArrayDeserializer.deserialize(json, entityParser, coreSettings);
        assertEquals(expectedResult, result);
    }

    private String createDataJson() {
        String json = "[\n"
                + "  {\n"
                + "    \"Datastream\": {\n"
                + "      \"@iot.id\": \"A\"\n"
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
                + "        \"A\"\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        30,\n"
                + "        \"B\"\n"
                + "      ]\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"Datastream\": {\n"
                + "      \"@iot.id\": \"B\"\n"
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
                + "        \"C\"\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        60,\n"
                + "        \"D\"\n"
                + "      ]\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"MultiDatastream\": {\n"
                + "      \"@iot.id\": \"A\"\n"
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
                + "        \"A\"\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        60,\n"
                + "        \"A\"\n"
                + "      ]\n"
                + "    ]\n"
                + "  }\n"
                + "]";
        return json;
    }

}
