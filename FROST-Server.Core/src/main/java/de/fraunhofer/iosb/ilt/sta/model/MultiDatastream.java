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

import de.fraunhofer.iosb.ilt.sta.model.builder.SensorBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.ObservationType;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 */
public class MultiDatastream extends AbstractDatastream {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastream.class);

    private List<String> multiObservationDataTypes;
    private List<UnitOfMeasurement> unitOfMeasurements;
    private EntitySet<ObservedProperty> observedProperties;

    private boolean setMultiObservationDataTypes;
    private boolean setUnitOfMeasurements;
    private boolean setObservedProperties;

    public MultiDatastream() {
        observedProperties = new EntitySetImpl<>(EntityType.OBSERVEDPROPERTY);
        unitOfMeasurements = new ArrayList<>();
        multiObservationDataTypes = new ArrayList<>();
        observationType = ObservationType.COMPLEX_OBSERVATION.getCode();
    }

    public MultiDatastream(Id id,
            String selfLink,
            String navigationLink,
            String name,
            String description,
            Map<String, Object> properties,
            List<String> multiObservationDataTypes,
            List<UnitOfMeasurement> unitOfMeasurements,
            Polygon observedArea,
            TimeInterval phenomenonTime,
            TimeInterval resultTime,
            Sensor sensor,
            EntitySet<ObservedProperty> observedProperties,
            Thing thing,
            EntitySet<Observation> observations) {
        super(id, selfLink, navigationLink, name, description, ObservationType.COMPLEX_OBSERVATION.getCode(), properties, observedArea, phenomenonTime, resultTime, sensor, thing, observations);
        this.multiObservationDataTypes = multiObservationDataTypes;
        this.unitOfMeasurements = unitOfMeasurements;
        this.observedProperties = observedProperties;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MULTIDATASTREAM;
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        ResourcePathElement parent = containingSet.getParent();
        if (parent instanceof EntityPathElement) {
            EntityPathElement parentEntity = (EntityPathElement) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                switch (parentEntity.getEntityType()) {
                    case SENSOR:
                        setSensor(new SensorBuilder().setId(parentId).build());
                        LOGGER.debug("Set sensorId to {}.", parentId);
                        break;

                    case THING:
                        setThing(new ThingBuilder().setId(parentId).build());
                        LOGGER.debug("Set thingId to {}.", parentId);
                        break;

                    default:
                        LOGGER.error("Incorrect 'parent' entity type for {}: {}", getEntityType(), parentEntity.getEntityType());
                        break;
                }
            }
        }
        super.complete(containingSet);
    }

    @Override
    public void complete(boolean entityPropertiesOnly) throws IncompleteEntityException {
        if (unitOfMeasurements.size() != multiObservationDataTypes.size()) {
            throw new IllegalStateException("Size of list of unitOfMeasurements (" + unitOfMeasurements.size() + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
        }
        if (!entityPropertiesOnly && observedProperties.size() != multiObservationDataTypes.size()) {
            throw new IllegalStateException("Size of list of observedProperties (" + observedProperties.size() + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
        }
        if (observationType == null || !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
            throw new IllegalStateException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
        }
        super.complete(entityPropertiesOnly);
    }

    @Override
    public void setEntityPropertiesSet() {
        super.setEntityPropertiesSet();
        setUnitOfMeasurements = true;
    }

    public List<String> getMultiObservationDataTypes() {
        return multiObservationDataTypes;
    }

    /**
     * @param observationTypes the observationTypes to set
     */
    public void setMultiObservationDataTypes(List<String> observationTypes) {
        this.multiObservationDataTypes = observationTypes;
        setMultiObservationDataTypes = multiObservationDataTypes != null;
    }

    /**
     * @return the setMultiObservationDataTypes
     */
    public boolean isSetMultiObservationDataTypes() {
        return setMultiObservationDataTypes;
    }

    /**
     * @return the unitOfMeasurements
     */
    public List<UnitOfMeasurement> getUnitOfMeasurements() {
        return unitOfMeasurements;
    }

    /**
     * @param unitsOfMeasurement the unitsOfMeasurement to set
     */
    public void setUnitOfMeasurements(List<UnitOfMeasurement> unitsOfMeasurement) {
        this.unitOfMeasurements = unitsOfMeasurement;
        setUnitOfMeasurements = unitOfMeasurements != null;
    }

    /**
     * @return the setUnitOfMeasurements
     */
    public boolean isSetUnitOfMeasurements() {
        return setUnitOfMeasurements;
    }

    /**
     * @return the observedProperty
     */
    public EntitySet<ObservedProperty> getObservedProperties() {
        return observedProperties;
    }

    /**
     * @param observedProperties the observedProperty to set
     */
    public void setObservedProperties(EntitySet<ObservedProperty> observedProperties) {
        this.observedProperties = observedProperties;
        setObservedProperties = observedProperties != null;
    }

    /**
     * @return the setObservedProperty
     */
    public boolean isSetObservedProperties() {
        return setObservedProperties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), multiObservationDataTypes, unitOfMeasurements, observedProperties);
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
        final MultiDatastream other = (MultiDatastream) obj;
        return super.equals(other)
                && Objects.equals(multiObservationDataTypes, other.multiObservationDataTypes)
                && Objects.equals(observedProperties, other.observedProperties)
                && Objects.equals(unitOfMeasurements, other.unitOfMeasurements);
    }

}
