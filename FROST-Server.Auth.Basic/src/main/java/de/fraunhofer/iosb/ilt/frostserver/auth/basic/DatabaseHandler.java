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

import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spatial.PostGISTemplates;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.LIQUIBASE_CHANGELOG_FILENAME;
import static de.fraunhofer.iosb.ilt.frostserver.auth.basic.BasicAuthProvider.TAG_AUTO_UPDATE_DATABASE;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.ConnectionUtils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.LiquibaseHelper;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import de.fraunhofer.iosb.ilt.sta.util.LiquibaseUtils;
import de.fraunhofer.iosb.ilt.sta.util.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class DatabaseHandler {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);
    private static DatabaseHandler instance;

    public static void init(CoreSettings coreSettings) {
        if (instance == null) {
            createInstance(coreSettings);
        }
    }

    private static synchronized void createInstance(CoreSettings coreSettings) {
        if (instance == null) {
            LOGGER.error("Initialising DatabaseHandler.");
            instance = new DatabaseHandler(coreSettings);
        }
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            LOGGER.error("DatabaseHandler not initialised.");
        }
        return instance;
    }

    private final CoreSettings coreSettings;
    private boolean maybeUpdateDatabase = true;
    private ConnectionUtils.ConnectionWrapper connectionProvider;
    private SQLQueryFactory queryFactory;

    private DatabaseHandler(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        Settings authSettings = coreSettings.getAuthSettings();

        maybeUpdateDatabase = authSettings.getBoolean(TAG_AUTO_UPDATE_DATABASE, BasicAuthProvider.class);
        connectionProvider = new ConnectionUtils.ConnectionWrapper(authSettings);

    }

    private synchronized SQLQueryFactory createQueryFactory() {
        if (queryFactory == null) {
            SQLTemplates templates = PostGISTemplates.builder().quote().build();
            queryFactory = new SQLQueryFactory(templates, connectionProvider);
        }
        return queryFactory;
    }

    public synchronized SQLQueryFactory getQueryFactory() {
        if (queryFactory == null) {
            createQueryFactory();
        }
        return queryFactory;
    }

    public boolean isValidUser(String userName, String password) {
        maybeUpdateDatabase();
        try {
            Integer one = createQueryFactory()
                    .selectOne()
                    .from(QUsers.USERS)
                    .where(
                            QUsers.USERS.userName.eq(userName)
                                    .and(QUsers.USERS.userPass.eq(password))
                    ).fetchFirst();
            return one != null;
        } catch (Exception exc) {
            LOGGER.error("Failed to check user credentials.", exc);
            return false;
        }
    }

    /**
     * This method checks if the given user exists and has the given role.
     *
     * @param userName The username of the user to check the role for.
     * @param userPass The password of the user to check the role for.
     * @param roleName The role to check.
     * @return true if the user exists AND has the given password AND has the
     * given role.
     */
    public boolean userHasRole(String userName, String userPass, String roleName) {
        maybeUpdateDatabase();
        try {
            Integer one = createQueryFactory()
                    .selectOne()
                    .from(QUsers.USERS)
                    .leftJoin(QUsersRoles.USER_ROLES)
                    .on(QUsers.USERS.userName.eq(QUsersRoles.USER_ROLES.userName))
                    .where(
                            QUsers.USERS.userName.eq(userName)
                                    .and(QUsers.USERS.userPass.eq(userPass))
                                    .and(QUsersRoles.USER_ROLES.roleName.eq(roleName))
                    ).fetchFirst();
            return one != null;
        } catch (Exception exc) {
            LOGGER.error("Failed to check user rights.", exc);
            return false;
        } finally {
            connectionProvider.doRollback();
        }
    }

    public boolean userHasRole(String userName, String roleName) {
        try {
            Integer one = createQueryFactory()
                    .selectOne()
                    .from(QUsersRoles.USER_ROLES)
                    .where(
                            QUsersRoles.USER_ROLES.userName.eq(userName)
                                    .and(QUsersRoles.USER_ROLES.roleName.eq(roleName))
                    ).fetchFirst();
            return one != null;
        } catch (Exception exc) {
            LOGGER.error("Failed to check user rights.", exc);
            return false;
        }
    }

    private void maybeUpdateDatabase() {
        if (maybeUpdateDatabase) {
            BasicAuthProvider basicAuthProvider = new BasicAuthProvider();
            basicAuthProvider.init(coreSettings);
            maybeUpdateDatabase = LiquibaseUtils.maybeUpdateDatabase(LOGGER, basicAuthProvider);
        }
    }

    public String checkForUpgrades() {
        Settings customSettings = coreSettings.getAuthSettings();
        try (Connection connection = ConnectionUtils.getConnection("FROST-BasicAuth", customSettings)) {
            return LiquibaseHelper.checkForUpgrades(connection, LIQUIBASE_CHANGELOG_FILENAME);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            return "Failed to initialise database:\n"
                    + ex.getLocalizedMessage()
                    + "\n";
        }
    }

    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        Settings customSettings = coreSettings.getAuthSettings();
        try (Connection connection = ConnectionUtils.getConnection("FROST-BasicAuth", customSettings)) {
            return LiquibaseHelper.doUpgrades(connection, LIQUIBASE_CHANGELOG_FILENAME, out);
        } catch (SQLException ex) {
            LOGGER.error("Could not initialise database.", ex);
            out.append("Failed to initialise database:\n");
            out.append(ex.getLocalizedMessage());
            out.append("\n");
            return false;
        }
    }
}
