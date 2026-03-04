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
import static de.fraunhofer.iosb.ilt.frostserver.auth.oidc.OidcSettings.TAG_REGISTER_USER_LOCALLY;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTHENTICATE_ONLY;

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
import de.fraunhofer.iosb.ilt.frostserver.util.user.UserData;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OidcAuthProvider implements AuthProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthProvider.class);

    private static final int CUTOFF_HOURS = 24;

    private CoreSettings coreSettings;
    private int maxClientsPerUser;
    private boolean registerUserLocally;
    private boolean authenticateOnly;
    private DatabaseHandler databaseHandler;
    private OidcConfiguration config;

    private final Map<String, UserClientInfo> clientidToUserinfo = new ConcurrentHashMap<>();
    private final Map<String, UserClientInfo> usernameToUserinfo = new ConcurrentHashMap<>();

    /**
     * The map of clients. We need those to determine the authorisation.
     */
    private static final Map<String, Client> CLIENTMAP = new ConcurrentHashMap<>();

    @Override
    public InitResult init(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        final Settings authSettings = coreSettings.getAuthSettings();
        maxClientsPerUser = authSettings.getInt(TAG_MAX_CLIENTS_PER_USER, OidcSettings.class);
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
        config = Utils.createConfiguration(coreSettings);
        return InitResult.INIT_OK;
    }

    @Override
    public void addFilter(Object context, CoreSettings coreSettings) {
        OidcFilterHelper.createFilters(context, coreSettings);
    }

    @Override
    public boolean isValidUser(String clientId, String username, String password) {
        clientMapCleanup();
        PrincipalExtended pe;
        if (password.length() > 50) {
            pe = Utils.checkLogin(config, password);
            if (!Strings.CS.equals(username, pe.getName())) {
                LOGGER.info("Username {} does not match sub {}", username, pe.getName());
                return false;
            }
        } else {
            pe = Utils.checkLogin(config, username, password);
        }
        if (pe == null) {
            return false;
        }
        if (registerUserLocally) {
            databaseHandler.enureUserInUsertable(pe.getName(), pe.getRoles());
        }

        final UserClientInfo userInfo = usernameToUserinfo.computeIfAbsent(pe.getName(), t -> new UserClientInfo());
        userInfo.setUserPrincipal(pe);
        String oldClientId = userInfo.addClientId(clientId, maxClientsPerUser);
        if (oldClientId != null) {
            clientidToUserinfo.remove(oldClientId);
        }
        clientidToUserinfo.put(clientId, userInfo);

        Client client = new Client(new UserData(pe.getName(), null));
        client.setLastSeen(Instant.now());
        CLIENTMAP.put(clientId, client);

        return false;
    }

    @Override
    public boolean userHasRole(String clientId, String userName, String roleName) {
        Client client = CLIENTMAP.get(clientId);
        if (client == null) {
            LOGGER.info("No user for {}", clientId);
            return false;
        }
        final UserData userData = client.getUserData();
        if (!Strings.CS.equals(userName, userData.getUserName())) {
            LOGGER.warn("ClientId {} belongs to user {}, not user {}", clientId, userData.getUserName(), userName);
            return false;
        }
        client.setLastSeen(Instant.now());
        if (authenticateOnly && !roleName.equalsIgnoreCase(config.getRoleAdmin())) {
            LOGGER.trace("Only authenticating, not checking if User {} has role {}", userName, roleName);
            return true;
        }
        boolean hasRole = userData.getRoles().contains(roleName);
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

        private final UserData userData;
        private Instant lastSeen;

        public Client(UserData userData) {
            this.userData = userData;
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

        public UserData getUserData() {
            return userData;
        }
    }

}
