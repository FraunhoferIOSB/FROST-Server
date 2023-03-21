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
 *
 * @author hylke
 */
public class SourceNamespaceName extends SourceNamespace {

    /**
     * The name of an annotation.
     */
    private final String name;

    public SourceNamespaceName(SourceNamespace sn, String name) {
        super(sn.getSourceUrlBase(), sn.getNameSpace());
        this.name = name;
    }

    public SourceNamespaceName(String sourceUrlBase, String nameSpace, String name) {
        super(sourceUrlBase, nameSpace);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
