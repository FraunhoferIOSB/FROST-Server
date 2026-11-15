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
package de.fraunhofer.iosb.ilt.frostserver.mcp.resource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the frost://sta/{path}?query split logic in
 * FrostResourceHandler (the resource read itself needs persistence and is
 * covered by the Tests-module McpSmokeTest).
 */
class FrostResourceHandlerSplitTest {

    @Test
    void testSplitStaTarget() {
        assertArrayEquals(new String[]{"Things", null}, ResourceHandler.splitStaTarget("Things"));
        assertArrayEquals(new String[]{"Things(1)", null}, ResourceHandler.splitStaTarget("Things(1)"));
        assertArrayEquals(new String[]{"Things", "$top=1"}, ResourceHandler.splitStaTarget("Things?$top=1"));
        assertArrayEquals(new String[]{"Things", "$filter=name eq 'a?b'"},
                ResourceHandler.splitStaTarget("Things?$filter=name eq 'a?b'"),
                "Only the first '?' starts the query part.");
        assertArrayEquals(new String[]{"", null}, ResourceHandler.splitStaTarget(""));
        assertArrayEquals(new String[]{"Things(1)/name/$value", "$select=nothing"},
                ResourceHandler.splitStaTarget("Things(1)/name/$value?$select=nothing"));
    }

    @Test
    void testSplitStaTargetOnlyQuery() {
        final String[] parts = ResourceHandler.splitStaTarget("?$top=5");
        assertEquals("", parts[0]);
        assertEquals("$top=5", parts[1]);
        assertNull(ResourceHandler.splitStaTarget("Things")[1]);
    }

}
