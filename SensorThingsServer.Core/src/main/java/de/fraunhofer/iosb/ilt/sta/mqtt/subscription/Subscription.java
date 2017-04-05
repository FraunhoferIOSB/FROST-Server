/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.mqtt.subscription;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;
import de.fraunhofer.iosb.ilt.sta.query.expression.Path;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.comparison.Equal;
import de.fraunhofer.iosb.ilt.sta.util.PathHelper;
import de.fraunhofer.iosb.ilt.sta.util.UrlHelper;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public abstract class Subscription {

    private static Map<EntityType, List<NavigationProperty>> navigationProperties = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(Subscription.class);
    // TODO make encoding global constant
    protected static final Charset ENCODING = Charset.forName("UTF-8");
    protected final String topic;
    protected final String errorMsg;
    protected EntityType entityType;
    protected Expression matchExpression = null;
    protected ResourcePath path;
    protected String serviceRootUrl;

    public Subscription(String topic, ResourcePath path, String serviceRootUrl) {
        initNavigationProperties();
        this.topic = topic;
        this.path = path;
        this.serviceRootUrl = serviceRootUrl;
        this.errorMsg = "Subscription to topic '" + topic + "' is invalid. Reason: ";
    }

    private void initNavigationProperties() {
        if (navigationProperties == null) {
            navigationProperties = new HashMap<>();
            for (EntityType entityType : EntityType.values()) {
                navigationProperties.put(entityType,
                        entityType.getPropertySet().stream()
                        .filter(x -> x instanceof NavigationProperty)
                        .map(x -> (NavigationProperty) x)
                        .collect(Collectors.toList()));
            }
        }
    }

    public boolean matches(PersistenceManager persistenceManager, Entity oldEntity, Entity newEntity) {
        if (!newEntity.getEntityType().equals(entityType)) {
            return false;
        }
        if (matchExpression != null) {
            Query query = new Query();
            query.setFilter(matchExpression);
            Object result = persistenceManager.get(newEntity.getPath(), query);
            return result != null;
        }
        return true;
    }

    protected void generateFilter(int pathElementOffset) {
        EntityType lastType = getEntityType();
        List<Property> properties = new ArrayList<>();
        for (int i = path.getPathElements().size() - 1 - pathElementOffset; i >= 0; i--) {

            if (path.getPathElements().get(i) instanceof EntityPathElement) {
                final EntityPathElement epe = (EntityPathElement) path.getPathElements().get(i);
                final NavigationProperty navProp = PathHelper.getNavigationProperty(lastType, epe.getEntityType());
                properties.add(navProp);
                lastType = epe.getEntityType();

                if (epe.getId() != null) {
                    properties.add(EntityProperty.Id);
                    matchExpression = new Equal(new Path(properties), new IntegerConstant(Integer.parseInt(epe.getId().getValue().toString())));
                    // there should be at most two PathElements left, the EntitySetPath and the EntityPath now visiting
                    assert (i <= 1);
                    return;
                }
            }

        }

    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getTopic() {
        return topic;
    }

    public String formatMessage(Entity entity) throws IOException {
        entity.setSelfLink(UrlHelper.generateSelfLink(path, entity));
        for (NavigationProperty navigationProperty : navigationProperties.get(entity.getEntityType())) {
            if (navigationProperty.isSet) {
                EntitySet property = (EntitySet) entity.getProperty(navigationProperty);
                property.setNavigationLink(UrlHelper.generateNavLink(path, entity, property, true));
            } else {
                Entity property = (Entity) entity.getProperty(navigationProperty);
                if (property != null) {
                    property.setNavigationLink(UrlHelper.generateNavLink(path, entity, property, true));
                }
            }
        }
        return doFormatMessage(entity);
    }

    public abstract String doFormatMessage(Entity entity) throws IOException;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.topic);
        hash = 97 * hash + Objects.hashCode(this.entityType);
        return hash;
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
        final Subscription other = (Subscription) obj;
        if (!Objects.equals(this.topic, other.topic)) {
            return false;
        }
        if (this.entityType != other.entityType) {
            return false;
        }
        return true;
    }

}
