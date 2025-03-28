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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools;

import com.fasterxml.jackson.core.TreeNode;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimple;
import de.fraunhofer.iosb.ilt.frostserver.util.GeoHelper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Feature;

/**
 * Collects all elements for a single feature in a feature collection.
 */
public class GjRowCollector {

    private Feature feature;

    /**
     * Create a new Collector.
     */
    public GjRowCollector() {
        newFeature();
    }

    private void newFeature() {
        feature = new Feature();
        feature.setProperties(new LinkedHashMap<>());
    }

    /**
     * Collect the ID for the element.
     *
     * @param value The value of the id for the current row.
     */
    public void collectId(Object value) {
        feature.setId(String.valueOf(value));
    }

    /**
     * Collect a new value for the element.
     *
     * @param headerName The name of the element.
     * @param value The value of the element for the current row.
     * @param type The type of the property.
     */
    public void collectEntry(String headerName, Object value, PropertyType type) {
        if (value == null) {
            return;
        }
        if (value instanceof Feature featureValue) {
            feature.setGeometry(featureValue.getGeometry());
            feature.getProperties().putAll(featureValue.getProperties());
            return;
        }
        if (value instanceof TimeValue timeValue) {
            if (!timeValue.isEmpty()) {
                feature.setProperty(headerName, value);
            }
            return;
        }
        boolean isGeom = false;
        if (type instanceof TypeSimple ts) {
            if (ts.getUnderlyingType().getName().startsWith("Edm.Geo")) {
                isGeom = true;
            }
        }
        if (isGeom) {
            if (feature.getGeometry() == null && value instanceof TreeNode tn) {
                feature.setGeometry(GeoHelper.parseGeoJson(tn));
            }
            return;
        }
        if (value instanceof Map) {
            flattenMap((Map<String, Object>) value, headerName);
            return;
        }
        if (value instanceof List list) {
            flattenList(list, headerName);
            return;
        }
        if (value instanceof Entity) {
            // Entities are not written here.
            return;
        }
        feature.setProperty(headerName, value);
    }

    private void flattenMap(Map<String, Object> map, String headerName) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String header = headerName + "/" + key;
            Object value = entry.getValue();
            collectEntry(header, value, null);
        }
    }

    private void flattenList(List<Object> list, String headerName) {
        int idx = 0;
        for (Object item : list) {
            String header = headerName + "/" + idx;
            collectEntry(header, item, null);
            idx++;
        }
    }

    public Feature toFeature() {
        Feature result = feature;
        newFeature();
        return result;
    }
}
