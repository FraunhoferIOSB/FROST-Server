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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.tools;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.io.IOException;
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
public class CsvElementSet {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvElementSet.class);

    private final String namePrefix;
    private final List<CsvEntityEntry> elements = new ArrayList<>();

    public CsvElementSet(String namePrefix) {
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
            CsvEntityEntry element = new CsvUnitOfMeasurementProperty(type, namePrefix);
            elements.add(element);
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.error("Failed to read element", ex);
        }
    }

    public void initFrom(EntityType type, EntityProperty property) {
        try {
            final String getterName = property.getGetterName();
            final Class<? extends Entity> implementingClass = type.getImplementingClass();
            final Method getter = implementingClass.getMethod(getterName);
            CsvEntityEntry element = new CsvEntityProperty(namePrefix + property.entitiyName, new CsvElementFetcherDefault(getter));
            elements.add(element);
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.error("Failed to read element", ex);
        }
    }

    public void initFrom(EntityType type, NavigationProperty property, Query query) {
        try {
            String getterName = property.getGetterName();
            final Class<? extends Entity> implementingClass = type.getImplementingClass();
            final Method getter = implementingClass.getMethod(getterName);
            CsvEntityExpand element = new CsvEntityExpand(
                    namePrefix + property.getName() + "/",
                    property,
                    query,
                    new NavigationPropertyFollowerDefault(getter));
            elements.add(element);
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.error("Failed to read element", ex);
        }
    }

    public void writeHeader(CsvRowCollector collector) {
        for (CsvEntityEntry element : elements) {
            element.writeHeader(collector);
        }
    }

    public void writeData(CsvRowCollector collector, Object obj) throws IOException {
        if (obj instanceof Entity) {
            writeData(collector, (Entity) obj);
        } else if (obj instanceof EntitySet) {
            writeData(collector, (EntitySet) obj);
        }
    }

    public void writeData(CsvRowCollector collector, Entity<?> entity) {
        if (entity == null) {
            return;
        }
        for (CsvEntityEntry element : elements) {
            element.writeData(collector, entity);
        }
    }

    public void writeData(CsvRowCollector collector, EntitySet<?> entitySet) throws IOException {
        if (entitySet == null) {
            return;
        }
        List<? extends Entity> list = entitySet.asList();
        for (Entity e : list) {
            for (CsvEntityEntry element : elements) {
                element.writeData(collector, e);
            }
            collector.flush();
        }
    }

    private static class CsvElementFetcherDefault implements CsvElementFetcher<Object> {

        private final Method getter;

        public CsvElementFetcherDefault(Method getter) {
            this.getter = getter;
        }

        @Override
        public Object fetch(Entity<?> e) {
            try {
                Object result = getter.invoke(e);
                return result;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.error("Failed to read element", ex);
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
                LOGGER.error("Failed to read element", ex);
            }
            return null;
        }
    }

}
