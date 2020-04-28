/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import static de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatDefault.DEFAULT_FORMAT_NAME;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class CoreSettings implements ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreSettings.class);

    /**
     * Tags
     */
    @DefaultValueBoolean(true)
    public static final String TAG_DEFAULT_COUNT = "defaultCount";
    @DefaultValueInt(100)
    public static final String TAG_DEFAULT_TOP = "defaultTop";
    @DefaultValueInt(100)
    public static final String TAG_MAX_TOP = "maxTop";
    @DefaultValueInt(25_000_000)
    public static final String TAG_MAX_DATASIZE = "maxDataSize";
    @DefaultValue("")
    public static final String TAG_SERVICE_ROOT_URL = "serviceRootUrl";
    @DefaultValueBoolean(true)
    public static final String TAG_USE_ABSOLUTE_NAVIGATION_LINKS = "useAbsoluteNavigationLinks";
    @DefaultValue("")
    public static final String TAG_TEMP_PATH = "tempPath";
    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_ACTUATION = "enableActuation";
    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_MULTIDATASTREAM = "enableMultiDatastream";

    /**
     * Used when passing CoreSettings in a map.
     */
    public static final String TAG_CORE_SETTINGS = "CoreSettings";

    // HTTP Tags
    @DefaultValueBoolean(false)
    public static final String TAG_CORS_ENABLE = "cors.enable";
    @DefaultValue("*")
    public static final String TAG_CORS_ALLOWED_ORIGINS = "cors.allowed.origins";
    @DefaultValue("GET,POST,HEAD,OPTIONS")
    public static final String TAG_CORS_ALLOWED_METHODS = "cors.allowed.methods";
    @DefaultValue("Location")
    public static final String TAG_CORS_EXPOSED_HEADERS = "cors.exposed.headers";
    @DefaultValue("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization")
    public static final String TAG_CORS_ALLOWED_HEADERS = "cors.allowed.headers";
    @DefaultValueBoolean(false)
    public static final String TAG_CORS_SUPPORT_CREDENTIALS = "cors.support.credentials";
    @DefaultValueInt(1800)
    public static final String TAG_CORS_PREFLIGHT_MAXAGE = "cors.preflight.maxage";
    @DefaultValueBoolean(true)
    public static final String TAG_CORS_REQUEST_DECORATE = "cors.request.decorate";

    // Auth Tags
    @DefaultValue("")
    public static final String TAG_AUTH_PROVIDER = "provider";
    @DefaultValueBoolean(false)
    public static final String TAG_AUTH_ALLOW_ANON_READ = "allowAnonymousRead";
    @DefaultValue("read")
    public static final String TAG_AUTH_ROLE_READ = "role.read";
    @DefaultValue("create")
    public static final String TAG_AUTH_ROLE_CREATE = "role.create";
    @DefaultValue("update")
    public static final String TAG_AUTH_ROLE_UPDATE = "role.update";
    @DefaultValue("delete")
    public static final String TAG_AUTH_ROLE_DELETE = "role.delete";
    @DefaultValue("admin")
    public static final String TAG_AUTH_ROLE_ADMIN = "role.admin";

    // Experimental settings
    @DefaultValueBoolean(false)
    public static final String TAG_EXPOSE_SERVICE_SETTINGS = "exposeServerSettings";
    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_CUSTOM_LINKS = "customLinks.enable";
    @DefaultValueInt(0)
    public static final String TAG_CUSTOM_LINKS_RECURSE_DEPTH = "customLinks.recurseDepth";

    /**
     * Prefixes
     */
    public static final String PREFIX_BUS = "bus.";
    public static final String PREFIX_MQTT = "mqtt.";
    public static final String PREFIX_HTTP = "http.";
    public static final String PREFIX_AUTH = "auth.";
    public static final String PREFIX_EXPERIMENTAL = "experimental.";
    public static final String PREFIX_PERSISTENCE = "persistence.";
    public static final String PREFIX_PLUGINS = "plugins.";

    /**
     * The core plugin manager. All plugins should register themselves here.
     */
    private final PluginManager pluginManager = new PluginManager();

    /**
     * Root URL of the service to run.
     */
    private final Map<Version, String> serviceRootUrl = new EnumMap<>(Version.class);

    /**
     * Root URL of the service to run.
     */
    private boolean useAbsoluteNavigationLinks = defaultValueBoolean(TAG_USE_ABSOLUTE_NAVIGATION_LINKS);

    /**
     * The default top to use when no specific top is set.
     */
    private int topDefault = defaultValueInt(TAG_DEFAULT_TOP);
    /**
     * The maximum allowed top.
     */
    private int topMax = defaultValueInt(TAG_MAX_TOP);
    /**
     * The maximum data size.
     */
    private long dataSizeMax = defaultValueInt(TAG_MAX_DATASIZE);
    /**
     * The default count to use when no specific count is set.
     */
    private boolean countDefault = defaultValueBoolean(TAG_DEFAULT_COUNT);
    /**
     * Path to temp folder.
     */
    private String tempPath;
    /**
     * Flag indicating actuation should be enabled (entities not hidden).
     */
    private boolean enableActuation;
    /**
     * Flag indicating MultiDatastream should be enabled (entities not hidden).
     */
    private boolean enableMultiDatastream;

    /**
     * The set of enabled extensions that are defined in the standard.
     */
    private final Set<Extension> enabledExtensions = EnumSet.noneOf(Extension.class);
    /**
     * The MQTT settings.
     */
    private MqttSettings mqttSettings;
    /**
     * The message bus settings.
     */
    private BusSettings busSettings;
    /**
     * The Persistence settings.
     */
    private PersistenceSettings persistenceSettings;
    /**
     * The HTTP settings.
     */
    private Settings httpSettings;
    /**
     * The Auth settings.
     */
    private Settings authSettings;
    /**
     * The settings for plugins.
     */
    private Settings pluginSettings;
    /**
     * The Experimental settings.
     */
    private Settings experimentalSettings;

    /**
     * The extensions, or other code parts that require Liquibase.
     */
    private final Set<Class<? extends LiquibaseUser>> liquibaseUsers = new LinkedHashSet<>();

    /**
     * Creates an empty, uninitialised CoreSettings.
     */
    public CoreSettings() {
        initChildSettings(new Settings(new Properties()));
    }

    /**
     * Creates a new CoreSettings and initialises it with the given properties,
     * and environment variables.
     *
     * @param properties The properties to use. Environment variables can
     * override these.
     */
    public CoreSettings(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must be non-null");
        }
        init(properties);
    }

    private void init(Properties properties) {
        Settings settings = new Settings(properties);
        initLocalFields(settings);
        initChildSettings(settings);
        initExtensions();
        pluginManager.init(this);
    }

    private void initLocalFields(Settings settings) {
        if (!settings.containsName(TAG_SERVICE_ROOT_URL)) {
            throw new IllegalArgumentException(getClass().getName() + " must contain property '" + TAG_SERVICE_ROOT_URL + "'");
        }
        if (!settings.containsName(TAG_TEMP_PATH)) {
            throw new IllegalArgumentException(getClass().getName() + " must contain property '" + TAG_TEMP_PATH + "'");
        }
        tempPath = settings.get(TAG_TEMP_PATH);
        if (tempPath == null || tempPath.isEmpty()) {
            throw new IllegalArgumentException("tempPath must be non-empty");
        }
        try {
            if (!Paths.get(tempPath).toRealPath(LinkOption.NOFOLLOW_LINKS).toFile().exists()) {
                throw new IllegalArgumentException("tempPath '" + tempPath + "' does not exist");
            }
        } catch (IOException exc) {
            LOGGER.error("Failed to find tempPath: {}.", tempPath);
            throw new IllegalArgumentException("tempPath '" + tempPath + "' does not exist", exc);
        }

        enableActuation = settings.getBoolean(TAG_ENABLE_ACTUATION, getClass());
        enableMultiDatastream = settings.getBoolean(TAG_ENABLE_MULTIDATASTREAM, getClass());
        generateRootUrls(settings.get(CoreSettings.TAG_SERVICE_ROOT_URL));
        useAbsoluteNavigationLinks = settings.getBoolean(TAG_USE_ABSOLUTE_NAVIGATION_LINKS, getClass());
        countDefault = settings.getBoolean(TAG_DEFAULT_COUNT, getClass());
        topDefault = settings.getInt(TAG_DEFAULT_TOP, getClass());
        topMax = settings.getInt(TAG_MAX_TOP, getClass());
        dataSizeMax = settings.getLong(TAG_MAX_DATASIZE, getClass());
    }

    private void initChildSettings(Settings settings) {
        mqttSettings = new MqttSettings(this, new Settings(settings.getProperties(), PREFIX_MQTT, false));
        persistenceSettings = new PersistenceSettings(new Settings(settings.getProperties(), PREFIX_PERSISTENCE, false));
        busSettings = new BusSettings(new Settings(settings.getProperties(), PREFIX_BUS, false));
        httpSettings = new Settings(settings.getProperties(), PREFIX_HTTP, false);
        authSettings = new Settings(settings.getProperties(), PREFIX_AUTH, false);
        pluginSettings = new CachedSettings(settings.getProperties(), PREFIX_PLUGINS, false);
        experimentalSettings = new CachedSettings(settings.getProperties(), PREFIX_EXPERIMENTAL, false);
    }

    private void initExtensions() {
        enabledExtensions.add(Extension.CORE);
        if (isEnableMultiDatastream()) {
            enabledExtensions.add(Extension.MULTI_DATASTREAM);
        }
        if (isEnableActuation()) {
            enabledExtensions.add(Extension.ACTUATION);
        }
        if (getExperimentalSettings().getBoolean(CoreSettings.TAG_ENABLE_CUSTOM_LINKS, CoreSettings.class)) {
            enabledExtensions.add(Extension.ENTITY_LINKING);
        }
    }

    /**
     * The core plugin manager. All plugins should register themselves here.
     *
     * @return The core plugin manager.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    private void generateRootUrls(String baseRootUrl) {
        for (Version version : Version.values()) {
            String url = URI.create(baseRootUrl + "/" + version.urlPart).normalize().toString();
            serviceRootUrl.put(version, url);
        }
    }

    public static CoreSettings load(String file) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Paths.get(file, (String) null))) {
            properties.load(reader);
        } catch (IOException ex) {
            LOGGER.error("error loading properties file, using defaults", ex);
        }
        return load(properties);
    }

    public static CoreSettings load(Properties properties) {
        return new CoreSettings(properties);
    }

    /**
     * The set of enabled extensions.
     *
     * @return the enabledExtensions
     */
    public Set<Extension> getEnabledExtensions() {
        return enabledExtensions;
    }

    public Settings getAuthSettings() {
        return authSettings;
    }

    public BusSettings getBusSettings() {
        return busSettings;
    }

    public Settings getExperimentalSettings() {
        return experimentalSettings;
    }

    public Settings getHttpSettings() {
        return httpSettings;
    }

    public MqttSettings getMqttSettings() {
        return mqttSettings;
    }

    public PersistenceSettings getPersistenceSettings() {
        return persistenceSettings;
    }

    public Settings getPluginSettings() {
        return pluginSettings;
    }

    public String getServiceRootUrl(Version version) {
        return serviceRootUrl.get(version);
    }

    public boolean isUseAbsoluteNavigationLinks() {
        return useAbsoluteNavigationLinks;
    }

    /**
     * @return true if actuation is enabled.
     */
    public boolean isEnableActuation() {
        return enableActuation;
    }

    /**
     * @return true if actuation is enabled.
     */
    public boolean isEnableMultiDatastream() {
        return enableMultiDatastream;
    }

    public String getTempPath() {
        return tempPath;
    }

    /**
     * The default top to use when no specific top is set.
     *
     * @return the topDefault
     */
    public int getTopDefault() {
        return topDefault;
    }

    /**
     * The default top to use when no specific top is set.
     *
     * @param topDefault the topDefault to set
     */
    public void setTopDefault(int topDefault) {
        this.topDefault = topDefault;
    }

    /**
     * The maximum allowed top.
     *
     * @return the topMax
     */
    public int getTopMax() {
        return topMax;
    }

    /**
     * The maximum allowed top.
     *
     * @param topMax the topMax to set
     */
    public void setTopMax(int topMax) {
        this.topMax = topMax;
    }

    /**
     * The maximum allowed data size to return in a single query. This uses a
     * very coarse estimation of the size, using only the fields with an
     * unbounded size, such as Observation.result and Thing.properties.
     *
     * @return The maximum result data size.
     */
    public long getDataSizeMax() {
        return dataSizeMax;
    }

    /**
     * The maximum allowed data size to return in a single query. This uses a
     * very coarse estimation of the size, using only the fields with an
     * unbounded size, such as Observation.result and Thing.properties.
     *
     * @param dataSizeMax The maximum result data size.
     */
    public void setDataSizeMax(long dataSizeMax) {
        this.dataSizeMax = dataSizeMax;
    }

    /**
     * The default count to use when no specific count is set.
     *
     * @return the countDefault
     */
    public boolean isCountDefault() {
        return countDefault;
    }

    /**
     * The default count to use when no specific count is set.
     *
     * @param countDefault the countDefault to set
     */
    public void setCountDefault(boolean countDefault) {
        this.countDefault = countDefault;
    }

    /**
     * Get the unmodifiable list of liquibase users.
     *
     * @return the unmodifiable list of liquibase users.
     */
    public Set<Class<? extends LiquibaseUser>> getLiquibaseUsers() {
        return Collections.unmodifiableSet(liquibaseUsers);
    }

    /**
     * Register a new liquibaseUser that wants to be called when it is time to
     * upgrade the database.
     *
     * @param liquibaseUser
     */
    public void addLiquibaseUser(Class<? extends LiquibaseUser> liquibaseUser) {
        liquibaseUsers.add(liquibaseUser);
    }

    /**
     * Returns the ResultFormatter with the given name.
     *
     * @param formatterName the name of the requested ResultFormatter. If null,
     * the default formatter will be returned.
     * @return the requested ResultFormatter.
     * @throws IncorrectRequestException if there is no formatter for the given
     * name.
     */
    public ResultFormatter getFormatter(String formatterName) throws IncorrectRequestException {
        if (formatterName == null) {
            return pluginManager.getFormatter(DEFAULT_FORMAT_NAME);
        }
        ResultFormatter formatter = pluginManager.getFormatter(formatterName);
        if (formatter == null) {
            throw new IncorrectRequestException("Unknown ResultFormatter: " + StringHelper.cleanForLogging(formatterName));
        }
        return formatter;
    }

}
