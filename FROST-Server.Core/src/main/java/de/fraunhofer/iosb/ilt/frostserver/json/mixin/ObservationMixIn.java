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
package de.fraunhofer.iosb.ilt.frostserver.json.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;

/**
 * MixIn for serialisation.
 *
 * @author jab, scf
 */
public interface ObservationMixIn extends AbstractEntityMixIn {

    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public abstract Object getResult();

    @JsonSetter(value = "result")
    public abstract Observation setResult(JsonNode result);

    @JsonIgnore
    public abstract boolean isSetPhenomenonTime();

    @JsonIgnore
    public abstract boolean isSetResultTime();

    @JsonIgnore
    public abstract boolean isSetResult();

    @JsonIgnore
    public abstract boolean isSetResultQuality();

    @JsonIgnore
    public abstract boolean isSetValidTime();

    @JsonIgnore
    public abstract boolean isSetParameters();

    @JsonIgnore
    public abstract boolean isSetDatastream();

    @JsonIgnore
    public abstract boolean isSetMultiDatastream();

    @JsonIgnore
    public abstract boolean isSetFeatureOfInterest();

}
