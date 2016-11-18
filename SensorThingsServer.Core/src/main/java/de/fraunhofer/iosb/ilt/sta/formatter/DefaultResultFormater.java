/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.formatter;

import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.id.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.serialize.EntityFormatter;
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

    public DefaultResultFormater() {
    }

    @Override
    public String format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        String entityJsonString = "";
        try {
            if (Entity.class.isAssignableFrom(result.getClass())) {

                Entity entity = (Entity) result;
                VisibilityHelper.applyVisibility(entity, path, query, useAbsoluteNavigationLinks);
                entityJsonString = new EntityFormatter().writeEntity(entity);

            } else if (EntitySet.class.isAssignableFrom(result.getClass())) {
                EntitySet entitySet = (EntitySet) result;
                if (query.getFormat() != null && query.getFormat().equalsIgnoreCase("dataarray") && entitySet.getEntityType() == EntityType.Observation) {
                    return formatDataArray(path, query, entitySet, useAbsoluteNavigationLinks);
                }
                VisibilityHelper.applyVisibility(entitySet, path, query, useAbsoluteNavigationLinks);
                entityJsonString = new EntityFormatter().writeEntityCollection(entitySet);
            } else if (path != null && path.isValue()) {
                if (result instanceof Map) {
                    entityJsonString = new EntityFormatter().writeObject(result);
                } else if (result instanceof Id) {
                    entityJsonString = ((Id) result).getValue().toString();
                } else {
                    entityJsonString = result.toString();
                }
            } else {
                entityJsonString = new EntityFormatter().writeObject(result);
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
            id = select.contains(EntityProperty.Id);
            phenomenonTime = select.contains(EntityProperty.PhenomenonTime);
            result = select.contains(EntityProperty.Result);
            resultTime = select.contains(EntityProperty.ResultTime);
            resultQuality = select.contains(EntityProperty.ResultQuality);
            validTime = select.contains(EntityProperty.ValidTime);
            parameters = select.contains(EntityProperty.Parameters);
        }

        public List<String> getComponents() {
            List<String> components = new ArrayList<>();
            if (id) {
                components.add(EntityProperty.Id.name);
            }
            if (phenomenonTime) {
                components.add(EntityProperty.PhenomenonTime.name);
            }
            if (result) {
                components.add(EntityProperty.Result.name);
            }
            if (resultTime) {
                components.add(EntityProperty.ResultTime.name);
            }
            if (resultQuality) {
                components.add(EntityProperty.ResultQuality.name);
            }
            if (validTime) {
                components.add(EntityProperty.ValidTime.name);
            }
            if (parameters) {
                components.add(EntityProperty.Parameters.name);
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

    public String formatDataArray(ResourcePath path, Query query, EntitySet<Observation> entitySet, boolean useAbsoluteNavigationLinks) throws IOException {
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
            DataArrayValue dataArray = dataArraySet.get(dataArrayId);
            if (dataArray == null) {
                dataArray = new DataArrayValue(path, obs, components);
                dataArraySet.put(dataArrayId, dataArray);
            }
            dataArray.getDataArray().add(visComps.fromObservation(obs));
        }

        DataArrayResult result = new DataArrayResult();
        List<DataArrayValue> values = result.getValue();
        for (DataArrayValue daValue : dataArraySet.values()) {
            values.add(daValue);
        }

        result.setCount(entitySet.getCount());
        result.setNextLink(entitySet.getNextLink());

        String entityJsonString = new EntityFormatter().writeObject(result);
        return entityJsonString;
    }

}
