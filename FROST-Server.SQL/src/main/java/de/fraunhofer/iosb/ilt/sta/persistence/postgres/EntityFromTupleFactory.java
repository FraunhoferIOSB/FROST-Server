/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.query.Query;

/**
 *
 * @author scf
 * @param <T>
 */
public interface EntityFromTupleFactory<T extends Entity,I extends Path<J>, J> {

    /**
     * Creates a T, reading the Tuple with a qObject using no alias.
     *
     * @param tuple The tuple to create the Entity from.
     * @param query The query used to request the data.
     * @param dataSize The counter for the data size. This counts only the
     * variable-sided elements, such as Observation.result and Thing.properties.
     * @return The Entity created from the Tuple.
     */
    public T create(Tuple tuple, Query query, DataSize dataSize);

    /**
     * Get the primary key of the table of the entity this factory
     *
     * @return The primary key of the table of the entity this factory creates,
     * using no alias.
     */
    public I getPrimaryKey();

    /**
     * Get the EntityType of the Entities created by this factory.
     *
     * @return The EntityType of the Entities created by this factory.
     */
    public EntityType getEntityType();

}
