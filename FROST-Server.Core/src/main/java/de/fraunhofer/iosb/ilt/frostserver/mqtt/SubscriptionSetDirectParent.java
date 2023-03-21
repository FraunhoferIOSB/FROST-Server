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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.Subscription;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of subscriptions that are keyed to a direct parent for faster access.
 *
 * @author scf
 */
class SubscriptionSetDirectParent {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionSetDirectParent.class.getName());

    private final MqttManager mqttManager;
    private final NavigationPropertyMain relationToParent;
    private final Map<Id, SubscriptionSet> subscriptions = new ConcurrentHashMap<>();
    private final AtomicInteger topicCount;

    public SubscriptionSetDirectParent(MqttManager mqttManager, NavigationPropertyMain relationToParent, AtomicInteger topicCount) {
        this.mqttManager = mqttManager;
        this.relationToParent = relationToParent;
        this.topicCount = topicCount;
    }

    public void handleEntityChanged(PersistenceManager persistenceManager, Entity entity, Set<Property> fields) {
        Entity parent = (Entity) entity.getProperty(relationToParent);
        if (parent == null) {
            return;
        }
        Id parentId = parent.getId();
        SubscriptionSet subsForParent = subscriptions.get(parentId);
        if (subsForParent == null) {
            return;
        }
        // for each subscription on EntityType check match
        for (Subscription subscription : subsForParent.getSubscriptions().keySet()) {
            if (subscription.matches(persistenceManager, entity, fields)) {
                mqttManager.notifySubscription(subscription, entity);
            }
        }
    }

    public boolean addSubscription(Subscription subscription) {
        synchronized (this) {
            NavigationPropertyMain parentRelation = subscription.getParentRelation();
            Id parentId = subscription.getParentId();
            if (parentRelation == null || parentId == null) {
                LOGGER.error("Parent Relation or ParentId is null! {} / {}", parentRelation, parentId);
                return false;
            }
            SubscriptionSet subsForParent = subscriptions.computeIfAbsent(parentId, t -> new SubscriptionSet(topicCount));
            subsForParent.addSubscription(subscription);
            return true;
        }
    }

    public void removeSubscription(Subscription subscription) {
        synchronized (this) {
            NavigationPropertyMain parentRelation = subscription.getParentRelation();
            Id parentId = subscription.getParentId();
            if (parentRelation == null || parentId == null) {
                LOGGER.error("Parent Relation or ParentId is null! {} / {}", parentRelation, parentId);
                return;
            }
            SubscriptionSet subsForParent = subscriptions.get(parentId);
            if (subsForParent == null) {
                return;
            }
            subsForParent.removeSubscription(subscription);
        }
    }

}
