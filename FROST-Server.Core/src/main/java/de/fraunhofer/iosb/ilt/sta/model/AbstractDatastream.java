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
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 */
public abstract class AbstractDatastream extends AbstractEntity {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatastream.class);
    private String name;
    private String description;
    protected String observationType;
    private Map<String, Object> properties;
    private Polygon observedArea; // reference to GeoJSON library
    private TimeInterval phenomenonTime;
    private TimeInterval resultTime;
    private Sensor sensor;
    private Thing thing;
    private EntitySet<Observation> observations;

    private boolean setName;
    private boolean setDescription;
    private boolean setObservationType;
    private boolean setObservedArea;
    private boolean setPhenomenonTime;
    private boolean setResultTime;
    private boolean setSensor;
    private boolean setThing;
    private boolean setProperties;

    public AbstractDatastream() {
        this.observations = new EntitySetImpl<>(EntityType.OBSERVATION);
    }

    public AbstractDatastream(Id id,
            String selfLink,
            String navigationLink,
            String name,
            String description,
            String observationType,
            Map<String, Object> properties,
            Polygon observedArea,
            TimeInterval phenomenonTime,
            TimeInterval resultTime,
            Sensor sensor,
            Thing thing,
            EntitySet<Observation> observations) {
        super(id, selfLink, navigationLink);
        this.name = name;
        this.description = description;
        this.observationType = observationType;
        this.observedArea = observedArea;
        this.phenomenonTime = phenomenonTime;
        this.resultTime = resultTime;
        this.sensor = sensor;
        this.thing = thing;
        this.observations = observations;
        if (properties != null && !properties.isEmpty()) {
            this.properties = new HashMap<>(properties);
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DATASTREAM;
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        ResourcePathElement parent = containingSet.getParent();
        if (parent instanceof EntityPathElement) {
            EntityPathElement parentEntity = (EntityPathElement) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                checkParent(parentEntity, parentId);
            }
        }

        super.complete(containingSet);
    }

    protected boolean checkParent(EntityPathElement parentEntity, Id parentId) {
        switch (parentEntity.getEntityType()) {
            case SENSOR:
                setSensor(new SensorBuilder().setId(parentId).build());
                LOGGER.debug("Set sensorId to {}.", parentId);
                return true;

            case THING:
                setThing(new ThingBuilder().setId(parentId).build());
                LOGGER.debug("Set thingId to {}.", parentId);
                return true;

            default:
                LOGGER.error("Incorrect 'parent' entity type for {}: {}", getEntityType(), parentEntity.getEntityType());
                return false;
        }
    }

    @Override
    public void setEntityPropertiesSet() {
        setDescription = true;
        setObservationType = true;
        setProperties = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setName = name != null;
    }

    public boolean isSetName() {
        return setName;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
        setDescription = description != null;
    }

    /**
     * @return the setDescription
     */
    public boolean isSetDescription() {
        return setDescription;
    }

    /**
     * @return the observationType
     */
    public String getObservationType() {
        return observationType;
    }

    /**
     * @param observationType the observationType to set
     */
    public void setObservationType(String observationType) {
        this.observationType = observationType;
        setObservationType = observationType != null;
    }

    /**
     * @return the setObservationType
     */
    public boolean isSetObservationType() {
        return setObservationType;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        if (properties != null && properties.isEmpty()) {
            properties = null;
        }
        this.properties = properties;
        setProperties = true;
    }

    public boolean isSetProperties() {
        return setProperties;
    }

    /**
     * @return the observedArea
     */
    public Polygon getObservedArea() {
        return observedArea;
    }

    /**
     * @param observedArea the observedArea to set
     */
    public void setObservedArea(Polygon observedArea) {
        this.observedArea = observedArea;
        setObservedArea = true;
    }

    /**
     * @return the setObservedArea
     */
    public boolean isSetObservedArea() {
        return setObservedArea;
    }

    /**
     * @return the phenomenonTime
     */
    public TimeInterval getPhenomenonTime() {
        return phenomenonTime;
    }

    /**
     * @param phenomenonTime the phenomenonTime to set
     */
    public void setPhenomenonTime(TimeInterval phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        setPhenomenonTime = true;
    }

    /**
     * @return the setPhenomenonTime
     */
    public boolean isSetPhenomenonTime() {
        return setPhenomenonTime;
    }

    /**
     * @return the resultTime
     */
    public TimeInterval getResultTime() {
        return resultTime;
    }

    /**
     * @param resultTime the resultTime to set
     */
    public void setResultTime(TimeInterval resultTime) {
        this.resultTime = resultTime;
        setResultTime = true;
    }

    /**
     * @return the setResultTime
     */
    public boolean isSetResultTime() {
        return setResultTime;
    }

    /**
     * @return the sensor
     */
    public Sensor getSensor() {
        return sensor;
    }

    /**
     * @param sensor the sensor to set
     */
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
        setSensor = sensor != null;
    }

    /**
     * @return the setSensor
     */
    public boolean isSetSensor() {
        return setSensor;
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
        setThing = thing != null;
    }

    /**
     * @return the setThing
     */
    public boolean isSetThing() {
        return setThing;
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

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                description,
                observationType,
                observedArea,
                phenomenonTime,
                resultTime,
                sensor,
                properties,
                thing);
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
        final AbstractDatastream other = (AbstractDatastream) obj;
        return super.equals(other)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.observationType, other.observationType)
                && Objects.equals(this.observedArea, other.observedArea)
                && Objects.equals(this.phenomenonTime, other.phenomenonTime)
                && Objects.equals(this.resultTime, other.resultTime)
                && Objects.equals(this.sensor, other.sensor)
                && Objects.equals(this.thing, other.thing)
                && Objects.equals(this.properties, other.properties);
    }

}
