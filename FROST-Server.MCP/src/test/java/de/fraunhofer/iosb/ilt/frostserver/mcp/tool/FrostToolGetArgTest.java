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
package de.fraunhofer.iosb.ilt.frostserver.mcp.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the package-private argument helpers in FrostToolGet,
 * shared by all frost_* tools.
 */
class FrostToolGetArgTest {

    @Test
    void testStringArg() {
        final Map<String, Object> arguments = new HashMap<>();
        assertNull(FrostToolGet.stringArg(arguments, "missing"));
        arguments.put("asNumber", 7);
        assertEquals("7", FrostToolGet.stringArg(arguments, "asNumber"), "Non-strings are stringified.");
        arguments.put("asString", "text");
        assertEquals("text", FrostToolGet.stringArg(arguments, "asString"));
    }

    @Test
    void testRequireStringArg() {
        final Map<String, Object> arguments = new HashMap<>();
        final McpError missing = assertThrows(McpError.class, () -> FrostToolGet.requireStringArg(arguments, "path"));
        assertEquals(McpSchema.ErrorCodes.INVALID_PARAMS, missing.getJsonRpcError().code());
        arguments.put("path", "  ");
        assertThrows(McpError.class, () -> FrostToolGet.requireStringArg(arguments, "path"), "Blank values are missing.");
        arguments.put("path", "Things(1)");
        assertEquals("Things(1)", FrostToolGet.requireStringArg(arguments, "path"));
    }

    @Test
    void testStringListArg() {
        final Map<String, Object> arguments = new HashMap<>();
        assertEquals(List.of(), FrostToolGet.stringListArg(arguments, "select"));
        arguments.put("select", List.of("id", "name"));
        assertEquals(List.of("id", "name"), FrostToolGet.stringListArg(arguments, "select"));
        arguments.put("select", "single");
        assertEquals(List.of("single"), FrostToolGet.stringListArg(arguments, "select"), "A bare string becomes a one-item list.");
        arguments.put("select", 4);
        assertThrows(McpError.class, () -> FrostToolGet.stringListArg(arguments, "select"), "Other types are invalid.");
    }

    @Test
    void testIntArg() {
        final Map<String, Object> arguments = new HashMap<>();
        assertNull(FrostToolGet.intArg(arguments, "top"));
        arguments.put("top", 5);
        assertEquals(5, FrostToolGet.intArg(arguments, "top"));
        arguments.put("top", 5.7);
        assertEquals(5, FrostToolGet.intArg(arguments, "top"), "Floating point numbers are truncated.");
        arguments.put("top", "12");
        assertEquals(12, FrostToolGet.intArg(arguments, "top"), "Numeric strings are parsed.");
        arguments.put("top", "twelve");
        final McpError error = assertThrows(McpError.class, () -> FrostToolGet.intArg(arguments, "top"));
        assertEquals(McpSchema.ErrorCodes.INVALID_PARAMS, error.getJsonRpcError().code());
    }

    @Test
    void testBoolArg() {
        final Map<String, Object> arguments = new HashMap<>();
        assertNull(FrostToolGet.boolArg(arguments, "count"));
        arguments.put("count", true);
        assertEquals(Boolean.TRUE, FrostToolGet.boolArg(arguments, "count"));
        arguments.put("count", "true");
        assertEquals(Boolean.TRUE, FrostToolGet.boolArg(arguments, "count"));
        arguments.put("count", "false");
        assertEquals(Boolean.FALSE, FrostToolGet.boolArg(arguments, "count"));
        assertTrue(FrostToolGet.boolArg(new HashMap<>(Map.of("count", Boolean.FALSE)), "count").equals(Boolean.FALSE));
    }

}
