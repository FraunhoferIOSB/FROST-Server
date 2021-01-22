/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.mqtt;

import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.Subscription;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of subscriptions, each with a client count. Adding a duplicate of a
 * subscription will only increase the count for that subscription, not actually
 * add a copy.
 *
 * @author scf
 */
class SubscriptionSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionSet.class.getName());

    private final Map<Subscription, AtomicInteger> subscriptions = new ConcurrentHashMap<>();
    private final AtomicInteger topicCount;

    public SubscriptionSet(AtomicInteger topicCount) {
        this.topicCount = topicCount;
    }

    public Map<Subscription, AtomicInteger> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(Subscription subscription) {
        AtomicInteger clientCount = subscriptions.get(subscription);
        if (clientCount == null) {
            clientCount = new AtomicInteger(1);
            subscriptions.put(subscription, clientCount);
            topicCount.incrementAndGet();
            LOGGER.debug("Created new subscription for topic {}.", subscription.getTopic());
        } else {
            int newCount = clientCount.incrementAndGet();
            LOGGER.debug("Now {} subscriptions for topic {}.", newCount, subscription.getTopic());
        }
    }

    public void removeSubscription(Subscription subscription) {
        AtomicInteger clientCount = subscriptions.get(subscription);
        if (clientCount == null) {
            return;
        }
        int newCount = clientCount.decrementAndGet();
        LOGGER.debug("Now {} subscriptions for topic {}.", newCount, subscription.getTopic());
        if (newCount <= 0) {
            subscriptions.remove(subscription);
            topicCount.decrementAndGet();
            LOGGER.debug("Removed last subscription for topic {}.", subscription.getTopic());
        }
    }

}
