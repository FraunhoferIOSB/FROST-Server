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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.ContentIdPair;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * A single request from a JSON Batch request.
 */
public class JsonBatchRequestItem {

    private String atomicityGroup;
    private String body;
    private Map<String, List<String>> headers;
    private String id;
    private String method;
    private String url;
    private String ifCondition;

    public String getAtomicityGroup() {
        return atomicityGroup;
    }

    public JsonBatchRequestItem setAtomicityGroup(String atomicityGroup) {
        this.atomicityGroup = atomicityGroup;
        return this;
    }

    public String getBody() {
        return body;
    }

    @JsonDeserialize(using = BodyDeserializer.class)
    public JsonBatchRequestItem setBody(String body) {
        this.body = body;
        return this;
    }

    public String getContentType() {
        List<String> contentType = getHeaders().get(Constants.CONTENT_TYPE);
        if (StringHelper.isNullOrEmpty(contentType)) {
            return Constants.CONTENT_TYPE_APPLICATION_JSON;
        }
        return contentType.get(0);
    }

    public Map<String, List<String>> getHeaders() {
        if (headers == null) {
            headers = new HashMap<>();
        }
        return headers;
    }

    @JsonDeserialize(using = HeadersDeserializer.class)
    public JsonBatchRequestItem setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public String getId() {
        return id;
    }

    public JsonBatchRequestItem setId(String id) {
        this.id = id;
        return this;
    }

    public String getIfCondition() {
        return ifCondition;
    }

    @JsonProperty(value = "if")
    public void setIfCondition(String ifCondition) {
        this.ifCondition = ifCondition;
    }

    public boolean matchesIfCondition(Map<String, ContentIdPair> ids) {
        if (StringHelper.isNullOrEmpty(ifCondition)) {
            return true;
        }
        String myIfCondition = ifCondition.trim();
        boolean not = myIfCondition.startsWith("not ");
        myIfCondition = StringUtils.removeStart(myIfCondition, "not ").trim();
        boolean found = ids.containsKey(StringUtils.removeStart(myIfCondition, "$"));
        if (not) {
            return !found;
        }
        return found;

    }

    public String getMethod() {
        return method;
    }

    public JsonBatchRequestItem setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public JsonBatchRequestItem setUrl(String url) {
        this.url = url;
        return this;
    }

    public static class BodyDeserializer extends ValueDeserializer<String> {

        @Override
        public String deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
            TreeNode tree = jp.readValueAsTree();
            return SimpleJsonMapper.getSimpleObjectMapper().writeValueAsString(tree);
        }
    }

    public static class HeadersDeserializer extends ValueDeserializer<Map<String, List<String>>> {

        @Override
        public Map<String, List<String>> deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
            JsonNode tree = jp.readValueAsTree();
            Map<String, List<String>> result = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = tree.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                final String name = entry.getKey().toLowerCase();
                final JsonNode value = entry.getValue();
                if (value.isValueNode()) {
                    result.put(name, Arrays.asList(value.asString()));
                } else {
                    List<String> list = new ArrayList<>();
                    for (JsonNode v : value) {
                        list.add(v.asString());
                    }
                    result.put(name, list);
                }
            }
            return result;
        }
    }

}
