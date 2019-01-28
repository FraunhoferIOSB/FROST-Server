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
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class Actuator extends NamedEntity<Actuator> {

    private String encodingType;
    private Object metadata;

    private EntitySet<TaskingCapability> taskingCapabilities;

    private boolean setEncodingType;
    private boolean setMetadata;

    public Actuator() {
        this(null);
    }

    public Actuator(Id id) {
        super(id);
        taskingCapabilities = new EntitySetImpl<>(EntityType.TASKINGCAPABILITY);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ACTUATOR;
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setSets(set);
    }

    private void setSets(boolean set) {
        setEncodingType = set;
        setMetadata = set;
    }

    @Override
    public void setEntityPropertiesSet(Actuator comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSets(false);
        if (!Objects.equals(encodingType, comparedTo.getEncodingType())) {
            setEncodingType = true;
            message.addEpField(EntityProperty.ENCODINGTYPE);
        }
        if (!Objects.equals(metadata, comparedTo.getMetadata())) {
            setMetadata = true;
            message.addEpField(EntityProperty.METADATA);
        }
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
        setMetadata = true;
    }

    public boolean isSetMetadata() {
        return setMetadata;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        setEncodingType = true;
    }

    public boolean isSetEncodingType() {
        return setEncodingType;
    }

    public EntitySet<TaskingCapability> getTaskingCapabilities() {
        return taskingCapabilities;
    }

    public void setTaskingCapabilities(EntitySet<TaskingCapability> taskingCapabilities) {
        this.taskingCapabilities = taskingCapabilities;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), encodingType, metadata, taskingCapabilities);
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
        final Actuator other = (Actuator) obj;
        return super.equals(other)
                && Objects.equals(encodingType, other.encodingType)
                && Objects.equals(metadata, other.metadata)
                && Objects.equals(taskingCapabilities, other.taskingCapabilities);
    }
}
