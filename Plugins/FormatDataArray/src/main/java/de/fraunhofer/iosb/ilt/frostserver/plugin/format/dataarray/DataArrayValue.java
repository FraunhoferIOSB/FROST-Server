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
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
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
    private Datastream datastream;
    @JsonProperty(value = "MultiDatastream")
    private MultiDatastream multiDatastream;
    private List<String> components;
    private List<List<Object>> dataArray = new ArrayList<>();

    public DataArrayValue() {
    }

    public DataArrayValue(Datastream datastream, List<String> components) {
        this.datastream = datastream;
        this.components = components;
    }

    public DataArrayValue(MultiDatastream multiDatastream, List<String> components) {
        this.multiDatastream = multiDatastream;
        this.components = components;
    }

    public DataArrayValue(Query query, ResourcePath path, Observation observation, List<String> components) {
        this.datastream = observation.getDatastream();
        this.multiDatastream = observation.getMultiDatastream();
        this.components = components;
        if (datastream != null) {
            datastream.setSelfLink(UrlHelper.generateSelfLink(query, path, datastream));
        }
        if (multiDatastream != null) {
            multiDatastream.setSelfLink(UrlHelper.generateSelfLink(query, path, multiDatastream));
        }
    }

    public Datastream getDatastream() {
        return datastream;
    }

    public void setDatastream(Datastream datastream) {
        if (multiDatastream != null) {
            throw new IllegalArgumentException("Can not have both a Datastream and a MultiDatastream.");
        }
        this.datastream = datastream;
    }

    public MultiDatastream getMultiDatastream() {
        return multiDatastream;
    }

    public void setMultiDatastream(MultiDatastream multiDatastream) {
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

    public static String dataArrayIdFor(Observation observation) {
        Datastream ds = observation.getDatastream();
        if (ds == null) {
            MultiDatastream mds = observation.getMultiDatastream();
            return "mds-" + mds.getId().getValue().toString();
        }
        return "ds-" + ds.getId().getValue().toString();
    }
}
