/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_GEOJSONOBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_ID;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_LIST_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_LIST_UOM;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_MAP;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_OBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEINSTANT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEINTERVAL;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEVALUE;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_UOM;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ModelRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelRegistry.class.getName());

    /**
     * The global EntityProperty @iot.id.
     */
    public static final EntityPropertyMain<Id> EP_ID = new EntityPropertyMain<>(AT_IOT_ID, TYPE_REFERENCE_ID, "id");
    /**
     * The global EntityProperty SelfLink.
     */
    public static final EntityPropertyMain<String> EP_SELFLINK = new EntityPropertyMain<>(AT_IOT_SELF_LINK, TYPE_REFERENCE_STRING, "SelfLink");
    /**
     * The global EntityProperty encodingType.
     */
    public static final EntityPropertyMain<String> EP_ENCODINGTYPE = new EntityPropertyMain<>("encodingType", TYPE_REFERENCE_STRING);

    public final EntityType THING = new EntityType("Thing", "Things");
    public final EntityType SENSOR = new EntityType("Sensor", "Sensors");
    public final EntityType OBSERVED_PROPERTY = new EntityType("ObservedProperty", "ObservedProperties");
    public final EntityType OBSERVATION = new EntityType("Observation", "Observations");
    public final EntityType LOCATION = new EntityType("Location", "Locations");
    public final EntityType HISTORICAL_LOCATION = new EntityType("HistoricalLocation", "HistoricalLocations");
    public final EntityType FEATURE_OF_INTEREST = new EntityType("FeatureOfInterest", "FeaturesOfInterest");
    public final EntityType MULTI_DATASTREAM = new EntityType("MultiDatastream", "MultiDatastreams");
    public final EntityType DATASTREAM = new EntityType("Datastream", "Datastreams");

    public final NavigationPropertyMain.NavigationPropertyEntity NP_DATASTREAM = new NavigationPropertyMain.NavigationPropertyEntity("Datastream");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_DATASTREAMS = new NavigationPropertyMain.NavigationPropertyEntitySet("Datastreams");
    public final NavigationPropertyMain.NavigationPropertyEntity NP_MULTIDATASTREAM = new NavigationPropertyMain.NavigationPropertyEntity("MultiDatastream");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_MULTIDATASTREAMS = new NavigationPropertyMain.NavigationPropertyEntitySet("MultiDatastreams");
    public final NavigationPropertyMain.NavigationPropertyEntity NP_FEATUREOFINTEREST = new NavigationPropertyMain.NavigationPropertyEntity("FeatureOfInterest");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_HISTORICALLOCATIONS = new NavigationPropertyMain.NavigationPropertyEntitySet("HistoricalLocations");
    public final NavigationPropertyMain.NavigationPropertyEntity NP_LOCATION = new NavigationPropertyMain.NavigationPropertyEntity("Location");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_LOCATIONS = new NavigationPropertyMain.NavigationPropertyEntitySet("Locations");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_OBSERVATIONS = new NavigationPropertyMain.NavigationPropertyEntitySet("Observations");
    public final NavigationPropertyMain.NavigationPropertyEntity NP_OBSERVEDPROPERTY = new NavigationPropertyMain.NavigationPropertyEntity("ObservedProperty");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_OBSERVEDPROPERTIES = new NavigationPropertyMain.NavigationPropertyEntitySet("ObservedProperties");
    public final NavigationPropertyMain.NavigationPropertyEntity NP_SENSOR = new NavigationPropertyMain.NavigationPropertyEntity("Sensor");
    public final NavigationPropertyMain.NavigationPropertyEntity NP_THING = new NavigationPropertyMain.NavigationPropertyEntity("Thing");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_THINGS = new NavigationPropertyMain.NavigationPropertyEntitySet("Things");

    public final EntityPropertyMain<TimeInstant> EP_CREATIONTIME = new EntityPropertyMain<>("creationTime", TYPE_REFERENCE_TIMEINSTANT);
    public final EntityPropertyMain<String> EP_DESCRIPTION = new EntityPropertyMain<>("description", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<String> EP_DEFINITION = new EntityPropertyMain<>("definition", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<Object> EP_FEATURE = new EntityPropertyMain<>("feature", null, true, false);
    public final EntityPropertyMain<Object> EP_LOCATION = new EntityPropertyMain<>("Location", null, true, false);
    public final EntityPropertyMain<String> EP_METADATA = new EntityPropertyMain<>("Metadata", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<List<String>> EP_MULTIOBSERVATIONDATATYPES = new EntityPropertyMain<>("MultiObservationDataTypes", TYPE_REFERENCE_LIST_STRING);
    public final EntityPropertyMain<String> EP_NAME = new EntityPropertyMain<>("Name", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<String> EP_OBSERVATIONTYPE = new EntityPropertyMain<>("ObservationType", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<GeoJsonObject> EP_OBSERVEDAREA = new EntityPropertyMain<>("ObservedArea", TYPE_REFERENCE_GEOJSONOBJECT);
    public final EntityPropertyMain<TimeValue> EP_PHENOMENONTIME = new EntityPropertyMain<>("PhenomenonTime", TYPE_REFERENCE_TIMEVALUE);
    public final EntityPropertyMain<TimeInterval> EP_PHENOMENONTIME_DS = new EntityPropertyMain<>("PhenomenonTime", TYPE_REFERENCE_TIMEINTERVAL);
    public final EntityPropertyMain<Map<String, Object>> EP_PARAMETERS = new EntityPropertyMain<>("Parameters", TYPE_REFERENCE_MAP, true, false);
    public final EntityPropertyMain<Map<String, Object>> EP_PROPERTIES = new EntityPropertyMain<>("Properties", TYPE_REFERENCE_MAP, true, false);
    public final EntityPropertyMain<Object> EP_RESULT = new EntityPropertyMain<>("Result", TYPE_REFERENCE_OBJECT, true, true);
    public final EntityPropertyMain<TimeInstant> EP_RESULTTIME = new EntityPropertyMain<>("ResultTime", TYPE_REFERENCE_TIMEINSTANT, false, true);
    public final EntityPropertyMain<TimeInterval> EP_RESULTTIME_DS = new EntityPropertyMain<>("ResultTime", TYPE_REFERENCE_TIMEINTERVAL, false, true);
    public final EntityPropertyMain<Object> EP_RESULTQUALITY = new EntityPropertyMain<>("ResultQuality", TYPE_REFERENCE_OBJECT, true, false);
    public final EntityPropertyMain<TimeInstant> EP_TIME = new EntityPropertyMain<>("Time", TYPE_REFERENCE_TIMEINSTANT);
    public final EntityPropertyMain<UnitOfMeasurement> EP_UNITOFMEASUREMENT = new EntityPropertyMain<>("UnitOfMeasurement", TYPE_REFERENCE_UOM, true, false);
    public final EntityPropertyMain<List<UnitOfMeasurement>> EP_UNITOFMEASUREMENTS = new EntityPropertyMain<>("UnitOfMeasurements", TYPE_REFERENCE_LIST_UOM, true, false);
    public final EntityPropertyMain<TimeInterval> EP_VALIDTIME = new EntityPropertyMain<>("ValidTime", TYPE_REFERENCE_TIMEINTERVAL);

    private final Map<String, EntityType> typesByName = new HashMap<>();
    private final Set<EntityType> types = new LinkedHashSet<>();

    private final Map<String, NavigationPropertyMain> navPropertyByName = new HashMap<>();
    private final Set<NavigationPropertyMain> navProperties = new LinkedHashSet<>();

    private final Map<String, EntityPropertyMain> entityPropertyByName = new HashMap<>();
    private final Set<EntityPropertyMain> entityProperties = new LinkedHashSet<>();

    private Class<? extends Id> idClass;

    /**
     * Entities need queries, even when sent through messages.
     */
    private final EntityChangedMessage.QueryGenerator messageQueryGenerator = new EntityChangedMessage.QueryGenerator();

    public final Set<NavigationPropertyMain> getNavProperties() {
        return navProperties;
    }

    public Set<EntityPropertyMain> getEntityProperties() {
        return entityProperties;
    }

    public EntityPropertyMain getEntityProperty(String name) {
        return entityPropertyByName.get(name);
    }

    public final <T> EntityPropertyMain<T> registerEntityProperty(EntityPropertyMain<T> property) {
        if (entityPropertyByName.containsKey(property.name)) {
            if (entityPropertyByName.get(property.name) == property) {
                // This exact property is already registered
                return property;
            } else {
                throw new IllegalArgumentException("A property named " + property.name + " is already registered");
            }
        }
        entityPropertyByName.put(property.name, property);
        for (String alias : property.getAliases()) {
            entityPropertyByName.put(alias, property);
        }
        entityProperties.add(property);
        return property;
    }

    /**
     * Finds the NavigationProperty registered for the given name.
     *
     * @param propertyName The name to search for.
     * @return The NavigationProperty registered for the given name, or NULL.
     */
    public final NavigationPropertyMain getNavProperty(String name) {
        return navPropertyByName.get(name);
    }

    public final <T extends NavigationPropertyMain> T registerNavProperty(T property) {
        if (navPropertyByName.containsKey(property.getName())) {
            if (navPropertyByName.get(property.getName()) == property) {
                // This exact property is already registered
                return property;
            } else {
                throw new IllegalArgumentException("A property named " + property.getName() + " is already registered");
            }
        }
        navPropertyByName.put(property.getName(), property);
        navProperties.add(property);
        return property;
    }

    public final EntityType registerEntityType(EntityType type) {
        if (typesByName.containsKey(type.entityName)) {
            throw new IllegalArgumentException("An entity type named " + type.entityName + " is already registered");
        }
        typesByName.put(type.entityName, type);
        typesByName.put(type.plural, type);
        types.add(type);
        return type;
    }

    public final EntityType getEntityTypeForName(String typeName) {
        if (typesByName.isEmpty()) {
            initDefaultTypes();
        }
        return typesByName.get(typeName);
    }

    public final Set<EntityType> getEntityTypes() {
        if (types.isEmpty()) {
            initDefaultTypes();
        }
        return types;
    }

    public Class<? extends Id> getIdClass() {
        return idClass;
    }

    public void setIdClass(Class<? extends Id> idClass) {
        this.idClass = idClass;
    }

    public EntityChangedMessage.QueryGenerator getMessageQueryGenerator() {
        return messageQueryGenerator;
    }

    // ToDo: move to extensions
    public synchronized void initDefaultTypes() {
        if (types.contains(DATASTREAM)) {
            return;
        }
        LOGGER.info("Initialising default Types...");
        registerEntityProperty(EP_CREATIONTIME);
        registerEntityProperty(EP_DESCRIPTION);
        registerEntityProperty(EP_DEFINITION);
        registerEntityProperty(EP_ENCODINGTYPE);
        registerEntityProperty(EP_FEATURE);
        registerEntityProperty(EP_ID);
        registerEntityProperty(EP_LOCATION);
        registerEntityProperty(EP_METADATA);
        registerEntityProperty(EP_MULTIOBSERVATIONDATATYPES);
        registerEntityProperty(EP_NAME);
        registerEntityProperty(EP_OBSERVATIONTYPE);
        registerEntityProperty(EP_OBSERVEDAREA);
        registerEntityProperty(EP_PHENOMENONTIME);
        registerEntityProperty(EP_PARAMETERS);
        registerEntityProperty(EP_PROPERTIES);
        registerEntityProperty(EP_RESULT);
        registerEntityProperty(EP_RESULTTIME);
        registerEntityProperty(EP_RESULTQUALITY);
        registerEntityProperty(EP_SELFLINK);
        registerEntityProperty(EP_TIME);
        registerEntityProperty(EP_UNITOFMEASUREMENT);
        registerEntityProperty(EP_UNITOFMEASUREMENTS);
        registerEntityProperty(EP_VALIDTIME);

        registerNavProperty(NP_DATASTREAM);
        registerNavProperty(NP_DATASTREAMS);
        registerNavProperty(NP_MULTIDATASTREAM);
        registerNavProperty(NP_MULTIDATASTREAMS);
        registerNavProperty(NP_FEATUREOFINTEREST);
        registerNavProperty(NP_HISTORICALLOCATIONS);
        registerNavProperty(NP_LOCATION);
        registerNavProperty(NP_LOCATIONS);
        registerNavProperty(NP_OBSERVATIONS);
        registerNavProperty(NP_OBSERVEDPROPERTY);
        registerNavProperty(NP_OBSERVEDPROPERTIES);
        registerNavProperty(NP_SENSOR);
        registerNavProperty(NP_THING);
        registerNavProperty(NP_THINGS);

        registerEntityType(DATASTREAM)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(EP_OBSERVATIONTYPE, true)
                .registerProperty(EP_UNITOFMEASUREMENT, true)
                .registerProperty(EP_OBSERVEDAREA, false)
                .registerProperty(EP_PHENOMENONTIME_DS, false)
                .registerProperty(EP_PROPERTIES, false)
                .registerProperty(EP_RESULTTIME_DS, false)
                .registerProperty(NP_OBSERVEDPROPERTY, true)
                .registerProperty(NP_SENSOR, true)
                .registerProperty(NP_THING, true)
                .registerProperty(NP_OBSERVATIONS, false);
        registerEntityType(MULTI_DATASTREAM)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(EP_OBSERVATIONTYPE, false)
                .registerProperty(EP_MULTIOBSERVATIONDATATYPES, true)
                .registerProperty(EP_UNITOFMEASUREMENTS, true)
                .registerProperty(EP_OBSERVEDAREA, false)
                .registerProperty(EP_PHENOMENONTIME_DS, false)
                .registerProperty(EP_PROPERTIES, false)
                .registerProperty(EP_RESULTTIME_DS, false)
                .registerProperty(NP_OBSERVEDPROPERTIES, false)
                .registerProperty(NP_SENSOR, true)
                .registerProperty(NP_THING, true)
                .registerProperty(NP_OBSERVATIONS, false)
                .addValidator((entity, entityPropertiesOnly) -> {
                    List<UnitOfMeasurement> unitOfMeasurements = entity.getProperty(EP_UNITOFMEASUREMENTS);
                    List<String> multiObservationDataTypes = entity.getProperty(EP_MULTIOBSERVATIONDATATYPES);
                    EntitySet observedProperties = entity.getProperty(NP_OBSERVEDPROPERTIES);
                    if (unitOfMeasurements == null || unitOfMeasurements.size() != multiObservationDataTypes.size()) {
                        throw new IllegalArgumentException("Size of list of unitOfMeasurements (" + unitOfMeasurements.size() + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
                    }
                    if (!entityPropertiesOnly && observedProperties == null || observedProperties.size() != multiObservationDataTypes.size()) {
                        final int opSize = observedProperties == null ? 0 : observedProperties.size();
                        throw new IllegalArgumentException("Size of list of observedProperties (" + opSize + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
                    }
                    String observationType = entity.getProperty(EP_OBSERVATIONTYPE);
                    if (observationType == null || !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
                        throw new IllegalArgumentException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
                    }
                });
        registerEntityType(FEATURE_OF_INTEREST)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(EP_ENCODINGTYPE, true)
                .registerProperty(EP_FEATURE, true)
                .registerProperty(EP_PROPERTIES, false)
                .registerProperty(NP_OBSERVATIONS, false);
        registerEntityType(HISTORICAL_LOCATION)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_TIME, true)
                .registerProperty(NP_THING, true)
                .registerProperty(NP_LOCATIONS, false);
        registerEntityType(LOCATION)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(EP_ENCODINGTYPE, true)
                .registerProperty(EP_LOCATION, true)
                .registerProperty(EP_PROPERTIES, false)
                .registerProperty(NP_HISTORICALLOCATIONS, false)
                .registerProperty(NP_THINGS, false);
        registerEntityType(OBSERVATION)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_PHENOMENONTIME, false)
                .registerProperty(EP_RESULTTIME, false)
                .registerProperty(EP_RESULT, true)
                .registerProperty(EP_RESULTQUALITY, false)
                .registerProperty(EP_VALIDTIME, false)
                .registerProperty(EP_PARAMETERS, false)
                .registerProperty(NP_DATASTREAM, false)
                .registerProperty(NP_MULTIDATASTREAM, false)
                .registerProperty(NP_FEATUREOFINTEREST, false)
                .addValidator((entity, entityPropertiesOnly) -> {
                    if (!entityPropertiesOnly) {
                        Entity datastream = entity.getProperty(NP_DATASTREAM);
                        Entity multiDatastream = entity.getProperty(NP_MULTIDATASTREAM);
                        if (datastream != null && multiDatastream != null) {
                            throw new IllegalArgumentException("Observation can not have both a Datasteam and a MultiDatastream.");
                        }
                        if (datastream == null && multiDatastream == null) {
                            throw new IncompleteEntityException("Observation must have either a Datasteam or a MultiDatastream.");
                        }
                        if (multiDatastream != null) {
                            Object result = entity.getProperty(EP_RESULT);
                            if (!(result instanceof List)) {
                                throw new IllegalArgumentException("Observation in a MultiDatastream must have an Array result.");
                            }
                        }
                    }
                });
        registerEntityType(OBSERVED_PROPERTY)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DEFINITION, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(EP_PROPERTIES, false)
                .registerProperty(NP_DATASTREAMS, false)
                .registerProperty(NP_MULTIDATASTREAMS, false);
        registerEntityType(SENSOR)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(EP_ENCODINGTYPE, true)
                .registerProperty(EP_METADATA, true)
                .registerProperty(EP_PROPERTIES, false)
                .registerProperty(NP_DATASTREAMS, false)
                .registerProperty(NP_MULTIDATASTREAMS, false);
        registerEntityType(THING)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(EP_PROPERTIES, false)
                .registerProperty(NP_LOCATIONS, false)
                .registerProperty(NP_HISTORICALLOCATIONS, false)
                .registerProperty(NP_DATASTREAMS, false)
                .registerProperty(NP_MULTIDATASTREAMS, false);
    }

    public synchronized void initFinalise() {
        initDefaultTypes();
        for (NavigationPropertyMain navProperty : navProperties) {
            navProperty.setEntityType(getEntityTypeForName(navProperty.getName()));
        }
        LOGGER.info("Finalising {} EntityTypes.", types.size());
        for (EntityType type : types) {
            type.init();
        }
    }

}
