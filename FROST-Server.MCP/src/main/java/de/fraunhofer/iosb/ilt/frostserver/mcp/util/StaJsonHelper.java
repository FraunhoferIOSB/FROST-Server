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
package de.fraunhofer.iosb.ilt.frostserver.mcp.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsonp.JSONPModule;

/**
 * JSON helpers for the MCP layer: parsing STA responses into structures the
 * MCP SDK can embed as structuredContent, pretty-printing for the text blob,
 * and byte-cap checking.
 */
public final class StaJsonHelper {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.ALWAYS))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.ALWAYS))
            .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .addModule(new JSONPModule())
            .build();

    private StaJsonHelper() {
        // Utility class.
    }

    /**
     * The shared generic mapper. Mirrors SimpleJsonMapper's configuration.
     *
     * @return the shared JsonMapper.
     */
    public static JsonMapper getJsonMapper() {
        return MAPPER;
    }

    /**
     * Parse a JSON string into a JsonNode. Returns null for null/empty input.
     *
     * @param json The JSON string to parse. May be null or empty.
     * @return the parsed tree, or null if the input was null or empty.
     * @throws IllegalArgumentException if the string is not valid JSON.
     */
    public static JsonNode parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Failed to parse JSON: " + ex.getOriginalMessage(), ex);
        }
    }

    /**
     * Parse a JSON string into a JsonNode, or null if it is not valid JSON.
     *
     * @param json The JSON string to parse. May be null or empty.
     * @return the parsed tree, or null if the input was null, empty or invalid.
     */
    public static JsonNode parseOrNull(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (JacksonException ex) {
            return null;
        }
    }

    /**
     * Serialize a value to a JSON string. Never throws; returns a fallback
     * string on failure.
     *
     * @param value The value to serialize. May be null.
     * @return the JSON string.
     */
    public static String writeValueAsString(Object value) {
        return MAPPER.writeValueAsString(value);
    }

    /**
     * Serialize a value to a pretty-printed JSON string.
     *
     * @param value The value to serialize. May be null.
     * @return the pretty-printed JSON string.
     */
    public static String writeValueAsPrettyString(Object value) {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    /**
     * Convert a parsed JSON tree into a structure suitable for the structured
     * content field of an MCP tool result. MCP requires a JSON object;
     * non-object results are wrapped in an object under the key "value", and
     * empty/blank results become an empty object.
     *
     * @param node The parsed result. May be null.
     * @return an Object (usually a Map) safe to use as structuredContent.
     */
    public static Object toStructuredContent(JsonNode node) {
        if (node == null || node.isNull()) {
            return java.util.Map.of();
        }
        if (node.isObject()) {
            return MAPPER.treeToValue(node, java.util.Map.class);
        }
        return java.util.Map.of("value", MAPPER.treeToValue(node, Object.class));
    }

}
