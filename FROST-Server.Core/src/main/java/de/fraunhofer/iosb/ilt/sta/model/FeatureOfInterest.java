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
public class FeatureOfInterest extends AbstractEntity {

    private String name;
    private String description;
    private String encodingType;
    private Object feature;
    private Map<String, Object> properties;
    private EntitySet<Observation> observations;

    private boolean setName;
    private boolean setDescription;
    private boolean setEncodingType;
    private boolean setFeature;
    private boolean setProperties;

    public FeatureOfInterest() {
        this.observations = new EntitySetImpl<>(EntityType.Observation);
    }

    public FeatureOfInterest(
            Id id,
            String selfLink,
            String navigationLink,
            String name,
            String description,
            String encodingType,
            Object feature,
            Map<String, Object> properties,
            EntitySet<Observation> observations) {
        super(id, selfLink, navigationLink);
        this.name = name;
        this.description = description;
        this.encodingType = encodingType;
        this.feature = feature;
        this.observations = observations;
        if (properties != null && !properties.isEmpty()) {
            this.properties = new HashMap<>(properties);
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FeatureOfInterest;
    }

    @Override
    public void setEntityPropertiesSet() {
        setDescription = true;
        setEncodingType = true;
        setFeature = true;
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

    public Object getFeature() {
        return feature;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public EntitySet<Observation> getObservations() {
        return observations;
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

    public boolean isSetFeature() {
        return setFeature;
    }

    public boolean isSetProperties() {
        return setProperties;
    }

    public void setName(String name) {
        this.name = name;
        setName = true;
    }

    public void setDescription(String description) {
        this.description = description;
        setDescription = true;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        setEncodingType = true;
    }

    public void setFeature(Object feature) {
        setFeature = true;
        this.feature = feature;
    }

    public void setObservations(EntitySet<Observation> observations) {
        this.observations = observations;
    }

    public void setProperties(Map<String, Object> properties) {
        if (properties != null && properties.isEmpty()) {
            properties = null;
        }
        this.properties = properties;
        setProperties = true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.description);
        hash = 41 * hash + Objects.hashCode(this.encodingType);
        hash = 41 * hash + Objects.hashCode(this.feature);
        hash = 41 * hash + Objects.hashCode(this.observations);
        hash = 41 * hash + Objects.hashCode(this.properties);
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
        final FeatureOfInterest other = (FeatureOfInterest) obj;
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
        if (!Objects.equals(this.feature, other.feature)) {
            return false;
        }
        if (!Objects.equals(this.observations, other.observations)) {
            return false;
        }
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        return true;
    }

}
