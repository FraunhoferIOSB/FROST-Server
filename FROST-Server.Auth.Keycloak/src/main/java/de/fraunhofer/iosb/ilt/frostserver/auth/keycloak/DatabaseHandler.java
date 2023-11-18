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
package de.fraunhofer.iosb.ilt.frostserver.auth.keycloak;

import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider.TAG_USERNAME_COLUMN;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider.TAG_USER_TABLE;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_URL;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.ConnectionWrapper;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
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

    private static final Map<CoreSettings, DatabaseHandler> INSTANCES = new HashMap<>();

    private final Settings authSettings;
    private final String connectionUrl;
    private final String userTable;
    private final String usernameColumn;

    public static void init(CoreSettings coreSettings) {
        if (INSTANCES.get(coreSettings) == null) {
            createInstance(coreSettings);
        }
    }

    private static synchronized DatabaseHandler createInstance(CoreSettings coreSettings) {
        return INSTANCES.computeIfAbsent(coreSettings, (s) -> {
            LOGGER.info("Initialising DatabaseHandler.");
            return new DatabaseHandler(coreSettings);
        });
    }

    public static DatabaseHandler getInstance(CoreSettings coreSettings) {
        DatabaseHandler instance = INSTANCES.get(coreSettings);
        if (instance == null) {
            LOGGER.error("DatabaseHandler not initialised.");
        }
        return instance;
    }

    private DatabaseHandler(CoreSettings coreSettings) {
        authSettings = coreSettings.getAuthSettings();
        connectionUrl = authSettings.get(TAG_DB_URL, ConnectionUtils.class, false);
        userTable = authSettings.get(TAG_USER_TABLE, KeycloakAuthProvider.class);
        usernameColumn = authSettings.get(TAG_USERNAME_COLUMN, KeycloakAuthProvider.class);
    }

    /**
     * Checks if the user is registered locally and if not, add the user.
     *
     * @param username the username
     */
    public void enureUserInUsertable(String username) {
        try (final ConnectionWrapper connectionProvider = new ConnectionWrapper(authSettings, connectionUrl)) {
            final DSLContext dslContext = DSL.using(connectionProvider.get(), SQLDialect.POSTGRES);
            final Field<String> usernameField = DSL.field(DSL.name(usernameColumn), String.class);
            final Table<Record> table = DSL.table(DSL.name(userTable));
            long count = dslContext
                    .selectCount()
                    .from(table)
                    .where(usernameField.eq(username))
                    .fetchOne()
                    .component1();
            if (count == 0) {
                dslContext.insertInto(table)
                        .set(usernameField, username)
                        .execute();
                connectionProvider.commit();
            }
        } catch (SQLException | RuntimeException exc) {
            LOGGER.error("Failed to register user locally.", exc);
        }
    }

}
