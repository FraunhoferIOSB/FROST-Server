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
package de.fraunhofer.iosb.ilt.sta.model.core;

import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.Map;
import java.util.Objects;

/**
 * A named entity that also a list of Datastreams and a list of
 * MultiDatastreams.
 *
 * @author jab, scf
 */
public abstract class NamedDsHoldingEntity extends NamedEntity {

    private EntitySet<Datastream> datastreams; // 0..*
    private EntitySet<MultiDatastream> multiDatastreams; // 0..*

    public NamedDsHoldingEntity() {
        this.datastreams = new EntitySetImpl<>(EntityType.DATASTREAM);
        this.multiDatastreams = new EntitySetImpl<>(EntityType.MULTIDATASTREAM);
    }

    public NamedDsHoldingEntity(Id id,
            String selfLink,
            String navigationLink,
            String name,
            String description,
            Map<String, Object> properties,
            EntitySet<Datastream> datastreams,
            EntitySet<MultiDatastream> multiDatastreams) {
        super(id, selfLink, navigationLink, name, description, properties);
        this.datastreams = datastreams;
        this.multiDatastreams = multiDatastreams;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.THING;
    }

    public EntitySet<Datastream> getDatastreams() {
        return datastreams;
    }

    public void setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
    }

    public EntitySet<MultiDatastream> getMultiDatastreams() {
        return multiDatastreams;
    }

    public void setMultiDatastreams(EntitySet<MultiDatastream> multiDatastreams) {
        this.multiDatastreams = multiDatastreams;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), datastreams, multiDatastreams);
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
        final NamedDsHoldingEntity other = (NamedDsHoldingEntity) obj;
        return super.equals(other)
                && Objects.equals(datastreams, other.datastreams)
                && Objects.equals(multiDatastreams, other.multiDatastreams);
    }
}
