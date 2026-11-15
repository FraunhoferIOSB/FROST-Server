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

import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.Subscription;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.Exceptions;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.List;

/**
 * Builds MCP subscriptions from tool arguments: parses path and query with the
 * STA parsers, applies the subscription gates ($filter/$select only, on
 * entity-set paths; no options at all on entity paths; property paths are
 * rejected), builds the canonical topic used for per-session deduplication, and
 * chooses the anonymous or fine-grained-authorisation constructor variant.
 * Parse and gate failures surface as IllegalArgumentException for the tool
 * layer to map to MCP -32602.
 */
public class SubscriptionFactory {

    private final CoreSettings coreSettings;

    /**
     * Creates a new factory.
     *
     * @param coreSettings The server settings.
     */
    public SubscriptionFactory(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }

    /**
     * Builds a subscription.
     *
     * @param principal The principal of the subscribing session. Anonymous
     * principals get the plain constructor (no fine-grained checks); named
     * principals get per-event visibility checks.
     * @param version The resolved STA version. May not be null.
     * @param staPath The STA path, relative to the version root.
     * @param filter The raw $filter expression, without prefix. May be null.
     * @param select The $select items. May be null or empty.
     * @return the subscription, with the canonical topic set.
     * @throws IllegalArgumentException on parse or gate failures.
     */
    public Subscription create(PrincipalExtended principal, Version version, String staPath, String filter, List<String> select) {
        Exceptions.illegalArgumentIf(StringHelper.isNullOrEmpty(staPath), "Missing subscription path.");

        final boolean anonymous = isAnonymous(PrincipalExtended.fromPrincipal(principal));
        principal = PrincipalExtended.fromPrincipal(principal);

        // Subscriptions never page, so the query must not be polluted with the
        // default $orderby fields that alwaysOrderbyId adds on validate; the
        // query gates below reject any $orderby entries.
        final QueryDefaults queryDefaults = coreSettings.getQueryDefaults().copy().setAlwaysOrder(false);
        final ServiceContext context = new ServiceContext()
                .setFunctionRegistry(coreSettings.getFunctionRegistry())
                .setModelRegistry(coreSettings.getModelRegistry())
                .setQueryDefaults(queryDefaults)
                .setPrefixGen(() -> queryDefaults.getServiceRootUrl() + '/' + version.urlPart + '/');

        final ResourcePath path = PathParser.parsePath(context, version, staPath, principal);
        final PathElement lastElement = path.getLastElement();
        Exceptions.illegalArgumentIf(
                lastElement instanceof PathElementProperty,
                "Invalid subscription to: '{}': property subscriptions are not supported; subscribe to the entity instead.",
                staPath);

        final String decodedQuery = assembleQuery(filter, select);
        Query query = null;
        if (!StringHelper.isNullOrEmpty(decodedQuery)) {
            query = QueryParser.parseQuery(decodedQuery, context, path, PrincipalExtended.ANONYMOUS_PRINCIPAL).validate();
        }

        final String queryPart = query == null ? "" : "?" + query.toUnencodedString(false);
        final String canonicalTopic = version.urlPart + "/" + path.getUrl() + queryPart;

        if (lastElement instanceof PathElementEntitySet) {
            return anonymous
                    ? new EntitySetSubscription(coreSettings, canonicalTopic, path, query)
                    : new EntitySetSubscription(coreSettings, principal, canonicalTopic, path, query);
        } else if (lastElement instanceof PathElementEntity) {
            Exceptions.illegalArgumentIf(
                    query != null,
                    "Invalid subscription to: '{}': query options are not allowed for subscription on an entity.",
                    staPath);
            return anonymous
                    ? new EntitySubscription(coreSettings, canonicalTopic, path)
                    : new EntitySubscription(coreSettings, principal, canonicalTopic, path);
        }
        throw new IllegalArgumentException("Invalid subscription to: '" + staPath + "': path must be an entity set or an entity.");
    }

    private static boolean isAnonymous(PrincipalExtended principal) {
        return principal == PrincipalExtended.ANONYMOUS_PRINCIPAL
                || StringHelper.isNullOrEmpty(principal.getName())
                || PrincipalExtended.USER_NAME_ANONYMOUS.equals(principal.getName());
    }

    /**
     * Assembles the raw, decoded query string from the typed arguments, in the
     * same shape frost_query uses (entries joined with '&').
     *
     * @param filter The $filter expression, without prefix. May be null.
     * @param select The $select items. May be null or empty.
     * @return the query string, or null when no options are present.
     */
    private String assembleQuery(String filter, List<String> select) {
        final StringBuilder sb = new StringBuilder();
        if (!StringHelper.isNullOrEmpty(filter)) {
            sb.append("$filter=").append(filter);
        }
        if (select != null && !select.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append("$select=").append(String.join(",", select));
        }
        return sb.isEmpty() ? null : sb.toString();
    }

}
