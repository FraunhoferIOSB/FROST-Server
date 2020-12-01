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

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;

/**
 *
 * @author scf
 */
public class GjUnitOfMeasurementProperty implements GjEntityEntry {

    private static final EntityPropertyMain<UnitOfMeasurement> EP_UOM = new EntityPropertyMain<>("unitOfMeasurement", new TypeReference<UnitOfMeasurement>() {
    });

    private final String headerName;
    private final String headerSymbol;
    private final String headerDefinition;

    public GjUnitOfMeasurementProperty(String name) {
        headerName = name + "/name";
        headerSymbol = name + "/symbol";
        headerDefinition = name + "/definition";
    }

    @Override
    public void writeData(GjRowCollector collector, Entity source, String namePrefix) {
        // TODO: Fix with a proper "coplexProperty" type.
        UnitOfMeasurement uom = source.getProperty(EP_UOM);
        collector.collectEntry(namePrefix + headerName, uom.getName());
        collector.collectEntry(namePrefix + headerSymbol, uom.getSymbol());
        collector.collectEntry(namePrefix + headerDefinition, uom.getDefinition());
    }

}
