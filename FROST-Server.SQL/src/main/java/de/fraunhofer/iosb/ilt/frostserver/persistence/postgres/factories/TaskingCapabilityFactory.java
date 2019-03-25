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
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.Actuator;
import de.fraunhofer.iosb.ilt.frostserver.model.Task;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.EntityFactories;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQTaskingCapabilities;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.AbstractQTasks;
import de.fraunhofer.iosb.ilt.frostserver.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.NoSuchEntityException;
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
public class TaskingCapabilityFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFactory<TaskingCapability, I, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskingCapabilityFactory.class);

    private final EntityFactories<I, J> entityFactories;
    private final AbstractQTaskingCapabilities<?, I, J> qInstance;
    private final QCollection<I, J> qCollection;

    public TaskingCapabilityFactory(EntityFactories<I, J> factories, AbstractQTaskingCapabilities<?, I, J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public TaskingCapability create(Tuple tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        TaskingCapability entity = new TaskingCapability();

        J entityId = entityFactories.getIdFromTuple(tuple, qInstance.getId());
        if (entityId != null) {
            entity.setId(entityFactories.idFromObject(entityId));
        }
        entity.setName(tuple.get(qInstance.name));
        entity.setDescription(tuple.get(qInstance.description));
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        if (select.isEmpty() || select.contains(EntityProperty.TASKINGPARAMETERS)) {
            String props = tuple.get(qInstance.taskingParameters);
            entity.setTaskingParameters(Utils.jsonToObject(props, Map.class));
        }
        entity.setActuator(entityFactories.actuatorFromId(tuple, qInstance.getActuatorId()));
        entity.setThing(entityFactories.thingFromId(tuple, qInstance.getThingId()));

        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<I, J> pm, TaskingCapability tc) throws NoSuchEntityException, IncompleteEntityException {
        // First check Actuator and Thing
        Actuator actuator = tc.getActuator();
        entityFactories.entityExistsOrCreate(pm, actuator);

        Thing thing = tc.getThing();
        entityFactories.entityExistsOrCreate(pm, thing);

        SQLQueryFactory qFactory = pm.createQueryFactory();

        AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> qtc = qCollection.qTaskingCapabilities;
        SQLInsertClause insert = qFactory.insert(qtc);
        insert.set(qtc.name, tc.getName());
        insert.set(qtc.description, tc.getDescription());
        insert.set(qtc.properties, EntityFactories.objectToJson(tc.getProperties()));
        insert.set(qtc.taskingParameters, EntityFactories.objectToJson(tc.getTaskingParameters()));

        insert.set(qtc.getActuatorId(), (J) actuator.getId().getValue());
        insert.set(qtc.getThingId(), (J) thing.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, qtc.getId(), tc);

        J tcId = insert.executeWithKey(qtc.getId());
        LOGGER.debug("Inserted TaskingCapability. Created id = {}.", tcId);
        tc.setId(entityFactories.idFromObject(tcId));

        // Create Tasks, if any.
        for (Task t : tc.getTasks()) {
            t.setTaskingCapability(new TaskingCapability(tc.getId()));
            t.complete();
            pm.insert(t);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, TaskingCapability tc, J dsId) throws NoSuchEntityException, IncompleteEntityException {

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> qd = qCollection.qTaskingCapabilities;

        SQLUpdateClause update = qFactory.update(qd);
        EntityChangedMessage message = new EntityChangedMessage();

        updateName(tc, update, qd, message);
        updateDescription(tc, update, qd, message);
        updateProperties(tc, update, qd, message);
        updateTaskingParameters(tc, update, qd, message);
        updateActuator(tc, pm, update, qd, message);
        updateThing(tc, pm, update, qd, message);

        update.where(qd.getId().eq(dsId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating TaskingCapability {} caused {} rows to change!", dsId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingTasks(tc, pm, qFactory, dsId);

        LOGGER.debug("Updated TaskingCapability {}", dsId);
        return message;
    }

    private void updateThing(TaskingCapability taskingCapability, PostgresPersistenceManager<I, J> pm, SQLUpdateClause update, AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> qd, EntityChangedMessage message) throws NoSuchEntityException {
        if (taskingCapability.isSetThing()) {
            if (!entityFactories.entityExists(pm, taskingCapability.getThing())) {
                throw new NoSuchEntityException("Thing with no id or not found.");
            }
            update.set(qd.getThingId(), (J) taskingCapability.getThing().getId().getValue());
            message.addField(NavigationProperty.THING);
        }
    }

    private void updateActuator(TaskingCapability taskingCapability, PostgresPersistenceManager<I, J> pm, SQLUpdateClause update, AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> qd, EntityChangedMessage message) throws NoSuchEntityException {
        if (taskingCapability.isSetActuator()) {
            if (!entityFactories.entityExists(pm, taskingCapability.getActuator())) {
                throw new NoSuchEntityException("Actuator with no id or not found.");
            }
            update.set(qd.getActuatorId(), (J) taskingCapability.getActuator().getId().getValue());
            message.addField(NavigationProperty.ACTUATOR);
        }
    }

    private void updateProperties(TaskingCapability taskingCapability, SQLUpdateClause update, AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> qd, EntityChangedMessage message) {
        if (taskingCapability.isSetProperties()) {
            update.set(qd.properties, EntityFactories.objectToJson(taskingCapability.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateTaskingParameters(TaskingCapability taskingCapability, SQLUpdateClause update, AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> qd, EntityChangedMessage message) {
        if (taskingCapability.isSetTaskingParameters()) {
            update.set(qd.taskingParameters, EntityFactories.objectToJson(taskingCapability.getTaskingParameters()));
            message.addField(EntityProperty.TASKINGPARAMETERS);
        }
    }

    private void updateDescription(TaskingCapability taskingCapability, SQLUpdateClause update, AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> qd, EntityChangedMessage message) throws IncompleteEntityException {
        if (taskingCapability.isSetDescription()) {
            if (taskingCapability.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qd.description, taskingCapability.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateName(TaskingCapability d, SQLUpdateClause update, AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> qd, EntityChangedMessage message) throws IncompleteEntityException {
        if (d.isSetName()) {
            if (d.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qd.name, d.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void linkExistingTasks(TaskingCapability d, PostgresPersistenceManager<I, J> pm, SQLQueryFactory qFactory, J tcId) throws NoSuchEntityException {
        for (Task o : d.getTasks()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J taskId = (J) o.getId().getValue();
            AbstractQTasks<? extends AbstractQTasks, I, J> qt = qCollection.qTasks;
            long oCount = qFactory.update(qt)
                    .set(qt.getTaskingcapabilityId(), tcId)
                    .where(qt.getId().eq(taskId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned Task {} to TaskingCapability {}.", taskId, tcId);
            }
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<I, J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createQueryFactory()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
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
    public I getPrimaryKey() {
        return qInstance.getId();
    }

}
