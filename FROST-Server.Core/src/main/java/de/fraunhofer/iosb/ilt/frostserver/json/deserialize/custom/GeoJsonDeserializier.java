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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom;

import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.geojson.GeoJsonObject;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;

/**
 * Deserialiser for GeoJSON objects.
 */
public class GeoJsonDeserializier implements CustomDeserializer {

    public static final String APPLICATION_GEOJSON = "application/geo+json";

    public static final Set<String> ENCODINGS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            APPLICATION_GEOJSON,
            "application/vnd.geo+json")));

    public Object deserialize(String json) throws JacksonException {
        return SimpleJsonMapper.getSimpleObjectMapper().readValue(json, GeoJsonObject.class);
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        return parser.readValueAs(GeoJsonObject.class);
    }

    @Override
    public Object deserialize(TreeNode json) throws IOException {
        return SimpleJsonMapper.getSimpleObjectMapper().treeToValue(json, GeoJsonObject.class);
    }
}
