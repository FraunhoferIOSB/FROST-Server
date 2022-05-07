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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_UOM;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings.TAG_ENABLE_CORE_MODEL;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DATETIMEOFFSET;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.GeoJsonObject;
import org.jooq.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PluginCoreModel implements PluginRootDocument, PluginModel, LiquibaseUser {

    public static final String NAME_ET_DATASTREAM = "Datastream";
    public static final String NAME_NP_DATASTREAM = "Datastream";
    public static final String NAME_NP_DATASTREAMS = "Datastreams";
    public static final String NAME_ET_FEATUREOFINTEREST = "FeatureOfInterest";
    public static final String NAME_NP_FEATUREOFINTEREST = "FeatureOfInterest";
    public static final String NAME_NP_FEATURESOFINTEREST = "FeaturesOfInterest";
    public static final String NAME_ET_HISTORICALLOCATION = "HistoricalLocation";
    public static final String NAME_NP_HISTORICALLOCATION = "HistoricalLocation";
    public static final String NAME_NP_HISTORICALLOCATIONS = "HistoricalLocations";
    public static final String NAME_ET_LOCATION = "Location";
    public static final String NAME_NP_LOCATION = "Location";
    public static final String NAME_NP_LOCATIONS = "Locations";
    public static final String NAME_ET_OBSERVATION = "Observation";
    public static final String NAME_NP_OBSERVATION = "Observation";
    public static final String NAME_NP_OBSERVATIONS = "Observations";
    public static final String NAME_ET_OBSERVEDPROPERTY = "ObservedProperty";
    public static final String NAME_NP_OBSERVEDPROPERTY = "ObservedProperty";
    public static final String NAME_NP_OBSERVEDPROPERTIES = "ObservedProperties";
    public static final String NAME_ET_SENSOR = "Sensor";
    public static final String NAME_NP_SENSOR = "Sensor";
    public static final String NAME_NP_SENSORS = "Sensors";
    public static final String NAME_ET_THING = "Thing";
    public static final String NAME_NP_THING = "Thing";
    public static final String NAME_NP_THINGS = "Things";

    public static final String NAME_DEFINITION = "definition";
    public static final String NAME_EP_CREATIONTIME = "creationTime";
    public static final String NAME_EP_DESCRIPTION = "description";
    public static final String NAME_EP_DEFINITION = NAME_DEFINITION;
    public static final String NAME_EP_FEATURE = "feature";
    public static final String NAME_EP_LOCATION = "location";
    public static final String NAME_EP_METADATA = "metadata";
    public static final String NAME_EP_NAME = "name";
    public static final String NAME_EP_OBSERVATIONTYPE = "observationType";
    public static final String NAME_EP_OBSERVEDAREA = "observedArea";
    public static final String NAME_EP_PARAMETERS = "parameters";
    public static final String NAME_EP_PHENOMENONTIME = "phenomenonTime";
    public static final String NAME_EP_RESULT = "result";
    public static final String NAME_EP_RESULTTIME = "resultTime";
    public static final String NAME_EP_RESULTQUALITY = "resultQuality";
    public static final String NAME_EP_TIME = "time";
    public static final String NAME_EP_UNITOFMEASUREMENT = "unitOfMeasurement";
    public static final String NAME_EP_VALIDTIME = "validTime";

    public static final String NAME_LINKTABLE_THINGS_LOCATIONS = "THINGS_LOCATIONS";
    public static final String NAME_COL_LT_THINGID = "THING_ID";
    public static final String NAME_COL_LT_LOCATIONID = "LOCATION_ID";

    public static final String NAME_LIQUIBASE_DATASTREAM = NAME_ET_DATASTREAM;
    private static final String NAME_LIQUIBASE_THING = "Thing";
    private static final String NAME_LIQUIBASE_SENSOR = "Sensor";
    private static final String NAME_LIQUIBASE_OBSERVATION = "Observation";
    private static final String NAME_LIQUIBASE_OBS_PROP = "ObsProp";
    private static final String NAME_LIQUIBASE_LOCATION = "Location";
    private static final String NAME_LIQUIBASE_HIST_LOCATION = "HistLocation";
    private static final String NAME_LIQUIBASE_FEATURE = "Feature";

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/plugincoremodel/tables.xml";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginCoreModel.class.getName());

    private TypeComplex eptUom;

    public final EntityPropertyMain<TimeInstant> epCreationTime = new EntityPropertyMain<>(NAME_EP_CREATIONTIME, EDM_DATETIMEOFFSET);
    public final EntityPropertyMain<String> epDescription = new EntityPropertyMain<>(NAME_EP_DESCRIPTION, EDM_STRING);
    public final EntityPropertyMain<String> epDefinition = new EntityPropertyMain<>(NAME_EP_DEFINITION, EDM_STRING);
    public final EntityPropertyMain<Object> epFeature = new EntityPropertyMain<>(NAME_EP_FEATURE, TypeSimpleCustom.STA_GEOJSON, true, false);
    public final EntityPropertyMain<Object> epLocation = new EntityPropertyMain<>(NAME_EP_LOCATION, TypeSimpleCustom.STA_GEOJSON, true, false);
    public final EntityPropertyMain<String> epMetadata = new EntityPropertyMain<>(NAME_EP_METADATA, EDM_STRING);
    public final EntityPropertyMain<String> epName = new EntityPropertyMain<>(NAME_EP_NAME, EDM_STRING);
    public final EntityPropertyMain<String> epObservationType = new EntityPropertyMain<>(NAME_EP_OBSERVATIONTYPE, EDM_STRING);
    public final EntityPropertyMain<GeoJsonObject> epObservedArea = new EntityPropertyMain<>(NAME_EP_OBSERVEDAREA, TypeSimplePrimitive.EDM_GEOMETRY);
    public final EntityPropertyMain<TimeValue> epPhenomenonTime = new EntityPropertyMain<>(NAME_EP_PHENOMENONTIME, TypeComplex.STA_TIMEVALUE, true, false);
    public final EntityPropertyMain<TimeInterval> epPhenomenonTimeDs = new EntityPropertyMain<>(NAME_EP_PHENOMENONTIME, TypeComplex.STA_TIMEINTERVAL, true, false);
    public final EntityPropertyMain<Map<String, Object>> epParameters = new EntityPropertyMain<>(NAME_EP_PARAMETERS, TypeComplex.STA_MAP, true, false);
    public final EntityPropertyMain<Object> epResult = new EntityPropertyMain<>(NAME_EP_RESULT, TypeSimplePrimitive.EDM_UNTYPED, true, true);
    public final EntityPropertyMain<TimeInstant> epResultTime = new EntityPropertyMain<>(NAME_EP_RESULTTIME, EDM_DATETIMEOFFSET, false, true);
    public final EntityPropertyMain<TimeInterval> epResultTimeDs = new EntityPropertyMain<>(NAME_EP_RESULTTIME, TypeComplex.STA_TIMEINTERVAL, false, false);
    public final EntityPropertyMain<Object> epResultQuality = new EntityPropertyMain<>(NAME_EP_RESULTQUALITY, TypeComplex.STA_OBJECT, true, false);
    public final EntityPropertyMain<TimeInstant> epTime = new EntityPropertyMain<>(NAME_EP_TIME, EDM_DATETIMEOFFSET);
    private EntityPropertyMain<UnitOfMeasurement> epUnitOfMeasurement;
    public final EntityPropertyMain<TimeInterval> epValidTime = new EntityPropertyMain<>(NAME_EP_VALIDTIME, TypeComplex.STA_TIMEINTERVAL);

    private EntityPropertyMain<?> epIdDatastream;
    private EntityPropertyMain<?> epIdFeature;
    private EntityPropertyMain<?> epIdHistLocation;
    private EntityPropertyMain<?> epIdLocation;
    private EntityPropertyMain<?> epIdObsProp;
    private EntityPropertyMain<?> epIdObservation;
    private EntityPropertyMain<?> epIdSensor;
    private EntityPropertyMain<?> epIdThing;

    public final NavigationPropertyEntity npDatastreamObservation = new NavigationPropertyEntity(NAME_NP_DATASTREAM);
    public final NavigationPropertyEntitySet npDatastreamsThing = new NavigationPropertyEntitySet(NAME_NP_DATASTREAMS);
    public final NavigationPropertyEntitySet npDatastreamsSensor = new NavigationPropertyEntitySet(NAME_NP_DATASTREAMS);
    public final NavigationPropertyEntitySet npDatastreamsObsProp = new NavigationPropertyEntitySet(NAME_NP_DATASTREAMS);

    public final NavigationPropertyEntity npFeatureOfInterestObservation = new NavigationPropertyEntity(NAME_NP_FEATUREOFINTEREST);

    public final NavigationPropertyEntitySet npHistoricalLocationsThing = new NavigationPropertyEntitySet(NAME_NP_HISTORICALLOCATIONS);
    public final NavigationPropertyEntitySet npHistoricalLocationsLocation = new NavigationPropertyEntitySet(NAME_NP_HISTORICALLOCATIONS);

    public final NavigationPropertyEntitySet npLocationsThing = new NavigationPropertyEntitySet(NAME_NP_LOCATIONS);
    public final NavigationPropertyEntitySet npLocationsHistLoc = new NavigationPropertyEntitySet(NAME_NP_LOCATIONS, npHistoricalLocationsLocation);

    public final NavigationPropertyEntitySet npObservationsDatastream = new NavigationPropertyEntitySet(NAME_NP_OBSERVATIONS, npDatastreamObservation);
    public final NavigationPropertyEntitySet npObservationsFeature = new NavigationPropertyEntitySet(NAME_NP_OBSERVATIONS, npFeatureOfInterestObservation);

    public final NavigationPropertyEntity npObservedPropertyDatastream = new NavigationPropertyEntity(NAME_NP_OBSERVEDPROPERTY, npDatastreamsObsProp);

    public final NavigationPropertyEntity npSensorDatastream = new NavigationPropertyEntity(NAME_NP_SENSOR, npDatastreamsSensor);

    public final NavigationPropertyEntity npThingDatasteam = new NavigationPropertyEntity(NAME_NP_THING, npDatastreamsThing);
    public final NavigationPropertyEntity npThingHistLoc = new NavigationPropertyEntity(NAME_NP_THING, npHistoricalLocationsThing);
    public final NavigationPropertyEntitySet npThingsLocation = new NavigationPropertyEntitySet(NAME_NP_THINGS, npLocationsThing);

    public final EntityType etThing = new EntityType(NAME_NP_THING, NAME_NP_THINGS);
    public final EntityType etSensor = new EntityType(NAME_NP_SENSOR, NAME_NP_SENSORS);
    public final EntityType etObservedProperty = new EntityType(NAME_NP_OBSERVEDPROPERTY, NAME_NP_OBSERVEDPROPERTIES);
    public final EntityType etObservation = new EntityType(NAME_NP_OBSERVATION, NAME_NP_OBSERVATIONS)
            .addCreateValidator("CM-ObsPhenTime", (entity, entityPropertiesOnly) -> {
                if (entity.getProperty(epPhenomenonTime) == null) {
                    entity.setProperty(epPhenomenonTime, new TimeValue(TimeInstant.now()));
                }
            });
    public final EntityType etLocation = new EntityType(NAME_NP_LOCATION, NAME_NP_LOCATIONS);
    public final EntityType etHistoricalLocation = new EntityType(NAME_NP_HISTORICALLOCATION, NAME_NP_HISTORICALLOCATIONS);
    public final EntityType etFeatureOfInterest = new EntityType(NAME_NP_FEATUREOFINTEREST, NAME_NP_FEATURESOFINTEREST);
    public final EntityType etDatastream = new EntityType(NAME_NP_DATASTREAM, NAME_NP_DATASTREAMS);

    private static final List<String> REQUIREMENTS_CORE_MODEL = Arrays.asList(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel");

    private CoreSettings settings;
    private CoreModelSettings modelSettings;
    private boolean enabled;
    private boolean fullyInitialised;

    public PluginCoreModel() {
        LOGGER.info("Creating new Core Model Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_CORE_MODEL, CoreModelSettings.class);
        if (enabled) {
            modelSettings = new CoreModelSettings(settings);
            settings.getPluginManager().registerPlugin(this);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isFullyInitialised() {
        return fullyInitialised;
    }

    @Override
    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        Map<String, Object> serverSettings = (Map<String, Object>) result.get(Service.KEY_SERVER_SETTINGS);
        if (serverSettings == null) {
            // Nothing to add to.
            return;
        }
        Set<String> extensionList = (Set<String>) serverSettings.get(Service.KEY_CONFORMANCE_LIST);
        extensionList.addAll(REQUIREMENTS_CORE_MODEL);
    }

    @Override
    public void registerEntityTypes() {
        LOGGER.info("Initialising Core Model Types...");
        ModelRegistry mr = settings.getModelRegistry();

        eptUom = new TypeComplex("UnitOfMeasurement", "The Unit Of Measurement Type", TYPE_REFERENCE_UOM)
                .addProperty("name", EDM_STRING, false)
                .addProperty("symbol", EDM_STRING, false)
                .addProperty(NAME_DEFINITION, EDM_STRING, false);
        mr.registerPropertyType(eptUom)
                .registerPropertyType(TypeSimpleCustom.STA_GEOJSON)
                .registerPropertyType(TypeComplex.STA_OBJECT)
                .registerPropertyType(TypeComplex.STA_MAP)
                .registerPropertyType(TypeComplex.STA_TIMEINTERVAL)
                .registerPropertyType(TypeComplex.STA_TIMEVALUE)
                .registerEntityType(etDatastream)
                .registerEntityType(etFeatureOfInterest)
                .registerEntityType(etHistoricalLocation)
                .registerEntityType(etLocation)
                .registerEntityType(etObservation)
                .registerEntityType(etObservedProperty)
                .registerEntityType(etSensor)
                .registerEntityType(etThing);
        epIdDatastream = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeDatastream), "id");
        epIdFeature = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeFeature), "id");
        epIdHistLocation = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeHistLoc), "id");
        epIdLocation = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeLocation), "id");
        epIdObsProp = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeObsProp), "id");
        epIdObservation = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeObservation), "id");
        epIdSensor = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeSensor), "id");
        epIdThing = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeThing), "id");
        epUnitOfMeasurement = new EntityPropertyMain<>(NAME_EP_UNITOFMEASUREMENT, eptUom, true, false);
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        LOGGER.info("Linking Core Model Types...");
        etDatastream
                .registerProperty(epIdDatastream, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(epObservationType, true)
                .registerProperty(epUnitOfMeasurement, true)
                .registerProperty(epObservedArea, false)
                .registerProperty(epPhenomenonTimeDs, false)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(epResultTimeDs, false)
                .registerProperty(npObservedPropertyDatastream, true)
                .registerProperty(npSensorDatastream, true)
                .registerProperty(npThingDatasteam, true)
                .registerProperty(npObservationsDatastream, false);
        etFeatureOfInterest
                .registerProperty(epIdFeature, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(epFeature, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npObservationsFeature, false);
        etHistoricalLocation
                .registerProperty(epIdHistLocation, false)
                .registerProperty(epTime, true)
                .registerProperty(npThingHistLoc, true)
                .registerProperty(npLocationsHistLoc, false);
        etLocation
                .registerProperty(epIdLocation, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(epLocation, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npHistoricalLocationsLocation, false)
                .registerProperty(npThingsLocation, false);
        etObservation
                .registerProperty(epIdObservation, false)
                .registerProperty(epPhenomenonTime, false)
                .registerProperty(epResultTime, false)
                .registerProperty(epResult, true)
                .registerProperty(epResultQuality, false)
                .registerProperty(epValidTime, false)
                .registerProperty(epParameters, false)
                .registerProperty(npDatastreamObservation, true)
                .registerProperty(npFeatureOfInterestObservation, false);
        etObservedProperty
                .registerProperty(epIdObsProp, false)
                .registerProperty(epName, true)
                .registerProperty(epDefinition, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npDatastreamsObsProp, false);
        etSensor
                .registerProperty(epIdSensor, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(epMetadata, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npDatastreamsSensor, false);
        etThing
                .registerProperty(epIdThing, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npLocationsThing, false)
                .registerProperty(npHistoricalLocationsThing, false)
                .registerProperty(npDatastreamsThing, false);

        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            TableCollection tableCollection = ppm.getTableCollection();
            final DataType dataTypeDstr = ppm.getDataTypeFor(modelSettings.idTypeDatastream);
            final DataType dataTypeFeat = ppm.getDataTypeFor(modelSettings.idTypeFeature);
            final DataType dataTypeHist = ppm.getDataTypeFor(modelSettings.idTypeHistLoc);
            final DataType dataTypeLctn = ppm.getDataTypeFor(modelSettings.idTypeLocation);
            final DataType dataTypeObPr = ppm.getDataTypeFor(modelSettings.idTypeObsProp);
            final DataType dataTypeObsr = ppm.getDataTypeFor(modelSettings.idTypeObservation);
            final DataType dataTypeSnsr = ppm.getDataTypeFor(modelSettings.idTypeSensor);
            final DataType dataTypeThng = ppm.getDataTypeFor(modelSettings.idTypeThing);
            tableCollection.registerTable(etDatastream, new TableImpDatastreams(dataTypeDstr, dataTypeObPr, dataTypeSnsr, dataTypeThng, this));
            tableCollection.registerTable(etFeatureOfInterest, new TableImpFeatures(dataTypeFeat, this));
            tableCollection.registerTable(etHistoricalLocation, new TableImpHistLocations(dataTypeHist, dataTypeThng, this));
            tableCollection.registerTable(etLocation, new TableImpLocations(dataTypeLctn, this));
            tableCollection.registerTable(new TableImpLocationsHistLocations(dataTypeLctn, dataTypeHist));
            tableCollection.registerTable(etObservation, new TableImpObservations(dataTypeObsr, dataTypeDstr, dataTypeFeat, this));
            tableCollection.registerTable(etObservedProperty, new TableImpObsProperties(dataTypeObPr, this));
            tableCollection.registerTable(etSensor, new TableImpSensors(dataTypeSnsr, this));
            tableCollection.registerTable(etThing, new TableImpThings(dataTypeThng, this));
            tableCollection.registerTable(new TableImpThingsLocations(dataTypeThng, dataTypeLctn));
        }
        fullyInitialised = true;
        return true;
    }

    public Map<String, Object> createLiqibaseParams(PostgresPersistenceManager ppm, Map<String, Object> target) {
        if (target == null) {
            target = new LinkedHashMap<>();
        }
        ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_DATASTREAM, modelSettings.idTypeDatastream);
        ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_FEATURE, modelSettings.idTypeFeature);
        ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_HIST_LOCATION, modelSettings.idTypeHistLoc);
        ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_LOCATION, modelSettings.idTypeLocation);
        ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_OBS_PROP, modelSettings.idTypeObsProp);
        ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_OBSERVATION, modelSettings.idTypeObservation);
        ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_SENSOR, modelSettings.idTypeSensor);
        ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_THING, modelSettings.idTypeThing);

        return target;
    }

    @Override
    public String checkForUpgrades() {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof PostgresPersistenceManager) {
                PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
                return ppm.checkForUpgrades(LIQUIBASE_CHANGELOG_FILENAME, createLiqibaseParams(ppm, null));
            }
            return "Unknown persistence manager class";
        }
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof PostgresPersistenceManager) {
                PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
                return ppm.doUpgrades(LIQUIBASE_CHANGELOG_FILENAME, createLiqibaseParams(ppm, null), out);
            }
            out.append("Unknown persistence manager class");
            return false;
        }
    }

    /**
     * @return the entity property type for Unit Of Measurement
     */
    public TypeComplex getEptUom() {
        return eptUom;
    }

    /**
     * @return the entity property for UnitOfMeasurement
     */
    public EntityPropertyMain<UnitOfMeasurement> getEpUnitOfMeasurement() {
        return epUnitOfMeasurement;
    }

    /**
     * @return the epIdDatastream
     */
    public EntityPropertyMain<?> getEpIdDatastream() {
        return epIdDatastream;
    }

    /**
     * @return the epIdFeature
     */
    public EntityPropertyMain<?> getEpIdFeature() {
        return epIdFeature;
    }

    /**
     * @return the epIdHistLocation
     */
    public EntityPropertyMain<?> getEpIdHistLocation() {
        return epIdHistLocation;
    }

    /**
     * @return the epIdLocation
     */
    public EntityPropertyMain<?> getEpIdLocation() {
        return epIdLocation;
    }

    /**
     * @return the epIdObsProp
     */
    public EntityPropertyMain<?> getEpIdObsProp() {
        return epIdObsProp;
    }

    /**
     * @return the epIdObservation
     */
    public EntityPropertyMain<?> getEpIdObservation() {
        return epIdObservation;
    }

    /**
     * @return the epIdSensor
     */
    public EntityPropertyMain<?> getEpIdSensor() {
        return epIdSensor;
    }

    /**
     * @return the epIdThing
     */
    public EntityPropertyMain<?> getEpIdThing() {
        return epIdThing;
    }

}
