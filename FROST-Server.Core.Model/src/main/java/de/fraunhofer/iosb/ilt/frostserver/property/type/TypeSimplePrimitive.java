/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.property.type;

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class TypeSimplePrimitive extends TypeSimple {

    public static final TypeSimplePrimitive EDM_BINARY = new TypeSimplePrimitive("Edm.Binary", "Binary data", TypeReferencesHelper.TYPE_REFERENCE_STRING);
    public static final TypeSimplePrimitive EDM_BOOLEAN = new TypeSimplePrimitive("Edm.Boolean", "Binary-valued logic", TypeReferencesHelper.TYPE_REFERENCE_BOOLEAN);
    public static final TypeSimplePrimitive EDM_BYTE = new TypeSimplePrimitive("Edm.Byte", "Unsigned 8-bit integer", TypeReferencesHelper.TYPE_REFERENCE_INTEGER);
    public static final TypeSimplePrimitive EDM_DATE = new TypeSimplePrimitive("Edm.Date", "Date without a time-zone offset", TypeReferencesHelper.TYPE_REFERENCE_DATE);
    public static final TypeSimplePrimitive EDM_DATETIMEOFFSET = new TypeSimplePrimitive("Edm.DateTimeOffset", "Date and time with a time-zone offset, no leap seconds", TypeReferencesHelper.TYPE_REFERENCE_TIMEINSTANT);
    public static final TypeSimplePrimitive EDM_DECIMAL = new TypeSimplePrimitive("Edm.Decimal", "Numeric values with decimal representation", TypeReferencesHelper.TYPE_REFERENCE_BIGDECIMAL);
    public static final TypeSimplePrimitive EDM_DOUBLE = new TypeSimplePrimitive("Edm.Double", "IEEE 754 binary64 floating-point number (15-17 decimal digits)", TypeReferencesHelper.TYPE_REFERENCE_BIGDECIMAL);
    public static final TypeSimplePrimitive EDM_DURATION = new TypeSimplePrimitive("Edm.Duration", "Signed duration in days, hours, minutes, and (sub)seconds", TypeReferencesHelper.TYPE_REFERENCE_DURATION);
    public static final TypeSimplePrimitive EDM_GUID = new TypeSimplePrimitive("Edm.Guid", "16-byte (128-bit) unique identifier", TypeReferencesHelper.TYPE_REFERENCE_UUID, SimpleParserUtils.PARSER_UUID);
    public static final TypeSimplePrimitive EDM_INT16 = new TypeSimplePrimitive("Edm.Int16", "Signed 16-bit integer", TypeReferencesHelper.TYPE_REFERENCE_INTEGER, SimpleParserUtils.PARSER_LONG);
    public static final TypeSimplePrimitive EDM_INT32 = new TypeSimplePrimitive("Edm.Int32", "Signed 32-bit integer", TypeReferencesHelper.TYPE_REFERENCE_INTEGER, SimpleParserUtils.PARSER_LONG);
    public static final TypeSimplePrimitive EDM_INT64 = new TypeSimplePrimitive("Edm.Int64", "Signed 64-bit integer", TypeReferencesHelper.TYPE_REFERENCE_LONG, SimpleParserUtils.PARSER_LONG);
    public static final TypeSimplePrimitive EDM_SBYTE = new TypeSimplePrimitive("Edm.SByte", "Signed 8-bit integer", TypeReferencesHelper.TYPE_REFERENCE_INTEGER, SimpleParserUtils.PARSER_LONG);
    public static final TypeSimplePrimitive EDM_SINGLE = new TypeSimplePrimitive("Edm.Single", "IEEE 754 binary32 floating-point number (6-9 decimal digits)", TypeReferencesHelper.TYPE_REFERENCE_BIGDECIMAL);
    public static final TypeSimplePrimitive EDM_STREAM = new TypeSimplePrimitive("Edm.Stream", "Binary data stream", TypeReferencesHelper.TYPE_REFERENCE_STRING);
    public static final TypeSimplePrimitive EDM_STRING = new TypeSimplePrimitive("Edm.String", "Sequence of characters", TypeReferencesHelper.TYPE_REFERENCE_STRING, SimpleParserUtils.PARSER_STRING);
    public static final TypeSimplePrimitive EDM_TIMEOFDAY = new TypeSimplePrimitive("Edm.TimeOfDay", "Clock time 00:00-23:59:59.999999999999", TypeReferencesHelper.TYPE_REFERENCE_DATE);
    public static final TypeSimplePrimitive EDM_GEOGRAPHY = new TypeSimplePrimitive("Edm.Geography", "Abstract base type for all Geography types", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOGRAPHYPOINT = new TypeSimplePrimitive("Edm.GeographyPoint", "A point in a round-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOGRAPHYLINESTRING = new TypeSimplePrimitive("Edm.GeographyLineString", "Line string in a round-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOGRAPHYPOLYGON = new TypeSimplePrimitive("Edm.GeographyPolygon", "Polygon in a round-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOGRAPHYMULTIPOINT = new TypeSimplePrimitive("Edm.GeographyMultiPoint", "Collection of points in a round-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOGRAPHYMULTILINESTRING = new TypeSimplePrimitive("Edm.GeographyMultiLineString", "Collection of line strings in a round-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOGRAPHYMULTIPOLYGON = new TypeSimplePrimitive("Edm.GeographyMultiPolygon", "Collection of polygons in a round-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOGRAPHYCOLLECTION = new TypeSimplePrimitive("Edm.GeographyCollection", "Collection of arbitrary Geography values", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOMETRY = new TypeSimplePrimitive("Edm.Geometry", "Abstract base type for all Geometry types", TypeReferencesHelper.TYPE_REFERENCE_GEOJSONOBJECT);
    public static final TypeSimplePrimitive EDM_GEOMETRYPOINT = new TypeSimplePrimitive("Edm.GeometryPoint", "Point in a flat-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOMETRYLINESTRING = new TypeSimplePrimitive("Edm.GeometryLineString", "Line string in a flat-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOMETRYPOLYGON = new TypeSimplePrimitive("Edm.GeometryPolygon", "Polygon in a flat-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOMETRYMULTIPOINT = new TypeSimplePrimitive("Edm.GeometryMultiPoint", "Collection of points in a flat-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOMETRYMULTILINESTRING = new TypeSimplePrimitive("Edm.GeometryMultiLineString", "Collection of line strings in a flat-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOMETRYMULTIPOLYGON = new TypeSimplePrimitive("Edm.GeometryMultiPolygon", "Collection of polygons in a flat-earth coordinate system", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_GEOMETRYCOLLECTION = new TypeSimplePrimitive("Edm.GeometryCollection", "Collection of arbitrary Geometry values", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);
    public static final TypeSimplePrimitive EDM_UNTYPED = new TypeSimplePrimitive("Edm.Untyped", "Can be any valid JSON.", TypeReferencesHelper.TYPE_REFERENCE_OBJECT);

    public static final TypeSimplePrimitive STA_ID_LONG = EDM_INT64;
    public static final TypeSimplePrimitive STA_ID_STRING = EDM_STRING;
    public static final TypeSimplePrimitive STA_ID_UUID = EDM_GUID;

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeSimplePrimitive.class.getName());
    private static final Map<String, TypeSimplePrimitive> TYPES = new HashMap<>();

    static {
        TYPES.put(Constants.VALUE_ID_TYPE_LONG, STA_ID_LONG);
        TYPES.put(Constants.VALUE_ID_TYPE_STRING, STA_ID_STRING);
        TYPES.put(Constants.VALUE_ID_TYPE_UUID, STA_ID_UUID);
        TYPES.put("TimeInstant", EDM_DATETIMEOFFSET);
        for (Field field : FieldUtils.getAllFields(TypeSimplePrimitive.class)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                final TypeSimplePrimitive primitive = (TypeSimplePrimitive) FieldUtils.readStaticField(field, false);
                final String name = primitive.getName();
                TYPES.put(name, primitive);
                LOGGER.debug("Registered type: {}", name);
            } catch (IllegalArgumentException ex) {
                LOGGER.error("Failed to initialise: {}", field, ex);
            } catch (IllegalAccessException ex) {
                LOGGER.trace("Failed to initialise: {}", field, ex);
            } catch (ClassCastException ex) {
                // It's not a TypeSimplePrimitive
            }
        }
    }

    public static TypeSimplePrimitive getType(String name) {
        return TYPES.get(name);
    }

    private TypeSimplePrimitive(String name, String description, TypeReference typeReference) {
        super(name, description, typeReference);
    }

    private TypeSimplePrimitive(String name, String description, TypeReference typeReference, Parser parser) {
        super(name, description, typeReference, parser);
    }

}
