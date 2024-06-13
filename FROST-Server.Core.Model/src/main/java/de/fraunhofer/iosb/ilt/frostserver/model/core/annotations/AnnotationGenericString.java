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
package de.fraunhofer.iosb.ilt.frostserver.model.core.annotations;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;

/**
 * A generic annotation that has a boolean value
 *
 * @author hylke
 */
public class AnnotationGenericString extends AbstractAnnotation<AnnotationGenericString> {

    @ConfigurableField(editor = EditorString.class,
            label = "Value", description = "The value for the annotation.")
    @EditorString.EdOptsString()
    private String value;

    public AnnotationGenericString() {
        // Default constructor
    }

    public AnnotationGenericString(SourceNamespaceName snn, String value) {
        super(snn);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    public AnnotationGenericString setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public AnnotationGenericString getThis() {
        return this;
    }

}
