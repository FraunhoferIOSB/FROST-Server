/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A wrapper for an Object array holding primary key values. This class
 * implements equals, which an array does not.
 */
public class PkValue implements Iterable<Object> {

    private final Object[] values;

    public PkValue(int size) {
        this.values = new Object[size];
    }

    public PkValue(Object[] value) {
        this.values = value;
    }

    public Object get(int idx) {
        return values[idx];
    }

    public PkValue set(int idx, Object value) {
        this.values[idx] = value;
        return this;
    }

    public String getUrl(PrimaryKey pk) {
        return UrlHelper.quoteForUrl(pk, this);
    }

    public boolean isFullySet() {
        for (var value : values) {
            if (value == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isFullyUnSet() {
        for (var value : values) {
            if (value != null) {
                return false;
            }
        }
        return true;
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
        final PkValue other = (PkValue) obj;
        return Arrays.equals(this.values, other.values);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return 41 * hash + Arrays.hashCode(values);
    }

    public int size() {
        return values.length;
    }

    @Override
    public Iterator<Object> iterator() {
        return Arrays.stream(values).iterator();
    }

    public static PkValue of(Object... value) {
        for (var item : value) {
            if (item instanceof PkValue) {
                throw new IllegalArgumentException("Wrapping a PkValue in a PkValue!");
            }
        }
        return new PkValue(value);
    }

}
