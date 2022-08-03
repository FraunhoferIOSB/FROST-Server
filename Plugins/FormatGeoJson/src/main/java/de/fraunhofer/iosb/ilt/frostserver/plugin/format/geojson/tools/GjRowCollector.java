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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;

/**
 * Collects all elements for a single row in a CSV file.
 *
 * First, each element is registered by its header name. This returns the index
 * of the element. After this, for each row, each element is collected in order.
 * If one or more elements are skipped, the elements set to null. Finally, the
 * entire row is flushed to the CSVPrinter, and the collector is reset.
 *
 * @author scf
 */
public class GjRowCollector {

    private static class FeatureList extends ArrayList<GeoJsonObject> {
        // Nothing to override.
    };

    private final Map<String, Object> collection = new LinkedHashMap<>();
    private final FeatureList features = new FeatureList();
    private Feature feature;

    /**
     * Create a new Collector.
     */
    public GjRowCollector() {
        // Ensure type & nextLink are first, nicer for users
        collection.put("type", "FeatureCollection");
        collection.put(SpecialNames.AT_IOT_NEXT_LINK, null);
        collection.put(SpecialNames.AT_IOT_COUNT, null);
        collection.put("features", features);
        newFeature();
    }

    public Map<String, Object> getCollection() {
        return collection;
    }

    public void setNextLink(String nextLink) {
        collection.put(SpecialNames.AT_IOT_NEXT_LINK, nextLink);
    }

    public void setCount(Long count) {
        if (count >= 0) {
            collection.put(SpecialNames.AT_IOT_COUNT, count);
        }
    }

    private void newFeature() {
        feature = new Feature();
        feature.setProperties(new LinkedHashMap<>());
    }

    /**
     * Collect a new value for the element with the given index.
     *
     * @param headerName The name of the element.
     * @param value The value of the element for the current row.
     */
    public void collectEntry(String headerName, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Feature) {
            Feature featureValue = (Feature) value;
            feature.setGeometry(featureValue.getGeometry());
            feature.getProperties().putAll(featureValue.getProperties());
            return;
        }
        if (value instanceof TimeValue) {
            if (!((TimeValue) value).isEmpty()) {
                feature.setProperty(headerName, value);
            }
            return;
        }
        if (value instanceof GeoJsonObject) {
            if (feature.getGeometry() == null) {
                feature.setGeometry((GeoJsonObject) value);
            }
            return;
        }
        if (value instanceof Map) {
            flattenMap((Map<String, Object>) value, headerName);
            return;
        }
        if (value instanceof List) {
            flattenList((List<Object>) value, headerName);
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
            collectEntry(header, value);
        }
    }

    private void flattenList(List<Object> list, String headerName) {
        int idx = 0;
        for (Object item : list) {
            String header = headerName + "/" + idx;
            collectEntry(header, item);
            idx++;
        }
    }

    /**
     * Flush the Feature to the FeatureCollection, and reset the collector.
     *
     */
    public void flush() {
        features.add(feature);
        newFeature();
    }

}
