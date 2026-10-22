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

import de.fraunhofer.iosb.ilt.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.settings.Settings;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueInt;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Settings for the MCP service in FROST. Read from the "mcp." sub-settings of
 * the core settings; the Core module stays untouched.
 */
public class McpSettings implements ConfigDefaults {

    public static final String PREFIX_MCP = "mcp.";

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE = "enable";
    @DefaultValue("0.0.0.0")
    public static final String TAG_HOST = "host";
    @DefaultValueInt(8080)
    public static final String TAG_PORT = "port";
    @DefaultValue("")
    public static final String TAG_DEFAULT_VERSION = "defaultVersion";
    @DefaultValueInt(100)
    public static final String TAG_MAX_TOP = "maxTop";
    @DefaultValueInt(1024 * 1024)
    public static final String TAG_MAX_RESULT_SIZE = "maxResultSize";
    @DefaultValueBoolean(true)
    public static final String TAG_COUNT = "count";
    @DefaultValue("get,query,create,update,replace,delete")
    public static final String TAG_ALLOW_TOOLS = "allowTools";
    @DefaultValueBoolean(false)
    public static final String TAG_REQUIRE_AUTHENTICATED = "auth.requireAuthenticated";
    @DefaultValueBoolean(false)
    public static final String TAG_START_BUS = "startBus";

    private final Settings customSettings;

    private boolean enable;
    private String host;
    private int port;
    private String defaultVersionName;
    private int maxTop;
    private int maxResultSize;
    private boolean count;
    private Set<String> allowTools;
    private boolean requireAuthenticated;
    private boolean startBus;

    public McpSettings(Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must be non-null");
        }
        this.customSettings = settings;
        init(settings);
    }

    private void init(Settings settings) {
        enable = settings.getBoolean(TAG_ENABLE, getClass());
        host = settings.get(TAG_HOST, getClass());
        port = settings.getInt(TAG_PORT, getClass());
        defaultVersionName = settings.get(TAG_DEFAULT_VERSION, getClass());
        maxTop = settings.getInt(TAG_MAX_TOP, getClass());
        maxResultSize = settings.getInt(TAG_MAX_RESULT_SIZE, getClass());
        count = settings.getBoolean(TAG_COUNT, getClass());
        requireAuthenticated = settings.getBoolean(TAG_REQUIRE_AUTHENTICATED, getClass());
        startBus = settings.getBoolean(TAG_START_BUS, getClass());
        allowTools = new LinkedHashSet<>();
        for (String entry : settings.get(TAG_ALLOW_TOOLS, getClass()).split(",")) {
            final String name = entry.trim().toLowerCase();
            if (!name.isEmpty()) {
                allowTools.add(name);
            }
        }
    }

    public Settings getCustomSettings() {
        return customSettings;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDefaultVersionName() {
        return defaultVersionName;
    }

    public int getMaxTop() {
        return maxTop;
    }

    public int getMaxResultSize() {
        return maxResultSize;
    }

    public boolean isCount() {
        return count;
    }

    public Set<String> getAllowTools() {
        return allowTools;
    }

    public boolean isRequireAuthenticated() {
        return requireAuthenticated;
    }

    public boolean isStartBus() {
        return startBus;
    }

    /**
     * Checks whether a tool is enabled. Tool names are accepted both in
     * their short form ("get") and full form ("frost_get").
     *
     * @param toolName the tool name, e.g. "frost_get".
     * @return true when the tool is enabled.
     */
    public boolean isToolAllowed(String toolName) {
        return allowTools.contains(toolName)
                || (toolName != null && toolName.startsWith("frost_")
                        && allowTools.contains(toolName.substring("frost_".length())));
    }
}
