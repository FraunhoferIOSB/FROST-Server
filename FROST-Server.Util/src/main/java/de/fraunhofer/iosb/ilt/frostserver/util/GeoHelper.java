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
package de.fraunhofer.iosb.ilt.frostserver.util;

import com.fasterxml.jackson.core.TreeNode;
import java.io.IOException;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class GeoHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoHelper.class.getName());

    private GeoHelper() {
        // Not for instantiation.
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

    public static GeoJsonObject parseGeoJson(TreeNode tn) {
        try {
            return SimpleJsonMapper.getSimpleObjectMapper().treeToValue(tn, GeoJsonObject.class);
        } catch (IOException ex) {
            LOGGER.debug("Failed to convert to GeoJson: ", tn);
        }
        return null;
    }
}
