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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author jab
 */
public interface Id {

    @JsonValue
    public Object getValue();

    /**
     * Get the value as it should be formatted in a url.
     *
     * @return the value as it should be formatted in a url.
     */
    public String getUrl();

    /**
     * Get the value as it appears in JSON, as an element. This is used to
     * directly search and replace in a json string.
     *
     * @return the value as JSON element.
     */
    public String getJson();

    public String getBasicPersistenceType();

    public Object asBasicPersistenceType();

}
