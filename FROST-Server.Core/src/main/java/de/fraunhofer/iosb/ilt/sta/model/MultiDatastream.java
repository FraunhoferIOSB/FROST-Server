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
import de.fraunhofer.iosb.ilt.sta.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.model.id.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MultiDatastream extends AbstractEntity {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastream.class);
    private String name;
    private String description;
    private String observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation";
    private Map<String, Object> properties;
    private List<String> multiObservationDataTypes;
    private List<UnitOfMeasurement> unitOfMeasurements;
    private Polygon observedArea; // reference to GeoJSON library
    private TimeInterval phenomenonTime;
    private TimeInterval resultTime;
    private Sensor sensor;
    private EntitySet<ObservedProperty> observedProperties;
    private Thing thing;
    private EntitySet<Observation> observations;

    private boolean setName;
    private boolean setDescription;
    private boolean setObservationType;
    private boolean setMultiObservationDataTypes;
    private boolean setUnitOfMeasurements;
    private boolean setObservedArea;
    private boolean setPhenomenonTime;
    private boolean setResultTime;
    private boolean setSensor;
    private boolean setObservedProperties;
    private boolean setThing;
    private boolean setProperties;

    public MultiDatastream() {
        this.observations = new EntitySetImpl<>(EntityType.Observation);
        this.observedProperties = new EntitySetImpl<>(EntityType.ObservedProperty);
        this.unitOfMeasurements = new ArrayList<>();
        this.multiObservationDataTypes = new ArrayList<>();
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
        super(id, selfLink, navigationLink);
        this.name = name;
        this.description = description;
        this.multiObservationDataTypes = multiObservationDataTypes;
        this.unitOfMeasurements = unitOfMeasurements;
        this.observedArea = observedArea;
        this.phenomenonTime = phenomenonTime;
        this.resultTime = resultTime;
        this.sensor = sensor;
        this.observedProperties = observedProperties;
        this.thing = thing;
        this.observations = observations;
        if (properties != null && !properties.isEmpty()) {
            this.properties = new HashMap<>(properties);
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MultiDatastream;
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        ResourcePathElement parent = containingSet.getParent();
        if (parent != null && parent instanceof EntityPathElement) {
            EntityPathElement parentEntity = (EntityPathElement) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                switch (parentEntity.getEntityType()) {
                    case Sensor:
                        setSensor(new SensorBuilder().setId(parentId).build());
                        LOGGER.debug("Set sensorId to {}.", parentId);
                        break;

                    case Thing:
                        setThing(new ThingBuilder().setId(parentId).build());
                        LOGGER.debug("Set thingId to {}.", parentId);
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
        if (!entityPropertiesOnly) {
            if (observedProperties.size() != multiObservationDataTypes.size()) {
                throw new IllegalStateException("Size of list of observedProperties (" + observedProperties.size() + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
            }
        }
        if (observationType == null || !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
            throw new IllegalStateException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
        }
        super.complete(entityPropertiesOnly);
    }

    @Override
    public void setEntityPropertiesSet() {
        setDescription = true;
        setObservationType = true;
        setUnitOfMeasurements = true;
        setProperties = true;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the observationType
     */
    public String getObservationType() {
        return observationType;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public List<String> getMultiObservationDataTypes() {
        return multiObservationDataTypes;
    }

    /**
     * @return the unitOfMeasurement
     */
    public List<UnitOfMeasurement> getUnitOfMeasurements() {
        return unitOfMeasurements;
    }

    /**
     * @return the observedArea
     */
    public Polygon getObservedArea() {
        return observedArea;
    }

    /**
     * @return the phenomenonTime
     */
    public TimeInterval getPhenomenonTime() {
        return phenomenonTime;
    }

    /**
     * @return the resultTime
     */
    public TimeInterval getResultTime() {
        return resultTime;
    }

    /**
     * @return the sensor
     */
    public Sensor getSensor() {
        return sensor;
    }

    /**
     * @param observedArea the observedArea to set
     */
    public void setObservedArea(Polygon observedArea) {
        this.observedArea = observedArea;
        setObservedArea = true;
    }

    /**
     * @param phenomenonTime the phenomenonTime to set
     */
    public void setPhenomenonTime(TimeInterval phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        setPhenomenonTime = true;
    }

    /**
     * @param resultTime the resultTime to set
     */
    public void setResultTime(TimeInterval resultTime) {
        this.resultTime = resultTime;
        setResultTime = true;
    }

    /**
     * @return the observedProperty
     */
    public EntitySet<ObservedProperty> getObservedProperties() {
        return observedProperties;
    }

    public void setName(String name) {
        this.name = name;
        setName = true;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
        setDescription = true;
    }

    /**
     * @param observationType the observationType to set
     */
    public void setObservationType(String observationType) {
        this.observationType = observationType;
        setObservationType = true;
    }

    /**
     * @param observationTypes the observationTypes to set
     */
    public void setMultiObservationDataTypes(List<String> observationTypes) {
        this.multiObservationDataTypes = observationTypes;
        setMultiObservationDataTypes = true;
    }

    /**
     * @param unitsOfMeasurement the unitsOfMeasurement to set
     */
    public void setUnitOfMeasurements(List<UnitOfMeasurement> unitsOfMeasurement) {
        this.unitOfMeasurements = unitsOfMeasurement;
        setUnitOfMeasurements = true;
    }

    /**
     * @param sensor the sensor to set
     */
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
        setSensor = true;
    }

    /**
     * @param observedProperties the observedProperty to set
     */
    public void setObservedProperties(EntitySet<ObservedProperty> observedProperties) {
        this.observedProperties = observedProperties;
        setObservedProperties = true;
    }

    /**
     * @return the thing
     */
    public Thing getThing() {
        return thing;
    }

    /**
     * @param thing the thing to set
     */
    public void setThing(Thing thing) {
        this.thing = thing;
        setThing = true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.description);
        hash = 29 * hash + Objects.hashCode(this.observationType);
        hash = 29 * hash + Objects.hashCode(this.multiObservationDataTypes);
        hash = 29 * hash + Objects.hashCode(this.unitOfMeasurements);
        hash = 29 * hash + Objects.hashCode(this.observedArea);
        hash = 29 * hash + Objects.hashCode(this.phenomenonTime);
        hash = 29 * hash + Objects.hashCode(this.resultTime);
        hash = 29 * hash + Objects.hashCode(this.sensor);
        hash = 29 * hash + Objects.hashCode(this.observedProperties);
        hash = 29 * hash + Objects.hashCode(this.thing);
        hash = 29 * hash + Objects.hashCode(this.properties);
        return hash;
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
        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.observationType, other.observationType)) {
            return false;
        }
        if (!Objects.equals(this.observedArea, other.observedArea)) {
            return false;
        }
        if (!Objects.equals(this.phenomenonTime, other.phenomenonTime)) {
            return false;
        }
        if (!Objects.equals(this.resultTime, other.resultTime)) {
            return false;
        }
        if (!Objects.equals(this.sensor, other.sensor)) {
            return false;
        }
        if (!Objects.equals(this.observedProperties, other.observedProperties)) {
            return false;
        }
        if (!Objects.equals(this.thing, other.thing)) {
            return false;
        }
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        return true;
    }

    /**
     * @return the observations
     */
    public EntitySet<Observation> getObservations() {
        return observations;
    }

    /**
     * @param observations the observations to set
     */
    public void setObservations(EntitySet<Observation> observations) {
        this.observations = observations;
    }

    public void setProperties(Map<String, Object> properties) {
        if (properties != null && properties.isEmpty()) {
            properties = null;
        }
        this.properties = properties;
        setProperties = true;
    }

    public boolean isSetName() {
        return setName;
    }

    /**
     * @return the setDescription
     */
    public boolean isSetDescription() {
        return setDescription;
    }

    /**
     * @return the setObservationType
     */
    public boolean isSetObservationType() {
        return setObservationType;
    }

    /**
     * @return the setMultiObservationDataTypes
     */
    public boolean isSetMultiObservationDataTypes() {
        return setMultiObservationDataTypes;
    }

    /**
     * @return the setObservedArea
     */
    public boolean isSetObservedArea() {
        return setObservedArea;
    }

    /**
     * @return the setPhenomenonTime
     */
    public boolean isSetPhenomenonTime() {
        return setPhenomenonTime;
    }

    /**
     * @return the setResultTime
     */
    public boolean isSetResultTime() {
        return setResultTime;
    }

    /**
     * @return the setSensor
     */
    public boolean isSetSensor() {
        return setSensor;
    }

    /**
     * @return the setObservedProperty
     */
    public boolean isSetObservedProperties() {
        return setObservedProperties;
    }

    public boolean isSetProperties() {
        return setProperties;
    }

    /**
     * @return the setThing
     */
    public boolean isSetThing() {
        return setThing;
    }

    /**
     * @return the setUnitOfMeasurements
     */
    public boolean isSetUnitOfMeasurements() {
        return setUnitOfMeasurements;
    }

}
