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

import de.fraunhofer.iosb.ilt.frostserver.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;

/**
 *
 * @author jab, scf
 */
public class HistoricalLocation extends AbstractEntity<HistoricalLocation> {

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
        this(null);
    }

    public HistoricalLocation(Id id) {
        super(id);
        this.locations = new EntitySetImpl<>(EntityType.LOCATION);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HISTORICALLOCATION;
    }

    @Override
    public void complete(PathElementEntitySet containingSet) throws IncompleteEntityException {
        EntityType type = containingSet.getEntityType();
        if (type != getEntityType()) {
            throw new IllegalStateException("Set of type " + type + " can not contain a " + getEntityType());
        }
        PathElement parent = containingSet.getParent();
        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                if (parentEntity.getEntityType() == EntityType.THING) {
                    setThing(new Thing(parentId));
                    LOGGER.debug("Set thingId to {}.", parentId);
                } else {
                    LOGGER.error("Incorrect 'parent' entity type for {}: {}", getEntityType(), parentEntity.getEntityType());
                }
            }
        }
        if (getLocations().isEmpty()) {
            throw new IncompleteEntityException(getEntityType() + " must have at least one Location.");
        }
        super.complete();
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setSets(set, entityPropertiesOnly);
    }

    private void setSets(boolean set, boolean entityPropertiesOnly) {
        setTime = set;
        if (!entityPropertiesOnly) {
            setThing = set;
        }
    }

    @Override
    public void setEntityPropertiesSet(HistoricalLocation comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSets(false, false);
        if (!Objects.equals(time, comparedTo.getTime())) {
            setTime = true;
            message.addEpField(EntityProperty.TIME);
        }
        if (!Objects.equals(thing, comparedTo.getThing())) {
            setThing = true;
            message.addNpField(NavigationProperty.THING);
        }
    }

    public TimeInstant getTime() {
        return time;
    }

    public void setTime(TimeInstant time) {
        this.time = time;
        setTime = time != null;
    }

    public boolean isSetTime() {
        return setTime;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
        setThing = thing != null;
    }

    public boolean isSetThing() {
        return setThing;
    }

    public EntitySet<Location> getLocations() {
        return locations;
    }

    public void setLocations(EntitySet<Location> locations) {
        this.locations = locations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, thing, locations);
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
        return super.equals(other)
                && Objects.equals(this.time, other.time)
                && Objects.equals(this.thing, other.thing)
                && Objects.equals(this.locations, other.locations);
    }

}
