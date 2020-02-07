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

import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NamedEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;

/**
 *
 * @author jab, scf
 */
public class Location extends NamedEntity<Location> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Location.class);

    private String encodingType;
    private Object location;
    private EntitySet<HistoricalLocation> historicalLocations; // 0..*
    private EntitySet<Thing> things;

    private boolean setEncodingType;
    private boolean setLocation;

    public Location() {
        this(null);
    }

    public Location(Id id) {
        super(id);
        this.things = new EntitySetImpl<>(EntityType.THING);
        this.historicalLocations = new EntitySetImpl<>(EntityType.HISTORICALLOCATION);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.LOCATION;
    }

    @Override
    public void complete(PathElementEntitySet containingSet) throws IncompleteEntityException {
        PathElement parent = containingSet.getParent();
        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                if (parentEntity.getEntityType() == EntityType.THING) {
                    getThings().add(new Thing(parentId));
                    LOGGER.debug("Added thingId to {}.", parentId);
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
        setSets(set);
    }

    private void setSets(boolean set) {
        setEncodingType = set;
        setLocation = set;
    }

    @Override
    public void setEntityPropertiesSet(Location comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSets(false);
        if (!Objects.equals(encodingType, comparedTo.getEncodingType())) {
            setEncodingType = true;
            message.addEpField(EntityProperty.ENCODINGTYPE);
        }
        if (!Objects.equals(location, comparedTo.getLocation())) {
            setLocation = true;
            message.addEpField(EntityProperty.LOCATION);
        }
    }

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        setEncodingType = encodingType != null;
    }

    public boolean isSetEncodingType() {
        return setEncodingType;
    }

    public Object getLocation() {
        return location;
    }

    public void setLocation(Object location) {
        this.location = location;
        setLocation = location != null;
    }

    public boolean isSetLocation() {
        return setLocation;
    }

    public EntitySet<HistoricalLocation> getHistoricalLocations() {
        return historicalLocations;
    }

    public void setHistoricalLocations(EntitySet<HistoricalLocation> historicalLocations) {
        this.historicalLocations = historicalLocations;
    }

    public EntitySet<Thing> getThings() {
        return things;
    }

    public void setThings(EntitySet<Thing> things) {
        this.things = things;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), encodingType, location, historicalLocations, things);
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
        return super.equals(other)
                && Objects.equals(encodingType, other.encodingType)
                && Objects.equals(location, other.location)
                && Objects.equals(historicalLocations, other.historicalLocations)
                && Objects.equals(things, other.things);
    }

}
