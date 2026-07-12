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
package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_AUTO_UPDATE_DATABASE;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_LOWCASE_TABLES;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_PLAIN_TEXT_PASSWORD;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_USER_CACHE_LIFE_MS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_URL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper.CHANGE_SET_NAME;

import de.fraunhofer.iosb.ilt.frostserver.model.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserData;
import de.fraunhofer.iosb.ilt.settings.Settings;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.jooq.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database handler for the basic auth plugin.
 */
public abstract class DatabaseHandler {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);

    private static final Map<CoreSettings, DatabaseHandler> INSTANCES = new HashMap<>();

    private final String liquibaseChangelogFilename;
    protected final CoreSettings coreSettings;
    protected final Settings authSettings;
    protected final boolean plainTextPassword;
    protected final String connectionUrl;
    protected boolean doUpdateDatabase;

    private PassiveExpiringMap<UserData, UserData> cache;

    public static void init(CoreSettings coreSettings) {
        if (INSTANCES.get(coreSettings) == null) {
            createInstance(coreSettings);
        }
    }

    private static synchronized DatabaseHandler createInstance(CoreSettings coreSettings) {
        return INSTANCES.computeIfAbsent(coreSettings, s -> {
            Settings pluginSet = s.getPluginSettings();
            Settings authSet = coreSettings.getAuthSettings();
            boolean enabledCoreModelV1 = pluginSet.getBoolean("coreModel.enable", true);
            boolean lowcaseTables = authSet.getBoolean(TAG_LOWCASE_TABLES, !enabledCoreModelV1);
            if (lowcaseTables) {
                LOGGER.info("Initialising DatabaseHandler with uppercase table names.");
                return new DatabaseHandlerV2(coreSettings);
            } else {
                LOGGER.info("Initialising DatabaseHandler with lowercase table names.");
                return new DatabaseHandlerV1(coreSettings);
            }
        });
    }

    public static DatabaseHandler getInstance(CoreSettings coreSettings) {
        DatabaseHandler instance = INSTANCES.get(coreSettings);
        if (instance == null) {
            LOGGER.error("DatabaseHandler not initialised.");
        }
        return instance;
    }

    protected DatabaseHandler(String liquibaseChangelogFilename, CoreSettings coreSettings) {
        this.liquibaseChangelogFilename = liquibaseChangelogFilename;
        this.coreSettings = coreSettings;
        authSettings = coreSettings.getAuthSettings();
        doUpdateDatabase = authSettings.getBoolean(TAG_AUTO_UPDATE_DATABASE, BasicAuthProvider.class);
        plainTextPassword = authSettings.getBoolean(TAG_PLAIN_TEXT_PASSWORD, BasicAuthProvider.class);
        connectionUrl = authSettings.get(TAG_DB_URL, ConnectionUtils.class);
        int userCacheLifeMs = authSettings.getInt(TAG_USER_CACHE_LIFE_MS, BasicAuthProvider.class);
        if (userCacheLifeMs > 0) {
            LOGGER.info("Enabling user cache.");
            cache = new PassiveExpiringMap<>(userCacheLifeMs);
        }
    }

    public boolean isPlainTextPassword() {
        return plainTextPassword;
    }

    protected abstract Condition passwordCondition(String passwordOrHash);

    /**
     * Checks if the user is valid and adds roles to the userData.
     *
     * @param userData the user data (username and password)
     * @return true if the user is value
     */
    public abstract boolean isValidUser(UserData userData);

    public UserData getFromCache(UserData userData) {
        if (cache == null) {
            return null;
        }
        try {
            return cache.get(userData);
        } catch (RuntimeException ex) {
            LOGGER.debug("Failed to check cache.", ex);
            return null;
        }
    }

    public void addToCache(UserData userData) {
        if (cache != null) {
            try {
                cache.put(userData, userData);
            } catch (RuntimeException exc) {
                LOGGER.debug("Failed to fill cache.", exc);
            }
        }
    }

    /**
     * This method checks if the given user exists with the given password and
     * has the given role.
     *
     * @param userName The username of the user to check the role for.
     * @param userPassOrHash The password or its hash of the user to check the
     * role for.
     * @param roleName The role to check.
     * @return true if the user exists AND has the given password AND has the
     * given role.
     */
    public abstract boolean userHasRole(String userName, String userPassOrHash, String roleName);

    /**
     * This method checks if the given user exists and has the given role.
     *
     * @param userName The username of the user to check the role for.
     * @param roleName The role to check.
     * @return true if the user exists AND has the given password AND has the
     * given role.
     */
    public abstract boolean userHasRole(String userName, String roleName);

    protected void maybeUpdateDatabase() {
        if (doUpdateDatabase) {
            BasicAuthProvider basicAuthProvider = new BasicAuthProvider();
            basicAuthProvider.init(coreSettings);
            Map<String, Object> params = CollectionsHelper.propertiesBuilder()
                    .addProperty(CHANGE_SET_NAME, "Auth.Basic")
                    .build();
            basicAuthProvider.createLiqibaseParams(null, params);
            doUpdateDatabase = LiquibaseUtils.maybeUpdateDatabase(LOGGER, basicAuthProvider, params);
        }
    }

    public String checkForUpgrades(Map<String, Object> params) {
        params.put(CHANGE_SET_NAME, "Auth.Basic");
        Settings customSettings = coreSettings.getAuthSettings();
        try (Connection connection = ConnectionUtils.getConnection(connectionUrl, customSettings)) {
            return LiquibaseHelper.checkForUpgrades(connection, liquibaseChangelogFilename, params);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            return "Failed to initialise database:\n"
                    + ex.getLocalizedMessage()
                    + "\n";
        }
    }

    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        return doUpgrades(out, Collections.emptyMap());
    }

    public boolean doUpgrades(Writer out, Map<String, Object> params) throws UpgradeFailedException, IOException {
        Settings customSettings = coreSettings.getAuthSettings();
        try (Connection connection = ConnectionUtils.getConnection(connectionUrl, customSettings)) {
            return LiquibaseHelper.doUpgrades(connection, liquibaseChangelogFilename, params, out);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            out.append("Failed to initialise database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
            return false;
        }
    }
}
