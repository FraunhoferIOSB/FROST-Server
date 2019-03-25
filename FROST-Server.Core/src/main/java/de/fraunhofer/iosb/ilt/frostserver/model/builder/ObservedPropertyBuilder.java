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
package de.fraunhofer.iosb.ilt.frostserver.model.builder;

import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.core.NamedDsHoldingEntityBuilder;

/**
 * Builder class for ObservedProperty objects.
 *
 * @author jab
 */
public class ObservedPropertyBuilder extends NamedDsHoldingEntityBuilder<ObservedProperty, ObservedPropertyBuilder> {

    private String definition;

    public ObservedPropertyBuilder setDefinition(String definition) {
        this.definition = definition;
        return this;
    }

    @Override
    protected ObservedPropertyBuilder getThis() {
        return this;
    }

    @Override
    public ObservedProperty build() {
        ObservedProperty op = new ObservedProperty();
        super.build(op);
        op.setDefinition(definition);
        return op;
    }

}
