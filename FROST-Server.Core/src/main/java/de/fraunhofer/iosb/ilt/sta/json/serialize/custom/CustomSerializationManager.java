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
package de.fraunhofer.iosb.ilt.sta.json.serialize.custom;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jab
 */
public class CustomSerializationManager {

    private static CustomSerializationManager instance;
    private final Map<String, CustomSerializer> customSerializers;

    private CustomSerializationManager() {
        customSerializers = new HashMap<>();
    }

    public static CustomSerializationManager getInstance() {
        if (instance == null) {
            instance = new CustomSerializationManager();
        }
        return instance;
    }

    public void registerSerializer(String encodingType, CustomSerializer serializer) {
        if (!customSerializers.containsKey(encodingType)) {
            customSerializers.put(encodingType, serializer);
        }
    }

    public CustomSerializer getSerializer(String encodingType) {
        CustomSerializer result = null;
        if (customSerializers.containsKey(encodingType)) {
            result = customSerializers.get(encodingType);
        }
        if (result == null) {
            result = new DefaultSerializer();
        }
        return result;
    }
}
