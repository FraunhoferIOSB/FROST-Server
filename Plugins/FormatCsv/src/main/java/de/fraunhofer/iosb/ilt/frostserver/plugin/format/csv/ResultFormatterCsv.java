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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv;

import de.fraunhofer.iosb.ilt.frostserver.formatter.FormatWriter;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.tools.CsvElementSet;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv.tools.CsvRowCollector;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class ResultFormatterCsv implements ResultFormatter {

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ResultFormatterCsv.class);

    @Override
    public void preProcessRequest(ResourcePath path, Query query) throws IncorrectRequestException {
        validateQuery(query);
    }

    private void validateQuery(Query query) throws IncorrectRequestException {
        if (query == null) {
            return;
        }
        for (Expand expand : query.getExpand()) {
            validateExpand(expand);
        }
    }

    private void validateExpand(Expand expand) throws IncorrectRequestException {
        NavigationProperty pathItem = expand.getPath();
        if (pathItem.isEntitySet()) {
            Query subQuery = expand.getSubQuery();
            if (subQuery == null || subQuery.getTopOrDefault() != 1) {
                throw new IncorrectRequestException("The CSV ResultFormat only allows expand on sets with $top=1.");
            }
        }
        validateQuery(expand.getSubQuery());
    }

    @Override
    public FormatWriter format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        EntityType type = path.getMainElementType();
        CsvElementSet elementSet = new CsvElementSet("");
        elementSet.initFrom(type, query);

        return (Writer writer) -> {
            try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.RFC4180)) {
                CsvRowCollector rowCollector = new CsvRowCollector(printer);
                elementSet.writeHeader(rowCollector);
                rowCollector.flush();
                elementSet.writeData(rowCollector, result);
            } catch (IOException ex) {
                LOGGER.error("Failed to generate CSV String.", ex);
            }
        };
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

}
