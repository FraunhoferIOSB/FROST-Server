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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.simple;

import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.constraint.AllowedValues;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * SWE Count class.
 */
public class Count extends AbstractSimpleComponent<Count, Number> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Count.class.getName());

    /**
     * Value
     *
     * an integer that must be within one of the constraint intervals or exactly
     * one of the enumerated values.
     */
    private Number value;

    /**
     * Constraint
     *
     * A list of inclusive intervals and/or single values.
     */
    private AllowedValues constraint;

    public AllowedValues getConstraint() {
        return constraint;
    }

    public void setConstraint(AllowedValues constraint) {
        this.constraint = constraint;
    }

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public Count setValue(Number value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean valueIsValid() {
        return validate(value);
    }

    @Override
    public boolean validate(Object input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (input instanceof JsonNode j) {
            return validate(j);
        }
        if (input instanceof Number n) {
            return validate(n);
        }
        LOGGER.debug("Non-integral value {} for Count.", input);
        return false;
    }

    @Override
    public boolean validate(JsonNode input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (!input.isIntegralNumber()) {
            LOGGER.debug("Non-integral value {} for Count.", input);
            return false;
        }
        return validate(input.bigIntegerValue());

    }

    public boolean validate(Number input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (input instanceof Double || input instanceof Float || input instanceof BigDecimal) {
            if (input.doubleValue() != input.longValue()) {
                LOGGER.debug("Non-integer value {} for Count!", input);
                return false;
            }
        }
        if (constraint == null) {
            return true;
        }
        if (input instanceof BigInteger bi) {
            return constraint.isValid(new BigDecimal(bi));
        } else if (input instanceof BigDecimal bd) {
            return constraint.isValid(bd);
        } else {
            return constraint.isValid(new BigDecimal(input.longValue()));
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.value);
        hash = 79 * hash + Objects.hashCode(this.constraint);
        hash = 79 * hash + super.hashCode();
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
        final Count other = (Count) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.constraint, other.constraint)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    protected Count self() {
        return this;
    }

}
