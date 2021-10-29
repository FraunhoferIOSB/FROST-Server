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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.statests.util;

/**
 *
 * @author hylke
 */
public enum IdType {
    LONG(() -> Long.MAX_VALUE - 1000),
    STRING(() -> java.util.UUID.randomUUID()),
    UUID(() -> java.util.UUID.randomUUID());

    private final RandomGenerator generator;

    private IdType(RandomGenerator generator) {
        this.generator = generator;
    }

    public Object generateUnlikely() {
        return generator.generate();
    }

    public static IdType findFor(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("null object can not be converted.");
        }
        if (id instanceof Number) {
            return LONG;
        }
        try {
            java.util.UUID.fromString(id.toString());
            return UUID;
        } catch (IllegalArgumentException ex) {
            // Not a uuid
        }
        if (id instanceof String) {
            return STRING;
        }
        throw new IllegalArgumentException("Can not detect id type from " + id);
    }

    private interface RandomGenerator {

        public Object generate();
    }
}
