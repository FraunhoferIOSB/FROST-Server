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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata;

import de.fraunhofer.iosb.ilt.frostserver.formatter.FormatWriter;
import de.fraunhofer.iosb.ilt.frostserver.formatter.FormatWriterGeneric;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatterDefault;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData.VERSION_ODATA_401;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize.EntitySetResultOdata;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize.EntityWrapper;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize.JsonWriterOdata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PluginResultFormatOData implements PluginResultFormat {

    /**
     * The "name" of the OData resultFormatter.
     */
    public static final String FORMAT_NAME_ODATA = "ODATA-JSON";

    private CoreSettings settings;

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        settings.getPluginManager().registerPlugin(this);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<Version> getVersions() {
        return Arrays.asList(VERSION_ODATA_401);
    }

    @Override
    public Collection<String> getFormatNames() {
        return Arrays.asList(PluginResultFormat.DEFAULT_FORMAT_NAME, FORMAT_NAME_ODATA);
    }

    @Override
    public ResultFormatter getResultFormatter() {
        return new ResultFormatterOData(settings);
    }

    public static class ResultFormatterOData implements ResultFormatter {

        private static final Logger LOGGER = LoggerFactory.getLogger(ResultFormatterDefault.class);

        public final CoreSettings settings;

        public ResultFormatterOData(CoreSettings settings) {
            LOGGER.trace("Creating a new resultFormatter.");
            this.settings = settings;
        }

        @Override
        public FormatWriter format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
            try {
                final String contextBase = settings.getQueryDefaults().getServiceRootUrl()
                        + '/' + path.getVersion().urlPart
                        + "/$metadata";
                if (Entity.class.isAssignableFrom(result.getClass())) {
                    LOGGER.trace("Formatting as Entity.");
                    final Entity entity = (Entity) result;
                    final EntityWrapper wrappedEntity = new EntityWrapper()
                            .setEntity(entity)
                            .setContext(contextBase + '#' + entity.getEntityType().plural + "/$entity");
                    return target -> JsonWriterOdata.writeEntity(target, wrappedEntity);
                }
                if (EntitySet.class.isAssignableFrom(result.getClass())) {
                    LOGGER.trace("Formatting as EntitySet.");
                    EntitySet entitySet = (EntitySet) result;
                    EntitySetResultOdata wrappedSet = new EntitySetResultOdata(entitySet)
                            .setContext(contextBase + '#' + entitySet.getEntityType().plural);
                    return target -> JsonWriterOdata.writeEntityCollection(target, wrappedSet);
                }
                // Not an Entity nor an EntitySet.
                String entityJsonString = "";
                if (path != null && path.isValue()) {
                    LOGGER.trace("Formatting as $Value.");
                    if (result instanceof Map || result instanceof GeoJsonObject) {
                        entityJsonString = JsonWriterOdata.writeObject(result);
                    } else if (result instanceof Id) {
                        entityJsonString = ((Id) result).getValue().toString();
                    } else {
                        entityJsonString = result.toString();
                    }
                } else {
                    LOGGER.trace("Formatting as Object.");
                    entityJsonString = JsonWriterOdata.writeObject(result);
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
}
