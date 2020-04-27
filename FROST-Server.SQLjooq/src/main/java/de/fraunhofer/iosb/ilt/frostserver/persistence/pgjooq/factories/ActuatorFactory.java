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

import de.fraunhofer.iosb.ilt.frostserver.model.Actuator;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableActuators;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <J> The type of the ID fields.
 */
public class ActuatorFactory<J extends Comparable> implements EntityFactory<Actuator, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActuatorFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableActuators<J> table;
    private final TableCollection<J> tableCollection;

    public ActuatorFactory(EntityFactories<J> factories, AbstractTableActuators<J> table) {
        this.entityFactories = factories;
        this.table = table;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public Actuator create(Record record, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Actuator entity = new Actuator();
        J id = getFieldOrNull(record, table.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setName(getFieldOrNull(record, table.name));
        entity.setDescription(getFieldOrNull(record, table.description));
        entity.setEncodingType(getFieldOrNull(record, table.encodingType));
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = getFieldOrNull(record, table.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        if (select.isEmpty() || select.contains(EntityProperty.METADATA)) {
            String metaDataString = getFieldOrNull(record, table.metadata);
            dataSize.increase(metaDataString == null ? 0 : metaDataString.length());
            entity.setMetadata(metaDataString);
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Actuator actuator) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> insert = new HashMap<>();
        insert.put(table.name, actuator.getName());
        insert.put(table.description, actuator.getDescription());
        insert.put(table.encodingType, actuator.getEncodingType());
        // We currently assume it's a string.
        insert.put(table.metadata, actuator.getMetadata().toString());
        insert.put(table.properties, EntityFactories.objectToJson(actuator.getProperties()));

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), actuator);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J generatedId = result.component1();
        LOGGER.debug("Inserted Actuator. Created id = {}.", generatedId);
        actuator.setId(entityFactories.idFromObject(generatedId));

        // Create new taskingCapabilities, if any.
        for (TaskingCapability tc : actuator.getTaskingCapabilities()) {
            tc.setActuator(new Actuator(actuator.getId()));
            tc.complete();
            pm.insert(tc);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Actuator actuator, J actuatorId) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        if (actuator.isSetName()) {
            if (actuator.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.name, actuator.getName());
            message.addField(EntityProperty.NAME);
        }
        if (actuator.isSetDescription()) {
            if (actuator.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.description, actuator.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (actuator.isSetEncodingType()) {
            if (actuator.getEncodingType() == null) {
                throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
            }
            update.put(table.encodingType, actuator.getEncodingType());
            message.addField(EntityProperty.ENCODINGTYPE);
        }
        if (actuator.isSetMetadata()) {
            if (actuator.getMetadata() == null) {
                throw new IncompleteEntityException("metadata" + CAN_NOT_BE_NULL);
            }
            // We currently assume it's a string.
            update.put(table.metadata, actuator.getMetadata().toString());
            message.addField(EntityProperty.METADATA);
        }
        if (actuator.isSetProperties()) {
            update.put(table.properties, EntityFactories.objectToJson(actuator.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(actuatorId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Actuator {} caused {} rows to change!", actuatorId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingTaskingCapabilities(actuator, pm, dslContext, actuatorId);

        LOGGER.debug("Updated Actuator {}", actuatorId);
        return message;
    }

    private void linkExistingTaskingCapabilities(Actuator s, PostgresPersistenceManager<J> pm, DSLContext dslContext, J actuatorId) throws NoSuchEntityException {
        for (TaskingCapability tc : s.getTaskingCapabilities()) {
            if (tc.getId() == null || !entityFactories.entityExists(pm, tc)) {
                throw new NoSuchEntityException("TaskingCapability" + NO_ID_OR_NOT_FOUND);
            }
            J tcId = (J) tc.getId().getValue();
            AbstractTableTaskingCapabilities<J> qtc = tableCollection.getTableTaskingCapabilities();
            long dsCount = dslContext.update(qtc)
                    .set(qtc.getActuatorId(), actuatorId)
                    .where(qtc.getId().eq(tcId))
                    .execute();
            if (dsCount > 0) {
                LOGGER.debug("Assigned TaskingCapability {} to Actuator {}.", tcId, actuatorId);
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
            throw new NoSuchEntityException("Actuator " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ACTUATOR;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return table.getId();
    }

}
