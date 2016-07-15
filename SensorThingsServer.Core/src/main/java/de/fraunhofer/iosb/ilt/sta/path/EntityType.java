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
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jab, scf
 */
public enum EntityType {

    Datastream("Datastreams", Datastream.class),
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
    private final Set<PropertyEntry> propertySet = new HashSet<>();
    private final Class<? extends Entity> implementingClass;

    static {
        Set<PropertyEntry> propertySet;
        propertySet = Datastream.getPropertySet();
        propertySet.add(new PropertyEntry(EntityProperty.Id, false));
        propertySet.add(new PropertyEntry(EntityProperty.SelfLink, false));
        propertySet.add(new PropertyEntry(EntityProperty.Name, true));
        propertySet.add(new PropertyEntry(EntityProperty.Description, true));
        propertySet.add(new PropertyEntry(EntityProperty.ObservationType, true));
        propertySet.add(new PropertyEntry(EntityProperty.UnitOfMeasurement, true));
        propertySet.add(new PropertyEntry(EntityProperty.ObservedArea, false));
        propertySet.add(new PropertyEntry(EntityProperty.PhenomenonTime, false));
        propertySet.add(new PropertyEntry(EntityProperty.ResultTime, false));
        propertySet.add(new PropertyEntry(NavigationProperty.ObservedProperty, true));
        propertySet.add(new PropertyEntry(NavigationProperty.Sensor, true));
        propertySet.add(new PropertyEntry(NavigationProperty.Thing, true));
        propertySet.add(new PropertyEntry(NavigationProperty.Observations, false));

        propertySet = FeatureOfInterest.getPropertySet();
        propertySet.add(new PropertyEntry(EntityProperty.Id, false));
        propertySet.add(new PropertyEntry(EntityProperty.SelfLink, false));
        propertySet.add(new PropertyEntry(EntityProperty.Name, true));
        propertySet.add(new PropertyEntry(EntityProperty.Description, true));
        propertySet.add(new PropertyEntry(EntityProperty.EncodingType, true));
        propertySet.add(new PropertyEntry(EntityProperty.Feature, true));
        propertySet.add(new PropertyEntry(NavigationProperty.Observations, false));

        propertySet = HistoricalLocation.getPropertySet();
        propertySet.add(new PropertyEntry(EntityProperty.Id, false));
        propertySet.add(new PropertyEntry(EntityProperty.SelfLink, false));
        propertySet.add(new PropertyEntry(EntityProperty.Time, true));
        propertySet.add(new PropertyEntry(NavigationProperty.Thing, true));
        propertySet.add(new PropertyEntry(NavigationProperty.Locations, false));

        propertySet = Location.getPropertySet();
        propertySet.add(new PropertyEntry(EntityProperty.Id, false));
        propertySet.add(new PropertyEntry(EntityProperty.SelfLink, false));
        propertySet.add(new PropertyEntry(EntityProperty.Name, true));
        propertySet.add(new PropertyEntry(EntityProperty.Description, true));
        propertySet.add(new PropertyEntry(EntityProperty.EncodingType, true));
        propertySet.add(new PropertyEntry(EntityProperty.Location, true));
        propertySet.add(new PropertyEntry(NavigationProperty.HistoricalLocations, false));
        propertySet.add(new PropertyEntry(NavigationProperty.Things, false));

        propertySet = Observation.getPropertySet();
        propertySet.add(new PropertyEntry(EntityProperty.Id, false));
        propertySet.add(new PropertyEntry(EntityProperty.SelfLink, false));
        propertySet.add(new PropertyEntry(EntityProperty.PhenomenonTime, false));
        propertySet.add(new PropertyEntry(EntityProperty.ResultTime, false));
        propertySet.add(new PropertyEntry(EntityProperty.Result, true));
        propertySet.add(new PropertyEntry(EntityProperty.ResultQuality, false));
        propertySet.add(new PropertyEntry(EntityProperty.ValidTime, false));
        propertySet.add(new PropertyEntry(EntityProperty.Parameters, false));
        propertySet.add(new PropertyEntry(NavigationProperty.Datastream, true));
        // FeatureOfInterest must be generated on the fly if not present.
        propertySet.add(new PropertyEntry(NavigationProperty.FeatureOfInterest, false));

        propertySet = ObservedProperty.getPropertySet();
        propertySet.add(new PropertyEntry(EntityProperty.Id, false));
        propertySet.add(new PropertyEntry(EntityProperty.SelfLink, false));
        propertySet.add(new PropertyEntry(EntityProperty.Name, true));
        propertySet.add(new PropertyEntry(EntityProperty.Definition, true));
        propertySet.add(new PropertyEntry(EntityProperty.Description, true));
        propertySet.add(new PropertyEntry(NavigationProperty.Datastreams, false));

        propertySet = Sensor.getPropertySet();
        propertySet.add(new PropertyEntry(EntityProperty.Id, false));
        propertySet.add(new PropertyEntry(EntityProperty.SelfLink, false));
        propertySet.add(new PropertyEntry(EntityProperty.Name, true));
        propertySet.add(new PropertyEntry(EntityProperty.Description, true));
        propertySet.add(new PropertyEntry(EntityProperty.EncodingType, true));
        propertySet.add(new PropertyEntry(EntityProperty.Metadata, true));
        propertySet.add(new PropertyEntry(NavigationProperty.Datastreams, false));

        propertySet = Thing.getPropertySet();
        propertySet.add(new PropertyEntry(EntityProperty.Id, false));
        propertySet.add(new PropertyEntry(EntityProperty.SelfLink, false));
        propertySet.add(new PropertyEntry(EntityProperty.Name, true));
        propertySet.add(new PropertyEntry(EntityProperty.Description, true));
        propertySet.add(new PropertyEntry(EntityProperty.Properties, false));
        propertySet.add(new PropertyEntry(NavigationProperty.Locations, false));
        propertySet.add(new PropertyEntry(NavigationProperty.HistoricalLocations, false));
        propertySet.add(new PropertyEntry(NavigationProperty.Datastreams, false));
    }

    private EntityType(String plural, Class<? extends Entity> implementingClass) {
        this.name = name();
        this.plural = plural;
        this.implementingClass = implementingClass;
    }

    /**
     * @return The Set of Properties that Entities of this type have.
     */
    public Set<PropertyEntry> getPropertySet() {
        return propertySet;
    }

    public Class<? extends Entity> getImplementingClass() {
        return implementingClass;
    }

}
