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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author scf
 */
@JsonIgnoreProperties("dataArray@iot.count")
public class DataArrayValue {

    /**
     * The typereference for a list of DataArrayValues, used for type-safe json
     * deserialization.
     */
    public static final TypeReference<List<DataArrayValue>> LIST_OF_DATAARRAYVALUE = new TypeReference<List<DataArrayValue>>() {
        // Empty by design.
    };
    @JsonProperty(value = "Datastream")
    private Entity datastream;
    @JsonProperty(value = "MultiDatastream")
    private Entity multiDatastream;
    private List<String> components;
    private List<List<Object>> dataArray = new ArrayList<>();

    public DataArrayValue() {
        components = new ArrayList<>();
    }

    public DataArrayValue(Entity parentEntitiy, List<String> components, EntityType datastream) {
        if (parentEntitiy.getEntityType() == datastream) {
            this.datastream = parentEntitiy;
        } else {
            this.multiDatastream = parentEntitiy;
        }
        this.components = components;
    }

    public DataArrayValue(Query query, ResourcePath path, Entity observation, List<String> components, NavigationPropertyMain<Entity> npDatastream, NavigationPropertyMain<Entity> npMultiDatastream) {
        this.datastream = observation.getProperty(npDatastream);
        this.components = components;
        if (datastream != null) {
            datastream.setSelfLink(UrlHelper.generateSelfLink(path, datastream));
        } else {
            if (npMultiDatastream == null) {
                throw new IllegalArgumentException("No Datastream found and MultiDatastream plugin not enabled.");
            }
            multiDatastream = observation.getProperty(npMultiDatastream);
            multiDatastream.setSelfLink(UrlHelper.generateSelfLink(path, multiDatastream));
        }
    }

    public Entity getDatastream() {
        return datastream;
    }

    public void setDatastream(Entity datastream) {
        if (multiDatastream != null) {
            throw new IllegalArgumentException("Can not have both a Datastream and a MultiDatastream.");
        }
        this.datastream = datastream;
    }

    public Entity getMultiDatastream() {
        return multiDatastream;
    }

    public void setMultiDatastream(Entity multiDatastream) {
        if (datastream != null) {
            throw new IllegalArgumentException("Can not have both a Datastream and a MultiDatastream.");
        }
        this.multiDatastream = multiDatastream;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public List<List<Object>> getDataArray() {
        return dataArray;
    }

    public void setDataArray(List<List<Object>> dataArray) {
        this.dataArray = dataArray;
    }

    public DataArrayValue newItemList() {
        dataArray.add(new ArrayList<>());
        return this;
    }

    public DataArrayValue addItemToTail(Object item) {
        dataArray
                .get(dataArray.size() - 1)
                .add(item);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(datastream, multiDatastream, components, dataArray);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataArrayValue other = (DataArrayValue) obj;
        if (!Objects.equals(this.datastream, other.datastream)) {
            return false;
        }
        if (!Objects.equals(this.multiDatastream, other.multiDatastream)) {
            return false;
        }
        if (!Objects.equals(this.components, other.components)) {
            return false;
        }
        return Objects.equals(this.dataArray, other.dataArray);
    }

    public static String dataArrayIdFor(Entity observation, NavigationPropertyEntity npDatastream, NavigationPropertyEntity npMultiDatastream) {
        Entity ds = observation.getProperty(npDatastream);
        if (ds == null) {
            if (npMultiDatastream == null) {
                throw new IllegalArgumentException("No Datastream found and MultiDatastream plugin not enabled.");
            }
            Entity mds = observation.getProperty(npMultiDatastream);
            return "mds-" + mds.getId().getValue().toString();
        }
        return "ds-" + ds.getId().getValue().toString();
    }
}
