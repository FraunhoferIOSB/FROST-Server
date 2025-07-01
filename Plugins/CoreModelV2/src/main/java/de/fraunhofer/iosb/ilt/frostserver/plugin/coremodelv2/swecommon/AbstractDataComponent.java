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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.Objects;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Swe-Common AbstractDataComponent.
 *
 * @param <T> The type of the extending class.
 * @param <V> The type of the Value field.
 */
public abstract class AbstractDataComponent<T extends AbstractDataComponent<T, V>, V> extends AbstractSWEIdentifiable<T> {

    /**
     * The name of the component when used as a field in a DataRecord.
     */
    private String name;

    /**
     * Flag indicating this property should be treated as a secret and values
     * should be encrypted when possible.
     */
    private boolean secret;

    /**
     * A scoped name that maps to a controlled term defined in a (web
     * accessible) dictionary, registry or ontology.
     */
    private String definition;

    /**
     * A flag indicating if the component value can be omitted.
     */
    private boolean optional;

    /**
     * A flag indicating if the component value is fixed or can be updated.
     */
    private boolean updatable;

    public String getDefinition() {
        return definition;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.definition);
        hash = 29 * hash + (this.optional ? 1 : 0);
        hash = 29 * hash + (this.updatable ? 1 : 0);
        hash = 29 * hash + super.hashCode();
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
        final AbstractDataComponent other = (AbstractDataComponent) obj;
        if (this.optional != other.optional) {
            return false;
        }
        if (this.updatable != other.updatable) {
            return false;
        }
        if (!Objects.equals(this.definition, other.definition)) {
            return false;
        }
        return super.equals(obj);
    }

    public String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return self();
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isOptional() {
        return optional;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isUpdatable() {
        return updatable;
    }

    /**
     * Checks if any set values are valid for any set constraints.
     *
     * @return true if the values are valid.
     */
    public abstract boolean valueIsValid();

    /**
     * Get the value of this DataComponent.
     *
     * @return The value of this DataComponent.
     */
    public abstract V getValue();

    public abstract T setValue(V value);

    /**
     * Validate the given value against this component.
     *
     * @param input the value to validate.
     * @return true if the value has the correct class and value.
     */
    public abstract boolean validate(Object input);

    /**
     * Validate the given value against this component.
     *
     * @param input the value to validate.
     * @return true if the value has the correct type and value.
     */
    public abstract boolean validate(JsonNode input);

    public T setDefinition(String definition) {
        this.definition = definition;
        return self();
    }

    public T setOptional(boolean optional) {
        this.optional = optional;
        return self();
    }

    public T setUpdatable(boolean updatable) {
        this.updatable = updatable;
        return self();
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isSecret() {
        return secret;
    }

    public T setSecret(boolean secret) {
        this.secret = secret;
        return self();
    }

    public void gatherDefinitions(Set<String> target) {
        if (!StringHelper.isNullOrEmpty(definition)) {
            target.add(definition);
        }
    }
}
