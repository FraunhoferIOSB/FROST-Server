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
package de.fraunhofer.iosb.ilt.frostserver.model.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import java.util.Set;

/**
 * MixIn for serialisation.
 *
 * @author jab, scf
 */
public interface AbstractEntityMixIn {

    @JsonProperty("@iot.id")
    public Id getId();

    @JsonProperty("@iot.selfLink")
    public String getSelfLink();

    @JsonIgnore
    public String getNavigationLink();

    @JsonIgnore
    public boolean isExportObject();

    @JsonIgnore
    public Set<String> getSelectedPropertyNames();

    @JsonIgnore
    public void setSelectedProperties(Set<Property> selectedProperties);

    @JsonIgnore
    public EntityType getEntityType();

    @JsonIgnore
    public boolean isSetId();

    @JsonIgnore
    public boolean isSetSelfLink();

    @JsonIgnore
    public void setSets(boolean set);
}
