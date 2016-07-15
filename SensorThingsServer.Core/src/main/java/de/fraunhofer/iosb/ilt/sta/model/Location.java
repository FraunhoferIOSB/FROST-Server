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

import de.fraunhofer.iosb.ilt.sta.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.id.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class Location extends AbstractEntity {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Location.class);
    private String name;
    private String description;
    private String encodingType;
    private Object location;
    private EntitySet<HistoricalLocation> historicalLocations; // 0..*
    private EntitySet<Thing> things;

    private boolean setName;
    private boolean setDescription;
    private boolean setEncodingType;
    private boolean setLocation;

    public Location() {
        this.things = new EntitySetImpl<>(EntityType.Thing);
        this.historicalLocations = new EntitySetImpl<>(EntityType.HistoricalLocation);
    }

    public Location(
            Id id,
            String selfLink,
            String navigationLink,
            String name,
            String description,
            String encodingType,
            Object location,
            EntitySet<HistoricalLocation> historicalLocations,
            EntitySet<Thing> things) {
        super(id, selfLink, navigationLink);
        this.name = name;
        this.description = description;
        this.encodingType = encodingType;
        this.location = location;
        this.historicalLocations = historicalLocations;
        this.things = things;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Location;
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        EntityType type = containingSet.getEntityType();
        if (type != getEntityType()) {
            throw new IllegalStateException("Set of type " + type + " can not contain a " + getEntityType());
        }
        ResourcePathElement parent = containingSet.getParent();
        if (parent != null && parent instanceof EntityPathElement) {
            EntityPathElement parentEntity = (EntityPathElement) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                switch (parentEntity.getEntityType()) {
                    case Thing:
                        getThings().add(new ThingBuilder().setId(parentId).build());
                        LOGGER.debug("Added thingId to {}.", parentId);
                        break;
                }
            }
        }
        super.complete();
    }

    @Override
    public void setEntityPropertiesSet() {
        setDescription = true;
        setEncodingType = true;
        setLocation = true;
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

    public Object getLocation() {
        return location;
    }

    public EntitySet<HistoricalLocation> getHistoricalLocations() {
        return historicalLocations;
    }

    public EntitySet<Thing> getThings() {
        return things;
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

    public boolean isSetLocation() {
        return setLocation;
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

    public void setLocation(Object location) {
        this.location = location;
        setLocation = true;
    }

    public void setHistoricalLocations(EntitySet<HistoricalLocation> historicalLocations) {
        this.historicalLocations = historicalLocations;
    }

    public void setThings(EntitySet<Thing> things) {
        this.things = things;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + Objects.hashCode(this.encodingType);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.historicalLocations);
        hash = 53 * hash + Objects.hashCode(this.things);
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
        final Location other = (Location) obj;
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
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.historicalLocations, other.historicalLocations)) {
            return false;
        }
        if (!Objects.equals(this.things, other.things)) {
            return false;
        }
        return true;
    }
}
