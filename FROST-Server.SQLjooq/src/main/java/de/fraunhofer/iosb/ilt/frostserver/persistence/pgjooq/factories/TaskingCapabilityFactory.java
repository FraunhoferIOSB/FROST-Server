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
import de.fraunhofer.iosb.ilt.frostserver.model.Task;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
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
public class TaskingCapabilityFactory<J extends Comparable> implements EntityFactory<TaskingCapability, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskingCapabilityFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableTaskingCapabilities<J> table;
    private final TableCollection<J> tableCollection;

    public TaskingCapabilityFactory(EntityFactories<J> factories, AbstractTableTaskingCapabilities<J> qInstance) {
        this.entityFactories = factories;
        this.table = qInstance;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public TaskingCapability create(Record record, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        TaskingCapability entity = new TaskingCapability();

        J entityId = getFieldOrNull(record, table.getId());
        if (entityId != null) {
            entity.setId(entityFactories.idFromObject(entityId));
        }
        entity.setName(getFieldOrNull(record, table.name));
        entity.setDescription(getFieldOrNull(record, table.description));
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = getFieldOrNull(record, table.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        if (select.isEmpty() || select.contains(EntityProperty.TASKINGPARAMETERS)) {
            String props = getFieldOrNull(record, table.taskingParameters);
            entity.setTaskingParameters(Utils.jsonToObject(props, Map.class));
        }
        entity.setActuator(entityFactories.actuatorFromId(record, table.getActuatorId()));
        entity.setThing(entityFactories.thingFromId(record, table.getThingId()));

        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, TaskingCapability tc) throws NoSuchEntityException, IncompleteEntityException {
        // First check Actuator and Thing
        Actuator actuator = tc.getActuator();
        entityFactories.entityExistsOrCreate(pm, actuator);

        Thing thing = tc.getThing();
        entityFactories.entityExistsOrCreate(pm, thing);

        Map<Field, Object> insert = new HashMap<>();

        insert.put(table.name, tc.getName());
        insert.put(table.description, tc.getDescription());
        insert.put(table.properties, EntityFactories.objectToJson(tc.getProperties()));
        insert.put(table.taskingParameters, EntityFactories.objectToJson(tc.getTaskingParameters()));

        insert.put(table.getActuatorId(), (J) actuator.getId().getValue());
        insert.put(table.getThingId(), (J) thing.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), tc);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J generatedId = result.component1();
        LOGGER.debug("Inserted TaskingCapability. Created id = {}.", generatedId);
        tc.setId(entityFactories.idFromObject(generatedId));

        // Create Tasks, if any.
        for (Task t : tc.getTasks()) {
            t.setTaskingCapability(new TaskingCapability(tc.getId()));
            t.complete();
            pm.insert(t);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, TaskingCapability tc, J tcId) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        updateName(tc, update, message);
        updateDescription(tc, update, message);
        updateProperties(tc, update, message);
        updateTaskingParameters(tc, update, message);
        updateActuator(tc, pm, update, message);
        updateThing(tc, pm, update, message);

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(tcId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating TaskingCapability {} caused {} rows to change!", tcId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingTasks(tc, pm, dslContext, tcId);

        LOGGER.debug("Updated TaskingCapability {}", tcId);
        return message;
    }

    private void updateThing(TaskingCapability taskingCapability, PostgresPersistenceManager<J> pm, Map<Field, Object> update, EntityChangedMessage message) throws NoSuchEntityException {
        if (taskingCapability.isSetThing()) {
            if (!entityFactories.entityExists(pm, taskingCapability.getThing())) {
                throw new NoSuchEntityException("Thing with no id or not found.");
            }
            update.put(table.getThingId(), (J) taskingCapability.getThing().getId().getValue());
            message.addField(NavigationProperty.THING);
        }
    }

    private void updateActuator(TaskingCapability taskingCapability, PostgresPersistenceManager<J> pm, Map<Field, Object> update, EntityChangedMessage message) throws NoSuchEntityException {
        if (taskingCapability.isSetActuator()) {
            if (!entityFactories.entityExists(pm, taskingCapability.getActuator())) {
                throw new NoSuchEntityException("Actuator with no id or not found.");
            }
            update.put(table.getActuatorId(), (J) taskingCapability.getActuator().getId().getValue());
            message.addField(NavigationProperty.ACTUATOR);
        }
    }

    private void updateProperties(TaskingCapability taskingCapability, Map<Field, Object> update, EntityChangedMessage message) {
        if (taskingCapability.isSetProperties()) {
            update.put(table.properties, EntityFactories.objectToJson(taskingCapability.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateTaskingParameters(TaskingCapability taskingCapability, Map<Field, Object> update, EntityChangedMessage message) {
        if (taskingCapability.isSetTaskingParameters()) {
            update.put(table.taskingParameters, EntityFactories.objectToJson(taskingCapability.getTaskingParameters()));
            message.addField(EntityProperty.TASKINGPARAMETERS);
        }
    }

    private void updateDescription(TaskingCapability taskingCapability, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (taskingCapability.isSetDescription()) {
            if (taskingCapability.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.description, taskingCapability.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateName(TaskingCapability taskingCapability, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (taskingCapability.isSetName()) {
            if (taskingCapability.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.name, taskingCapability.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void linkExistingTasks(TaskingCapability taskingCapability, PostgresPersistenceManager<J> pm, DSLContext dslContext, J tcId) throws NoSuchEntityException {
        for (Task o : taskingCapability.getTasks()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J taskId = (J) o.getId().getValue();
            AbstractTableTasks<J> qt = tableCollection.tableTasks;
            long oCount = dslContext.update(qt)
                    .set(qt.getTaskingCapabilityId(), tcId)
                    .where(qt.getId().eq(taskId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned Task {} to TaskingCapability {}.", taskId, tcId);
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
            throw new NoSuchEntityException("TaskingCapability " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TASKINGCAPABILITY;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return table.getId();
    }

}
