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
    private List<String> components;
    private List<List<Object>> dataArray = new ArrayList<>();

    public DataArrayValue() {
    }

    public DataArrayValue(Datastream datastream, List<String> components) {
        this.datastream = datastream;
        this.components = components;
    }

    public Datastream getDatastream() {
        return datastream;
    }

    public void setDatastream(Datastream datastream) {
        this.datastream = datastream;
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
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.datastream);
        hash = 29 * hash + Objects.hashCode(this.components);
        hash = 29 * hash + Objects.hashCode(this.dataArray);
        return hash;
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
        if (!Objects.equals(this.components, other.components)) {
            return false;
        }
        if (!Objects.equals(this.dataArray, other.dataArray)) {
            return false;
        }
        return true;
    }

}
