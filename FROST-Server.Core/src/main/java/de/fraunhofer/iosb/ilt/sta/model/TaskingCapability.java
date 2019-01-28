/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.model;

import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.core.NamedEntity;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 */
public class TaskingCapability extends NamedEntity<TaskingCapability> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskingCapability.class);

    private Map<String, Object> taskingParameters;

    private Actuator actuator;
    private Thing thing;
    private EntitySet<Task> tasks;

    private boolean setTaskingParameters;
    private boolean setActuator;
    private boolean setThing;

    public TaskingCapability() {
        this(null);
    }

    public TaskingCapability(Id id) {
        super(id);
        this.tasks = new EntitySetImpl<>(EntityType.TASK);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TASKINGCAPABILITY;
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        ResourcePathElement parent = containingSet.getParent();
        if (parent instanceof EntityPathElement) {
            EntityPathElement parentEntity = (EntityPathElement) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                checkParent(parentEntity, parentId);
            }
        }
        super.complete(containingSet);
    }

    private void checkParent(EntityPathElement parentEntity, Id parentId) {
        switch (parentEntity.getEntityType()) {
            case ACTUATOR:
                setActuator(new Actuator(parentId));
                LOGGER.debug("Set actuatorId to {}.", parentId);
                break;

            case THING:
                setThing(new Thing(parentId));
                LOGGER.debug("Set thingId to {}.", parentId);
                break;

            default:
                LOGGER.error("Incorrect 'parent' entity type for {}: {}", getEntityType(), parentEntity.getEntityType());
                break;
        }
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setSets(set, entityPropertiesOnly);
    }

    private void setSets(boolean set, boolean entityPropertiesOnly) {
        setTaskingParameters = set;
        if (!entityPropertiesOnly) {
            setActuator = set;
            setThing = set;
        }
    }

    @Override
    public void setEntityPropertiesSet(TaskingCapability comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSets(false, false);
        if (!Objects.equals(taskingParameters, comparedTo.getTaskingParameters())) {
            setTaskingParameters = true;
            message.addEpField(EntityProperty.TASKINGPARAMETERS);
        }
        if (!Objects.equals(actuator, comparedTo.getActuator())) {
            setActuator = true;
            message.addNpField(NavigationProperty.ACTUATOR);
        }
        if (!Objects.equals(thing, comparedTo.getThing())) {
            setThing = true;
            message.addNpField(NavigationProperty.THING);
        }
    }

    public Map<String, Object> getTaskingParameters() {
        return taskingParameters;
    }

    public void setTaskingParameters(Map<String, Object> taskingParameters) {
        if (taskingParameters == null || taskingParameters.isEmpty()) {
            this.taskingParameters = null;
        } else {
            this.taskingParameters = taskingParameters;
        }
        setTaskingParameters = true;
    }

    public boolean isSetTaskingParameters() {
        return setTaskingParameters;
    }

    public Actuator getActuator() {
        return actuator;
    }

    public void setActuator(Actuator actuator) {
        this.actuator = actuator;
        setActuator = true;
    }

    public boolean isSetActuator() {
        return setActuator;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
        setThing = true;
    }

    public boolean isSetThing() {
        return setThing;
    }

    public EntitySet<Task> getTasks() {
        return tasks;
    }

    public void setTasks(EntitySet<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), taskingParameters, actuator, tasks, thing);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskingCapability other = (TaskingCapability) obj;
        return super.equals(other)
                && Objects.equals(taskingParameters, other.taskingParameters)
                && Objects.equals(actuator, other.actuator)
                && Objects.equals(tasks, other.tasks)
                && Objects.equals(thing, other.thing);
    }

}
