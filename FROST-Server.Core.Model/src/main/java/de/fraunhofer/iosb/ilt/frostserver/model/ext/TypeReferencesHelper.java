/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.model.ext;

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import java.util.List;
import java.util.Map;
import org.geojson.GeoJsonObject;

/**
 *
 * @author hylke
 */
public class TypeReferencesHelper {

    public static final TypeReference<Entity> TYPE_REFERENCE_ENTITY = new TypeReference<Entity>() {
        // Empty on purpose.
    };
    public static final TypeReference<EntitySet> TYPE_REFERENCE_ENTITYSET = new TypeReference<EntitySet>() {
        // Empty on purpose.
    };
    public static final TypeReference<Id> TYPE_REFERENCE_ID = new TypeReference<Id>() {
        // Empty on purpose.
    };
    public static final TypeReference<Object> TYPE_REFERENCE_OBJECT = new TypeReference<Object>() {
        // Empty on purpose.
    };
    public static final TypeReference<String> TYPE_REFERENCE_STRING = new TypeReference<String>() {
        // Empty on purpose.
    };
    public static final TypeReference<Integer> TYPE_REFERENCE_INTEGER = new TypeReference<Integer>() {
        // Empty on purpose.
    };
    public static final TypeReference<Number> TYPE_REFERENCE_NUMBER = new TypeReference<Number>() {
        // Empty on purpose.
    };
    public static final TypeReference<Map<String, Object>> TYPE_REFERENCE_MAP = new TypeReference<Map<String, Object>>() {
        // Empty on purpose.
    };
    public static final TypeReference<TimeInterval> TYPE_REFERENCE_TIME_INTERVAL = new TypeReference<TimeInterval>() {
        // Empty on purpose.
    };
    public static final TypeReference<GeoJsonObject> TYPE_REFERENCE_GEOJSONOBJECT = new TypeReference<GeoJsonObject>() {
        // Empty on purpose.
    };
    public static final TypeReference<UnitOfMeasurement> TYPE_REFERENCE_UOM = new TypeReference<UnitOfMeasurement>() {
        // Empty on purpose.
    };
    public static final TypeReference<List<UnitOfMeasurement>> TYPE_REFERENCE_LIST_UOM = new TypeReference<List<UnitOfMeasurement>>() {
        // Empty on purpose.
    };
    public static final TypeReference<List<String>> TYPE_REFERENCE_LIST_STRING = new TypeReference<List<String>>() {
        // Empty on purpose.
    };
    public static final TypeReference<TimeInstant> TYPE_REFERENCE_TIMEINSTANT = new TypeReference<TimeInstant>() {
        // Empty on purpose.
    };
    public static final TypeReference<TimeInterval> TYPE_REFERENCE_TIMEINTERVAL = new TypeReference<TimeInterval>() {
        // Empty on purpose.
    };
    public static final TypeReference<TimeValue> TYPE_REFERENCE_TIMEVALUE = new TypeReference<TimeValue>() {
        // Empty on purpose.
    };

    private TypeReferencesHelper() {
        // Utility class
    }

}
