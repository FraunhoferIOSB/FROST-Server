/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;

/**
 * An object holding a reference to an Entity.
 */
public class EntityReference {

    private String atId;

    public Entity resolve(ModelRegistry mr, boolean isAdmin) {
        if (StringHelper.isNullOrEmpty(atId)) {
            throw new IllegalArgumentException("@id is missing or empty");
        }
        return UrlHelper.parseSelfLink(atId, mr, isAdmin);
    }

    @JsonProperty("@id")
    public String getAtId() {
        return atId;
    }

    @JsonProperty("@id")
    public EntityReference setAtId(String atId) {
        this.atId = atId;
        return this;
    }

}
