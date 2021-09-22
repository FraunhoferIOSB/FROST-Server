/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;

/**
 *
 * @author hylke
 */
public class ParserUtils {

    private ParserUtils() {
        // Utility class.
    }

    public static Query parsePathAndQuery(IdManager idManager, String serviceRootUrl, Version version, String pathAndQuery, CoreSettings settings) {
        int index = pathAndQuery.indexOf('?');
        String pathString = pathAndQuery.substring(0, index);
        String queryString = pathAndQuery.substring(index + 1);
        ResourcePath path = PathParser.parsePath(settings.getModelRegistry(), idManager, serviceRootUrl, version, pathString);
        return QueryParser.parseQuery(queryString, settings, path).validate(path.getMainElementType());
    }

}
