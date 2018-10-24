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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.util.UrlHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author scf
 */
@JsonIgnoreProperties("dataArray@iot.count")
public class DataArrayValue {

    private Datastream datastream;
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

    public DataArrayValue(ResourcePath path, Observation observation, List<String> components) {
        this.datastream = observation.getDatastream();
        this.multiDatastream = observation.getMultiDatastream();
        this.components = components;
        if (datastream != null) {
            datastream.setNavigationLink(UrlHelper.generateSelfLink(path, datastream));
        }
        if (multiDatastream != null) {
            multiDatastream.setNavigationLink(UrlHelper.generateSelfLink(path, multiDatastream));
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
