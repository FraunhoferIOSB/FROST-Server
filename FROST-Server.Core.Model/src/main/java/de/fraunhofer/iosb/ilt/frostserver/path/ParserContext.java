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
package de.fraunhofer.iosb.ilt.frostserver.path;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomLink;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.PropertyReference;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.HashMap;
import java.util.Map;

/**
 * The context for validating an expression tree.
 *
 * @author scf
 */
public class ParserContext {

    private final ParserContext parentContext;
    private final ModelRegistry modelRegistry;
    private final Map<String, PropertyReference> lambdaVariables = new HashMap<>();

    public ParserContext(ModelRegistry modelRegistry) {
        this.parentContext = null;
        this.modelRegistry = modelRegistry;
    }

    public ParserContext(ParserContext parentContext) {
        this.parentContext = parentContext;
        this.modelRegistry = parentContext.getModelRegistry();
    }

    public void registerVariable(String name, PropertyReference type) {
        if (getVariable(name) != null) {
            throw new IllegalArgumentException("Variable name '" + name + "' used multiple times.");
        }
        lambdaVariables.put(name, type);
    }

    public PropertyReference getVariable(String name) {
        var variable = lambdaVariables.get(name);
        if (variable == null && parentContext != null) {
            return parentContext.getVariable(name);
        }
        return variable;
    }

    public ModelRegistry getModelRegistry() {
        return modelRegistry;
    }

    public Property parseProperty(EntityType type, String propertyName, Property previous) {
        String decodedName = StringHelper.urlDecode(propertyName);
        var variable = getVariable(decodedName);
        if (variable != null) {
            return variable;
        }
        if (previous instanceof EntityPropertyMain || previous instanceof EntityPropertyCustom) {
            return parseCustomProperty(decodedName);
        }
        NavigationPropertyMain navProp = type.getNavigationProperty(decodedName);
        if (navProp != null) {
            return navProp;
        }
        EntityPropertyMain entityProp = type.getEntityProperty(decodedName);
        if (entityProp != null) {
            return entityProp;
        }
        throw new IllegalArgumentException("Could not place " + propertyName + " under type " + type + " after " + previous);
    }

    private Property parseCustomProperty(String decodedName) {
        EntityType typeForCustomLink = modelRegistry.getCustomLinksHelper().getTypeForCustomLinkName(decodedName);
        if (typeForCustomLink == null) {
            return new EntityPropertyCustom(decodedName);
        } else {
            return new EntityPropertyCustomLink(decodedName, typeForCustomLink);
        }
    }
}
