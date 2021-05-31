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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import java.util.Objects;

/**
 *
 * @author jab
 */
public class IdLong implements Id {

    /**
     * The KEY used to identify the persistence type of this ID implementation.
     */
    public static final String PERSISTENCE_TYPE_INTEGER = "INTEGER";

    private Long value;

    public IdLong(Long value) {
        this.value = value;
    }

    public IdLong(int value) {
        this.value = Long.valueOf(value);
    }

    @Override
    public String getBasicPersistenceType() {
        return PERSISTENCE_TYPE_INTEGER;
    }

    @Override
    public Object asBasicPersistenceType() {
        return value;
    }

    @Override
    public void fromBasicPersitenceType(Object data) {
        value = Long.parseLong(data.toString());
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public String getUrl() {
        return toString();
    }

    @Override
    public String getJson() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IdLong other = (IdLong) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Objects.toString(getValue());
    }

}
