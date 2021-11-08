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
package de.fraunhofer.iosb.ilt.frostserver.util;

import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class Constants {

    public static final ZoneOffset UTC = ZoneOffset.UTC;

    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_TYPE_APPLICATION_GEOJSON = "application/geo+json";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_APPLICATION_JSONPATCH = "application/json-patch+json";
    public static final String CONTENT_TYPE_APPLICATION_HTTP = "application/http";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    public static final String CHARSET_UTF8 = "charset=UTF-8";

    public static final String VALUE_ID_TYPE_LONG = "LONG";
    public static final String VALUE_ID_TYPE_STRING = "STRING";
    public static final String VALUE_ID_TYPE_UUID = "UUID";

    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.class.getName());

    private Constants() {
        // Utility class, not to be instantiated.
    }

    /**
     * Throws an IllegalArgumentException if entity is not an instance of
     * targetClass, or if entity is null. Returns the entity so calls can be
     * chained.
     *
     * @param <T> The type of the entity (auto detected)
     * @param entity The entity to check the class of.
     * @param targetClass The class to check the entity against.
     * @return The entity to check.
     */
    public static <T> T throwIfNullOrTypeNot(Object entity, Class<T> targetClass) {
        if (entity == null || !targetClass.isAssignableFrom(entity.getClass())) {
            throw new IllegalArgumentException("Expected " + targetClass + " got " + entity);
        }
        return (T) entity;
    }

    /**
     * Throws an IllegalArgumentException if entity is not an instance of
     * targetClass. Does not throw if entity is null. Returns the entity so
     * calls can be chained.
     *
     * @param <T> The type of the entity (auto detected)
     * @param entity The entity to check the class of.
     * @param targetClass The class to check the entity against.
     * @return The entity to check.
     */
    public static <T> T throwIfTypeNot(Object entity, Class<T> targetClass) {
        if (entity != null && !targetClass.isAssignableFrom(entity.getClass())) {
            LOGGER.error("Expected {}, but got {}", targetClass, entity);
            throw new IllegalArgumentException("Expected " + targetClass + " got " + entity);
        }
        return (T) entity;
    }
}
