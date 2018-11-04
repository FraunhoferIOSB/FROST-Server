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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFromTupleFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import de.fraunhofer.iosb.ilt.sta.query.Query;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class HistoricalLocationFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<HistoricalLocation, I, J> {

    private final EntityFactories<I, J> factories;
    private final AbstractQHistLocations<?, I, J> qInstance;

    public HistoricalLocationFactory(EntityFactories<I, J> factories, AbstractQHistLocations<?, I, J> qInstance) {
        this.factories = factories;
        this.qInstance = qInstance;
    }

    @Override
    public HistoricalLocation create(Tuple tuple, Query query, DataSize dataSize) {
        HistoricalLocation entity = new HistoricalLocation();
        J id = factories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(factories.idFromObject(id));
        }
        entity.setThing(factories.thingFromId(tuple, qInstance.getThingId()));
        entity.setTime(Utils.instantFromTime(tuple.get(qInstance.time)));
        return entity;
    }

    @Override
    public I getPrimaryKey() {
        return qInstance.getId();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HISTORICALLOCATION;
    }

}
