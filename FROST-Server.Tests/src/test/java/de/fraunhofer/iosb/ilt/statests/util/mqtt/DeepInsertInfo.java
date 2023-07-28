/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.statests.util.mqtt;

import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jab
 */
public class DeepInsertInfo {

    private final EntityType entityType;
    private final List<EntityType> subEntityTypes;

    public DeepInsertInfo(EntityType entityType, List<EntityType> subEntityTypes) {
        this.entityType = entityType;
        this.subEntityTypes = subEntityTypes;
    }

    public DeepInsertInfo(EntityType entityType) {
        this.entityType = entityType;
        this.subEntityTypes = new ArrayList<>();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public List<EntityType> getSubEntityTypes() {
        return subEntityTypes;
    }

}
