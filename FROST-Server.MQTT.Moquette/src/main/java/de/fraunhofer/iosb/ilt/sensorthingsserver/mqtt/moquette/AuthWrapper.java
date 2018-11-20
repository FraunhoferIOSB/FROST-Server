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
package de.fraunhofer.iosb.ilt.sensorthingsserver.mqtt.moquette;

import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.DEF_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import de.fraunhofer.iosb.ilt.sta.util.AuthProvider;
import de.fraunhofer.iosb.ilt.sta.util.StringHelper;
import de.fraunhofer.iosb.ilt.sta.util.UpgradeFailedException;
import io.moquette.spi.impl.subscriptions.Topic;
import io.moquette.spi.security.IAuthenticator;
import io.moquette.spi.security.IAuthorizator;
import java.io.IOException;
import java.io.Writer;
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
    private static final AuthProvider authProviderDenyAll = new AuthProvider() {
        @Override
        public void addFilter(Object context, CoreSettings coreSettings) {
        }

        @Override
        public boolean isValidUser(String userName, String password) {
            return false;
        }

        @Override
        public boolean userHasRole(String userName, String roleName) {
            return false;
        }

        @Override
        public void init(CoreSettings coreSettings) {
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
    private AuthProvider authProvider;
    private boolean anonymousRead;

    public AuthWrapper(CoreSettings coreSettings, String authProviderClassName) {
        LOGGER.info("Initialising authentication.");
        anonymousRead = coreSettings.getAuthSettings().getBoolean(TAG_AUTH_ALLOW_ANON_READ, DEF_AUTH_ALLOW_ANON_READ);
        try {
            Class<?> authConfigClass = ClassUtils.getClass(authProviderClassName);
            if (AuthProvider.class.isAssignableFrom(authConfigClass)) {
                Class<AuthProvider> filterConfigClass = (Class<AuthProvider>) authConfigClass;
                authProvider = filterConfigClass.newInstance();
                authProvider.init(coreSettings);
            } else {
                LOGGER.error("Configured class does not implement AuthProvider: {}", authProviderClassName);
                authProvider = authProviderDenyAll;
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exc) {
            LOGGER.error("Could not initialise auth class.", exc);
            authProvider = authProviderDenyAll;
        }
    }

    @Override
    public boolean checkValid(String clientId, String username, byte[] password) {
        return authProvider.isValidUser(username, new String(password, StringHelper.UTF8));
    }

    @Override
    public boolean canWrite(Topic topic, String user, String client) {
        return authProvider.userHasRole(user, "create");
    }

    @Override
    public boolean canRead(Topic topic, String user, String client) {
        return anonymousRead || authProvider.userHasRole(user, "read");
    }

}
