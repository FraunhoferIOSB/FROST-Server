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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

import de.fraunhofer.iosb.ilt.frostserver.util.WktParser;
import java.util.Objects;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.Point;
import org.geojson.Polygon;

/**
 *
 * @author jab
 * @param <T> The type of GeoJSON object this constant wraps.
 */
public abstract class GeoJsonConstant<T extends GeoJsonObject> extends Constant<T> {

    /**
     * The WKT that generated this geometry.
     */
    private String source;

    public static GeoJsonConstant<? extends GeoJsonObject> fromString(String value) {

        GeoJsonObject geoJsonObject = WktParser.parseWkt(value);
        if (geoJsonObject instanceof Point point) {
            return new PointConstant(point, value);
        }
        if (geoJsonObject instanceof LineString lineString) {
            return new LineStringConstant(lineString, value);
        }
        if (geoJsonObject instanceof Polygon polygon) {
            return new PolygonConstant(polygon, value);
        }
        throw new IllegalArgumentException("unknown WKT string format '" + value + "'");
    }

    protected GeoJsonConstant(T value) {
        this(value, null);
    }

    protected GeoJsonConstant(T value, String source) {
        super(value);
        this.source = source;
    }

    /**
     * The WKT that generated this geometry.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * The WKT that generated this geometry.
     *
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toUrl() {
        return "geography'" + source + "'";
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeoJsonConstant<?> other = (GeoJsonConstant<?>) obj;
        return Objects.equals(this.source, other.source);
    }

}
