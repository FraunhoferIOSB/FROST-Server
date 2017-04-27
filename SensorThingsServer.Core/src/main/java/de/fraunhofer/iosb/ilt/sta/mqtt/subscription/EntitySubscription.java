/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.mqtt.subscription;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.sta.serialize.EntityFormatter;
import java.io.IOException;
import java.util.function.Predicate;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class EntitySubscription extends Subscription {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntitySubscription.class);
    private Predicate<? super Entity> matcher;

    public EntitySubscription(String topic, ResourcePath path, String serviceRootUrl) {
        super(topic, path, serviceRootUrl);
        init();
    }

    private void init() {
        if (!SubscriptionFactory.getQueryFromTopic(topic).isEmpty()) {
            throw new IllegalArgumentException("Invalid subscription to: '" + topic + "': query options not allowed for subscription on an entitiy.");
        }
        entityType = ((EntityPathElement) path.getLastElement()).getEntityType();
        if (path.getPathElements().size() == 2
                && path.getPathElements().get(path.getPathElements().size() - 2) instanceof EntitySetPathElement) {
            matcher = x -> x.getProperty(EntityProperty.Id).equals(((EntityPathElement) path.getLastElement()).getId());
        }
        generateFilter(1);
    }

    @Override
    public boolean matches(PersistenceManager persistenceManager, Entity oldEntity, Entity newEntity) {
        if (matcher != null) {
            if (!matcher.test(newEntity)) {
                return false;
            }
        }
        return super.matches(persistenceManager, oldEntity, newEntity);
    }

    @Override
    public String doFormatMessage(Entity entity) throws IOException {
        return new EntityFormatter().writeEntity(entity);
    }

}
