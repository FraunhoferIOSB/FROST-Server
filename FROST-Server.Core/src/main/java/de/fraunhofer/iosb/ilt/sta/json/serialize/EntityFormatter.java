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
package de.fraunhofer.iosb.ilt.sta.json.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.sta.formatter.DataArrayResult;
import de.fraunhofer.iosb.ilt.sta.formatter.DataArrayValue;
import de.fraunhofer.iosb.ilt.sta.json.serialize.custom.CustomSerializationManager;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntitySetResult;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.model.mixin.DatastreamMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.EntitySetResultMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.FeatureOfInterestMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.HistoricalLocationMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.LocationMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.MultiDatastreamMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.ObservationMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.ObservedPropertyMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.SensorMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.ThingMixIn;
import de.fraunhofer.iosb.ilt.sta.model.mixin.UnitOfMeasurementMixIn;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enables serialization of entities as JSON.
 *
 * @author jab
 */
public class EntityFormatter {

    private final ObjectMapper mapper;

    public EntityFormatter() {
        this(null);
    }

    public EntityFormatter(List<Property> selectedProperties) {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setPropertyNamingStrategy(new EntitySetCamelCaseNamingStrategy());
        mapper.addMixIn(Datastream.class, DatastreamMixIn.class);
        mapper.addMixIn(MultiDatastream.class, MultiDatastreamMixIn.class);
        mapper.addMixIn(FeatureOfInterest.class, FeatureOfInterestMixIn.class);
        mapper.addMixIn(HistoricalLocation.class, HistoricalLocationMixIn.class);
        mapper.addMixIn(Location.class, LocationMixIn.class);
        mapper.addMixIn(Observation.class, ObservationMixIn.class);
        mapper.addMixIn(ObservedProperty.class, ObservedPropertyMixIn.class);
        mapper.addMixIn(Sensor.class, SensorMixIn.class);
        mapper.addMixIn(Thing.class, ThingMixIn.class);
        mapper.addMixIn(UnitOfMeasurement.class, UnitOfMeasurementMixIn.class);
        mapper.addMixIn(EntitySetResult.class, EntitySetResultMixIn.class);

        SimpleModule module = new SimpleModule();
        GeoJsonSerializer geoJsonSerializer = new GeoJsonSerializer();
        for (String encodingType : GeoJsonSerializer.encodings) {
            CustomSerializationManager.getInstance().registerSerializer(encodingType, geoJsonSerializer);
        }

        if (selectedProperties != null && !selectedProperties.isEmpty()) {
            module.addSerializer(Entity.class, new EntitySerializer(
                    selectedProperties.stream().map(x -> x.getJsonName()).collect(Collectors.toList())
            ));
        } else {
            module.addSerializer(Entity.class, new EntitySerializer());
        }
        module.addSerializer(EntitySetResult.class, new EntitySetResultSerializer());
        module.addSerializer(DataArrayValue.class, new DataArrayValueSerializer());
        module.addSerializer(DataArrayResult.class, new DataArrayResultSerializer());
        mapper.registerModule(module);
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public <T extends Entity> String writeEntity(T entity) throws IOException {
        return mapper.writeValueAsString(entity);
    }

    public String writeEntityCollection(EntitySet entityCollection) throws IOException {
        return mapper.writeValueAsString(new EntitySetResult(entityCollection));
    }

    public String writeDatastream(Datastream datastream) throws IOException {
        return writeEntity(datastream);
    }

    public String writeFeatureOfInterest(FeatureOfInterest featureOfInterest) throws IOException {
        return writeEntity(featureOfInterest);
    }

    public String writeHistoricalLocation(HistoricalLocation historicalLocation) throws IOException {
        return writeEntity(historicalLocation);
    }

    public String writeLocation(Location location) throws IOException {
        return writeEntity(location);
    }

    public String writeObservation(Observation observation) throws IOException {
        return writeEntity(observation);
    }

    public String writeObservedProperty(ObservedProperty observedProperty) throws IOException {
        return writeEntity(observedProperty);
    }

    public String writeSensor(Sensor sensor) throws IOException {
        return writeEntity(sensor);
    }

    public String writeThing(Thing thing) throws IOException {
        return writeEntity(thing);
    }

    public String writeObject(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }
}
