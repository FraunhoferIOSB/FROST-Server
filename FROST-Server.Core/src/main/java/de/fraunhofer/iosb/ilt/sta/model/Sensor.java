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

import de.fraunhofer.iosb.ilt.sta.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class Sensor extends AbstractEntity {

    private String name;
    private String description;
    private String encodingType;
    private Object metadata;
    private Map<String, Object> properties;
    private EntitySet<Datastream> datastreams;
    private EntitySet<MultiDatastream> multiDatastreams;

    private boolean setName;
    private boolean setDescription;
    private boolean setEncodingType;
    private boolean setMetadata;
    private boolean setProperties;

    public Sensor() {
        this.datastreams = new EntitySetImpl<>(EntityType.DATASTREAM);
        this.multiDatastreams = new EntitySetImpl<>(EntityType.MULTIDATASTREAM);
    }

    public Sensor(
            Id id,
            String selfLink,
            String navigationLink,
            String name,
            String description,
            String encodingType,
            Object metadata,
            Map<String, Object> properties,
            EntitySet<Datastream> datastreams,
            EntitySet<MultiDatastream> multiDatastreams) {
        super(id, selfLink, navigationLink);
        this.name = name;
        this.description = description;
        this.encodingType = encodingType;
        this.metadata = metadata;
        this.datastreams = datastreams;
        this.multiDatastreams = multiDatastreams;
        if (properties != null && !properties.isEmpty()) {
            this.properties = new HashMap<>(properties);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.description);
        hash = 97 * hash + Objects.hashCode(this.encodingType);
        hash = 97 * hash + Objects.hashCode(this.metadata);
        hash = 97 * hash + Objects.hashCode(this.datastreams);
        hash = 97 * hash + Objects.hashCode(this.multiDatastreams);
        hash = 97 * hash + Objects.hashCode(this.properties);
        return hash;
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
        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.encodingType, other.encodingType)) {
            return false;
        }
        if (!Objects.equals(this.metadata, other.metadata)) {
            return false;
        }
        if (!Objects.equals(this.datastreams, other.datastreams)) {
            return false;
        }
        if (!Objects.equals(this.multiDatastreams, other.multiDatastreams)) {
            return false;
        }
        return Objects.equals(this.properties, other.properties);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SENSOR;
    }

    @Override
    public void setEntityPropertiesSet() {
        setDescription = true;
        setEncodingType = true;
        setMetadata = true;
        setProperties = true;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public Object getMetadata() {
        return metadata;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public EntitySet<Datastream> getDatastreams() {
        return datastreams;
    }

    public EntitySet<MultiDatastream> getMultiDatastreams() {
        return multiDatastreams;
    }

    public boolean isSetName() {
        return setName;
    }

    public boolean isSetDescription() {
        return setDescription;
    }

    public boolean isSetEncodingType() {
        return setEncodingType;
    }

    public boolean isSetMetadata() {
        return setMetadata;
    }

    public boolean isSetProperties() {
        return setProperties;
    }

    public void setName(String name) {
        this.name = name;
        setName = name != null;
    }

    public void setDescription(String description) {
        this.description = description;
        setDescription = description != null;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        setEncodingType = encodingType != null;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
        setMetadata = metadata != null;
    }

    public void setProperties(Map<String, Object> properties) {
        if (properties != null && properties.isEmpty()) {
            properties = null;
        }
        this.properties = properties;
        setProperties = true;
    }

    public void setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
    }

    public void setMultiDatastreams(EntitySet<MultiDatastream> multiDatastreams) {
        this.multiDatastreams = multiDatastreams;
    }

}
