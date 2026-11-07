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
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.AbstractSubscription;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.function.Predicate;

/**
 * An MCP subscription on a single entity, e.g. Things(1) or
 * Datastreams(2)/ObservedProperty. No query options are allowed at all. For
 * the direct two-element shape (entity set plus identified entity) a cheap
 * primary-key matcher is installed; deeper shapes fall back to the
 * match-expression machinery of the base class.
 */
public class FrostEntitySubscription extends AbstractSubscription {

    private final Query emptyQuery;
    private final Predicate<? super Entity> matcher;

    /**
     * Create a new subscription.
     *
     * @param settings The server settings.
     * @param topic The canonical topic: the version path of the entity.
     * @param path The parsed path. The last element must be an entity.
     */
    public FrostEntitySubscription(CoreSettings settings, String topic, ResourcePath path) {
        super(settings, topic, path);
        this.emptyQuery = buildEmptyQuery();
        this.matcher = buildMatcher();
        init();
    }

    /**
     * Create a new subscription for an authenticated user, with fine-grained
     * authorisation checks.
     *
     * @param settings The server settings.
     * @param userPrincipal The principal of the subscribing user.
     * @param topic The canonical topic: the version path of the entity.
     * @param path The parsed path. The last element must be an entity.
     */
    public FrostEntitySubscription(CoreSettings settings, PrincipalExtended userPrincipal, String topic, ResourcePath path) {
        super(settings, userPrincipal, topic, path);
        this.emptyQuery = buildEmptyQuery();
        this.matcher = buildMatcher();
        init();
    }

    private Query buildEmptyQuery() {
        return new Query(context, path, ANONYMOUS_PRINCIPAL)
                .setMetadata(Metadata.MINIMAL_WITH_ID)
                .validate();
    }

    private Predicate<? super Entity> buildMatcher() {
        final int size = path.size();
        if (size == 2 && path.get(0) instanceof PathElementEntitySet) {
            final PkValue id = ((PathElementEntity) path.getLastElement()).getPkValues();
            return x -> x.getPrimaryKeyValues().equals(id);
        }
        return null;
    }

    private void init() {
        if (!(path.getLastElement() instanceof PathElementEntity)) {
            throw new IllegalArgumentException("Path is not an Entity!");
        }
        entityType = ((PathElementEntity) path.getLastElement()).getEntityType();
        generateFilter(1, null);
    }

    /**
     * The parsed path of the subscription.
     *
     * @return the resource path.
     */
    public ResourcePath getPath() {
        return path;
    }

    @Override
    public boolean matches(de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager persistenceManager, Entity newEntity, java.util.Set<de.fraunhofer.iosb.ilt.frostserver.property.Property> fields) {
        if (matcher != null && !matcher.test(newEntity)) {
            return false;
        }
        return super.matches(persistenceManager, newEntity, fields);
    }

    @Override
    public String doFormatMessage(Entity entity) {
        PrincipalExtended oldLocalPrincipal = PrincipalExtended.getLocalPrincipal();
        try {
            PrincipalExtended.setLocalPrincipal(userPrincipal);
            entity.setQuery(emptyQuery);
            return settings.getFormatter(emptyQuery.getVersion(), FORMAT_NAME_DEFAULT).format(path, emptyQuery, entity).getFormatted();
        } catch (IncorrectRequestException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            PrincipalExtended.setLocalPrincipal(oldLocalPrincipal);
        }
    }

}
