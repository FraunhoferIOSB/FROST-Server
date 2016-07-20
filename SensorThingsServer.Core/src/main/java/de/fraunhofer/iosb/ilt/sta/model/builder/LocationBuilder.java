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

import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;

/**
 * Builder class for Location objects.
 *
 * @author jab
 */
public class LocationBuilder extends AbstractEntityBuilder<Location, LocationBuilder> {

    private String name;
    private String description;
    private String encodingType;
    private Object location;
    private EntitySet<HistoricalLocation> historicalLocations;
    private EntitySet<Thing> things;

    public LocationBuilder() {
        things = new EntitySetImpl<>(EntityType.Thing);
        historicalLocations = new EntitySetImpl<>(EntityType.HistoricalLocation);
    }

    public LocationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public LocationBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public LocationBuilder setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        return this;
    }

    public LocationBuilder setLocation(Object location) {
        this.location = location;
        return this;
    }

    public LocationBuilder setHistoricalLocations(EntitySet<HistoricalLocation> historicalLocations) {
        this.historicalLocations = historicalLocations;
        return this;
    }

    public LocationBuilder addHistoricalLocation(HistoricalLocation historicalLocation) {
        this.historicalLocations.add(historicalLocation);
        return this;
    }

    public LocationBuilder setThings(EntitySet<Thing> things) {
        this.things = things;
        return this;
    }

    public LocationBuilder addThing(Thing thing) {
        this.things.add(thing);
        return this;
    }

    @Override
    protected LocationBuilder getThis() {
        return this;
    }

    @Override
    public Location build() {
        Location l = new Location(id, selfLink, navigationLink, name, description, encodingType, location, historicalLocations, things);
        l.setExportObject(isExportObject());
        return l;
    }

}
