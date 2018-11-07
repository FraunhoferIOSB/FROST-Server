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
package de.fraunhofer.iosb.ilt.sta.formatter;

import de.fraunhofer.iosb.ilt.sta.json.serialize.EntityFormatter;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.VisibilityHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jab
 */
public class DefaultResultFormater implements ResultFormatter {

    @Override
    public String format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        String entityJsonString = "";
        try {
            if (Entity.class.isAssignableFrom(result.getClass())) {

                Entity entity = (Entity) result;
                VisibilityHelper.applyVisibility(entity, path, query, useAbsoluteNavigationLinks);
                entityJsonString = EntityFormatter.writeEntity(entity);

            } else if (EntitySet.class.isAssignableFrom(result.getClass())) {
                EntitySet entitySet = (EntitySet) result;
                if (query.getFormat() != null && query.getFormat().equalsIgnoreCase("dataarray") && entitySet.getEntityType() == EntityType.OBSERVATION) {
                    return formatDataArray(path, query, entitySet);
                }
                VisibilityHelper.applyVisibility(entitySet, path, query, useAbsoluteNavigationLinks);
                entityJsonString = EntityFormatter.writeEntityCollection(entitySet);
            } else if (path != null && path.isValue()) {
                if (result instanceof Map) {
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
            Logger.getLogger(DefaultResultFormater.class.getName()).log(Level.SEVERE, null, ex);
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
