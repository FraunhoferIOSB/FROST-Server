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
package de.fraunhofer.iosb.ilt.sta.model;

import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.core.NamedDsHoldingEntity;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.Objects;

/**
 *
 * @author jab, scf
 */
public class Thing extends NamedDsHoldingEntity<Thing> {

    private EntitySet<Location> locations; // 0..*
    private EntitySet<HistoricalLocation> historicalLocations; // 0..*

    public Thing() {
        this(null);
    }

    public Thing(Id id) {
        super(id);
        this.locations = new EntitySetImpl<>(EntityType.LOCATION);
        this.historicalLocations = new EntitySetImpl<>(EntityType.HISTORICALLOCATION);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.THING;
    }

    public EntitySet<Location> getLocations() {
        return locations;
    }

    public void setLocations(EntitySet<Location> locations) {
        this.locations = locations;
    }

    public EntitySet<HistoricalLocation> getHistoricalLocations() {
        return historicalLocations;
    }

    public void setHistoricalLocations(EntitySet<HistoricalLocation> historicalLocations) {
        this.historicalLocations = historicalLocations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), locations, historicalLocations);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Thing other = (Thing) obj;
        return super.equals(other)
                && Objects.equals(locations, other.locations)
                && Objects.equals(historicalLocations, other.historicalLocations);
    }
}
