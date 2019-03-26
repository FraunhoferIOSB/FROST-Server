/*
 * Copyright 2016 Open Geospatial Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
