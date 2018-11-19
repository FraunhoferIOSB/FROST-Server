/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.model.builder;

import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;

/**
 * Builder class for UnitOfMeasurements objects.
 *
 * @author jab
 */
public class UnitOfMeasurementBuilder {

    private String name;
    private String symbol;
    private String definition;

    public UnitOfMeasurementBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public UnitOfMeasurementBuilder setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public UnitOfMeasurementBuilder setDefinition(String definition) {
        this.definition = definition;
        return this;
    }

    public UnitOfMeasurement build() {
        return new UnitOfMeasurement(name, symbol, definition);
    }

}
