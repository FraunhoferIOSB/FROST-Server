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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.tools;

import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVPrinter;
import org.geojson.GeoJsonObject;
import org.slf4j.LoggerFactory;

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
public class CsvRowCollector {

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CsvRowCollector.class);

    private final CSVPrinter printer;
    private final List<Object> elements = new ArrayList<>();
    private int countTotal = 0;
    private int pointer = 0;

    /**
     * Create a new Collector.
     *
     * @param printer The printer to write the collected elements to.
     */
    public CsvRowCollector(CSVPrinter printer) {
        this.printer = printer;
    }

    /**
     * Register a new element in the collector. The index of the new element is
     * returned. This index must be used when collecting the values of this
     * element.
     *
     * @param headerName The header name of the new element.
     * @return The index of the new element.
     */
    public int registerHeader(String headerName) {
        elements.add(headerName);
        countTotal = elements.size();
        pointer++;
        return countTotal - 1;
    }

    /**
     * Collect a new value for the element with the given index.
     *
     * @param idx The index of the element.
     * @param value The value of the element for the current row.
     */
    public void collectEntry(int idx, Object value) {
        if (idx < pointer) {
            throw new IllegalArgumentException("Elements must be collected in order.");
        }
        while (pointer < idx) {
            elements.set(pointer, null);
            pointer++;
        }
        if (value == null) {
            elements.set(idx, value);
        } else if (value instanceof Collection || value instanceof Map || value instanceof GeoJsonObject || value.getClass().isArray()) {
            try {
                String json = SimpleJsonMapper.getSimpleObjectMapper().writeValueAsString(value);
                elements.set(idx, json);
            } catch (IOException ex) {
                LOGGER.warn("Could not transform collection to JSON.", ex);
                elements.set(idx, value);
            }
        } else {
            elements.set(idx, value);
        }
        pointer++;
    }

    /**
     * Flush the row to the CSVPrinter, and reset the collector.
     *
     * @throws IOException If there is a problem with the printer.
     */
    public void flush() throws IOException {
        while (pointer < countTotal) {
            elements.set(pointer, null);
            pointer++;
        }
        printer.printRecord(elements);
        pointer = 0;
    }

}
