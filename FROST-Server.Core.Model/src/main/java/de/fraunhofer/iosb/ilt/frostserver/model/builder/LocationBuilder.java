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
package de.fraunhofer.iosb.ilt.frostserver.model.builder;

import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.core.NamedEntityBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;

/**
 * Builder class for Location objects.
 *
 * @author jab
 */
public class LocationBuilder extends NamedEntityBuilder<Location, LocationBuilder> {

    private String encodingType;
    private Object location;
    private EntitySet<HistoricalLocation> historicalLocations;
    private EntitySet<Thing> things;

    public LocationBuilder() {
        things = new EntitySetImpl<>(EntityType.THING);
        historicalLocations = new EntitySetImpl<>(EntityType.HISTORICALLOCATION);
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
        Location l = new Location();
        super.build(l);
        l.setEncodingType(encodingType);
        l.setLocation(location);
        l.setHistoricalLocations(historicalLocations);
        l.setThings(things);
        return l;
    }

}
