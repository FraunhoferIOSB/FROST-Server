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

import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for FeatureOfInterest objects.
 *
 * @author jab
 */
public class FeatureOfInterestBuilder extends AbstractEntityBuilder<FeatureOfInterest, FeatureOfInterestBuilder> {

    private String name;
    private String description;
    private String encodingType;
    private Object feature;
    private Map<String, Object> properties;
    private EntitySet<Observation> observations;

    public FeatureOfInterestBuilder() {
        properties = new HashMap<>();
        observations = new EntitySetImpl<>(EntityType.OBSERVATION);
    }

    public FeatureOfInterestBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public FeatureOfInterestBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public FeatureOfInterestBuilder setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        return this;
    }

    public FeatureOfInterestBuilder setFeature(Object feature) {
        this.feature = feature;
        return this;
    }

    public FeatureOfInterestBuilder setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public FeatureOfInterestBuilder addProperty(String name, Object value) {
        this.properties.put(name, value);
        return this;
    }

    public FeatureOfInterestBuilder setObservations(EntitySet<Observation> observations) {
        this.observations = observations;
        return this;
    }

    public FeatureOfInterestBuilder addObservation(Observation observation) {
        this.observations.add(observation);
        return this;
    }

    @Override
    protected FeatureOfInterestBuilder getThis() {
        return this;
    }

    @Override
    public FeatureOfInterest build() {
        FeatureOfInterest foi = new FeatureOfInterest(id);
        foi.setSelfLink(selfLink);
        foi.setNavigationLink(navigationLink);
        foi.setName(name);
        foi.setDescription(description);
        foi.setEncodingType(encodingType);
        foi.setFeature(feature);
        foi.setProperties(properties);
        foi.setObservations(observations);
        foi.setExportObject(isExportObject());
        return foi;
    }

}
