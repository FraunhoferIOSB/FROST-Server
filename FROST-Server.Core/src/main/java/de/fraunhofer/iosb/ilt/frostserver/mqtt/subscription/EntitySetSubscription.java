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

import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_DEFAULT;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended.ANONYMOUS_PRINCIPAL;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.Exceptions;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.net.URLDecoder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscription on a EntitySet.
 */
public class EntitySetSubscription extends AbstractSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntitySetSubscription.class);
    private Query query;
    private Query expandQuery;

    public EntitySetSubscription(CoreSettings settings, String topic, ResourcePath path) {
        super(settings, topic, path);
        init();
    }

    public EntitySetSubscription(CoreSettings settings, PrincipalExtended userPrincipal, String topic, ResourcePath path) {
        super(settings, userPrincipal, topic, path);
        init();
    }

    private void init() {
        Exceptions.illegalArgumentIf(path.isEmpty(), "Path is empty!");
        final PathElement lastElement = path.getLastElement();
        Exceptions.illegalArgumentIf(!(lastElement instanceof PathElementEntitySet), "Path is not an EntitySet!");
        entityType = ((PathElementEntitySet) lastElement).getEntityType();

        String queryString = SubscriptionFactory.getQueryFromTopic(topic);
        query = parseQuery(queryString)
                .setMetadata(Metadata.MINIMAL_WITH_ID);
        Expression filter = null;
        if (query != null) {
            if (query.getCount().isPresent()
                    || !query.getOrderBy().isEmpty()
                    || query.getSkip().isPresent()
                    || query.getTop().isPresent()) {
                throw new IllegalArgumentException("Invalid subscription to: '" + topic + "': $count, $skip, $top and $orderby are not allowed in query options.");
            }
            if (!query.getExpand().isEmpty() && !settings.getMqttSettings().isAllowMqttExpand()) {
                throw new IllegalArgumentException("Invalid subscription to: '" + topic + "': $expand is not allowed in query options.");
            }
            if (query.getFilter() != null && !settings.getMqttSettings().isAllowMqttFilter()) {
                throw new IllegalArgumentException("Invalid subscription to: '" + topic + "': $filter is not allowed in query options.");
            }
            filter = query.getFilter();
            if (!query.getExpand().isEmpty()) {
                List<Expand> expandList = query.getExpand();
                expandQuery = new Query(query.getContext(), query.getPath(), userPrincipal)
                        .setExpand(expandList)
                        .addSelect(query.getSelect().toArray(Property[]::new))
                        .validate();
            }
        }
        generateFilter(1, filter);
    }

    private Query parseQuery(String topic) {
        String queryString;
        queryString = URLDecoder.decode(topic, StringHelper.UTF8);
        try {
            return QueryParser.parseQuery(queryString, context, path, ANONYMOUS_PRINCIPAL).validate();
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid query: {} ERROR: {}", queryString, e.getMessage());
            return new Query(context, path, ANONYMOUS_PRINCIPAL).validate();
        }
    }

    @Override
    public String doFormatMessage(Entity entity) {
        PrincipalExtended oldLocalPrincipal = PrincipalExtended.getLocalPrincipal();
        try {
            PrincipalExtended.setLocalPrincipal(userPrincipal);
            entity.setQuery(query);
            return settings.getFormatter(query.getVersion(), FORMAT_NAME_DEFAULT).format(path, query, entity).getFormatted();
        } catch (IncorrectRequestException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            PrincipalExtended.setLocalPrincipal(oldLocalPrincipal);
        }
    }

    @Override
    public Entity fetchExpand(PersistenceManager persistenceManager, Entity newEntity) {
        if (expandQuery != null) {
            ResourcePath resourcePath = newEntity.getPath()
                    .setVersion(expandQuery.getVersion());
            PrincipalExtended oldLocalPrincipal = PrincipalExtended.getLocalPrincipal();
            try {
                PrincipalExtended.setLocalPrincipal(userPrincipal);
                Object expandEntity = persistenceManager.get(resourcePath, expandQuery);
                if (expandEntity instanceof Entity entity) {
                    return entity;
                }
            } finally {
                PrincipalExtended.setLocalPrincipal(oldLocalPrincipal);
            }
        }
        return super.fetchExpand(persistenceManager, newEntity);
    }

}
