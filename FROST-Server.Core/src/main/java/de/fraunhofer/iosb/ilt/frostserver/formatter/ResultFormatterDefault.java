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
package de.fraunhofer.iosb.ilt.frostserver.formatter;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;

import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default resultFormatter, generating JSON.
 *
 * @author scf
 * @author jab
 */
public class ResultFormatterDefault implements ResultFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultFormatterDefault.class);

    public ResultFormatterDefault() {
        LOGGER.trace("Creating a new resultFormatter.");
    }

    @Override
    public FormatWriter format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        try {
            if (result instanceof Entity entity) {
                LOGGER.trace("Formatting as Entity.");
                return target -> JsonWriter.writeEntity(target, entity);
            }
            if (result instanceof EntitySet entitySet) {
                LOGGER.trace("Formatting as EntitySet.");
                return target -> JsonWriter.writeEntityCollection(target, entitySet, query);
            }
            // Not an Entity nor an EntitySet.
            String entityJsonString;
            if (path != null && path.isValue()) {
                LOGGER.trace("Formatting as $Value.");
                if (result instanceof Map || result instanceof GeoJsonObject) {
                    entityJsonString = JsonWriter.writeObject(result);
                } else if (result instanceof Id id) {
                    entityJsonString = id.getValue().toString();
                } else {
                    entityJsonString = Objects.toString(result);
                }
            } else {
                LOGGER.trace("Formatting as Object.");
                entityJsonString = JsonWriter.writeObject(result);
            }
            return new FormatWriterGeneric(entityJsonString);
        } catch (IOException ex) {
            LOGGER.error("Failed to format response.", ex);
        }
        return null;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE_APPLICATION_JSON;
    }

}
