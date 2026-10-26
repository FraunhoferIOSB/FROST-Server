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
package de.fraunhofer.iosb.ilt.frostserver.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.settings.Settings;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for McpSettings: defaults, configured overrides and the tool
 * allowlist contract (short and full tool names, case-insensitivity).
 */
class McpSettingsTest {

    private static McpSettings from(Properties properties) {
        final CoreSettings coreSettings = new CoreSettings();
        final Settings settings = coreSettings.getSettings();
        properties.forEach((key, value) -> settings.set(String.valueOf(key), String.valueOf(value)));
        return new McpSettings(settings.getSubSettings(McpSettings.PREFIX_MCP));
    }

    @Test
    void testDefaults() {
        final McpSettings settings = from(new Properties());
        assertTrue(settings.isEnable());
        assertEquals("0.0.0.0", settings.getHost());
        assertEquals(8080, settings.getPort());
        assertEquals("", settings.getDefaultVersionName());
        assertEquals(100, settings.getMaxTop());
        assertEquals(1024 * 1024, settings.getMaxResultSize());
        assertTrue(settings.isCount());
        assertFalse(settings.isRequireAuthenticated());
        assertFalse(settings.isStartBus());
        assertTrue(settings.isToolAllowed("frost_get"));
        assertTrue(settings.isToolAllowed("frost_query"));
        assertTrue(settings.isToolAllowed("frost_create"));
        assertTrue(settings.isToolAllowed("frost_update"));
        assertTrue(settings.isToolAllowed("frost_replace"));
        assertTrue(settings.isToolAllowed("frost_delete"));
        assertFalse(settings.isToolAllowed("frost_other"));
    }

    @Test
    void testToolAllowlistShortAndFullNames() {
        final Properties properties = new Properties();
        properties.put(McpSettings.PREFIX_MCP + McpSettings.TAG_ALLOW_TOOLS, "get, frost_create ,Delete");
        final McpSettings settings = from(properties);
        assertTrue(settings.isToolAllowed("frost_get"), "Short form must map to the full tool name.");
        assertTrue(settings.isToolAllowed("get"), "The bare short form used in the config also matches.");
        assertTrue(settings.isToolAllowed("frost_create"), "Full form allowed.");
        assertTrue(settings.isToolAllowed("frost_delete"), "Config entries are lowercased.");
        assertFalse(settings.isToolAllowed("frost_query"));
        assertFalse(settings.isToolAllowed("frost_delete_all"));
        assertFalse(settings.isToolAllowed(null));
    }

}
