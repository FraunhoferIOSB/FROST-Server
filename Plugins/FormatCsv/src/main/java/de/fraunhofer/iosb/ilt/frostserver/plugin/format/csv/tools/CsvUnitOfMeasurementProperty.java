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
import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class CsvUnitOfMeasurementProperty implements CsvEntityEntry {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvUnitOfMeasurementProperty.class);

    private final String headerPrefix;
    private final CsvElementFetcher<UnitOfMeasurement> fetcher;
    private int idxName;
    private int idxSymbol;
    private int idxDefinition;

    public CsvUnitOfMeasurementProperty(EntityType type, String headerPrefix) throws NoSuchMethodException, SecurityException {
        this.headerPrefix = headerPrefix;
        EntityProperty property = EntityProperty.UNITOFMEASUREMENT;
        final String getterName = property.getGetterName();

        final Class<? extends Entity> implementingClass = type.getImplementingClass();
        final Method getter = implementingClass.getMethod(getterName);
        fetcher = (Entity<?> e) -> {
            try {
                Object result = getter.invoke(e);
                if (result instanceof UnitOfMeasurement) {
                    return (UnitOfMeasurement) result;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.error("Failed to read element", ex);
            }
            return null;
        };
    }

    @Override
    public void writeHeader(CsvRowCollector collector) {
        idxName = collector.registerHeader(headerPrefix + "unitOfMeasurement/name");
        idxSymbol = collector.registerHeader(headerPrefix + "unitOfMeasurement/symbol");
        idxDefinition = collector.registerHeader(headerPrefix + "unitOfMeasurement/definition");
    }

    @Override
    public void writeData(CsvRowCollector collector, Entity<?> source) {
        UnitOfMeasurement uom = fetcher.fetch(source);
        collector.collectEntry(idxName, uom.getName());
        collector.collectEntry(idxSymbol, uom.getSymbol());
        collector.collectEntry(idxDefinition, uom.getDefinition());
    }

}
