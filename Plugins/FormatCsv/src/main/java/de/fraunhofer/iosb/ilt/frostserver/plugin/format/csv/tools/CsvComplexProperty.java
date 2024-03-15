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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.tools;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author scf
 */
public class CsvComplexProperty implements CsvEntityEntry {

    private final String headerPrefix;
    private final EntityProperty property;
    private final Map<Integer, String> subProperties = new TreeMap<>();

    public CsvComplexProperty(String headerPrefix, EntityProperty property) {
        this.headerPrefix = headerPrefix;
        this.property = property;
    }

    @Override
    public void writeHeader(CsvRowCollector collector) {
        final String baseName = property.getJsonName();
        final TypeComplex type = (TypeComplex) property.getType();
        for (Map.Entry<String, Property> subProperty : type.getPropertiesByName().entrySet()) {
            String subName = subProperty.getKey();
            int idx = collector.registerHeader(headerPrefix + baseName + "/" + subName);
            subProperties.put(idx, subName);
        }
    }

    @Override
    public void writeData(CsvRowCollector collector, Entity source) {
        Object value = source.getProperty(property);
        if (value instanceof ComplexValue complexValue) {
            for (Map.Entry<Integer, String> entry : subProperties.entrySet()) {
                collector.collectEntry(entry.getKey(), complexValue.getProperty(entry.getValue()));
            }
        } else if (value instanceof Map mapValue) {
            for (Map.Entry<Integer, String> entry : subProperties.entrySet()) {
                collector.collectEntry(entry.getKey(), mapValue.get(entry.getValue()));
            }
        }
    }

}
