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

import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.builder.core.AbstractDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for MultiDatastream objects.
 *
 * @author scf
 */
public class MultiDatastreamBuilder extends AbstractDatastreamBuilder<MultiDatastream, MultiDatastreamBuilder> {

    private List<String> multiObservationDataTypes;
    private List<UnitOfMeasurement> unitOfMeasurements;
    private EntitySet<ObservedProperty> observedProperties;

    public MultiDatastreamBuilder() {
        observedProperties = new EntitySetImpl<>(EntityType.OBSERVEDPROPERTY);
        unitOfMeasurements = new ArrayList<>();
        multiObservationDataTypes = new ArrayList<>();
    }

    public MultiDatastreamBuilder setObservedProperties(EntitySet<ObservedProperty> observedProperties) {
        this.observedProperties = observedProperties;
        return this;
    }

    public MultiDatastreamBuilder addObservedProperty(ObservedProperty observedProperty) {
        this.observedProperties.add(observedProperty);
        return this;
    }

    public MultiDatastreamBuilder setMultiObservationDataTypes(List<String> multiObservationDataTypes) {
        this.multiObservationDataTypes = multiObservationDataTypes;
        return this;
    }

    public MultiDatastreamBuilder addObservationType(String observationType) {
        this.multiObservationDataTypes.add(observationType);
        return this;
    }

    public MultiDatastreamBuilder setUnitOfMeasurements(List<UnitOfMeasurement> unitOfMeasurements) {
        this.unitOfMeasurements = unitOfMeasurements;
        return this;
    }

    public MultiDatastreamBuilder addUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurements.add(unitOfMeasurement);
        return this;
    }

    @Override
    protected MultiDatastreamBuilder getThis() {
        return this;
    }

    @Override
    public MultiDatastream build() {
        MultiDatastream mds = new MultiDatastream();
        super.build(mds);
        mds.setMultiObservationDataTypes(multiObservationDataTypes);
        mds.setUnitOfMeasurements(unitOfMeasurements);
        mds.setObservedProperties(observedProperties);
        return mds;
    }

}
