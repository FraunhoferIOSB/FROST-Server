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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray;

import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class ResultFormatterDataArray implements ResultFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultFormatterDataArray.class);
    private static final String OBSERVATIONS_ONLY = "ResultFormat=dataArray is only valid for /Observations";

    public ResultFormatterDataArray() {
        LOGGER.debug("Creating a new ResultFormaterDataArray.");
    }

    @Override
    public void preProcessRequest(ResourcePath path, Query query) throws IncorrectRequestException {
        if (!(path.getLastElement() instanceof PathElementEntitySet)
                || path.isRef()) {
            throw new IncorrectRequestException(OBSERVATIONS_ONLY);
        }
        if (!query.getSelect().isEmpty()) {
            PathElement lastElement = path.getLastElement();
            if (lastElement instanceof PathElementEntitySet && ((PathElementEntitySet) lastElement).getEntityType() == EntityType.OBSERVATION) {
                query.getSelect().add(NavigationPropertyMain.DATASTREAM);
                query.getSelect().add(NavigationPropertyMain.MULTIDATASTREAM);
            }
        }
    }

    @Override
    public String format(ResourcePath path, Query query, Object result, boolean useAbsoluteNavigationLinks) {
        String entityJsonString = "";
        try {
            if (EntitySet.class.isAssignableFrom(result.getClass())) {
                EntitySet entitySet = (EntitySet) result;
                if (entitySet.getEntityType() == EntityType.OBSERVATION) {
                    return formatDataArray(path, query, entitySet);
                }
            }
            throw new IllegalArgumentException(OBSERVATIONS_ONLY);
        } catch (IOException ex) {
            LOGGER.error("Failed to format response.", ex);
        }
        return entityJsonString;
    }

    @Override
    public String getContentType() {
        return "application/json";
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
            id = select.contains(EntityPropertyMain.ID);
            phenomenonTime = select.contains(EntityPropertyMain.PHENOMENONTIME);
            result = select.contains(EntityPropertyMain.RESULT);
            resultTime = select.contains(EntityPropertyMain.RESULTTIME);
            resultQuality = select.contains(EntityPropertyMain.RESULTQUALITY);
            validTime = select.contains(EntityPropertyMain.VALIDTIME);
            parameters = select.contains(EntityPropertyMain.PARAMETERS);
        }

        public List<String> getComponents() {
            List<String> components = new ArrayList<>();
            if (id) {
                components.add(EntityPropertyMain.ID.entitiyName);
            }
            if (phenomenonTime) {
                components.add(EntityPropertyMain.PHENOMENONTIME.entitiyName);
            }
            if (result) {
                components.add(EntityPropertyMain.RESULT.entitiyName);
            }
            if (resultTime) {
                components.add(EntityPropertyMain.RESULTTIME.entitiyName);
            }
            if (resultQuality) {
                components.add(EntityPropertyMain.RESULTQUALITY.entitiyName);
            }
            if (validTime) {
                components.add(EntityPropertyMain.VALIDTIME.entitiyName);
            }
            if (parameters) {
                components.add(EntityPropertyMain.PARAMETERS.entitiyName);
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

        return JsonWriter.writeObject(result);
    }

}
