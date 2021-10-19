/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import static de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatDefault.DEFAULT_FORMAT_NAME;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author jab
 */
public class EntitySubscription extends AbstractSubscription {

    private Query emptyQuery;

    private Predicate<? super Entity> matcher;

    public EntitySubscription(CoreSettings settings, String topic, ResourcePath path) {
        super(topic, path, settings);
        this.settings = settings;
        init();
    }

    private void init() {
        emptyQuery = new Query(settings.getModelRegistry(), settings.getQueryDefaults(), path).validate();
        if (!SubscriptionFactory.getQueryFromTopic(topic).isEmpty()) {
            throw new IllegalArgumentException("Invalid subscription to: '" + topic + "': query options not allowed for subscription on an entity.");
        }
        entityType = ((PathElementEntity) path.getLastElement()).getEntityType();
        final int size = path.size();
        if (size == 2 && path.get(0) instanceof PathElementEntitySet) {
            Id id = ((PathElementEntity) path.getLastElement()).getId();
            matcher = x -> x.getProperty(entityType.getPrimaryKey()).equals(id);
        }
        generateFilter(1);
    }

    @Override
    public boolean matches(PersistenceManager persistenceManager, Entity newEntity, Set<Property> fields) {
        if (matcher != null && !matcher.test(newEntity)) {
            return false;
        }
        return super.matches(persistenceManager, newEntity, fields);
    }

    @Override
    public String doFormatMessage(Entity entity) throws IOException {
        try {
            entity.setQuery(emptyQuery);
            return settings.getFormatter(DEFAULT_FORMAT_NAME).format(path, emptyQuery, entity, true).getFormatted();
        } catch (IncorrectRequestException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
