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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.model.core.AbstractDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
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
    protected boolean checkParent(PathElementEntity parentEntity, Id parentId) {
        if (parentEntity.getEntityType() == EntityType.OBSERVEDPROPERTY) {
            setObservedProperty(new ObservedProperty(parentId));
            LOGGER.debug("Set observedPropertyId to {}.", parentId);
            return true;
        }

        return super.checkParent(parentEntity, parentId);
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setSets(set, entityPropertiesOnly);
    }

    private void setSets(boolean set, boolean entityPropertiesOnly) {
        setUnitOfMeasurement = set;
        if (!entityPropertiesOnly) {
            setObservedProperty = set;
        }
    }

    @Override
    public void setEntityPropertiesSet(Datastream comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSets(false, false);
        if (!Objects.equals(unitOfMeasurement, comparedTo.getUnitOfMeasurement())) {
            setUnitOfMeasurement = true;
            message.addEpField(EntityPropertyMain.UNITOFMEASUREMENT);
        }
        if (!Objects.equals(observedProperty, comparedTo.getObservedProperty())) {
            setObservedProperty = true;
            message.addNpField(NavigationPropertyMain.OBSERVEDPROPERTY);
        }
    }

    public UnitOfMeasurement getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public Datastream setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
        setUnitOfMeasurement = unitOfMeasurement != null;
        return this;
    }

    public boolean isSetUnitOfMeasurement() {
        return setUnitOfMeasurement;
    }

    public ObservedProperty getObservedProperty() {
        return observedProperty;
    }

    public Datastream setObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
        setObservedProperty = observedProperty != null;
        return this;
    }

    /**
     * @return true if the ObservedProperty was explicitly set.
     */
    public boolean isSetObservedProperty() {
        return setObservedProperty;
    }

    @Override
    protected Datastream getThis() {
        return this;
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
