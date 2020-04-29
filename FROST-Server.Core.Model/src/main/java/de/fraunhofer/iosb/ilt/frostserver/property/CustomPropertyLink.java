/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.property;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;

/**
 *
 * @author hylke
 */
public class CustomPropertyLink implements Property {

    private static final String UNSUPPORTED = "Not supported on custom properties.";

    private final String name;
    private final EntityType targetEntityType;

    public CustomPropertyLink(String name, EntityType targetEntityType) {
        this.name = name;
        this.targetEntityType = targetEntityType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJsonName() {
        return getName();
    }

    public EntityType getTargetEntityType() {
        return targetEntityType;
    }

    @Override
    public Object getFrom(Entity<?> entity) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public void setOn(Entity<?> entity, Object value) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public boolean isSetOn(Entity<?> entity) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

}
