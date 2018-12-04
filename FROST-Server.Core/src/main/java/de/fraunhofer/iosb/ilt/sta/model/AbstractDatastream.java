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

import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.core.NamedEntity;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.Objects;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 * @param <T> The exact type of the entity.
 */
public abstract class AbstractDatastream<T extends AbstractDatastream<T>> extends NamedEntity<T> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatastream.class);
    private String observationType;
    private Polygon observedArea; // reference to GeoJSON library
    private TimeInterval phenomenonTime;
    private TimeInterval resultTime;
    private Sensor sensor;
    private Thing thing;
    private EntitySet<Observation> observations;

    private boolean setObservationType;
    private boolean setObservedArea;
    private boolean setPhenomenonTime;
    private boolean setResultTime;
    private boolean setSensor;
    private boolean setThing;

    public AbstractDatastream() {
        this(null);
    }

    public AbstractDatastream(Id id) {
        super(id);
        this.observations = new EntitySetImpl<>(EntityType.OBSERVATION);
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
                setSensor(new Sensor(parentId));
                LOGGER.debug("Set sensorId to {}.", parentId);
                return true;

            case THING:
                setThing(new Thing(parentId));
                LOGGER.debug("Set thingId to {}.", parentId);
                return true;

            default:
                LOGGER.error("Incorrect 'parent' entity type for {}: {}", getEntityType(), parentEntity.getEntityType());
                return false;
        }
    }

    @Override
    public void setEntityPropertiesSet(boolean set) {
        super.setEntityPropertiesSet(set);
        setSetsAd(set);
    }

    private void setSetsAd(boolean set) {
        setObservationType = set;
        setObservedArea = set;
        setPhenomenonTime = set;
        setResultTime = set;
        setSensor = set;
        setThing = set;
    }

    @Override
    public void setEntityPropertiesSet(T comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSetsAd(false);
        if (!Objects.equals(observationType, comparedTo.getObservationType())) {
            setObservationType = true;
            message.addEpField(EntityProperty.OBSERVATIONTYPE);
        }
        if (!Objects.equals(observedArea, comparedTo.getObservedArea())) {
            setObservedArea = true;
            message.addEpField(EntityProperty.OBSERVEDAREA);
        }
        if (!Objects.equals(phenomenonTime, comparedTo.getPhenomenonTime())) {
            setPhenomenonTime = true;
            message.addEpField(EntityProperty.PHENOMENONTIME);
        }
        if (!Objects.equals(resultTime, comparedTo.getResultTime())) {
            setResultTime = true;
            message.addEpField(EntityProperty.RESULTTIME);
        }
        if (!Objects.equals(sensor, comparedTo.getSensor())) {
            setSensor = true;
            message.addNpField(NavigationProperty.SENSOR);
        }
        if (!Objects.equals(thing, comparedTo.getThing())) {
            setThing = true;
            message.addNpField(NavigationProperty.THING);
        }
    }

    public String getObservationType() {
        return observationType;
    }

    /**
     * Set the observation type without changing the "set" status of it.
     *
     * @param observationType The observation type to set.
     */
    protected final void setObservationTypeIntern(String observationType) {
        this.observationType = observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
        setObservationType = observationType != null;
    }

    /**
     * Flag indicating the observation type was set by the user.
     *
     * @return Flag indicating the observation type was set by the user.
     */
    public boolean isSetObservationType() {
        return setObservationType;
    }

    public Polygon getObservedArea() {
        return observedArea;
    }

    public void setObservedArea(Polygon observedArea) {
        this.observedArea = observedArea;
        setObservedArea = true;
    }

    /**
     * Flag indicating the observedArea was set by the user.
     *
     * @return Flag indicating the observedArea was set by the user.
     */
    public boolean isSetObservedArea() {
        return setObservedArea;
    }

    public TimeInterval getPhenomenonTime() {
        return phenomenonTime;
    }

    public void setPhenomenonTime(TimeInterval phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        setPhenomenonTime = true;
    }

    /**
     * Flag indicating the PhenomenonTime was set by the user.
     *
     * @return Flag indicating the PhenomenonTime was set by the user.
     */
    public boolean isSetPhenomenonTime() {
        return setPhenomenonTime;
    }

    public TimeInterval getResultTime() {
        return resultTime;
    }

    public void setResultTime(TimeInterval resultTime) {
        this.resultTime = resultTime;
        setResultTime = true;
    }

    /**
     * Flag indicating the ResultTime was set by the user.
     *
     * @return Flag indicating the ResultTime was set by the user.
     */
    public boolean isSetResultTime() {
        return setResultTime;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
        setSensor = sensor != null;
    }

    /**
     * Flag indicating the Sensor was set by the user.
     *
     * @return Flag indicating the Sensor was set by the user.
     */
    public boolean isSetSensor() {
        return setSensor;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
        setThing = thing != null;
    }

    /**
     * Flag indicating the Thing was set by the user.
     *
     * @return Flag indicating the Thing was set by the user.
     */
    public boolean isSetThing() {
        return setThing;
    }

    public EntitySet<Observation> getObservations() {
        return observations;
    }

    public void setObservations(EntitySet<Observation> observations) {
        this.observations = observations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                observationType,
                observedArea,
                phenomenonTime,
                resultTime,
                sensor,
                thing,
                observations);
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
                && Objects.equals(observationType, other.observationType)
                && Objects.equals(observedArea, other.observedArea)
                && Objects.equals(phenomenonTime, other.phenomenonTime)
                && Objects.equals(resultTime, other.resultTime)
                && Objects.equals(sensor, other.sensor)
                && Objects.equals(thing, other.thing)
                && Objects.equals(observations, other.observations);
    }

}
