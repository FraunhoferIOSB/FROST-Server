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
     * The Set of Properties that Entities of this type have.
     */
    private final Map<Property, Boolean> propertySet = new HashMap<>();
    private final Class<? extends Entity> implementingClass;

    static {
        Map<Property, Boolean> propertySet;
        propertySet = Datastream.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.Name, true);
        propertySet.put(EntityProperty.Description, true);
        propertySet.put(EntityProperty.ObservationType, true);
        propertySet.put(EntityProperty.UnitOfMeasurement, true);
        propertySet.put(EntityProperty.ObservedArea, false);
        propertySet.put(EntityProperty.PhenomenonTime, false);
        propertySet.put(EntityProperty.ResultTime, false);
        propertySet.put(NavigationProperty.ObservedProperty, true);
        propertySet.put(NavigationProperty.Sensor, true);
        propertySet.put(NavigationProperty.Thing, true);
        propertySet.put(NavigationProperty.Observations, false);

        propertySet = MultiDatastream.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.Name, true);
        propertySet.put(EntityProperty.Description, true);
        propertySet.put(EntityProperty.ObservationType, true);
        propertySet.put(EntityProperty.MultiObservationDataTypes, true);
        propertySet.put(EntityProperty.UnitOfMeasurements, true);
        propertySet.put(EntityProperty.ObservedArea, false);
        propertySet.put(EntityProperty.PhenomenonTime, false);
        propertySet.put(EntityProperty.ResultTime, false);
        propertySet.put(NavigationProperty.ObservedProperties, true);
        propertySet.put(NavigationProperty.Sensor, true);
        propertySet.put(NavigationProperty.Thing, true);
        propertySet.put(NavigationProperty.Observations, false);

        propertySet = FeatureOfInterest.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.Name, true);
        propertySet.put(EntityProperty.Description, true);
        propertySet.put(EntityProperty.EncodingType, true);
        propertySet.put(EntityProperty.Feature, true);
        propertySet.put(NavigationProperty.Observations, false);

        propertySet = HistoricalLocation.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.Time, true);
        propertySet.put(NavigationProperty.Thing, true);
        propertySet.put(NavigationProperty.Locations, false);

        propertySet = Location.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.Name, true);
        propertySet.put(EntityProperty.Description, true);
        propertySet.put(EntityProperty.EncodingType, true);
        propertySet.put(EntityProperty.Location, true);
        propertySet.put(NavigationProperty.HistoricalLocations, false);
        propertySet.put(NavigationProperty.Things, false);

        propertySet = Observation.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.PhenomenonTime, false);
        propertySet.put(EntityProperty.ResultTime, false);
        propertySet.put(EntityProperty.Result, true);
        propertySet.put(EntityProperty.ResultQuality, false);
        propertySet.put(EntityProperty.ValidTime, false);
        propertySet.put(EntityProperty.Parameters, false);
        // One of the following two is mandatory.
        propertySet.put(NavigationProperty.Datastream, false);
        propertySet.put(NavigationProperty.MultiDatastream, false);
        // FeatureOfInterest must be generated on the fly if not present.
        propertySet.put(NavigationProperty.FeatureOfInterest, false);

        propertySet = ObservedProperty.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.Name, true);
        propertySet.put(EntityProperty.Definition, true);
        propertySet.put(EntityProperty.Description, true);
        propertySet.put(NavigationProperty.Datastreams, false);
        propertySet.put(NavigationProperty.MultiDatastreams, false);

        propertySet = Sensor.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.Name, true);
        propertySet.put(EntityProperty.Description, true);
        propertySet.put(EntityProperty.EncodingType, true);
        propertySet.put(EntityProperty.Metadata, true);
        propertySet.put(NavigationProperty.Datastreams, false);
        propertySet.put(NavigationProperty.MultiDatastreams, false);

        propertySet = Thing.propertySet;
        propertySet.put(EntityProperty.Id, false);
        propertySet.put(EntityProperty.SelfLink, false);
        propertySet.put(EntityProperty.Name, true);
        propertySet.put(EntityProperty.Description, true);
        propertySet.put(EntityProperty.Properties, false);
        propertySet.put(NavigationProperty.Locations, false);
        propertySet.put(NavigationProperty.HistoricalLocations, false);
        propertySet.put(NavigationProperty.Datastreams, false);
        propertySet.put(NavigationProperty.MultiDatastreams, false);
    }

    private EntityType(String plural, Class<? extends Entity> implementingClass) {
        this.name = name();
        this.plural = plural;
        this.implementingClass = implementingClass;
    }

    /**
     * @return The Set of Properties that Entities of this type have.
     */
    public Set<Property> getPropertySet() {
        return propertySet.keySet();
    }

    /**
     * @param property The property to check the required state for.
     * @return True when the property is required, false otherwise.
     */
    public boolean isRequired(Property property) {
        if (!propertySet.containsKey(property)) {
            return false;
        }
        return propertySet.get(property);
    }

    public Class<? extends Entity> getImplementingClass() {
        return implementingClass;
    }

}
