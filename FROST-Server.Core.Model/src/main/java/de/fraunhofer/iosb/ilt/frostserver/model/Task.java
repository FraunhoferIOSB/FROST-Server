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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class Task extends AbstractEntity<Task> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    private TimeInstant creationTime;
    private Map<String, Object> taskingParameters;
    private TaskingCapability taskingCapability;

    private boolean setCreationTime;
    private boolean setTaskingParameters;
    private boolean setTaskingCapability;

    public Task() {
        this(null);
    }

    public Task(Id id) {
        super(id);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TASK;
    }

    @Override
    public void complete(PathElementEntitySet containingSet) throws IncompleteEntityException {
        PathElement parent = containingSet.getParent();
        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                if (parentEntity.getEntityType() == EntityType.TASKINGCAPABILITY) {
                    setTaskingCapability(new TaskingCapability(parentId));
                    LOGGER.debug("Set taskingCapabilityId to {}.", parentId);
                } else {
                    LOGGER.error("Incorrect 'parent' entity type for {}: {}", getEntityType(), parentEntity.getEntityType());
                }
            }
        }

        super.complete(containingSet);
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setSets(set, entityPropertiesOnly);
    }

    private void setSets(boolean set, boolean entityPropertiesOnly) {
        setCreationTime = set;
        setTaskingParameters = set;
        if (!entityPropertiesOnly) {
            setTaskingCapability = set;
        }
    }

    @Override
    public void setEntityPropertiesSet(Task comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSets(false, false);
        if (!Objects.equals(creationTime, comparedTo.getCreationTime())) {
            setCreationTime = true;
            message.addEpField(EntityPropertyMain.CREATIONTIME);
        }
        if (!Objects.equals(taskingParameters, comparedTo.getTaskingParameters())) {
            setTaskingParameters = true;
            message.addEpField(EntityPropertyMain.TASKINGPARAMETERS);
        }
        if (!Objects.equals(taskingCapability, comparedTo.getTaskingCapability())) {
            setTaskingCapability = true;
            message.addNpField(NavigationPropertyMain.TASKINGCAPABILITY);
        }
    }

    public TimeInstant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(TimeInstant creationTime) {
        this.creationTime = creationTime;
        setCreationTime = true;
    }

    public boolean isSetCreationTime() {
        return setCreationTime;
    }

    public Map<String, Object> getTaskingParameters() {
        return taskingParameters;
    }

    public void setTaskingParameters(Map<String, Object> taskingParameters) {
        setTaskingParameters = true;
        if (taskingParameters == null || taskingParameters.isEmpty()) {
            this.taskingParameters = null;
        } else {
            this.taskingParameters = taskingParameters;
        }
    }

    public boolean isSetTaskingParameters() {
        return setTaskingParameters;
    }

    public TaskingCapability getTaskingCapability() {
        return taskingCapability;
    }

    public boolean isSetTaskingCapability() {
        return setTaskingCapability;
    }

    public void setTaskingCapability(TaskingCapability taskingCapability) {
        this.taskingCapability = taskingCapability;
        setTaskingCapability = true;
    }

    @Override
    protected Task getThis() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), creationTime, taskingParameters, taskingCapability);
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
        final Task other = (Task) obj;
        return super.equals(other)
                && Objects.equals(creationTime, other.creationTime)
                && Objects.equals(taskingParameters, other.taskingParameters)
                && Objects.equals(taskingCapability, other.taskingCapability);
    }

}
