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

import de.fraunhofer.iosb.ilt.frostserver.model.Actuator;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.builder.core.NamedEntityBuilder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;

/**
 * Builder class for Sensor objects.
 *
 * @author jab
 */
public class ActuatorBuilder extends NamedEntityBuilder<Actuator, ActuatorBuilder> {

    private String encodingType;
    private Object metadata;
    private EntitySet<TaskingCapability> taskingCapabilities;

    public ActuatorBuilder() {
        taskingCapabilities = new EntitySetImpl<>(EntityType.TASKINGCAPABILITY);
    }

    public ActuatorBuilder setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        return this;
    }

    public ActuatorBuilder setMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    public ActuatorBuilder setTaskingCapabilities(EntitySet<TaskingCapability> taskingCapabilities) {
        this.taskingCapabilities = taskingCapabilities;
        return this;
    }

    public ActuatorBuilder addTaskingCapability(TaskingCapability taskingCapability) {
        this.taskingCapabilities.add(taskingCapability);
        return this;
    }

    @Override
    protected ActuatorBuilder getThis() {
        return this;
    }

    @Override
    public Actuator build() {
        Actuator actuator = new Actuator();
        super.build(actuator);
        actuator.setEncodingType(encodingType);
        actuator.setMetadata(metadata);
        actuator.setTaskingCapabilities(taskingCapabilities);
        return actuator;
    }

}
