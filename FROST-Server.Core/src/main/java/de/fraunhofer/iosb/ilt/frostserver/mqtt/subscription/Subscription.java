/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
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
    String formatMessage(Entity entity) throws IOException;

    /**
     * Get the type of entity that is of interest for this Subscription.
     *
     * @return the type of entity that is of interest for this Subscription.
     */
    EntityType getEntityType();

    /**
     * Get the topic of the Subscription.
     *
     * @return The topic of the Subscription.
     */
    String getTopic();

    /**
     * Check of the given entity is of interest to this Subscription.
     *
     * @param persistenceManager The PersistenceManager to use for queries.
     * @param newEntity The entity to check.
     * @param fields The fields of the entity that changed.
     * @return true if the change is of interest for the Subscription.
     */
    boolean matches(PersistenceManager persistenceManager, Entity newEntity, Set<Property> fields);

}
