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
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.util.UnitOfMeasurement;
import java.math.BigDecimal;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * SWE Quantity class.
 *
 * A real value that is within one of the constraint intervals or exactly one of
 * the enumerated values, and most importantly is expressed in the unit
 * specified.
 */
public class Quantity extends AbstractSimpleComponent<Quantity, Number> implements Quality<Quantity, Number> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Quantity.class.getName());

    /**
     * Constraint
     *
     * A limited list of possible values.
     */
    private AllowedValues constraint;

    /**
     * UoM
     *
     * The units of the value of this Quantity.
     */
    private UnitOfMeasurement uom;

    public Quantity setConstraint(AllowedValues constraint) {
        this.constraint = constraint;
        return this;
    }

    public Quantity setUom(UnitOfMeasurement uom) {
        this.uom = uom;
        return this;
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
        if (!input.isNumber()) {
            LOGGER.debug("Non-number value {} for Count.", input);
            return false;
        }
        return validate(input.decimalValue());
    }

    public boolean validate(Number input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (constraint == null) {
            return true;
        }
        if (input instanceof BigDecimal bd) {
            return constraint.isValid(bd);
        } else {
            return constraint.isValid(new BigDecimal(input.toString()));
        }
    }

    public UnitOfMeasurement getUom() {
        return uom;
    }

    public AllowedValues getConstraint() {
        return constraint;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.constraint);
        hash = 53 * hash + Objects.hashCode(this.uom);
        hash = 53 * hash + super.hashCode();
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
        final Quantity other = (Quantity) obj;
        if (!Objects.equals(this.uom, other.uom)) {
            return false;
        }
        return Objects.equals(this.constraint, other.constraint);
    }

    @Override
    protected Quantity self() {
        return this;
    }

}
