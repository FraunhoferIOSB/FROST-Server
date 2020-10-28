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
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;

/**
 *
 * @author scf
 */
public class CsvUnitOfMeasurementProperty implements CsvEntityEntry {

    private final String headerPrefix;
    private int idxName;
    private int idxSymbol;
    private int idxDefinition;

    public CsvUnitOfMeasurementProperty(String headerPrefix) {
        this.headerPrefix = headerPrefix;
    }

    @Override
    public void writeHeader(CsvRowCollector collector) {
        idxName = collector.registerHeader(headerPrefix + "unitOfMeasurement/name");
        idxSymbol = collector.registerHeader(headerPrefix + "unitOfMeasurement/symbol");
        idxDefinition = collector.registerHeader(headerPrefix + "unitOfMeasurement/definition");
    }

    @Override
    public void writeData(CsvRowCollector collector, Entity source) {
        UnitOfMeasurement uom = (UnitOfMeasurement) EntityPropertyMain.UNITOFMEASUREMENT.getFrom(source);
        collector.collectEntry(idxName, uom.getName());
        collector.collectEntry(idxSymbol, uom.getSymbol());
        collector.collectEntry(idxDefinition, uom.getDefinition());
    }

}
