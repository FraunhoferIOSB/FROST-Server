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

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.Objects;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author jab
 */
public class IdString implements Id {

    /**
     * The KEY used to identify the persistence type of this ID implementation.
     */
    public static final String PERSISTENCE_TYPE_STRING = "STRING";

    private String value;

    public IdString(String value) {
        this.value = value;
    }

    @Override
    public String getBasicPersistenceType() {
        return PERSISTENCE_TYPE_STRING;
    }

    @Override
    public Object asBasicPersistenceType() {
        return value;
    }

    @Override
    public void fromBasicPersitenceType(Object data) {
        value = data.toString();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getUrl() {
        return "'"
                + StringHelper.urlEncode(StringHelper.escapeForStringConstant(value), true)
                + "'";
    }

    @Override
    public String getJson() {
        return '"' + StringEscapeUtils.escapeJson(value) + '"';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IdString other = (IdString) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Objects.toString(getValue());
    }

}
