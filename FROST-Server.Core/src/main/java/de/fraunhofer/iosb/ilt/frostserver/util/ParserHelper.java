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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomLink;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

/**
 *
 * @author jab
 * @author scf
 */
public class ParserHelper {

    private final ModelRegistry modelRegistry;
    private final CustomLinksHelper customLinksHelper;

    public ParserHelper(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
        this.customLinksHelper = new CustomLinksHelper(modelRegistry);
    }

    public Property parseProperty(String propertyName, Property previous) {
        String decodedName = StringHelper.urlDecode(propertyName);
        if (previous instanceof EntityPropertyMain || previous instanceof EntityPropertyCustom) {
            return parseCustomProperty(decodedName);
        }
        NavigationPropertyMain navProp = null;
        try {
            navProp = modelRegistry.getNavProperty(decodedName);
        } catch (IllegalArgumentException exc) {
            // Not a navigationProperty
        }
        EntityPropertyMain entityProp = null;
        try {
            entityProp = modelRegistry.getEntityProperty(decodedName);
        } catch (IllegalArgumentException exc) {
            // Not an entityProperty
        }
        if (navProp != null && entityProp != null) {
            char first = decodedName.charAt(0);
            if (first >= 'A' && first <= 'Z') {
                return navProp;
            } else {
                return entityProp;
            }
        } else if (navProp != null) {
            return navProp;
        } else if (entityProp != null) {
            return entityProp;
        }
        return parseCustomProperty(decodedName);
    }

    private Property parseCustomProperty(String decodedName) {
        EntityType typeForCustomLink = customLinksHelper.getTypeForCustomLinkName(decodedName);
        if (typeForCustomLink == null) {
            return new EntityPropertyCustom(decodedName);
        } else {
            return new EntityPropertyCustomLink(decodedName, typeForCustomLink);
        }
    }

    public Query parsePathAndQuery(IdManager idManager, String serviceRootUrl, Version version, String pathAndQuery, CoreSettings settings) {
        int index = pathAndQuery.indexOf('?');
        String pathString = pathAndQuery.substring(0, index);
        String queryString = pathAndQuery.substring(index + 1);
        ResourcePath path = PathParser.parsePath(modelRegistry, idManager, serviceRootUrl, version, pathString);
        return QueryParser.parseQuery(queryString, settings, path);
    }
}
