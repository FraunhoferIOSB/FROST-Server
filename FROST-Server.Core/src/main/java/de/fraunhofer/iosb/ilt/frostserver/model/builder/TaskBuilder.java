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
package de.fraunhofer.iosb.ilt.frostserver.model.builder;

import de.fraunhofer.iosb.ilt.frostserver.model.Task;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.core.AbstractEntityBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for Observation objects.
 *
 * @author jab
 */
public class TaskBuilder extends AbstractEntityBuilder<Task, TaskBuilder> {

    private TimeInstant creationTime;
    private Map<String, Object> taskingParameters;
    private TaskingCapability taskingCapability;

    public TaskBuilder() {
        taskingParameters = new HashMap<>();
    }

    public TaskBuilder setCreationTime(TimeInstant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public TaskBuilder setTaskingParameters(Map<String, Object> taskingParameters) {
        this.taskingParameters = taskingParameters;
        return this;
    }

    public TaskBuilder setTaskingCapability(TaskingCapability taskingCapability) {
        this.taskingCapability = taskingCapability;
        return this;
    }

    @Override
    protected TaskBuilder getThis() {
        return this;
    }

    @Override
    public Task build() {
        Task task = new Task();
        super.build(task);
        task.setCreationTime(creationTime);
        task.setTaskingParameters(taskingParameters);
        task.setTaskingCapability(taskingCapability);

        return task;
    }

}
