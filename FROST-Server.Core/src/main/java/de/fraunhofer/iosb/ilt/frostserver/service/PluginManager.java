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
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * The manager that handles plugins.
 *
 * @author scf
 */
public class PluginManager implements ConfigDefaults {

    /**
     * The plugins provided with FROST by default. When editing these, also
     * check the docker-compose and helm files.
     */
    @DefaultValue(
            "de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatDefault"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.actuation.PluginActuation"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.PluginBatchProcessing"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.PluginResultFormatDataArray"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.PluginResultFormatCsv"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.PluginResultFormatGeoJson"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.PluginOpenApi"
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
     * The plugins that can handle registered paths.
     */
    private final Map<String, PluginService> pathHandlers = new HashMap<>();

    /**
     * The plugins that can handle registered request types.
     */
    private final Map<String, PluginService> requestTypeHandlers = new HashMap<>();

    /**
     * The plugins that want to modify the service document.
     */
    private final List<PluginRootDocument> serviceDocModifiers = new ArrayList<>();

    /**
     * The plugins that change the data model.
     */
    private final List<PluginModel> modelModifiers = new ArrayList<>();

    /**
     * All plugins, by their class.
     */
    private final Map<Class<? extends Plugin>, Object> plugins = new HashMap<>();

    public void init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        String provided = pluginSettings.get(TAG_PROVIDED_PLUGINS, getClass()).trim();
        loadPlugins(settings, provided);
        String extra = pluginSettings.get(TAG_PLUGINS, getClass()).trim();
        loadPlugins(settings, extra);
        initPlugins(settings, PersistenceManagerFactory.getInstance(settings).create());
    }

    public void initPlugins(CoreSettings settings, PersistenceManager pm) {
        ModelRegistry modelRegistry = settings.getModelRegistry();
        modelRegistry.initDefaultTypes();
        for (PluginModel plugin : modelModifiers) {
            plugin.registerProperties();
        }
        List<PluginModel> redo = new ArrayList<>(modelModifiers);
        while (!redo.isEmpty()) {
            for (Iterator<PluginModel> it = redo.iterator(); it.hasNext();) {
                PluginModel plugin = it.next();
                if (plugin.registerEntityTypes(pm)) {
                    it.remove();
                }
            }
        }
        modelRegistry.initFinalise();
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
                LOGGER.warn("Could not load given plugin class: '{}'", StringHelper.cleanForLogging(className));
                LOGGER.debug("", ex);
            } catch (RuntimeException ex) {
                LOGGER.warn("Plugin caused an exception during initialisation.", ex);
            }
        }
    }

    public void registerPlugin(Plugin plugin) {
        if (plugin instanceof PluginService) {
            registerPlugin((PluginService) plugin);
        }
        if (plugin instanceof PluginRootDocument) {
            serviceDocModifiers.add((PluginRootDocument) plugin);
        }
        if (plugin instanceof PluginModel) {
            modelModifiers.add((PluginModel) plugin);
        }
        if (plugin instanceof PluginResultFormat) {
            registerPlugin((PluginResultFormat) plugin);
        }
        plugins.put(plugin.getClass(), plugin);
    }

    private void registerPlugin(PluginResultFormat plugin) {
        for (String format : plugin.getFormatNames()) {
            resultFormatters.put(format.toLowerCase(), plugin);
        }
    }

    private void registerPlugin(PluginService plugin) {
        for (String path : plugin.getUrlPaths()) {
            pathHandlers.put(path, plugin);
        }
        for (String path : plugin.getRequestTypes()) {
            requestTypeHandlers.put(path, plugin);
        }
    }

    public <P extends Plugin> P getPlugin(Class<P> plugin) {
        return (P) plugins.get(plugin);
    }

    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        for (PluginRootDocument plugin : serviceDocModifiers) {
            plugin.modifyServiceDocument(request, result);
        }
    }

    public PluginService getServiceForRequestType(String requestType) {
        return requestTypeHandlers.get(requestType);
    }

    public PluginService getServiceForPath(String path) {
        return pathHandlers.get(path);
    }

    public ResultFormatter getFormatter(String formatName) {
        PluginResultFormat plugin = resultFormatters.get(formatName.toLowerCase());
        if (plugin == null) {
            return null;
        }
        return plugin.getResultFormatter();
    }
}
