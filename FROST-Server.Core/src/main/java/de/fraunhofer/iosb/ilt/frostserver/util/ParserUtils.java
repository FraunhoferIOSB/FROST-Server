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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdUuid;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.UUID;

/**
 *
 * @author hylke
 */
public class ParserUtils {

    private ParserUtils() {
        // Utility class.
    }

    public static Query parsePathAndQuery(String serviceRootUrl, Version version, String pathAndQuery, CoreSettings settings) {
        int index = pathAndQuery.indexOf('?');
        String pathString = pathAndQuery.substring(0, index);
        String queryString = pathAndQuery.substring(index + 1);
        ResourcePath path = PathParser.parsePath(settings.getModelRegistry(), serviceRootUrl, version, pathString);
        return QueryParser.parseQuery(queryString, settings, path).validate(path.getMainElementType());
    }

    public static Id idFromObject(Object input) {
        if (input instanceof Id id) {
            return id;
        }
        if (input instanceof UUID uuid) {
            return new IdUuid(uuid);
        }
        if (input instanceof Number number) {
            return new IdLong(number.longValue());
        }
        if (input instanceof CharSequence) {
            return new IdString(input.toString());
        }
        throw new IllegalArgumentException("Can not use " + ((input == null) ? "null" : input.getClass().getName()) + " (" + input + ") as an Id");
    }

}
