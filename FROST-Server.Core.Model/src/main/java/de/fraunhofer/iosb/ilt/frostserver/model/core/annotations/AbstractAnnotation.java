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

import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;

/**
 *
 * @author hylke
 * @param <T> The exact type of the annotation.
 */
public abstract class AbstractAnnotation<T extends AbstractAnnotation> implements Annotation, AnnotatedConfigurable<Void, Void> {

    @ConfigurableField(editor = EditorString.class,
            label = "BaseUrl", description = "The base URL defining the annotation, without file-extension.")
    @EditorString.EdOptsString()
    private String sourceUrlBase;

    @ConfigurableField(editor = EditorString.class,
            label = "NameSpace", description = "The Namespace of the annotation.")
    @EditorString.EdOptsString()
    private String nameSpace;

    @ConfigurableField(editor = EditorString.class,
            label = "Name", description = "The Name of the annotation.")
    @EditorString.EdOptsString()
    private String name;

    @Override
    public String getSourceUrl(DocType docType) {
        return sourceUrlBase + "." + docType.getDefaultExtension();
    }

    public T setSourceUrlBase(String sourceUrlBase) {
        this.sourceUrlBase = sourceUrlBase;
        return getThis();
    }

    @Override
    public String getNameSpace() {
        return nameSpace;
    }

    public T setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
        return getThis();
    }

    @Override
    public String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return getThis();
    }

    public abstract T getThis();
}
