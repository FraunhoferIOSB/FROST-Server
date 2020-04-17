/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.VisibilityHelper;
import java.io.IOException;
import java.util.Map;
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

    private final VisibilityHelper visibilityHelper;

    public ResultFormatterDefault(CoreSettings settings) {
        this.visibilityHelper = new VisibilityHelper(settings);
        LOGGER.debug("Creating a new resultFormatter.");
    }

    @Override
    public String format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        String entityJsonString = "";
        try {
            if (Entity.class.isAssignableFrom(result.getClass())) {
                Entity entity = (Entity) result;
                visibilityHelper.applyVisibility(entity, path, query, useAbsoluteNavigationLinks);
                entityJsonString = EntityFormatter.writeEntity(entity);

            } else if (EntitySet.class.isAssignableFrom(result.getClass())) {
                EntitySet entitySet = (EntitySet) result;
                visibilityHelper.applyVisibility(entitySet, path, query, useAbsoluteNavigationLinks);
                entityJsonString = EntityFormatter.writeEntityCollection(entitySet);

            } else if (path != null && path.isValue()) {
                if (result instanceof Map || result instanceof GeoJsonObject) {
                    entityJsonString = EntityFormatter.writeObject(result);
                } else if (result instanceof Id) {
                    entityJsonString = ((Id) result).getValue().toString();
                } else {
                    entityJsonString = result.toString();
                }
            } else {
                entityJsonString = EntityFormatter.writeObject(result);
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to format response.", ex);
        }
        return entityJsonString;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

}
