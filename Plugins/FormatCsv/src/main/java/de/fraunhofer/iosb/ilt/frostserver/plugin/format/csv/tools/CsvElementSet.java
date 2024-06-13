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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.tools;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.io.IOException;
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
            initFrom(type.getPropertySet(), query);
        } else {
            initFrom(query.getSelect(), query);
        }
    }

    public void initFrom(Set<Property> properties, Query query) {
        initProperties(properties);
        if (query == null) {
            return;
        }
        for (Expand expand : query.getExpand()) {
            NavigationProperty path = expand.getPath();
            initFrom(path, expand.getSubQuery());
        }
    }

    private void initProperties(Set<Property> properties) {
        for (Property property : properties) {
            if (property == ModelRegistry.EP_SELFLINK) {
                continue;
            }
            if (property instanceof EntityPropertyMain epm) {
                initFrom(epm);
            } else if (property instanceof EntityPropertyCustomSelect epcs) {
                initFrom(epcs);
            }
        }
    }

    public void initFrom(EntityPropertyMain property) {
        PropertyType type = property.getType();
        CsvEntityEntry element;
        if (type instanceof TypeComplex tc && !tc.isOpenType()) {
            element = new CsvComplexProperty(namePrefix, property);
        } else {
            element = new CsvEntityProperty(namePrefix + property.getName(), property);
        }

        elements.add(element);
    }

    public void initFrom(EntityPropertyCustomSelect property) {
        CsvEntityEntry element = new CsvEntityProperty(namePrefix + property.getName(), property);
        elements.add(element);
    }

    public void initFrom(NavigationProperty property, Query query) {
        CsvEntityExpand element = new CsvEntityExpand(
                namePrefix + property.getName() + "/",
                property,
                query,
                new NavigationPropertyFollowerDefault(property));
        elements.add(element);
    }

    public void writeHeader(CsvRowCollector collector) {
        for (CsvEntityEntry element : elements) {
            element.writeHeader(collector);
        }
    }

    public void writeData(CsvRowCollector collector, Object obj) throws IOException {
        if (obj instanceof Entity entity) {
            writeData(collector, entity);
        } else if (obj instanceof EntitySet entitySet) {
            writeData(collector, entitySet);
        }
    }

    public void writeData(CsvRowCollector collector, Entity entity) {
        if (entity == null) {
            return;
        }
        for (CsvEntityEntry element : elements) {
            element.writeData(collector, entity);
        }
    }

    public void writeData(CsvRowCollector collector, EntitySet entitySet) throws IOException {
        if (entitySet == null) {
            return;
        }
        for (Entity e : entitySet) {
            for (CsvEntityEntry element : elements) {
                element.writeData(collector, e);
            }
            collector.flush();
        }
    }

    private static class NavigationPropertyFollowerDefault implements NavigationPropertyFollower {

        private final NavigationProperty property;

        public NavigationPropertyFollowerDefault(NavigationProperty getter) {
            this.property = getter;
        }

        @Override
        public Entity fetch(Entity source) {
            try {
                Object result = property.getFrom(source);
                if (result instanceof Entity entity) {
                    return entity;
                }
                if (result instanceof EntitySet entitySet) {
                    return entitySet.isEmpty() ? null : entitySet.iterator().next();
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.error("Failed to read element", ex);
            }
            return null;
        }
    }

}
