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
package de.fraunhofer.iosb.ilt.frostserver.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;

/**
 *
 * @author jab
 */
public class GeoHelper {

    private static final String NUMBER_REGEX = "[+-]?[0-9]*(\\.[0-9]+)?";
    private static final String POINT_2D_REGEX = NUMBER_REGEX + " " + NUMBER_REGEX;
    private static final String POINT_3D_REGEX = POINT_2D_REGEX + " " + NUMBER_REGEX;
    private static final String LIST_POINT_2D_REGEX = POINT_2D_REGEX + "(?:," + POINT_2D_REGEX + ")*";
    private static final String LIST_POINT_3D_REGEX = POINT_3D_REGEX + "(?:," + POINT_3D_REGEX + ")*";
    private static final String LIST_LIST_POINT_2D_REGEX = "[(]" + LIST_POINT_2D_REGEX + "[)](?:,[(]" + LIST_POINT_2D_REGEX + "[)])*";
    private static final String LIST_LIST_POINT_3D_REGEX = "[(]" + LIST_POINT_3D_REGEX + "[)](?:,[(]" + LIST_POINT_3D_REGEX + "[)])*";

    private static final String WKT_POINT_REGEX = "POINT[(](" + POINT_2D_REGEX + "|" + POINT_3D_REGEX + ")[)]";
    private static final String WKT_LINE_REGEX = "LINESTRING[(](" + LIST_POINT_2D_REGEX + "|" + LIST_POINT_3D_REGEX + ")[)]";
    private static final String WKT_POLYGON_REGEX = "POLYGON( Z)?[(](" + LIST_LIST_POINT_2D_REGEX + "|" + LIST_LIST_POINT_3D_REGEX + ")[)]";

    public static final Pattern WKT_POINT_PATTERN = Pattern.compile(WKT_POINT_REGEX, Pattern.CASE_INSENSITIVE);
    public static final Pattern WKT_LINE_PATTERN = Pattern.compile(WKT_LINE_REGEX, Pattern.CASE_INSENSITIVE);
    public static final Pattern WKT_POLYGON_PATTERN = Pattern.compile(WKT_POLYGON_REGEX, Pattern.CASE_INSENSITIVE);

    private static final String DOES_NOT_MATCH_PATTERN = "' does not match pattern '";

    private GeoHelper() {

    }

    public static Point parsePoint(String value) {
        // Replace all whitespace-chains with a single space.
        value = cleanInput(value);
        Matcher matcher = GeoHelper.WKT_POINT_PATTERN.matcher(value);
        if (matcher.matches()) {
            String[] coordinates = matcher.group(1).split(" ");
            if (coordinates.length < 2 || coordinates.length > 3) {
                throw new IllegalArgumentException("only 2d or 3d points are supported");
            }
            if (coordinates.length == 2) {
                return new Point(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
            } else {
                return new Point(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]), Double.parseDouble(coordinates[2]));
            }
        } else {
            throw new IllegalArgumentException("'" + value + DOES_NOT_MATCH_PATTERN + GeoHelper.WKT_POINT_PATTERN.pattern() + "'");
        }
    }

    public static LineString parseLine(String value) {
        value = cleanInput(value);
        Matcher matcher = GeoHelper.WKT_LINE_PATTERN.matcher(value);
        if (matcher.matches()) {
            String[] points = matcher.group(1).split(" ?, ?");
            return new LineString(
                    Arrays.asList(points).stream()
                            .map(x -> Arrays.asList(x.split(" "))) //split each point in coorinates array
                            .map(x -> x.stream().map(Double::parseDouble)) // parse each coordinate to double
                            .map(x -> getPoint(x.toArray(size -> new Double[size])).getCoordinates()) //collect double coordinate into double[] and convert to Point
                            .toArray(size -> new LngLatAlt[size]));
        } else {
            throw new IllegalArgumentException("'" + value + DOES_NOT_MATCH_PATTERN + GeoHelper.WKT_LINE_PATTERN.pattern() + "'");
        }
    }

    private static String cleanInput(String value) {
        // Replace all whitespace-chains with a single space.
        // Remove spaces before and after ,()
        return value.trim()
                .replaceAll("\\s+", " ")
                .replaceAll(" ?([,()]) ?", "$1");
    }

    private static LngLatAlt[] stringListToPoints(String value) {
        return Arrays.asList(value.split(" ?, ?")).stream()
                .map(x -> Arrays.asList(x.split(" "))) //split each point in coorinates array
                .map(x -> x.stream().map(Double::parseDouble)) // parse each coordinate to double
                .map(x -> getPoint(x.toArray(size -> new Double[size])).getCoordinates()) //collect double coordinate into double[] and convert to Point
                .toArray(size -> new LngLatAlt[size]);
    }

    public static Polygon parsePolygon(String value) {
        // Replace all whitespace-chains with a single space.
        value = cleanInput(value);
        Matcher matcher = GeoHelper.WKT_POLYGON_PATTERN.matcher(value);
        if (matcher.matches()) {
            // definition of GeoJson Polygon:
            // First parameter is exterior ring, all others are interior rings
            String group = matcher.group(2);
            String[] rings = group.trim().substring(1, group.length() - 1).split("[)] ?, ?[(]");
            Polygon result = new Polygon(stringListToPoints(rings[0]));
            for (int i = 1; i < rings.length; i++) {
                // add interior rings
                result.addInteriorRing(stringListToPoints(rings[i]));
            }
            return result;
        } else {
            throw new IllegalArgumentException("'" + value + DOES_NOT_MATCH_PATTERN + GeoHelper.WKT_POLYGON_PATTERN.pattern() + "'");
        }
    }

    public static GeoJsonObject parseWkt(String value) {
        try {
            return parsePoint(value);
        } catch (IllegalArgumentException e1) {
            // Not a Point
        }
        try {
            return parseLine(value);
        } catch (IllegalArgumentException e2) {
            // Not a LineString
        }
        try {
            return parsePolygon(value);
        } catch (IllegalArgumentException e3) {
            // Not a Polygon
        }
        throw new IllegalArgumentException("unknown WKT string format '" + value + "'");
    }

    public static <T extends Number> Point getPoint(T... values) {
        if (values == null || values.length < 2 || values.length > 3) {
            throw new IllegalArgumentException("values must have a length of 2 or 3.");
        }
        if (values.length == 2) {
            return new Point(values[0].doubleValue(), values[1].doubleValue());
        }
        return new Point(values[0].doubleValue(), values[1].doubleValue(), values[2].doubleValue());
    }

    public static GeoJsonObject parseGeoJson(String geoJsonString) throws IOException {
        return SimpleJsonMapper.getSimpleObjectMapper().readValue(geoJsonString, GeoJsonObject.class);
    }
}
