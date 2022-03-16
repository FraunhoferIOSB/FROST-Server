/*
 * Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.model.core.annotations;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorBoolean;

/**
 * A generic annotation that has a boolean value
 *
 * @author hylke
 */
public class AnnotationGenericBoolean extends AbstractAnnotation<AnnotationGenericBoolean> {

    @ConfigurableField(editor = EditorBoolean.class,
            label = "Value", description = "The value for the annotation.")
    @EditorBoolean.EdOptsBool()
    private boolean value;

    @Override
    public Boolean getValue() {
        return value;
    }

    public AnnotationGenericBoolean setValue(boolean value) {
        this.value = value;
        return this;
    }

    @Override
    public AnnotationGenericBoolean getThis() {
        return this;
    }

}
