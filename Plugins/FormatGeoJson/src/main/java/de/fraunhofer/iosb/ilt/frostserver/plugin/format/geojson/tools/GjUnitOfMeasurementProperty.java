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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class GjUnitOfMeasurementProperty implements GjEntityEntry {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GjUnitOfMeasurementProperty.class);

    private final GjElementFetcher<UnitOfMeasurement> fetcher;
    private final String headerName;
    private final String headerSymbol;
    private final String headerDefinition;

    public GjUnitOfMeasurementProperty(EntityType type, String headerPrefix) throws NoSuchMethodException, SecurityException {
        headerName = headerPrefix + "unitOfMeasurement/name";
        headerSymbol = headerPrefix + "unitOfMeasurement/symbol";
        headerDefinition = headerPrefix + "unitOfMeasurement/definition";
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
    public void writeData(GjRowCollector collector, Entity<?> source) {
        UnitOfMeasurement uom = fetcher.fetch(source);
        collector.collectEntry(headerName, uom.getName());
        collector.collectEntry(headerSymbol, uom.getSymbol());
        collector.collectEntry(headerDefinition, uom.getDefinition());
    }

}
