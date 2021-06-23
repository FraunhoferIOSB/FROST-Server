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
package de.fraunhofer.iosb.ilt.frostserver.property;

import de.fraunhofer.iosb.ilt.frostserver.model.Actuator;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Task;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.core.AbstractDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.core.AbstractEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EncodingTypeHolder;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NamedEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geojson.GeoJsonObject;

/**
 *
 * @author jab
 */
public enum EntityPropertyMain implements EntityProperty {
    CREATIONTIME("CreationTime"),
    DESCRIPTION("Description"),
    DEFINITION("Definition"),
    ENCODINGTYPE("EncodingType"),
    FEATURE("Feature", true, false),
    ID("id", AT_IOT_ID),
    LOCATION("Location", true, false),
    METADATA("Metadata"),
    MULTIOBSERVATIONDATATYPES("MultiObservationDataTypes"),
    NAME("Name"),
    OBSERVATIONTYPE("ObservationType"),
    OBSERVEDAREA("ObservedArea"),
    PHENOMENONTIME("PhenomenonTime"),
    PARAMETERS("Parameters", true, false),
    PROPERTIES("Properties", true, false),
    RESULT("Result", true, true),
    RESULTTIME("ResultTime", false, true),
    RESULTQUALITY("ResultQuality", true, false),
    SELFLINK(AT_IOT_SELF_LINK, AT_IOT_SELF_LINK),
    TASKINGPARAMETERS("TaskingParameters", true, false),
    TIME("Time"),
    UNITOFMEASUREMENT("UnitOfMeasurement", true, false),
    UNITOFMEASUREMENTS("UnitOfMeasurements", true, false),
    VALIDTIME("ValidTime");

    private static final Map<String, EntityPropertyMain> PROPERTY_BY_NAME = new HashMap<>();

    static {
        for (EntityPropertyMain property : EntityPropertyMain.values()) {
            for (String alias : property.aliases) {
                PROPERTY_BY_NAME.put(alias.toLowerCase(), property);
            }
        }

        CREATIONTIME.addGetterSetterCombo(new GetterSetterSet<>(
                Task.class,
                Task::getCreationTime,
                (Task entity, Object value) -> entity.setCreationTime(Constants.throwIfTypeNot(value, TimeInstant.class)),
                Task::isSetCreationTime));
        DESCRIPTION.addGetterSetterCombo(new GetterSetterSet<>(
                NamedEntity.class,
                NamedEntity::getDescription,
                (NamedEntity entity, Object value) -> entity.setDescription(Constants.throwIfTypeNot(value, String.class)),
                NamedEntity::isSetDescription));
        DEFINITION.addGetterSetterCombo(new GetterSetterSet<>(
                ObservedProperty.class,
                ObservedProperty::getDefinition,
                (ObservedProperty entity, Object value) -> entity.setDefinition(Constants.throwIfTypeNot(value, String.class)),
                ObservedProperty::isSetDefinition));
        ENCODINGTYPE.addGetterSetterCombo(new GetterSetterSet<>(
                EncodingTypeHolder.class,
                EncodingTypeHolder::getEncodingType,
                (EncodingTypeHolder entity, Object value) -> entity.setEncodingType(Constants.throwIfTypeNot(value, String.class)),
                EncodingTypeHolder::isSetEncodingType));
        FEATURE.addGetterSetterCombo(new GetterSetterSet<>(
                FeatureOfInterest.class,
                FeatureOfInterest::getFeature,
                (FeatureOfInterest entity, Object value) -> entity.setFeature(value),
                FeatureOfInterest::isSetFeature));
        ID.addGetterSetterCombo(new GetterSetterSet<>(
                AbstractEntity.class, AbstractEntity::getId,
                (AbstractEntity entity, Object value) -> entity.setId(Constants.throwIfTypeNot(value, Id.class)),
                AbstractEntity::isSetId));
        LOCATION.addGetterSetterCombo(new GetterSetterSet<>(
                Location.class,
                Location::getLocation,
                (Location entity, Object value) -> entity.setLocation(value),
                Location::isSetLocation));
        METADATA.addGetterSetterCombo(new GetterSetterSet<>(
                Actuator.class,
                Actuator::getMetadata,
                (Actuator entity, Object value) -> entity.setMetadata(value),
                Actuator::isSetMetadata));
        METADATA.addGetterSetterCombo(new GetterSetterSet<>(
                Sensor.class,
                Sensor::getMetadata,
                (Sensor entity, Object value) -> entity.setMetadata(value),
                Sensor::isSetMetadata));
        MULTIOBSERVATIONDATATYPES.addGetterSetterCombo(new GetterSetterSet<>(
                MultiDatastream.class,
                MultiDatastream::getMultiObservationDataTypes,
                (MultiDatastream entity, Object value) -> entity.setMultiObservationDataTypes(Constants.throwIfTypeNot(value, List.class)),
                MultiDatastream::isSetMultiObservationDataTypes));
        NAME.addGetterSetterCombo(new GetterSetterSet<>(
                NamedEntity.class,
                NamedEntity::getName,
                (NamedEntity entity, Object value) -> entity.setName(Constants.throwIfTypeNot(value, String.class)),
                NamedEntity::isSetName));
        OBSERVATIONTYPE.addGetterSetterCombo(new GetterSetterSet<>(
                AbstractDatastream.class,
                AbstractDatastream::getObservationType,
                (AbstractDatastream entity, Object value) -> entity.setObservationType(Constants.throwIfTypeNot(value, String.class)),
                AbstractDatastream::isSetObservationType));
        OBSERVEDAREA.addGetterSetterCombo(new GetterSetterSet<>(
                AbstractDatastream.class,
                AbstractDatastream::getObservedArea,
                (AbstractDatastream entity, Object value) -> entity.setObservedArea(Constants.throwIfTypeNot(value, GeoJsonObject.class)),
                AbstractDatastream::isSetObservedArea));
        PHENOMENONTIME.addGetterSetterCombo(new GetterSetterSet<>(
                Observation.class,
                Observation::getPhenomenonTime,
                (Observation entity, Object value) -> entity.setPhenomenonTime(Constants.throwIfTypeNot(value, TimeValue.class)),
                Observation::isSetPhenomenonTime));
        PHENOMENONTIME.addGetterSetterCombo(new GetterSetterSet<>(
                AbstractDatastream.class,
                AbstractDatastream::getPhenomenonTime,
                (AbstractDatastream entity, Object value) -> entity.setPhenomenonTime(Constants.throwIfTypeNot(value, TimeInterval.class)),
                AbstractDatastream::isSetPhenomenonTime));
        PARAMETERS.addGetterSetterCombo(new GetterSetterSet<>(
                Observation.class,
                Observation::getParameters,
                (Observation entity, Object value) -> entity.setParameters(Constants.throwIfTypeNot(value, Map.class)),
                Observation::isSetParameters));
        PROPERTIES.addGetterSetterCombo(new GetterSetterSet<>(
                NamedEntity.class,
                NamedEntity::getProperties,
                (NamedEntity entity, Object value) -> entity.setProperties(Constants.throwIfTypeNot(value, Map.class)),
                NamedEntity::isSetProperties));
        RESULT.addGetterSetterCombo(new GetterSetterSet<>(
                Observation.class,
                Observation::getResult,
                (Observation entity, Object value) -> entity.setResult(value),
                Observation::isSetResult));
        RESULTTIME.addGetterSetterCombo(new GetterSetterSet<>(
                Observation.class,
                Observation::getResultTime,
                (Observation entity, Object value) -> entity.setResultTime(Constants.throwIfTypeNot(value, TimeInstant.class)),
                Observation::isSetResultTime));
        RESULTTIME.addGetterSetterCombo(new GetterSetterSet<>(
                AbstractDatastream.class,
                AbstractDatastream::getResultTime,
                (AbstractDatastream entity, Object value) -> entity.setResultTime(Constants.throwIfTypeNot(value, TimeInterval.class)),
                AbstractDatastream::isSetResultTime));
        RESULTQUALITY.addGetterSetterCombo(new GetterSetterSet<>(
                Observation.class,
                Observation::getResultQuality,
                (Observation entity, Object value) -> entity.setResultQuality(value),
                Observation::isSetResultQuality));
        SELFLINK.addGetterSetterCombo(new GetterSetterSet<>(
                AbstractEntity.class, AbstractEntity::getSelfLink,
                (AbstractEntity entity, Object value) -> entity.setSelfLink(Constants.throwIfTypeNot(value, String.class)),
                AbstractEntity::isSetSelfLink));
        TASKINGPARAMETERS.addGetterSetterCombo(new GetterSetterSet<>(
                Task.class,
                Task::getTaskingParameters,
                (Task entity, Object value) -> entity.setTaskingParameters(Constants.throwIfTypeNot(value, Map.class)),
                Task::isSetTaskingParameters));
        TASKINGPARAMETERS.addGetterSetterCombo(new GetterSetterSet<>(
                TaskingCapability.class,
                TaskingCapability::getTaskingParameters,
                (TaskingCapability entity, Object value) -> entity.setTaskingParameters(Constants.throwIfTypeNot(value, Map.class)),
                TaskingCapability::isSetTaskingParameters));
        TIME.addGetterSetterCombo(new GetterSetterSet<>(
                HistoricalLocation.class,
                HistoricalLocation::getTime,
                (HistoricalLocation entity, Object value) -> entity.setTime(Constants.throwIfTypeNot(value, TimeInstant.class)),
                HistoricalLocation::isSetTime));
        UNITOFMEASUREMENT.addGetterSetterCombo(new GetterSetterSet<>(
                Datastream.class,
                Datastream::getUnitOfMeasurement,
                (Datastream entity, Object value) -> entity.setUnitOfMeasurement(Constants.throwIfTypeNot(value, UnitOfMeasurement.class)),
                Datastream::isSetUnitOfMeasurement));
        UNITOFMEASUREMENTS.addGetterSetterCombo(new GetterSetterSet<>(
                MultiDatastream.class,
                MultiDatastream::getUnitOfMeasurements,
                (MultiDatastream entity, Object value) -> entity.setUnitOfMeasurements(Constants.throwIfTypeNot(value, List.class)),
                MultiDatastream::isSetUnitOfMeasurements));
        VALIDTIME.addGetterSetterCombo(new GetterSetterSet<>(
                Observation.class,
                Observation::getValidTime,
                (Observation entity, Object value) -> entity.setValidTime(Constants.throwIfTypeNot(value, TimeInterval.class)),
                Observation::isSetValidTime));
    }

    /**
     * The entityName of this property as used in URLs.
     */
    public final String entityName;
    /**
     * The name of this property as used in json.
     */
    public final String jsonName;

    public final boolean hasCustomProperties;
    /**
     * Flag indicating a null value should not be ignored, but serialised as
     * Json NULL.
     */
    public final boolean serialiseNull;

    private final Collection<String> aliases;

    private final List<GetterSetterSet> gettersSetters = new ArrayList<>();

    private EntityPropertyMain(String codeName) {
        this(codeName, false, false);
    }

    private EntityPropertyMain(String codeName, boolean hasCustomProperties, boolean serialiseNull) {
        this.aliases = new ArrayList<>();
        this.aliases.add(codeName);
        this.entityName = StringHelper.deCapitalize(codeName);
        this.jsonName = entityName;
        this.hasCustomProperties = hasCustomProperties;
        this.serialiseNull = serialiseNull;
    }

    private EntityPropertyMain(String pathName, String jsonName, String... aliases) {
        this.aliases = new ArrayList<>();
        this.entityName = pathName;
        this.jsonName = jsonName;
        this.aliases.add(name());
        this.aliases.add(jsonName);
        this.aliases.addAll(Arrays.asList(aliases));
        this.hasCustomProperties = false;
        this.serialiseNull = false;
    }

    public void addGetterSetterCombo(GetterSetterSet gsc) {
        gettersSetters.add(gsc);
    }

    public static EntityPropertyMain fromString(String propertyName) {
        EntityPropertyMain property = PROPERTY_BY_NAME.get(propertyName.toLowerCase());
        if (property == null) {
            throw new IllegalArgumentException("no entity property with name '" + propertyName + "'");
        }
        return property;
    }

    @Override
    public String getName() {
        return entityName;
    }

    @Override
    public String getJsonName() {
        return jsonName;
    }

    @Override
    public Object getFrom(Entity entity) {
        for (GetterSetterSet gsc : gettersSetters) {
            if (gsc.forClass.isAssignableFrom(entity.getClass())) {
                return gsc.getter.getFrom(entity);
            }
        }
        throw new IllegalArgumentException("Do not know how to get " + this + " from " + entity);
    }

    @Override
    public void setOn(Entity entity, Object value) {
        for (GetterSetterSet gsc : gettersSetters) {
            if (gsc.forClass.isAssignableFrom(entity.getClass())) {
                gsc.setter.setOn(entity, value);
                return;
            }
        }
        throw new IllegalArgumentException("Do not know how to set " + this + " on " + entity);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        for (GetterSetterSet gsc : gettersSetters) {
            if (gsc.forClass.isAssignableFrom(entity.getClass())) {
                return gsc.isSetter.isSetOn(entity);
            }
        }
        throw new IllegalArgumentException("Do not know how to check " + this + " on " + entity);
    }

    public static interface PropertyGet<T> {

        public Object getFrom(T entity);
    }

    public static interface PropertySet<T> {

        public void setOn(T entity, Object value);
    }

    public static interface PropertyIsSet<T> {

        public boolean isSetOn(T entity);
    }

    public static class GetterSetterSet<T> {

        public final Class<T> forClass;
        public final PropertyGet<T> getter;
        public final PropertySet<T> setter;
        public final PropertyIsSet<T> isSetter;

        public GetterSetterSet(Class<T> forClass, PropertyGet<T> getter, PropertySet<T> setter, PropertyIsSet<T> isSetter) {
            this.forClass = forClass;
            this.getter = getter;
            this.setter = setter;
            this.isSetter = isSetter;
        }

    }
}
