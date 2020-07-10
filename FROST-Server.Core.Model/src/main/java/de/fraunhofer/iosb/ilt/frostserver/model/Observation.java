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

import de.fraunhofer.iosb.ilt.frostserver.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 */
public class Observation extends AbstractEntity<Observation> {

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
        this(null);
    }

    public Observation(Id id) {
        super(id);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.OBSERVATION;
    }

    @Override
    public void complete(PathElementEntitySet containingSet) throws IncompleteEntityException {
        PathElement parent = containingSet.getParent();
        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            Id parentId = parentEntity.getId();
            if (parentId != null) {
                switch (parentEntity.getEntityType()) {
                    case DATASTREAM:
                        setDatastream(new Datastream(parentId));
                        LOGGER.debug("Set datastreamId to {}.", parentId);
                        break;

                    case MULTIDATASTREAM:
                        setMultiDatastream(new MultiDatastream(parentId));
                        LOGGER.debug("Set multiDatastreamId to {}.", parentId);
                        break;

                    case FEATUREOFINTEREST:
                        setFeatureOfInterest(new FeatureOfInterest(parentId));
                        LOGGER.debug("Set featureOfInterestId to {}.", parentId);
                        break;

                    default:
                        LOGGER.error("Incorrect 'parent' entity type for {}: {}", getEntityType(), parentEntity.getEntityType());
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
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        super.setEntityPropertiesSet(set, entityPropertiesOnly);
        setSets(set, entityPropertiesOnly);
    }

    private void setSets(boolean set, boolean entityPropertiesOnly) {
        setPhenomenonTime = set;
        setResultTime = set;
        setResult = set;
        setResultQuality = set;
        setValidTime = set;
        setParameters = set;
        if (!entityPropertiesOnly) {
            setDatastream = set;
            setMultiDatastream = set;
            setFeatureOfInterest = set;
        }
    }

    @Override
    public void setEntityPropertiesSet(Observation comparedTo, EntityChangedMessage message) {
        super.setEntityPropertiesSet(comparedTo, message);
        setSets(false, false);
        if (!Objects.equals(featureOfInterest, comparedTo.getFeatureOfInterest())) {
            setFeatureOfInterest = true;
            message.addNpField(NavigationPropertyMain.FEATUREOFINTEREST);
        }
        if (!Objects.equals(datastream, comparedTo.getDatastream())) {
            setDatastream = true;
            message.addNpField(NavigationPropertyMain.DATASTREAM);
        }
        if (!Objects.equals(multiDatastream, comparedTo.getMultiDatastream())) {
            setMultiDatastream = true;
            message.addNpField(NavigationPropertyMain.MULTIDATASTREAM);
        }
        if (!Objects.equals(phenomenonTime, comparedTo.getPhenomenonTime())) {
            setPhenomenonTime = true;
            message.addEpField(EntityPropertyMain.PHENOMENONTIME);
        }
        if (!Objects.equals(resultTime, comparedTo.getResultTime())) {
            setResultTime = true;
            message.addEpField(EntityPropertyMain.RESULTTIME);
        }
        if (!Objects.equals(result, comparedTo.getResult())) {
            setResult = true;
            message.addEpField(EntityPropertyMain.RESULT);
        }
        if (!Objects.equals(resultQuality, comparedTo.getResultQuality())) {
            setResultQuality = true;
            message.addEpField(EntityPropertyMain.RESULTQUALITY);
        }
        if (!Objects.equals(validTime, comparedTo.getValidTime())) {
            setValidTime = true;
            message.addEpField(EntityPropertyMain.VALIDTIME);
        }
        if (!Objects.equals(parameters, comparedTo.getParameters())) {
            setParameters = true;
            message.addEpField(EntityPropertyMain.PARAMETERS);
        }
    }

    public TimeValue getPhenomenonTime() {
        return phenomenonTime;
    }

    public Observation setPhenomenonTime(TimeValue phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
        setPhenomenonTime = phenomenonTime != null;
        return this;
    }

    public boolean isSetPhenomenonTime() {
        return setPhenomenonTime;
    }

    public TimeInstant getResultTime() {
        return resultTime;
    }

    public Observation setResultTime(TimeInstant resultTime) {
        this.resultTime = resultTime;
        setResultTime = resultTime != null;
        return this;
    }

    public boolean isSetResultTime() {
        return setResultTime;
    }

    public Object getResult() {
        return result;
    }

    public Observation setResult(Object result) {
        this.result = result;
        setResult = true;
        return this;
    }

    public boolean isSetResult() {
        return setResult;
    }

    public Object getResultQuality() {
        return resultQuality;
    }

    public Observation setResultQuality(Object resultQuality) {
        this.resultQuality = resultQuality;
        setResultQuality = resultQuality != null;
        return this;
    }

    public boolean isSetResultQuality() {
        return setResultQuality;
    }

    public TimeInterval getValidTime() {
        return validTime;
    }

    public Observation setValidTime(TimeInterval validTime) {
        this.validTime = validTime;
        setValidTime = validTime != null;
        return this;
    }

    public boolean isSetValidTime() {
        return setValidTime;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        if (parameters != null && parameters.isEmpty()) {
            this.parameters = null;
        } else {
            this.parameters = parameters;
        }
        setParameters = true;
    }

    public Observation addParameter(String name, Object value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(name, value);
        setParameters = true;
        return this;
    }

    public boolean isSetParameters() {
        return setParameters;
    }

    public Datastream getDatastream() {
        return datastream;
    }

    public Observation setDatastream(Datastream datastream) {
        this.datastream = datastream;
        setDatastream = datastream != null;
        return this;
    }

    public boolean isSetDatastream() {
        return setDatastream;
    }

    public MultiDatastream getMultiDatastream() {
        return multiDatastream;
    }

    public Observation setMultiDatastream(MultiDatastream multiDatastream) {
        this.multiDatastream = multiDatastream;
        setMultiDatastream = multiDatastream != null;
        return this;
    }

    public boolean isSetMultiDatastream() {
        return setMultiDatastream;
    }

    public FeatureOfInterest getFeatureOfInterest() {
        return featureOfInterest;
    }

    public Observation setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
        setFeatureOfInterest = featureOfInterest != null;
        return this;
    }

    public boolean isSetFeatureOfInterest() {
        return setFeatureOfInterest;
    }

    @Override
    protected Observation getThis() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                phenomenonTime,
                resultTime,
                result,
                resultQuality,
                validTime,
                parameters,
                datastream,
                multiDatastream,
                featureOfInterest
        );
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
        return super.equals(other)
                && Objects.equals(phenomenonTime, other.phenomenonTime)
                && Objects.equals(resultTime, other.resultTime)
                && Objects.equals(result, other.result)
                && Objects.equals(resultQuality, other.resultQuality)
                && Objects.equals(validTime, other.validTime)
                && Objects.equals(parameters, other.parameters)
                && Objects.equals(datastream, other.datastream)
                && Objects.equals(multiDatastream, other.multiDatastream)
                && Objects.equals(featureOfInterest, other.featureOfInterest);
    }

}
