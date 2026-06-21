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
package de.fraunhofer.iosb.ilt.frostserver.mqtt.create;

import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Event that an entity should be created.
 */
public class RequestEvent {

    private final String topic;
    private final String payload;
    private final PrincipalExtended principal;
    private final Map<String, String> userProperties = new LinkedHashMap<>();
    private String contentType = Constants.CONTENT_TYPE_APPLICATION_JSON;
    private String responseTopic;
    private byte[] correlationData;

    public RequestEvent(String topic, String payload) {
        this(topic, payload, PrincipalExtended.ANONYMOUS_PRINCIPAL);
    }

    public RequestEvent(String topic, String payload, PrincipalExtended principal) {
        this.topic = topic;
        this.payload = payload;
        this.principal = principal;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }

    public PrincipalExtended getPrincipal() {
        return principal;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(byte[] correlationData) {
        this.correlationData = correlationData;
    }

    public String getResponseTopic() {
        return responseTopic;
    }

    public void setResponseTopic(String responseTopic) {
        this.responseTopic = responseTopic;
    }

    public String getUserProperty(String name) {
        return userProperties.get(name);
    }

    public Map<String, String> getUserProperties() {
        return userProperties;
    }

    public void addUserProperty(String name, String value) {
        userProperties.put(name, value);
    }

}
