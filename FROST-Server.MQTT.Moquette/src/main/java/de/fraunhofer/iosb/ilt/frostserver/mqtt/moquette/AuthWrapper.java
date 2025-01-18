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
package de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette;

import static de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette.MoquetteMqttServer.TAG_MQTT_TOPIC_ALLOWLIST;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.frostserver.util.StringHelper.isNullOrEmpty;

import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionFactory;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.UnknownVersionException;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthProvider;
import de.fraunhofer.iosb.ilt.frostserver.util.AuthUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.broker.subscriptions.Topic;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the Moquette Auth classes and forwards the auth requests to the FROST
 * AuthProvider.
 */
public class AuthWrapper implements IAuthenticator, IAuthorizatorPolicy {

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
        public InitResult init(CoreSettings coreSettings) {
            // This dummy needs no initialisation.
            return InitResult.INIT_OK;
        }

        @Override
        public String checkForUpgrades() {
            return "Something went wrong while configuring your auth provider. You have a configuration error.";
        }

        @Override
        public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
            return false;
        }

        @Override
        public PrincipalExtended getUserPrincipal(String clientId) {
            return PrincipalExtended.ANONYMOUS_PRINCIPAL;
        }

    };

    private final CoreSettings coreSettings;
    private final AuthProvider authProvider;
    private final boolean anonymousRead;
    private final String roleAdmin;
    private final String roleCeate;
    private final String roleRead;
    private final String frostClientId;
    private final Pattern topicAllowPattern;
    private PersistenceManager persistenceManager;

    public AuthWrapper(CoreSettings coreSettings, String authProviderClassName, String frostClientId) {
        LOGGER.info("Initialising authentication.");
        this.coreSettings = coreSettings;
        this.frostClientId = frostClientId;
        Settings authSettings = coreSettings.getAuthSettings();
        anonymousRead = authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class);
        String topicAllowListRegex = authSettings.get(TAG_MQTT_TOPIC_ALLOWLIST, MoquetteMqttServer.class);

        if (isNullOrEmpty(topicAllowListRegex)) {
            topicAllowPattern = null;
        } else {
            topicAllowPattern = Pattern.compile(topicAllowListRegex);
            PersistenceManagerFactory.init(coreSettings);
        }

        Map<AuthUtils.Role, String> roleMapping = AuthUtils.loadRoleMapping(authSettings);
        roleAdmin = roleMapping.get(AuthUtils.Role.ADMIN);
        roleCeate = roleMapping.get(AuthUtils.Role.CREATE);
        roleRead = roleMapping.get(AuthUtils.Role.READ);

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
            LOGGER.error("FROST-ClientID Used in checkValid");
            return true;
        }
        return authProvider.isValidUser(clientId, username, new String(password, StringHelper.UTF8));
    }

    @Override
    public boolean canWrite(Topic topic, String user, String clientId) {
        if (frostClientId.equalsIgnoreCase(clientId)) {
            LOGGER.error("FROST-ClientID Used in write");
            return true;
        }
        return authProvider.userHasRole(clientId, user, roleCeate);
    }

    @Override
    public boolean canRead(Topic mqttTopic, String user, String clientId) {
        String topic = mqttTopic.toString();
        if (frostClientId.equalsIgnoreCase(clientId)) {
            LOGGER.error("FROST-ClientID Used in read");
            return true;
        }
        if (StringUtils.containsAny(topic, '#', '+')) {
            LOGGER.debug("Denied access to {}, wildcards not allowed.", topic);
            return false;
        }
        PrincipalExtended userPrincipal = authProvider.getUserPrincipal(clientId);
        if (user != null && !user.equals(userPrincipal.getName())) {
            LOGGER.warn("Username {} does not match name in Principal: {}", user, userPrincipal);
            return false;
        }
        if (user == null && userPrincipal != PrincipalExtended.ANONYMOUS_PRINCIPAL) {
            LOGGER.warn("Username is null, but Principal is: {}", userPrincipal);
            return false;
        }
        if (userPrincipal.hasRole(roleRead) || userPrincipal.hasRole(roleAdmin)) {
            LOGGER.debug("Allowing access to {}, user {} has role 'read' or 'admin'.", topic, user);
            return true;
        }
        if (topicAllowPattern != null) {
            Version version;
            try {
                version = MqttManager.getVersionFromTopic(coreSettings, topic);
            } catch (UnknownVersionException ex) {
                LOGGER.debug("Denied access to {}, unknown version.", topic);
                return false;
            }
            String internalTopic = topic.substring(version.urlPart.length() + 1);
            internalTopic = SubscriptionFactory.getPathFromTopic(internalTopic);

            if (!topicAllowPattern.matcher(internalTopic).matches()) {
                LOGGER.debug("Denied access to {}, not matching allow pattern.", internalTopic);
                return false;
            }

            return validatePath(version, internalTopic, userPrincipal);
        }

        return anonymousRead;
    }

    private boolean validatePath(Version version, String topic, PrincipalExtended userPrincipal) {
        PrincipalExtended lp = PrincipalExtended.getLocalPrincipal();
        try {
            String internalTopic = URLDecoder.decode(topic, StringHelper.UTF8.name());
            ResourcePath path = PathParser.parsePath(
                    coreSettings.getModelRegistry(),
                    coreSettings.getQueryDefaults().getServiceRootUrl(),
                    version,
                    internalTopic,
                    userPrincipal);
            PrincipalExtended.setLocalPrincipal(userPrincipal);
            boolean validPath = getPm().validatePath(path);
            if (validPath) {
                LOGGER.debug("Allowing access for user {} to {}.", topic, userPrincipal);
            } else {
                LOGGER.debug(" Denying access for user {} to {}.", topic, userPrincipal);
            }
            return validPath;
        } catch (RuntimeException | UnsupportedEncodingException ex) {
            LOGGER.warn("Exception trying to validate access for user {} to topic {}.", userPrincipal, topic, ex);
            return false;
        } finally {
            PrincipalExtended.setLocalPrincipal(lp);
        }
    }

    public PersistenceManager getPm() {
        if (persistenceManager == null) {
            persistenceManager = PersistenceManagerFactory.getInstance(coreSettings).create();
        }
        return persistenceManager;
    }

    public PrincipalExtended getUserPrincipal(String clientId) {
        final PrincipalExtended userPrincipal = authProvider.getUserPrincipal(clientId);
        LOGGER.debug("User principal for {} is {}", clientId, userPrincipal);
        return userPrincipal;
    }
}
