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
package de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author scf
 */
public interface Subscription {

    /**
     * Format the given entity so it fits for the subscription.
     *
     * @param entity The entity to format.
     * @return A message body.
     * @throws IOException If the formatting failed.
     */
    public String formatMessage(Entity entity) throws IOException;

    /**
     * Get the type of entity that is of interest for this Subscription.
     *
     * @return the type of entity that is of interest for this Subscription.
     */
    public EntityType getEntityType();

    /**
     * Get the topic of the Subscription.
     *
     * @return The topic of the Subscription.
     */
    public String getTopic();

    /**
     * Check of the given entity is of interest to this Subscription.
     *
     * @param persistenceManager The PersistenceManager to use for queries.
     * @param newEntity The entity to check.
     * @param fields The fields of the entity that changed.
     * @return true if the change is of interest for the Subscription.
     */
    public boolean matches(PersistenceManager persistenceManager, Entity newEntity, Set<Property> fields);

    /**
     * Fetch expand relations
     *
     * @param persistenceManager The PersistenceManager to use for queries.
     * @param newEntity The entity to expand.
     * @return entity with expanded relations.
     */
    public default Entity fetchExpand(PersistenceManager persistenceManager, Entity newEntity) {
        return newEntity;
    }

    /**
     * If the subscription is over a one-to-many relation, this has a value.
     *
     * @return The relation to the parent of the main entity type of this
     * subscription, if any.
     */
    public NavigationPropertyMain getParentRelation();

    /**
     * If the subscription depends on a parent with a fixed Primary Key, this
     * returns the Primary Key that the parent must have for the subscription to
     * be matched.
     *
     * @return The Primary Key of the determining parent.
     */
    public PkValue getParentId();
}
