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
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableSensors;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.HashMap;
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
public class SensorFactory<J extends Comparable> implements EntityFactory<Sensor, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableSensors<J> table;
    private final TableCollection<J> tableCollection;

    public SensorFactory(EntityFactories<J> factories, AbstractTableSensors<J> table) {
        this.entityFactories = factories;
        this.table = table;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Sensor s) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> insert = new HashMap<>();
        insert.put(table.colName, s.getName());
        insert.put(table.colDescription, s.getDescription());
        insert.put(table.colEncodingType, s.getEncodingType());
        // We currently assume it's a string.
        insert.put(table.colMetadata, s.getMetadata().toString());
        insert.put(table.colProperties, new JsonValue(s.getProperties()));

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), s);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J generatedId = result.component1();
        LOGGER.debug("Inserted Sensor. Created id = {}.", generatedId);
        s.setId(entityFactories.idFromObject(generatedId));

        // Create new datastreams, if any.
        for (Datastream ds : s.getDatastreams()) {
            ds.setSensor(new Sensor(s.getId()));
            ds.complete();
            pm.insert(ds);
        }

        // Create new multiDatastreams, if any.
        for (MultiDatastream mds : s.getMultiDatastreams()) {
            mds.setSensor(new Sensor(s.getId()));
            mds.complete();
            pm.insert(mds);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Sensor s, J sensorId) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        if (s.isSetName()) {
            if (s.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.colName, s.getName());
            message.addField(EntityPropertyMain.NAME);
        }
        if (s.isSetDescription()) {
            if (s.getDescription() == null) {
                throw new IncompleteEntityException(EntityPropertyMain.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.colDescription, s.getDescription());
            message.addField(EntityPropertyMain.DESCRIPTION);
        }
        if (s.isSetEncodingType()) {
            if (s.getEncodingType() == null) {
                throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
            }
            update.put(table.colEncodingType, s.getEncodingType());
            message.addField(EntityPropertyMain.ENCODINGTYPE);
        }
        if (s.isSetMetadata()) {
            if (s.getMetadata() == null) {
                throw new IncompleteEntityException("metadata" + CAN_NOT_BE_NULL);
            }
            // We currently assume it's a string.
            update.put(table.colMetadata, s.getMetadata().toString());
            message.addField(EntityPropertyMain.METADATA);
        }
        if (s.isSetProperties()) {
            update.put(table.colProperties, new JsonValue(s.getProperties()));
            message.addField(EntityPropertyMain.PROPERTIES);
        }

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(sensorId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Sensor {} caused {} rows to change!", sensorId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingDatastreams(s, pm, dslContext, sensorId);

        linkExistingMultiDatastreams(s, pm, dslContext, sensorId);

        LOGGER.debug("Updated Sensor {}", sensorId);
        return message;
    }

    private void linkExistingMultiDatastreams(Sensor s, PostgresPersistenceManager<J> pm, DSLContext dslContext, J sensorId) throws NoSuchEntityException {
        for (MultiDatastream mds : s.getMultiDatastreams()) {
            if (mds.getId() == null || !entityFactories.entityExists(pm, mds)) {
                throw new NoSuchEntityException("MultiDatastream" + NO_ID_OR_NOT_FOUND);
            }
            J mdsId = (J) mds.getId().getValue();
            AbstractTableMultiDatastreams<J> qmds = tableCollection.getTableMultiDatastreams();
            long mdsCount = dslContext.update(qmds)
                    .set(qmds.getSensorId(), sensorId)
                    .where(qmds.getId().eq(mdsId))
                    .execute();
            if (mdsCount > 0) {
                LOGGER.debug("Assigned multiDatastream {} to sensor {}.", mdsId, sensorId);
            }
        }
    }

    private void linkExistingDatastreams(Sensor s, PostgresPersistenceManager<J> pm, DSLContext dslContext, J sensorId) throws NoSuchEntityException {
        for (Datastream ds : s.getDatastreams()) {
            if (ds.getId() == null || !entityFactories.entityExists(pm, ds)) {
                throw new NoSuchEntityException("Datastream" + NO_ID_OR_NOT_FOUND);
            }
            J dsId = (J) ds.getId().getValue();
            AbstractTableDatastreams<J> qds = tableCollection.getTableDatastreams();
            long dsCount = dslContext.update(qds)
                    .set(qds.getSensorId(), sensorId)
                    .where(qds.getId().eq(dsId))
                    .execute();
            if (dsCount > 0) {
                LOGGER.debug("Assigned datastream {} to sensor {}.", dsId, sensorId);
            }
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.getDslContext()
                .delete(table)
                .where(table.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Sensor " + entityId + " not found.");
        }
    }

}
