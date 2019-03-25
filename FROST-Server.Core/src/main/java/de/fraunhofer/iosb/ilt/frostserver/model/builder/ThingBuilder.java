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
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.core.NamedDsHoldingEntityBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;

/**
 * Builder class for Thing objects.
 *
 * @author jab
 */
public class ThingBuilder extends NamedDsHoldingEntityBuilder<Thing, ThingBuilder> {

    private EntitySet<Location> locations;
    private EntitySet<HistoricalLocation> historicalLocations;
    private EntitySet<TaskingCapability> taskingCapabilities;

    public ThingBuilder() {
        locations = new EntitySetImpl<>(EntityType.LOCATION);
        historicalLocations = new EntitySetImpl<>(EntityType.HISTORICALLOCATION);
        taskingCapabilities = new EntitySetImpl<>(EntityType.TASKINGCAPABILITY);
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

    public ThingBuilder setTaskingCapabilities(EntitySet<TaskingCapability> taskingCapabilities) {
        this.taskingCapabilities = taskingCapabilities;
        return this;
    }

    public ThingBuilder addTaskingCapability(TaskingCapability taskingCapability) {
        this.taskingCapabilities.add(taskingCapability);
        return this;
    }

    @Override
    protected ThingBuilder getThis() {
        return this;
    }

    @Override
    public Thing build() {
        Thing thing = new Thing();
        super.build(thing);
        thing.setLocations(locations);
        thing.setHistoricalLocations(historicalLocations);
        thing.setTaskingCapabilities(taskingCapabilities);
        return thing;
    }

}
