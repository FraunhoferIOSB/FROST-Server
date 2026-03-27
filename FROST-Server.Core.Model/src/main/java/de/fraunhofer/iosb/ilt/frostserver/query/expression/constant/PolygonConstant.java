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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.WktParser;
import org.geojson.GeoJsonObject;
import org.geojson.Polygon;

/**
 * A constant geojson polygon.
 */
public class PolygonConstant extends GeoJsonConstant<PolygonConstant, Polygon> {

    public static final String EXPR_NAME_POLYGONCONSTANT = "polygonconstant";

    public PolygonConstant() {
        super(EXPR_NAME_POLYGONCONSTANT, null);
    }

    public PolygonConstant(Polygon value, String source) {
        super(EXPR_NAME_POLYGONCONSTANT, value, source);
    }

    public static PolygonConstant parse(String value) {
        GeoJsonObject result = WktParser.parseWkt(value);
        if (result instanceof Polygon polygon) {
            return new PolygonConstant(polygon, value);
        }
        throw new IllegalArgumentException("Can not parse Polygon from: " + StringHelper.cleanForLogging(value));
    }
}
