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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;

/**
 * Interface defining basic methods of an Entity.
 *
 * @author jab
 * @author scf
 */
public interface Entity extends NavigableElement {

    /**
     * Get the primary key definition of the (EntityType of the) Entity. This is
     * a shorthand for getEntityType().getPrimaryKey();
     *
     * @return The primary key definition of the Entity.
     */
    public PrimaryKey getPrimaryKey();

    /**
     * Key the values of the primary key fields for this Entity.
     *
     * @return the primary key values.
     */
    public PkValue getPrimaryKeyValues();

    public boolean primaryKeyFullySet();

    public Entity setPrimaryKeyValues(PkValue values);

    /**
     * @return self link. Might be null.
     */
    public String getSelfLink();

    public Entity setSelfLink(String selfLink);

    /**
     * @return The type of this entity.
     */
    @JsonIgnore
    public EntityType getEntityType();

    /**
     * Sets the type of the entity if it had not been previously set. This will
     * cause all already set properties to be validated.
     *
     * @param entityType The type of the entity to set.
     * @return this;
     */
    public Entity setEntityType(EntityType entityType);

    /**
     * Returns true if the property is explicitly set to a value, even if this
     * value is null.
     *
     * @param property the property to check.
     * @return true if the property is explicitly set.
     */
    public boolean isSetProperty(Property property);

    /**
     * Get the value of the given Property as it is set on this Entity.
     *
     * @param <P> The type of the value of the property.
     * @param property The property to get
     * @return The value of the property.
     */
    public <P> P getProperty(Property<P> property);

    /**
     * Get the value pointed to by the given Path.
     *
     * @param path The path to navigate to in the Entity.
     * @return The value for the given path, or NULL.
     */
    public Object getProperty(Path path);

    /**
     * Set the value of the given property.
     *
     * @param <P> The type of the value of the property.
     * @param property The property to set the value for.
     * @param value The value to set for the given property.
     * @return This.
     */
    public <P> Entity setProperty(Property<P> property, P value);

    public Entity unsetProperty(Property property);

    /**
     * Add the given linked Entity to the entity set identified by the given
     * navigation property.
     *
     * @param navProperty The navigation property that identifies the EntitySet
     * to add the given entity to.
     * @param linkedEntity The entity to add to the EntitySet identified by the
     * given navigation property.
     * @return this.
     */
    public Entity addNavigationEntity(NavigationPropertyEntitySet navProperty, Entity linkedEntity);

    /**
     * Toggle all Entity Properties to "set". Both EntityProperties and
     * NavigationProperties are set.
     */
    public default void setEntityPropertiesSet() {
        setEntityPropertiesSet(true, false);
    }

    /**
     * Toggle all Entity Properties to "set" or "unset".
     *
     * @param set the flag indicating if all properties should be "set" or
     * "unset".
     * @param entityPropertiesOnly flag indicating only the EntityProperties
     * should be set to "set", NavigationProperties are not changed.
     */
    @JsonIgnore
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly);

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
    public void setEntityPropertiesSet(Entity comparedTo, EntityChangedMessage message);

    /**
     * Checks if the entity is valid (complete) for creation. If all required
     * properties are non-null.
     *
     * @throws IncompleteEntityException If any of the required properties are
     * null.
     * @throws IllegalStateException If any of the required properties are
     * incorrect (i.e. Observation with both a Datastream and a MultiDatastream.
     */
    public default void validateCreate() throws IncompleteEntityException {
        getEntityType().validateCreate(this);
    }

    /**
     * Checks if the entity is valid (complete) for updates. If all required
     * properties are non-null.
     *
     * @throws IncompleteEntityException If any of the required properties are
     * null.
     * @throws IllegalStateException If any of the required properties are
     * incorrect (i.e. Observation with both a Datastream and a MultiDatastream.
     */
    public default void validateUpdate() throws IncompleteEntityException {
        getEntityType().validateUpdate(this);
    }

    /**
     * Get the query that has resulted in this Entity.
     *
     * @return the query that has resulted in this Entity.
     */
    public Query getQuery();

    /**
     * Set the query that has resulted in this Entity.
     *
     * @param query the query that has resulted in this Entity.
     * @return This entity.
     */
    public Entity setQuery(Query query);

    /**
     *
     * @return The resource path pointing to this entity.
     */
    @JsonIgnore
    public default ResourcePath getPath() {
        EntityType type = getEntityType();
        PathElementEntity epe = new PathElementEntity(type, null);
        epe.setPkValues(getPrimaryKeyValues());
        ResourcePath resourcePath = new ResourcePath();
        resourcePath.addPathElement(epe, true, false);
        return resourcePath;
    }
}
