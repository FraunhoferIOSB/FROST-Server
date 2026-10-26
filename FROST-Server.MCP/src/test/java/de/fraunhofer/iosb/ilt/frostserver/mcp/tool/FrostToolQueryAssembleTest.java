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

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.mcp.McpSettings;
import de.fraunhofer.iosb.ilt.frostserver.mcp.TestModelMcp;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.DefaultFunctions;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for frost_query's typed-argument assembly: each option joins
 * into a single raw STA query string in a stable order, which the STA
 * QueryParser then validates.
 */
class FrostToolQueryAssembleTest {

    private static FrostToolQuery tool;

    @BeforeAll
    static void beforeAll() {
        final CoreSettings coreSettings = new CoreSettings();
        DefaultFunctions.registerDefaultFunctions(coreSettings.getFunctionRegistry());
        final TestModelMcp testModel = new TestModelMcp();
        testModel.initModel(coreSettings.getModelRegistry(), VALUE_ID_TYPE_LONG);
        testModel.init(coreSettings);
        coreSettings.getModelRegistry().initFinalise();
        final Properties properties = new Properties();
        properties.forEach((key, value) -> coreSettings.getSettings().set(String.valueOf(key), String.valueOf(value)));
        tool = new FrostToolQuery(new FrostToolHandler(
                coreSettings,
                new McpSettings(coreSettings.getSettings().getSubSettings(McpSettings.PREFIX_MCP))));
    }

    @Test
    void testAssembleQuery() {
        assertEquals(null, tool.assembleQuery(null, null, null, null, null, null, null));
        assertEquals("$filter=name eq 'x'", tool.assembleQuery("name eq 'x'", null, null, null, null, null, null));
        assertEquals("$filter=a&$select=id,name&$expand=Rooms&$orderby=name&$top=5&$skip=3&$count=true",
                tool.assembleQuery("a", List.of("id", "name"), List.of("Rooms"), "name", 5, 3, true));
        assertEquals("$select=id", tool.assembleQuery(null, List.of("id"), null, null, null, null, null));
        assertEquals("$count=false", tool.assembleQuery(null, null, null, null, null, null, false));
    }

    @Test
    void testAssembleQuerySkipsEmptyValues() {
        assertEquals(null, tool.assembleQuery("", List.of(), List.of(), "", null, null, null));
        assertTrue(tool.assembleQuery("a", null, null, null, 1, null, null).contains("$top=1"));
        assertFalse(tool.assembleQuery("a", null, null, null, 1, null, null).contains("$skip"));
    }

    @Test
    void testName() {
        assertEquals("frost_query", FrostToolQuery.NAME);
        assertEquals("frost_query", tool.toolName());
    }

}
