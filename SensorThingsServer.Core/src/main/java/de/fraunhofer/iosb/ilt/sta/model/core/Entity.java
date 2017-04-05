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
import de.fraunhofer.iosb.ilt.sta.model.id.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;

/**
 * Interface defining basic methods of an Entity.
 *
 * @author jab
 */
public interface Entity extends NavigableElement {

    public Id getId();

    public void setId(Id id);

    public String getSelfLink();

    public void setSelfLink(String selfLink);

    /**
     * @return The type of this entity.
     */
    @JsonIgnore
    public EntityType getEntityType();

    public Object getProperty(Property property);

    public void setProperty(Property property, Object value);

    public void unsetProperty(Property property);

    /**
     * Toggle all Entity Properties to "set".
     */
    public void setEntityPropertiesSet();

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
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException, IllegalStateException;

    /**
     * Checks if all required properties are non-null.
     *
     * @throws IncompleteEntityException If any of the required properties are
     * null.
     * @throws IllegalStateException If any of the required properties are
     * incorrect (i.e. Observation with both a Datastream and a MultiDatastream.
     */
    public default void complete() throws IncompleteEntityException, IllegalStateException {
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
    public default void complete(boolean entityPropertiesOnly) throws IncompleteEntityException, IllegalStateException {
        EntityType type = getEntityType();
        for (Property property : type.getPropertySet()) {
            if (entityPropertiesOnly && !(property instanceof EntityProperty)) {
                continue;
            }
            if (type.isRequired(property)) {
                Object value = getProperty(property);
                if (value == null) {
                    throw new IncompleteEntityException("Missing required property '" + property + "'");
                }
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
        resourcePath.getPathElements().add(epe);
        resourcePath.setMainElement(epe);
        return resourcePath;
    }
}
