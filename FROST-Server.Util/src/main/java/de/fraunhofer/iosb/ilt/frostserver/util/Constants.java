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
    /**
     * This semi-constant (arrays are not real constants) MUST be kept aligned
     * with the WebServlet annotations!
     */
    public static final String[] HTTP_URL_PATTERNS = {"/v1.0", "/v1.0/*", "/v1.1", "/v1.1/*"};

    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.class.getName());

    private Constants() {
        // Utility class, not to be instantiated.
    }

    /**
     * Throws an IllegalArgumentException if entity is not an instance of
     * targetClass, or if entity is null.
     *
     * @param entity The entity to check the class of.
     * @param targetClass The class to check the entity against.
     */
    public static void throwIfTypeNullOrNot(Object entity, Class targetClass) {
        if (entity == null || !targetClass.isAssignableFrom(entity.getClass())) {
            throw new IllegalArgumentException("Expected " + targetClass + " got " + entity);
        }
    }

    /**
     * Throws an IllegalArgumentException if entity is not an instance of
     * targetClass. Does not throw if entity is null.
     *
     * @param entity The entity to check the class of.
     * @param targetClass The class to check the entity against.
     */
    public static void throwIfTypeNot(Object entity, Class targetClass) {
        if (entity != null && !targetClass.isAssignableFrom(entity.getClass())) {
            LOGGER.error("Expected {}, but got {}", targetClass, entity);
            throw new IllegalArgumentException("Expected " + targetClass + " got " + entity);
        }
    }
}
