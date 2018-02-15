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
package de.fraunhofer.iosb.ilt.sta.model.id;

import com.fasterxml.jackson.annotation.JsonValue;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
//import de.fraunhofer.iosb.ilt.sta.dao.BasicPersistenceType;

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

    public BasicPersistenceType getBasicPersistenceType();

    public Object asBasicPersistenceType();

    public void fromBasicPersitenceType(Object data);
}
