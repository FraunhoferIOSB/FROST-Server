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
package de.fraunhofer.iosb.ilt.sta.model.builder;

import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for Thing objects.
 *
 * @author jab
 */
public class ThingBuilder extends AbstractEntityBuilder<Thing, ThingBuilder> {

    private String name;
    private String description;
    private Map<String, Object> properties;
    private EntitySet<Location> locations;
    private EntitySet<HistoricalLocation> historicalLocations;
    private EntitySet<Datastream> datastreams;
    private EntitySet<MultiDatastream> multiDatastreams;

    public ThingBuilder() {
        this.properties = new HashMap<>();
        this.locations = new EntitySetImpl<>(EntityType.Location);
        this.historicalLocations = new EntitySetImpl<>(EntityType.HistoricalLocation);
        this.datastreams = new EntitySetImpl<>(EntityType.Datastream);
        this.multiDatastreams = new EntitySetImpl<>(EntityType.MultiDatastream);
    }

    public ThingBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ThingBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ThingBuilder setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public ThingBuilder addProperty(String name, Object value) {
        this.properties.put(name, value);
        return this;
    }

    public ThingBuilder setLocations(EntitySet<Location> locations) {
        this.locations = locations;
        return this;
    }

    public ThingBuilder addLocation(Location location) {
        this.locations.add(location);
        return this;
    }

    public ThingBuilder setHistoricalLocations(EntitySet<HistoricalLocation> historicalLocations) {
        this.historicalLocations = historicalLocations;
        return this;
    }

    public ThingBuilder addHistoricalLocation(HistoricalLocation historicalLocation) {
        this.historicalLocations.add(historicalLocation);
        return this;
    }

    public ThingBuilder setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
        return this;
    }

    public ThingBuilder addDatastream(Datastream datastream) {
        this.datastreams.add(datastream);
        return this;
    }

    public ThingBuilder setMultiDatastreams(EntitySet<MultiDatastream> multiDatastreams) {
        this.multiDatastreams = multiDatastreams;
        return this;
    }

    public ThingBuilder addMultiDatastream(MultiDatastream multiDatastream) {
        this.multiDatastreams.add(multiDatastream);
        return this;
    }

    @Override
    protected ThingBuilder getThis() {
        return this;
    }

    @Override
    public Thing build() {
        Thing thing = new Thing(
                id,
                selfLink,
                navigationLink,
                name,
                description,
                properties,
                locations,
                historicalLocations,
                datastreams,
                multiDatastreams);
        thing.setExportObject(isExportObject());
        return thing;
    }

}
