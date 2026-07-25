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
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * SWE Category class.
 */
public class Category extends AbstractSimpleComponent<Category, String> implements Quality<Category, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Category.class.getName());

    /**
     * Constraint
     *
     * A limited list of possible values.
     */
    private AllowedTokens constraint;

    //TODO
    private Map<String, String> codeSpace;

    public AllowedTokens getConstraint() {
        return constraint;
    }

    public Category setConstraint(AllowedTokens constraint) {
        this.constraint = constraint;
        return this;
    }

    @Override
    public boolean validate(Object input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (input instanceof String s) {
            return validate(s);
        }
        LOGGER.debug("Value is not a String: {}", input);
        return false;
    }

    @Override
    public boolean validate(JsonNode input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (!input.isString()) {
            LOGGER.debug("Given value is not textual: {}", input);
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
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.constraint);
        hash = 17 * hash + Objects.hashCode(this.codeSpace);
        hash = 17 * hash + super.hashCode();
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
        final Category other = (Category) obj;
        if (!Objects.equals(this.constraint, other.constraint)) {
            return false;
        }
        return Objects.equals(this.codeSpace, other.codeSpace);
    }

    @Override
    protected Category self() {
        return this;
    }

}
