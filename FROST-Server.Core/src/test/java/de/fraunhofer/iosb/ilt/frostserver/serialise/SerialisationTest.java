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
package de.fraunhofer.iosb.ilt.frostserver.serialise;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.time4j.Moment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
class SerialisationTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SerialisationTest.class.getName());
    private static ModelRegistry modelRegistry;
    private static TestModel testModel;

    @BeforeAll
    public static void beforeClass() {
        modelRegistry = new ModelRegistry();
        testModel = new TestModel();
        testModel.initModel(modelRegistry, Constants.VALUE_ID_TYPE_LONG);
        modelRegistry.initFinalise();
    }

    @BeforeEach
    public void setUp() {
    }

    @Test
    void serialiseDate() throws IOException {
        String expResult = "\"1987-06-05\"";
        final String result = JsonWriter.writeObject(new Date(87, 5, 5));
        assertTrue(jsonEqual(expResult, result), "Date not serialised correctly.");
    }

    @Test
    void serialiseEmptyProperties() throws IOException {
        String expResult = "{\"foo\":\"bar\"}";
        Map<String, String> props = new HashMap<>();
        props.put("foo", "bar");
        props.put("nullable", null);
        props.put("empty", "");
        final String result = JsonWriter.writeObject(props);
        assertTrue(jsonEqual(expResult, result), "Empty properties not serialised correctly: " + result);
    }

    @Test
    void parseDateTimeOffset() {
        TimeInstant expected = TimeInstant.create(Moment.from(Instant.ofEpochSecond(1_700_000_000)));
        Assertions.assertEquals(expected, TimeInstant.parse("2023-11-14T22:13:20Z"));
        Assertions.assertEquals(expected, TimeInstant.parse("2023-11-14T22:13:20+00:00"));
        Assertions.assertEquals(expected, TimeInstant.parse("2023-11-14T23:13:20+01:00"));
        Assertions.assertEquals(expected, TimeInstant.parse("2023-11-15T00:13:20+02:00"));
        Assertions.assertEquals(expected, TimeInstant.parse("2023-11-14T21:13:20-01"));
        // Not supported at the moment: Assertions.assertEquals(expected, TimeInstant.parse("20231114T221320+00:00"));
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
