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

import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_DEFAULT;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.CustomLinksHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The core settings of FROST-Server.
 */
public class CoreSettings implements ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreSettings.class);

    /**
     * Prefixes
     */
    public static final String PREFIX_BUS = "bus.";
    public static final String PREFIX_MQTT = "mqtt.";
    public static final String PREFIX_HTTP = "http.";
    public static final String PREFIX_AUTH = "auth.";
    public static final String PREFIX_EXTENSION = "extension.";
    public static final String PREFIX_PERSISTENCE = "persistence.";
    public static final String PREFIX_PLUGINS = "plugins.";

    /**
     * Tags
     */
    @DefaultValueBoolean(false)
    public static final String TAG_LOG_SENSITIVE_DATA = "logSensitiveData";
    @DefaultValueBoolean(false)
    public static final String TAG_DEFAULT_COUNT = "defaultCount";
    @DefaultValueInt(100)
    public static final String TAG_DEFAULT_TOP = "defaultTop";
    @DefaultValueInt(10000)
    public static final String TAG_MAX_TOP = "maxTop";
    @DefaultValueInt(25_000_000)
    public static final String TAG_MAX_DATASIZE = "maxDataSize";

    @DefaultValueBoolean(true)
    public static final String TAG_ALWAYS_ORDERBY_ID = "alwaysOrderbyId";

    @DefaultValue("")
    public static final String TAG_SERVICE_ROOT_URL = "serviceRootUrl";
    @DefaultValueBoolean(true)
    public static final String TAG_USE_ABSOLUTE_NAVIGATION_LINKS = "useAbsoluteNavigationLinks";
    @DefaultValue("")
    public static final String TAG_TEMP_PATH = "tempPath";
    @DefaultValueInt(0)
    public static final String TAG_QUEUE_LOGGING_INTERVAL = "queueLoggingInterval";

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
    @DefaultValue("")
    public static final String TAG_SESSION_COOKIE_PATH = "sessionCookiePath";

    // Auth Tags
    @DefaultValue("")
    public static final String TAG_AUTH_PROVIDER = "provider";
    @DefaultValueBoolean(false)
    public static final String TAG_AUTH_ALLOW_ANON_READ = "allowAnonymousRead";
    @DefaultValueBoolean(false)
    public static final String TAG_AUTHENTICATE_ONLY = "authenticateOnly";
    @DefaultValue(PrincipalExtended.ROLE_READ)
    public static final String TAG_AUTH_ROLE_READ = "role.read";
    @DefaultValue(PrincipalExtended.ROLE_CREATE)
    public static final String TAG_AUTH_ROLE_CREATE = "role.create";
    @DefaultValue(PrincipalExtended.ROLE_UPDATE)
    public static final String TAG_AUTH_ROLE_UPDATE = "role.update";
    @DefaultValue(PrincipalExtended.ROLE_DELETE)
    public static final String TAG_AUTH_ROLE_DELETE = "role.delete";
    @DefaultValue(PrincipalExtended.ROLE_ADMIN)
    public static final String TAG_AUTH_ROLE_ADMIN = "role.admin";

    // Experimental settings
    @DefaultValueBoolean(false)
    public static final String TAG_FILTER_DELETE_ENABLE = PREFIX_EXTENSION + "filterDelete.enable";
    @DefaultValueBoolean(false)
    public static final String TAG_CUSTOM_LINKS_ENABLE = "customLinks.enable";
    @DefaultValueInt(0)
    public static final String TAG_CUSTOM_LINKS_RECURSE_DEPTH = "customLinks.recurseDepth";

    /**
     * The core plugin manager. All plugins should register themselves here.
     */
    private final PluginManager pluginManager = new PluginManager();

    private final QueryDefaults queryDefaults = new QueryDefaults(
            defaultValueBoolean(TAG_USE_ABSOLUTE_NAVIGATION_LINKS),
            defaultValueBoolean(TAG_DEFAULT_COUNT),
            defaultValueInt(TAG_DEFAULT_TOP),
            defaultValueInt(TAG_MAX_TOP),
            defaultValueBoolean(TAG_ALWAYS_ORDERBY_ID));
    /**
     * The maximum data size.
     */
    private long dataSizeMax = defaultValueInt(TAG_MAX_DATASIZE);
    /**
     * Path to temp folder.
     */
    private String tempPath;
    /**
     * Flag indicating if delete on entity sets is allowed.
     */
    private boolean filterDeleteEnabled;
    /**
     * The set of enabled extensions that are defined in the standard.
     */
    private final Set<Extension> enabledExtensions = EnumSet.noneOf(Extension.class);
    /**
     * The Settings object holding the configuration values.
     */
    private Settings settings;
    /**
     * Flag indicating things like passwords should be logged completely, not
     * hidden.
     */
    private boolean logSensitiveData;
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
     * The settings of various non-standard extensions that are not plugins.
     */
    private Settings extensionSettings;

    /**
     * The extensions, or other code parts that require Liquibase.
     */
    private final Set<LiquibaseUser> liquibaseUsers = new LinkedHashSet<>();

    private final ModelRegistry modelRegistry = new ModelRegistry();

    private MessageBus messageBus;

    private CustomLinksHelper customLinksHelper;

    /**
     * Creates an empty, uninitialised CoreSettings.
     */
    public CoreSettings() {
        settings = new Settings(new Properties());
        initChildSettings();
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
        settings = new Settings(properties);
        SettingsMigrator.migrate(settings.getProperties());
        initLocalFields();
        initChildSettings();
        initExtensions();
        pluginManager.init();
    }

    private void initLocalFields() {
        logSensitiveData = settings.getBoolean(TAG_LOG_SENSITIVE_DATA, getClass());
        settings.setLogSensitiveData(logSensitiveData);

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

        queryDefaults.setServiceRootUrl(settings.get(CoreSettings.TAG_SERVICE_ROOT_URL));
        queryDefaults.setUseAbsoluteNavigationLinks(settings.getBoolean(TAG_USE_ABSOLUTE_NAVIGATION_LINKS, getClass()));
        queryDefaults.setCountDefault(settings.getBoolean(TAG_DEFAULT_COUNT, getClass()));
        queryDefaults.setTopDefault(settings.getInt(TAG_DEFAULT_TOP, getClass()));
        queryDefaults.setTopMax(settings.getInt(TAG_MAX_TOP, getClass()));
        queryDefaults.setAlwaysOrder(settings.getBoolean(TAG_ALWAYS_ORDERBY_ID, getClass()));
        dataSizeMax = settings.getLong(TAG_MAX_DATASIZE, getClass());
        filterDeleteEnabled = settings.getBoolean(TAG_FILTER_DELETE_ENABLE, getClass());
    }

    private void initChildSettings() {
        pluginManager.setCoreSettings(this);
        mqttSettings = new MqttSettings(this, new CachedSettings(settings.getProperties(), PREFIX_MQTT, false, logSensitiveData));
        persistenceSettings = new PersistenceSettings(new CachedSettings(settings.getProperties(), PREFIX_PERSISTENCE, false, logSensitiveData));
        busSettings = new BusSettings(new CachedSettings(settings.getProperties(), PREFIX_BUS, false, logSensitiveData));
        httpSettings = new CachedSettings(settings.getProperties(), PREFIX_HTTP, false, logSensitiveData);
        authSettings = new CachedSettings(settings.getProperties(), PREFIX_AUTH, false, logSensitiveData);
        pluginSettings = new CachedSettings(settings.getProperties(), PREFIX_PLUGINS, false, logSensitiveData);
        extensionSettings = new CachedSettings(settings.getProperties(), PREFIX_EXTENSION, false, logSensitiveData);
    }

    private void initExtensions() {
        enabledExtensions.add(Extension.CORE);
        if (getExtensionSettings().getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class)) {
            enabledExtensions.add(Extension.ENTITY_LINKING);
        }
        if (isFilterDeleteEnabled()) {
            enabledExtensions.add(Extension.FILTERED_DELETES);
        }
        if (mqttSettings.isAllowMqttExpand()) {
            enabledExtensions.add(Extension.MQTT_EXPAND);
        }
        if (mqttSettings.isAllowMqttFilter()) {
            enabledExtensions.add(Extension.MQTT_FILTER);
        }
    }

    /**
     * Get the raw Settings object.
     *
     * @return The raw Settings object.
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * The core plugin manager. All plugins should register themselves here.
     *
     * @return The core plugin manager.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Get the registry of entity types currently enabled.
     *
     * @return the registry of entity types currently enabled.
     */
    public ModelRegistry getModelRegistry() {
        return modelRegistry;
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

    public Settings getExtensionSettings() {
        return extensionSettings;
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

    public String getTempPath() {
        return tempPath;
    }

    public QueryDefaults getQueryDefaults() {
        return queryDefaults;
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

    public boolean isFilterDeleteEnabled() {
        return filterDeleteEnabled;
    }

    /**
     * Get the unmodifiable list of liquibase users.
     *
     * @return the unmodifiable list of liquibase users.
     */
    public Set<LiquibaseUser> getLiquibaseUsers() {
        return Collections.unmodifiableSet(liquibaseUsers);
    }

    /**
     * Register a new liquibaseUser that wants to be called when it is time to
     * upgrade the database.
     *
     * @param liquibaseUser the class instance that has Liquibase jobs.
     */
    public void addLiquibaseUser(LiquibaseUser liquibaseUser) {
        liquibaseUsers.add(liquibaseUser);
    }

    /**
     * Returns the ResultFormatter with the given name.
     *
     * @param version The version to get the formatter for.
     * @param formatterName the name of the requested ResultFormatter. If null,
     * the default formatter will be returned.
     * @return the requested ResultFormatter.
     * @throws IncorrectRequestException if there is no formatter for the given
     * name.
     */
    public ResultFormatter getFormatter(Version version, String formatterName) throws IncorrectRequestException {
        if (formatterName == null) {
            return pluginManager.getFormatter(version, FORMAT_NAME_DEFAULT);
        }
        ResultFormatter formatter = pluginManager.getFormatter(version, formatterName);
        if (formatter == null) {
            throw new IncorrectRequestException("Unknown ResultFormatter: " + StringHelper.cleanForLogging(formatterName));
        }
        return formatter;
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public void setMessageBus(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public CustomLinksHelper getCustomLinksHelper() {
        if (customLinksHelper == null) {
            final Settings experimentalSettings = getExtensionSettings();
            boolean customLinksEnabled = experimentalSettings.getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class);
            int customLinksRecurseDepth = experimentalSettings.getInt(CoreSettings.TAG_CUSTOM_LINKS_RECURSE_DEPTH, CoreSettings.class);
            customLinksHelper = new CustomLinksHelper(modelRegistry, customLinksEnabled, customLinksRecurseDepth);
        }
        return customLinksHelper;
    }

}
