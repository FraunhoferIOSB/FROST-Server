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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hylke
 */
public class NavigationPropertyCustom implements NavigationProperty {

    private final EntityProperty entityProperty;
    private final List<String> subPath = new ArrayList<>();

    public NavigationPropertyCustom(EntityProperty entityProperty) {
        this.entityProperty = entityProperty;
    }

    @Override
    public EntityType getType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> getSubPath() {
        return subPath;
    }

    public void addToSubPath(String subPathElement) {
        subPath.add(subPathElement);
    }

    @Override
    public boolean validFor(EntityType entityType) {
        return entityType.getPropertySet().contains(entityProperty);
    }

    @Override
    public boolean isSet() {
        return false;
    }

    @Override
    public String getName() {
        return entityProperty.entitiyName + "/" + StringUtils.join(subPath, '/');
    }

    @Override
    public String getJsonName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getGetterName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSetterName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getIsSetName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
