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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories;

import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 *
 * @param <J> The type of the ID fields.
 */
public class ObservedPropertyFactory<J> implements EntityFactory<ObservedProperty, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservedPropertyFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableObsProperties<J> qInstance;
    private final QCollection<J> qCollection;

    public ObservedPropertyFactory(EntityFactories<J> factories, AbstractTableObsProperties<J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public ObservedProperty create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        ObservedProperty entity = new ObservedProperty();
        entity.setDefinition(getFieldOrNull(tuple, qInstance.definition));
        entity.setDescription(getFieldOrNull(tuple, qInstance.description));
        J id = getFieldOrNull(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setName(getFieldOrNull(tuple, qInstance.name));
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = getFieldOrNull(tuple, qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, ObservedProperty op) throws NoSuchEntityException, IncompleteEntityException {
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableObsProperties<J> qop = qCollection.qObsProperties;
        Map<Field, Object> insert = new HashMap<>();
        insert.put(qop.definition, op.getDefinition());
        insert.put(qop.name, op.getName());
        insert.put(qop.description, op.getDescription());
        insert.put(qop.properties, EntityFactories.objectToJson(op.getProperties()));

        entityFactories.insertUserDefinedId(pm, insert, qop.getId(), op);

        Record1<J> result = dslContext.insertInto(qop)
                .set(insert)
                .returningResult(qop.getId())
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
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableObsProperties<J> qop = qCollection.qObsProperties;
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        if (op.isSetDefinition()) {
            if (op.getDefinition() == null) {
                throw new IncompleteEntityException("definition" + CAN_NOT_BE_NULL);
            }
            update.put(qop.definition, op.getDefinition());
            message.addField(EntityProperty.DEFINITION);
        }
        if (op.isSetDescription()) {
            if (op.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(qop.description, op.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (op.isSetName()) {
            if (op.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(qop.name, op.getName());
            message.addField(EntityProperty.NAME);
        }
        if (op.isSetProperties()) {
            update.put(qop.properties, EntityFactories.objectToJson(op.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(qop)
                    .set(update)
                    .where(qop.getId().equal(opId))
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
            AbstractTableDatastreams<J> qds = qCollection.qDatastreams;
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
        AbstractTableMultiDatastreams<J> qMd = qCollection.qMultiDatastreams;
        AbstractTableMultiDatastreamsObsProperties<J> qMdOp = qCollection.qMultiDatastreamsObsProperties;
        long count = pm.createDdslContext()
                .delete(qMd)
                .where(
                        qMd.getId().in(
                                DSL.select(qMdOp.getMultiDatastreamId()).from(qMdOp).where(qMdOp.getObsPropertyId().eq(entityId))
                        ))
                .execute();
        LOGGER.debug("Deleted {} MultiDatastreams.", count);
        // Then actually delete the OP.
        count = pm.createDdslContext()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("ObservedProperty " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.OBSERVEDPROPERTY;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return qInstance.getId();
    }

}
