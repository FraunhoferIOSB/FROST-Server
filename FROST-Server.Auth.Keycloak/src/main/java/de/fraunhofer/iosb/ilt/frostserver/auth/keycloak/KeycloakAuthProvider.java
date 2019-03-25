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

import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthProvider;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import org.keycloak.adapters.jaas.AbstractKeycloakLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class KeycloakAuthProvider implements AuthProvider, LiquibaseUser, ConfigDefaults {

    @DefaultValue("")
    public static final String TAG_KEYCLOAK_CONFIG = "keycloakConfig";
    @DefaultValue("")
    public static final String TAG_KEYCLOAK_CONFIG_FILE = "keycloakConfigFile";
    /**
     * The URL on the Keycloak server that can be used to download the Keycloak
     * config file. Usually this url is in the for of:
     * https://keycloak.example.com/auth/realms/[realm]/clients-registrations/install/[client
     * id]
     */
    @DefaultValue("")
    public static final String TAG_KEYCLOAK_CONFIG_URL = "keycloakConfigUrl";
    /**
     * If the client has "access-type" set to "confidential" then a secret is
     * required to download the configuration. This secret can be found in the
     * configuration itself, in Keycloak.
     */
    @DefaultValue("")
    public static final String TAG_KEYCLOAK_CONFIG_SECRET = "keycloakConfigSecret";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakAuthProvider.class);
    /**
     * This is a "fake" filename, because Keycloak wants to have a filename to
     * store configurations in a map.
     */
    private static final String FROST_SERVER_KEYCLOAKJSON = "FROST-Server-Keycloak.json";
    private static final int CUTOFF_HOURS = 24;

    private CoreSettings coreSettings;

    private class Client {

        public final String userName;
        private Instant lastSeen;
        private Subject subject;

        public Client(String userName) {
            this.userName = userName;
        }

        /**
         * @return the lastSeen
         */
        public Instant getLastSeen() {
            return lastSeen;
        }

        /**
         * @param lastSeen the lastSeen to set
         */
        public void setLastSeen(Instant lastSeen) {
            this.lastSeen = lastSeen;
        }

        /**
         * @return the subject
         */
        public Subject getSubject() {
            return subject;
        }

        /**
         * @param subject the subject to set
         */
        public void setSubject(Subject subject) {
            this.subject = subject;
        }

    }

    /**
     * The map of clients. We need those to determine the authorisation.
     */
    private static final Map<String, Client> CLIENTMAP = new ConcurrentHashMap<>();
    private static final Map<String, Object> SHARED_STATE = new ConcurrentHashMap<>();
    private static final Map<String, Object> OPTIONS = new HashMap<>();

    @Override
    public void init(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        OPTIONS.put("keycloak-config-file", FROST_SERVER_KEYCLOAKJSON);
    }

    @Override
    public void addFilter(Object context, CoreSettings coreSettings) {
        KeycloakFilterHelper.createFilters(context);
    }

    @Override
    public boolean isValidUser(String clientId, String username, String password) {
        AbstractKeycloakLoginModule loginModule;
        if (password.length() > 50) {
            LOGGER.debug("Using BearerTokenLoginModule...");
            loginModule = new BearerTokenLoginModuleFrost(coreSettings);
        } else {
            LOGGER.debug("Using DirectAccessGrantsLoginModule...");
            loginModule = new DirectAccessGrantsLoginModuleFrost(coreSettings);
        }

        clientMapCleanup();

        return checkLogin(loginModule, username, password, clientId);
    }

    private boolean checkLogin(AbstractKeycloakLoginModule loginModule, String username, String password, String clientId) {
        try {
            LOGGER.debug("Login for user {} ({})", username, clientId);
            Subject subject = new Subject();
            loginModule.initialize(
                    subject,
                    (Callback[] callbacks) -> {
                        ((NameCallback) callbacks[0]).setName(username);
                        ((PasswordCallback) callbacks[1]).setPassword(password.toCharArray());
                    },
                    SHARED_STATE,
                    OPTIONS);
            boolean login = loginModule.login();
            if (login) {
                loginModule.commit();
                Client client = new Client(username);
                client.setLastSeen(Instant.now());
                client.setSubject(subject);
                CLIENTMAP.put(clientId, client);
            }
            return login;
        } catch (LoginException ex) {
            LOGGER.error("Login failed with exception: {}", ex.getMessage());
            LOGGER.debug("Exception:", ex);
            return false;
        }
    }

    @Override
    public boolean userHasRole(String clientId, String userName, String roleName) {
        Client client = CLIENTMAP.get(clientId);
        if (client == null) {
            return false;
        }
        client.setLastSeen(Instant.now());
        boolean hasRole = client.getSubject().getPrincipals().stream().anyMatch(p -> p.getName().equalsIgnoreCase(roleName));
        LOGGER.trace("User {} has role {}: {}", userName, roleName, hasRole);
        return hasRole;
    }

    @Override
    public String checkForUpgrades() {
        return "";
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        return true;
    }

    private void clientMapCleanup() {
        try {
            Instant cutoff = Instant.now();

            cutoff = cutoff.plus(-CUTOFF_HOURS, ChronoUnit.HOURS);
            LOGGER.debug("Cleaning up client map... Current size: {}.", CLIENTMAP.size());
            Iterator<Map.Entry<String, Client>> i;
            for (i = CLIENTMAP.entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, Client> entry = i.next();
                if (entry.getValue().getLastSeen().isBefore(cutoff)) {
                    i.remove();
                }
            }
            LOGGER.debug("Done cleaning up client map. Current size: {}.", CLIENTMAP.size());
        } catch (Exception e) {
            LOGGER.warn("Exception while cleaning up client map.", e);
        }
    }
}
