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

import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_USER_CACHE_CLEANUP_INTERVAL;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_USER_CACHE_LIFETIME;
import static de.fraunhofer.iosb.ilt.frostserver.auth.keycloak.KeycloakSettings.TAG_USER_ROLE_DECODER_CLASS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.TAG_DB_URL;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ConnectionUtils.ConnectionWrapper;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database handler for the keycloak auth provider.
 */
public class DatabaseHandler {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);

    private static final Map<CoreSettings, DatabaseHandler> INSTANCES = new HashMap<>();

    private final Settings authSettings;
    private final String connectionUrl;
    private UserRoleDecoder userRoleDecoder;
    private final Duration lifetime;
    private final long cleanupIntervalMs;
    private Thread cleanupThread;

    private LinkedHashMap<String, SeenUser> seenUsers = new LinkedHashMap<>();

    public static void init(CoreSettings coreSettings) {
        if (INSTANCES.get(coreSettings) == null) {
            createInstance(coreSettings);
        }
    }

    private static synchronized DatabaseHandler createInstance(CoreSettings coreSettings) {
        return INSTANCES.computeIfAbsent(coreSettings, s -> {
            LOGGER.info("Initialising DatabaseHandler.");
            return new DatabaseHandler(coreSettings);
        });
    }

    public static DatabaseHandler getInstance(CoreSettings coreSettings) {
        DatabaseHandler instance = INSTANCES.get(coreSettings);
        if (instance == null) {
            LOGGER.error("DatabaseHandler not initialised.");
        }
        return instance;
    }

    private DatabaseHandler(CoreSettings coreSettings) {
        authSettings = coreSettings.getAuthSettings();
        connectionUrl = authSettings.get(TAG_DB_URL, ConnectionUtils.class);
        String userRoleDecoderClass = authSettings.get(TAG_USER_ROLE_DECODER_CLASS, KeycloakSettings.class);
        String lifeTimeString = authSettings.get(TAG_USER_CACHE_LIFETIME, KeycloakSettings.class);
        lifetime = Duration.parse(lifeTimeString);
        try {
            Class<?> urdClass = Class.forName(userRoleDecoderClass);
            userRoleDecoder = (UserRoleDecoder) urdClass.getDeclaredConstructor().newInstance();
            userRoleDecoder.init(coreSettings);
        } catch (ReflectiveOperationException ex) {
            LOGGER.error("Could not create UserRoleDecoder: Class '{}' could not be instantiated", userRoleDecoderClass, ex);
        }
        String cleanupIntervalString = authSettings.get(TAG_USER_CACHE_CLEANUP_INTERVAL, KeycloakSettings.class);
        cleanupIntervalMs = Duration.parse(cleanupIntervalString).toMillis();
    }

    /**
     * Checks if the user is registered locally and if not, add the user.
     *
     * @param username the username
     * @param roles the roles the user has.
     */
    public void enureUserInUsertable(String username, Set<String> roles) {
        startCleanupThread();
        SeenUser user = seenUsers.get(username);
        Instant now = Instant.now();
        if (user != null && user.expire.isAfter(now)) {
            LOGGER.debug("Already seen user {}", username);
            return;
        }
        if (user != null) {
            LOGGER.debug("User {} timed out", username);
            seenUsers.remove(username);
        }

        LOGGER.info("Decoding roles for user {}", username);
        try (final ConnectionWrapper connectionProvider = new ConnectionWrapper(authSettings, connectionUrl)) {
            final DSLContext dslContext = DSL.using(connectionProvider.get(), SQLDialect.POSTGRES);
            userRoleDecoder.decodeUserRoles(username, roles, dslContext);
            seenUsers.put(username, new SeenUser(username, lifetime));
        } catch (SQLException | RuntimeException exc) {
            LOGGER.error("Failed to register user locally.", exc);
        }
    }

    private void startCleanupThread() {
        if (cleanupThread != null) {
            return;
        }
        synchronized (this) {
            if (cleanupThread != null) {
                return;
            }
            cleanupThread = new Thread(this::cleanUserCacheLoop, "userCacheCleaner");
            cleanupThread.setDaemon(true);
            cleanupThread.start();
        }
    }

    private void cleanUserCacheLoop() {
        while (!Thread.interrupted()) {
            try {
                cleanUserCache();
            } catch (RuntimeException ex) {
                LOGGER.trace("Exception during cleanup.", ex);
            }
            cleanupSleep();
        }
    }

    private void cleanUserCache() {
        Instant now = Instant.now();
        for (Iterator<SeenUser> it = seenUsers.values().iterator(); it.hasNext();) {
            SeenUser user = it.next();
            if (user.expire.isBefore(now)) {
                LOGGER.debug("User {} timed out", user.username);
                it.remove();
            } else {
                // The rest must also be still valid, since the are in insertion order.
                return;
            }
        }
    }

    private void cleanupSleep() {
        try {
            Thread.sleep(cleanupIntervalMs);
        } catch (InterruptedException ex) {
            LOGGER.trace("Rude Wakeup.", ex);
            Thread.currentThread().interrupt();
        }
    }

    private static class SeenUser {

        final Instant expire;
        final String username;

        public SeenUser(String username, TemporalAmount lifetime) {
            this.expire = Instant.now().plus(lifetime);
            this.username = username;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.username);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SeenUser other = (SeenUser) obj;
            return Objects.equals(this.username, other.username);
        }

    }

}
