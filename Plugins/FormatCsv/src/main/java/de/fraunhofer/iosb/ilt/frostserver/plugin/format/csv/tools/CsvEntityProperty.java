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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.tools;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;

/**
 * A class for gathering data from an Property of an Entity into a CSV file.
 *
 * @author scf
 */
public class CsvEntityProperty implements CsvEntityEntry {

    private final String headerName;
    private final EntityProperty property;
    private int columnIndex;

    /**
     * Create a new instance.
     *
     * @param headerName The name of the property in the CSV file.
     * @param property The property.
     */
    public CsvEntityProperty(String headerName, EntityProperty property) {
        this.headerName = headerName;
        this.property = property;
    }

    @Override
    public void writeHeader(CsvRowCollector collector) {
        columnIndex = collector.registerHeader(headerName);
    }

    @Override
    public void writeData(CsvRowCollector collector, Entity source) {
        collector.collectEntry(columnIndex, property.getFrom(source));
    }

}
