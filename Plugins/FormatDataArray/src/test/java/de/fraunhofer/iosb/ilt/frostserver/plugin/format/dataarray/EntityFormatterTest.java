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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author jab
 */
public class EntityFormatterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() {
        PluginResultFormatDataArray.modifyEntityFormatter();
    }

    @Test
    public void writeObservationDataArray() throws IOException {
        String expResult = createDataJson();

        List<String> components = new ArrayList<>();
        components.add("id");
        components.add("phenomenonTime");
        components.add("result");

        Datastream ds1 = new Datastream().setNavigationLink("navLinkHere");

        DataArrayValue dav1 = new DataArrayValue(ds1, components);
        dav1.getDataArray().add(Arrays.asList(new Object[]{446, "2010-12-23T10:20:00.000Z", 48}));
        dav1.getDataArray().add(Arrays.asList(new Object[]{447, "2010-12-23T10:21:00.000Z", 49}));

        Datastream ds2 = new Datastream().setNavigationLink("navLinkHere");

        DataArrayValue dav2 = new DataArrayValue(ds2, components);
        dav2.getDataArray().add(Arrays.asList(new Object[]{448, "2010-12-23T10:20:00.000Z", 1}));
        dav2.getDataArray().add(Arrays.asList(new Object[]{449, "2010-12-23T10:21:00.000Z", 2}));

        MultiDatastream mds1 = new MultiDatastream().setNavigationLink("navLinkHere");

        DataArrayValue dav3 = new DataArrayValue(mds1, components);
        dav3.getDataArray().add(Arrays.asList(new Object[]{444, "2010-12-23T10:20:00.000Z", 5}));
        dav3.getDataArray().add(Arrays.asList(new Object[]{445, "2010-12-23T10:21:00.000Z", 6}));

        DataArrayResult source = new DataArrayResult();
        source.setNextLink("nextLinkHere");
        source.setCount(108);
        source.getValue().add(dav1);
        source.getValue().add(dav2);
        source.getValue().add(dav3);

        Assert.assertTrue(jsonEqual(expResult, EntityFormatter.writeObject(source)));
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
