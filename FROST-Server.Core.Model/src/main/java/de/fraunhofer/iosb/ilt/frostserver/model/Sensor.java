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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NamedDsHoldingEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import java.util.Objects;

/**
 *
 * @author jab, scf
 */
public class Sensor extends NamedDsHoldingEntity<Sensor> {

    private String encodingType;
    private Object metadata;

    private boolean setEncodingType;
    private boolean setMetadata;

    public Sensor() {
        this(null);
    }

    public Sensor(Id id) {
        super(id);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SENSOR;
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
    public void setEntityPropertiesSet(Sensor comparedTo, EntityChangedMessage message) {
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

    public String getEncodingType() {
        return encodingType;
    }

    public Sensor setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        setEncodingType = true;
        return this;
    }

    public boolean isSetEncodingType() {
        return setEncodingType;
    }

    public Object getMetadata() {
        return metadata;
    }

    public Sensor setMetadata(Object metadata) {
        this.metadata = metadata;
        setMetadata = true;
        return this;
    }

    public boolean isSetMetadata() {
        return setMetadata;
    }

    @Override
    protected Sensor getThis() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), encodingType, metadata);
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
        final Sensor other = (Sensor) obj;
        return super.equals(other)
                && Objects.equals(encodingType, other.encodingType)
                && Objects.equals(metadata, other.metadata);
    }

}
