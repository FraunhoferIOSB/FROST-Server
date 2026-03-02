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
package de.fraunhofer.iosb.ilt.frostserver.auth.oidc;

import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_MAX_CLIENTS_PER_USER;
import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_MAX_PASSWORD_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_MAX_USERNAME_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_REGISTER_USER_LOCALLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTHENTICATE_ONLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ROLE_ADMIN;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_PASSWORD_LENGTH;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.UserData.MAX_USERNAME_LENGTH;

import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthProvider;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserClientInfo;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OidcAuthProvider implements AuthProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthProvider.class);

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
        maxClientsPerUser = authSettings.getInt(TAG_MAX_CLIENTS_PER_USER, OidcSettings.class);
        maxPassLength = authSettings.getInt(TAG_MAX_PASSWORD_LENGTH, OidcSettings.class);
        maxNameLength = authSettings.getInt(TAG_MAX_USERNAME_LENGTH, OidcSettings.class);
        registerUserLocally = authSettings.getBoolean(TAG_REGISTER_USER_LOCALLY, OidcSettings.class);
        authenticateOnly = authSettings.getBoolean(TAG_AUTHENTICATE_ONLY, CoreSettings.class);
        if (authenticateOnly) {
            PersistenceManagerFactory pm = PersistenceManagerFactory.getInstance(coreSettings);
            if (pm instanceof JooqPersistenceManager jpm) {
                // Ensure security validators are initialised even if no specific
                // security validators are defined.
                jpm.getTableCollection().addSecurityValidator("", null);
            }
        }
        if (registerUserLocally) {
            DatabaseHandler.init(coreSettings);
            databaseHandler = DatabaseHandler.getInstance(coreSettings);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public void addFilter(Object context, CoreSettings coreSettings) {
        OidcFilterHelper.createFilters(context, coreSettings);
    }

    @Override
    public boolean isValidUser(String clientId, String username, String password) {
        return false;
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
    public Map<String, Object> createLiqibaseParams(PersistenceManager pm, Map<String, Object> target) {
        return target;
    }

    @Override
    public String checkForUpgrades(Map<String, Object> liquibaseParams) {
        return "";
    }

    @Override
    public boolean doUpgrades(Writer out, Map<String, Object> liquibaseParams) throws UpgradeFailedException, IOException {
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
