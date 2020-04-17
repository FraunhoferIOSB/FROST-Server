/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.PathHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author jab
 */
public abstract class AbstractSubscription implements Subscription {

    private static Map<EntityType, List<NavigationPropertyMain>> navigationProperties = null;

    protected final String topic;
    protected EntityType entityType;
    protected Expression matchExpression = null;
    private Predicate<? super Entity> matcher;
    protected ResourcePath path;
    protected CoreSettings settings;

    public AbstractSubscription(String topic, ResourcePath path, CoreSettings settings) {
        initNavigationProperties();
        this.topic = topic;
        this.path = path;
        this.settings = settings;
    }

    private static void initNavigationProperties() {
        if (navigationProperties == null) {
            navigationProperties = new EnumMap<>(EntityType.class);
            for (EntityType type : EntityType.values()) {
                navigationProperties.put(type,
                        type.getPropertySet().stream()
                                .filter(x -> x instanceof NavigationPropertyMain)
                                .map(x -> (NavigationPropertyMain) x)
                                .collect(Collectors.toList()));
            }
        }
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
            Query query = new Query(settings);
            query.setFilter(matchExpression);
            Object result = persistenceManager.get(newEntity.getPath(), query);
            return result != null;
        }
        return true;
    }

    protected void generateFilter(int pathElementOffset) {
        EntityType lastType = getEntityType();
        List<Property> properties = new ArrayList<>();
        for (int i = path.size() - 1 - pathElementOffset; i >= 0; i--) {
            PathElement element = path.get(i);
            if (!(element instanceof PathElementEntity)) {
                continue;
            }
            final PathElementEntity epe = (PathElementEntity) element;
            final NavigationPropertyMain navProp = PathHelper.getNavigationProperty(lastType, epe.getEntityType());

            Id id = epe.getId();
            if (!navProp.isEntitySet() && id != null) {
                createMatcher(navProp, id);
                assert (i <= 1);
                return;
            }

            properties.add(navProp);
            lastType = epe.getEntityType();

            if (id != null) {
                createMatchExpression(properties, epe);
                // there should be at most two PathElements left, the EntitySetPath and the EntityPath now visiting
                assert (i <= 1);
                return;
            }
        }
    }

    private void createMatcher(final NavigationPropertyMain navProp, Id id) {
        // We have a collectionSubscription of type one-to-many.
        // Create a (cheap) matcher instead of an (expensive) Expression
        matcher = (Entity t) -> {
            Entity parent = (Entity) t.getProperty(navProp);
            if (parent == null) {
                // can be for Observation->Datastream when Observation is MultiDatastream.
                return false;
            }
            return id.equals(parent.getId());
        };
    }

    private void createMatchExpression(List<Property> properties, final PathElementEntity epe) {
        properties.add(EntityProperty.ID);
        String epeId = epe.getId().getUrl();
        if (epeId.startsWith("'")) {
            matchExpression = new Equal(new Path(properties), new StringConstant(epeId.substring(1, epeId.length() - 1)));
        } else {
            matchExpression = new Equal(new Path(properties), new IntegerConstant(epeId));
        }
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
