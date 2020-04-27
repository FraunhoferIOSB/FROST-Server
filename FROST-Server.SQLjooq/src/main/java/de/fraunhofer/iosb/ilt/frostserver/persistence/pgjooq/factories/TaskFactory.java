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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Task;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.sql.Timestamp;
import java.util.Calendar;
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
public class TaskFactory<J extends Comparable> implements EntityFactory<Task, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskFactory.class);
    private final EntityFactories<J> entityFactories;
    private final AbstractTableTasks<J> table;

    public TaskFactory(EntityFactories<J> factories, AbstractTableTasks<J> table) {
        this.entityFactories = factories;
        this.table = table;
    }

    @Override
    public Task create(Record record, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Task entity = new Task();
        J id = getFieldOrNull(record, table.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setTaskingCapability(entityFactories.taskingCapabilityFromId(record, table.getTaskingCapabilityId()));
        entity.setCreationTime(Utils.instantFromTime(getFieldOrNull(record, table.colCreationTime)));
        if (select.isEmpty() || select.contains(EntityProperty.TASKINGPARAMETERS)) {
            String taskingParams = getFieldOrNull(record, table.colTaskingParameters);
            entity.setTaskingParameters(Utils.jsonToObject(taskingParams, Map.class));
        }

        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Task task) throws NoSuchEntityException, IncompleteEntityException {
        TaskingCapability tc = task.getTaskingCapability();
        entityFactories.entityExistsOrCreate(pm, tc);
        J tcId = (J) task.getTaskingCapability().getId().getValue();

        Map<Field, Object> insert = new HashMap<>();

        insert.put(table.colCreationTime, new Timestamp(Calendar.getInstance().getTimeInMillis()));
        insert.put(table.getTaskingCapabilityId(), tcId);
        insert.put(table.colTaskingParameters, EntityFactories.objectToJson(task.getTaskingParameters()));

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), task);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J generatedId = result.component1();
        LOGGER.debug("Inserted Task. Created id = {}.", generatedId);
        task.setId(entityFactories.idFromObject(generatedId));

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Task task, J taskId) throws IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        if (task.isSetTaskingCapability()) {
            if (!entityFactories.entityExists(pm, task.getTaskingCapability())) {
                throw new IncompleteEntityException("TaskingCapability" + CAN_NOT_BE_NULL);
            }
            update.put(table.getTaskingCapabilityId(),  task.getTaskingCapability().getId().getValue());
            message.addField(NavigationPropertyMain.TASKINGCAPABILITY);
        }
        if (task.isSetCreationTime()) {
            if (task.getCreationTime() == null) {
                throw new IncompleteEntityException("creationTime" + CAN_NOT_BE_NULL);
            }
            EntityFactories.insertTimeInstant(update, table.colCreationTime, task.getCreationTime());
            message.addField(EntityProperty.TIME);
        }
        if (task.isSetTaskingParameters()) {
            update.put(table.colTaskingParameters, EntityFactories.objectToJson(task.getTaskingParameters()));
            message.addField(EntityProperty.TASKINGPARAMETERS);
        }

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(taskId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Task {} caused {} rows to change!", taskId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Task {}", taskId);

        return message;
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.getDslContext()
                .delete(table)
                .where(table.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Task " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TASK;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return table.getId();
    }

}
