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
package de.fraunhofer.iosb.ilt.sta.mqtt;

import de.fraunhofer.iosb.ilt.sta.MqttSettings;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.Subscription;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionFactory;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.EntityChangeListener;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MqttManager implements SubscriptionListener, EntityChangeListener {

    private static MqttManager instance;
    private static final Charset ENCODING = Charset.forName("UTF-8");
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttManager.class);

    public static synchronized void init(String serviceRootUrl, MqttSettings settings) {
        if (instance == null) {
            instance = new MqttManager(serviceRootUrl, settings);
        }
    }

    public static void shutdown() {
        if (instance != null && instance.server != null) {
            instance.server.stop();
        }
    }

    public static MqttManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MqttManager is not initialized! Call init() before accessing the instance.");
        }
        return instance;
    }
    private final ConcurrentMap<EntityType, List<Subscription>> subscriptions = new ConcurrentHashMap<>();
    private final MqttSettings settings;
    private final String serviceRootUrl;

    private final MqttServer server;

    private MqttManager(String serviceRootUrl, MqttSettings settings) {
        this.serviceRootUrl = serviceRootUrl;
        this.settings = settings;
        if (settings.isEnableMqtt()) {
            this.server = MqttServerFactory.getInstance().get(settings);
            server.addSubscriptionListener(this);
            server.start();
        } else {
            server = null;
        }
        SubscriptionFactory.init(settings, serviceRootUrl);
    }

    public MqttServer getServer() {
        return server;
    }

    private void entityChanged(PersistenceManager source, Entity oldEntity, Entity newEntity) {
        // check if there is any subscription, if not do not publish at all
        if (!subscriptions.containsKey(newEntity.getEntityType())) {
            return;
        }
        // for each subscription on EntityType check match
        for (Subscription subscription : subscriptions.get(newEntity.getEntityType())) {
            if (subscription.matches(source, oldEntity, newEntity)) {
                Entity realEntity = source.getEntityById(serviceRootUrl, newEntity.getEntityType(), newEntity.getId());
                try {
                    String payload = subscription.formatMessage(realEntity);
                    server.publish(subscription.getTopic(), payload.getBytes(ENCODING), settings.getQosLevel());
                } catch (IOException ex) {
                    LOGGER.error("publishing to MQTT on topic '" + subscription.getTopic() + "' failed", ex);
                }

            }
        }
    }

    @Override
    public void onSubscribe(SubscriptionEvent e) {
        Subscription subscription = SubscriptionFactory.getInstance().get(e.getTopic());
        if (!subscriptions.containsKey(subscription.getEntityType())) {
            subscriptions.put(subscription.getEntityType(), new CopyOnWriteArrayList<>());
        }
        subscriptions.get(subscription.getEntityType()).add(subscription);
    }

    @Override
    public void onUnsubscribe(SubscriptionEvent e) {
        Subscription subscription = SubscriptionFactory.getInstance().get(e.getTopic());
        if (subscriptions.containsKey(subscription.getEntityType())) {
            subscriptions.get(subscription.getEntityType()).remove(subscription);
        }
    }

    @Override
    public void entityInserted(PersistenceManager source, Entity entity) {
        entityChanged(source, null, entity);
    }

    @Override
    public void entityDeleted(PersistenceManager source, Entity entity) {
        //entityChanged(source, null, entity);
    }

    @Override
    public void entityUpdated(PersistenceManager source, Entity oldEntity, Entity newEntity) {
        entityChanged(source, oldEntity, newEntity);
    }

    public String getServiceRootUrl() {
        return serviceRootUrl;
    }

    public MqttSettings getSettings() {
        return settings;
    }
}
