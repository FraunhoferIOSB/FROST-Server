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

import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_DEFAULT;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class EntitySetSubscription extends AbstractSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntitySetSubscription.class);
    private Query query;
    private Query expandQuery;

    public EntitySetSubscription(CoreSettings settings, String topic, ResourcePath path) {
        super(topic, path, settings);
        init();
    }

    private void init() {
        entityType = ((PathElementEntitySet) path.getLastElement()).getEntityType();

        String queryString = SubscriptionFactory.getQueryFromTopic(topic);
        query = parseQuery(queryString);
        Expression filter = null;
        if (query != null) {
            if (query.getCount().isPresent()
                    || !query.getOrderBy().isEmpty()
                    || query.getSkip().isPresent()
                    || query.getTop().isPresent()) {
                throw new IllegalArgumentException("Invalid subscription to: '" + topic + "': $count, $skip, $top and $orderby are not allowed in query options.");
            }
            if (query.getFilter() != null && !settings.getMqttSettings().isAllowMqttFilter()) {
                throw new IllegalArgumentException("Invalid subscription to: '" + topic + "': $filter is not allowed in query options.");
            }
            filter = query.getFilter();
            if (!query.getExpand().isEmpty()) {
                Query queryCopy = parseQuery(queryString);
                if (queryCopy != null) {
                    List<Expand> expandList = queryCopy.getExpand();
                    expandQuery = new Query(modelRegistry, queryDefaults, queryCopy.getPath())
                            .setExpand(expandList)
                            .addSelect(entityType.getPrimaryKey().getKeyProperties());
                }
            }
        }
        generateFilter(1, filter);
    }

    private Query parseQuery(String topic) {
        String queryString = null;
        queryString = URLDecoder.decode(topic, StringHelper.UTF8);
        try {
            return QueryParser.parseQuery(queryString, queryDefaults, modelRegistry, path).validate();
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid query: {} ERROR: {}", queryString, e.getMessage());
            return null;
        }
    }

    @Override
    public String doFormatMessage(Entity entity) {
        try {
            entity.setQuery(query);
            return settings.getFormatter(query.getVersion(), FORMAT_NAME_DEFAULT).format(path, query, entity, true).getFormatted();
        } catch (IncorrectRequestException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Entity fetchExpand(PersistenceManager persistenceManager, Entity newEntity) {
        if (expandQuery != null) {
            ResourcePath resourcePath = newEntity.getPath()
                    .setVersion(expandQuery.getVersion())
                    .setServiceRootUrl(expandQuery.getServiceRootUrl());
            Object expandEntity = persistenceManager.get(resourcePath, expandQuery);

            if (expandEntity instanceof Entity entity) {
                Set<EntityPropertyMain> fields = entity.getEntityType().getEntityProperties();
                fields.forEach(field -> entity.setProperty(field, newEntity.getProperty(field)));
                entity.setQuery(query);
                return entity;
            }
        }
        return super.fetchExpand(persistenceManager, newEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), query);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final EntitySetSubscription other = (EntitySetSubscription) obj;
        return Objects.equals(this.query, other.query);
    }

}
