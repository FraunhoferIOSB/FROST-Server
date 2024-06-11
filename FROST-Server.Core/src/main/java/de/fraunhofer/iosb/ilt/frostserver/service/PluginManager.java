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
package de.fraunhofer.iosb.ilt.frostserver.service;

import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_OK;

import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.LoggerFactory;

/**
 * The manager that handles plugins.
 *
 * @author scf
 */
public class PluginManager implements ConfigDefaults {

    public static final String VALUE_PROVIDED_PLUGINS = "de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreService"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel"
            + ",de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatDefault"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.actuation.PluginActuation"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.PluginBatchProcessing"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.PluginResultFormatDataArray"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.PluginResultFormatCsv"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.PluginResultFormatGeoJson"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.PluginOpenApi"
            + ",de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader";

    /**
     * The plugins provided with FROST by default. When editing these, also
     * check the docker-compose and helm files.
     */
    @DefaultValue(VALUE_PROVIDED_PLUGINS)
    public static final String TAG_PROVIDED_PLUGINS = "providedPlugins";

    /**
     * Additional plugins not provided or enabled by default.
     */
    @DefaultValue("")
    public static final String TAG_PLUGINS = "plugins";

    public static final String PATH_WILDCARD = "*";
    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    /**
     * The plugins that supply resultFormatters.
     */
    private final List<PluginResultFormat> resultFormatPlugins = new ArrayList<>();
    private final Map<Version, Map<String, PluginResultFormat>> resultFormattersByFormat = new HashMap<>();
    private final Map<Version, List<PluginResultFormat>> resultFormatters = new HashMap<>();

    /**
     * The plugins that can handle registered paths.
     */
    private final Map<Version, Map<String, PluginService>> pathHandlers = new HashMap<>();

    /**
     * The plugins that can handle registered request types.
     */
    private final Map<Version, Map<String, PluginService>> requestTypeHandlers = new HashMap<>();

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

    /**
     * All versions defined by service plugins.
     */
    private final Map<String, Version> versions = new TreeMap<>();

    private CoreSettings settings;

    public PluginManager setCoreSettings(CoreSettings settings) {
        this.settings = settings;
        return this;
    }

    public void init() {
        Settings pluginSettings = settings.getPluginSettings();
        String provided = pluginSettings.get(TAG_PROVIDED_PLUGINS, getClass()).trim();
        String extra = pluginSettings.get(TAG_PLUGINS, getClass()).trim();
        LOGGER.info("Loading plugins...");
        loadPlugins(provided);
        loadPlugins(extra);
        initPlugins(PersistenceManagerFactory.getInstance(settings).create());
    }

    public void initPlugins(PersistenceManager pm) {
        initResultFormatters();
        ModelRegistry modelRegistry = settings.getModelRegistry();
        for (PluginModel plugin : modelModifiers) {
            plugin.registerEntityTypes();
        }
        List<PluginModel> redo = new ArrayList<>(modelModifiers);
        int pass = 0;
        while (!redo.isEmpty() && pass < 5) {
            pass++;
            LOGGER.info("Initialising data model plugins. Pass {}, {} plugins.", pass, redo.size());
            for (Iterator<PluginModel> it = redo.iterator(); it.hasNext();) {
                PluginModel plugin = it.next();
                if (plugin.linkEntityTypes(pm)) {
                    it.remove();
                }
            }
        }
        if (!redo.isEmpty()) {
            LOGGER.error("Failed to initialise {} data model plugins:", redo.size());
            for (PluginModel plugin : redo) {
                LOGGER.error("    {}", plugin.getClass().getName());
            }
        }
        for (PluginModel plugin : modelModifiers) {
            plugin.installSecurityDefinitions(pm);
        }
        modelRegistry.initFinalise();
    }

    private void loadPlugins(String classList) {
        if (classList.isEmpty()) {
            return;
        }
        final List<Plugin> pluginsToLoad = new ArrayList<>();
        final String[] split = classList.trim().split(",");
        LOGGER.info("Loading {} plugins...", split.length);
        for (String className : split) {
            try {
                LOGGER.info("Loading {}", className);
                Class<? extends Plugin> clazz = getClass().getClassLoader().loadClass(className.trim()).asSubclass(Plugin.class);
                Plugin plugin = clazz.getDeclaredConstructor().newInstance();
                pluginsToLoad.add(plugin);
            } catch (NoClassDefFoundError | ClassNotFoundException ex) {
                LOGGER.warn("Could not find given plugin class: '{}': {}", StringHelper.cleanForLogging(className), ex.getMessage());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.warn("Could not load given plugin class: '{}': {}", StringHelper.cleanForLogging(className), ex.getMessage());
                LOGGER.info("Exception:", ex);
            } catch (RuntimeException ex) {
                LOGGER.warn("Plugin caused an exception during loading.", ex);
            }
        }

        while (!pluginsToLoad.isEmpty()) {
            int count = pluginsToLoad.size();
            LOGGER.info("Initialising {} plugins...", count);
            for (Iterator<Plugin> it = pluginsToLoad.iterator(); it.hasNext();) {
                Plugin plugin = it.next();
                try {
                    LOGGER.info("Initialising {}", plugin.getClass().getName());
                    InitResult result = plugin.init(settings);
                    switch (result) {
                        case INIT_DELAY:
                            // don't remove, try again later.
                            break;

                        case INIT_FAILED:
                            // Failed, don't try again.
                            LOGGER.warn("Failed to initialise plugin: '{}'", plugin);
                            it.remove();
                            break;

                        default:
                        case INIT_OK:
                            // All is fine.
                            it.remove();
                            break;
                    }

                } catch (RuntimeException ex) {
                    LOGGER.warn("Plugin caused an exception during initialisation.", ex);
                }
            }
            if (count == pluginsToLoad.size()) {
                LOGGER.error("All {} remaining plugins can not be initialised", count);
            }
        }
    }

    public void registerPlugin(Plugin plugin) {
        if (plugin instanceof PluginService ps) {
            registerPlugin(ps);
        }
        if (plugin instanceof PluginRootDocument prd) {
            serviceDocModifiers.add(prd);
        }
        if (plugin instanceof PluginModel pm) {
            modelModifiers.add(pm);
        }
        if (plugin instanceof PluginResultFormat prf) {
            resultFormatPlugins.add(prf);
        }
        if (plugin instanceof LiquibaseUser lu) {
            settings.addLiquibaseUser(lu);
        }
        plugins.put(plugin.getClass(), plugin);
    }

    private void initResultFormatters() {
        for (PluginResultFormat plugin : resultFormatPlugins) {
            initResultFormatter(plugin);
        }
    }

    private void initResultFormatter(PluginResultFormat plugin) {
        final Collection<Version> pluginVersions = plugin.getVersions();
        final Collection<String> formatNames = plugin.getFormatNames();
        for (Version version : pluginVersions) {
            resultFormatters.computeIfAbsent(version, t -> new ArrayList<>())
                    .add(plugin);
            for (String format : formatNames) {
                if (versions.containsKey(version.urlPart)) {
                    resultFormattersByFormat.computeIfAbsent(version, v -> new TreeMap<>()).put(format.toLowerCase(), plugin);
                }
            }
        }
    }

    private void registerPlugin(PluginService plugin) {
        final Collection<Version> pluginVersions = plugin.getVersions();
        if (plugin.definesVersions()) {
            for (Version version : pluginVersions) {
                versions.put(version.urlPart, version);
            }
        }
        for (String path : plugin.getVersionedUrlPaths()) {
            for (Version version : pluginVersions) {
                if (versions.containsKey(version.urlPart)) {
                    pathHandlers.computeIfAbsent(version, v -> new TreeMap<>()).put(path, plugin);
                }
            }
            for (String type : plugin.getRequestTypes()) {
                for (Version version : pluginVersions) {
                    if (versions.containsKey(version.urlPart)) {
                        requestTypeHandlers.computeIfAbsent(version, t -> new TreeMap<>()).put(type, plugin);
                    }
                }
            }
        }
    }

    public <P extends Plugin> boolean isPluginEnabled(Class<P> pluginClass) {
        P plugin = getPlugin(pluginClass);
        if (plugin == null) {
            return false;
        }
        return plugin.isEnabled();
    }

    public <P extends Plugin> P getPlugin(Class<P> plugin) {
        return (P) plugins.get(plugin);
    }

    public List<PluginModel> getModelPlugins() {
        return modelModifiers;
    }

    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        for (PluginRootDocument plugin : serviceDocModifiers) {
            plugin.modifyServiceDocument(request, result);
        }
    }

    public PluginService getServiceForRequestType(Version version, String requestType) {
        Map<String, PluginService> types = requestTypeHandlers.get(version);
        if (types == null) {
            return null;
        }
        return types.get(requestType);
    }

    public PluginService getServiceForPath(Version version, String path) {
        Map<String, PluginService> paths = pathHandlers.get(version);
        if (paths == null) {
            return null;
        }
        final PluginService service = paths.get(path);
        if (service == null) {
            return paths.get(PATH_WILDCARD);
        }
        return service;
    }

    public ResultFormatter getFormatter(Version version, String formatName) {
        final Map<String, PluginResultFormat> formatters = resultFormattersByFormat.get(version);
        if (formatters == null) {
            return null;
        }
        final PluginResultFormat plugin = formatters.get(formatName.toLowerCase());
        if (plugin == null) {
            return null;
        }
        return plugin.getResultFormatter(formatName);
    }

    public void parsedQuery(CoreSettings settings, ServiceRequest request, Query query) {
        List<PluginResultFormat> formatters = resultFormatters.get(query.getVersion());
        for (PluginResultFormat formatter : formatters) {
            formatter.parsedPathAndQuery(settings, request, query);
        }
    }

    /**
     * Finds the Version instance that matches the given version String, or null
     * if the string does not match any version.
     *
     * @param versionString The String that appears in a url.
     * @return The Version that matches the given String.
     */
    public Version getVersion(String versionString) {
        return versions.get(versionString);
    }

    public Map<String, Version> getVersions() {
        return versions;
    }

}
