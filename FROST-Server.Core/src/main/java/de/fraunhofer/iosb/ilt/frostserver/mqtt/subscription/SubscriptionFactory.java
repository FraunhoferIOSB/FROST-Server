/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.UnknownVersionException;
import de.fraunhofer.iosb.ilt.frostserver.settings.Version;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class SubscriptionFactory {

    private static final String URI_PATH_SEP = "/";
    private static SubscriptionFactory instance;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionFactory.class);

    public static synchronized void init(CoreSettings settings) {
        if (instance == null) {
            instance = new SubscriptionFactory(settings);
        }
    }

    public static synchronized SubscriptionFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SubscriptionFactory is not initialized! Call init() before accessing the instance.");
        }
        return instance;
    }

    private static String getPathFromTopic(String topic) {
        String pathString = topic.contains("?")
                ? topic.substring(0, topic.indexOf('?'))
                : topic;
        if (!pathString.startsWith(URI_PATH_SEP)) {
            pathString = URI_PATH_SEP + pathString;
        }
        return pathString;
    }

    public static String getQueryFromTopic(String topic) {
        return topic.contains("?")
                ? topic.substring(topic.indexOf('?') + 1)
                : "";
    }
    private final CoreSettings settings;
    private final IdManager idManager;

    private SubscriptionFactory(CoreSettings settings) {
        this.settings = settings;
        this.idManager = PersistenceManagerFactory.getInstance().getIdManager();
    }

    public Subscription get(String topic) {
        final String errorMsg = "Subscription to topic '" + topic + "' is invalid. Reason: ";
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(errorMsg + "topic must be non-empty.");
        }
        if (topic.startsWith(URI_PATH_SEP)) {
            throw new IllegalArgumentException(errorMsg + "topic must not start with '" + URI_PATH_SEP + "'.");
        }
        Version version;
        try {
            version = MqttManager.getVersionFromTopic(topic);
        } catch (UnknownVersionException ex) {
            throw new IllegalArgumentException(errorMsg + "topic must start with a version number.");
        }
        
        String internalTopic = topic.substring(version.urlPart.length()+1);
        ResourcePath path = parsePath(getPathFromTopic(internalTopic));
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException(errorMsg + "invalid path.");
        }
        String serviceRootUrl = settings.getServiceRootUrl(version);
        path.setServiceRootUrl(serviceRootUrl);
        path.compress();
        final int size = path.size();
        if (path.getLastElement() instanceof EntitySetPathElement) {
            // SensorThings Standard 14.2.1 - Subscribe to EntitySet
            return new EntitySetSubscription(settings, topic, path, serviceRootUrl);
        } else if (path.getLastElement() instanceof EntityPathElement) {
            // SensorThings Standard 14.2.2 - Subscribe to Entity
            return new EntitySubscription(settings, topic, path, serviceRootUrl);
        } else if (size >= 2
                && path.get(size - 2) instanceof EntityPathElement
                && path.get(size - 1) instanceof PropertyPathElement) {
            // SensorThings Standard 14.2.3 - Subscribe to Property
            return new PropertySubscription(topic, path, serviceRootUrl);

        } else {
            throw new IllegalArgumentException(errorMsg + "topic does not match any allowed pattern (RESOURCE_PATH/COLLECTION_NAME, RESOURCE_PATH_TO_AN_ENTITY, RESOURCE_PATH_TO_AN_ENTITY/PROPERTY_NAME, RESOURCE_PATH/COLLECTION_NAME?$select=PROPERTY_1,PROPERTY_2,…)");
        }

    }

    private ResourcePath parsePath(String topic) {
        ResourcePath result = null;
        try {
            String pathString = URLDecoder.decode(topic, StringHelper.UTF8.name());
            result = PathParser.parsePath(idManager, "", pathString);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Encoding not supported.", ex);
        } catch (NumberFormatException e) {
            LOGGER.error("Not a valid id.");
        } catch (IllegalStateException e) {
            LOGGER.error("Not a valid path: {}", e.getMessage());
        }
        return result;
    }
}
