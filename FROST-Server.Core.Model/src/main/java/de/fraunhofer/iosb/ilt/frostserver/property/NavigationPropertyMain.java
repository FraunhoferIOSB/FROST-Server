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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Convert away from enum.
 *
 * @author jab
 */
public enum NavigationPropertyMain implements NavigationProperty {
    ACTUATOR("Actuator", false),
    ACTUATORS("Actuators", true),
    DATASTREAM("Datastream", false),
    DATASTREAMS("Datastreams", true),
    MULTIDATASTREAM("MultiDatastream", false),
    MULTIDATASTREAMS("MultiDatastreams", true),
    FEATUREOFINTEREST("FeatureOfInterest", false),
    HISTORICALLOCATIONS("HistoricalLocations", true),
    LOCATION("Location", false),
    LOCATIONS("Locations", true),
    OBSERVATIONS("Observations", true),
    OBSERVEDPROPERTY("ObservedProperty", false),
    OBSERVEDPROPERTIES("ObservedProperties", true),
    SENSOR("Sensor", false),
    TASK("Task", false),
    TASKS("Tasks", true),
    TASKINGCAPABILITY("TaskingCapability", false),
    TASKINGCAPABILITIES("TaskingCapabilities", true),
    THING("Thing", false),
    THINGS("Things", true);

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationPropertyMain.class.getName());
    private static final Map<String, NavigationPropertyMain> PROPERTY_BY_NAME = new HashMap<>();

    static {
        for (NavigationPropertyMain property : NavigationPropertyMain.values()) {
            for (String alias : property.aliases) {
                PROPERTY_BY_NAME.put(alias.toLowerCase(), property);
            }
        }
    }

    private final Collection<String> aliases;

    /**
     * The type of entity that this navigation property points to.
     */
    private EntityType type;
    /**
     * The name of the navigation property in urls.
     */
    private final String propertyName;

    /**
     * Flag indication the path is to an EntitySet.
     */
    private final boolean entitySet;

    private NavigationPropertyMain(String propertyName, boolean isSet) {
        this.propertyName = propertyName;
        this.aliases = new ArrayList<>();
        this.aliases.add(propertyName);
        this.entitySet = isSet;
    }

    public static NavigationPropertyMain fromString(String propertyName) {
        NavigationPropertyMain property = PROPERTY_BY_NAME.get(propertyName.toLowerCase());
        if (property == null) {
            throw new IllegalArgumentException("no navigation property with name '" + propertyName + "'");
        }
        return property;
    }

    @Override
    public EntityType getType() {
        if (type == null) {
            type = EntityType.getEntityTypeForName(propertyName);
        }
        return type;
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
        return propertyName;
    }

    @Override
    public String getJsonName() {
        return propertyName;
    }

    @Override
    public Object getFrom(Entity entity) {
        return entity.getProperty(this);
    }

    @Override
    public void setOn(Entity entity, Object value) {
        entity.setProperty(this, value);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        return entity.isSetProperty(this);
    }

    @Override
    public String getNavigationLink(Entity parent) {
        String link = parent.getSelfLink() + '/' + propertyName;
        Query query = parent.getQuery();
        if (query != null && !query.getSettings().useAbsoluteNavigationLinks()) {
            ResourcePath path = query.getPath();
            String curPath = path.getServiceRootUrl() + '/' + path.getVersion().urlPart + path.getPath();
            link = UrlHelper.getRelativePath(link, curPath);
        }
        return link;
    }

}
