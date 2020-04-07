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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson;

import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools.GjElementSet;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools.GjRowCollector;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import java.io.IOException;
import org.geojson.FeatureCollection;

/**
 *
 * @author scf
 */
public class ResultFormatterGeoJson implements ResultFormatter {

    @Override
    public void preProcessRequest(ResourcePath path, Query query) throws IncorrectRequestException {
        validateQuery(query);
    }

    private void validateExpand(Expand expand) throws IncorrectRequestException {
        NavigationProperty pathItem = expand.getPath();
        if (pathItem.isSet) {
            Query subQuery = expand.getSubQuery();
            if (subQuery == null || subQuery.getTopOrDefault() != 1) {
                throw new IncorrectRequestException("The GeoJSON ResultFormat only allows expand on sets with $top=1. Use GeoJSON-Deep instead.");
            }
        }
        validateQuery(expand.getSubQuery());
    }

    private void validateQuery(Query query) throws IncorrectRequestException {
        if (query == null) {
            return;
        }
        for (Expand expand : query.getExpand()) {
            validateExpand(expand);
        }
    }

    @Override
    public String getContentType() {
        return "application/geo+json";
    }

    @Override
    public String format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        EntityType type = path.getMainElementType();
        GjElementSet elementSet = new GjElementSet("");
        elementSet.initFrom(type, query);

        FeatureCollection collection = new FeatureCollection();
        GjRowCollector rowCollector = new GjRowCollector(collection);
        elementSet.writeData(rowCollector, result);

        try {
            return EntityFormatter.writeObject(collection);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate GeoJSON.", ex);
        }
    }

}
