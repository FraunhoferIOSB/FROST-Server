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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch;

/**
 * Turns a ContentIdPair into the correct form to include in a JSON-Batch
 * request.
 */
public interface ContentIdPairHandler {

    /**
     * Transforms the given pair to be included in raw JSON.
     *
     * @param pair The pair to transform.
     * @param brackets if true, the placeholder was found in brackets in a
     * bigger string. The brackets must be included in the returned string.
     * @return the transformed pair.
     */
    public String transform(ContentIdPair pair, boolean brackets);
}
