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

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.ConnectionWrapper;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserData;
import java.sql.SQLException;
import java.util.Objects;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database handler for the basic auth plugin.
 */
public class DatabaseHandlerV1 extends DatabaseHandler {

    public static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/basicAuthTables.xml";

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandlerV1.class);

    protected DatabaseHandlerV1(CoreSettings coreSettings) {
        super(LIQUIBASE_CHANGELOG_FILENAME, coreSettings);
    }

    @Override
    protected Condition passwordCondition(String passwordOrHash) {
        return TableV1Users.USERS.userPass.eq(isPlainTextPassword()
                ? DSL.val(passwordOrHash)
                : DSL.function(
                        "crypt", String.class, DSL.val(passwordOrHash), TableV1Users.USERS.userPass));
    }

    @Override
    public boolean isValidUser(UserData userData) {
        final UserData cachedData = getFromCache(userData);
        if (cachedData != null) {
            userData.roles.addAll(cachedData.roles);
            return true;
        }
        maybeUpdateDatabase();
        try (final ConnectionWrapper connectionProvider = new ConnectionWrapper(authSettings, connectionUrl)) {
            final DSLContext dslContext = DSL.using(connectionProvider.get(), SQLDialect.POSTGRES);
            Result<Record1<String>> roles = dslContext
                    .select(TableV1UsersRoles.USER_ROLES.roleName)
                    .from(TableV1Users.USERS)
                    .leftJoin(TableV1UsersRoles.USER_ROLES).on(TableV1Users.USERS.userName.eq(TableV1UsersRoles.USER_ROLES.userName))
                    .where(
                            TableV1Users.USERS.userName.eq(userData.userName)
                                    .and(passwordCondition(userData.userPass)))
                    .fetch();
            roles.getValues(TableV1UsersRoles.USER_ROLES.roleName)
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(userData.roles::add);
            boolean valid = !roles.isEmpty();
            if (valid) {
                addToCache(userData);
            }
            return valid;
        } catch (SQLException | RuntimeException exc) {
            LOGGER.error("Failed to check user credentials.", exc);
            return false;
        }
    }

    @Override
    public boolean userHasRole(String userName, String userPassOrHash, String roleName) {
        maybeUpdateDatabase();
        try (final ConnectionWrapper connectionProvider = new ConnectionWrapper(authSettings, connectionUrl)) {
            final DSLContext dslContext = DSL.using(connectionProvider.get(), SQLDialect.POSTGRES);
            Record1<Integer> one = dslContext
                    .selectOne()
                    .from(TableV1Users.USERS)
                    .leftJoin(TableV1UsersRoles.USER_ROLES)
                    .on(TableV1Users.USERS.userName.eq(TableV1UsersRoles.USER_ROLES.userName))
                    .where(
                            TableV1Users.USERS.userName.eq(userName)
                                    .and(passwordCondition(userPassOrHash))
                                    .and(TableV1UsersRoles.USER_ROLES.roleName.eq(roleName)))
                    .fetchOne();
            return one != null;
        } catch (SQLException | RuntimeException exc) {
            LOGGER.error("Failed to check user rights.", exc);
            return false;
        }
    }

    @Override
    public boolean userHasRole(String userName, String roleName) {
        if (userName == null) {
            return false;
        }
        maybeUpdateDatabase();
        try (final ConnectionWrapper connectionProvider = new ConnectionWrapper(authSettings, connectionUrl)) {
            final DSLContext dslContext = DSL.using(connectionProvider.get(), SQLDialect.POSTGRES);
            Record1<Integer> one = dslContext
                    .selectOne()
                    .from(TableV1UsersRoles.USER_ROLES)
                    .where(
                            TableV1UsersRoles.USER_ROLES.userName.eq(userName)
                                    .and(TableV1UsersRoles.USER_ROLES.roleName.eq(roleName)))
                    .fetchOne();
            return one != null;
        } catch (SQLException | RuntimeException exc) {
            LOGGER.error("Failed to check user rights.", exc);
            return false;
        }
    }

}
