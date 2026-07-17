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
package de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_MQTT_FINE_GRAINED_AUTH;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.URI_PATH_SEP;

import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.UnknownVersionException;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.UserCaches;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.Exceptions;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for turning topics into subscriptions.
 */
public class SubscriptionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionFactory.class);

    private final CoreSettings settings;
    private final ServiceContext context;
    private final UserCaches userCaches;
    private boolean fineGrainedAuth = false;

    public SubscriptionFactory(CoreSettings settings, UserCaches userCaches) {
        this.settings = settings;
        context = new ServiceContext()
                .setModelRegistry(settings.getModelRegistry())
                .setFunctionRegistry(settings.getFunctionRegistry())
                .setQueryDefaults(settings.getQueryDefaults().setAlwaysOrder(false));
        this.userCaches = userCaches;
        fineGrainedAuth = settings.getAuthSettings().getBoolean(TAG_AUTH_MQTT_FINE_GRAINED_AUTH, CoreSettings.class);
    }

    public Subscription get(String topic) {
        Exceptions.illegalArgumentIf(StringHelper.isNullOrEmpty(topic), "topic must be non-empty.");
        Exceptions.illegalArgumentIf(topic.startsWith(URI_PATH_SEP), "Invalid topic: '{}', topic must not start with {}", topic, URI_PATH_SEP);

        PrincipalExtended userPrincipal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
        if (fineGrainedAuth) {
            String[] split = StringUtils.split(topic, "/", 2);
            try {
                long userKey = Long.parseLong(split[0]);
                userPrincipal = userCaches.getUserPrincipal(userKey);
                topic = split[1];
            } catch (NumberFormatException ex) {
                LOGGER.error("Incorrect internal topic, not starting with a userkey: {}", topic);
                throw Exceptions.illegalArgument("Expected a topic starting with a number, not {}", split[0]);
            }
        }
        Version version;
        try {
            version = MqttManager.getVersionFromTopic(settings, topic);
        } catch (UnknownVersionException ex) {
            throw Exceptions.illegalArgument("Invalid topic '{}', topic must start with a version numer. Known versions :{}", topic, settings.getPluginManager().getVersions().keySet());
        }

        String internalTopic = topic.substring(version.urlPart.length() + 1);
        ResourcePath path = parsePath(
                version,
                getPathFromTopic(internalTopic));
        Exceptions.illegalArgumentIf(path == null || path.isEmpty(), "Invalid topic: '{}', invalid path.", topic);
        path.compress();
        return createSubscription(path, userPrincipal, topic);
    }

    private Subscription createSubscription(ResourcePath path, PrincipalExtended userPrincipal, String topic) {
        if (path.getLastElement() instanceof PathElementEntitySet) {
            // SensorThings Standard 14.2.1 - Subscribe to EntitySet
            if (fineGrainedAuth) {
                return new EntitySetSubscription(settings, userPrincipal, topic, path);
            } else {
                return new EntitySetSubscription(settings, topic, path);
            }
        }
        if (path.getLastElement() instanceof PathElementEntity) {
            // SensorThings Standard 14.2.2 - Subscribe to Entity
            if (fineGrainedAuth) {
                return new EntitySubscription(settings, userPrincipal, topic, path);
            } else {
                return new EntitySubscription(settings, topic, path);
            }
        }
        final int size = path.size();
        if (size >= 2
                && path.get(size - 2) instanceof PathElementEntity
                && path.get(size - 1) instanceof PathElementProperty) {
            // SensorThings Standard 14.2.3 - Subscribe to Property
            if (fineGrainedAuth) {
                return new PropertySubscription(settings, userPrincipal, topic, path);
            } else {
                return new PropertySubscription(settings, topic, path);
            }
        }
        throw Exceptions.illegalArgument("Invalid topic: '{}', topic does not match any allowed pattern (RESOURCE_PATH/COLLECTION_NAME, RESOURCE_PATH_TO_AN_ENTITY, RESOURCE_PATH_TO_AN_ENTITY/PROPERTY_NAME, RESOURCE_PATH/COLLECTION_NAME?$select=PROPERTY_1,PROPERTY_2,…)", topic);
    }

    private ResourcePath parsePath(Version version, String topic) {
        ResourcePath result = null;
        try {
            String pathString = URLDecoder.decode(topic, StringHelper.UTF8.name());
            result = PathParser.parsePath(context, version, pathString, PrincipalExtended.ANONYMOUS_PRINCIPAL);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Encoding not supported.", ex);
        } catch (NumberFormatException e) {
            LOGGER.error("Not a valid id.");
        } catch (IllegalStateException e) {
            LOGGER.error("Not a valid path: {}", e.getMessage());
        }
        return result;
    }

    public static String getPathFromTopic(String topic) {
        final int idx = topic.indexOf('?');
        String pathString = idx >= 0
                ? topic.substring(0, idx)
                : topic;
        if (!pathString.startsWith(URI_PATH_SEP)) {
            pathString = URI_PATH_SEP + pathString;
        }
        return pathString;
    }

    public static String getQueryFromTopic(String topic) {
        final int idx = topic.indexOf('?');
        return idx >= 0
                ? topic.substring(idx + 1)
                : "";
    }

}
