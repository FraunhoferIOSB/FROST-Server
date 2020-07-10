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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NamedEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import java.util.Objects;

/**
 *
 * @author jab, scf
 */
public class FeatureOfInterest extends NamedEntity<FeatureOfInterest> {

    private String encodingType;
    private Object feature;
    private EntitySet<Observation> observations;

    private boolean setEncodingType;
    private boolean setFeature;

    public FeatureOfInterest() {
        this(null);
    }

    public FeatureOfInterest(Id id) {
        super(id);
        this.observations = new EntitySetImpl<>(EntityType.OBSERVATION);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FEATUREOFINTEREST;
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setSets(set);
    }

    private void setSets(boolean set) {
        setEncodingType = set;
        setFeature = set;
    }

    @Override
    public void setEntityPropertiesSet(FeatureOfInterest comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSets(false);
        if (!Objects.equals(encodingType, comparedTo.getEncodingType())) {
            setEncodingType = true;
            message.addEpField(EntityPropertyMain.ENCODINGTYPE);
        }
        if (!Objects.equals(feature, comparedTo.getFeature())) {
            setFeature = true;
            message.addEpField(EntityPropertyMain.FEATURE);
        }
    }

    public String getEncodingType() {
        return encodingType;
    }

    public FeatureOfInterest setEncodingType(String encodingType) {
        this.encodingType = encodingType;
        setEncodingType = encodingType != null;
        return this;
    }

    public boolean isSetEncodingType() {
        return setEncodingType;
    }

    public Object getFeature() {
        return feature;
    }

    public FeatureOfInterest setFeature(Object feature) {
        setFeature = feature != null;
        this.feature = feature;
        return this;
    }

    public boolean isSetFeature() {
        return setFeature;
    }

    public EntitySet<Observation> getObservations() {
        return observations;
    }

    public FeatureOfInterest setObservations(EntitySet<Observation> observations) {
        this.observations = observations;
        return this;
    }

    @Override
    protected FeatureOfInterest getThis() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), encodingType, feature, observations);
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
        final FeatureOfInterest other = (FeatureOfInterest) obj;
        return super.equals(other)
                && Objects.equals(encodingType, other.encodingType)
                && Objects.equals(feature, other.feature)
                && Objects.equals(observations, other.observations);
    }

}
