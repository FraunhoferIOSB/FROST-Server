/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.mqtt.subscription;

import de.fraunhofer.iosb.ilt.sta.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class SubscriptionFactory {

    private static SubscriptionFactory instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(Subscription.class);
    // TODO make encoding global constant
    private static final Charset ENCODING = Charset.forName("UTF-8");

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
                ? topic.substring(0, topic.indexOf("?"))
                : topic;
        if (!pathString.startsWith("/")) {
            pathString = "/" + pathString;
        }
        return pathString;
    }

    public static String getQueryFromTopic(String topic) {
        return topic.contains("?")
                ? topic.substring(topic.indexOf("?") + 1)
                : "";
    }
    private final CoreSettings settings;

    private SubscriptionFactory(CoreSettings settings) {
        this.settings = settings;
    }

    public Subscription get(String topic) {
        final String errorMsg = "Subscription to topic '" + topic + "' is invalid. Reason: ";
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(errorMsg + "topic must be non-empty.");
        }
        if (topic.startsWith("/")) {
            throw new IllegalArgumentException(errorMsg + "topic must not start with '/'.");
        }
        String internalTopic = topic;
        String topicPrefix = settings.getMqttSettings().getTopicPrefix();
        if (topicPrefix != null && !topicPrefix.isEmpty()) {
            if (!topic.startsWith(topicPrefix)) {
                // TODO maybe just ignore subscriptio here?
                throw new IllegalArgumentException("Topic '" + topic + " does not start with expected prefix '" + topicPrefix + "'");
            }
            internalTopic = topic.substring(topicPrefix.length());
        }
        ResourcePath path = parsePath(getPathFromTopic(internalTopic));
        if (path == null || path.getPathElements().isEmpty()) {
            throw new IllegalArgumentException(errorMsg + "invalid path.");
        }
        path.setServiceRootUrl(settings.getServiceRootUrl());
        path.compress();
        if (path.getLastElement() instanceof EntitySetPathElement) {
            // SensorThings Standard 14.2.1 - Subscribe to EntitySet
            return new EntitySetSubscription(topic, path, settings.getServiceRootUrl());
        } else if (path.getLastElement() instanceof EntityPathElement) {
            // SensorThings Standard 14.2.2 - Subscribe to Entity
            return new EntitySubscription(topic, path, settings.getServiceRootUrl());
        } else if (path.getPathElements().size() >= 2
                && path.getPathElements().get(path.getPathElements().size() - 2) instanceof EntityPathElement
                && path.getPathElements().get(path.getPathElements().size() - 1) instanceof PropertyPathElement) {
            // SensorThings Standard 14.2.3 - Subscribe to Property
            return new PropertySubscription(topic, path, settings.getServiceRootUrl());

        } else {
            throw new IllegalArgumentException(errorMsg + "topic does not match any allowed pattern (RESOURCE_PATH/COLLECTION_NAME, RESOURCE_PATH_TO_AN_ENTITY, RESOURCE_PATH_TO_AN_ENTITY/PROPERTY_NAME, RESOURCE_PATH/COLLECTION_NAME?$select=PROPERTY_1,PROPERTY_2,…)");
        }

    }

    private ResourcePath parsePath(String topic) {
        ResourcePath result = null;
        try {
            String pathString = URLDecoder.decode(topic, ENCODING.name());
            result = PathParser.parsePath("", pathString);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Encoding not supported.", ex);
        } catch (NumberFormatException e) {
            LOGGER.error("Not a valid id.");
        } catch (IllegalStateException e) {
            LOGGER.error("Not a valid path: " + e.getMessage());
        }
        return result;
    }
}
