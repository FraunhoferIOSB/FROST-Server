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
package de.fraunhofer.iosb.ilt.sta.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.Set;

/**
 * Interface defining basic methods of an Entity.
 *
 * @author jab
 * @param <T> The exact type of the entity.
 */
public interface Entity<T extends Entity<T>> extends NavigableElement {

    public Id getId();

    public void setId(Id id);

    public String getSelfLink();

    public void setSelfLink(String selfLink);

    /**
     * @return The type of this entity.
     */
    @JsonIgnore
    public EntityType getEntityType();

    /**
     * Get the list of names of properties that should be serialised.
     *
     * @return The list of property names that should be serialised when
     * converting this Entity to JSON.
     */
    public Set<String> getSelectedPropertyNames();

    /**
     * Set the names of the properties that should be serialised.
     *
     * @param selectedProperties the names of the properties that should be
     * serialised.
     */
    public void setSelectedPropertyNames(Set<String> selectedProperties);

    /**
     * Set the properties that should be serialised.
     *
     * @param selectedProperties the properties that should be serialised.
     */
    public void setSelectedProperties(Set<Property> selectedProperties);

    /**
     * Returns true if the property is explicitly set to a value, even if this
     * value is null.
     *
     * @param property the property to check.
     * @return true if the property is explicitly set.
     */
    public boolean isSetProperty(Property property);

    public Object getProperty(Property property);

    public void setProperty(Property property, Object value);

    public void unsetProperty(Property property);

    /**
     * Toggle all Entity Properties to "set".
     */
    public default void setEntityPropertiesSet() {
        setEntityPropertiesSet(true);
    }

    /**
     * Toggle all Entity Properties to "set" or "unset".
     *
     * @param set the flag indicating if all properties should be "set" or
     * "unset".
     */
    @JsonIgnore
    public void setEntityPropertiesSet(boolean set);

    /**
     * Toggle all Entity Properties to "set" or "unset" by checking the
     * reference entitiy to see if they changed. Any properties that are
     * different than the reference entity will be "set", all others "unset". If
     * the EntityChangedMessage is not null, the changed properties are also
     * recorded in the message.
     *
     * @param comparedTo the reference entity to compare this entity to.
     * @param message the optional (can be null) message to record changes in.
     */
    @JsonIgnore
    public void setEntityPropertiesSet(T comparedTo, EntityChangedMessage message);

    /**
     * Complete the element.
     *
     * @param containingSet The pathElement of the set this entity will belong
     * to. This pathElement and any of its parents will be used to supply
     * additional information to the element.
     *
     * @throws IncompleteEntityException If the entity can not be completed.
     * @throws IllegalStateException If the containing set is not of the type
     * that can contain this entity.
     */
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException;

    /**
     * Checks if all required properties are non-null.
     *
     * @throws IncompleteEntityException If any of the required properties are
     * null.
     * @throws IllegalStateException If any of the required properties are
     * incorrect (i.e. Observation with both a Datastream and a MultiDatastream.
     */
    public default void complete() throws IncompleteEntityException {
        complete(false);
    }

    /**
     * Checks if all required properties are non-null.
     *
     * @param entityPropertiesOnly flag indicating only the EntityProperties
     * should be checked.
     * @throws IncompleteEntityException If any of the required properties are
     * null.
     * @throws IllegalStateException If any of the required properties are
     * incorrect (i.e. Observation with both a Datastream and a MultiDatastream.
     */
    public default void complete(boolean entityPropertiesOnly) throws IncompleteEntityException {
        EntityType type = getEntityType();
        for (Property property : type.getPropertySet()) {
            if (entityPropertiesOnly && !(property instanceof EntityProperty)) {
                continue;
            }
            if (type.isRequired(property) && !isSetProperty(property)) {
                throw new IncompleteEntityException("Missing required property '" + property.getJsonName() + "'");
            }
        }
    }

    /**
     *
     * @return The resource path pointing to this entity.
     */
    @JsonIgnore
    public default ResourcePath getPath() {
        EntityType type = getEntityType();
        EntityPathElement epe = new EntityPathElement();
        epe.setEntityType(type);
        epe.setId(getId());
        ResourcePath resourcePath = new ResourcePath();
        resourcePath.addPathElement(epe, true, false);
        return resourcePath;
    }
}
