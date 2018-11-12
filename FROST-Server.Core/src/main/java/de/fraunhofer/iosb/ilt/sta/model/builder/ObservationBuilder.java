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
package de.fraunhofer.iosb.ilt.sta.model.builder;

import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for Observation objects.
 *
 * @author jab
 */
public class ObservationBuilder extends AbstractEntityBuilder<Observation, ObservationBuilder> {

    private TimeValue phenomenonTime;
    private TimeInstant resultTime;
    private Object result;
    private Object resultQuality;
    private TimeInterval validTime;
    private Map<String, Object> parameters;
    private Datastream datastream;
    private MultiDatastream multiDatastream;
    private FeatureOfInterest featureOfInterest;

    public ObservationBuilder() {
        parameters = new HashMap<>();
    }

    public ObservationBuilder setPhenomenonTime(TimeValue phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        return this;
    }

    public ObservationBuilder setResultTime(TimeInstant resultTime) {
        this.resultTime = resultTime;
        return this;
    }

    public ObservationBuilder setResult(Object result) {
        this.result = result;
        return this;
    }

    public ObservationBuilder setResultQuality(Object resultQuality) {
        this.resultQuality = resultQuality;
        return this;
    }

    public ObservationBuilder setValidTime(TimeInterval validTime) {
        this.validTime = validTime;
        return this;
    }

    public ObservationBuilder setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public ObservationBuilder addParameter(String name, Object value) {
        this.parameters.put(name, value);
        return this;
    }

    public ObservationBuilder setDatastream(Datastream datastream) {
        this.datastream = datastream;
        return this;
    }

    public ObservationBuilder setMultiDatastream(MultiDatastream multiDatastream) {
        this.multiDatastream = multiDatastream;
        return this;
    }

    public ObservationBuilder setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
        return this;
    }

    @Override
    protected ObservationBuilder getThis() {
        return this;
    }

    @Override
    public Observation build() {
        Observation o = new Observation(id);
        o.setSelfLink(selfLink);
        o.setNavigationLink(navigationLink);
        o.setPhenomenonTime(phenomenonTime);
        o.setResultTime(resultTime);
        o.setResult(result);
        o.setResultQuality(resultQuality);
        o.setValidTime(validTime);
        o.setParameters(parameters);
        o.setDatastream(datastream);
        o.setMultiDatastream(multiDatastream);
        o.setFeatureOfInterest(featureOfInterest);
        o.setExportObject(isExportObject());
        return o;
    }

}
