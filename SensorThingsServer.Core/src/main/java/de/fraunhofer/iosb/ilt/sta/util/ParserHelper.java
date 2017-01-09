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
package de.fraunhofer.iosb.ilt.sta.util;

import de.fraunhofer.iosb.ilt.sta.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.sta.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.sta.path.CustomProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class ParserHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParserHelper.class);

    public static final class PathQuery {

        public final ResourcePath path;
        public final Query query;

        public PathQuery(ResourcePath path, Query query) {
            this.path = path;
            this.query = query;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.path);
            hash = 97 * hash + Objects.hashCode(this.query);
            return hash;
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
            if (!Objects.equals(this.path, other.path)) {
                return false;
            }
            if (!Objects.equals(this.query, other.query)) {
                return false;
            }
            return true;
        }

    }

    private ParserHelper() {

    }

    public static Property parseProperty(String propertyName, Property previous) {
        String decodedName;
        try {
            decodedName = URLDecoder.decode(propertyName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("UTF-8 is not a supported encoding?!", ex);
            throw new IllegalStateException(ex);
        }
        if (previous instanceof EntityProperty || previous instanceof CustomProperty) {
            return new CustomProperty(decodedName);
        }
        NavigationProperty navProp = null;
        try {
            navProp = NavigationProperty.fromString(decodedName);
        } catch (IllegalArgumentException e) {
        }
        EntityProperty entityProp = null;
        try {
            entityProp = EntityProperty.fromString(decodedName);
        } catch (IllegalArgumentException e2) {
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

    public static PathQuery parsePathAndQuery(String serviceRootUrl, String pathAndQuery) {
        return parsePathAndQuery(serviceRootUrl, pathAndQuery, new CoreSettings());
    }

    public static PathQuery parsePathAndQuery(String serviceRootUrl, String pathAndQuery, CoreSettings settings) {
        int index = pathAndQuery.indexOf('?');
        String pathString = pathAndQuery.substring(0, index);
        String queryString = pathAndQuery.substring(index + 1);
        ResourcePath path = PathParser.parsePath(serviceRootUrl, pathString);
        Query query = QueryParser.parseQuery(queryString, settings);
        return new PathQuery(path, query);
    }
}
