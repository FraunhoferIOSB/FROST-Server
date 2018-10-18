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
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
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
public class HistoricalLocation extends AbstractEntity {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalLocation.class);
    private TimeInstant time;
    private Thing thing;
    private EntitySet<Location> locations;

    private boolean setTime;
    private boolean setThing;

    public HistoricalLocation() {
        this.locations = new EntitySetImpl<>(EntityType.LOCATION);
    }

    public HistoricalLocation(
            Id id,
            String selfLink,
            String navigationLink,
            TimeInstant time,
            Thing thing,
            EntitySet<Location> locations) {
        super(id, selfLink, navigationLink);
        this.time = time;
        this.thing = thing;
        this.locations = locations;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HISTORICALLOCATION;
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        EntityType type = containingSet.getEntityType();
        if (type != getEntityType()) {
            throw new IllegalStateException("Set of type " + type + " can not contain a " + getEntityType());
        }
        ResourcePathElement parent = containingSet.getParent();
        if (parent instanceof EntityPathElement) {
            EntityPathElement parentEntity = (EntityPathElement) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                switch (parentEntity.getEntityType()) {
                    case THING:
                        setThing(new ThingBuilder().setId(parentId).build());
                        LOGGER.debug("Set thingId to {}.", parentId);
                        break;

                    default:
                        LOGGER.error("Incorrect 'parent' entity type for {}: {}", getEntityType(), parentEntity.getEntityType());
                        break;
                }
            }
        }
        if (getLocations().isEmpty()) {
            throw new IncompleteEntityException(getEntityType() + " must have at least one Location.");
        }
        super.complete();
    }

    @Override
    public void setEntityPropertiesSet() {
        setTime = true;
    }

    public TimeInstant getTime() {
        return time;
    }

    public Thing getThing() {
        return thing;
    }

    public EntitySet<Location> getLocations() {
        return locations;
    }

    public boolean isSetTime() {
        return setTime;
    }

    public boolean isSetThing() {
        return setThing;
    }

    public void setTime(TimeInstant time) {
        this.time = time;
        setTime = time != null;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
        setThing = thing != null;
    }

    public void setLocations(EntitySet<Location> locations) {
        this.locations = locations;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.time);
        hash = 97 * hash + Objects.hashCode(this.thing);
        hash = 97 * hash + Objects.hashCode(this.locations);
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
        final HistoricalLocation other = (HistoricalLocation) obj;
        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.time, other.time)) {
            return false;
        }
        if (!Objects.equals(this.thing, other.thing)) {
            return false;
        }
        if (!Objects.equals(this.locations, other.locations)) {
            return false;
        }
        return true;
    }
}
