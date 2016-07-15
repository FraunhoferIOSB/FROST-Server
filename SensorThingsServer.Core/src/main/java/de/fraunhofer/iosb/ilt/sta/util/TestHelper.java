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
package de.fraunhofer.iosb.ilt.sta.util;

import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Helper class for testing JSON de-/serialization.
 *
 * @author jab
 */
public class TestHelper {

    public static <T extends Number> Polygon getPolygon(int dimensions, T... values) {
        assert (values != null);
        assert (dimensions == 2 || dimensions == 3);
        assert (values.length % dimensions == 0);
        List<LngLatAlt> points = new ArrayList<>(values.length / dimensions);
        for (int i = 0; i < values.length; i += dimensions) {
            if (dimensions == 2) {
                points.add(new LngLatAlt(values[i].doubleValue(), values[i + 1].doubleValue()));
            } else {
                points.add(new LngLatAlt(values[i].doubleValue(), values[i + 1].doubleValue(), values[i + 2].doubleValue()));
            }
        }
        return new Polygon(points);
    }

    public static <T extends Number> Point getPoint(T... values) {
        assert (values != null);
        assert (values.length == 2 || values.length == 3);
        if (values.length == 2) {
            return new Point(values[0].doubleValue(), values[1].doubleValue());
        }
        return new Point(values[0].doubleValue(), values[1].doubleValue(), values[2].doubleValue());
    }

    public static <T extends Number> LineString getLine(T[]... values) {
        assert (values != null);
        assert (values[0].length == 2 || values[0].length == 3);
        return new LineString(Arrays.asList(values).stream().map(x -> getPoint(x).getCoordinates()).toArray(size -> new LngLatAlt[size]));
    }

    public static <T extends Number> Feature getFeatureWithPoint(T... values) {
        return getFeatureWithGeometry(getPoint(values));
    }

    public static Feature getFeatureWithGeometry(GeoJsonObject geometry) {
        assert (geometry != null);
        Feature result = new Feature();
        result.setGeometry(geometry);
        return result;
    }

    public static TimeInstant createTimeInstant(int year, int month, int day, int hour, int minute, int second, DateTimeZone timeZoneInput, DateTimeZone timeZoneOutput) {
        return TimeInstant.create(new DateTime(year, month, day, hour, minute, second, timeZoneInput).getMillis(), timeZoneOutput);
    }

    public static TimeInstant createTimeInstantUTC(int year, int month, int day, int hour, int minute, int second) {
        return createTimeInstant(year, month, day, hour, minute, second, DateTimeZone.UTC, DateTimeZone.UTC);
    }

    public static TimeInstant createTimeInstant(int year, int month, int day, int hour, int minute, int second) {
        return TimeInstant.create(new DateTime(year, month, day, hour, minute, second).getMillis());
    }

    public static TimeInterval createTimeInterval(int year1, int month1, int day1, int hour1, int minute1, int second1,
            int year2, int month2, int day2, int hour2, int minute2, int second2, DateTimeZone timeZone) {
        return TimeInterval.create(
                new DateTime(year1, month1, day1, hour1, minute1, second1, timeZone).getMillis(),
                new DateTime(year2, month2, day2, hour2, minute2, second2, timeZone).getMillis(),
                timeZone);
    }

}
