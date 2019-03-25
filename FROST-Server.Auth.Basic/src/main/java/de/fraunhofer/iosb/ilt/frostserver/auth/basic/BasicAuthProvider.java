/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.util.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author scf
 */
public class BasicAuthProvider implements AuthProvider, LiquibaseUser, ConfigDefaults {

    public static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/basicAuthTables.xml";
    @DefaultValueBoolean(false)
    public static final String TAG_AUTO_UPDATE_DATABASE = "autoUpdateDatabase";
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

    @Override
    public void init(CoreSettings coreSettings) {
        DatabaseHandler.init(coreSettings);
    }

    @Override
    public void addFilter(Object context, CoreSettings coreSettings) {
        BasicAuthFilterHelper.createFilters(context, coreSettings);
    }

    @Override
    public boolean isValidUser(String clientId, String userName, String password) {
        return DatabaseHandler.getInstance().isValidUser(userName, password);
    }

    @Override
    public boolean userHasRole(String clientId, String userName, String roleName) {
        return DatabaseHandler.getInstance().userHasRole(userName, roleName);
    }

    @Override
    public String checkForUpgrades() {
        return DatabaseHandler.getInstance().checkForUpgrades();
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        return DatabaseHandler.getInstance().doUpgrades(out);
    }
}
