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
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.VisibilityHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class DefaultResultFormater implements ResultFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResultFormater.class);

    private VisibilityHelper visibilityHelper;

    public DefaultResultFormater(CoreSettings settings) {
        this.visibilityHelper = new VisibilityHelper(settings);
        LOGGER.debug("Creating a new resultFormatter.");
    }

    @Override
    public void preProcessRequest(ResourcePath path, Query query) {
        if ("dataarray".equalsIgnoreCase(query.getFormat()) && !query.getSelect().isEmpty()) {
            ResourcePathElement lastElement = path.getLastElement();
            if (lastElement instanceof EntitySetPathElement && ((EntitySetPathElement) lastElement).getEntityType() == EntityType.OBSERVATION) {
                query.getSelect().add(NavigationProperty.DATASTREAM);
                query.getSelect().add(NavigationProperty.MULTIDATASTREAM);
            }
        }
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
                if (query.getFormat() != null
                        && query.getFormat().equalsIgnoreCase(PluginDefaultResultFormat.DATA_ARRAY_FORMAT_NAME)
                        && entitySet.getEntityType() == EntityType.OBSERVATION) {
                    return formatDataArray(path, query, entitySet);
                }
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

    public static class VisibleComponents {

        public final boolean id;
        public final boolean phenomenonTime;
        public final boolean result;
        public final boolean resultTime;
        public final boolean resultQuality;
        public final boolean validTime;
        public final boolean parameters;

        public VisibleComponents() {
            this(false);
        }

        public VisibleComponents(boolean allValue) {
            id = allValue;
            phenomenonTime = allValue;
            result = allValue;
            resultTime = allValue;
            resultQuality = allValue;
            validTime = allValue;
            parameters = allValue;
        }

        public VisibleComponents(Set<Property> select) {
            id = select.contains(EntityProperty.ID);
            phenomenonTime = select.contains(EntityProperty.PHENOMENONTIME);
            result = select.contains(EntityProperty.RESULT);
            resultTime = select.contains(EntityProperty.RESULTTIME);
            resultQuality = select.contains(EntityProperty.RESULTQUALITY);
            validTime = select.contains(EntityProperty.VALIDTIME);
            parameters = select.contains(EntityProperty.PARAMETERS);
        }

        public List<String> getComponents() {
            List<String> components = new ArrayList<>();
            if (id) {
                components.add(EntityProperty.ID.entitiyName);
            }
            if (phenomenonTime) {
                components.add(EntityProperty.PHENOMENONTIME.entitiyName);
            }
            if (result) {
                components.add(EntityProperty.RESULT.entitiyName);
            }
            if (resultTime) {
                components.add(EntityProperty.RESULTTIME.entitiyName);
            }
            if (resultQuality) {
                components.add(EntityProperty.RESULTQUALITY.entitiyName);
            }
            if (validTime) {
                components.add(EntityProperty.VALIDTIME.entitiyName);
            }
            if (parameters) {
                components.add(EntityProperty.PARAMETERS.entitiyName);
            }
            return components;
        }

        public List<Object> fromObservation(Observation o) {
            List<Object> value = new ArrayList<>();
            if (id) {
                value.add(o.getId().getValue());
            }
            if (phenomenonTime) {
                value.add(o.getPhenomenonTime());
            }
            if (result) {
                value.add(o.getResult());
            }
            if (resultTime) {
                value.add(o.getResultTime());
            }
            if (resultQuality) {
                value.add(o.getResultQuality());
            }
            if (validTime) {
                value.add(o.getValidTime());
            }
            if (parameters) {
                value.add(o.getParameters());
            }
            return value;
        }
    }

    public String formatDataArray(ResourcePath path, Query query, EntitySet<Observation> entitySet) throws IOException {
        VisibleComponents visComps;
        if (query == null || query.getSelect().isEmpty()) {
            visComps = new VisibleComponents(true);
        } else {
            visComps = new VisibleComponents(query.getSelect());
        }
        List<String> components = visComps.getComponents();

        Map<String, DataArrayValue> dataArraySet = new LinkedHashMap<>();
        for (Observation obs : entitySet) {
            String dataArrayId = DataArrayValue.dataArrayIdFor(obs);
            DataArrayValue dataArray = dataArraySet.computeIfAbsent(
                    dataArrayId,
                    k -> new DataArrayValue(path, obs, components)
            );
            dataArray.getDataArray().add(visComps.fromObservation(obs));
        }

        DataArrayResult result = new DataArrayResult();
        List<DataArrayValue> values = result.getValue();
        for (DataArrayValue daValue : dataArraySet.values()) {
            values.add(daValue);
        }

        result.setCount(entitySet.getCount());
        result.setNextLink(entitySet.getNextLink());

        return EntityFormatter.writeObject(result);
    }

}
