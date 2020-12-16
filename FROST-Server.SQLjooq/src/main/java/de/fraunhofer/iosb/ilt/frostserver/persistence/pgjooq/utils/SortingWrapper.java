/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils;

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
    public int compareTo(SortingWrapper<O, T> other) {
        return order.compareTo(other.order);
    }

}
