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
package de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthProvider;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.UpgradeFailedException;
import io.moquette.spi.impl.subscriptions.Topic;
import io.moquette.spi.security.IAuthenticator;
import io.moquette.spi.security.IAuthorizator;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class AuthWrapper implements IAuthenticator, IAuthorizator {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthWrapper.class);
    private static final AuthProvider AUTH_PROVIDER_DENY_ALL = new AuthProvider() {
        @Override
        public void addFilter(Object context, CoreSettings coreSettings) {
            // This dummy does not add filters.
        }

        @Override
        public boolean isValidUser(String clientId, String userName, String password) {
            return false;
        }

        @Override
        public boolean userHasRole(String clientId, String userName, String roleName) {
            return false;
        }

        @Override
        public void init(CoreSettings coreSettings) {
            // This dummy needs no initialisation.
        }

        @Override
        public String checkForUpgrades() {
            return "Something went wrong while configuring your auth provider. You have a configuration error.";
        }

        @Override
        public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
            return false;
        }
    };

    private final AuthProvider authProvider;
    private final boolean anonymousRead;
    private final String roleRead;
    private final String roleCeate;
    private final String frostClientId;

    public AuthWrapper(CoreSettings coreSettings, String authProviderClassName, String frostClientId) {
        LOGGER.info("Initialising authentication.");
        this.frostClientId = frostClientId;
        Settings authSettings = coreSettings.getAuthSettings();
        anonymousRead = authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class);
        Map<AuthUtils.Role, String> roleMapping = AuthUtils.loadRoleMapping(authSettings);
        roleRead = roleMapping.get(AuthUtils.Role.READ);
        roleCeate = roleMapping.get(AuthUtils.Role.CREATE);

        AuthProvider tempAuthProvider;
        try {
            Class<?> authConfigClass = ClassUtils.getClass(authProviderClassName);
            if (AuthProvider.class.isAssignableFrom(authConfigClass)) {
                Class<AuthProvider> filterConfigClass = (Class<AuthProvider>) authConfigClass;
                tempAuthProvider = filterConfigClass.getDeclaredConstructor().newInstance();
                tempAuthProvider.init(coreSettings);
            } else {
                LOGGER.error("Configured class does not implement AuthProvider: {}", authProviderClassName);
                tempAuthProvider = AUTH_PROVIDER_DENY_ALL;
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException exc) {
            LOGGER.error("Could not initialise auth class.", exc);
            tempAuthProvider = AUTH_PROVIDER_DENY_ALL;
        }
        authProvider = tempAuthProvider;
    }

    @Override
    public boolean checkValid(String clientId, String username, byte[] password) {
        if (frostClientId.equalsIgnoreCase(clientId)) {
            return true;
        }
        return authProvider.isValidUser(clientId, username, new String(password, StringHelper.UTF8));
    }

    @Override
    public boolean canWrite(Topic topic, String user, String clientId) {
        if (frostClientId.equalsIgnoreCase(clientId)) {
            return true;
        }
        return authProvider.userHasRole(clientId, user, roleCeate);
    }

    @Override
    public boolean canRead(Topic topic, String user, String clientId) {
        if (frostClientId.equalsIgnoreCase(clientId)) {
            return true;
        }
        return anonymousRead || authProvider.userHasRole(clientId, user, roleRead);
    }

}
