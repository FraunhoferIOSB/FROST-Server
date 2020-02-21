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
import de.fraunhofer.iosb.ilt.frostserver.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.MultiDatastreamBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.DataArrayValue.LIST_OF_DATAARRAYVALUE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author jab
 */
public class EntityParserTest {

    private EntityParser entityParser;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        entityParser = new EntityParser(IdLong.class);
    }

    @Test
    public void readObservationDataArray() throws IOException {
        String json = createDataJson();
        List<DataArrayValue> expectedResult = new ArrayList<>();

        List<String> components = new ArrayList<>();
        components.add("phenomenonTime");
        components.add("result");
        components.add("FeatureOfInterest/id");

        Datastream ds1 = new DatastreamBuilder().setId(new IdLong(1L)).build();

        DataArrayValue dav1 = new DataArrayValue(ds1, components);
        dav1.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 20, 1}));
        dav1.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 30, 1}));

        Datastream ds2 = new DatastreamBuilder().setId(new IdLong(2L)).build();

        DataArrayValue dav2 = new DataArrayValue(ds2, components);
        dav2.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 65, 1}));
        dav2.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 60, 1}));

        MultiDatastream mds1 = new MultiDatastreamBuilder().setId(new IdLong(2L)).build();

        DataArrayValue dav3 = new DataArrayValue(mds1, components);
        dav3.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:20:00-0700", 65, 1}));
        dav3.getDataArray().add(Arrays.asList(new Object[]{"2010-12-23T10:21:00-0700", 60, 1}));

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
