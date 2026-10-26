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
package de.fraunhofer.iosb.ilt.frostserver.mcp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * Unit tests for StaJsonHelper: parse/parseOrNull behaviour and the
 * structuredContent contract (objects kept, non-objects wrapped under
 * "value", empty input to an empty object).
 */
class StaJsonHelperTest {

    @Test
    void testParseOrNull() {
        assertNull(StaJsonHelper.parseOrNull(null));
        assertNull(StaJsonHelper.parseOrNull(""));
        assertNull(StaJsonHelper.parseOrNull("   "));
        assertNull(StaJsonHelper.parseOrNull("{{{not json"));
        assertInstanceOf(JsonNode.class, StaJsonHelper.parseOrNull("{\"a\":1}"));
        assertEquals(1, StaJsonHelper.parseOrNull("{\"a\":1}").get("a").asInt());
    }

    @Test
    void testParseInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> StaJsonHelper.parse("{invalid"));
    }

    @Test
    void testToStructuredContent() {
        assertEquals(Map.of(), StaJsonHelper.toStructuredContent(null));
        assertEquals(Map.of(), StaJsonHelper.toStructuredContent(StaJsonHelper.parseOrNull("null")));
        @SuppressWarnings("unchecked")
        final Map<String, Object> object = assertInstanceOf(Map.class,
                StaJsonHelper.toStructuredContent(StaJsonHelper.parseOrNull("{\"a\":\"b\"}")));
        assertEquals("b", object.get("a"));
        @SuppressWarnings("unchecked")
        final Map<String, Object> wrapped = assertInstanceOf(Map.class,
                StaJsonHelper.toStructuredContent(StaJsonHelper.parseOrNull("[1,2]")));
        assertEquals(List.of(1, 2), wrapped.get("value"));
    }

    @Test
    void testWriteValueAsPrettyString() {
        final Map<String, Object> value = Map.of("b", 1, "a", "x");
        final String pretty = StaJsonHelper.writeValueAsPrettyString(value);
        assertTrue(pretty.contains("\n"));
        assertTrue(pretty.contains("\"a\""));
    }

}
