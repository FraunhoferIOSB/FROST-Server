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
package de.fraunhofer.iosb.ilt.sta.model;

import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 */
public class Datastream extends AbstractDatastream<Datastream> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Datastream.class);
    private UnitOfMeasurement unitOfMeasurement;
    private ObservedProperty observedProperty;

    private boolean setUnitOfMeasurement;
    private boolean setObservedProperty;

    public Datastream() {
        this(true, null);
    }

    public Datastream(Id id) {
        this(true, id);
    }

    public Datastream(boolean onlyId, Id id) {
        super(id);
        if (!onlyId) {
            this.unitOfMeasurement = new UnitOfMeasurement();
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DATASTREAM;
    }

    @Override
    protected boolean checkParent(EntityPathElement parentEntity, Id parentId) {
        if (parentEntity.getEntityType() == EntityType.OBSERVEDPROPERTY) {
            setObservedProperty(new ObservedProperty(parentId));
            LOGGER.debug("Set observedPropertyId to {}.", parentId);
            return true;
        }

        return super.checkParent(parentEntity, parentId);
    }

    @Override
    public void setEntityPropertiesSet() {
        super.setEntityPropertiesSet();
        setUnitOfMeasurement = true;
    }

    public UnitOfMeasurement getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
        setUnitOfMeasurement = unitOfMeasurement != null;
    }

    public boolean isSetUnitOfMeasurement() {
        return setUnitOfMeasurement;
    }

    public ObservedProperty getObservedProperty() {
        return observedProperty;
    }

    public void setObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
        setObservedProperty = observedProperty != null;
    }

    /**
     * @return true if the ObservedProperty was explicitly set.
     */
    public boolean isSetObservedProperty() {
        return setObservedProperty;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), unitOfMeasurement, observedProperty);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Datastream other = (Datastream) obj;
        return super.equals(other)
                && Objects.equals(observedProperty, other.observedProperty)
                && Objects.equals(unitOfMeasurement, other.unitOfMeasurement);
    }

}
