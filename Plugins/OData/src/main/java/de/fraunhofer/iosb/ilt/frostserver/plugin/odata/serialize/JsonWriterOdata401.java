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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.DateSerialiser;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityChangedMessageSerializer;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityPropertySerialiser;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntitySerializer;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityTypeSerialiser;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.MomentSerializer;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.io.Writer;
import java.util.Date;
import net.time4j.Moment;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Enables serialization of entities as JSON.
 */
public class JsonWriterOdata401 {

    public static final String AT_CONTEXT = "@context";
    public static final String AT_COUNT = "@count";
    public static final String AT_ID = "@id";
    public static final String AT_NAVIGATION_LINK = "@navigationLink";
    public static final String AT_NEXT_LINK = "@nextLink";

    private static ObjectMapper objectMapperInstance;

    private JsonWriterOdata401() {
    }

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
        SimpleModule module = new SimpleModule()
                .addSerializer(EntityWrapper.class, new EntityWrapperSerializer(AT_CONTEXT, AT_COUNT, AT_NAVIGATION_LINK, AT_NEXT_LINK, AT_ID))
                .addSerializer(Entity.class, new EntitySerializer(true, AT_COUNT, AT_NAVIGATION_LINK, AT_NEXT_LINK, AT_ID))
                .addSerializer(EntityChangedMessage.class, new EntityChangedMessageSerializer())
                .addSerializer(EntitySetResultOdata.class, new EntitySetResultOdataSerializer(AT_CONTEXT, AT_COUNT, AT_NEXT_LINK))
                .addSerializer(Moment.class, new MomentSerializer())
                .addSerializer(TimeValue.class, new TimeValueSerializer())
                .addSerializer(TimeInstant.class, new TimeInstantSerializer())
                .addSerializer(TimeInterval.class, new TimeIntervalSerializer())
                .addSerializer(EntityType.class, new EntityTypeSerialiser())
                .addSerializer(Property.class, new EntityPropertySerialiser())
                .addSerializer(Date.class, new DateSerialiser());

        ObjectMapper mapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_EMPTY))
                .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)
                .addModule(module)
                .build();

        return mapper;
    }

    public static void writeEntity(Writer writer, EntityWrapper entity) throws JacksonException {
        getObjectMapper().writeValue(writer, entity);
    }

    public static void writeEntityCollection(Writer writer, EntitySetResultOdata entityCollection) throws JacksonException {
        getObjectMapper().writeValue(writer, entityCollection);
    }

    public static void writeObject(Writer writer, Object object) throws JacksonException {
        getObjectMapper().writeValue(writer, object);
    }

    public static String writeObject(Object object) throws JacksonException {
        return getObjectMapper().writeValueAsString(object);
    }
}
