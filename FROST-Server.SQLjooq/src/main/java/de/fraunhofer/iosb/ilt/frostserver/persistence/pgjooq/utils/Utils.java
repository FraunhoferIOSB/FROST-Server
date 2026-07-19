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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import static java.time.ZoneOffset.UTC;
import static net.time4j.TemporalType.INSTANT;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import net.time4j.Moment;
import org.geolatte.common.dataformats.json.jackson.JsonMapper;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Various utility methods.
 */
public class Utils {

    public static final String INTERVAL_PARAM = "(?)::interval";
    public static final String TIMESTAMP_PARAM = "(?)::timestamp";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static JsonMapper geoJsonMapper;
    private static final JsonValue NULL_JSON_VALUE = new JsonValue((JsonNode) null);

    private Utils() {
        // Utility class, should not be instantiated.
    }

    public static JsonMapper getGeoJsonMapper() {
        if (geoJsonMapper == null) {
            geoJsonMapper = new JsonMapper();
        }
        return geoJsonMapper;
    }

    public static TimeInterval intervalFromTimes(Moment timeStart, Moment timeEnd) {
        if (timeStart == null) {
            timeStart = INSTANT.translate(LocalDateTime.MAX.toInstant(UTC));
        }
        if (timeEnd == null) {
            timeEnd = INSTANT.translate(LocalDateTime.MIN.toInstant(UTC));
        }
        if (timeEnd.isBefore(timeStart)) {
            return null;
        }
        return TimeInterval.create(timeStart, timeEnd);
    }

    public static TimeInstant instantFromTime(Moment time) {
        if (time == null) {
            return new TimeInstant(null);
        }
        return new TimeInstant(time);
    }

    public static TimeValue valueFromTimes(Moment timeStart, Moment timeEnd) {
        if (timeStart == null && timeEnd == null) {
            return null;
        }
        if (timeEnd == null || timeEnd.equals(timeStart)) {
            return new TimeValue(instantFromTime(timeStart));
        }
        return new TimeValue(intervalFromTimes(timeStart, timeEnd));
    }

    public static Object locationFromEncoding(String encodingType, JsonNode location) {
        if (location == null || location.isEmpty()) {
            return null;
        }
        if (encodingType == null) {
            return locationUnknownEncoding(location);
        }
        if (GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
            try {
                return new GeoJsonDeserializier().deserialize(location);
            } catch (IOException | RuntimeException ex) {
                LOGGER.error("Failed to deserialise geoJson: {}.", location, ex);
            }
            return location;
        }
        return location;
    }

    public static Object locationUnknownEncoding(JsonNode locationString) {
        if (locationString == null) {
            return null;
        }
        // We have to guess, since encodingType is not loaded.
        try {
            return new GeoJsonDeserializier().deserialize(locationString);
        } catch (IOException ex) {
            LOGGER.trace("Not geoJson.", ex);
        }
        return locationString;
    }

    /**
     * Get the given Field from the record, or null if the record does not have
     * the Field.
     *
     * @param <T> The type of the requested Field.
     * @param input The Record to fetch the field from.
     * @param field The field to fetch from the input.
     * @return The value of the field, or null if the input does not have the
     * Field.
     */
    public static <T> T getFieldOrNull(Record input, Field<T> field) {
        if (input.field(field) != null) {
            return input.get(field);
        }
        return null;
    }

    public static JsonValue getFieldJsonValue(Record input, Field<JsonValue> field) {
        if (input.field(field) != null) {
            return input.get(field);
        }
        return NULL_JSON_VALUE;
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

        public void addAll(List<Field> fields) {
            for (var field : fields) {
                add(field, OrderBy.OrderType.ASCENDING);
            }
        }

        public List<OrderField> getSqlSortFields() {
            return sqlSortFields;
        }

        public List<Field> getSqlSortSelectFields() {
            return sqlSortSelectFields;
        }
    }

}
