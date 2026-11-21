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
package de.fraunhofer.iosb.ilt.frostserver.util;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_DELETE;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_READ;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_UPDATE;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.settings.Settings;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for Auth.
 */
public class AuthUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUtils.class.getName());

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

    public static <T> Class<T> findClass(String className, Class<T> requiredInterface) {
        try {
            Class<?> foundClass = ClassUtils.getClass(className);
            if (requiredInterface.isAssignableFrom(foundClass)) {
                return (Class<T>) foundClass;
            } else {
                LOGGER.error("Configured class does not implement {}: {}", requiredInterface, className);
                return null;
            }
        } catch (ClassNotFoundException | SecurityException exc) {
            LOGGER.error("Could not initialise auth class.", exc);
        }
        return null;
    }

    public static <T> T instantiateClass(String className, Class<T> requiredInterface, T fallback) {
        Class<T> foundClass = findClass(className, requiredInterface);
        if (foundClass == null) {
            return fallback;
        }
        try {
            return foundClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException exc) {
            LOGGER.error("Could not initialise auth class.", exc);
        }
        return fallback;
    }

    public static AuthProvider loadAuthProvider(String authProviderClassName, CoreSettings coreSettings, AuthProvider fallback) {
        AuthProvider tempProvider = instantiateClass(authProviderClassName, AuthProvider.class, fallback);
        if (tempProvider == fallback) {
            return fallback;
        }
        tempProvider.init(coreSettings);
        return tempProvider;
    }
}
