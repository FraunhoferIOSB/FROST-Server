/*
 * Copyright (C) 2021 Meo Limited.
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
package de.fraunhofer.iosb.ilt.frostserver.query;

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;

public enum Metadata {
    FULL, MINIMAL, NONE, OFF, INTERNAL_COMPARE;

    public static final Metadata DEFAULT = Metadata.FULL;

    public static Metadata lookup(String metadata) {
        if (metadata == null) {
            return DEFAULT;
        }
        try {
            return Enum.valueOf(Metadata.class, metadata.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown metadata: " + StringHelper.cleanForLogging(metadata));
        }
    }
}
