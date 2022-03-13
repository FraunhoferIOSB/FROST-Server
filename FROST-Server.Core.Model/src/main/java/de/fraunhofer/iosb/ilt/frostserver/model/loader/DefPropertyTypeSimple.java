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
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;

/**
 *
 * @author hylke
 */
@ConfigurableClass
public class DefPropertyTypeSimple implements AnnotatedConfigurable<Void, Void> {

    @ConfigurableField(editor = EditorString.class,
            label = "Name", description = "The name of the PropertyType.")
    @EditorString.EdOptsString()
    private String name;

    @ConfigurableField(editor = EditorString.class,
            label = "Description", description = "The description of the PropertyType.")
    @EditorString.EdOptsString()
    private String description;

    @ConfigurableField(editor = EditorString.class,
            label = "Base Type", description = "The base type of the PropertyType.")
    @EditorString.EdOptsString()
    private String baseType;

    public PropertyType getPropertyType() {
        return new TypeSimpleCustom(name, description, TypeSimplePrimitive.getType(baseType));
    }
}
