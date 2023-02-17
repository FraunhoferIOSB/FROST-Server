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
package de.fraunhofer.iosb.ilt.frostserver.auth.basic;

import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthProvider;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author scf
 */
public class BasicAuthProvider implements AuthProvider, LiquibaseUser, ConfigDefaults {

    public static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/basicAuthTables.xml";

    @DefaultValueBoolean(false)
    public static final String TAG_AUTO_UPDATE_DATABASE = "autoUpdateDatabase";

    @DefaultValueBoolean(true)
    public static final String TAG_PLAIN_TEXT_PASSWORD = "plainTextPassword";

    @DefaultValue("FROST-Server")
    public static final String TAG_AUTH_REALM_NAME = "realmName";

    @DefaultValue("read")
    public static final String TAG_ROLE_GET = "roleGet";
    @DefaultValue("update")
    public static final String TAG_ROLE_PATCH = "rolePatch";
    @DefaultValue("create")
    public static final String TAG_ROLE_POST = "rolePost";
    @DefaultValue("update")
    public static final String TAG_ROLE_PUT = "rolePut";
    @DefaultValue("delete")
    public static final String TAG_ROLE_DELETE = "roleDelete";
    @DefaultValue("admin")
    public static final String TAG_ROLE_ADMIN = "roleAdmin";

    private CoreSettings coreSettings;

    @Override
    public void init(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        DatabaseHandler.init(coreSettings);
    }

    @Override
    public void addFilter(Object context, CoreSettings coreSettings) {
        BasicAuthFilterHelper.createFilters(context, coreSettings);
    }

    @Override
    public boolean isValidUser(String clientId, String userName, String password) {
        return DatabaseHandler.getInstance(coreSettings).isValidUser(new BasicAuthFilter.UserData(userName, password));
    }

    @Override
    public boolean userHasRole(String clientId, String userName, String roleName) {
        return DatabaseHandler.getInstance(coreSettings).userHasRole(userName, roleName);
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
