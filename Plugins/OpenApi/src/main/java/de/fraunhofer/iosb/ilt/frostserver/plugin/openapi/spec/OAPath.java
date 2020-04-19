/*
 * Copyright (C) 2019 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import java.util.ArrayList;
import java.util.List;

/**
 * An OpenAPI path object.
 *
 * @author scf
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class OAPath {

    @JsonProperty(value = "$ref")
    private String ref;
    private List<OAParameter> parameters;
    private OAOperation get;
    private OAOperation put;
    private OAOperation post;
    private OAOperation patch;
    private OAOperation delete;
    private OAOperation options;
    private OAOperation head;

    public OAPath addParameter(OAParameter parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        parameters.add(parameter);
        return this;
    }

    /**
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     * @return this
     */
    public OAPath setRef(String ref) {
        this.ref = ref;
        return this;
    }

    /**
     * @return the parameters
     */
    public List<OAParameter> getParameters() {
        return parameters;
    }

    /**
     * @return the get
     */
    public OAOperation getGet() {
        return get;
    }

    /**
     * @param get the get to set
     * @return this
     */
    public OAPath setGet(OAOperation get) {
        this.get = get;
        return this;
    }

    /**
     * @return the put
     */
    public OAOperation getPut() {
        return put;
    }

    /**
     * @param put the put to set
     * @return this
     */
    public OAPath setPut(OAOperation put) {
        this.put = put;
        return this;
    }

    /**
     * @return the post
     */
    public OAOperation getPost() {
        return post;
    }

    /**
     * @param post the post to set
     * @return this
     */
    public OAPath setPost(OAOperation post) {
        this.post = post;
        return this;
    }

    /**
     * @return the patch
     */
    public OAOperation getPatch() {
        return patch;
    }

    /**
     * @param patch the patch to set
     * @return this
     */
    public OAPath setPatch(OAOperation patch) {
        this.patch = patch;
        return this;
    }

    /**
     * @return the delete
     */
    public OAOperation getDelete() {
        return delete;
    }

    /**
     * @param delete the delete to set
     * @return this
     */
    public OAPath setDelete(OAOperation delete) {
        this.delete = delete;
        return this;
    }

    /**
     * @return the options
     */
    public OAOperation getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     * @return this
     */
    public OAPath setOptions(OAOperation options) {
        this.options = options;
        return this;
    }

    /**
     * @return the head
     */
    public OAOperation getHead() {
        return head;
    }

    /**
     * @param head the head to set
     * @return this
     */
    public OAPath setHead(OAOperation head) {
        this.head = head;
        return this;
    }

}
