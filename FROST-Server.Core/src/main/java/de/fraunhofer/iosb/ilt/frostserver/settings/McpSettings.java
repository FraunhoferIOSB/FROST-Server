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
package de.fraunhofer.iosb.ilt.frostserver.settings;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import de.fraunhofer.iosb.ilt.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.settings.Settings;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Settings for the MCP service in FROST. Read from the "mcp." sub-settings of
 * the core settings.
 */
public class McpSettings implements ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpSettings.class);

    /**
     * The prefix used for the MCP settings.
     */
    public static final String PREFIX_MCP = "mcp.";

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
    @DefaultValue("get,query,create,update,replace,delete,subscribe,unsubscribe")
    public static final String TAG_ALLOW_TOOLS = "allowTools";
    @DefaultValueInt(16)
    public static final String TAG_MAX_SUBSCRIPTIONS_PER_SESSION = "maxSubscriptionsPerSession";
    @DefaultValueBoolean(false)
    public static final String TAG_REQUIRE_AUTHENTICATED = "auth.requireAuthenticated";
    @DefaultValue("")
    public static final String TAG_EXPOSED_MCP_ENDPOINTS = "exposedEndpoints";
    @DefaultValueInt(16 * 1024 * 1024)
    public static final String TAG_MAX_REQUEST_SIZE = "maxRequestSize";
    @DefaultValueInt(30 * 60)
    public static final String TAG_KEEP_ALIVE_INTERVAL_SEC = "keepAliveIntervalSec";
    @DefaultValueInt(30 * 60)
    public static final String TAG_SESSION_SWEEP_INTERVAL_SEC = "sessionSweepIntervalSec";

    private final Settings customSettings;

    private String host;
    private int port;
    private String defaultVersionName;
    private int maxTop;
    private int maxResultSize;
    private Set<String> allowTools;
    private boolean requireAuthenticated;
    private List<String> endpoints = null;
    private int maxRequestSize;
    private int keepAliveIntervalSec;
    private int sessionSweepIntervalSec;
    private int maxSubscriptionsPerSession;

    public McpSettings(CoreSettings coreSettings, Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must be non-null");
        }
        this.customSettings = settings;
        init(coreSettings, settings);
    }

    private void init(CoreSettings coreSettings, Settings customSettings) {
        host = customSettings.get(TAG_HOST, getClass());
        port = customSettings.getInt(TAG_PORT, getClass());
        defaultVersionName = customSettings.get(TAG_DEFAULT_VERSION, getClass());
        maxTop = customSettings.getInt(TAG_MAX_TOP, getClass());
        maxResultSize = customSettings.getInt(TAG_MAX_RESULT_SIZE, getClass());
        requireAuthenticated = customSettings.getBoolean(TAG_REQUIRE_AUTHENTICATED, getClass());
        maxRequestSize = customSettings.getInt(TAG_MAX_REQUEST_SIZE, getClass());
        keepAliveIntervalSec = customSettings.getInt(TAG_KEEP_ALIVE_INTERVAL_SEC, getClass());
        sessionSweepIntervalSec = customSettings.getInt(TAG_SESSION_SWEEP_INTERVAL_SEC, getClass());
        maxSubscriptionsPerSession = customSettings.getInt(TAG_MAX_SUBSCRIPTIONS_PER_SESSION, getClass());
        allowTools = new LinkedHashSet<>();
        for (String entry : customSettings.get(TAG_ALLOW_TOOLS, getClass()).split(",")) {
            final String name = entry.trim().toLowerCase();
            if (!name.isEmpty()) {
                allowTools.add(name);
            }
        }
        if (getEndpoints().isEmpty()) {
            LOGGER.debug("No MCP endpoints exposed; the MCP server will not be advertised on the index page.");
        } else {
            coreSettings.getEnabledExtensions().add(Extension.MCP);
        }
    }

    public Settings getCustomSettings() {
        return customSettings;
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

    public Set<String> getAllowTools() {
        return allowTools;
    }

    public boolean isRequireAuthenticated() {
        return requireAuthenticated;
    }

    /**
     * The maximum size, in bytes, of a single MCP request the server accepts
     * (SDK default 16 MB). Larger requests are rejected by the transport.
     *
     * @return the maximum request size in bytes.
     */
    public int getMaxRequestSize() {
        return maxRequestSize;
    }

    /**
     * The interval, in seconds, of the transport keep-alive signal (SDK
     * default 30 minutes). Only relevant for long-lived listening streams.
     *
     * @return the keep-alive interval in seconds.
     */
    public int getKeepAliveIntervalSec() {
        return keepAliveIntervalSec;
    }

    /**
     * The interval, in seconds, at which the transport sweeps stale sessions
     * (SDK default 30 minutes).
     *
     * @return the session sweep interval in seconds.
     */
    public int getSessionSweepIntervalSec() {
        return sessionSweepIntervalSec;
    }

    /**
     * The maximum number of active subscriptions a single MCP session may
     * hold. When the limit is reached further subscribe calls fail (default
     * 16).
     *
     * @return the maximum number of subscriptions per MCP session.
     */
    public int getMaxSubscriptionsPerSession() {
        return maxSubscriptionsPerSession;
    }

    /**
     * The MCP endpoints to list on the index page, or an empty list when the
     * MCP server is not exposed.
     *
     * @return the list of MCP endpoints.
     */
    public List<String> getEndpoints() {
        if (endpoints == null) {
            searchExposedEndpoints();
        }
        return endpoints;
    }

    private void searchExposedEndpoints() {
        String endpointsString = customSettings.get(TAG_EXPOSED_MCP_ENDPOINTS, getClass());
        if (!endpointsString.isEmpty()) {
            String[] splitEndpoints = endpointsString.split(",");
            List<String> endpointList = new ArrayList<>();
            for (String endpoint : splitEndpoints) {
                final String trimmed = endpoint.trim();
                if (!trimmed.isEmpty()) {
                    endpointList.add(trimmed);
                }
            }
            endpoints = Collections.unmodifiableList(endpointList);
        } else {
            endpoints = Collections.emptyList();
        }
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
