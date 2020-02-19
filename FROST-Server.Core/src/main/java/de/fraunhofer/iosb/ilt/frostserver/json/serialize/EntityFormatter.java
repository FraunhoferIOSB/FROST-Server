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
package de.fraunhofer.iosb.ilt.frostserver.json.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.frostserver.formatter.DataArrayResult;
import de.fraunhofer.iosb.ilt.frostserver.formatter.DataArrayValue;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.custom.CustomSerializationManager;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.EntitySetResult;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.json.mixin.MixinUtils;
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
        module.addSerializer(TimeValue.class, new TimeValueSerializer());
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

    public static String writeObject(Object object) throws IOException {
        return getObjectMapper().writeValueAsString(object);
    }
}
