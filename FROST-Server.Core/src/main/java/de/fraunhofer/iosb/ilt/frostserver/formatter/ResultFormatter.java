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
package de.fraunhofer.iosb.ilt.frostserver.formatter;

import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;

/**
 *
 * @author jab
 */
public interface ResultFormatter {

    /**
     * Validate and optionally modify the request.
     *
     * @param path The path that was requested.
     * @param query The query parameters of the request.
     * @throws IncorrectRequestException if the request is not valid for this
     * formatter.
     */
    public default void preProcessRequest(ResourcePath path, Query query) throws IncorrectRequestException {
        // By default nothing is preprocessed.
    }

    /**
     * Format the result object.
     *
     * @param path The path that was requested.
     * @param query The query parameters of the request.
     * @param result The result to format.
     * @param useAbsoluteNavigationLinks Flag indicating absolute navigation
     * links should be used.
     * @return The formatted result object.
     */
    public FormatWriter format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks);

    /**
     * Get the content type of the result, when formatted by this
     * ResultFormatter.
     *
     * @return The content type of the formatted result.
     */
    public String getContentType();
}
