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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.UTC;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.geolatte.common.dataformats.json.jackson.JsonMapper;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class Utils {

    public static final String INTERVAL_PARAM = "(?)::interval";
    public static final String TIMESTAMP_PARAM = "(?)::timestamp";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final String FAILED_JSON_PARSE = "Failed to parse stored json.";
    private static JsonMapper geoJsonMapper;

    private Utils() {
        // Utility class, should not be instantiated.
    }

    public static JsonMapper getGeoJsonMapper() {
        if (geoJsonMapper == null) {
            geoJsonMapper = new JsonMapper();
        }
        return geoJsonMapper;
    }

    public static TimeInterval intervalFromTimes(OffsetDateTime timeStart, OffsetDateTime timeEnd) {
        if (timeStart == null) {
            timeStart = OffsetDateTime.of(LocalDateTime.MAX, UTC);
        }
        if (timeEnd == null) {
            timeEnd = OffsetDateTime.of(LocalDateTime.MIN, UTC);
        }
        if (timeEnd.isBefore(timeStart)) {
            return null;
        } else {
            return TimeInterval.create(timeStart.toInstant().toEpochMilli(), timeEnd.toInstant().toEpochMilli());
        }
    }

    public static TimeInstant instantFromTime(OffsetDateTime time) {
        if (time == null) {
            return new TimeInstant(null);
        }
        return TimeInstant.create(time.toInstant().toEpochMilli());
    }

    public static TimeValue valueFromTimes(OffsetDateTime timeStart, OffsetDateTime timeEnd) {
        if (timeEnd == null || timeEnd.equals(timeStart)) {
            return instantFromTime(timeStart);
        }
        return intervalFromTimes(timeStart, timeEnd);
    }

    public static Object locationFromEncoding(String encodingType, String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
            try {
                return new GeoJsonDeserializier().deserialize(locationString);
            } catch (IOException ex) {
                LOGGER.error("Failed to deserialise geoJson.");

            }
        } else {
            try {
                return jsonToObject(locationString, Map.class);
            } catch (Exception e) {
                LOGGER.trace("Not a map.");
            }
            return locationString;
        }
        return null;
    }

    public static JsonNode jsonToTree(String json) {
        if (json == null) {
            return null;
        }

        try {
            return EntityParser.getSimpleObjectMapper().readTree(json);
        } catch (IOException ex) {
            throw new IllegalStateException(FAILED_JSON_PARSE, ex);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return EntityParser.getSimpleObjectMapper().readValue(json, clazz);
        } catch (IOException ex) {
            throw new IllegalStateException(FAILED_JSON_PARSE, ex);
        }
    }

    public static <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }
        try {
            return EntityParser.getSimpleObjectMapper().readValue(json, typeReference);
        } catch (IOException ex) {
            throw new IllegalStateException(FAILED_JSON_PARSE, ex);
        }
    }

    /**
     * Get the given Field from the record, or null if the record does not have
     * the Field.
     *
     * @param <T> The type of the requested Field.
     * @param record The record to fetch the field from.
     * @param field The field to fetch from the record.
     * @return The value of the field, or null if the record does not have the
     * Field.
     */
    public static <T> T getFieldOrNull(Record record, Field<T> field) {
        if (record.field(field) != null) {
            return record.get(field);
        }
        return null;
    }

    public static class SortSelectFields {

        private final List<OrderField> sqlSortFields = new ArrayList<>();
        private final List<Field> sqlSortSelectFields = new ArrayList<>();

        public void add(Field field, OrderBy.OrderType type) {
            if (type == OrderBy.OrderType.ASCENDING) {
                sqlSortFields.add(field.asc());
            } else {
                sqlSortFields.add(field.desc());
            }
            sqlSortSelectFields.add(field);
        }

        public List<OrderField> getSqlSortFields() {
            return sqlSortFields;
        }

        public List<Field> getSqlSortSelectFields() {
            return sqlSortSelectFields;
        }
    }
}
