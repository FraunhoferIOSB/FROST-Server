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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray;

import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.MdsModelSettings.TAG_ENABLE_MDS_MODEL;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class EntityFormatterTest {

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

    @BeforeAll
    public static void setUp() {
        PluginResultFormatDataArray.modifyEntityFormatter();
    }

    @Test
    void writeObservationDataArray() throws IOException {
        String expResult = createDataJson();

        List<String> components = new ArrayList<>();
        components.add("id");
        components.add("phenomenonTime");
        components.add("result");

        Entity ds1 = new DefaultEntity(pluginCoreModel.etDatastream).setSelfLink("navLinkHere");

        DataArrayValue dav1 = new DataArrayValue(ds1, components, pluginCoreModel.etDatastream);
        dav1.getDataArray().add(Arrays.asList(new Object[]{446, "2010-12-23T10:20:00.000Z", 48}));
        dav1.getDataArray().add(Arrays.asList(new Object[]{447, "2010-12-23T10:21:00.000Z", 49}));

        Entity ds2 = new DefaultEntity(pluginCoreModel.etDatastream).setSelfLink("navLinkHere");

        DataArrayValue dav2 = new DataArrayValue(ds2, components, pluginCoreModel.etDatastream);
        dav2.getDataArray().add(Arrays.asList(new Object[]{448, "2010-12-23T10:20:00.000Z", 1}));
        dav2.getDataArray().add(Arrays.asList(new Object[]{449, "2010-12-23T10:21:00.000Z", 2}));

        Entity mds1 = new DefaultEntity(etMultiDatastream).setSelfLink("navLinkHere");

        DataArrayValue dav3 = new DataArrayValue(mds1, components, pluginCoreModel.etDatastream);
        dav3.getDataArray().add(Arrays.asList(new Object[]{444, "2010-12-23T10:20:00.000Z", 5}));
        dav3.getDataArray().add(Arrays.asList(new Object[]{445, "2010-12-23T10:21:00.000Z", 6}));

        DataArrayResult source = new DataArrayResult();
        source.setNextLink("nextLinkHere");
        source.setCount(108);
        source.getValue().add(dav1);
        source.getValue().add(dav2);
        source.getValue().add(dav3);

        assertTrue(jsonEqual(expResult, JsonWriter.writeObject(source)));
    }

    private String createDataJson() {
        String expResult = "{\n"
                + "    \"@iot.count\": 108,\n"
                + "    \"@iot.nextLink\": \"nextLinkHere\",\n"
                + "    \"value\": [\n"
                + "        {\n"
                + "            \"Datastream@iot.navigationLink\": \"navLinkHere\",\n"
                + "            \"components\": [\n"
                + "                \"id\",\n"
                + "                \"phenomenonTime\",\n"
                + "                \"result\"],\n"
                + "            \"dataArray@iot.count\": 2,\n"
                + "            \"dataArray\": [\n"
                + "                [\n"
                + "                    446,\n"
                + "                    \"2010-12-23T10:20:00.000Z\",\n"
                + "                    48],\n"
                + "                [\n"
                + "                    447,\n"
                + "                    \"2010-12-23T10:21:00.000Z\",\n"
                + "                    49\n"
                + "                ]\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"Datastream@iot.navigationLink\": \"navLinkHere\",\n"
                + "            \"components\": [\n"
                + "                \"id\",\n"
                + "                \"phenomenonTime\",\n"
                + "                \"result\"\n"
                + "            ],\n"
                + "            \"dataArray@iot.count\": 2,\n"
                + "            \"dataArray\": [\n"
                + "                [\n"
                + "                    448,\n"
                + "                    \"2010-12-23T10:20:00.000Z\",\n"
                + "                    1\n"
                + "                ],\n"
                + "                [\n"
                + "                    449,\n"
                + "                    \"2010-12-23T10:21:00.000Z\",\n"
                + "                    2\n"
                + "                ]\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"MultiDatastream@iot.navigationLink\": \"navLinkHere\",\n"
                + "            \"components\": [\n"
                + "                \"id\",\n"
                + "                \"phenomenonTime\",\n"
                + "                \"result\"\n"
                + "            ],\n"
                + "            \"dataArray@iot.count\": 2,\n"
                + "            \"dataArray\": [\n"
                + "                [\n"
                + "                    444,\n"
                + "                    \"2010-12-23T10:20:00.000Z\",\n"
                + "                    5\n"
                + "                ],\n"
                + "                [\n"
                + "                    445,\n"
                + "                    \"2010-12-23T10:21:00.000Z\",\n"
                + "                    6\n"
                + "                ]\n"
                + "            ]\n"
                + "        }\n"
                + "    ]\n"
                + "}\n"
                + "";
        return expResult;
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
