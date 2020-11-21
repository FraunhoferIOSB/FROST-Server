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
package de.fraunhofer.iosb.ilt.frostserver.property;

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_ENTITY;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_ENTITYSET;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jab
 * @author scf
 * @param <P> The entityType of the value of the property.
 */
public class NavigationPropertyMain<P extends NavigableElement> implements NavigationProperty<P> {

    public static class NavigationPropertyEntity extends NavigationPropertyMain<Entity> {

        public NavigationPropertyEntity(String propertyName) {
            super(propertyName, false, TYPE_REFERENCE_ENTITY);
        }
    }

    public static class NavigationPropertyEntitySet extends NavigationPropertyMain<EntitySet> {

        public NavigationPropertyEntitySet(String propertyName) {
            super(propertyName, true, TYPE_REFERENCE_ENTITYSET);
        }
    }

    private static final Map<String, NavigationPropertyMain> PROPERTY_BY_NAME = new HashMap<>();
    private static final Set<NavigationPropertyMain> ALL_PROPERTIES = new LinkedHashSet<>();

    public static final NavigationPropertyEntity DATASTREAM = registerProperty(new NavigationPropertyEntity("Datastream"));
    public static final NavigationPropertyEntitySet DATASTREAMS = registerProperty(new NavigationPropertyEntitySet("Datastreams"));
    public static final NavigationPropertyEntity MULTIDATASTREAM = registerProperty(new NavigationPropertyEntity("MultiDatastream"));
    public static final NavigationPropertyEntitySet MULTIDATASTREAMS = registerProperty(new NavigationPropertyEntitySet("MultiDatastreams"));
    public static final NavigationPropertyEntity FEATUREOFINTEREST = registerProperty(new NavigationPropertyEntity("FeatureOfInterest"));
    public static final NavigationPropertyEntitySet HISTORICALLOCATIONS = registerProperty(new NavigationPropertyEntitySet("HistoricalLocations"));
    public static final NavigationPropertyEntity LOCATION = registerProperty(new NavigationPropertyEntity("Location"));
    public static final NavigationPropertyEntitySet LOCATIONS = registerProperty(new NavigationPropertyEntitySet("Locations"));
    public static final NavigationPropertyEntitySet OBSERVATIONS = registerProperty(new NavigationPropertyEntitySet("Observations"));
    public static final NavigationPropertyEntity OBSERVEDPROPERTY = registerProperty(new NavigationPropertyEntity("ObservedProperty"));
    public static final NavigationPropertyEntitySet OBSERVEDPROPERTIES = registerProperty(new NavigationPropertyEntitySet("ObservedProperties"));
    public static final NavigationPropertyEntity SENSOR = registerProperty(new NavigationPropertyEntity("Sensor"));
    public static final NavigationPropertyEntity THING = registerProperty(new NavigationPropertyEntity("Thing"));
    public static final NavigationPropertyEntitySet THINGS = registerProperty(new NavigationPropertyEntitySet("Things"));

    public static Set<NavigationPropertyMain> values() {
        return ALL_PROPERTIES;
    }

    public static NavigationPropertyMain valueOf(String name) {
        return PROPERTY_BY_NAME.get(name);
    }

    public static final <T extends NavigationPropertyMain> T registerProperty(T property) {
        if (PROPERTY_BY_NAME.containsKey(property.getName())) {
            if (PROPERTY_BY_NAME.get(property.getName()) == property) {
                // This exact property is already registered
                return property;
            } else {
                throw new IllegalArgumentException("A property named " + property.getName() + " is already registered");
            }
        }
        PROPERTY_BY_NAME.put(property.getName(), property);
        ALL_PROPERTIES.add(property);
        return property;
    }

    /**
     * The name of the navigation property in urls.
     */
    private final String name;
    /**
     * The type(class) of the type of the value of this property.
     */
    private final TypeReference<P> type;
    /**
     * The entityType of entity that this navigation property points to.
     */
    private EntityType entityType;
    /**
     * Flag indication the path is to an EntitySet.
     */
    private final boolean entitySet;

    private final Collection<String> aliases;

    private NavigationPropertyMain(String propertyName, boolean isSet, TypeReference<P> type) {
        this.type = type;
        this.name = propertyName;
        this.aliases = new ArrayList<>();
        this.aliases.add(propertyName);
        this.entitySet = isSet;
    }

    /**
     * Finds the NavigationProperty registered for the given name. Throws an
     * IllegalArgumentException when there is no property with the given name.
     *
     * @param propertyName The name to search for.
     * @return The NavigationProperty registered for the given name.
     */
    public static NavigationPropertyMain fromString(String propertyName) {
        NavigationPropertyMain property = PROPERTY_BY_NAME.get(propertyName);
        if (property == null) {
            throw new IllegalArgumentException("no navigation property with name '" + propertyName + "'");
        }
        return property;
    }

    /**
     * Finds the NavigationProperty registered for the given name.
     *
     * @param propertyName The name to search for.
     * @return The NavigationProperty registered for the given name, or NULL.
     */
    public static NavigationPropertyMain forName(String propertyName) {
        return PROPERTY_BY_NAME.get(propertyName);
    }

    @Override
    public EntityType getEntityType() {
        if (entityType == null) {
            entityType = EntityType.getEntityTypeForName(name);
        }
        return entityType;
    }

    @Override
    public boolean validFor(EntityType entityType) {
        return entityType.getPropertySet().contains(this);
    }

    @Override
    public boolean isEntitySet() {
        return entitySet;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJsonName() {
        return name;
    }

    @Override
    public TypeReference<P> getType() {
        return type;
    }

    @Override
    public P getFrom(Entity entity) {
        return entity.getProperty(this);
    }

    @Override
    public void setOn(Entity entity, P value) {
        entity.setProperty(this, value);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        return entity.isSetProperty(this);
    }

    @Override
    public String getNavigationLink(Entity parent) {
        String link = parent.getSelfLink() + '/' + name;
        Query query = parent.getQuery();
        if (query != null && !query.getSettings().useAbsoluteNavigationLinks()) {
            ResourcePath path = query.getPath();
            String curPath = path.getServiceRootUrl() + '/' + path.getVersion().urlPart + path.getPath();
            link = UrlHelper.getRelativePath(link, curPath);
        }
        return link;
    }

    @Override
    public String toString() {
        return getName();
    }
}
