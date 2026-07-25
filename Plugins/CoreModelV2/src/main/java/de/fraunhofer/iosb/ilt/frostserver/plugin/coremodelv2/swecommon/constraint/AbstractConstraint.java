/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.constraint;

import java.util.List;
import java.util.Objects;

/**
 * The abstract class for constraints.
 *
 * @param <T> The type of the extending class.
 * @param <V> The value type of the constraint.
 */
public abstract class AbstractConstraint<T extends AbstractConstraint<T, V>, V> {

    /**
     * Values
     *
     * The values that the user can choose from.
     */
    private List<V> values;

    public final List<V> getValues() {
        return values;
    }

    public final T setValues(List<V> values) {
        this.values = values;
        return self();
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
        final AbstractConstraint<?, ?> other = (AbstractConstraint<?, ?>) obj;
        return Objects.equals(this.values, other.values);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.values);
        return hash;
    }

    protected abstract T self();
}
