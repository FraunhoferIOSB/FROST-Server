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

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTHENTICATE_ONLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_PASSWORD_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_USERNAME_LENGTH;

import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthProvider;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserClientInfo;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserData;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A FROST Auth implementation for Basic Authentication.
 */
public class BasicAuthProvider implements AuthProvider, LiquibaseUser, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthProvider.class.getName());

    public static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/basicAuthTables.xml";

    @DefaultValueBoolean(false)
    public static final String TAG_AUTO_UPDATE_DATABASE = "autoUpdateDatabase";

    @DefaultValueBoolean(true)
    public static final String TAG_PLAIN_TEXT_PASSWORD = "plainTextPassword";

    @DefaultValueInt(10)
    public static final String TAG_MAX_CLIENTS_PER_USER = "maxClientsPerUser";

    @DefaultValueInt(MAX_PASSWORD_LENGTH)
    public static final String TAG_MAX_PASSWORD_LENGTH = "maxPasswordLength";

    @DefaultValueInt(MAX_USERNAME_LENGTH)
    public static final String TAG_MAX_USERNAME_LENGTH = "maxUsernameLength";

    @DefaultValueInt(60_000)
    public static final String TAG_USER_CACHE_LIFE_MS = "userCacheLifeMs";

    @DefaultValue("FROST-Server")
    public static final String TAG_AUTH_REALM_NAME = "realmName";

    @DefaultValue(PrincipalExtended.ROLE_READ)
    public static final String TAG_HTTP_ROLE_GET = "roleGet";
    @DefaultValue(PrincipalExtended.ROLE_UPDATE)
    public static final String TAG_HTTP_ROLE_PATCH = "rolePatch";
    @DefaultValue(PrincipalExtended.ROLE_CREATE)
    public static final String TAG_HTTP_ROLE_POST = "rolePost";
    @DefaultValue(PrincipalExtended.ROLE_UPDATE)
    public static final String TAG_HTTP_ROLE_PUT = "rolePut";
    @DefaultValue(PrincipalExtended.ROLE_DELETE)
    public static final String TAG_HTTP_ROLE_DELETE = "roleDelete";

    private CoreSettings coreSettings;
    private String roleAdmin;
    private boolean authenticateOnly;
    private int maxClientsPerUser;
    private int maxPassLength = MAX_PASSWORD_LENGTH;
    private int maxNameLength = MAX_USERNAME_LENGTH;

    private final Map<String, UserClientInfo> clientidToUserinfo = new ConcurrentHashMap<>();
    private final Map<String, UserClientInfo> usernameToUserinfo = new ConcurrentHashMap<>();

    @Override
    public InitResult init(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        DatabaseHandler.init(coreSettings);
        final Settings authSettings = coreSettings.getAuthSettings();
        authenticateOnly = authSettings.getBoolean(TAG_AUTHENTICATE_ONLY, CoreSettings.class);
        roleAdmin = authSettings.get(TAG_AUTH_ROLE_ADMIN, CoreSettings.class);
        maxClientsPerUser = authSettings.getInt(TAG_MAX_CLIENTS_PER_USER, getClass());
        maxPassLength = authSettings.getInt(TAG_MAX_PASSWORD_LENGTH, getClass());
        maxNameLength = authSettings.getInt(TAG_MAX_USERNAME_LENGTH, getClass());
        return InitResult.INIT_OK;
    }

    @Override
    public void addFilter(Object context, CoreSettings coreSettings) {
        BasicAuthFilterHelper.createFilters(context, coreSettings);
    }

    @Override
    public boolean isValidUser(String clientId, String userName, String password) {
        final UserData userData = new UserData(userName, maxNameLength, password, maxPassLength);
        final boolean validUser = DatabaseHandler.getInstance(coreSettings)
                .isValidUser(userData);
        if (!validUser) {
            return false;
        }
        boolean admin = userData.roles.contains(roleAdmin);

        final PrincipalExtended userPrincipal = new PrincipalExtended(userData.userName, admin, userData.roles);
        final UserClientInfo userInfo = usernameToUserinfo.computeIfAbsent(userData.userName, t -> new UserClientInfo());
        userInfo.setUserPrincipal(userPrincipal);

        String oldClientId = userInfo.addClientId(clientId, maxClientsPerUser);
        if (oldClientId != null) {
            clientidToUserinfo.remove(oldClientId);
        }
        clientidToUserinfo.put(clientId, userInfo);
        return validUser;
    }

    @Override
    public boolean userHasRole(String clientId, String userName, String roleName) {
        if (authenticateOnly && !roleName.equalsIgnoreCase(roleAdmin)) {
            LOGGER.trace("Only authenticating, not checking if User {} has role {}", userName, roleName);
            return true;
        }
        return DatabaseHandler.getInstance(coreSettings)
                .userHasRole(userName, roleName);
    }

    @Override
    public PrincipalExtended getUserPrincipal(String clientId) {
        UserClientInfo userInfo = clientidToUserinfo.get(clientId);
        if (userInfo == null) {
            return PrincipalExtended.ANONYMOUS_PRINCIPAL;
        }
        return userInfo.getUserPrincipal();
    }

    public Map<String, Object> createLiqibaseParams(DatabaseHandler dbHandler, Map<String, Object> target) {
        if (target == null) {
            target = new LinkedHashMap<>();
        }
        target.put(TAG_PLAIN_TEXT_PASSWORD, Boolean.toString(dbHandler.isPlainTextPassword()));
        return target;
    }

    @Override
    public String checkForUpgrades() {
        final DatabaseHandler dbHandler = DatabaseHandler.getInstance(coreSettings);
        return dbHandler.checkForUpgrades(createLiqibaseParams(dbHandler, null));
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        final DatabaseHandler dbHandler = DatabaseHandler.getInstance(coreSettings);
        return dbHandler.doUpgrades(out, createLiqibaseParams(dbHandler, null));
    }
}
