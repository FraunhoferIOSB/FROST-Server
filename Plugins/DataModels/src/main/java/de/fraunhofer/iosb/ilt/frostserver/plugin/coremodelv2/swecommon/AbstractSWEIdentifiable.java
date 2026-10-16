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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.util.SweTypeIdResolver;
import java.util.Objects;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * SWE abstract Class AbstractSWEIdentifiable.
 *
 * @param <T> The type of the extending class.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(SweTypeIdResolver.class)
public abstract class AbstractSWEIdentifiable<T extends AbstractSWEIdentifiable<T>> extends AbstractSWE {

    /**
     * Identifier
     *
     * A unique identifier.
     */
    private String identifier;

    /**
     * Label
     *
     * A short descriptive name.
     */
    private String label;

    /**
     * Description
     *
     * A longer description.
     */
    private String description;

    /**
     * The type of the component.
     */
    private String type;

    public String getDescription() {
        return description;
    }

    public T setDescription(String description) {
        this.description = description;
        return self();
    }

    public String getIdentifier() {
        return identifier;
    }

    public T setIdentifier(String identifier) {
        this.identifier = identifier;
        return self();
    }

    public String getLabel() {
        return label;
    }

    public T setLabel(String label) {
        this.label = label;
        return self();
    }

    public String getType() {
        return type;
    }

    public T setType(String type) {
        this.type = type;
        return self();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.identifier);
        hash = 67 * hash + Objects.hashCode(this.label);
        hash = 67 * hash + Objects.hashCode(this.description);
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
        final AbstractSWEIdentifiable other = (AbstractSWEIdentifiable) obj;
        if (!Objects.equals(this.identifier, other.identifier)) {
            return false;
        }
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        return Objects.equals(this.description, other.description);
    }

    protected abstract T self();

}
