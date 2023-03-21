/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author jab
 */
public class PropertySubscription extends AbstractSubscription {

    private Property property;
    private Predicate<? super Entity> matcher;
    private Query query;

    public PropertySubscription(String topic, ResourcePath path, CoreSettings settings) {
        super(topic, path, settings);
        init();
    }

    private void init() {
        if (!SubscriptionFactory.getQueryFromTopic(topic).isEmpty()) {
            throw new IllegalArgumentException("Invalid subscription to: '" + topic + "': query options not allowed for subscription on a property.");
        }
        final int size = path.size();
        entityType = ((PathElementEntity) path.get(size - 2)).getEntityType();
        property = ((PathElementProperty) path.get(size - 1)).getProperty();
        if (path.getIdentifiedElement() != null) {
            Id id = path.getIdentifiedElement().getId();
            matcher = x -> x.getProperty(entityType.getPrimaryKey()).equals(id);
        }
        query = new Query(modelRegistry, queryDefaults, path);
        query.addSelect(property);
        generateFilter(2);
    }

    @Override
    public boolean matches(PersistenceManager persistenceManager, Entity newEntity, Set<Property> fields) {
        if (matcher != null && !matcher.test(newEntity)) {
            return false;
        }
        if (fields == null || !fields.contains(property)) {
            return false;
        }

        return super.matches(persistenceManager, newEntity, fields);
    }

    @Override
    public String doFormatMessage(Entity entity) throws IOException {
        entity.setQuery(query);
        return JsonWriter.writeEntity(entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), property);
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
        final PropertySubscription other = (PropertySubscription) obj;
        return super.equals(obj)
                && Objects.equals(this.property, other.property);
    }

}
