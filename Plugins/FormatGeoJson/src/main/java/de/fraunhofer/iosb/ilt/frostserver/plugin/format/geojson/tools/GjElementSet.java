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

    /**
     * The name of this EntitySet.
     */
    private final String name;

    /**
     * The elements to output for each Entity in the set.
     */
    private final List<GjEntityEntry> elements = new ArrayList<>();

    /**
     * Should the collection be flushed after each entity.
     */
    private final boolean flush;

    public GjElementSet(String name, boolean flush) {
        this.name = name;
        this.flush = flush;
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
                initFromUnitOfMeasurement(type, (EntityProperty) property);
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

    public void initFromUnitOfMeasurement(EntityType type, EntityProperty property) {
        try {
            GjEntityEntry element = new GjUnitOfMeasurementProperty(type, property.entitiyName);
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
            GjEntityEntry element = new GjEntityProperty(property.entitiyName, new CsvElementFetcherDefault(getter));
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
                    property.getName() + "/",
                    property,
                    query,
                    new NavigationPropertyFollowerDefault(getter));
            elements.add(element);
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.error(FAILED_TO_READ_ELEMENT, ex);
        }
    }

    public void writeData(GjRowCollector collector, Object obj, String namePrefix) {
        if (obj instanceof Entity) {
            writeData(collector, (Entity) obj, namePrefix + name);
        } else if (obj instanceof EntitySet) {
            writeData(collector, (EntitySet) obj, namePrefix + name);
        }
    }

    public void writeData(GjRowCollector collector, Entity<?> entity, String namePrefix) {
        if (entity == null) {
            return;
        }
        collectElements(collector, entity, namePrefix);
        if (flush) {
            collector.flush();
        }
    }

    public void writeData(GjRowCollector collector, EntitySet<?> entitySet, String namePrefix) {
        if (entitySet == null) {
            return;
        }
        List<? extends Entity> list = entitySet.asList();
        int idx = 0;
        for (Entity entity : list) {
            String localName = flush ? namePrefix : namePrefix + idx + "/";
            collectElements(collector, entity, localName);
            if (flush) {
                collector.flush();
            }
            idx++;
        }
    }

    private void collectElements(GjRowCollector collector, Entity<?> entity, String namePrefix) {
        for (GjEntityEntry element : elements) {
            element.writeData(collector, entity, namePrefix);
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
        public Object fetch(Entity<?> source) {
            try {
                return getter.invoke(source);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.error(FAILED_TO_READ_ELEMENT, ex);
            }
            return null;
        }
    }

}
