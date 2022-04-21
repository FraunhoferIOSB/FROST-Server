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

import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorClass;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hylke
 */
@ConfigurableClass
public class DefModel implements AnnotatedConfigurable<Void, Void> {

    @ConfigurableField(editor = EditorList.class,
            label = "Conformance", description = "The conformance classes this model implements.")
    @EditorList.EdOptsList(editor = EditorString.class)
    @EditorString.EdOptsString()
    private List<String> conformance;

    @ConfigurableField(editor = EditorList.class,
            label = "Property Types", description = "Custom property types.")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = DefPropertyTypeSimple.class)
    private List<DefPropertyTypeSimple> simplePropertyTypes;

    @ConfigurableField(editor = EditorList.class,
            label = "Entity Types", description = "The entity types of the model.")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = DefEntityType.class)
    private List<DefEntityType> entityTypes;

    public void init() {
        for (DefEntityType type : entityTypes) {
            type.init();
        }
    }

    public void registerPropertyTypes(ModelRegistry modelRegistry) {
        if (simplePropertyTypes != null) {
            for (DefPropertyTypeSimple propertyType : simplePropertyTypes) {
                modelRegistry.registerPropertyType(propertyType.getPropertyType());
            }
        }
    }

    public void registerEntityTypes(ModelRegistry modelRegistry) {
        for (DefEntityType defType : entityTypes) {
            modelRegistry.registerEntityType(defType.getEntityType(modelRegistry));
        }
    }

    public boolean linkEntityTypes(ModelRegistry modelRegistry) {
        for (DefEntityType defType : entityTypes) {
            defType.linkProperties(modelRegistry);
        }
        return true;
    }

    public List<DefEntityType> getEntityTypes() {
        if (entityTypes == null) {
            entityTypes = new ArrayList<>();
        }
        return entityTypes;
    }

    public DefModel setEntityTypes(List<DefEntityType> entityTypes) {
        this.entityTypes = entityTypes;
        return this;
    }

    public DefModel addEntityType(DefEntityType entityType) {
        getEntityTypes().add(entityType);
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

    public List<String> getConformance() {
        if (conformance == null) {
            conformance = new ArrayList<>();
        }
        return conformance;
    }

    public void addConformance(String conformanceClass) {
        getConformance().add(conformanceClass);
    }
}
