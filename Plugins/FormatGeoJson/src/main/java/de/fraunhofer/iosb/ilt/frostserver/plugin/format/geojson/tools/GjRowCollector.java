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

import java.util.Map;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
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

    private final FeatureCollection collection;
    private Feature feature;

    /**
     * Create a new Collector.
     *
     * @param collection The FeatureCollection to write the new Feature to.
     */
    public GjRowCollector(FeatureCollection collection) {
        this.collection = collection;
        feature = new Feature();
    }

    /**
     * Collect a new value for the element with the given index.
     *
     * @param headerName The name of the element.
     * @param value The value of the element for the current row.
     */
    public void collectEntry(String headerName, Object value) {
        if (value instanceof Feature) {
            Feature featureValue = (Feature) value;
            feature.setGeometry(featureValue.getGeometry());
            feature.getProperties().putAll(featureValue.getProperties());
        } else if (value instanceof GeoJsonObject) {
            feature.setGeometry((GeoJsonObject) value);
        } else if (value instanceof Map) {
            feature.getProperties().putAll((Map) value);
        } else {
            feature.setProperty(headerName, value);
        }
    }

    /**
     * Flush the Feature to the FeatureCollection, and reset the collector.
     *
     */
    public void flush() {
        collection.add(feature);
        feature = new Feature();
    }

}
