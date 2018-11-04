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
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFromTupleFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.ResultType;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class ObservationFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<Observation, I, J> {

    private final EntityFactories<I, J> factories;
    private final AbstractQObservations<?, I, J> qInstance;

    public ObservationFactory(EntityFactories<I, J> factories, AbstractQObservations<?, I, J> qInstance) {
        this.factories = factories;
        this.qInstance = qInstance;
    }

    @Override
    public Observation create(Tuple tuple, Query query, DataSize dataSize) {
        Observation entity = new Observation();
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        J dsId = factories.getIdFromTuple(tuple, qInstance.getDatastreamId());
        if (dsId != null) {
            entity.setDatastream(factories.datastreamFromId(dsId));
        }
        J mDsId = factories.getIdFromTuple(tuple, qInstance.getMultiDatastreamId());
        if (mDsId != null) {
            entity.setMultiDatastream(factories.multiDatastreamFromId(mDsId));
        }
        entity.setFeatureOfInterest(factories.featureOfInterestFromId(tuple, qInstance.getFeatureId()));
        J id = factories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(factories.idFromObject(id));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PARAMETERS)) {
            String props = tuple.get(qInstance.parameters);
            dataSize.increase(props == null ? 0 : props.length());
            entity.setParameters(Utils.jsonToObject(props, Map.class));
        }
        Timestamp pTimeStart = tuple.get(qInstance.phenomenonTimeStart);
        Timestamp pTimeEnd = tuple.get(qInstance.phenomenonTimeEnd);
        entity.setPhenomenonTime(Utils.valueFromTimes(pTimeStart, pTimeEnd));
        if (select.isEmpty() || select.contains(EntityProperty.RESULT)) {
            Byte resultTypeOrd = tuple.get(qInstance.resultType);
            if (resultTypeOrd != null) {
                ResultType resultType = ResultType.fromSqlValue(resultTypeOrd);
                switch (resultType) {
                    case BOOLEAN:
                        entity.setResult(tuple.get(qInstance.resultBoolean));
                        break;
                    case NUMBER:
                        try {
                            entity.setResult(new BigDecimal(tuple.get(qInstance.resultString)));
                        } catch (NumberFormatException e) {
                            // It was not a Number? Use the double value.
                            entity.setResult(tuple.get(qInstance.resultNumber));
                        }
                        break;
                    case OBJECT_ARRAY:
                        String jsonData = tuple.get(qInstance.resultJson);
                        dataSize.increase(jsonData == null ? 0 : jsonData.length());
                        entity.setResult(Utils.jsonToTree(jsonData));
                        break;
                    case STRING:
                        String stringData = tuple.get(qInstance.resultString);
                        dataSize.increase(stringData == null ? 0 : stringData.length());
                        entity.setResult(stringData);
                        break;
                }
            }
        }
        if (select.isEmpty() || select.contains(EntityProperty.RESULTQUALITY)) {
            String resultQuality = tuple.get(qInstance.resultQuality);
            dataSize.increase(resultQuality == null ? 0 : resultQuality.length());
            entity.setResultQuality(Utils.jsonToObject(resultQuality, Object.class));
        }
        entity.setResultTime(Utils.instantFromTime(tuple.get(qInstance.resultTime)));
        Timestamp vTimeStart = tuple.get(qInstance.validTimeStart);
        Timestamp vTimeEnd = tuple.get(qInstance.validTimeEnd);
        if (vTimeStart != null && vTimeEnd != null) {
            entity.setValidTime(Utils.intervalFromTimes(vTimeStart, vTimeEnd));
        }
        return entity;
    }

    @Override
    public I getPrimaryKey() {
        return qInstance.getId();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.OBSERVATION;
    }

}
