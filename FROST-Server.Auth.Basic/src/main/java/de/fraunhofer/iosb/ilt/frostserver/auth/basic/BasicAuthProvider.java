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

import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.util.AuthProvider;
import de.fraunhofer.iosb.ilt.sta.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.sta.util.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author scf
 */
public class BasicAuthProvider implements AuthProvider, LiquibaseUser {

    public static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/basicAuthTables.xml";
    public static final String TAG_AUTO_UPDATE_DATABASE = "autoUpdateDatabase";
    public static final boolean DEF_AUTO_UPDATE_DATABASE = false;
    public static final String TAG_AUTH_REALM_NAME = "realmName";
    public static final String DEF_AUTH_REALM_NAME = "FROST-Server";
    public static final String TAG_ROLE_GET = "roleGet";
    public static final String DEF_ROLE_GET = "read";
    public static final String TAG_ROLE_PATCH = "rolePatch";
    public static final String DEF_ROLE_PATCH = "update";
    public static final String TAG_ROLE_POST = "rolePost";
    public static final String DEF_ROLE_POST = "create";
    public static final String TAG_ROLE_PUT = "rolePut";
    public static final String DEF_ROLE_PUT = "update";
    public static final String TAG_ROLE_DELETE = "roleDelete";
    public static final String DEF_ROLE_DELETE = "delete";

    @Override
    public void init(CoreSettings coreSettings) {
        DatabaseHandler.init(coreSettings);
    }

    @Override
    public void addFilter(Object context, CoreSettings coreSettings) {
        BasicAuthFilter.createFilters(context, coreSettings);
    }

    @Override
    public boolean isValidUser(String userName, String password) {
        return DatabaseHandler.getInstance().isValidUser(userName, password);
    }

    @Override
    public boolean userHasRole(String userName, String roleName) {
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
