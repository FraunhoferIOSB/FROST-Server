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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

import java.util.Objects;

/**
 * A wrapper to sort entities that have no natural sorting order.
 *
 * @author hylke
 */
public class SortingWrapper<O extends Comparable<O>, T> implements Comparable<SortingWrapper<O, T>> {

    private final O order;
    private final T object;

    public SortingWrapper(O order, T object) {
        this.order = order;
        this.object = object;
    }

    public O getOrder() {
        return order;
    }

    public T getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.order);
        hash = 31 * hash + Objects.hashCode(this.object);
        return hash;
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
        final SortingWrapper<?, ?> other = (SortingWrapper<?, ?>) obj;
        if (!Objects.equals(this.order, other.order)) {
            return false;
        }
        return Objects.equals(this.object, other.object);
    }

    @Override
    public int compareTo(SortingWrapper<O, T> other) {
        if (order.equals(other.order)) {
            return Integer.compare(object.hashCode(), other.object.hashCode());
        }
        return order.compareTo(other.order);
    }

}
