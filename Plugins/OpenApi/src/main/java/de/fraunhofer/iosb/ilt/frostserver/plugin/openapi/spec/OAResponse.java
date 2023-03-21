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
package de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.TreeMap;

/**
 * An OpenAPI response object.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OAResponse {

    @JsonProperty(value = "$ref")
    private String ref;
    private String description;
    private Map<String, OAHeader> headers;
    private Map<String, OAMediaType> content;

    public void addHeader(String name, OAHeader header) {
        if (headers == null) {
            headers = new TreeMap<>();
        }
        headers.put(name, header);
    }

    public boolean hasHeader(String name) {
        if (headers == null) {
            return false;
        }
        return headers.containsKey(name);
    }

    public OAHeader getHeader(String name) {
        if (headers == null) {
            return null;
        }
        return headers.get(name);
    }

    public void addContent(String name, OAMediaType item) {
        if (content == null) {
            content = new TreeMap<>();
        }
        content.put(name, item);
    }

    public boolean hasContent(String name) {
        if (content == null) {
            return false;
        }
        return content.containsKey(name);
    }

    public OAMediaType getContent(String name) {
        if (content == null) {
            return null;
        }
        return content.get(name);
    }

    /**
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the headers
     */
    public Map<String, OAHeader> getHeaders() {
        return headers;
    }

    /**
     * @param headers the headers to set
     */
    public void setHeaders(Map<String, OAHeader> headers) {
        this.headers = headers;
    }

    /**
     * @return the content
     */
    public Map<String, OAMediaType> getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(Map<String, OAMediaType> content) {
        this.content = content;
    }

}
