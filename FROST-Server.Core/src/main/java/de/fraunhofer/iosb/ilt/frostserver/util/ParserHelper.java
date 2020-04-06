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

import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.property.CustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class ParserHelper {

    public static final class PathQuery {

        public final ResourcePath path;
        public final Query query;

        public PathQuery(ResourcePath path, Query query) {
            this.path = path;
            this.query = query;
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, query);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PathQuery other = (PathQuery) obj;
            return Objects.equals(this.path, other.path)
                    && Objects.equals(this.query, other.query);
        }

    }

    private ParserHelper() {

    }

    public static Property parseProperty(String propertyName, Property previous) {
        String decodedName;
        decodedName = StringHelper.urlDecode(propertyName);
        if (previous instanceof EntityProperty || previous instanceof CustomProperty) {
            return new CustomProperty(decodedName);
        }
        NavigationPropertyMain navProp = null;
        try {
            navProp = NavigationPropertyMain.fromString(decodedName);
        } catch (IllegalArgumentException exc) {
            // Not a navigationProperty
        }
        EntityProperty entityProp = null;
        try {
            entityProp = EntityProperty.fromString(decodedName);
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
        return new CustomProperty(decodedName);
    }

    public static PathQuery parsePathAndQuery(IdManager idManager, String serviceRootUrl, String pathAndQuery) {
        return parsePathAndQuery(idManager, serviceRootUrl, pathAndQuery, new CoreSettings());
    }

    public static PathQuery parsePathAndQuery(IdManager idManager, String serviceRootUrl, String pathAndQuery, CoreSettings settings) {
        int index = pathAndQuery.indexOf('?');
        String pathString = pathAndQuery.substring(0, index);
        String queryString = pathAndQuery.substring(index + 1);
        ResourcePath path = PathParser.parsePath(idManager, serviceRootUrl, pathString);
        Query query = QueryParser.parseQuery(queryString, settings);
        return new PathQuery(path, query);
    }
}
