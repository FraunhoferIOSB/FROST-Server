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
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
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
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author jab
 * @author scf
 */
public abstract class AbstractSubscription implements Subscription {

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
    protected QueryDefaults queryDefaults;
    protected ModelRegistry modelRegistry;

    protected AbstractSubscription(String topic, ResourcePath path, CoreSettings settings) {
        this.topic = topic;
        this.path = path;
        this.settings = settings;
        this.queryDefaults = settings.getQueryDefaults().setAlwaysOrder(false);
        this.modelRegistry = settings.getModelRegistry();
    }

    @Override
    public boolean matches(PersistenceManager persistenceManager, Entity newEntity, Set<Property> fields) {
        if (!newEntity.getEntityType().equals(entityType)) {
            return false;
        }
        if (matcher != null && !matcher.test(newEntity)) {
            return false;
        }
        if (matchExpression != null) {
            Object result = persistenceManager.get(newEntity.getPath(), query);
            return result != null;
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
                NavigationPropertyMain navPropInverse = null;
                if (nextPathElement instanceof PathElementEntityType peet) {
                    final NavigationPropertyMain navProp = peet.getNavigationProperty();
                    if (navProp != null) {
                        navPropInverse = navProp.getInverse();
                    }
                }
                properties.add(navPropInverse);
                direct = false;

            } else if (element instanceof PathElementEntity epe) {
                NavigationPropertyMain navProp = null;
                if (nextPathElement instanceof PathElementEntityType peet) {
                    navProp = peet.getNavigationProperty().getInverse();
                }

                final PkValue id = epe.getPkValues();
                if (direct && navProp != null && !navProp.isEntitySet() && id != null) {
                    createMatcher(navProp, id);
                    assert (i <= 1);
                    return;
                }

                properties.add(navProp);

                if (id != null) {
                    createMatchExpression(properties, epe, extraFilter);
                    // there should be at most two PathElements left, the EntitySetPath and the EntityPath now visiting
                    assert (i <= 1);
                    return;
                }
            }
            nextPathElement = element;
        }
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
        query = new Query(modelRegistry, queryDefaults, path, ANONYMOUS_PRINCIPAL);
        query.setFilter(extraFilter);
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
        query = new Query(modelRegistry, queryDefaults, path, ANONYMOUS_PRINCIPAL);
        query.setFilter(matchExpression);
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String getTopic() {
        return topic;
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

    public abstract String doFormatMessage(Entity entity) throws IOException;

    @Override
    public int hashCode() {
        return Objects.hash(topic, entityType);
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
        return this.entityType == other.entityType;
    }

}
