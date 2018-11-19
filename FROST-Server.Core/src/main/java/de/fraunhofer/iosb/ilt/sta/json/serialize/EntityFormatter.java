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
import de.fraunhofer.iosb.ilt.sta.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.sta.json.serialize.custom.CustomSerializationManager;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntitySetResult;
import de.fraunhofer.iosb.ilt.sta.model.mixin.MixinUtils;
import java.io.IOException;

/**
 * Enables serialization of entities as JSON.
 *
 * @author jab
 */
public class EntityFormatter {

    private static ObjectMapper objectMapperInstance;

    public static ObjectMapper getObjectMapper() {
        if (objectMapperInstance == null) {
            initObjectMapper();
        }
        return objectMapperInstance;
    }

    private static synchronized void initObjectMapper() {
        if (objectMapperInstance == null) {
            objectMapperInstance = createObjectMapper();
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setPropertyNamingStrategy(new EntitySetCamelCaseNamingStrategy());

        MixinUtils.addMixins(mapper);

        SimpleModule module = new SimpleModule();
        GeoJsonSerializer geoJsonSerializer = new GeoJsonSerializer();
        for (String encodingType : GeoJsonDeserializier.ENCODINGS) {
            CustomSerializationManager.getInstance().registerSerializer(encodingType, geoJsonSerializer);
        }

        module.addSerializer(Entity.class, new EntitySerializer());
        module.addSerializer(EntitySetResult.class, new EntitySetResultSerializer());
        module.addSerializer(DataArrayValue.class, new DataArrayValueSerializer());
        module.addSerializer(DataArrayResult.class, new DataArrayResultSerializer());
        mapper.registerModule(module);
        return mapper;
    }

    private EntityFormatter() {
    }

    public static <T extends Entity> String writeEntity(T entity) throws IOException {
        return getObjectMapper().writeValueAsString(entity);
    }

    public static String writeEntityCollection(EntitySet entityCollection) throws IOException {
        return getObjectMapper().writeValueAsString(new EntitySetResult(entityCollection));
    }

    public static String writeDatastream(Datastream datastream) throws IOException {
        return writeEntity(datastream);
    }

    public static String writeFeatureOfInterest(FeatureOfInterest featureOfInterest) throws IOException {
        return writeEntity(featureOfInterest);
    }

    public static String writeHistoricalLocation(HistoricalLocation historicalLocation) throws IOException {
        return writeEntity(historicalLocation);
    }

    public static String writeLocation(Location location) throws IOException {
        return writeEntity(location);
    }

    public static String writeObservation(Observation observation) throws IOException {
        return writeEntity(observation);
    }

    public static String writeObservedProperty(ObservedProperty observedProperty) throws IOException {
        return writeEntity(observedProperty);
    }

    public static String writeSensor(Sensor sensor) throws IOException {
        return writeEntity(sensor);
    }

    public static String writeThing(Thing thing) throws IOException {
        return writeEntity(thing);
    }

    public static String writeObject(Object object) throws IOException {
        return getObjectMapper().writeValueAsString(object);
    }
}
