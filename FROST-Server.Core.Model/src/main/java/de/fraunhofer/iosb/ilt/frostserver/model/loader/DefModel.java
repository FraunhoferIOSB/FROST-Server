/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.model.loader;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author hylke
 */
public class DefModel {

    private List<DefPropertyTypeSimple> simplePropertyTypes;
    private Map<String, DefEntityType> entityTypes;

    public void init() {
        for (Map.Entry<String, DefEntityType> entry : entityTypes.entrySet()) {
            String typeName = entry.getKey();
            DefEntityType type = entry.getValue();
            if (type.getName() == null) {
                type.setName(typeName);
            }
            type.init();
        }
    }

    public void registerPropertyTypes(ModelRegistry modelRegistry) {
        if (simplePropertyTypes != null) {
            for (DefPropertyTypeSimple propertyType : simplePropertyTypes) {
                modelRegistry.registerPropertyType(propertyType.getPropertyType(modelRegistry));
            }
        }
    }

    public void registerEntityTypes(ModelRegistry modelRegistry) {
        for (DefEntityType defType : entityTypes.values()) {
            modelRegistry.registerEntityType(defType.getEntityType());
        }
    }

    public boolean linkEntityTypes(ModelRegistry modelRegistry) {
        for (DefEntityType defType : entityTypes.values()) {
            defType.linkProperties(modelRegistry);
        }
        return true;
    }

    public Map<String, DefEntityType> getEntityTypes() {
        if (entityTypes == null) {
            entityTypes = new TreeMap<>();
        }
        return entityTypes;
    }

    public DefModel setEntityTypes(Map<String, DefEntityType> entityTypes) {
        this.entityTypes = entityTypes;
        return this;
    }

    public DefModel addEntityType(DefEntityType entityType) {
        getEntityTypes().put(entityType.getName(), entityType);
        return this;
    }

    public List<DefPropertyTypeSimple> getSimplePropertyTypes() {
        if (simplePropertyTypes == null) {
            simplePropertyTypes = new ArrayList<>();
        }
        return simplePropertyTypes;
    }

    public void setPropertyTypes(List<DefPropertyTypeSimple> simplePropertyTypes) {
        this.simplePropertyTypes = simplePropertyTypes;
    }

    public DefModel addSimplePropertyType(DefPropertyTypeSimple propertyType) {
        getSimplePropertyTypes().add(propertyType);
        return this;
    }

}
