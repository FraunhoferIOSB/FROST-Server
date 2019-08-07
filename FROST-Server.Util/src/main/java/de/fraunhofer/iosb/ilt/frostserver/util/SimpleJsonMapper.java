/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fraunhofer.iosb.ilt.frostserver.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author scf
 */
public class SimpleJsonMapper {

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
            simpleObjectMapper = new ObjectMapper()
                    .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        }
        return simpleObjectMapper;
    }
}
