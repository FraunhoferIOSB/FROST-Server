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
package de.fraunhofer.iosb.ilt.frostserver.plugin.projects;

import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_USERNAME_COLUMN;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_USER_TABLE;

import de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings;
import de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.UserRoleDecoder;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes Roles for the Projects plugin.
 */
public class ProjectRoleDecoder implements UserRoleDecoder, ConfigDefaults {

    private static final String UPR_CLEANUP_QUERY = "delete from \"USER_PROJECT_ROLE\" where \"USER_NAME\" = ?";
    private static final String UPR_INSERT_QUERY = "insert into \"USER_PROJECT_ROLE\""
            + " (\"USER_NAME\", \"PROJECT_ID\", \"ROLE_NAME\")"
            + " VALUES (?,(select \"ID\" from \"PROJECTS\" where \"NAME\"=?),?)";

    private static final String UPR_ROLE_REGEX = "^([a-zA-Z0-9 ]+)__([a-zA-Z0-9]+)$";

    @DefaultValue(UPR_INSERT_QUERY)
    public static final String TAG_UPR_INSERT_QUERY = "prd.insertQuery";

    @DefaultValue(UPR_CLEANUP_QUERY)
    public static final String TAG_UPR_CLEANUP_QUERY = "prd.cleanupQuery";

    @DefaultValue(UPR_ROLE_REGEX)
    public static final String TAG_ROLE_REGEX = "prd.roleRegex";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRoleDecoder.class.getName());

    private String userTable;
    private String usernameColumn;
    private Pattern projectRoleMatcher = Pattern.compile(UPR_ROLE_REGEX);
    private String uprInsertQuery = UPR_INSERT_QUERY;
    private String uprCleanupQuery = UPR_CLEANUP_QUERY;

    @Override
    public void init(CoreSettings coreSettings) {
        final Settings authSettings = coreSettings.getAuthSettings();
        userTable = authSettings.get(TAG_USER_TABLE, KeycloakSettings.class);
        usernameColumn = authSettings.get(TAG_USERNAME_COLUMN, KeycloakSettings.class);
        uprInsertQuery = authSettings.get(TAG_UPR_INSERT_QUERY, ProjectRoleDecoder.class);
        uprCleanupQuery = authSettings.get(TAG_UPR_CLEANUP_QUERY, ProjectRoleDecoder.class);
        String projectRoleRegex = authSettings.get(TAG_ROLE_REGEX, ProjectRoleDecoder.class);
        projectRoleMatcher = Pattern.compile(projectRoleRegex);
    }

    @Override
    public void decodeUserRoles(String username, Set<String> roles, DSLContext dslContext) {
        LOGGER.debug("Checking user {} in database...", username);
        final Field<String> usernameField = DSL.field(DSL.name(usernameColumn), String.class);
        final Table<org.jooq.Record> table = DSL.table(DSL.name(userTable));
        long count = dslContext
                .selectCount()
                .from(table)
                .where(usernameField.eq(username))
                .fetchOne()
                .component1();

        if (count == 0) {
            LOGGER.debug("Adding user {} to database...", username);
            dslContext.insertInto(table)
                    .set(usernameField, username)
                    .execute();
            dslContext.commit().execute();
        }
        int result = dslContext.execute(uprCleanupQuery, username);
        LOGGER.debug("Executed uprCleanup: {} -> {}", username, result);
        for (String role : roles) {
            decodeRole(role, username, dslContext);
        }
        dslContext.commit().execute();
    }

    private void decodeRole(String role, String username, DSLContext dslContext) {
        Matcher m = projectRoleMatcher.matcher(role);
        if (m.matches()) {
            String projectName = m.group(1);
            String roleName = m.group(2);
            try {
                LOGGER.debug("Executing uprInsert: {}, {}, {}", username, projectName, roleName);
                int result = dslContext.execute(uprInsertQuery, username, projectName, roleName);
                LOGGER.debug(" Executed uprInsert: {}, {}, {} -> {}", username, projectName, roleName, result);
            } catch (RuntimeException ex) {
                LOGGER.warn("Exception inserting role " + roleName, ex);
            }
        }
    }
}
