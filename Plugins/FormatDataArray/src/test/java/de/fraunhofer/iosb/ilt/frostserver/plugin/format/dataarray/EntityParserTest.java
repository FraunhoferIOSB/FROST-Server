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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray;

import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.MdsModelSettings.TAG_ENABLE_MDS_MODEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.json.DataArrayDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
    private static EntityType etMultiDatastream;

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
        }
    }

    private JsonReaderDefault entityParser;

    @BeforeEach
    public void setUp() {
        entityParser = new JsonReaderDefault(modelRegistry);
    }

    @Test
    void readObservationDataArray() throws IOException {
        String json = createDataJson();
        List<DataArrayValue> expectedResult = new ArrayList<>();

        List<String> components = new ArrayList<>();
        components.add("phenomenonTime");
        components.add("result");
        components.add("FeatureOfInterest/id");

        Entity ds1 = new DefaultEntity(pluginCoreModel.etDatastream).setPrimaryKeyValues(PkValue.of(1L));

        DataArrayValue dav1 = new DataArrayValue(ds1, components, pluginCoreModel.etDatastream);
        dav1.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 20, 1}));
        dav1.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 30, 1}));

        Entity ds2 = new DefaultEntity(pluginCoreModel.etDatastream).setPrimaryKeyValues(PkValue.of(2L));

        DataArrayValue dav2 = new DataArrayValue(ds2, components, pluginCoreModel.etDatastream);
        dav2.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 65, 1}));
        dav2.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 60, 1}));

        Entity mds1 = new DefaultEntity(etMultiDatastream).setPrimaryKeyValues(PkValue.of(2L));

        DataArrayValue dav3 = new DataArrayValue(mds1, components, pluginCoreModel.etDatastream);
        dav3.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 65, 1}));
        dav3.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 60, 1}));

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
        return json;
    }

}
