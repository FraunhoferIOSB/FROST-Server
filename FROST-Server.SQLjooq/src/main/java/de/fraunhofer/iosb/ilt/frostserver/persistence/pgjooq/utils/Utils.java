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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.UTC;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.math.BigDecimal;
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
    public static final TypeReference<Object> TYPE_OBJECT = new TypeReference<Object>() {
        // Empty on purpose.
    };
    public static final TypeReference<List<String>> TYPE_LIST_STRING = new TypeReference<List<String>>() {
        // Empty on purpose.
    };
    public static final TypeReference<List<UnitOfMeasurement>> TYPE_LIST_UOM = new TypeReference<List<UnitOfMeasurement>>() {
        // Empty on purpose.
    };
    public static final TypeReference<Map<String, Object>> TYPE_MAP_STRING_OBJECT = new TypeReference<Map<String, Object>>() {
        // Empty on purpose.
    };

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final String FAILED_JSON_PARSE = "Failed to parse stored json.";
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
        if (encodingType == null) {
            return locationFromEncoding(locationString);
        } else {
            if (GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
                try {
                    return new GeoJsonDeserializier().deserialize(locationString);
                } catch (IOException ex) {
                    LOGGER.error("Failed to deserialise geoJson.", ex);
                }
                return locationString;
            } else {
                try {
                    return jsonToObject(locationString, Map.class);
                } catch (Exception ex) {
                    LOGGER.trace("Not a map.", ex);
                }
                return locationString;
            }
        }
    }

    private static Object locationFromEncoding(String locationString) {
        // We have to guess, since encodingType is not loaded.
        try {
            return new GeoJsonDeserializier().deserialize(locationString);
        } catch (IOException ex) {
            LOGGER.trace("Not geoJson.", ex);
        }
        try {
            return jsonToObject(locationString, Map.class);
        } catch (Exception ex) {
            LOGGER.trace("Not a map.", ex);
        }
        return locationString;
    }

    public static JsonNode jsonToTree(String json) {
        if (json == null) {
            return null;
        }

        try {
            return SimpleJsonMapper.getSimpleObjectMapper().readTree(json);
        } catch (IOException ex) {
            throw new IllegalStateException(FAILED_JSON_PARSE, ex);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return SimpleJsonMapper.getSimpleObjectMapper().readValue(json, clazz);
        } catch (IOException ex) {
            throw new IllegalStateException(FAILED_JSON_PARSE, ex);
        }
    }

    public static <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }
        try {
            return SimpleJsonMapper.getSimpleObjectMapper().readValue(json, typeReference);
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

    public static JsonValue getFieldJsonValue(Record record, Field<JsonValue> field) {
        if (record.field(field) != null) {
            return record.get(field);
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

        public List<OrderField> getSqlSortFields() {
            return sqlSortFields;
        }

        public List<Field> getSqlSortSelectFields() {
            return sqlSortSelectFields;
        }
    }

    public static <J extends Comparable<J>> void readResultFromDb(AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) {
        Short resultTypeOrd = Utils.getFieldOrNull(tuple, table.colResultType);
        if (resultTypeOrd != null) {
            ResultType resultType = ResultType.fromSqlValue(resultTypeOrd);
            switch (resultType) {
                case BOOLEAN:
                    entity.setResult(Utils.getFieldOrNull(tuple, table.colResultBoolean));
                    break;

                case NUMBER:
                    handleNumber(entity, tuple, table);
                    break;

                case OBJECT_ARRAY:
                    JsonValue jsonData = Utils.getFieldJsonValue(tuple, table.colResultJson);
                    dataSize.increase(jsonData.getStringLength());
                    entity.setResult(jsonData.getValue());
                    break;

                case STRING:
                    String stringData = Utils.getFieldOrNull(tuple, table.colResultString);
                    dataSize.increase(stringData == null ? 0 : stringData.length());
                    entity.setResult(stringData);
                    break;

                default:
                    LOGGER.error("Unhandled result type: {}", resultType);
                    throw new IllegalStateException("Unhandled resultType: " + resultType);
            }
        }
    }

    private static <J extends Comparable> void handleNumber(Observation entity, Record tuple, AbstractTableObservations<J> table) {
        try {
            entity.setResult(new BigDecimal(Utils.getFieldOrNull(tuple, table.colResultString)));
        } catch (NumberFormatException | NullPointerException e) {
            // It was not a Number? Use the double value.
            entity.setResult(Utils.getFieldOrNull(tuple, table.colResultNumber));
        }
    }

}
