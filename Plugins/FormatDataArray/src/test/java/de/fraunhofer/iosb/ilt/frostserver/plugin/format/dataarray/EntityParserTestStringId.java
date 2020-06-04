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

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.DataArrayValue.LIST_OF_DATAARRAYVALUE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class EntityParserTestStringId {

    private EntityParser entityParser;

    @Before
    public void setUp() {
        entityParser = new EntityParser(IdString.class);
    }

    @Test
    public void readObservationDataArray() throws IOException {
        String json = createDataJson();
        List<DataArrayValue> expectedResult = new ArrayList<>();

        List<String> components = new ArrayList<>();
        components.add("phenomenonTime");
        components.add("result");
        components.add("FeatureOfInterest/id");

        Datastream ds1 = new Datastream().setId(new IdString("A"));

        DataArrayValue dav1 = new DataArrayValue(ds1, components);
        dav1.newItemList()
                .addItemToTail("2010-12-23T10:20:00-0700")
                .addItemToTail(20)
                .addItemToTail("A");
        dav1.newItemList()
                .addItemToTail("2010-12-23T10:21:00-0700")
                .addItemToTail(30)
                .addItemToTail("B");

        Datastream ds2 = new Datastream().setId(new IdString("B"));

        DataArrayValue dav2 = new DataArrayValue(ds2, components);
        dav2.newItemList()
                .addItemToTail("2010-12-23T10:20:00-0700")
                .addItemToTail(65)
                .addItemToTail("C");
        dav2.newItemList()
                .addItemToTail("2010-12-23T10:21:00-0700")
                .addItemToTail(60)
                .addItemToTail("D");

        MultiDatastream mds1 = new MultiDatastream().setId(new IdString("A"));

        DataArrayValue dav3 = new DataArrayValue(mds1, components);
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
        List<DataArrayValue> result = entityParser.parseObject(LIST_OF_DATAARRAYVALUE, json);
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
