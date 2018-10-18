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
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.HashMap;
import java.util.Map;
import org.geojson.Polygon;

/**
 * Builder class for Datastream objects.
 *
 * @author jab
 */
public class DatastreamBuilder extends AbstractEntityBuilder<Datastream, DatastreamBuilder> {

    private String name;
    private String description;
    private String observationType;
    private UnitOfMeasurement unitOfMeasurement;
    private Polygon observedArea;
    private TimeInterval phenomenonTime;
    private TimeInterval resultTime;
    private Map<String, Object> properties;
    private Sensor sensor;
    private ObservedProperty observedProperty;
    private Thing thing;
    private EntitySet<Observation> observations;

    public DatastreamBuilder() {
        properties = new HashMap<>();
        observations = new EntitySetImpl<>(EntityType.OBSERVATION);
    }

    public DatastreamBuilder setObservations(EntitySet<Observation> observations) {
        this.observations = observations;
        this.unitOfMeasurement = new UnitOfMeasurement();
        return this;
    }

    public DatastreamBuilder addObservation(Observation observation) {
        this.observations.add(observation);
        return this;
    }

    public DatastreamBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public DatastreamBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public DatastreamBuilder setObservationType(String observationType) {
        this.observationType = observationType;
        return this;
    }

    public DatastreamBuilder setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
        return this;
    }

    public DatastreamBuilder setObservedArea(Polygon observedArea) {
        this.observedArea = observedArea;
        return this;
    }

    public DatastreamBuilder setPhenomenonTime(TimeInterval phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        return this;
    }

    public DatastreamBuilder setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public DatastreamBuilder addProperty(String name, Object value) {
        this.properties.put(name, value);
        return this;
    }

    public DatastreamBuilder setResultTime(TimeInterval resultTime) {
        this.resultTime = resultTime;
        return this;
    }

    public DatastreamBuilder setSensor(Sensor sensor) {
        this.sensor = sensor;
        return this;
    }

    public DatastreamBuilder setObservedProperty(ObservedProperty observedProperty) {
        this.observedProperty = observedProperty;
        return this;
    }

    public DatastreamBuilder setThing(Thing thing) {
        this.thing = thing;
        return this;
    }

    @Override
    protected DatastreamBuilder getThis() {
        return this;
    }

    @Override
    public Datastream build() {
        Datastream ds = new Datastream(
                id,
                selfLink,
                navigationLink,
                name,
                description,
                observationType,
                properties,
                unitOfMeasurement,
                observedArea,
                phenomenonTime,
                resultTime,
                sensor,
                observedProperty,
                thing,
                observations);
        ds.setExportObject(isExportObject());
        return ds;
    }

}
