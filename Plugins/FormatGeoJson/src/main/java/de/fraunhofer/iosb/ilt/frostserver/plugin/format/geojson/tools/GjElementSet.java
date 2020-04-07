/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class GjElementSet {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GjElementSet.class);
    private static final String FAILED_TO_READ_ELEMENT = "Failed to read element";

    private final String namePrefix;
    private final List<GjEntityEntry> elements = new ArrayList<>();

    public GjElementSet(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public void initFrom(EntityType type, Query query) {
        if (query == null || query.getSelect().isEmpty()) {
            initFrom(type, type.getPropertySet(), query);
        } else {
            initFrom(type, query.getSelect(), query);
        }
    }

    public void initFrom(EntityType type, Set<Property> properties, Query query) {
        for (Property property : properties) {
            if (property == EntityProperty.SELFLINK) {
                continue;
            }
            if (property == EntityProperty.UNITOFMEASUREMENT) {
                initFromUnitOfMeasurement(type);
            } else if (property instanceof EntityProperty) {
                initFrom(type, (EntityProperty) property);
            }
        }
        if (query == null) {
            return;
        }
        for (Expand expand : query.getExpand()) {
            NavigationProperty path = expand.getPath();
            initFrom(type, path, expand.getSubQuery());
        }
    }

    public void initFromUnitOfMeasurement(EntityType type) {
        try {
            GjEntityEntry element = new GjUnitOfMeasurementProperty(type, namePrefix);
            elements.add(element);
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.error(FAILED_TO_READ_ELEMENT, ex);
        }
    }

    public void initFrom(EntityType type, EntityProperty property) {
        try {
            final String getterName = property.getGetterName();
            final Class<? extends Entity> implementingClass = type.getImplementingClass();
            final Method getter = implementingClass.getMethod(getterName);
            GjEntityEntry element = new GjEntityProperty(namePrefix + property.entitiyName, new CsvElementFetcherDefault(getter));
            elements.add(element);
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.error(FAILED_TO_READ_ELEMENT, ex);
        }
    }

    public void initFrom(EntityType type, NavigationProperty property, Query query) {
        try {
            String getterName = property.getGetterName();
            final Class<? extends Entity> implementingClass = type.getImplementingClass();
            final Method getter = implementingClass.getMethod(getterName);
            GjEntityExpand element = new GjEntityExpand(
                    namePrefix + property.getName() + "/",
                    property,
                    query,
                    new NavigationPropertyFollowerDefault(getter));
            elements.add(element);
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.error(FAILED_TO_READ_ELEMENT, ex);
        }
    }

    public void writeData(GjRowCollector collector, Object obj) {
        if (obj instanceof Entity) {
            writeData(collector, (Entity) obj);
        } else if (obj instanceof EntitySet) {
            writeData(collector, (EntitySet) obj);
        }
    }

    public void writeData(GjRowCollector collector, Entity<?> entity) {
        if (entity == null) {
            return;
        }
        for (GjEntityEntry element : elements) {
            element.writeData(collector, entity);
        }
    }

    public void writeData(GjRowCollector collector, EntitySet<?> entitySet) {
        if (entitySet == null) {
            return;
        }
        List<? extends Entity> list = entitySet.asList();
        for (Entity entity : list) {
            for (GjEntityEntry element : elements) {
                element.writeData(collector, entity);
            }
            collector.flush();
        }
    }

    private static class CsvElementFetcherDefault implements GjElementFetcher<Object> {

        private final Method getter;

        public CsvElementFetcherDefault(Method getter) {
            this.getter = getter;
        }

        @Override
        public Object fetch(Entity<?> e) {
            try {
                return getter.invoke(e);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.error(FAILED_TO_READ_ELEMENT, ex);
            }
            return null;
        }
    }

    private static class NavigationPropertyFollowerDefault implements NavigationPropertyFollower {

        private final Method getter;

        public NavigationPropertyFollowerDefault(Method getter) {
            this.getter = getter;
        }

        @Override
        public Entity<?> fetch(Entity<?> source) {
            try {
                Object result = getter.invoke(source);
                if (result instanceof Entity) {
                    return (Entity) result;
                }
                if (result instanceof EntitySet) {
                    EntitySet entitySet = (EntitySet<? extends Entity>) result;
                    List<? extends Entity> asList = entitySet.asList();
                    return asList.isEmpty() ? null : asList.get(0);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.error(FAILED_TO_READ_ELEMENT, ex);
            }
            return null;
        }
    }

}
