/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.serialise;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class SerialisationTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SerialisationTest.class.getName());
    private static ModelRegistry modelRegistry;
    private static TestModel testModel;

    @BeforeClass
    public static void beforeClass() {
        modelRegistry = new ModelRegistry();
        testModel = new TestModel();
        testModel.initModel(modelRegistry, Constants.VALUE_ID_TYPE_LONG);
        modelRegistry.initFinalise();
    }

    @Before
    public void setUp() {
    }

    @Test
    public void serialiseDate() throws IOException {
        String expResult = "\"1987-06-05\"";
        final String result = JsonWriter.writeObject(new Date(87, 5, 5));
        Assert.assertTrue("Date not serialised correctly.", jsonEqual(expResult, result));
    }

    @Test
    public void deSerialiseDate() throws IOException {
        String input = "\"1987-06-05\"";
        Date expResult = new Date(87, 5, 5);
        expResult.setHours(0);
        expResult.setMinutes(0);
        expResult.setSeconds(0);
        Date result = new JsonReader(modelRegistry).parseObject(Date.class, input);
        result.setHours(0);
        result.setMinutes(0);
        result.setSeconds(0);
        Assert.assertEquals(expResult, result);
    }

    private boolean jsonEqual(String string1, String string2) {
        ObjectMapper mapper = SimpleJsonMapper.getSimpleObjectMapper();
        try {
            JsonNode json1 = mapper.readTree(string1);
            JsonNode json2 = mapper.readTree(string2);
            return json1.equals(json2);
        } catch (IOException ex) {
            LOGGER.error("Failed", ex);
        }
        return false;
    }

}
