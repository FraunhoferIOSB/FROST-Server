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
 * SWE Class QuantityRange.
 */
public class QuantityRange extends AbstractRange<QuantityRange, BigDecimal> implements Quality<QuantityRange, List<BigDecimal>> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityRange.class);

    /**
     * Unit of Measure
     *
     * The units of the value of this Quantity.
     */
    private String uom;

    /**
     * Constraint
     *
     * A limited list of possible values.
     */
    private AllowedValues constraint;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.uom);
        hash = 67 * hash + Objects.hashCode(this.constraint);
        hash = 67 * hash + super.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final QuantityRange other = (QuantityRange) obj;
        if (!Objects.equals(this.uom, other.uom)) {
            return false;
        }
        if (!Objects.equals(this.constraint, other.constraint)) {
            return false;
        }
        return super.equals(obj);
    }

    public String getUom() {
        return uom;
    }

    public QuantityRange setUom(String uom) {
        this.uom = uom;
        return this;
    }

    public AllowedValues getConstraint() {
        return constraint;
    }

    public QuantityRange setConstraint(AllowedValues constraint) {
        this.constraint = constraint;
        return this;
    }

    @Override
    protected boolean validateArray(JsonNode input) {
        if (constraint == null) {
            return true;
        }
        for (JsonNode item : input) {
            if (!item.isNumber()) {
                LOGGER.debug("Non-number value {} for CountRange.", input);
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
    public boolean validate(List<BigDecimal> input) {
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
        for (BigDecimal item : input) {
            if (!constraint.isValid(item)) {
                LOGGER.error("Item '{}' does not fit the constraint", item);
                return false;
            }
        }
        return true;
    }

    @Override
    protected QuantityRange self() {
        return this;
    }

}
