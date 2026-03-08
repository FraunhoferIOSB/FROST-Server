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
package de.fraunhofer.iosb.ilt.frostserver.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;
import tools.jackson.datatype.jsonp.JSONPModule;

/**
 * A simple JSON Mapper for non-STA use.
 */
public class SimpleJsonMapper {

    private static final String FAILED_JSON_PARSE = "Failed to parse stored json.";

    private static ObjectMapper simpleObjectMapper;

    private SimpleJsonMapper() {
        // Utility class.
    }

    /**
     * get an ObjectMapper for generic, non-STA use.
     *
     * @return an ObjectMapper for generic, non-STA use.
     */
    public static ObjectMapper getSimpleObjectMapper() {
        if (simpleObjectMapper == null) {
            simpleObjectMapper = JsonMapper.builder()
                    .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.ALWAYS))
                    .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.ALWAYS))
                    .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                    .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                    .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    .addModule(new JSONPModule())
                    .build();
        }
        return simpleObjectMapper;
    }

    public static JsonNode valueToTree(Object value) {
        return getSimpleObjectMapper().valueToTree(value);
    }

    public static JsonNode jsonToTreeOrString(String json) {
        if (json == null) {
            return null;
        }

        try {
            return getSimpleObjectMapper().readTree(json);
        } catch (JacksonException ex) {
            return new StringNode(json);
        }
    }

    public static JsonNode jsonToTree(String json) {
        if (json == null) {
            return null;
        }

        try {
            return getSimpleObjectMapper().readTree(json);
        } catch (JacksonException ex) {
            throw new IllegalStateException(FAILED_JSON_PARSE, ex);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return getSimpleObjectMapper().readValue(json, clazz);
        } catch (JacksonException ex) {
            throw new IllegalStateException(FAILED_JSON_PARSE, ex);
        }
    }

    public static <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }
        try {
            return getSimpleObjectMapper().readValue(json, typeReference);
        } catch (JacksonException ex) {
            throw new IllegalStateException(FAILED_JSON_PARSE, ex);
        }
    }
}
