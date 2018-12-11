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
import de.fraunhofer.iosb.ilt.sta.model.Task;
import de.fraunhofer.iosb.ilt.sta.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.insertTimeInstant;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQTasks;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.sql.Timestamp;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class TaskFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFactory<Task, I, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskFactory.class);
    private final EntityFactories<I, J> entityFactories;
    private final AbstractQTasks<?, I, J> qInstance;
    private final QCollection<I, J> qCollection;

    public TaskFactory(EntityFactories<I, J> factories, AbstractQTasks<?, I, J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public Task create(Tuple tuple, Query query, DataSize dataSize) {
        Task entity = new Task();
        J id = entityFactories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setTaskingCapability(entityFactories.taskingCapabilityFromId(tuple, qInstance.getTaskingcapabilityId()));
        entity.setCreationTime(Utils.instantFromTime(tuple.get(qInstance.creationTime)));
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<I, J> pm, Task task) throws NoSuchEntityException, IncompleteEntityException {
        TaskingCapability tc = task.getTaskingCapability();
        entityFactories.entityExistsOrCreate(pm, tc);
        J tcId = (J) task.getTaskingCapability().getId().getValue();

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQTasks<? extends AbstractQTasks, I, J> qt = qCollection.qTasks;
        SQLInsertClause insert = qFactory.insert(qt);
        insert.set(qt.creationTime, new Timestamp(Calendar.getInstance().getTimeInMillis()));
        insert.set(qt.getTaskingcapabilityId(), tcId);
        insert.set(qt.taskingParameters, EntityFactories.objectToJson(task.getTaskingParameters()));

        entityFactories.insertUserDefinedId(pm, insert, qt.getId(), task);

        J generatedId = insert.executeWithKey(qt.getId());
        LOGGER.debug("Inserted Task. Created id = {}.", generatedId);
        task.setId(entityFactories.idFromObject(generatedId));

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, Task task, J id) throws IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQTasks<? extends AbstractQTasks, I, J> qt = qCollection.qTasks;
        SQLUpdateClause update = qFactory.update(qt);
        EntityChangedMessage message = new EntityChangedMessage();

        if (task.isSetTaskingCapability()) {
            if (!entityFactories.entityExists(pm, task.getTaskingCapability())) {
                throw new IncompleteEntityException("TaskingCapability" + CAN_NOT_BE_NULL);
            }
            update.set(qt.getTaskingcapabilityId(), (J) task.getTaskingCapability().getId().getValue());
            message.addField(NavigationProperty.TASKINGCAPABILITY);
        }
        if (task.isSetCreationTime()) {
            if (task.getCreationTime() == null) {
                throw new IncompleteEntityException("creationTime" + CAN_NOT_BE_NULL);
            }
            insertTimeInstant(update, qt.creationTime, task.getCreationTime());
            message.addField(EntityProperty.TIME);
        }
        if (task.isSetTaskingParameters()) {
            update.set(qt.taskingParameters, EntityFactories.objectToJson(task.getTaskingParameters()));
            message.addField(EntityProperty.TASKINGPARAMETERS);
        }
        update.where(qt.getId().eq(id));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Task {} caused {} rows to change!", id, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Task {}", id);

        return message;
    }

    @Override
    public void delete(PostgresPersistenceManager<I, J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createQueryFactory()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
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
    public I getPrimaryKey() {
        return qInstance.getId();
    }

}
