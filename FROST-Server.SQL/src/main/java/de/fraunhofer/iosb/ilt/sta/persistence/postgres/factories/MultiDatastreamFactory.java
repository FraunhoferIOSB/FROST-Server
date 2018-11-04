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
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFromTupleFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.GeoHelper;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.Polygon;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class MultiDatastreamFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<MultiDatastream, I, J> {

    private final EntityFactories<I, J> factories;
    private final AbstractQMultiDatastreams<?, I, J> qInstance;

    public MultiDatastreamFactory(EntityFactories<I, J> factories, AbstractQMultiDatastreams<?, I, J> qInstance) {
        this.factories = factories;
        this.qInstance = qInstance;
    }

    @Override
    public MultiDatastream create(Tuple tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        MultiDatastream entity = new MultiDatastream();
        entity.setName(tuple.get(qInstance.name));
        entity.setDescription(tuple.get(qInstance.description));
        J id = factories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(factories.idFromObject(id));
        }
        List<String> observationTypes = Utils.jsonToObject(tuple.get(qInstance.observationTypes), EntityFactories.TYPE_LIST_STRING);
        entity.setMultiObservationDataTypes(observationTypes);
        String observedArea = tuple.get(qInstance.observedArea.asText());
        if (observedArea != null) {
            try {
                Polygon polygon = GeoHelper.parsePolygon(observedArea);
                entity.setObservedArea(polygon);
            } catch (IllegalArgumentException e) {
                // It's not a polygon, probably a point or a line.
            }
        }
        Timestamp pTimeStart = tuple.get(qInstance.phenomenonTimeStart);
        Timestamp pTimeEnd = tuple.get(qInstance.phenomenonTimeEnd);
        if (pTimeStart != null && pTimeEnd != null) {
            entity.setPhenomenonTime(Utils.intervalFromTimes(pTimeStart, pTimeEnd));
        }
        Timestamp rTimeEnd = tuple.get(qInstance.resultTimeEnd);
        Timestamp rTimeStart = tuple.get(qInstance.resultTimeStart);
        if (rTimeStart != null && rTimeEnd != null) {
            entity.setResultTime(Utils.intervalFromTimes(rTimeStart, rTimeEnd));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        entity.setSensor(factories.sensorFromId(tuple, qInstance.getSensorId()));
        entity.setThing(factories.thingFromId(tuple, qInstance.getThingId()));
        List<UnitOfMeasurement> units = Utils.jsonToObject(tuple.get(qInstance.unitOfMeasurements), EntityFactories.TYPE_LIST_UOM);
        entity.setUnitOfMeasurements(units);
        return entity;
    }

    @Override
    public I getPrimaryKey() {
        return qInstance.getId();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MULTIDATASTREAM;
    }

}
