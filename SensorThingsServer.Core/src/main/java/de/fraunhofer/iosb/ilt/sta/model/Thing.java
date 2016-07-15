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
import de.fraunhofer.iosb.ilt.sta.model.id.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class Thing extends AbstractEntity {

    private String name;
    private String description;
    private Map<String, Object> properties;
    private EntitySet<Location> locations; // 0..*

    private EntitySet<HistoricalLocation> historicalLocations; // 0..*
    private EntitySet<Datastream> datastreams; // 0..*

    private boolean setName;
    private boolean setDescription;
    private boolean setProperties;

    public Thing() {
        this.properties = new HashMap<>();
        this.locations = new EntitySetImpl<>(EntityType.Location);
        this.historicalLocations = new EntitySetImpl<>(EntityType.HistoricalLocation);
        this.datastreams = new EntitySetImpl<>(EntityType.Datastream);
    }

    public Thing(Id id,
            String selfLink,
            String navigationLink,
            String name,
            String description,
            Map<String, Object> properties,
            EntitySet<Location> locations,
            EntitySet<HistoricalLocation> historicalLocations,
            EntitySet<Datastream> datastreams) {
        super(id, selfLink, navigationLink);
        this.properties = new HashMap<>();
        this.name = name;
        this.description = description;
        this.properties.putAll(properties);
        this.locations = locations;
        this.historicalLocations = historicalLocations;
        this.datastreams = datastreams;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Thing;
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        EntityType type = containingSet.getEntityType();
        if (type != getEntityType()) {
            throw new IllegalStateException("Set of type " + type + " can not contain a " + getEntityType());
        }
        super.complete();
    }

    @Override
    public void setEntityPropertiesSet() {
        setDescription = true;
        setProperties = true;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public EntitySet<Location> getLocations() {
        return locations;
    }

    public EntitySet<HistoricalLocation> getHistoricalLocations() {
        return historicalLocations;
    }

    public EntitySet<Datastream> getDatastreams() {
        return datastreams;
    }

    public boolean isSetName() {
        return setName;
    }

    public boolean isSetDescription() {
        return setDescription;
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

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
        setProperties = true;
    }

    public void setLocations(EntitySet<Location> locations) {
        this.locations = locations;
    }

    public void setHistoricalLocations(EntitySet<HistoricalLocation> historicalLocations) {
        this.historicalLocations = historicalLocations;
    }

    public void setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.description);
        hash = 71 * hash + Objects.hashCode(this.properties);
        hash = 71 * hash + Objects.hashCode(this.locations);
        hash = 71 * hash + Objects.hashCode(this.historicalLocations);
        hash = 71 * hash + Objects.hashCode(this.datastreams);
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
        final Thing other = (Thing) obj;
        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        if (!Objects.equals(this.locations, other.locations)) {
            return false;
        }
        if (!Objects.equals(this.historicalLocations, other.historicalLocations)) {
            return false;
        }
        if (!Objects.equals(this.datastreams, other.datastreams)) {
            return false;
        }
        return true;
    }
}
