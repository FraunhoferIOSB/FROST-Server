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

import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.builder.core.AbstractDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;

/**
 * Builder class for Datastream objects.
 *
 * @author jab
 */
public class DatastreamBuilder extends AbstractDatastreamBuilder<Datastream, DatastreamBuilder> {

    private UnitOfMeasurement unitOfMeasurement;
    private ObservedProperty observedProperty;

    public DatastreamBuilder() {
    }

    public DatastreamBuilder setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
        return this;
    }

    public DatastreamBuilder setObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
        return this;
    }

    @Override
    protected DatastreamBuilder getThis() {
        return this;
    }

    @Override
    public Datastream build() {
        Datastream ds = new Datastream();
        super.build(ds);
        ds.setUnitOfMeasurement(unitOfMeasurement);
        ds.setObservedProperty(observedProperty);
        return ds;
    }

}
