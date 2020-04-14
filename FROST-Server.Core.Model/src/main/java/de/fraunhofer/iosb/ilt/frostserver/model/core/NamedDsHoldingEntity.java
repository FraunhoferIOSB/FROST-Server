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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import java.util.Objects;

/**
 * A named entity that also a list of Datastreams and a list of
 * MultiDatastreams.
 *
 * @author jab, scf
 * @param <T> The exact type of the entity.
 */
public abstract class NamedDsHoldingEntity<T extends NamedDsHoldingEntity<T>> extends NamedEntity<T> {

    private EntitySet<Datastream> datastreams; // 0..*
    private EntitySet<MultiDatastream> multiDatastreams; // 0..*

    public NamedDsHoldingEntity() {
        this(null);
    }

    public NamedDsHoldingEntity(Id id) {
        super(id);
        this.datastreams = new EntitySetImpl<>(EntityType.DATASTREAM);
        this.multiDatastreams = new EntitySetImpl<>(EntityType.MULTIDATASTREAM);
    }

    public EntitySet<Datastream> getDatastreams() {
        return datastreams;
    }

    public T setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
        return getThis();
    }

    public T addDatastream(Datastream datastream) {
        if (datastreams == null) {
            datastreams = new EntitySetImpl<>(EntityType.DATASTREAM);
        }
        datastreams.add(datastream);
        return getThis();
    }

    public EntitySet<MultiDatastream> getMultiDatastreams() {
        return multiDatastreams;
    }

    public T setMultiDatastreams(EntitySet<MultiDatastream> multiDatastreams) {
        this.multiDatastreams = multiDatastreams;
        return getThis();
    }

    public T addMultiDatastream(MultiDatastream multiDatastream) {
        if (multiDatastreams == null) {
            multiDatastreams = new EntitySetImpl<>(EntityType.MULTIDATASTREAM);
        }
        multiDatastreams.add(multiDatastream);
        return getThis();
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
