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

import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_PASSWORD_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_USERNAME_LENGTH;

import de.fraunhofer.iosb.ilt.frostclient.settings.annotation.SensitiveValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;

/**
 * Holds the settigs for the Keycloak Auth Provider.
 */
public class KeycloakSettings implements ConfigDefaults {

    @DefaultValue("")
    @SensitiveValue
    public static final String TAG_KEYCLOAK_CONFIG = "keycloakConfig";

    @DefaultValue("")
    public static final String TAG_KEYCLOAK_CONFIG_FILE = "keycloakConfigFile";

    /**
     * The URL on the Keycloak server that can be used to download the Keycloak
     * config file. Usually this URL is in the form of:
     * https://keycloak.example.com/auth/realms/[realm]/clients-registrations/install/[clientId]
     */
    @DefaultValue("")
    public static final String TAG_KEYCLOAK_CONFIG_URL = "keycloakConfigUrl";

    /**
     * If the client has "access-type" set to "confidential" then a secret is
     * required to download the configuration. This secret can be found in the
     * configuration itself, in Keycloak.
     */
    @DefaultValue("")
    @SensitiveValue
    public static final String TAG_KEYCLOAK_CONFIG_SECRET = "keycloakConfigSecret";

    @DefaultValueInt(10)
    public static final String TAG_MAX_CLIENTS_PER_USER = "maxClientsPerUser";

    @DefaultValueBoolean(false)
    public static final String TAG_REGISTER_USER_LOCALLY = "registerUserLocally";

    @DefaultValue("USERS")
    public static final String TAG_USER_TABLE = "userTable";

    @DefaultValue("USER_NAME")
    public static final String TAG_USERNAME_COLUMN = "usernameColumn";

    @DefaultValueInt(MAX_PASSWORD_LENGTH)
    public static final String TAG_MAX_PASSWORD_LENGTH = "maxPasswordLength";

    @DefaultValueInt(MAX_USERNAME_LENGTH)
    public static final String TAG_MAX_USERNAME_LENGTH = "maxUsernameLength";

    @DefaultValue("de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.UserRoleDecoderDflt")
    public static final String TAG_USER_ROLE_DECODER_CLASS = "userRoleDecoderClass";

    @DefaultValue("PT5M")
    public static final String TAG_USER_CACHE_LIFETIME = "userCacheLifetime";

    @DefaultValue("PT5S")
    public static final String TAG_USER_CACHE_CLEANUP_INTERVAL = "userCacheCleanupInterval";

}
