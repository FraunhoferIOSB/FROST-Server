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

import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_MAX_CLIENTS_PER_USER;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_MAX_PASSWORD_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_MAX_USERNAME_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_REGISTER_USER_LOCALLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTHENTICATE_ONLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_PASSWORD_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_USERNAME_LENGTH;

import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthProvider;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserClientInfo;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserData;
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
public class KeycloakAuthProvider implements AuthProvider, LiquibaseUser {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakAuthProvider.class);

    /**
     * This is a "fake" filename, because Keycloak wants to have a filename to
     * store configurations in a map.
     */
    private static final String FROST_SERVER_KEYCLOAKJSON = "FROST-Server-Keycloak.json";

    private static final int CUTOFF_HOURS = 24;

    private CoreSettings coreSettings;
    private String roleAdmin;
    private int maxClientsPerUser;
    private boolean registerUserLocally;
    private boolean authenticateOnly;
    private DatabaseHandler databaseHandler;
    private int maxPassLength = MAX_PASSWORD_LENGTH;
    private int maxNameLength = MAX_USERNAME_LENGTH;

    private final Map<String, UserClientInfo> clientidToUserinfo = new ConcurrentHashMap<>();
    private final Map<String, UserClientInfo> usernameToUserinfo = new ConcurrentHashMap<>();

    /**
     * The map of clients. We need those to determine the authorisation.
     */
    private static final Map<String, Client> CLIENTMAP = new ConcurrentHashMap<>();
    private static final Map<String, Object> SHARED_STATE = new ConcurrentHashMap<>();
    private static final Map<String, Object> OPTIONS = new HashMap<>();

    @Override
    public InitResult init(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        OPTIONS.put("keycloak-config-file", FROST_SERVER_KEYCLOAKJSON);
        final Settings authSettings = coreSettings.getAuthSettings();
        roleAdmin = authSettings.get(TAG_AUTH_ROLE_ADMIN, CoreSettings.class);
        maxClientsPerUser = authSettings.getInt(TAG_MAX_CLIENTS_PER_USER, KeycloakSettings.class);
        maxPassLength = authSettings.getInt(TAG_MAX_PASSWORD_LENGTH, KeycloakSettings.class);
        maxNameLength = authSettings.getInt(TAG_MAX_USERNAME_LENGTH, KeycloakSettings.class);
        registerUserLocally = authSettings.getBoolean(TAG_REGISTER_USER_LOCALLY, KeycloakSettings.class);
        authenticateOnly = authSettings.getBoolean(TAG_AUTHENTICATE_ONLY, CoreSettings.class);
        if (registerUserLocally) {
            DatabaseHandler.init(coreSettings);
            databaseHandler = DatabaseHandler.getInstance(coreSettings);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public void addFilter(Object context, CoreSettings coreSettings) {
        KeycloakFilterHelper.createFilters(context, coreSettings);
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

        final UserData userData = new UserData(username, maxNameLength, password, maxPassLength);

        clientMapCleanup();
        final boolean validUser = checkLogin(loginModule, userData, clientId);
        if (!validUser) {
            return false;
        }
        boolean admin = userData.roles.contains(roleAdmin);

        final PrincipalExtended userPrincipal = new PrincipalExtended(userData.userName, admin, userData.roles);
        final UserClientInfo userInfo = usernameToUserinfo.computeIfAbsent(userData.userName, t -> new UserClientInfo());
        userInfo.setUserPrincipal(userPrincipal);

        String oldClientId = userInfo.addClientId(clientId, maxClientsPerUser);
        if (oldClientId != null) {
            clientidToUserinfo.remove(oldClientId);
        }
        clientidToUserinfo.put(clientId, userInfo);

        return validUser;
    }

    private boolean checkLogin(AbstractKeycloakLoginModule loginModule, UserData userData, String clientId) {
        try {
            LOGGER.debug("Login for user {} ({})", userData.userName, clientId);
            Subject subject = new Subject();
            loginModule.initialize(
                    subject,
                    (Callback[] callbacks) -> {
                        ((NameCallback) callbacks[0]).setName(userData.userName);
                        ((PasswordCallback) callbacks[1]).setPassword(userData.userPass.toCharArray());
                    },
                    SHARED_STATE,
                    OPTIONS);
            boolean login = loginModule.login();
            if (login) {
                loginModule.commit();
                Client client = new Client(userData.userName);
                client.setLastSeen(Instant.now());
                client.setSubject(subject);
                CLIENTMAP.put(clientId, client);
                client.getSubject().getPrincipals().stream().forEach(t -> userData.roles.add(t.getName()));
                if (registerUserLocally) {
                    databaseHandler.enureUserInUsertable(userData.userName, userData.roles);
                }
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
            LOGGER.info("No user for {}", clientId);
            return false;
        }
        client.setLastSeen(Instant.now());
        if (authenticateOnly && !roleName.equalsIgnoreCase(roleAdmin)) {
            LOGGER.trace("Only authenticating, not checking if User {} has role {}", userName, roleName);
            return true;
        }
        boolean hasRole = client.getSubject().getPrincipals().stream().anyMatch(p -> p.getName().equalsIgnoreCase(roleName));
        LOGGER.trace("User {} has role {}: {}", userName, roleName, hasRole);
        return hasRole;
    }

    @Override
    public PrincipalExtended getUserPrincipal(String clientId) {
        UserClientInfo userInfo = clientidToUserinfo.get(clientId);
        if (userInfo == null) {
            return PrincipalExtended.ANONYMOUS_PRINCIPAL;
        }
        return userInfo.getUserPrincipal();
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

}
