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
import de.fraunhofer.iosb.ilt.frostserver.model.builder.core.AbstractEntityBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;

/**
 * Builder class for HistoricalLocation objects.
 *
 * @author jab
 */
public class HistoricalLocationBuilder extends AbstractEntityBuilder<HistoricalLocation, HistoricalLocationBuilder> {

    private TimeInstant time;
    private Thing thing;
    private EntitySet<Location> locations;

    public HistoricalLocationBuilder() {
        locations = new EntitySetImpl<>(EntityType.LOCATION);
    }

    public HistoricalLocationBuilder setTime(TimeInstant time) {
        this.time = time;
        return this;
    }

    public HistoricalLocationBuilder setThing(Thing thing) {
        this.thing = thing;
        return this;
    }

    public HistoricalLocationBuilder setLocations(EntitySet<Location> locations) {
        this.locations = locations;
        return this;
    }

    public HistoricalLocationBuilder addLocation(Location location) {
        this.locations.add(location);
        return this;
    }

    @Override
    protected HistoricalLocationBuilder getThis() {
        return this;
    }

    @Override
    public HistoricalLocation build() {
        HistoricalLocation hl = new HistoricalLocation();
        super.build(hl);
        hl.setTime(time);
        hl.setThing(thing);
        hl.setLocations(locations);
        return hl;
    }

}
