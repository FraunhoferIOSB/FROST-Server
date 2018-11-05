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
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CHANGED_MULTIPLE_ROWS;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.ResultType;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class ObservationFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFactory<Observation, I, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationFactory.class);

    private final EntityFactories<I, J> entityFactories;
    private final AbstractQObservations<?, I, J> qInstance;
    private final QCollection<I, J> qCollection;

    public ObservationFactory(EntityFactories<I, J> factories, AbstractQObservations<?, I, J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public Observation create(Tuple tuple, Query query, DataSize dataSize) {
        Observation entity = new Observation();
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        J dsId = entityFactories.getIdFromTuple(tuple, qInstance.getDatastreamId());
        if (dsId != null) {
            entity.setDatastream(entityFactories.datastreamFromId(dsId));
        }
        J mDsId = entityFactories.getIdFromTuple(tuple, qInstance.getMultiDatastreamId());
        if (mDsId != null) {
            entity.setMultiDatastream(entityFactories.multiDatastreamFromId(mDsId));
        }
        entity.setFeatureOfInterest(entityFactories.featureOfInterestFromId(tuple, qInstance.getFeatureId()));
        J id = entityFactories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
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
    public boolean insert(PostgresPersistenceManager<I, J> pm, Observation o) throws NoSuchEntityException, IncompleteEntityException {
        Datastream ds = o.getDatastream();
        MultiDatastream mds = o.getMultiDatastream();
        Id streamId;
        boolean isMultiDatastream = false;
        if (ds != null) {
            entityFactories.entityExistsOrCreate(pm, ds);
            streamId = ds.getId();
        } else if (mds != null) {
            entityFactories.entityExistsOrCreate(pm, mds);
            streamId = mds.getId();
            isMultiDatastream = true;
        } else {
            throw new IncompleteEntityException("Missing Datastream or MultiDatastream.");
        }

        FeatureOfInterest f = o.getFeatureOfInterest();
        if (f == null) {
            f = entityFactories.generateFeatureOfInterest(pm, streamId, isMultiDatastream);
        } else {
            entityFactories.entityExistsOrCreate(pm, f);
        }

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
        SQLInsertClause insert = qFactory.insert(qo);

        insert.set(qo.parameters, EntityFactories.objectToJson(o.getParameters()));
        TimeValue phenomenonTime = o.getPhenomenonTime();
        if (phenomenonTime == null) {
            phenomenonTime = TimeInstant.now();
        }
        EntityFactories.insertTimeValue(insert, qo.phenomenonTimeStart, qo.phenomenonTimeEnd, phenomenonTime);
        EntityFactories.insertTimeInstant(insert, qo.resultTime, o.getResultTime());
        EntityFactories.insertTimeInterval(insert, qo.validTimeStart, qo.validTimeEnd, o.getValidTime());

        Object result = o.getResult();
        if (isMultiDatastream) {
            if (!(result instanceof List)) {
                throw new IllegalArgumentException("Multidatastream only accepts array results.");
            }
            List list = (List) result;
            ResourcePath path = mds.getPath();
            path.addPathElement(new EntitySetPathElement(EntityType.OBSERVEDPROPERTY, null), false, false);
            long count = pm.count(path, null);
            if (count != list.size()) {
                throw new IllegalArgumentException("Size of result array (" + list.size() + ") must match number of observed properties (" + count + ") in the MultiDatastream.");
            }
        }

        if (result instanceof Number) {
            insert.set(qo.resultType, ResultType.NUMBER.sqlValue());
            insert.set(qo.resultString, result.toString());
            insert.set(qo.resultNumber, ((Number) result).doubleValue());
        } else if (result instanceof Boolean) {
            insert.set(qo.resultType, ResultType.BOOLEAN.sqlValue());
            insert.set(qo.resultString, result.toString());
            insert.set(qo.resultBoolean, (Boolean) result);
        } else if (result instanceof String) {
            insert.set(qo.resultType, ResultType.STRING.sqlValue());
            insert.set(qo.resultString, result.toString());
        } else {
            insert.set(qo.resultType, ResultType.OBJECT_ARRAY.sqlValue());
            insert.set(qo.resultJson, EntityFactories.objectToJson(result));
        }

        if (o.getResultQuality() != null) {
            insert.set(qo.resultQuality, o.getResultQuality().toString());
        }
        if (ds != null) {
            insert.set(qo.getDatastreamId(), (J) ds.getId().getValue());
        }
        if (mds != null) {
            insert.set(qo.getMultiDatastreamId(), (J) mds.getId().getValue());
        }
        insert.set(qo.getFeatureId(), (J) f.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, qo.getId(), o);

        J generatedId = insert.executeWithKey(qo.getId());
        LOGGER.debug("Inserted Observation. Created id = {}.", generatedId);
        o.setId(entityFactories.idFromObject(generatedId));
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, Observation o, J id) throws IncompleteEntityException {
        Observation oldObservation = (Observation) pm.get(EntityType.OBSERVATION, entityFactories.idFromObject(id));
        Datastream ds = oldObservation.getDatastream();
        MultiDatastream mds = oldObservation.getMultiDatastream();
        boolean newHasDatastream = ds != null;
        boolean newHasMultiDatastream = mds != null;

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
        SQLUpdateClause update = qFactory.update(qo);
        EntityChangedMessage message = new EntityChangedMessage();

        if (o.isSetDatastream()) {
            if (o.getDatastream() == null) {
                newHasDatastream = false;
                update.setNull(qo.getDatastreamId());
                message.addField(NavigationProperty.DATASTREAM);
            } else {
                if (!entityFactories.entityExists(pm, o.getDatastream())) {
                    throw new IncompleteEntityException("Datastream not found.");
                }
                newHasDatastream = true;
                ds = o.getDatastream();
                update.set(qo.getDatastreamId(), (J) ds.getId().getValue());
                message.addField(NavigationProperty.DATASTREAM);
            }
        }
        if (o.isSetMultiDatastream()) {
            mds = o.getMultiDatastream();
            if (mds == null) {
                newHasMultiDatastream = false;
                update.setNull(qo.getMultiDatastreamId());
                message.addField(NavigationProperty.MULTIDATASTREAM);
            } else {
                if (!entityFactories.entityExists(pm, mds)) {
                    throw new IncompleteEntityException("MultiDatastream not found.");
                }
                newHasMultiDatastream = true;
                update.set(qo.getMultiDatastreamId(), (J) mds.getId().getValue());
                message.addField(NavigationProperty.MULTIDATASTREAM);
            }
        }
        if (newHasDatastream == newHasMultiDatastream) {
            throw new IllegalArgumentException("Observation must have either a Datastream or a MultiDatastream.");
        }
        if (o.isSetFeatureOfInterest()) {
            if (!entityFactories.entityExists(pm, o.getFeatureOfInterest())) {
                throw new IncompleteEntityException("FeatureOfInterest not found.");
            }
            update.set(qo.getFeatureId(), (J) o.getFeatureOfInterest().getId().getValue());
            message.addField(NavigationProperty.FEATUREOFINTEREST);
        }
        if (o.isSetParameters()) {
            update.set(qo.parameters, EntityFactories.objectToJson(o.getParameters()));
            message.addField(EntityProperty.PARAMETERS);
        }
        if (o.isSetPhenomenonTime()) {
            if (o.getPhenomenonTime() == null) {
                throw new IncompleteEntityException("phenomenonTime" + CAN_NOT_BE_NULL);
            }
            EntityFactories.insertTimeValue(update, qo.phenomenonTimeStart, qo.phenomenonTimeEnd, o.getPhenomenonTime());
            message.addField(EntityProperty.PHENOMENONTIME);
        }

        if (o.isSetResult() && o.getResult() != null) {
            Object result = o.getResult();
            if (newHasMultiDatastream) {
                if (!(result instanceof List)) {
                    throw new IllegalArgumentException("Multidatastream only accepts array results.");
                }
                List list = (List) result;
                ResourcePath path = mds.getPath();
                path.addPathElement(new EntitySetPathElement(EntityType.OBSERVEDPROPERTY, null), false, false);
                long count = pm.count(path, null);
                if (count != list.size()) {
                    throw new IllegalArgumentException("Size of result array (" + list.size() + ") must match number of observed properties (" + count + ") in the MultiDatastream.");
                }
            }
            if (result instanceof Number) {
                update.set(qo.resultType, ResultType.NUMBER.sqlValue());
                update.set(qo.resultString, result.toString());
                update.set(qo.resultNumber, ((Number) result).doubleValue());
                update.setNull(qo.resultBoolean);
                update.setNull(qo.resultJson);
            } else if (result instanceof Boolean) {
                update.set(qo.resultType, ResultType.BOOLEAN.sqlValue());
                update.set(qo.resultString, result.toString());
                update.set(qo.resultBoolean, (Boolean) result);
                update.setNull(qo.resultNumber);
                update.setNull(qo.resultJson);
            } else if (result instanceof String) {
                update.set(qo.resultType, ResultType.STRING.sqlValue());
                update.set(qo.resultString, result.toString());
                update.setNull(qo.resultNumber);
                update.setNull(qo.resultBoolean);
                update.setNull(qo.resultJson);
            } else {
                update.set(qo.resultType, ResultType.OBJECT_ARRAY.sqlValue());
                update.set(qo.resultJson, EntityFactories.objectToJson(result));
                update.setNull(qo.resultString);
                update.setNull(qo.resultNumber);
                update.setNull(qo.resultBoolean);
            }
            message.addField(EntityProperty.RESULT);
        }

        if (o.isSetResultQuality()) {
            update.set(qo.resultQuality, EntityFactories.objectToJson(o.getResultQuality()));
            message.addField(EntityProperty.RESULTQUALITY);
        }
        if (o.isSetResultTime()) {
            EntityFactories.insertTimeInstant(update, qo.resultTime, o.getResultTime());
            message.addField(EntityProperty.RESULTTIME);
        }
        if (o.isSetValidTime()) {
            EntityFactories.insertTimeInterval(update, qo.validTimeStart, qo.validTimeEnd, o.getValidTime());
            message.addField(EntityProperty.VALIDTIME);
        }
        update.where(qo.getId().eq(id));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Observation {} caused {} rows to change!", id, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Observation {}", id);
        return message;
    }

    @Override
    public void delete(PostgresPersistenceManager<I, J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createQueryFactory()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Observation " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.OBSERVATION;
    }

    @Override
    public I getPrimaryKey() {
        return qInstance.getId();
    }

}
