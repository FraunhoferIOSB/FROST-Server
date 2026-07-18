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

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomDeserializationManager;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomEntityChangedMessageDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.CustomEntityDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.request.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
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
    private static final Map<ModelRegistry, ObjectMapper> mappers = new HashMap<>();

    /**
     * The mappers to use for admin users.
     */
    private static final Map<ModelRegistry, ObjectMapper> mappersAdmin = new HashMap<>();

    /**
     * Get an object mapper for the given id Class. If the id class is the same
     * as for the first call, the cached mapper is returned.
     *
     * @param modelRegistry The modelRegistry holding the data model to get a
     * mapper for.
     * @return The cached or created object mapper.
     */
    private static ObjectMapper getObjectMapper(ModelRegistry modelRegistry, Version version, boolean isAdmin) {
        ObjectMapper mapper;
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

    private static synchronized ObjectMapper initObjectMapper(ModelRegistry modelRegistry, Version version, boolean isAdmin) {
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
    private static synchronized ObjectMapper createObjectMapper(ModelRegistry modelRegistry, Version version, boolean isAdmin) {
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

        return JsonMapper.builder()
                .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .addModule(module)
                .build();
    }

    /**
     * The objectMapper for this instance of EntityParser.
     */
    private final ObjectMapper mapper;
    private final ModelRegistry modelRegistry;
    private final Version version;

    /**
     * Create a non-admin JsonReader.
     *
     * @param modelRegistry the model registry to create the json reader for.
     * @param version the API version to create the json reader for.
     */
    public JsonReaderDefault(ModelRegistry modelRegistry, Version version) {
        this(modelRegistry, version, false);
    }

    /**
     * Create a JsonReader.
     *
     * @param modelRegistry the model registry to create the json reader for.
     * @param version the API version to create the json reader for.
     * @param user the user to create the reader for.
     */
    public JsonReaderDefault(ModelRegistry modelRegistry, Version version, PrincipalExtended user) {
        this(modelRegistry, version, user.isAdmin());
    }

    /**
     * Create a JsonReader.
     *
     * @param modelRegistry the model registry to create the json reader for.
     * @param version The API version to create the reader for.
     * @param isAdmin flag indicating if the user is an admin.
     */
    public JsonReaderDefault(ModelRegistry modelRegistry, Version version, boolean isAdmin) {
        this.modelRegistry = modelRegistry;
        this.version = version;
        this.mapper = getObjectMapper(modelRegistry, version, isAdmin);
    }

    @Override
    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public Entity parseEntity(EntityType entityType, String value) throws JacksonException, IOException {
        try (final JsonParser parser = mapper.createParser(value)) {
            DeserializationContext dsc = mapper._deserializationContext();
            return CustomEntityDeserializer.getInstance(modelRegistry, entityType, version)
                    .deserializeFull(parser, dsc);
        } catch (StackOverflowError err) {
            throw new IOException("Json is too deeply nested.");
        }
    }

    @Override
    public Entity parseEntity(EntityType entityType, Reader value) throws JacksonException, IOException {
        try (final JsonParser parser = mapper.createParser(value)) {
            DeserializationContext dsc = mapper._deserializationContext();
            return CustomEntityDeserializer.getInstance(modelRegistry, entityType, version)
                    .deserializeFull(parser, dsc);
        } catch (StackOverflowError err) {
            throw new IOException("Json is too deeply nested.");
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
