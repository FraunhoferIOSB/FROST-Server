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
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * SWE Class CountRange.
 */
public class CountRange extends AbstractRange<CountRange, Long> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CountRange.class);

    /**
     * Value
     *
     * The starting end ending values of this CountRange.
     */
    private List<Long> value;

    /**
     * Constraint
     *
     * A limited list of possible values.
     */
    private AllowedValues constraint;

    @Override
    public List<Long> getValue() {
        return value;
    }

    @Override
    public CountRange setValue(List<Long> value) {
        if (value.size() != 2) {
            throw new IllegalArgumentException("CountRange must have a value with exactly 2 values.");
        }
        this.value = value;
        return this;
    }

    public AllowedValues getConstraint() {
        return constraint;
    }

    public CountRange setConstraint(AllowedValues constraint) {
        this.constraint = constraint;
        return this;
    }

    @Override
    public boolean valueIsValid() {
        return validate(value);
    }

    @Override
    protected boolean validateArray(JsonNode input) {
        if (constraint == null) {
            return true;
        }
        for (JsonNode item : input) {
            if (!item.isIntegralNumber()) {
                LOGGER.debug("Non-integral value {} for CountRange.", input);
                return false;
            }
            if (!constraint.isValid(item.decimalValue())) {
                LOGGER.error("Item '{}' does not fit the constraint", item);
                return false;
            }
        }
        return true;

    }

    @Override
    public boolean validate(List<Long> input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        final int size = input.size();
        if (size != 2) {
            LOGGER.debug("Range must have 2 items, found: {}", size);
            return false;
        }
        if (constraint == null) {
            return true;
        }
        for (Long item : input) {
            if (!constraint.isValid(new BigDecimal(item))) {
                LOGGER.error("Item '{}' does not fit the constraint", item);
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final CountRange other = (CountRange) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.constraint, other.constraint)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    protected CountRange self() {
        return this;
    }

}
