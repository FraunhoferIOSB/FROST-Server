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
package de.fraunhofer.iosb.ilt.frostserver.util;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_DELETE;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_READ;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_UPDATE;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author scf
 */
public class AuthUtils {

    private AuthUtils() {
        // Utility class.
    }

    public enum Role {
        /**
         * Read Role required.
         */
        READ,
        /**
         * Create Role required.
         */
        CREATE,
        /**
         * Update Role required.
         */
        UPDATE,
        /**
         * Delete Role required.
         */
        DELETE,
        /**
         * Admin Role required.
         */
        ADMIN,
        /**
         * No Role required.
         */
        NONE,
        /**
         * This request is not allowed at all.
         */
        ERROR
    }

    public static Map<Role, String> loadRoleMapping(Settings authSettings) {
        Map<Role, String> mapping = new EnumMap<>(Role.class);
        mapping.put(Role.READ, authSettings.get(TAG_AUTH_ROLE_READ, CoreSettings.class));
        mapping.put(Role.CREATE, authSettings.get(TAG_AUTH_ROLE_CREATE, CoreSettings.class));
        mapping.put(Role.UPDATE, authSettings.get(TAG_AUTH_ROLE_UPDATE, CoreSettings.class));
        mapping.put(Role.DELETE, authSettings.get(TAG_AUTH_ROLE_DELETE, CoreSettings.class));
        mapping.put(Role.ADMIN, authSettings.get(TAG_AUTH_ROLE_ADMIN, CoreSettings.class));
        return mapping;
    }

}
