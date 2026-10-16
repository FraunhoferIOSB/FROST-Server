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

import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.constraint.AllowedTokens;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * SWE Text class.
 */
public class Text extends AbstractSimpleComponent<Text, String> implements Quality<Text, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Text.class.getName());

    /**
     * Constraint
     *
     * The constraints put on the value of this component.
     */
    private AllowedTokens constraint;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.constraint);
        hash = 59 * hash + super.hashCode();
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
        final Text other = (Text) obj;
        return Objects.equals(this.constraint, other.constraint);
    }

    public AllowedTokens getConstraint() {
        return constraint;
    }

    public Text setConstraint(AllowedTokens constraint) {
        this.constraint = constraint;
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
        if (input instanceof String s) {
            return validate(s);
        }
        LOGGER.debug("Non-String value {} for Text.", input);
        return false;
    }

    @Override
    public boolean validate(JsonNode input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (!input.isString()) {
            LOGGER.debug("Non-Text value {} for Text.", input);
            return false;
        }
        return validate(input.asString());
    }

    public boolean validate(String input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (constraint == null) {
            return true;
        }
        return constraint.isValid(input);
    }

    @Override
    protected Text self() {
        return this;
    }

}
