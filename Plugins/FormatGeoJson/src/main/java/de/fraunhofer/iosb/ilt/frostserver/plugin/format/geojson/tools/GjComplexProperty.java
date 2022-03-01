/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.property.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author scf
 */
public class GjComplexProperty implements GjEntityEntry {

    private final String name;
    private final EntityProperty property;
    private final Map<String, String> subProperties = new TreeMap<>();

    public GjComplexProperty(String name, EntityProperty property) {
        this.name = name;
        this.property = property;
        final TypeComplex type = (TypeComplex) property.getType();
        for (Map.Entry<String, PropertyType> subProperty : type.getProperties().entrySet()) {
            String subName = subProperty.getKey();
            subProperties.put(name + "/" + subName, subName);
        }
    }

    @Override
    public void writeData(GjRowCollector collector, Entity source, String namePrefix) {
        Object value = source.getProperty(property);
        if (value instanceof ComplexValue) {
            ComplexValue complexValue = (ComplexValue) value;
            for (Map.Entry<String, String> entry : subProperties.entrySet()) {
                collector.collectEntry(namePrefix + entry.getKey(), complexValue.get(entry.getValue()));
            }
        } else if (value instanceof Map) {
            Map mapValue = (Map) value;
            for (Map.Entry<String, String> entry : subProperties.entrySet()) {
                collector.collectEntry(namePrefix + entry.getKey(), mapValue.get(entry.getValue()));
            }
        } else {
            collector.collectEntry(namePrefix + name, property.getFrom(source));
        }
    }

}
