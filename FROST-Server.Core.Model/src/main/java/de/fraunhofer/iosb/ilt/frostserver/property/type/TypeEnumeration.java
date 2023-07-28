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
package de.fraunhofer.iosb.ilt.frostserver.property.type;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author hylke
 * @param <K> The enum this Type extends.
 */
public class TypeEnumeration<K extends Enum<K>> extends PropertyType {

    private final Class<K> enumClass;

    public TypeEnumeration(String name, String description, Class<K> enumClass, TypeReference typeReference) {
        super(name, description, typeReference);
        this.enumClass = enumClass;
    }

    public Class<K> getEnumClass() {
        return enumClass;
    }

    public Map<String, Number> getValues() {
        Map<String, Number> members = new LinkedHashMap<>();
        for (K member : enumClass.getEnumConstants()) {
            members.put(member.toString(), member.ordinal());
        }
        return members;
    }
}
