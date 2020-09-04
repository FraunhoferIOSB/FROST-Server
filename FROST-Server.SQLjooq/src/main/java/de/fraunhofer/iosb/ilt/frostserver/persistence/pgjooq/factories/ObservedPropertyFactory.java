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
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.HashMap;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 *
 * @param <J> The type of the ID fields.
 */
public class ObservedPropertyFactory<J extends Comparable> implements EntityFactory<ObservedProperty, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservedPropertyFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableObsProperties<J> table;
    private final TableCollection<J> tableCollection;

    public ObservedPropertyFactory(EntityFactories<J> factories, AbstractTableObsProperties<J> table) {
        this.entityFactories = factories;
        this.table = table;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, ObservedProperty op) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> insert = new HashMap<>();
        insert.put(table.colDefinition, op.getDefinition());
        insert.put(table.colName, op.getName());
        insert.put(table.colDescription, op.getDescription());
        insert.put(table.colProperties, new JsonValue(op.getProperties()));

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), op);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J generatedId = result.component1();
        LOGGER.debug("Inserted ObservedProperty. Created id = {}.", generatedId);
        op.setId(entityFactories.idFromObject(generatedId));

        // Create new datastreams, if any.
        for (Datastream ds : op.getDatastreams()) {
            ds.setSensor(new Sensor(op.getId()));
            ds.complete();
            pm.insert(ds);
        }

        // Create new multiDatastreams, if any.
        for (MultiDatastream mds : op.getMultiDatastreams()) {
            mds.setSensor(new Sensor(op.getId()));
            mds.complete();
            pm.insert(mds);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, ObservedProperty op, J opId) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        if (op.isSetDefinition()) {
            if (op.getDefinition() == null) {
                throw new IncompleteEntityException("definition" + CAN_NOT_BE_NULL);
            }
            update.put(table.colDefinition, op.getDefinition());
            message.addField(EntityPropertyMain.DEFINITION);
        }
        if (op.isSetDescription()) {
            if (op.getDescription() == null) {
                throw new IncompleteEntityException(EntityPropertyMain.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.colDescription, op.getDescription());
            message.addField(EntityPropertyMain.DESCRIPTION);
        }
        if (op.isSetName()) {
            if (op.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.colName, op.getName());
            message.addField(EntityPropertyMain.NAME);
        }
        if (op.isSetProperties()) {
            update.put(table.colProperties, new JsonValue(op.getProperties()));
            message.addField(EntityPropertyMain.PROPERTIES);
        }

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(opId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating ObservedProperty {} caused {} rows to change!", opId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkDatastreams(op, pm, dslContext, opId);

        if (!op.getMultiDatastreams().isEmpty()) {
            throw new IllegalArgumentException("Can not add MultiDatastreams to an ObservedProperty.");
        }

        LOGGER.debug("Updated ObservedProperty {}", opId);
        return message;
    }

    private void linkDatastreams(ObservedProperty op, PostgresPersistenceManager<J> pm, DSLContext dslContext, J opId) throws NoSuchEntityException {
        // Link existing Datastreams to the observedProperty.
        for (Datastream ds : op.getDatastreams()) {
            if (ds.getId() == null || !entityFactories.entityExists(pm, ds)) {
                throw new NoSuchEntityException("ObservedProperty" + NO_ID_OR_NOT_FOUND);
            }
            J dsId = (J) ds.getId().getValue();
            AbstractTableDatastreams<J> qds = tableCollection.getTableDatastreams();
            long dsCount = dslContext.update(qds)
                    .set(qds.getObsPropertyId(), opId)
                    .where(qds.getId().eq(dsId))
                    .execute();
            if (dsCount > 0) {
                LOGGER.debug("Assigned datastream {} to ObservedProperty {}.", dsId, opId);
            }
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        // First delete all MultiDatastreams that link to this ObservedProperty.
        // Must happen first, since the links in the link table would be gone otherwise.
        AbstractTableMultiDatastreams<J> tMd = tableCollection.getTableMultiDatastreams();
        AbstractTableMultiDatastreamsObsProperties<J> tMdOp = tableCollection.getTableMultiDatastreamsObsProperties();
        long count = pm.getDslContext()
                .delete(tMd)
                .where(
                        tMd.getId().in(
                                DSL.select(tMdOp.getMultiDatastreamId()).from(tMdOp).where(tMdOp.getObsPropertyId().eq(entityId))
                        ))
                .execute();
        LOGGER.debug("Deleted {} MultiDatastreams.", count);
        // Then actually delete the OP.
        count = pm.getDslContext()
                .delete(table)
                .where(table.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("ObservedProperty " + entityId + " not found.");
        }
    }

}
