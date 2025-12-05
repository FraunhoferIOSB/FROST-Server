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
package de.fraunhofer.iosb.ilt.frostserver.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A placeholder for a deep path.
 */
public class PropertyPlaceholder {

    private final String name;
    private List<String> subPath;

    public PropertyPlaceholder(String name) {
        this.name = name;
    }

    public PropertyPlaceholder(String... names) {
        this.name = names[0];
        for (int i = 1; i < names.length; i++) {
            addToSubPath(names[i]);
        }
    }

    public String getName() {
        return name;
    }

    public boolean hasSubPath() {
        return !(subPath == null || subPath.isEmpty());
    }

    public final PropertyPlaceholder addToSubPath(String name) {
        if (subPath == null) {
            subPath = new ArrayList<>();
        }
        subPath.add(name);
        return this;
    }

    public List<String> getSubPath() {
        if (subPath == null) {
            return Collections.emptyList();
        }
        return subPath;
    }

    @Override
    public String toString() {
        return name;
    }

}
