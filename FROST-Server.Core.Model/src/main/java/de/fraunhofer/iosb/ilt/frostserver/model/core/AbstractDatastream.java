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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.Objects;
import org.geojson.GeoJsonObject;
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
    private GeoJsonObject observedArea; // reference to GeoJSON library
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
    public void complete(PathElementEntitySet containingSet) throws IncompleteEntityException {
        PathElement parent = containingSet.getParent();
        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                checkParent(parentEntity, parentId);
            }
        }

        super.complete(containingSet);
    }

    protected boolean checkParent(PathElementEntity parentEntity, Id parentId) {
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
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setSetsAd(set, entityPropertiesOnly);
    }

    private void setSetsAd(boolean set, boolean entityPropertiesOnly) {
        setObservationType = set;
        setObservedArea = set;
        setPhenomenonTime = set;
        setResultTime = set;
        if (!entityPropertiesOnly) {
            setSensor = set;
            setThing = set;
        }
    }

    @Override
    public void setEntityPropertiesSet(T comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSetsAd(false, false);
        if (!Objects.equals(observationType, comparedTo.getObservationType())) {
            setObservationType = true;
            message.addEpField(EntityPropertyMain.OBSERVATIONTYPE);
        }
        if (!Objects.equals(observedArea, comparedTo.getObservedArea())) {
            setObservedArea = true;
            message.addEpField(EntityPropertyMain.OBSERVEDAREA);
        }
        if (!Objects.equals(phenomenonTime, comparedTo.getPhenomenonTime())) {
            setPhenomenonTime = true;
            message.addEpField(EntityPropertyMain.PHENOMENONTIME);
        }
        if (!Objects.equals(resultTime, comparedTo.getResultTime())) {
            setResultTime = true;
            message.addEpField(EntityPropertyMain.RESULTTIME);
        }
        if (!Objects.equals(sensor, comparedTo.getSensor())) {
            setSensor = true;
            message.addNpField(NavigationPropertyMain.SENSOR);
        }
        if (!Objects.equals(thing, comparedTo.getThing())) {
            setThing = true;
            message.addNpField(NavigationPropertyMain.THING);
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

    public T setObservationType(String observationType) {
        this.observationType = observationType;
        setObservationType = observationType != null;
        return getThis();
    }

    /**
     * Flag indicating the observation type was set by the user.
     *
     * @return Flag indicating the observation type was set by the user.
     */
    public boolean isSetObservationType() {
        return setObservationType;
    }

    public GeoJsonObject getObservedArea() {
        return observedArea;
    }

    public T setObservedArea(GeoJsonObject observedArea) {
        this.observedArea = observedArea;
        setObservedArea = true;
        return getThis();
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

    public T setPhenomenonTime(TimeInterval phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        setPhenomenonTime = true;
        return getThis();
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

    public T setResultTime(TimeInterval resultTime) {
        this.resultTime = resultTime;
        setResultTime = true;
        return getThis();
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

    public T setSensor(Sensor sensor) {
        this.sensor = sensor;
        setSensor = sensor != null;
        return getThis();
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

    public T setThing(Thing thing) {
        this.thing = thing;
        setThing = thing != null;
        return getThis();
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

    public T setObservations(EntitySet<Observation> observations) {
        this.observations = observations;
        return getThis();
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
        final AbstractDatastream<T> other = (AbstractDatastream<T>) obj;
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
