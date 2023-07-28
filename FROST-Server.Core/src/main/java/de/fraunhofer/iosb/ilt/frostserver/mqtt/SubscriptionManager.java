/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.mqtt;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.Subscription;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A manger for subscriptions for a single entity type.
 *
 * @author scf
 */
class SubscriptionManager {

    /**
     * The main entity type of the subscriptions in this manager.
     */
    private final EntityType entityType;
    /**
     * Subscriptions that are keyed to a fixed parent with an ID that is always
     * supplied directly in the changeEvent. For example:
     * /Datastreams(1)/Observations
     */
    private final Map<NavigationPropertyMain, SubscriptionSetDirectParent> parentedSubscriptions = new HashMap<>();
    /**
     * All other subscriptions.
     */
    private final SubscriptionSet complexSubscriptions;
    private final MqttManager mqttManager;
    private final AtomicInteger topicCount;

    public SubscriptionManager(EntityType entityType, MqttManager mqttManager, AtomicInteger topicCount) {
        this.entityType = entityType;
        this.mqttManager = mqttManager;
        this.topicCount = topicCount;
        complexSubscriptions = new SubscriptionSet(topicCount);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void handleEntityChanged(PersistenceManager persistenceManager, Entity entity, Set<Property> fields) {
        for (SubscriptionSetDirectParent subSet : parentedSubscriptions.values()) {
            subSet.handleEntityChanged(persistenceManager, entity, fields);
        }
        for (Subscription subscription : complexSubscriptions.getSubscriptions().keySet()) {
            if (subscription.matches(persistenceManager, entity, fields)) {
                mqttManager.notifySubscription(subscription, entity);
            }
        }
    }

    public synchronized void addSubscription(Subscription subscription) {
        NavigationPropertyMain parentRelation = subscription.getParentRelation();
        if (parentRelation != null) {
            SubscriptionSetDirectParent parentSet = parentedSubscriptions.computeIfAbsent(parentRelation, t -> new SubscriptionSetDirectParent(mqttManager, parentRelation, topicCount));
            if (parentSet.addSubscription(subscription)) {
                return;
            }
        }
        complexSubscriptions.addSubscription(subscription);
    }

    public synchronized void removeSubscription(Subscription subscription) {
        NavigationPropertyMain parentRelation = subscription.getParentRelation();
        if (parentRelation != null) {
            SubscriptionSetDirectParent parentSet = parentedSubscriptions.get(parentRelation);
            parentSet.removeSubscription(subscription);
        }
        complexSubscriptions.removeSubscription(subscription);
    }

}
