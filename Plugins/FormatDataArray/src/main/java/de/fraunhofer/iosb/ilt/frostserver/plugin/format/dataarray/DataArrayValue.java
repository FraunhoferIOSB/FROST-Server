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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
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

    public DataArrayValue(Entity parentEntitiy, List<String> components, ModelRegistry modelRegistry) {
        if (parentEntitiy.getEntityType() == modelRegistry.DATASTREAM) {
            this.datastream = parentEntitiy;
        } else {
            this.multiDatastream = parentEntitiy;
        }
        this.components = components;
    }

    public DataArrayValue(ResourcePath path, Entity observation, List<String> components, ModelRegistry modelRegistry) {
        this.datastream = (Entity) observation.getProperty(modelRegistry.NP_DATASTREAM);
        this.multiDatastream = (Entity) observation.getProperty(modelRegistry.NP_MULTIDATASTREAM);
        this.components = components;
        if (datastream != null) {
            datastream.setSelfLink(UrlHelper.generateSelfLink(path, datastream));
        }
        if (multiDatastream != null) {
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

    public static String dataArrayIdFor(Entity observation, ModelRegistry modelRegistry) {
        Entity ds = (Entity) observation.getProperty(modelRegistry.NP_DATASTREAM);
        if (ds == null) {
            Entity mds = (Entity) observation.getProperty(modelRegistry.NP_MULTIDATASTREAM);
            return "mds-" + mds.getId().getValue().toString();
        }
        return "ds-" + ds.getId().getValue().toString();
    }
}
