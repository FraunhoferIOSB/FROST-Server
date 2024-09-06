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

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.Set;
import org.jooq.DSLContext;

/**
 * A interface for classes that decode Roles.
 */
public interface UserRoleDecoder {

    /**
     * Initialise the role decoder.
     *
     * @param coreSettings The CoreSettings to load setting from.
     */
    public default void init(CoreSettings coreSettings) {
        // Does nothing by default.
    }

    /**
     * Decode the given role for the user with the given name. Use the given
     * DSLContext for any database access.
     *
     * @param username The name of the user the role belongs to.
     * @param roles The roles to decode for the user.
     * @param dslContext The DSL Context for database access.
     */
    public void decodeUserRoles(String username, Set<String> roles, DSLContext dslContext);
}
