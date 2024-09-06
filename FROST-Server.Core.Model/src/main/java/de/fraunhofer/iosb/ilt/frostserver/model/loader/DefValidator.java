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
package de.fraunhofer.iosb.ilt.frostserver.model.loader;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorClass;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorSubclass;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityValidator;
import java.util.ArrayList;
import java.util.List;

/**
 * Definition of validators in model JSON files.
 */
@ConfigurableClass
public class DefValidator {

    @ConfigurableField(editor = EditorList.class,
            label = "createValidators", description = "The validators to apply when creating a new entity of this type.")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = NamedValidator.class)
    private List<NamedValidator> createValidators;

    @ConfigurableField(editor = EditorList.class,
            label = "updateValidators", description = "The validators to apply when updating an entity of this type.")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = NamedValidator.class)
    private List<NamedValidator> updateValidators;

    /**
     * Create the defined validator(s) for the given EntityType on the model in
     * the given ModelRegistry.
     *
     * @param modelRegistry The model to modify.
     * @param entityType The entity type to create the validator for.
     */
    public void createValidators(ModelRegistry modelRegistry, EntityType entityType) {
        for (var namedValidator : getCreateValidators()) {
            entityType.addCreateValidator(namedValidator.name, namedValidator.validator);
        }
        for (var namedValidator : getUpdateValidators()) {
            entityType.addUpdateValidator(namedValidator.name, namedValidator.validator);
        }
    }

    public List<NamedValidator> getCreateValidators() {
        if (createValidators == null) {
            createValidators = new ArrayList<>();
        }
        return createValidators;
    }

    public DefValidator setCreateValidators(List<NamedValidator> createValidators) {
        this.createValidators = createValidators;
        return this;
    }

    public List<NamedValidator> getUpdateValidators() {
        if (updateValidators == null) {
            updateValidators = new ArrayList<>();
        }
        return updateValidators;
    }

    public DefValidator setUpdateValidators(List<NamedValidator> updateValidators) {
        this.updateValidators = updateValidators;
        return this;
    }

    @ConfigurableClass
    public static class NamedValidator {

        @ConfigurableField(editor = EditorString.class,
                label = "name", description = "The name of the validator. Using an existing name overwrites that validator.")
        @EditorString.EdOptsString()
        public String name;

        @ConfigurableField(editor = EditorSubclass.class,
                label = "validator", description = "The implementation class of the validator.")
        @EditorSubclass.EdOptsSubclass(iface = EntityValidator.class, merge = true, nameField = "@class", shortenClassNames = true)
        public EntityValidator validator;
    }

}
