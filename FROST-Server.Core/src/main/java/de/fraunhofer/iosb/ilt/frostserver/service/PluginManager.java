/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.service;

import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PluginManager implements ConfigDefaults {

    /**
     * The plugins provided with FROST by default.
     */
    @DefaultValue(
            "de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatDefault"
            + ",de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatDataArray"
            + ",de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatCsv"
    )
    public static final String TAG_PROVIDED_PLUGINS = "providedPlugins";

    /**
     * Additional plugins not provided or enabled by default.
     */
    @DefaultValue("")
    public static final String TAG_PLUGINS = "plugins";

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    /**
     * The plugins that supply resultFormatters.
     */
    private final Map<String, PluginResultFormat> resultFormatters = new HashMap<>();
    /**
     * The plugins that want to modify the service document.
     */
    private final List<PluginServiceDocument> serviceDocModifiers = new ArrayList<>();

    public void init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        String provided = pluginSettings.get(TAG_PROVIDED_PLUGINS, getClass()).trim();
        loadPlugins(settings, provided);
        String extra = pluginSettings.get(TAG_PLUGINS, getClass()).trim();
        loadPlugins(settings, extra);
    }

    private void loadPlugins(CoreSettings settings, String classList) {
        if (classList.isEmpty()) {
            return;
        }
        String[] split = classList.trim().split(",");
        for (String className : split) {
            try {
                Class<?> clazz = Class.forName(className.trim());
                Object newInstance = clazz.getDeclaredConstructor().newInstance();
                if (newInstance instanceof Plugin) {
                    Plugin plugin = (Plugin) newInstance;
                    plugin.init(settings);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.warn("Could not load given plugin class: '" + StringHelper.cleanForLogging(className) + "'", ex);
            } catch (RuntimeException ex) {
                LOGGER.warn("Plugin caused an exception during initialisation.", ex);
            }
        }
    }

    public void registerPlugin(Plugin plugin) {
        if (plugin instanceof PluginServiceDocument) {
            serviceDocModifiers.add((PluginServiceDocument) plugin);
        }
        if (plugin instanceof PluginResultFormat) {
            registerPlugin((PluginResultFormat) plugin);
        }
    }

    private void registerPlugin(PluginResultFormat formatterPlugin) {
        for (String format : formatterPlugin.getFormatNames()) {
            resultFormatters.put(format.toLowerCase(), formatterPlugin);
        }
    }

    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        for (PluginServiceDocument plugin : serviceDocModifiers) {
            plugin.modifyServiceDocument(request, result);
        }
    }

    public ResultFormatter getFormatter(String formatName) {
        PluginResultFormat plugin = resultFormatters.get(formatName.toLowerCase());
        if (plugin == null) {
            return null;
        }
        return plugin.getResultFormatter();
    }
}
