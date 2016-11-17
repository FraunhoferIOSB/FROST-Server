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
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class ObservedProperty extends AbstractEntity {

    private String name;
    private String definition;
    private String description;
    private EntitySet<Datastream> datastreams;
    private EntitySet<MultiDatastream> multiDatastreams;

    private boolean setName;
    private boolean setDefinition;
    private boolean setDescription;

    public ObservedProperty() {
        this.datastreams = new EntitySetImpl<>(EntityType.Datastream);
        this.multiDatastreams = new EntitySetImpl<>(EntityType.MultiDatastream);
    }

    public ObservedProperty(
            Id id,
            String selfLink,
            String navigationLink,
            String name,
            String definition,
            String description,
            EntitySet<Datastream> datastreams,
            EntitySet<MultiDatastream> multiDatastreams) {
        super(id, selfLink, navigationLink);
        this.name = name;
        this.definition = definition;
        this.description = description;
        this.datastreams = datastreams;
        this.multiDatastreams = multiDatastreams;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.definition);
        hash = 29 * hash + Objects.hashCode(this.description);
        hash = 29 * hash + Objects.hashCode(this.datastreams);
        hash = 29 * hash + Objects.hashCode(this.multiDatastreams);
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
        final ObservedProperty other = (ObservedProperty) obj;
        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.definition, other.definition)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.datastreams, other.datastreams)) {
            return false;
        }
        if (!Objects.equals(this.multiDatastreams, other.multiDatastreams)) {
            return false;
        }
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ObservedProperty;
    }

    @Override
    public void setEntityPropertiesSet() {
        setName = true;
        setDefinition = true;
        setDescription = true;
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    public String getDescription() {
        return description;
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

    public boolean isSetDefinition() {
        return setDefinition;
    }

    public boolean isSetDescription() {
        return setDescription;
    }

    public void setName(String name) {
        this.name = name;
        setName = true;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        setDefinition = true;
    }

    public void setDescription(String description) {
        this.description = description;
        setDescription = true;
    }

    public void setDatastreams(EntitySet<Datastream> datastreams) {
        this.datastreams = datastreams;
    }

    public void setMultiDatastreams(EntitySet<MultiDatastream> multiDatastreams) {
        this.multiDatastreams = multiDatastreams;
    }

}
