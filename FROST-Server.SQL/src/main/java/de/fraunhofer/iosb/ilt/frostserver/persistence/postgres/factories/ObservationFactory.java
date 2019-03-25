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
package de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.factories;

import com.querydsl.core.Tuple;
import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.EntityFactories;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.EntityFactories.CHANGED_MULTIPLE_ROWS;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.ResultType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.NoSuchEntityException;
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

        readResultFromDb(tuple, entity, dataSize, select);
        readResultQuality(select, tuple, dataSize, entity);

        entity.setResultTime(Utils.instantFromTime(tuple.get(qInstance.resultTime)));
        Timestamp vTimeStart = tuple.get(qInstance.validTimeStart);
        Timestamp vTimeEnd = tuple.get(qInstance.validTimeEnd);
        if (vTimeStart != null && vTimeEnd != null) {
            entity.setValidTime(Utils.intervalFromTimes(vTimeStart, vTimeEnd));
        }
        return entity;
    }

    private void readResultQuality(Set<Property> select, Tuple tuple, DataSize dataSize, Observation entity) {
        if (select.isEmpty() || select.contains(EntityProperty.RESULTQUALITY)) {
            String resultQuality = tuple.get(qInstance.resultQuality);
            dataSize.increase(resultQuality == null ? 0 : resultQuality.length());
            entity.setResultQuality(Utils.jsonToObject(resultQuality, Object.class));
        }
    }

    private void readResultFromDb(Tuple tuple, Observation entity, DataSize dataSize, Set<Property> select) {
        if (!select.isEmpty() && !select.contains(EntityProperty.RESULT)) {
            return;
        }
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

    @Override
    public boolean insert(PostgresPersistenceManager<I, J> pm, Observation newObservation) throws NoSuchEntityException, IncompleteEntityException {
        Datastream ds = newObservation.getDatastream();
        MultiDatastream mds = newObservation.getMultiDatastream();
        Id streamId;
        boolean newIsMultiDatastream = false;
        if (ds != null) {
            entityFactories.entityExistsOrCreate(pm, ds);
            streamId = ds.getId();
        } else if (mds != null) {
            entityFactories.entityExistsOrCreate(pm, mds);
            streamId = mds.getId();
            newIsMultiDatastream = true;
        } else {
            throw new IncompleteEntityException("Missing Datastream or MultiDatastream.");
        }

        FeatureOfInterest f = newObservation.getFeatureOfInterest();
        if (f == null) {
            f = entityFactories.generateFeatureOfInterest(pm, streamId, newIsMultiDatastream);
        } else {
            entityFactories.entityExistsOrCreate(pm, f);
        }

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
        SQLInsertClause query = qFactory.insert(qo);

        if (ds != null) {
            query.set(qo.getDatastreamId(), (J) ds.getId().getValue());
        }
        if (mds != null) {
            query.set(qo.getMultiDatastreamId(), (J) mds.getId().getValue());
        }

        TimeValue phenomenonTime = newObservation.getPhenomenonTime();
        if (phenomenonTime == null) {
            phenomenonTime = TimeInstant.now();
        }
        EntityFactories.insertTimeValue(query, qo.phenomenonTimeStart, qo.phenomenonTimeEnd, phenomenonTime);
        EntityFactories.insertTimeInstant(query, qo.resultTime, newObservation.getResultTime());
        EntityFactories.insertTimeInterval(query, qo.validTimeStart, qo.validTimeEnd, newObservation.getValidTime());

        handleResult(newObservation, newIsMultiDatastream, pm, query, qo);

        if (newObservation.getResultQuality() != null) {
            query.set(qo.resultQuality, newObservation.getResultQuality().toString());
        }
        query.set(qo.parameters, EntityFactories.objectToJson(newObservation.getParameters()));
        query.set(qo.getFeatureId(), (J) f.getId().getValue());

        entityFactories.insertUserDefinedId(pm, query, qo.getId(), newObservation);

        J generatedId = query.executeWithKey(qo.getId());
        LOGGER.debug("Inserted Observation. Created id = {}.", generatedId);
        newObservation.setId(entityFactories.idFromObject(generatedId));
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, Observation newObservation, J id) throws IncompleteEntityException {
        Observation oldObservation = (Observation) pm.get(EntityType.OBSERVATION, entityFactories.idFromObject(id));

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
        SQLUpdateClause query = qFactory.update(qo);
        EntityChangedMessage message = new EntityChangedMessage();

        boolean newHasDatastream = checkDatastreamSet(newObservation, oldObservation, message, query, qo, pm);
        boolean newIsMultiDatastream = checkMultiDatastreamSet(oldObservation, newObservation, message, query, qo, pm);

        if (newHasDatastream == newIsMultiDatastream) {
            throw new IllegalArgumentException("Observation must have either a Datastream or a MultiDatastream.");
        }
        if (newObservation.isSetFeatureOfInterest()) {
            if (!entityFactories.entityExists(pm, newObservation.getFeatureOfInterest())) {
                throw new IncompleteEntityException("FeatureOfInterest not found.");
            }
            query.set(qo.getFeatureId(), (J) newObservation.getFeatureOfInterest().getId().getValue());
            message.addField(NavigationProperty.FEATUREOFINTEREST);
        }
        if (newObservation.isSetParameters()) {
            query.set(qo.parameters, EntityFactories.objectToJson(newObservation.getParameters()));
            message.addField(EntityProperty.PARAMETERS);
        }
        if (newObservation.isSetPhenomenonTime()) {
            if (newObservation.getPhenomenonTime() == null) {
                throw new IncompleteEntityException("phenomenonTime" + CAN_NOT_BE_NULL);
            }
            EntityFactories.insertTimeValue(query, qo.phenomenonTimeStart, qo.phenomenonTimeEnd, newObservation.getPhenomenonTime());
            message.addField(EntityProperty.PHENOMENONTIME);
        }

        if (newObservation.isSetResult()) {
            handleResult(newObservation, newIsMultiDatastream, pm, query, qo);
            message.addField(EntityProperty.RESULT);
        }

        if (newObservation.isSetResultQuality()) {
            query.set(qo.resultQuality, EntityFactories.objectToJson(newObservation.getResultQuality()));
            message.addField(EntityProperty.RESULTQUALITY);
        }
        if (newObservation.isSetResultTime()) {
            EntityFactories.insertTimeInstant(query, qo.resultTime, newObservation.getResultTime());
            message.addField(EntityProperty.RESULTTIME);
        }
        if (newObservation.isSetValidTime()) {
            EntityFactories.insertTimeInterval(query, qo.validTimeStart, qo.validTimeEnd, newObservation.getValidTime());
            message.addField(EntityProperty.VALIDTIME);
        }
        query.where(qo.getId().eq(id));
        long count = 0;
        if (!query.isEmpty()) {
            count = query.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Observation {} caused {} rows to change!", id, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Observation {}", id);
        return message;
    }

    private void handleResult(Observation newObservation, boolean newIsMultiDatastream, PostgresPersistenceManager<I, J> pm, StoreClause query, AbstractQObservations<? extends AbstractQObservations, I, J> qo) {
        Object result = newObservation.getResult();
        if (newIsMultiDatastream) {
            MultiDatastream mds = newObservation.getMultiDatastream();
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
            query.set(qo.resultType, ResultType.NUMBER.sqlValue());
            query.set(qo.resultString, result.toString());
            query.set(qo.resultNumber, ((Number) result).doubleValue());
            query.setNull(qo.resultBoolean);
            query.setNull(qo.resultJson);
        } else if (result instanceof Boolean) {
            query.set(qo.resultType, ResultType.BOOLEAN.sqlValue());
            query.set(qo.resultString, result.toString());
            query.set(qo.resultBoolean, result);
            query.setNull(qo.resultNumber);
            query.setNull(qo.resultJson);
        } else if (result instanceof String) {
            query.set(qo.resultType, ResultType.STRING.sqlValue());
            query.set(qo.resultString, result.toString());
            query.setNull(qo.resultNumber);
            query.setNull(qo.resultBoolean);
            query.setNull(qo.resultJson);
        } else {
            query.set(qo.resultType, ResultType.OBJECT_ARRAY.sqlValue());
            query.set(qo.resultJson, EntityFactories.objectToJson(result));
            query.setNull(qo.resultString);
            query.setNull(qo.resultNumber);
            query.setNull(qo.resultBoolean);
        }
    }

    private boolean checkMultiDatastreamSet(Observation oldObservation, Observation newObservation, EntityChangedMessage message, SQLUpdateClause update, AbstractQObservations<? extends AbstractQObservations, I, J> qo, PostgresPersistenceManager<I, J> pm) throws IncompleteEntityException {
        MultiDatastream mds = oldObservation.getMultiDatastream();
        boolean newHasMultiDatastream = mds != null;
        if (newObservation.isSetMultiDatastream()) {
            mds = newObservation.getMultiDatastream();
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
        return newHasMultiDatastream;
    }

    private boolean checkDatastreamSet(Observation newObservation, Observation oldObservation, EntityChangedMessage message, SQLUpdateClause update, AbstractQObservations<? extends AbstractQObservations, I, J> qo, PostgresPersistenceManager<I, J> pm) throws IncompleteEntityException {
        Datastream ds = oldObservation.getDatastream();
        boolean newHasDatastream = ds != null;
        if (newObservation.isSetDatastream()) {
            if (newObservation.getDatastream() == null) {
                newHasDatastream = false;
                update.setNull(qo.getDatastreamId());
                message.addField(NavigationProperty.DATASTREAM);
            } else {
                if (!entityFactories.entityExists(pm, newObservation.getDatastream())) {
                    throw new IncompleteEntityException("Datastream not found.");
                }
                newHasDatastream = true;
                ds = newObservation.getDatastream();
                update.set(qo.getDatastreamId(), (J) ds.getId().getValue());
                message.addField(NavigationProperty.DATASTREAM);
            }
        }
        return newHasDatastream;
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
