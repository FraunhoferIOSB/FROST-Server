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
package de.fraunhofer.iosb.ilt.frostserver.http.openapi;

import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAResponse;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OAPath;
import de.fraunhofer.iosb.ilt.frostserver.http.openapi.spec.OADoc;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author scf
 */
final class GeneratorContext {

    public static final String PARAM_RECURSE = "depth";
    public static final String PARAM_ADD_REF = "ref";
    public static final String PARAM_ADD_PROPS = "properties";
    public static final String PARAM_ADD_VALUE = "value";
    public static final String PARAM_ADD_EDITING = "editing";

    OADoc document;
    int recurse = 1;
    boolean addRef = false;
    boolean addEntityProperties = false;
    boolean addValue = false;
    boolean addEditing = false;
    String base = "/v1.0";
    Map<String, OAPath> pathTargets = new HashMap<>();
    Map<String, OAResponse> responseTargets = new HashMap<>();

    public GeneratorContext(HttpServletRequest request) {
        recurse = paramValueAsInt(request, PARAM_RECURSE, recurse);
        addRef = paramValueAsBool(request, PARAM_ADD_REF, addRef);
        addEntityProperties = paramValueAsBool(request, PARAM_ADD_PROPS, addEntityProperties);
        addValue = paramValueAsBool(request, PARAM_ADD_VALUE, addValue);
        addEditing = paramValueAsBool(request, PARAM_ADD_EDITING, addEditing);
    }

    public boolean paramValueAsBool(HttpServletRequest request, String name, boolean dflt) {
        String value = request.getParameter(name);
        if (value == null) {
            return dflt;
        }
        return value.equalsIgnoreCase("true");
    }

    public int paramValueAsInt(HttpServletRequest request, String name, int dflt) {
        String value = request.getParameter(name);
        if (value == null) {
            return dflt;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return dflt;
        }
    }

}
