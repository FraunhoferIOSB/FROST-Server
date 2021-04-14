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
package de.fraunhofer.iosb.ilt.frostserver.path;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomLink;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;

/**
 *
 * @author jab
 * @author scf
 */
public class ParserHelper {

    private final CustomLinksHelper customLinksHelper;

    public ParserHelper(ModelRegistry modelRegistry) {
        this.customLinksHelper = new CustomLinksHelper(modelRegistry, false, 0);
    }

    public Property parseProperty(EntityType type, String propertyName, Property previous) {
        String decodedName = StringHelper.urlDecode(propertyName);
        if (previous instanceof EntityPropertyMain || previous instanceof EntityPropertyCustom) {
            return parseCustomProperty(decodedName);
        }
        NavigationPropertyMain navProp = type.getNavigationProperty(decodedName);
        if (navProp != null) {
            return navProp;
        }
        EntityPropertyMain entityProp = type.getEntityProperty(decodedName);
        if (entityProp != null) {
            return entityProp;
        }
        throw new IllegalArgumentException("Could not place " + propertyName + " under type " + type + " after " + previous);
    }

    private Property parseCustomProperty(String decodedName) {
        EntityType typeForCustomLink = customLinksHelper.getTypeForCustomLinkName(decodedName);
        if (typeForCustomLink == null) {
            return new EntityPropertyCustom(decodedName);
        } else {
            return new EntityPropertyCustomLink(decodedName, typeForCustomLink);
        }
    }
}
