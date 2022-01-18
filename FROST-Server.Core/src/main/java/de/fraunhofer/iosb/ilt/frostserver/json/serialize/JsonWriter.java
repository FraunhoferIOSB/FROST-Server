/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.json.mixin.MixinUtils;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.EntitySetResult;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Enables serialization of entities as JSON.
 *
 * @author jab
 * @author scf
 */
public class JsonWriter {

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
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);

        MixinUtils.addMixins(mapper);

        SimpleModule module = new SimpleModule();
        module.addSerializer(Entity.class, new EntitySerializer());
        module.addSerializer(EntityChangedMessage.class, new EntityChangedMessageSerializer());
        module.addSerializer(EntitySetResult.class, new EntitySetResultSerializer());
        module.addSerializer(TimeValue.class, new TimeValueSerializer());
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        module.addSerializer(EntityType.class, new EntityTypeSerialiser());
        module.addSerializer(Property.class, new EntityPropertySerialiser());
        module.addSerializer(Date.class, new DateSerialiser());
        mapper.registerModule(module);
        return mapper;
    }

    private JsonWriter() {
    }

    public static <T extends Entity> void writeEntity(Writer writer, T entity) throws IOException {
        getObjectMapper().writeValue(writer, entity);
    }

    public static <T extends Entity> String writeEntity(T entity) throws IOException {
        return getObjectMapper().writeValueAsString(entity);
    }

    public static void writeEntityCollection(Writer writer, EntitySet entityCollection) throws IOException {
        getObjectMapper().writeValue(writer, new EntitySetResult(entityCollection));
    }

    public static String writeEntityCollection(EntitySet entityCollection) throws IOException {
        return getObjectMapper().writeValueAsString(new EntitySetResult(entityCollection));
    }

    public static void writeObject(Writer writer, Object object) throws IOException {
        getObjectMapper().writeValue(writer, object);
    }

    public static String writeObject(Object object) throws IOException {
        return getObjectMapper().writeValueAsString(object);
    }
}
