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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_GEOJSON;

import de.fraunhofer.iosb.ilt.frostserver.formatter.FormatWriter;
import de.fraunhofer.iosb.ilt.frostserver.formatter.FormatWriterGeneric;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools.GjElementSet;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.geojson.tools.GjRowCollector;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import java.io.IOException;

/**
 *
 * @author scf
 */
public class ResultFormatterGeoJson implements ResultFormatter {

    @Override
    public void preProcessRequest(ResourcePath path, Query query) throws IncorrectRequestException {
        // Nothing to preProcess.
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE_APPLICATION_GEOJSON;
    }

    @Override
    public FormatWriter format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        EntityType type = path.getMainElementType();
        GjElementSet elementSet = new GjElementSet(query, path.getServiceRootUrl(), path.getVersion(), "", true);
        elementSet.initFrom(type);

        GjRowCollector rowCollector = new GjRowCollector();
        elementSet.writeData(rowCollector, result, "");

        try {
            return new FormatWriterGeneric(JsonWriter.writeObject(rowCollector.getCollection()));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate GeoJSON.", ex);
        }
    }

}
