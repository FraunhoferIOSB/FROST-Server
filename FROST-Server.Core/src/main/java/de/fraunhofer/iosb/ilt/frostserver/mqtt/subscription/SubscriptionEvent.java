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

/**
 * Event that a client subscribed.
 */
public class SubscriptionEvent {

    private String clientId;
    private String topic;

    public SubscriptionEvent() {
    }

    public SubscriptionEvent(String clientId, String topic) {
        this.clientId = clientId;
        this.topic = topic;
    }

    public String getClientId() {
        return clientId;
    }

    public SubscriptionEvent setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public SubscriptionEvent setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public String toString() {
        return getTopic();
    }

}
