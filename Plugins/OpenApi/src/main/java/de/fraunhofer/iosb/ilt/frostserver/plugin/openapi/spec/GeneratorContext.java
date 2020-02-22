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

import de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.ServiceOpenApi;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.Version;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author scf
 */
public final class GeneratorContext {

    public static final String PARAM_RECURSE = "depth";
    public static final String PARAM_ADD_REF = "ref";
    public static final String PARAM_ADD_PROPS = "properties";
    public static final String PARAM_ADD_VALUE = "value";
    public static final String PARAM_ADD_EDITING = "editing";

    private OADoc document;
    private int recurse = 1;
    private boolean addRef = false;
    private boolean addEntityProperties = false;
    private boolean addValue = false;
    private boolean addEditing = false;
    private Version version = Version.V_1_0;
    private String base = "/v1.0";

    private final Map<String, OAPath> pathTargets = new HashMap<>();
    private final Map<String, OAResponse> responseTargets = new HashMap<>();

    public GeneratorContext initFromRequest(ServiceRequest request) {
        recurse = ServiceOpenApi.paramValueAsInt(request, PARAM_RECURSE, recurse);
        addRef = ServiceOpenApi.paramValueAsBool(request, PARAM_ADD_REF, addRef);
        addEntityProperties = ServiceOpenApi.paramValueAsBool(request, PARAM_ADD_PROPS, addEntityProperties);
        addValue = ServiceOpenApi.paramValueAsBool(request, PARAM_ADD_VALUE, addValue);
        addEditing = ServiceOpenApi.paramValueAsBool(request, PARAM_ADD_EDITING, addEditing);
        version = request.getVersion();
        base = "/" + version.urlPart;
        return this;
    }

    public boolean isAddEditing() {
        return addEditing;
    }

    public GeneratorContext setAddEditing(boolean addEditing) {
        this.addEditing = addEditing;
        return this;
    }

    public boolean isAddEntityProperties() {
        return addEntityProperties;
    }

    public GeneratorContext setAddEntityProperties(boolean addEntityProperties) {
        this.addEntityProperties = addEntityProperties;
        return this;
    }

    public boolean isAddRef() {
        return addRef;
    }

    public GeneratorContext setAddRef(boolean addRef) {
        this.addRef = addRef;
        return this;

    }

    public boolean isAddValue() {
        return addValue;
    }

    public GeneratorContext setAddValue(boolean addValue) {
        this.addValue = addValue;
        return this;
    }

    public String getBase() {
        return base;
    }

    public GeneratorContext setBase(String base) {
        this.base = base;
        return this;
    }

    public OADoc getDocument() {
        return document;
    }

    public GeneratorContext setDocument(OADoc document) {
        this.document = document;
        return this;
    }

    public Map<String, OAPath> getPathTargets() {
        return pathTargets;
    }

    public int getRecurse() {
        return recurse;
    }

    public GeneratorContext setRecurse(int recurse) {
        this.recurse = recurse;
        return this;
    }

    public Map<String, OAResponse> getResponseTargets() {
        return responseTargets;
    }

    public Version getVersion() {
        return version;
    }

    public GeneratorContext setVersion(Version version) {
        this.version = version;
        return this;
    }

}
