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
package de.fraunhofer.iosb.ilt.sta.query.expression.constant;

import java.util.Objects;
import org.geojson.GeoJsonObject;

/**
 *
 * @author jab
 * @param <T>
 */
public abstract class GeoJsonConstant<T extends GeoJsonObject> extends Constant<T> {

    /**
     * The WKT that generated this geometry.
     */
    private String source;

    public static GeoJsonConstant fromString(String value) {
        // brute force all implementations
        try {
            return new PointConstant(value);
        } catch (IllegalArgumentException e1) {
            try {
                return new LineStringConstant(value);
            } catch (IllegalArgumentException e2) {
                try {
                    return new PolygonConstant(value);
                } catch (IllegalArgumentException e3) {
                    throw new IllegalArgumentException("unknown WKT string format '" + value + "'");
                }
            }
        }
    }

    public GeoJsonConstant(T value) {
        super(value);
    }

    protected GeoJsonConstant(String value) {
        this.source = value;
        this.value = parse(value);
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

    protected abstract T parse(String value);

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.source);
        return hash;
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
