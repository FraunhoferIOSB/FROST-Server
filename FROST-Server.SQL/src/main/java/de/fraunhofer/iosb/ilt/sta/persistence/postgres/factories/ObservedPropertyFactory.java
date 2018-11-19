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
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class ObservedPropertyFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFactory<ObservedProperty, I, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservedPropertyFactory.class);

    private final EntityFactories<I, J> entityFactories;
    private final AbstractQObsProperties<?, I, J> qInstance;
    private final QCollection<I, J> qCollection;

    public ObservedPropertyFactory(EntityFactories<I, J> factories, AbstractQObsProperties<?, I, J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public ObservedProperty create(Tuple tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        ObservedProperty entity = new ObservedProperty();
        entity.setDefinition(tuple.get(qInstance.definition));
        entity.setDescription(tuple.get(qInstance.description));
        J id = entityFactories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setName(tuple.get(qInstance.name));
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<I, J> pm, ObservedProperty op) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObsProperties<? extends AbstractQObsProperties, I, J> qop = qCollection.qObsProperties;
        SQLInsertClause insert = qFactory.insert(qop);
        insert.set(qop.definition, op.getDefinition());
        insert.set(qop.name, op.getName());
        insert.set(qop.description, op.getDescription());
        insert.set(qop.properties, EntityFactories.objectToJson(op.getProperties()));

        entityFactories.insertUserDefinedId(pm, insert, qop.getId(), op);

        J generatedId = insert.executeWithKey(qop.getId());
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
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, ObservedProperty op, J opId) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObsProperties<? extends AbstractQObsProperties, I, J> qop = qCollection.qObsProperties;
        SQLUpdateClause update = qFactory.update(qop);
        EntityChangedMessage message = new EntityChangedMessage();

        if (op.isSetDefinition()) {
            if (op.getDefinition() == null) {
                throw new IncompleteEntityException("definition" + CAN_NOT_BE_NULL);
            }
            update.set(qop.definition, op.getDefinition());
            message.addField(EntityProperty.DEFINITION);
        }
        if (op.isSetDescription()) {
            if (op.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qop.description, op.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (op.isSetName()) {
            if (op.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qop.name, op.getName());
            message.addField(EntityProperty.NAME);
        }
        if (op.isSetProperties()) {
            update.set(qop.properties, EntityFactories.objectToJson(op.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        update.where(qop.getId().eq(opId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating ObservedProperty {} caused {} rows to change!", opId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkDatastreams(op, pm, qFactory, opId);

        if (!op.getMultiDatastreams().isEmpty()) {
            throw new IllegalArgumentException("Can not add MultiDatastreams to an ObservedProperty.");
        }

        LOGGER.debug("Updated ObservedProperty {}", opId);
        return message;
    }

    private void linkDatastreams(ObservedProperty op, PostgresPersistenceManager<I, J> pm, SQLQueryFactory qFactory, J opId) throws NoSuchEntityException {
        // Link existing Datastreams to the observedProperty.
        for (Datastream ds : op.getDatastreams()) {
            if (ds.getId() == null || !entityFactories.entityExists(pm, ds)) {
                throw new NoSuchEntityException("ObservedProperty" + NO_ID_OR_NOT_FOUND);
            }
            J dsId = (J) ds.getId().getValue();
            AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qds = qCollection.qDatastreams;
            long dsCount = qFactory.update(qds)
                    .set(qds.getObsPropertyId(), opId)
                    .where(qds.getId().eq(dsId))
                    .execute();
            if (dsCount > 0) {
                LOGGER.debug("Assigned datastream {} to ObservedProperty {}.", dsId, opId);
            }
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<I, J> pm, J entityId) throws NoSuchEntityException {
        // First delete all MultiDatastreams that link to this ObservedProperty.
        // Must happen first, since the links in the link table would be gone otherwise.
        AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qMd = qCollection.qMultiDatastreams;
        AbstractQMultiDatastreamsObsProperties<? extends AbstractQMultiDatastreamsObsProperties, I, J> qMdOp = qCollection.qMultiDatastreamsObsProperties;
        long count = pm.createQueryFactory()
                .delete(qMd)
                .where(qMd.getId()
                        .in(
                                SQLExpressions.select(qMdOp.getMultiDatastreamId()).from(qMdOp).where(qMdOp.getObsPropertyId().eq(entityId))
                        ))
                .execute();
        LOGGER.debug("Deleted {} MultiDatastreams.", count);
        // Then actually delete the OP.
        count = pm.createQueryFactory()
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
    public I getPrimaryKey() {
        return qInstance.getId();
    }

}
