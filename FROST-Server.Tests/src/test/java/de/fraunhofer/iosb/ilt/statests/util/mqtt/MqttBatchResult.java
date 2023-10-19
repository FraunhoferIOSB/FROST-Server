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
package de.fraunhofer.iosb.ilt.statests.util.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jab
 *
 * @param <T> The type of the result.
 */
public class MqttBatchResult<T> {

    private T actionResult;
    private Map<String, JsonNode> messages;

    public MqttBatchResult() {
        messages = new HashMap<>();
    }

    public MqttBatchResult(int size) {
        messages = new HashMap<>(size);
    }

    public MqttBatchResult withActionResult(T actionResult) {
        setActionResult(actionResult);
        return this;
    }

    public MqttBatchResult withMessages(Map<String, JsonNode> messages) {
        setMessages(messages);
        return this;
    }

    public T getActionResult() {
        return actionResult;
    }

    public Map<String, JsonNode> getMessages() {
        return messages;
    }

    public void setActionResult(T actionResult) {
        this.actionResult = actionResult;
    }

    public void setMessages(Map<String, JsonNode> messages) {
        this.messages = messages;
    }

    public void addMessage(String topic, JsonNode message) {
        messages.put(topic, message);
    }

}
