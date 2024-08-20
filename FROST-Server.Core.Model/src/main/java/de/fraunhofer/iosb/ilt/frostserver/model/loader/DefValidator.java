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
package de.fraunhofer.iosb.ilt.frostserver.model.loader;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;

/**
 *
 * @author hylke
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface DefValidator {

    /**
     * Create the defined validator(s) for the given EntityType on the model in
     * the given ModelRegistry.
     *
     * @param modelRegistry The model to modify.
     * @param entityType The entity type to create the validator for.
     */
    public void createValidators(ModelRegistry modelRegistry, EntityType entityType);
}
