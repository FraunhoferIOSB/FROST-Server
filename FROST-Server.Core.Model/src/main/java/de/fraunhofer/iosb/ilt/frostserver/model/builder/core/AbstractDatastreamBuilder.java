/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.model.builder.core;

import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.AbstractDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import org.geojson.Polygon;

/**
 * @author scf
 * @param <U> Type of class to build.
 * @param <V> Type of the builder class (will be recursive)
 */
public abstract class AbstractDatastreamBuilder<U extends AbstractDatastream<U>, V extends AbstractEntityBuilder<U, V>> extends NamedEntityBuilder<U, V> {

    private String observationType;
    private Polygon observedArea;
    private TimeInterval phenomenonTime;
    private TimeInterval resultTime;
    private Sensor sensor;
    private Thing thing;
    private EntitySet<Observation> observations;

    public AbstractDatastreamBuilder() {
        observations = new EntitySetImpl<>(EntityType.OBSERVATION);
    }

    public final V setObservedArea(Polygon observedArea) {
        this.observedArea = observedArea;
        return getThis();
    }

    public final V setPhenomenonTime(TimeInterval phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        return getThis();
    }

    public final V setResultTime(TimeInterval resultTime) {
        this.resultTime = resultTime;
        return getThis();
    }

    public final V setSensor(Sensor sensor) {
        this.sensor = sensor;
        return getThis();
    }

    public final V setThing(Thing thing) {
        this.thing = thing;
        return getThis();
    }

    public final V setObservationType(String observationType) {
        this.observationType = observationType;
        return getThis();
    }

    public final V setObservations(EntitySet<Observation> observations) {
        this.observations = observations;
        return getThis();
    }

    public final V addObservation(Observation observation) {
        this.observations.add(observation);
        return getThis();
    }

    @Override
    protected U build(U entity) {
        super.build(entity);
        if (observationType != null) {
            entity.setObservationType(observationType);
        }
        entity.setObservedArea(observedArea);
        entity.setPhenomenonTime(phenomenonTime);
        entity.setResultTime(resultTime);
        entity.setSensor(sensor);
        entity.setThing(thing);
        entity.setObservations(observations);
        return entity;
    }

}
