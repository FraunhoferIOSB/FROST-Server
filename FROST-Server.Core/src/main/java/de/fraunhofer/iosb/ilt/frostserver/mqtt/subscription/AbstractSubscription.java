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

import static de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended.ANONYMOUS_PRINCIPAL;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PrimaryKey;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all subscription types.
 */
public abstract class AbstractSubscription implements Subscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSubscription.class.getName());

    protected final String topic;
    protected EntityType entityType;

    private Expression matchExpression = null;
    private Query query;
    private Predicate<? super Entity> matcher;

    /**
     * If the subscription is over a one-to-many relation, this has a value.
     */
    private NavigationPropertyMain parentRelation;
    private PkValue parentId;

    protected ResourcePath path;
    protected CoreSettings settings;
    protected ServiceContext context;

    protected final PrincipalExtended userPrincipal;
    protected boolean fineGrainedAuth;

    protected AbstractSubscription(CoreSettings settings, String topic, ResourcePath path) {
        this(settings, PrincipalExtended.ANONYMOUS_PRINCIPAL, topic, path, false);
    }

    protected AbstractSubscription(CoreSettings settings, PrincipalExtended userPrincipal, String topic, ResourcePath path) {
        this(settings, userPrincipal, topic, path, true);
    }

    private AbstractSubscription(CoreSettings settings, PrincipalExtended userPrincipal, String topic, ResourcePath path, boolean fga) {
        this.userPrincipal = userPrincipal;
        this.fineGrainedAuth = fga;
        this.topic = topic;
        this.path = path;
        this.settings = settings;
        LOGGER.debug("Subscription for {} on {}", userPrincipal, topic);
        final Version version = path.getVersion();
        final QueryDefaults queryDefaults = settings.getQueryDefaults().setAlwaysOrder(false);
        context = new ServiceContext()
                .setModelRegistry(settings.getModelRegistry())
                .setFunctionRegistry(settings.getFunctionRegistry())
                .setQueryDefaults(queryDefaults)
                .setMqttContext(true)
                .setPrefixGen(() -> (version.getMqttFullUrls() ? queryDefaults.getServiceRootUrl() + '/' : "") + path.getVersion().urlPart + '/');
    }

    @Override
    public boolean matches(PersistenceManager persistenceManager, Entity newEntity, Set<Property> fields) {
        if (!newEntity.getType().equals(entityType)) {
            LOGGER.trace("      Wrong entity type, expected {}", entityType);
            return false;
        }
        if (matcher != null && !matcher.test(newEntity)) {
            LOGGER.trace("      Matcher failed: {}", matcher);
            return false;
        }
        if (matchExpression != null) {
            PrincipalExtended oldLocalPrincipal = PrincipalExtended.getLocalPrincipal();
            PrincipalExtended.setLocalPrincipal(userPrincipal);
            try {
                Object result = persistenceManager.get(newEntity.getPath(), query);
                return result != null;
            } finally {
                PrincipalExtended.setLocalPrincipal(oldLocalPrincipal);
            }
        }
        if (fineGrainedAuth) {
            PrincipalExtended oldLocalPrincipal = PrincipalExtended.getLocalPrincipal();
            PrincipalExtended.setLocalPrincipal(userPrincipal);
            try {
                Object result = persistenceManager.get(newEntity.getType(), newEntity.getPrimaryKeyValues());
                return result != null;
            } finally {
                PrincipalExtended.setLocalPrincipal(oldLocalPrincipal);
            }
        }
        return true;
    }

    protected void generateFilter(int pathElementOffset, Expression extraFilter) {
        final List<Property> properties = new ArrayList<>();
        boolean direct = extraFilter == null;
        final int size = path.size();
        final int startIdx = size - 1 - pathElementOffset;
        if (startIdx < 0) {
            createMatchExpression(extraFilter);
            return;
        }
        PathElement nextPathElement = startIdx < size ? path.get(startIdx + 1) : null;
        for (int i = startIdx; i >= 0; i--) {
            PathElement element = path.get(i);
            if (element instanceof PathElementEntitySet) {
                handleEntitySet(nextPathElement, properties);
                direct = false;

            } else if (element instanceof PathElementEntity epe && handleEntity(epe, nextPathElement, direct, i, properties, extraFilter)) {
                return;
            }
            nextPathElement = element;
        }
    }

    private void handleEntitySet(PathElement nextPathElement, final List<Property> properties) {
        NavigationPropertyMain navPropInverse = null;
        if (nextPathElement instanceof PathElementEntityType peet) {
            final NavigationPropertyMain navProp = peet.getNavigationProperty();
            if (navProp != null) {
                navPropInverse = navProp.getInverse();
            }
        }
        properties.add(navPropInverse);
    }

    private boolean handleEntity(PathElementEntity epe, PathElement nextPathElement, boolean direct, int i, final List<Property> properties, Expression extraFilter) {
        NavigationPropertyMain navProp = null;
        if (nextPathElement instanceof PathElementEntityType peet) {
            navProp = peet.getNavigationProperty().getInverse();
        }
        final PkValue id = epe.getPkValues();
        if (direct && navProp != null && !navProp.isEntitySet() && id != null) {
            createMatcher(navProp, id);
            assert (i <= 1);
            return true;
        }
        properties.add(navProp);
        if (id != null) {
            createMatchExpression(properties, epe, extraFilter);
            // there should be at most two PathElements left, the EntitySetPath and the EntityPath now visiting
            assert (i <= 1);
            return true;
        }
        return false;
    }

    private void createMatcher(final NavigationPropertyMain navProp, PkValue pkValue) {
        // We have a collectionSubscription of type one-to-many.
        // Create a (cheap) matcher instead of an (expensive) Expression
        parentRelation = navProp;
        parentId = pkValue;

        matcher = (Entity t) -> {
            Entity parent = (Entity) t.getProperty(navProp);
            if (parent == null) {
                // can be for Observation->Datastream when Observation is MultiDatastream.
                return false;
            }
            return pkValue.equals(parent.getPrimaryKeyValues());
        };
    }

    private void createMatchExpression(Expression extraFilter) {
        if (extraFilter == null) {
            return;
        }
        matchExpression = extraFilter;
        query = new Query(context, path, ANONYMOUS_PRINCIPAL)
                .setMetadata(Metadata.MINIMAL_WITH_ID)
                .setFilter(extraFilter)
                .validate();
    }

    private void createMatchExpression(List<Property> properties, final PathElementEntity epe, Expression extraFilter) {
        final PrimaryKey primaryKey = entityType.getPrimaryKey();
        properties.addAll(primaryKey.getKeyProperties());
        String epeId = UrlHelper.quoteForUrl(primaryKey, epe.getPkValues());
        if (epeId.startsWith("'")) {
            matchExpression = new Equal(new Path(properties), new StringConstant(epeId.substring(1, epeId.length() - 1)));
        } else {
            matchExpression = new Equal(new Path(properties), new IntegerConstant(epeId));
        }
        if (extraFilter != null) {
            matchExpression = new And(matchExpression, extraFilter);
        }
        query = new Query(context, path, ANONYMOUS_PRINCIPAL)
                .setMetadata(Metadata.MINIMAL_WITH_ID)
                .setFilter(matchExpression)
                .validate();
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String getTopic() {
        final long userKey = userPrincipal.getUserKey();
        if (fineGrainedAuth) {
            return Long.toString(userKey) + '/' + topic;
        }
        return topic;
    }

    @Override
    public Version getVersion() {
        return path.getVersion();
    }

    @Override
    public String formatMessage(Entity entity) throws IOException {
        return doFormatMessage(entity);
    }

    @Override
    public NavigationPropertyMain getParentRelation() {
        return parentRelation;
    }

    @Override
    public PkValue getParentId() {
        return parentId;
    }

    public abstract String doFormatMessage(Entity entity);

    @Override
    public int hashCode() {
        return Objects.hash(topic, entityType, userPrincipal);
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
        final AbstractSubscription other = (AbstractSubscription) obj;
        if (!Objects.equals(this.topic, other.topic)) {
            return false;
        }
        if (!Objects.equals(this.entityType, other.entityType)) {
            return false;
        }
        return Objects.equals(this.userPrincipal, other.userPrincipal);
    }

    @Override
    public String toString() {
        return userPrincipal + ":" + getTopic();
    }

}
