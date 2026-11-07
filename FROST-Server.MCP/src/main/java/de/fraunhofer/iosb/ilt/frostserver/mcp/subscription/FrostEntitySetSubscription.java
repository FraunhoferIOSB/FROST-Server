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
package de.fraunhofer.iosb.ilt.frostserver.mcp.subscription;

import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_DEFAULT;
import static de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended.ANONYMOUS_PRINCIPAL;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.AbstractSubscription;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.Exceptions;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;

/**
 * An MCP subscription on an entity set, including navigation-property
 * addresses like Things(1)/Datastreams. Unlike the MQTT variant it always
 * allows $filter and $select, and never allows $count/$skip/$top/$orderby
 * and $expand. The query is supplied pre-parsed by the
 * FrostSubscriptionFactory; this class only applies the subscription gates
 * and builds the matcher infrastructure.
 */
public class FrostEntitySetSubscription extends AbstractSubscription {

    private final Query query;

    /**
     * Create a new subscription.
     *
     * @param settings The server settings.
     * @param topic The canonical topic: version path plus the validated query
     * string, or null when the query carries no options.
     * @param path The parsed path. The last element must be an entity set.
     * @param query The parsed (validated) query carrying the agent's options,
     * or null when there are none.
     */
    public FrostEntitySetSubscription(CoreSettings settings, String topic, ResourcePath path, Query query) {
        super(settings, topic, path);
        this.query = query;
        init();
    }

    /**
     * Create a new subscription for an authenticated user, with fine-grained
     * authorisation checks.
     *
     * @param settings The server settings.
     * @param userPrincipal The principal of the subscribing user.
     * @param topic The canonical topic: version path plus the validated query
     * string, or null when the query carries no options.
     * @param path The parsed path. The last element must be an entity set.
     * @param query The parsed (validated) query carrying the agent's options,
     * or null when there are none.
     */
    public FrostEntitySetSubscription(CoreSettings settings, PrincipalExtended userPrincipal, String topic, ResourcePath path, Query query) {
        super(settings, userPrincipal, topic, path);
        this.query = query;
        init();
    }

    private void init() {
        Exceptions.illegalArgumentIf(path.isEmpty(), "Path is empty!");
        final PathElement lastElement = path.getLastElement();
        Exceptions.illegalArgumentIf(!(lastElement instanceof PathElementEntitySet), "Path is not an EntitySet!");
        entityType = ((PathElementEntitySet) lastElement).getEntityType();

        Expression filter = null;
        if (query != null) {
            if (query.getCount().isPresent()
                    || !query.getOrderBy().isEmpty()
                    || query.getSkip().isPresent()
                    || query.getTop().isPresent()) {
                throw new IllegalArgumentException("Invalid subscription: $count, $skip, $top and $orderby are not allowed in query options.");
            }
            if (!query.getExpand().isEmpty()) {
                throw new IllegalArgumentException("Invalid subscription: $expand is not allowed in query options.");
            }
            filter = query.getFilter();
        }
        generateFilter(1, filter);
    }

    @Override
    public String doFormatMessage(Entity entity) {
        PrincipalExtended oldLocalPrincipal = PrincipalExtended.getLocalPrincipal();
        try {
            PrincipalExtended.setLocalPrincipal(userPrincipal);
            final Query formatQuery = query == null
                    ? new Query(context, path, ANONYMOUS_PRINCIPAL)
                            .setMetadata(Metadata.MINIMAL_WITH_ID)
                            .validate()
                    : query.setMetadata(Metadata.MINIMAL_WITH_ID);
            entity.setQuery(formatQuery);
            return settings.getFormatter(formatQuery.getVersion(), FORMAT_NAME_DEFAULT).format(path, formatQuery, entity).getFormatted();
        } catch (IncorrectRequestException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            PrincipalExtended.setLocalPrincipal(oldLocalPrincipal);
        }
    }

}
