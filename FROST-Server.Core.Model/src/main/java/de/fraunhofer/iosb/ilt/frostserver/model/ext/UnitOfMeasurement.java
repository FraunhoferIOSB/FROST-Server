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
package de.fraunhofer.iosb.ilt.frostserver.model.ext;

import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValueImpl;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;

/**
 * Model class for UnitOfMeasurement. This is not a first class entity in STA.
 */
public class UnitOfMeasurement {

    public static final String NAME_DEFINITION = "definition";
    public static final String NAME_NAME = "name";
    public static final String NAME_SYMBOL = "symbol";
    public static final EntityPropertyMain<String> EP_NAME = new EntityPropertyMain<>(NAME_NAME, EDM_STRING, false, true, false, true);
    public static final EntityPropertyMain<String> EP_DEFINITION = new EntityPropertyMain<>(NAME_DEFINITION, EDM_STRING, false, true, false, true);
    public static final EntityPropertyMain<String> EP_SYMBOL = new EntityPropertyMain<>(NAME_SYMBOL, EDM_STRING, false, true, false, true);

    public static ComplexValueImpl UnitOfMeasurement(
            String name,
            String symbol,
            String definition) {
        return new ComplexValueImpl(TypeComplex.TYPE_UOM)
                .setProperty(EP_NAME, name)
                .setProperty(EP_DEFINITION, definition)
                .setProperty(EP_SYMBOL, symbol);
    }

}
