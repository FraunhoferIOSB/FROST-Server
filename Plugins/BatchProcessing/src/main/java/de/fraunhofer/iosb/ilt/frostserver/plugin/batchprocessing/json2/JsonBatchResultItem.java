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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The result of an individual batch request item.
 */
public class JsonBatchResultItem {

    public EntityType entityType;
    public String id;
    public PkValue idValue;
    public int status;
    public Map<String, Object> headers = new HashMap<>();
    public Object body;
    /**
     * For backwards compatibility, should be in headers.
     */
    public String location;

    public Object getBody() {
        return body;
    }

    public JsonBatchResultItem setBody(Object body) {
        this.body = body;
        return this;
    }

    @JsonIgnore
    public EntityType getEntityType() {
        return entityType;
    }

    public JsonBatchResultItem setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void addHeaders(Map<String, List<String>> values) {
        values.entrySet()
                .forEach(x -> addHeaders(x.getKey(), x.getValue()));
    }

    public void addHeaders(String name, List<String> values) {
        for (String value : values) {
            addHeader(name, value);
        }
    }

    public void addHeader(String name, String value) {
        Object oldVal = headers.get(name);
        if (oldVal instanceof String s) {
            List<String> list = new ArrayList<>();
            list.add(s);
            list.add(value);
            headers.put(name, list);
        } else if (oldVal instanceof List list) {
            list.add(value);
        } else {
            headers.put(name, value);
        }
        if ("location".equalsIgnoreCase(name)) {
            location = value;
        }
    }

    public void setHeader(String name, String value) {
        headers.put(name, Arrays.asList(value));
    }

    public String getId() {
        return id;
    }

    public JsonBatchResultItem setId(String id) {
        this.id = id;
        return this;
    }

    @JsonIgnore
    public PkValue getIdValue() {
        return idValue;
    }

    public JsonBatchResultItem setIdValue(PkValue idValue) {
        this.idValue = idValue;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public JsonBatchResultItem setLocation(String location) {
        this.location = location;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public JsonBatchResultItem setStatus(int code) {
        this.status = code;
        return this;
    }

}
