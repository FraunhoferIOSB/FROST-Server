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
package de.fraunhofer.iosb.ilt.sta.model;

import de.fraunhofer.iosb.ilt.sta.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.FeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.MultiDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.sta.model.id.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class Observation extends AbstractEntity {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Observation.class);
    private TimeValue phenomenonTime;
    private TimeInstant resultTime;
    private Object result;
    private Object resultQuality;
    private TimeInterval validTime;
    private Map<String, Object> parameters;
    private Datastream datastream;
    private MultiDatastream multiDatastream;
    private FeatureOfInterest featureOfInterest;

    private boolean setPhenomenonTime;
    private boolean setResultTime;
    private boolean setResult;
    private boolean setResultQuality;
    private boolean setValidTime;
    private boolean setParameters;
    private boolean setDatastream;
    private boolean setMultiDatastream;
    private boolean setFeatureOfInterest;

    public Observation() {
    }

    public Observation(Id id,
            String selfLink,
            String navigationLink,
            TimeValue phenomenonTime,
            TimeInstant resultTime,
            Object result,
            Object resultQuality,
            TimeInterval validTime,
            Map<String, Object> parameters,
            Datastream datastream,
            MultiDatastream multiDatastreams,
            FeatureOfInterest featureOfInterest) {
        super(id, selfLink, navigationLink);
        this.phenomenonTime = phenomenonTime;
        this.resultTime = resultTime;
        this.result = result;
        this.resultQuality = resultQuality;
        this.validTime = validTime;
        if (parameters != null && !parameters.isEmpty()) {
            this.parameters = new HashMap<>(parameters);
        }
        this.datastream = datastream;
        this.multiDatastream = multiDatastreams;
        this.featureOfInterest = featureOfInterest;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Observation;
    }

    @Override
    public void complete(EntitySetPathElement containingSet) throws IncompleteEntityException {
        ResourcePathElement parent = containingSet.getParent();
        if (parent != null && parent instanceof EntityPathElement) {
            EntityPathElement parentEntity = (EntityPathElement) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                switch (parentEntity.getEntityType()) {
                    case Datastream:
                        setDatastream(new DatastreamBuilder().setId(parentId).build());
                        LOGGER.debug("Set datastreamId to {}.", parentId);
                        break;

                    case MultiDatastream:
                        setMultiDatastream(new MultiDatastreamBuilder().setId(parentId).build());
                        LOGGER.debug("Set multiDatastreamId to {}.", parentId);
                        break;

                    case FeatureOfInterest:
                        setFeatureOfInterest(new FeatureOfInterestBuilder().setId(parentId).build());
                        LOGGER.debug("Set featureOfInterestId to {}.", parentId);
                        break;
                }
            }
        }

        super.complete(containingSet);
    }

    @Override
    public void complete(boolean entityPropertiesOnly) throws IncompleteEntityException {
        if (!entityPropertiesOnly) {
            if (datastream != null && multiDatastream != null) {
                throw new IllegalStateException("Observation can not have both a Datasteam and a MultiDatastream.");
            }
            if (datastream == null && multiDatastream == null) {
                throw new IllegalStateException("Observation must have either a Datasteam or a MultiDatastream.");
            }
        }

        super.complete(entityPropertiesOnly);
    }

    @Override
    public void setEntityPropertiesSet() {
        setPhenomenonTime = true;
        setResultTime = true;
        setResult = true;
        setResultQuality = true;
        setValidTime = true;
        setParameters = true;
    }

    public TimeValue getPhenomenonTime() {
        return phenomenonTime;
    }

    public TimeInstant getResultTime() {
        return resultTime;
    }

    public Object getResult() {
        return result;
    }

    public Object getResultQuality() {
        return resultQuality;
    }

    public TimeInterval getValidTime() {
        return validTime;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Datastream getDatastream() {
        return datastream;
    }

    public MultiDatastream getMultiDatastream() {
        return multiDatastream;
    }

    public FeatureOfInterest getFeatureOfInterest() {
        return featureOfInterest;
    }

    public boolean isSetPhenomenonTime() {
        return setPhenomenonTime;
    }

    public boolean isSetResultTime() {
        return setResultTime;
    }

    public boolean isSetResult() {
        return setResult;
    }

    public boolean isSetResultQuality() {
        return setResultQuality;
    }

    public boolean isSetValidTime() {
        return setValidTime;
    }

    public boolean isSetParameters() {
        return setParameters;
    }

    public boolean isSetDatastream() {
        return setDatastream;
    }

    public boolean isSetMultiDatastream() {
        return setMultiDatastream;
    }

    public boolean isSetFeatureOfInterest() {
        return setFeatureOfInterest;
    }

    public void setPhenomenonTime(TimeValue phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        setPhenomenonTime = true;
    }

    public void setResultTime(TimeInstant resultTime) {
        this.resultTime = resultTime;
        setResultTime = true;
    }

    public void setResult(Object result) {
        this.result = result;
        setResult = true;
    }

    public void setResultQuality(Object resultQuality) {
        this.resultQuality = resultQuality;
        setResultQuality = true;
    }

    public void setValidTime(TimeInterval validTime) {
        this.validTime = validTime;
        setValidTime = true;
    }

    public void setParameters(Map<String, Object> parameters) {
        if (parameters != null && parameters.isEmpty()) {
            parameters = null;
        }
        this.parameters = parameters;
        setParameters = true;
    }

    public void setDatastream(Datastream datastream) {
        this.datastream = datastream;
        setDatastream = true;
    }

    public void setMultiDatastream(MultiDatastream multiDatastream) {
        this.multiDatastream = multiDatastream;
        setMultiDatastream = true;
    }

    public void setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
        setFeatureOfInterest = true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.phenomenonTime);
        hash = 59 * hash + Objects.hashCode(this.resultTime);
        hash = 59 * hash + Objects.hashCode(this.result);
        hash = 59 * hash + Objects.hashCode(this.resultQuality);
        hash = 59 * hash + Objects.hashCode(this.validTime);
        hash = 59 * hash + Objects.hashCode(this.parameters);
        hash = 59 * hash + Objects.hashCode(this.datastream);
        hash = 59 * hash + Objects.hashCode(this.multiDatastream);
        hash = 59 * hash + Objects.hashCode(this.featureOfInterest);
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
        final Observation other = (Observation) obj;
        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.phenomenonTime, other.phenomenonTime)) {
            return false;
        }
        if (!Objects.equals(this.resultTime, other.resultTime)) {
            return false;
        }
        if (!Objects.equals(this.result, other.result)) {
            return false;
        }
        if (!Objects.equals(this.resultQuality, other.resultQuality)) {
            return false;
        }
        if (!Objects.equals(this.validTime, other.validTime)) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        if (!Objects.equals(this.datastream, other.datastream)) {
            return false;
        }
        if (!Objects.equals(this.multiDatastream, other.multiDatastream)) {
            return false;
        }
        if (!Objects.equals(this.featureOfInterest, other.featureOfInterest)) {
            return false;
        }
        return true;
    }

}
