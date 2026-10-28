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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.jsonarray;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;

import de.fraunhofer.iosb.ilt.frostserver.formatter.FormatWriter;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formats DataArrays.
 */
public class ResultFormatterJsonArray implements ResultFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultFormatterJsonArray.class);
    private static final String ENTITY_ONLY = "ResultFormat=dataArray is only valid for Entity or EntitySet";

    private final PluginCoreModel pluginCoreModel;
    private NavigationPropertyEntity npMultiDatastream;

    public ResultFormatterJsonArray(CoreSettings settings) {
        pluginCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        LOGGER.debug("Creating a new ResultFormaterJsonArray.");
    }

    @Override
    public void preProcessRequest(ResourcePath path, Query query) throws IncorrectRequestException {
        if (npMultiDatastream == null) {
            npMultiDatastream = (NavigationPropertyEntity) pluginCoreModel.etObservation.getNavigationProperty("MultiDatastream");
        }
        if (!(path.getLastElement() instanceof PathElementEntitySet)
                || path.isRef()) {
            throw new IncorrectRequestException(ENTITY_ONLY);
        }
        if (!query.getSelect().isEmpty()) {
            PathElement lastElement = path.getLastElement();
            if (lastElement instanceof PathElementEntitySet pees && pees.getEntityType() == pluginCoreModel.etObservation) {
                query.getSelect().add(pluginCoreModel.npDatastreamObservation);
                if (npMultiDatastream != null) {
                    query.getSelect().add(npMultiDatastream);
                }
            }
        }
    }

    @Override
    public FormatWriter format(ResourcePath path, Query query, Object result) {
        if (EntitySet.class.isAssignableFrom(result.getClass())) {
            EntitySet entitySet = (EntitySet) result;
            return target -> JsonWriter.writeObject(target, JsonArrayResult.of(query, entitySet));
        }
        throw new IllegalArgumentException(ENTITY_ONLY);
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE_APPLICATION_JSON;
    }

}
