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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class JsonValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonValue.class.getName());

    private Object value;
    private TypeReference type;
    private String stringValue;
    private int stringLength = 0;

    public JsonValue(String stringValue) {
        this.stringValue = stringValue;
        if (stringValue != null) {
            stringLength = stringValue.length();
        }
    }

    public JsonValue(JsonNode value) {
        this.value = value;
    }

    public JsonValue(Object value) {
        this.value = value;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Object getValue() {
        if (value == null) {
            return Utils.jsonToTree(stringValue);
        }
        return value;
    }

    public Map<String, Object> getMapValue() {
        return getValue(Utils.TYPE_MAP_STRING_OBJECT);
    }

    public <T> T getValue(TypeReference<T> typeReference) {
        if (type != null && !type.equals(typeReference)) {
            LOGGER.warn("Type Switch, from {} to {}", type, typeReference);
            type = null;
        }
        if (type == null) {
            type = typeReference;
            value = Utils.jsonToObject(stringValue, typeReference);
        }
        return (T) value;
    }

    public int getStringLength() {
        return stringLength;
    }

}
