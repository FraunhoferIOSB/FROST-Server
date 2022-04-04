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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeObject;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserHelper;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Value;
import java.util.Objects;

/**
 *
 * @author jab, scf
 * @param <T> The type of the constant value.
 */
public abstract class Constant<T> implements Value {

    protected T value;

    protected Constant() {
        // Nothing here
    }

    protected Constant(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public void validate(ParserHelper helper, EntityType type) {
        // Nothing to validate by default.
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
        final Constant<?> other = (Constant<?>) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public static Constant of(Object object) {
        if (object instanceof Boolean) {
            return new BooleanConstant((Boolean) object);
        }
        if (object instanceof Id) {
            return new IdConstant((Id) object);
        }
        if (object instanceof Integer) {
            return new IntegerConstant((Integer) object);
        }
        if (object instanceof Long) {
            return new IntegerConstant((Long) object);
        }
        if (object instanceof TimeObject) {
            return new TimeObjectConstant((TimeObject) object);
        }
        throw new IllegalArgumentException("Can not make constant of " + object);
    }
}
