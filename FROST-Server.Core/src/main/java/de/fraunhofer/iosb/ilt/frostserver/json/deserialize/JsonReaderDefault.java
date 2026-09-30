/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import com.fasterxml.jackson.annotation.JsonInclude;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomDeserializationManager;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomEntityChangedMessageDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomEntityDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.DateSerialiser;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityChangedMessageSerializer;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityPropertySerialiser;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntitySerializer;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntitySetResultSerializer;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityTypeSerialiser;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.MomentSerializer;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.OffsetDateTimeSerializer;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.TimeObjectSerializer;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.EntitySetResult;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeObject;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.request.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.time4j.Moment;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Allows parsing of STA entities from JSON. Fails on unknown properties in the
 * JSON input!
 */
public class JsonReaderDefault implements JsonReader {

    /**
     * The mappers to use for normal users.
     */
    private static final Map<ModelRegistry, JsonMapper> mappers = new HashMap<>();

    /**
     * The mappers to use for admin users.
     */
    private static final Map<ModelRegistry, JsonMapper> mappersAdmin = new HashMap<>();

    /**
     * Get an object mapper for the given id Class. If the id class is the same
     * as for the first call, the cached mapper is returned.
     *
     * @param modelRegistry The modelRegistry holding the data model to get a
     * mapper for.
     * @return The cached or created object mapper.
     */
    public static JsonMapper getObjectMapper(ModelRegistry modelRegistry, Version version, boolean isAdmin) {
        JsonMapper mapper;
        if (isAdmin) {
            mapper = mappersAdmin.get(modelRegistry);
        } else {
            mapper = mappers.get(modelRegistry);
        }
        if (mapper == null) {
            // computeIfAbsent is not thread-safe, and we don't want this method to be synchronised.
            mapper = initObjectMapper(modelRegistry, version, isAdmin);
        }
        return mapper;
    }

    private static synchronized JsonMapper initObjectMapper(ModelRegistry modelRegistry, Version version, boolean isAdmin) {
        if (isAdmin) {
            return mappersAdmin.computeIfAbsent(modelRegistry, mr -> createObjectMapper(mr, version, isAdmin));
        } else {
            return mappers.computeIfAbsent(modelRegistry, mr -> createObjectMapper(mr, version, isAdmin));
        }
    }

    /**
     * Create a new object mapper for the given model Registry.
     *
     * @param modelRegistry The modelRegistry holding the data model to create a
     * mapper for.
     * @return The created object mapper.
     */
    private static synchronized JsonMapper createObjectMapper(ModelRegistry modelRegistry, Version version, boolean isAdmin) {
        GeoJsonDeserializier geoJsonDeserializier = new GeoJsonDeserializier();
        for (String encodingType : GeoJsonDeserializier.ENCODINGS) {
            CustomDeserializationManager.registerDeserializer(encodingType, geoJsonDeserializier);
        }

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(EntitySet.class, EntitySetImpl.class);
        for (EntityType entityType : modelRegistry.getEntityTypes(isAdmin)) {
            CustomEntityDeserializer.getInstance(modelRegistry, entityType, version);
        }
        module.addDeserializer(EntityChangedMessage.class, new CustomEntityChangedMessageDeserializer(modelRegistry));
        module.addDeserializer(TimeInstant.class, new TimeInstantDeserializer());
        module.addDeserializer(TimeInterval.class, new TimeIntervalDeserializer());
        module.addDeserializer(TimeValue.class, new TimeValueDeserializer());
        module.addSerializer(Entity.class, new EntitySerializer());
        module.addSerializer(EntityChangedMessage.class, new EntityChangedMessageSerializer());
        module.addSerializer(EntitySetResult.class, new EntitySetResultSerializer());
        module.addSerializer(TimeObject.class, new TimeObjectSerializer());
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        module.addSerializer(Moment.class, new MomentSerializer());
        module.addSerializer(EntityType.class, new EntityTypeSerialiser());
        module.addSerializer(Property.class, new EntityPropertySerialiser());
        module.addSerializer(Date.class, new DateSerialiser());

        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
                .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)
                .addModule(module)
                .build();
    }

    /**
     * The objectMapper for this instance of EntityParser.
     */
    private final JsonMapper mapper;
    private final ModelRegistry modelRegistry;
    private final Version version;

    /**
     * Create a non-admin JsonReader.
     *
     * @param modelRegistry the model registry to create the JSON reader for.
     * @param version The API version to create the JSON reader for.
     */
    public JsonReaderDefault(ModelRegistry modelRegistry, Version version) {
        this(modelRegistry, version, false);
    }

    /**
     * Create a JsonReader.
     *
     * @param modelRegistry the model registry to create the json reader for.
     * @param version The API version to create the JSON reader for.
     * @param user the user to create the reader for.
     */
    public JsonReaderDefault(ModelRegistry modelRegistry, Version version, PrincipalExtended user) {
        this(modelRegistry, version, user.isAdmin());
    }

    /**
     * Create a JsonReader.
     *
     * @param modelRegistry the model registry to create the JSON reader for.
     * @param version The API version to create the JSON reader for.
     * @param isAdmin flag indicating if the user is an admin.
     */
    public JsonReaderDefault(ModelRegistry modelRegistry, Version version, boolean isAdmin) {
        this.modelRegistry = modelRegistry;
        this.version = version;
        this.mapper = getObjectMapper(modelRegistry, version, isAdmin);
    }

    @Override
    public JsonMapper getMapper() {
        return mapper;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public Entity parseEntity(EntityType entityType, String value) throws JacksonException {
        try (final JsonParser parser = mapper.createParser(value)) {
            DeserializationContext dsc = mapper._deserializationContext();
            return CustomEntityDeserializer.getInstance(modelRegistry, entityType, version)
                    .deserializeFull(parser, dsc);
        } catch (StackOverflowError err) {
            throw new StreamReadException("Json is too deeply nested.");
        }
    }

    @Override
    public Entity parseEntity(EntityType entityType, Reader value) throws JacksonException {
        try (final JsonParser parser = mapper.createParser(value)) {
            DeserializationContext dsc = mapper._deserializationContext();
            return CustomEntityDeserializer.getInstance(modelRegistry, entityType, version)
                    .deserializeFull(parser, dsc);
        } catch (StackOverflowError err) {
            throw new StreamReadException("Json is too deeply nested.");
        }
    }

    public <T> T parseObject(Class<T> clazz, String value) throws JacksonException {
        return mapper.readValue(value, clazz);
    }

    public <T> T parseObject(Class<T> clazz, Reader value) throws JacksonException {
        return mapper.readValue(value, clazz);
    }

    public <T> T parseObject(TypeReference<T> typeReference, String value) throws JacksonException {
        return mapper.readValue(value, typeReference);
    }

    public <T> T parseObject(TypeReference<T> typeReference, Reader value) throws JacksonException {
        return mapper.readValue(value, typeReference);
    }

}
