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
package de.fraunhofer.iosb.ilt.frostserver.auth.keycloak;

import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider.TAG_USERNAME_COLUMN;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider.TAG_USER_TABLE;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRoleDecoderDflt implements UserRoleDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleDecoderDflt.class.getName());

    private String userTable;
    private String usernameColumn;

    @Override
    public void init(CoreSettings coreSettings) {
        Settings authSettings = coreSettings.getAuthSettings();
        userTable = authSettings.get(TAG_USER_TABLE, KeycloakAuthProvider.class);
        usernameColumn = authSettings.get(TAG_USERNAME_COLUMN, KeycloakAuthProvider.class);
    }

    @Override
    public void decodeUserRoles(String username, Set<String> roles, DSLContext dslContext) {
        LOGGER.info("Checking user {} in database...", username);
        final Field<String> usernameField = DSL.field(DSL.name(usernameColumn), String.class);
        final Table<org.jooq.Record> table = DSL.table(DSL.name(userTable));
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
            dslContext.commit().execute();
        }
    }

}
