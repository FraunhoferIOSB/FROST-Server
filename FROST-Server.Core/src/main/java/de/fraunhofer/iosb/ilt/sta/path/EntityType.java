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
package de.fraunhofer.iosb.ilt.sta.path;

import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jab, scf
 */
public enum EntityType {

    Datastream("Datastreams", Datastream.class),
    MultiDatastream("MultiDatastreams", MultiDatastream.class),
    FeatureOfInterest("FeaturesOfInterest", FeatureOfInterest.class),
    HistoricalLocation("HistoricalLocations", HistoricalLocation.class),
    Location("Locations", Location.class),
    Observation("Observations", Observation.class),
    ObservedProperty("ObservedProperties", ObservedProperty.class),
    Sensor("Sensors", Sensor.class),
    Thing("Things", Thing.class);

    public static class PropertyEntry {

        public final Property property;
        /**
         * Flag indicating the property must be set when creating an Entity.
         */
        public final boolean required;

        public PropertyEntry(Property property, boolean required) {
            this.property = property;
            this.required = required;
        }

    }
    /**
     * The name of this entity type as used in URLs.
     */
    public final String name;
    /**
     * The name of collections of this entity type as used in URLs.
     */
    public final String plural;
    /**
     * The Set of Properties that Entities of this type have, mapped to the flag
     * indicating if they are required.
     */
    private final Map<Property, Boolean> propertyMap = new HashMap<>();
    /**
     * The set of Navigation properties pointing to single entities.
     */
    private final Set<NavigationProperty> navigationEntities = new HashSet<>();
    /**
     * The set of Navigation properties pointing to entity sets.
     */
    private final Set<NavigationProperty> navigationSets = new HashSet<>();

    private final Class<? extends Entity> implementingClass;

    private static void init() {
        Map<Property, Boolean> propertyMap;
        propertyMap = Datastream.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.Name, true);
        propertyMap.put(EntityProperty.Description, true);
        propertyMap.put(EntityProperty.ObservationType, true);
        propertyMap.put(EntityProperty.UnitOfMeasurement, true);
        propertyMap.put(EntityProperty.ObservedArea, false);
        propertyMap.put(EntityProperty.PhenomenonTime, false);
        propertyMap.put(EntityProperty.Properties, false);
        propertyMap.put(EntityProperty.ResultTime, false);
        propertyMap.put(NavigationProperty.ObservedProperty, true);
        propertyMap.put(NavigationProperty.Sensor, true);
        propertyMap.put(NavigationProperty.Thing, true);
        propertyMap.put(NavigationProperty.Observations, false);

        propertyMap = MultiDatastream.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.Name, true);
        propertyMap.put(EntityProperty.Description, true);
        // ObservationType is required, but must always be the same, thus we set it ourselves.
        propertyMap.put(EntityProperty.ObservationType, false);
        propertyMap.put(EntityProperty.MultiObservationDataTypes, true);
        propertyMap.put(EntityProperty.UnitOfMeasurements, true);
        propertyMap.put(EntityProperty.ObservedArea, false);
        propertyMap.put(EntityProperty.PhenomenonTime, false);
        propertyMap.put(EntityProperty.Properties, false);
        propertyMap.put(EntityProperty.ResultTime, false);
        propertyMap.put(NavigationProperty.ObservedProperties, true);
        propertyMap.put(NavigationProperty.Sensor, true);
        propertyMap.put(NavigationProperty.Thing, true);
        propertyMap.put(NavigationProperty.Observations, false);

        propertyMap = FeatureOfInterest.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.Name, true);
        propertyMap.put(EntityProperty.Description, true);
        propertyMap.put(EntityProperty.EncodingType, true);
        propertyMap.put(EntityProperty.Feature, true);
        propertyMap.put(EntityProperty.Properties, false);
        propertyMap.put(NavigationProperty.Observations, false);

        propertyMap = HistoricalLocation.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.Time, true);
        propertyMap.put(NavigationProperty.Thing, true);
        propertyMap.put(NavigationProperty.Locations, false);

        propertyMap = Location.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.Name, true);
        propertyMap.put(EntityProperty.Description, true);
        propertyMap.put(EntityProperty.EncodingType, true);
        propertyMap.put(EntityProperty.Location, true);
        propertyMap.put(EntityProperty.Properties, false);
        propertyMap.put(NavigationProperty.HistoricalLocations, false);
        propertyMap.put(NavigationProperty.Things, false);

        propertyMap = Observation.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.PhenomenonTime, false);
        propertyMap.put(EntityProperty.ResultTime, false);
        propertyMap.put(EntityProperty.Result, true);
        propertyMap.put(EntityProperty.ResultQuality, false);
        propertyMap.put(EntityProperty.ValidTime, false);
        propertyMap.put(EntityProperty.Parameters, false);
        // One of the following two is mandatory.
        propertyMap.put(NavigationProperty.Datastream, false);
        propertyMap.put(NavigationProperty.MultiDatastream, false);
        // FeatureOfInterest must be generated on the fly if not present.
        propertyMap.put(NavigationProperty.FeatureOfInterest, false);

        propertyMap = ObservedProperty.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.Name, true);
        propertyMap.put(EntityProperty.Definition, true);
        propertyMap.put(EntityProperty.Description, true);
        propertyMap.put(EntityProperty.Properties, false);
        propertyMap.put(NavigationProperty.Datastreams, false);
        propertyMap.put(NavigationProperty.MultiDatastreams, false);

        propertyMap = Sensor.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.Name, true);
        propertyMap.put(EntityProperty.Description, true);
        propertyMap.put(EntityProperty.EncodingType, true);
        propertyMap.put(EntityProperty.Metadata, true);
        propertyMap.put(EntityProperty.Properties, false);
        propertyMap.put(NavigationProperty.Datastreams, false);
        propertyMap.put(NavigationProperty.MultiDatastreams, false);

        propertyMap = Thing.propertyMap;
        propertyMap.put(EntityProperty.Id, false);
        propertyMap.put(EntityProperty.SelfLink, false);
        propertyMap.put(EntityProperty.Name, true);
        propertyMap.put(EntityProperty.Description, true);
        propertyMap.put(EntityProperty.Properties, false);
        propertyMap.put(NavigationProperty.Locations, false);
        propertyMap.put(NavigationProperty.HistoricalLocations, false);
        propertyMap.put(NavigationProperty.Datastreams, false);
        propertyMap.put(NavigationProperty.MultiDatastreams, false);

        for (EntityType type : EntityType.values()) {
            for (Property property : type.getPropertySet()) {
                if (property instanceof NavigationProperty) {
                    NavigationProperty navigationProperty = (NavigationProperty) property;
                    if (navigationProperty.isSet) {
                        type.getNavigationSets().add(navigationProperty);
                    } else {
                        type.getNavigationEntities().add(navigationProperty);
                    }
                }
            }
        }
    }

    private EntityType(String plural, Class<? extends Entity> implementingClass) {
        this.name = name();
        this.plural = plural;
        this.implementingClass = implementingClass;
    }

    /**
     * The Map of Properties that Entities of this type have, with their
     * required status.
     *
     * @return The Set of Properties that Entities of this type have.
     */
    public Map<Property, Boolean> getPropertyMap() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return propertyMap;
    }

    /**
     * The Set of Properties that Entities of this type have.
     *
     * @return The Set of Properties that Entities of this type have.
     */
    public Set<Property> getPropertySet() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return propertyMap.keySet();
    }

    public Set<NavigationProperty> getNavigationEntities() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return navigationEntities;
    }

    public Set<NavigationProperty> getNavigationSets() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return navigationSets;
    }

    /**
     * @param property The property to check the required state for.
     * @return True when the property is required, false otherwise.
     */
    public boolean isRequired(Property property) {
        if (propertyMap.isEmpty()) {
            init();
        }
        if (!propertyMap.containsKey(property)) {
            return false;
        }
        return propertyMap.get(property);
    }

    public Class<? extends Entity> getImplementingClass() {
        return implementingClass;
    }

}
