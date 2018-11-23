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
package de.fraunhofer.iosb.ilt.frostserver.auth.keycloak;

import com.google.common.base.Strings;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider.TAG_KEYCLOAK_CONFIG;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakAuthProvider.TAG_KEYCLOAK_CONFIG_FILE;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.Settings;
import de.fraunhofer.iosb.ilt.sta.util.AuthUtils.Role;
import de.fraunhofer.iosb.ilt.sta.util.HttpMethod;
import de.fraunhofer.iosb.ilt.sta.util.StringHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class Utils {

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
        // Utility class.
    }

    /**
     * An interface for classes that map HTTP methods to a user Role.
     */
    public static interface MethodRoleMapper {

        /**
         * Map the given method to a user Role.
         *
         * @param method The method to map.
         * @return the Role the user must have to execute the method.
         */
        public Role findRole(HttpMethod method);
    }

    /**
     * Load the keycloak config from the given CoreSettings.
     *
     * @param coreSettings the CoreSettings to load the keycloak config from.
     * @return the keycloak config.
     */
    public static String getKeycloakConfig(CoreSettings coreSettings) {
        Settings authSettings = coreSettings.getAuthSettings();
        String keycloakConfig = authSettings.get(TAG_KEYCLOAK_CONFIG, "");
        if (!Strings.isNullOrEmpty(keycloakConfig)) {
            return keycloakConfig;
        }
        return getKeycloakConfigFromFile(authSettings);
    }

    /**
     * Load the contents of a keycloak config file, specified in the given
     * Settings object.
     *
     * @param authSettings The settings object to fetch the config file path
     * from.
     * @return the contents of the config file.
     */
    private static String getKeycloakConfigFromFile(Settings authSettings) {
        String keycloakConfigFile = authSettings.get(TAG_KEYCLOAK_CONFIG_FILE, "");
        if (Strings.isNullOrEmpty(keycloakConfigFile)) {
            return "";
        }
        try {
            return FileUtils.readFileToString(FileUtils.getFile(keycloakConfigFile), StringHelper.UTF8);
        } catch (IOException exc) {
            LOGGER.error("Failed to read keycloak config file.", exc);
            return "";
        }
    }

    /**
     * Create a new KeycloakDeployment from settings loaded from the given
     * CoreSettings.
     *
     * @param coreSettings The CoreSettings to create a KeycloakDeployment from.
     * @return the new KeycloakDeployment.
     */
    public static KeycloakDeployment resolveDeployment(CoreSettings coreSettings) {
        String keycloakConfig = getKeycloakConfig(coreSettings);
        InputStream input = new ByteArrayInputStream(keycloakConfig.getBytes(StringHelper.UTF8));
        return KeycloakDeploymentBuilder.build(input);
    }

}
