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
package de.fraunhofer.iosb.ilt.frostserver.request;

import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import java.util.Map;
import java.util.TreeMap;

/**
 * Properties that are generated on-the-fly.
 */
public class SyntheticPropertyRegistry {

    private final Map<String, EntityPropertyMain<?>> properties = new TreeMap<>();

    public <P> SyntheticPropertyRegistry registerProperty(EntityPropertyMain<P> property) {
        return registerProperty(property.getName(), property);
    }

    public <P> SyntheticPropertyRegistry registerProperty(String externalName, EntityPropertyMain<P> property) {
        properties.put(externalName, property);
        return this;
    }

    public <P> EntityPropertyMain<P> getProperty(String name) {
        return (EntityPropertyMain<P>) properties.get(name);
    }

}
