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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.csv;

import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        if (pathItem.isSet) {
            Query subQuery = expand.getSubQuery();
            if (subQuery == null || subQuery.getTopOrDefault() != 1) {
                throw new IncorrectRequestException("The CSV ResultFormat only allows expand on sets with $top=1.");
            }
        }
        validateQuery(expand.getSubQuery());
    }

    @Override
    public String format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        EntityType type = path.getMainElementType();
        CsvElementSet elementSet = new CsvElementSet("");
        elementSet.initFrom(type, query);

        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.RFC4180)) {
            elementSet.writeHeader(printer);
            printer.println();
            elementSet.writeData(printer, result);
        } catch (IOException ex) {
            LOGGER.error("Failed to generate CSV String.", ex);
        }
        return writer.toString();
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    private class CsvElementSet {

        private final String namePrefix;
        private final List<CsvEntityEntry> elements = new ArrayList<>();

        public CsvElementSet(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        public void initFrom(EntityType type, Query query) {
            if (query == null || query.getSelect().isEmpty()) {
                initFrom(type, type.getPropertySet(), query);
            } else {
                initFrom(type, query.getSelect(), query);
            }
        }

        public void initFrom(EntityType type, Set<Property> properties, Query query) {
            for (Property property : properties) {
                if (property == EntityProperty.SELFLINK) {
                    continue;
                }
                if (property instanceof EntityProperty) {
                    initFrom(type, (EntityProperty) property);
                }
            }
            if (query == null) {
                return;
            }
            for (Expand expand : query.getExpand()) {
                NavigationProperty path = expand.getPath();
                initFrom(type, path, expand.getSubQuery());
            }
        }

        public void initFrom(EntityType type, EntityProperty property) {
            try {
                final String getterName = property.getGetterName();
                final Class<? extends Entity> implementingClass = type.getImplementingClass();
                final Method getter = implementingClass.getMethod(getterName);
                CsvEntityEntry element = new CsvEntityProperty(namePrefix + property.entitiyName, (CSVPrinter printer, Entity<?> e) -> {
                    try {
                        Object result = getter.invoke(e);
                        printer.print(result);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        LOGGER.error("Failed to read element", ex);
                    }
                });
                elements.add(element);
            } catch (NoSuchMethodException | SecurityException ex) {
                LOGGER.error("Failed to read element", ex);
            }
        }

        public void initFrom(EntityType type, NavigationProperty property, Query query) {
            try {
                String getterName = property.getGetterName();
                final Class<? extends Entity> implementingClass = type.getImplementingClass();
                final Method getter = implementingClass.getMethod(getterName);
                CsvEntityExpand element = new CsvEntityExpand(
                        namePrefix + property.getName() + "/",
                        property,
                        query,
                        (Entity<?> source) -> {
                            try {
                                Object result = getter.invoke(source);
                                if (result instanceof Entity) {
                                    return (Entity) result;
                                }
                                if (result instanceof EntitySet) {
                                    EntitySet entitySet = (EntitySet<? extends Entity>) result;
                                    List<? extends Entity> asList = entitySet.asList();
                                    return asList.isEmpty() ? null : asList.get(0);
                                }
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                LOGGER.error("Failed to read element", ex);
                            }
                            return null;
                        });
                elements.add(element);
            } catch (NoSuchMethodException | SecurityException ex) {
                LOGGER.error("Failed to read element", ex);
            }
        }

        public void writeHeader(CSVPrinter printer) throws IOException {
            for (CsvEntityEntry element : elements) {
                element.writeHeader(printer);
            }
        }

        public void writeData(CSVPrinter printer, Object obj) throws IOException {
            if (obj instanceof Entity) {
                writeData(printer, (Entity) obj);
            } else if (obj instanceof EntitySet) {
                writeData(printer, (EntitySet) obj);
            }
        }

        public void writeData(CSVPrinter printer, Entity<?> e) throws IOException {
            if (e == null) {
                return;
            }
            for (CsvEntityEntry element : elements) {
                element.writeData(printer, e);
            }
        }

        public void writeData(CSVPrinter printer, EntitySet<?> es) throws IOException {
            if (es == null) {
                return;
            }
            List<? extends Entity> list = es.asList();
            for (Entity e : list) {
                for (CsvEntityEntry element : elements) {
                    element.writeData(printer, e);
                }
                printer.println();
            }
        }
    }

    private class CsvEntityProperty implements CsvEntityEntry {

        private final String headerName;
        private final CsvElementFetcher fetcher;

        public CsvEntityProperty(String headerName, CsvElementFetcher fetcher) {
            this.headerName = headerName;
            this.fetcher = fetcher;
        }

        @Override
        public void writeHeader(CSVPrinter printer) throws IOException {
            printer.print(headerName);
        }

        @Override
        public void writeData(CSVPrinter printer, Entity<?> e) throws IOException {
            fetcher.fetch(printer, e);
        }
    }

    private class CsvEntityExpand implements CsvEntityEntry {

        private final NavigationPropertyFollower propertyFollower;
        private final CsvElementSet expandedElements;

        public CsvEntityExpand(String namePrefix, NavigationProperty property, Query subQuery, NavigationPropertyFollower propertyFollower) {
            this.propertyFollower = propertyFollower;
            this.expandedElements = new CsvElementSet(namePrefix);
            expandedElements.initFrom(property.getType(), subQuery);
        }

        @Override
        public void writeHeader(CSVPrinter printer) throws IOException {
            expandedElements.writeHeader(printer);
        }

        @Override
        public void writeData(CSVPrinter printer, Entity<?> e) throws IOException {
            expandedElements.writeData(printer, propertyFollower.fetch(e));
        }

    }

    private interface CsvEntityEntry {

        public void writeHeader(CSVPrinter printer) throws IOException;

        public void writeData(CSVPrinter printer, Entity<?> e) throws IOException;
    }

    private interface CsvElementFetcher {

        public void fetch(CSVPrinter printer, Entity<?> e) throws IOException;
    }

    private interface NavigationPropertyFollower {

        public Entity<?> fetch(Entity<?> source) throws IOException;
    }

}
