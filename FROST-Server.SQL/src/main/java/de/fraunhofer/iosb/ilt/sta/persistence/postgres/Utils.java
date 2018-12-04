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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import org.geolatte.common.dataformats.json.jackson.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class Utils {

    public static final String INTERVAL_0 = "({0})::interval";
    public static final String INTERVAL_1 = "({1})::interval";
    public static final String TIMESTAMP_0 = "({0})::timestamp";
    public static final String TIMESTAMP_1 = "({1})::timestamp";

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

    public static TimeInterval intervalFromTimes(Timestamp timeStart, Timestamp timeEnd) {
        if (timeStart == null) {
            timeStart = Timestamp.valueOf(LocalDateTime.MAX);
        }
        if (timeEnd == null) {
            timeEnd = Timestamp.valueOf(LocalDateTime.MIN);
        }
        if (timeEnd.before(timeStart)) {
            return null;
        } else {
            return TimeInterval.create(timeStart.getTime(), timeEnd.getTime());
        }
    }

    public static TimeInstant instantFromTime(Timestamp time) {
        if (time == null) {
            return new TimeInstant(null);
        }
        return TimeInstant.create(time.getTime());
    }

    public static TimeValue valueFromTimes(Timestamp timeStart, Timestamp timeEnd) {
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

}
