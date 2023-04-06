/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author scf
 */
public class GjElementSet {

    /**
     * The name of this EntitySet.
     */
    private final String name;

    /**
     * The elements to output for each Entity in the set.
     */
    private final List<GjEntityEntry> elements = new ArrayList<>();

    /**
     * Is this the top-level collection, meaning the collection should be
     * flushed after each entity.
     */
    private final boolean topLevel;

    /**
     * The serviceRootUrl for the current request.
     */
    private final String serviceRootUrl;

    /**
     * The version of the current request.
     */
    private final Version version;

    private final Query query;

    public GjElementSet(Query query, String serviceRootUrl, Version version, String name, boolean flush) {
        this.query = query;
        this.serviceRootUrl = serviceRootUrl;
        this.version = version;
        this.name = name;
        this.topLevel = flush;
    }

    public void initFrom(EntityType type) {
        if (query == null || query.getSelect().isEmpty()) {
            initFrom(type.getPropertySet());
        } else {
            initFrom(query.getSelect());
        }
    }

    public void initFrom(Set<Property> properties) {
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
                elements.add(new GjSelfLinkProperty(query, serviceRootUrl, version, ModelRegistry.EP_SELFLINK.getName()));
            }
            if (property instanceof EntityPropertyMain) {
                initFrom((EntityPropertyMain) property);
            } else if (property instanceof EntityPropertyCustomSelect) {
                elements.add(new GjEntityProperty(((EntityPropertyCustomSelect) property).getName(), property));
            }
        }
    }

    private void initFrom(EntityPropertyMain property) {
        PropertyType type = property.getType();
        if (type == TypeComplex.STA_TIMEVALUE) {
            elements.add(new GjEntityProperty(property.getName(), property));
        } else if (type == TypeComplex.STA_TIMEINTERVAL) {
            elements.add(new GjEntityProperty(property.getName(), property));
        } else if (type instanceof TypeComplex && !((TypeComplex) type).isOpenType()) {
            elements.add(new GjComplexProperty(property.getName(), property));
        } else {
            elements.add(new GjEntityProperty(property.getName(), property));
        }
    }

    public void initFrom(NavigationProperty property, Query query) {
        GjEntityExpand element = new GjEntityExpand(
                serviceRootUrl,
                version,
                property.getName() + "/",
                property,
                query);
        elements.add(element);
    }

    public void writeData(GjRowCollector collector, Object obj, String namePrefix) {
        if (obj instanceof Entity) {
            writeData(collector, (Entity) obj, namePrefix + name);
        } else if (obj instanceof EntitySet) {
            writeData(collector, (EntitySet) obj, namePrefix + name);
        }
    }

    public void writeData(GjRowCollector collector, Entity entity, String namePrefix) {
        if (entity == null) {
            return;
        }
        collectElements(collector, entity, namePrefix);
        if (topLevel) {
            collector.flush();
        }
    }

    public void writeData(GjRowCollector collector, EntitySet entitySet, String namePrefix) {
        if (entitySet == null) {
            return;
        }
        int idx = 0;
        if (topLevel) {
            collector.setNextLink(entitySet.getNextLink());
            collector.setCount(entitySet.getCount());
        }
        for (Entity entity : entitySet) {
            String localName = topLevel ? namePrefix : namePrefix + idx + "/";
            collectElements(collector, entity, localName);
            if (topLevel) {
                collector.flush();
            }
            idx++;
        }
    }

    private void collectElements(GjRowCollector collector, Entity entity, String namePrefix) {
        for (GjEntityEntry element : elements) {
            element.writeData(collector, entity, namePrefix);
        }
    }

}
