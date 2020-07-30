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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomDeserializationManager;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomEntityChangedMessageDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomEntityDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.json.mixin.MixinUtils;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows parsing of STA entities from JSON. Fails on unknown properties in the
 * JSON input!
 *
 * @author jab
 */
public class JsonReader {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonReader.class);

    private static ObjectMapper mainMapper;
    private static Class<? extends Id> mainIdClass;

    /**
     * Get an object mapper for the given id Class. If the id class is the same
     * as for the first call, the cached mapper is returned.
     *
     * @param idClass The id class to use for this mapper.
     * @return The cached or created object mapper.
     */
    private static ObjectMapper getObjectMapper(Class<? extends Id> idClass) {
        if (mainMapper == null) {
            initMainObjectMapper(idClass);
        }
        if (mainIdClass != idClass) {
            LOGGER.warn("Object Mapper requested with different id class. {} instead of {}", idClass, mainIdClass);
            return createObjectMapper(idClass);
        }
        return mainMapper;
    }

    /**
     * Initialise the main object mapper.
     *
     * @param idClass The id class to use for the main object mapper.
     */
    private static synchronized void initMainObjectMapper(Class<? extends Id> idClass) {
        if (mainMapper != null) {
            return;
        }
        mainMapper = createObjectMapper(idClass);
        mainIdClass = idClass;
    }

    /**
     * Create a new object mapper for the given id Class.
     *
     * @param idClass The id class to use for this mapper.
     * @return The created object mapper.
     */
    private static ObjectMapper createObjectMapper(Class<? extends Id> idClass) {
        GeoJsonDeserializier geoJsonDeserializier = new GeoJsonDeserializier();
        for (String encodingType : GeoJsonDeserializier.ENCODINGS) {
            CustomDeserializationManager.getInstance().registerDeserializer(encodingType, geoJsonDeserializier);
        }
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        //mapper.setPropertyNamingStrategy(new EntitySetCamelCaseNamingStrategy());
        MixinUtils.addMixins(mapper);

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(EntitySet.class, EntitySetImpl.class);
        module.addAbstractTypeMapping(Id.class, idClass);
        for (EntityType entityType : EntityType.values()) {
            Class implementingClass = entityType.getImplementingClass();
            module.addDeserializer(implementingClass, new CustomEntityDeserializer<>(implementingClass));
        }
        module.addDeserializer(EntityChangedMessage.class, new CustomEntityChangedMessageDeserializer());
        module.addDeserializer(TimeInstant.class, new TimeInstantDeserializer());
        module.addDeserializer(TimeInterval.class, new TimeIntervalDeserializer());
        module.addDeserializer(TimeValue.class, new TimeValueDeserializer());

        mapper.registerModule(module);
        return mapper;
    }

    /**
     * The objectMapper for this instance of EntityParser.
     */
    private final ObjectMapper mapper;

    public JsonReader(Class<? extends Id> idClass) {
        mapper = getObjectMapper(idClass);
    }

    public Datastream parseDatastream(String value) throws IOException {
        return mapper.readValue(value, Datastream.class);
    }

    public MultiDatastream parseMultiDatastream(String value) throws IOException {
        return mapper.readValue(value, MultiDatastream.class);
    }

    public FeatureOfInterest parseFeatureOfInterest(String value) throws IOException {
        return mapper.readValue(value, FeatureOfInterest.class);
    }

    public HistoricalLocation parseHistoricalLocation(String value) throws IOException {
        return mapper.readValue(value, HistoricalLocation.class);
    }

    public Location parseLocation(String value) throws IOException {
        return mapper.readValue(value, Location.class);
    }

    public Observation parseObservation(String value) throws IOException {
        return mapper.readValue(value, Observation.class);
    }

    public ObservedProperty parseObservedProperty(String value) throws IOException {
        return mapper.readValue(value, ObservedProperty.class);
    }

    public Sensor parseSensor(String value) throws IOException {
        return mapper.readValue(value, Sensor.class);
    }

    public Thing parseThing(String value) throws IOException {
        return mapper.readValue(value, Thing.class);
    }

    public <T extends Entity> T parseEntity(Class<T> clazz, String value) throws IOException {
        return mapper.readValue(value, clazz);
    }

    public <T extends Entity> T parseEntity(Class<T> clazz, JsonNode value) throws IOException {
        return mapper.treeToValue(value, clazz);
    }

    public <T> T parseObject(Class<T> clazz, String value) throws IOException {
        return mapper.readValue(value, clazz);
    }

    public <T> T parseObject(TypeReference<T> typeReference, String value) throws IOException {
        return mapper.readValue(value, typeReference);
    }

}
