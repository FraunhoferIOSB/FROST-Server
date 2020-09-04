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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories;

import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ResultType;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 *
 * @param <J> The type of the ID fields.
 */
public class ObservationFactory<J extends Comparable> implements EntityFactory<Observation, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableObservations<J> table;
    private final TableCollection<J> tableCollection;

    public ObservationFactory(EntityFactories<J> factories, AbstractTableObservations<J> table) {
        this.entityFactories = factories;
        this.table = table;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Observation newObservation) throws NoSuchEntityException, IncompleteEntityException {
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

        Map<Field, Object> insert = new HashMap<>();

        if (ds != null) {
            insert.put(table.getDatastreamId(), ds.getId().getValue());
        }
        if (mds != null) {
            insert.put(table.getMultiDatastreamId(), mds.getId().getValue());
        }

        TimeValue phenomenonTime = newObservation.getPhenomenonTime();
        if (phenomenonTime == null) {
            phenomenonTime = TimeInstant.now();
        }
        EntityFactories.insertTimeValue(insert, table.colPhenomenonTimeStart, table.colPhenomenonTimeEnd, phenomenonTime);
        EntityFactories.insertTimeInstant(insert, table.colResultTime, newObservation.getResultTime());
        EntityFactories.insertTimeInterval(insert, table.colValidTimeStart, table.colValidTimeEnd, newObservation.getValidTime());

        handleResult(newObservation, newIsMultiDatastream, pm, insert);

        if (newObservation.getResultQuality() != null) {
            insert.put(table.colResultQuality, EntityFactories.objectToJson(newObservation.getResultQuality()));
        }
        insert.put(table.colParameters, EntityFactories.objectToJson(newObservation.getParameters()));
        insert.put(table.getFeatureId(), f.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), newObservation);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J generatedId = result.component1();
        LOGGER.debug("Inserted Observation. Created id = {}.", generatedId);
        newObservation.setId(entityFactories.idFromObject(generatedId));
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Observation newObservation, J id) throws IncompleteEntityException {
        Observation oldObservation = (Observation) pm.get(EntityType.OBSERVATION, entityFactories.idFromObject(id));

        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        boolean newHasDatastream = checkDatastreamSet(newObservation, oldObservation, message, update, pm);
        boolean newIsMultiDatastream = checkMultiDatastreamSet(oldObservation, newObservation, message, update, pm);

        if (newHasDatastream == newIsMultiDatastream) {
            throw new IllegalArgumentException("Observation must have either a Datastream or a MultiDatastream.");
        }
        if (newObservation.isSetFeatureOfInterest()) {
            if (!entityFactories.entityExists(pm, newObservation.getFeatureOfInterest())) {
                throw new IncompleteEntityException("FeatureOfInterest not found.");
            }
            update.put(table.getFeatureId(), newObservation.getFeatureOfInterest().getId().getValue());
            message.addField(NavigationPropertyMain.FEATUREOFINTEREST);
        }
        if (newObservation.isSetParameters()) {
            update.put(table.colParameters, EntityFactories.objectToJson(newObservation.getParameters()));
            message.addField(EntityPropertyMain.PARAMETERS);
        }
        if (newObservation.isSetPhenomenonTime()) {
            if (newObservation.getPhenomenonTime() == null) {
                throw new IncompleteEntityException("phenomenonTime" + CAN_NOT_BE_NULL);
            }
            EntityFactories.insertTimeValue(update, table.colPhenomenonTimeStart, table.colPhenomenonTimeEnd, newObservation.getPhenomenonTime());
            message.addField(EntityPropertyMain.PHENOMENONTIME);
        }

        if (newObservation.isSetResult()) {
            handleResult(newObservation, newIsMultiDatastream, pm, update);
            message.addField(EntityPropertyMain.RESULT);
        }

        if (newObservation.isSetResultQuality()) {
            update.put(table.colResultQuality, EntityFactories.objectToJson(newObservation.getResultQuality()));
            message.addField(EntityPropertyMain.RESULTQUALITY);
        }
        if (newObservation.isSetResultTime()) {
            EntityFactories.insertTimeInstant(update, table.colResultTime, newObservation.getResultTime());
            message.addField(EntityPropertyMain.RESULTTIME);
        }
        if (newObservation.isSetValidTime()) {
            EntityFactories.insertTimeInterval(update, table.colValidTimeStart, table.colValidTimeEnd, newObservation.getValidTime());
            message.addField(EntityPropertyMain.VALIDTIME);
        }

        long count = 0;
        if (!update.isEmpty()) {
            DSLContext dslContext = pm.getDslContext();
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(id))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Observation {} caused {} rows to change!", id, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Observation {}", id);
        return message;
    }

    private void handleResult(Observation newObservation, boolean newIsMultiDatastream, PostgresPersistenceManager<J> pm, Map<Field, Object> record) {
        Object result = newObservation.getResult();
        if (newIsMultiDatastream) {
            if (!(result instanceof List)) {
                throw new IllegalArgumentException("Multidatastream only accepts array results.");
            }
            List list = (List) result;
            MultiDatastream mds = newObservation.getMultiDatastream();
            J mdsId = (J) mds.getId().getValue();
            AbstractTableMultiDatastreamsObsProperties<J> tableMdsOps = tableCollection.getTableMultiDatastreamsObsProperties();
            Integer count = pm.getDslContext()
                    .selectCount()
                    .from(tableMdsOps)
                    .where(tableMdsOps.getMultiDatastreamId().eq(mdsId))
                    .fetchOne().component1();
            if (count != list.size()) {
                throw new IllegalArgumentException("Size of result array (" + list.size() + ") must match number of observed properties (" + count + ") in the MultiDatastream.");
            }
        }

        if (result instanceof Number) {
            record.put(table.colResultType, ResultType.NUMBER.sqlValue());
            record.put(table.colResultString, result.toString());
            record.put(table.colResultNumber, ((Number) result).doubleValue());
            record.put(table.colResultBoolean, null);
            record.put(table.colResultJson, null);
        } else if (result instanceof Boolean) {
            record.put(table.colResultType, ResultType.BOOLEAN.sqlValue());
            record.put(table.colResultString, result.toString());
            record.put(table.colResultBoolean, result);
            record.put(table.colResultNumber, null);
            record.put(table.colResultJson, null);
        } else if (result instanceof String) {
            record.put(table.colResultType, ResultType.STRING.sqlValue());
            record.put(table.colResultString, result.toString());
            record.put(table.colResultNumber, null);
            record.put(table.colResultBoolean, null);
            record.put(table.colResultJson, null);
        } else {
            record.put(table.colResultType, ResultType.OBJECT_ARRAY.sqlValue());
            record.put(table.colResultJson, EntityFactories.objectToJson(result));
            record.put(table.colResultString, null);
            record.put(table.colResultNumber, null);
            record.put(table.colResultBoolean, null);
        }
    }

    private boolean checkMultiDatastreamSet(Observation oldObservation, Observation newObservation, EntityChangedMessage message, Map<Field, Object> update, PostgresPersistenceManager<J> pm) throws IncompleteEntityException {
        MultiDatastream mds = oldObservation.getMultiDatastream();
        boolean newHasMultiDatastream = mds != null;
        if (newObservation.isSetMultiDatastream()) {
            mds = newObservation.getMultiDatastream();
            if (mds == null) {
                newHasMultiDatastream = false;
                update.put(table.getMultiDatastreamId(), null);
                message.addField(NavigationPropertyMain.MULTIDATASTREAM);
            } else {
                if (!entityFactories.entityExists(pm, mds)) {
                    throw new IncompleteEntityException("MultiDatastream not found.");
                }
                newHasMultiDatastream = true;
                update.put(table.getMultiDatastreamId(), mds.getId().getValue());
                message.addField(NavigationPropertyMain.MULTIDATASTREAM);
            }
        }
        return newHasMultiDatastream;
    }

    private boolean checkDatastreamSet(Observation newObservation, Observation oldObservation, EntityChangedMessage message, Map<Field, Object> update, PostgresPersistenceManager<J> pm) throws IncompleteEntityException {
        Datastream ds = oldObservation.getDatastream();
        boolean newHasDatastream = ds != null;
        if (newObservation.isSetDatastream()) {
            if (newObservation.getDatastream() == null) {
                newHasDatastream = false;
                update.put(table.getDatastreamId(), null);
                message.addField(NavigationPropertyMain.DATASTREAM);
            } else {
                if (!entityFactories.entityExists(pm, newObservation.getDatastream())) {
                    throw new IncompleteEntityException("Datastream not found.");
                }
                newHasDatastream = true;
                ds = newObservation.getDatastream();
                update.put(table.getDatastreamId(), ds.getId().getValue());
                message.addField(NavigationPropertyMain.DATASTREAM);
            }
        }
        return newHasDatastream;
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.getDslContext()
                .delete(table)
                .where(table.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Observation " + entityId + " not found.");
        }
    }

}
