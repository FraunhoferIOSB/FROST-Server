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
package de.fraunhofer.iosb.ilt.frostserver.model;

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NamedDsHoldingEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import java.util.Objects;

/**
 *
 * @author jab, scf
 */
public class ObservedProperty extends NamedDsHoldingEntity<ObservedProperty> {

    public static final TypeReference<ObservedProperty> TYPE_REFERENCE_OBSERVEDPROPERTY = new TypeReference<ObservedProperty>() {
        // Empty on purpose.
    };

    private String definition;

    private boolean setDefinition;

    public ObservedProperty() {
    }

    public ObservedProperty(Id id) {
        super(id);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.OBSERVEDPROPERTY;
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setDefinition = set;
    }

    @Override
    public void setEntityPropertiesSet(ObservedProperty comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        if (!Objects.equals(definition, comparedTo.getDefinition())) {
            setDefinition = true;
            message.addEpField(EntityPropertyMain.DEFINITION);
        } else {
            setDefinition = false;
        }
    }

    public String getDefinition() {
        return definition;
    }

    public ObservedProperty setDefinition(String definition) {
        this.definition = definition;
        setDefinition = definition != null;
        return this;
    }

    public boolean isSetDefinition() {
        return setDefinition;
    }

    @Override
    protected ObservedProperty getThis() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), definition);
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
        final ObservedProperty other = (ObservedProperty) obj;
        return super.equals(other)
                && Objects.equals(definition, other.definition);
    }

}
