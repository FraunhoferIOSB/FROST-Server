/*
 * Copyright 2016 Open Geospatial Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.statests.util.mqtt;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author jab
 *
 * @param <T> The type of the result.
 */
public class MqttBatchResult<T> {

    private T actionResult;
    private Map<String, JSONObject> messages;

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

    public MqttBatchResult withMessages(Map<String, JSONObject> messages) {
        setMessages(messages);
        return this;
    }

    public T getActionResult() {
        return actionResult;
    }

    public Map<String, JSONObject> getMessages() {
        return messages;
    }

    public void setActionResult(T actionResult) {
        this.actionResult = actionResult;
    }

    public void setMessages(Map<String, JSONObject> messages) {
        this.messages = messages;
    }

    public void addMessage(String topic, JSONObject message) {
        messages.put(topic, message);
    }

}
