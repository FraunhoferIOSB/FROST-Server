/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

/**
 * A holder for the combination of an Annotation Source and Annotation
 * Namespace.
 *
 * @author hylke
 */
public class SourceNamespace {

    /**
     * The base URL defining an annotation, without file-extension.
     */
    private final String sourceUrlBase;

    /**
     * The Namespace of an annotation.
     */
    private final String nameSpace;

    public SourceNamespace(String sourceUrlBase, String nameSpace) {
        this.sourceUrlBase = sourceUrlBase;
        this.nameSpace = nameSpace;
    }

    /**
     * The base URL defining an annotation, without file-extension.
     *
     * @return the base URL defining an annotation, without file-extension.
     */
    public String getSourceUrlBase() {
        return sourceUrlBase;
    }

    /**
     * The Namespace of an annotation.
     *
     * @return the Namespace of an annotation.
     */
    public String getNameSpace() {
        return nameSpace;
    }

}
