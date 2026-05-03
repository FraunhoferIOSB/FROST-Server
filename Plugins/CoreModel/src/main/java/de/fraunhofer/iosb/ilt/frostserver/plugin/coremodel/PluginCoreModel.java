/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper.CHANGE_SET_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings.TAG_ENABLE_CORE_MODEL;
import static de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain.CUSTOM_PROPS;
import static de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain.SERIALISE_NULLS;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.NULLABLE;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.EP_ENCODINGTYPE;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.STA_LOCATION;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.STA_MAP;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.STA_TIMEINTERVAL;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.STA_TIMEVALUE;
import static de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties.TYPE_UOM;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_DATETIMEOFFSET;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_GEOMETRY;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_UNTYPED;
import static de.fraunhofer.iosb.ilt.frostserver.service.Service.KEY_SERVER_SETTINGS;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValueImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import de.fraunhofer.iosb.ilt.settings.Settings;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jooq.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.TreeNode;

/**
 * The core SensorThings v1.1 data model plugin.
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

    public final EntityPropertyMain<TimeInstant> epCreationTime = new EntityPropertyMain<>(NAME_EP_CREATIONTIME, EDM_DATETIMEOFFSET);
    public final EntityPropertyMain<String> epDescription = new EntityPropertyMain<>(NAME_EP_DESCRIPTION, EDM_STRING, REQUIRED);
    public final EntityPropertyMain<String> epDefinition = new EntityPropertyMain<>(NAME_EP_DEFINITION, EDM_STRING, REQUIRED);
    public final EntityPropertyMain<Object> epFeature = new EntityPropertyMain<>(NAME_EP_FEATURE, STA_LOCATION, REQUIRED, CUSTOM_PROPS);
    public final EntityPropertyMain<Object> epLocation = new EntityPropertyMain<>(NAME_EP_LOCATION, STA_LOCATION, REQUIRED, CUSTOM_PROPS);
    public final EntityPropertyMain<Object> epMetadata = new EntityPropertyMain<>(NAME_EP_METADATA, EDM_UNTYPED, REQUIRED, CUSTOM_PROPS);
    public final EntityPropertyMain<String> epName = new EntityPropertyMain<>(NAME_EP_NAME, EDM_STRING, REQUIRED);
    public final EntityPropertyMain<String> epObservationType = new EntityPropertyMain<>(NAME_EP_OBSERVATIONTYPE, EDM_STRING, REQUIRED);
    public final EntityPropertyMain<Object> epObservedArea = new EntityPropertyMain<>(NAME_EP_OBSERVEDAREA, EDM_GEOMETRY, NULLABLE, CUSTOM_PROPS);
    public final EntityPropertyMain<TimeValue> epPhenomenonTime = new EntityPropertyMain<>(NAME_EP_PHENOMENONTIME, STA_TIMEVALUE);
    public final EntityPropertyMain<TimeInterval> epPhenomenonTimeDs = new EntityPropertyMain<>(NAME_EP_PHENOMENONTIME, STA_TIMEINTERVAL, NULLABLE);
    public final EntityPropertyMain<TreeNode> epParameters = new EntityPropertyMain<>(NAME_EP_PARAMETERS, STA_MAP, NULLABLE, CUSTOM_PROPS);
    public final EntityPropertyMain<Object> epResult = new EntityPropertyMain<>(NAME_EP_RESULT, EDM_UNTYPED, REQUIRED, NULLABLE, CUSTOM_PROPS, SERIALISE_NULLS);
    public final EntityPropertyMain<TimeInstant> epResultTime = new EntityPropertyMain<>(NAME_EP_RESULTTIME, EDM_DATETIMEOFFSET, NULLABLE, SERIALISE_NULLS);
    public final EntityPropertyMain<TimeInterval> epResultTimeDs = new EntityPropertyMain<>(NAME_EP_RESULTTIME, STA_TIMEINTERVAL, NULLABLE);
    public final EntityPropertyMain<Object> epResultQuality = new EntityPropertyMain<>(NAME_EP_RESULTQUALITY, EDM_UNTYPED, NULLABLE, CUSTOM_PROPS);
    public final EntityPropertyMain<TimeInstant> epTime = new EntityPropertyMain<>(NAME_EP_TIME, EDM_DATETIMEOFFSET, REQUIRED);
    public final EntityPropertyMain<ComplexValueImpl> epUnitOfMeasurement = new EntityPropertyMain<>(NAME_EP_UNITOFMEASUREMENT, TYPE_UOM, REQUIRED);
    public final EntityPropertyMain<TimeInterval> epValidTime = new EntityPropertyMain<>(NAME_EP_VALIDTIME, STA_TIMEINTERVAL);

    private EntityPropertyMain<?> epIdDatastream;
    private EntityPropertyMain<?> epIdFeature;
    private EntityPropertyMain<?> epIdHistLocation;
    private EntityPropertyMain<?> epIdLocation;
    private EntityPropertyMain<?> epIdObsProp;
    private EntityPropertyMain<?> epIdObservation;
    private EntityPropertyMain<?> epIdSensor;
    private EntityPropertyMain<?> epIdThing;

    public final NavigationPropertyEntity npDatastreamObservation = new NavigationPropertyEntity(NAME_NP_DATASTREAM, -10, REQUIRED);
    public final NavigationPropertyEntitySet npDatastreamsThing = new NavigationPropertyEntitySet(NAME_NP_DATASTREAMS, 10);
    public final NavigationPropertyEntitySet npDatastreamsSensor = new NavigationPropertyEntitySet(NAME_NP_DATASTREAMS);
    public final NavigationPropertyEntitySet npDatastreamsObsProp = new NavigationPropertyEntitySet(NAME_NP_DATASTREAMS);

    public final NavigationPropertyEntity npFeatureOfInterestObservation = new NavigationPropertyEntity(NAME_NP_FEATUREOFINTEREST);

    public final NavigationPropertyEntitySet npHistoricalLocationsThing = new NavigationPropertyEntitySet(NAME_NP_HISTORICALLOCATIONS);
    public final NavigationPropertyEntitySet npHistoricalLocationsLocation = new NavigationPropertyEntitySet(NAME_NP_HISTORICALLOCATIONS);

    public final NavigationPropertyEntitySet npLocationsThing = new NavigationPropertyEntitySet(NAME_NP_LOCATIONS);
    public final NavigationPropertyEntitySet npLocationsHistLoc = new NavigationPropertyEntitySet(NAME_NP_LOCATIONS, npHistoricalLocationsLocation);

    public final NavigationPropertyEntitySet npObservationsDatastream = new NavigationPropertyEntitySet(NAME_NP_OBSERVATIONS, npDatastreamObservation);
    public final NavigationPropertyEntitySet npObservationsFeature = new NavigationPropertyEntitySet(NAME_NP_OBSERVATIONS, npFeatureOfInterestObservation);

    public final NavigationPropertyEntity npObservedPropertyDatastream = new NavigationPropertyEntity(NAME_NP_OBSERVEDPROPERTY, npDatastreamsObsProp, REQUIRED);

    public final NavigationPropertyEntity npSensorDatastream = new NavigationPropertyEntity(NAME_NP_SENSOR, npDatastreamsSensor, REQUIRED);

    public final NavigationPropertyEntity npThingDatasteam = new NavigationPropertyEntity(NAME_NP_THING, npDatastreamsThing, REQUIRED);
    public final NavigationPropertyEntity npThingHistLoc = new NavigationPropertyEntity(NAME_NP_THING, npHistoricalLocationsThing, REQUIRED);
    public final NavigationPropertyEntitySet npThingsLocation = new NavigationPropertyEntitySet(NAME_NP_THINGS, npLocationsThing);

    public final EntityType etThing = new EntityType(NAME_NP_THING, NAME_NP_THINGS);
    public final EntityType etSensor = new EntityType(NAME_NP_SENSOR, NAME_NP_SENSORS);
    public final EntityType etObservedProperty = new EntityType(NAME_NP_OBSERVEDPROPERTY, NAME_NP_OBSERVEDPROPERTIES);
    public final EntityType etObservation = new EntityType(NAME_NP_OBSERVATION, NAME_NP_OBSERVATIONS)
            .addCreateValidator("CM-ObsPhenTime", new PhenomenonTimeValidator());
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

    @Override
    public InitResult init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_CORE_MODEL, CoreModelSettings.class);
        if (enabled) {
            modelSettings = new CoreModelSettings(settings);
            settings.getPluginManager().registerPlugin(this);
            epPhenomenonTimeDs.setReadOnly(!modelSettings.dsPropsEditable);
            epResultTimeDs.setReadOnly(!modelSettings.dsPropsEditable);
            epObservedArea.setReadOnly(!modelSettings.dsPropsEditable);
        }
        return InitResult.INIT_OK;
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
        Version version = request.getVersion();
        if (version == PluginCoreService.V_1_0) {
            return;
        }
        Map<String, Object> serverSettings = (Map<String, Object>) result.computeIfAbsent(KEY_SERVER_SETTINGS, t -> new LinkedHashMap<>());
        Set<String> extensionList = (Set<String>) serverSettings.computeIfAbsent(Service.KEY_CONFORMANCE_LIST, t -> new TreeSet<>());
        extensionList.addAll(REQUIREMENTS_CORE_MODEL);
    }

    @Override
    public void registerEntityTypes() {
        LOGGER.info("Initialising Core Model Types...");
        ModelRegistry mr = settings.getModelRegistry();

        mr.registerPropertyType(StandardProperties.TYPE_UOM)
                .registerPropertyType(STA_LOCATION)
                .registerPropertyType(STA_MAP)
                .registerPropertyType(STA_TIMEINTERVAL)
                .registerPropertyType(STA_TIMEVALUE)
                .registerEntityType(etDatastream)
                .registerEntityType(etFeatureOfInterest)
                .registerEntityType(etHistoricalLocation)
                .registerEntityType(etLocation)
                .registerEntityType(etObservation)
                .registerEntityType(etObservedProperty)
                .registerEntityType(etSensor)
                .registerEntityType(etThing);
        epIdDatastream = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeDatastream)).setAliases("id");
        epIdFeature = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeFeature)).setAliases("id");
        epIdHistLocation = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeHistLoc)).setAliases("id");
        epIdLocation = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeLocation)).setAliases("id");
        epIdObsProp = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeObsProp)).setAliases("id");
        epIdObservation = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeObservation)).setAliases("id");
        epIdSensor = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeSensor)).setAliases("id");
        epIdThing = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeThing)).setAliases("id");
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        LOGGER.info("Linking Core Model Types...");
        etDatastream
                .registerProperty(epIdDatastream)
                .registerProperty(epName)
                .registerProperty(epDescription)
                .registerProperty(epObservationType)
                .registerProperty(epUnitOfMeasurement)
                .registerProperty(epObservedArea)
                .registerProperty(epPhenomenonTimeDs)
                .registerProperty(EP_PROPERTIES)
                .registerProperty(epResultTimeDs)
                .registerProperty(npObservedPropertyDatastream)
                .registerProperty(npSensorDatastream)
                .registerProperty(npThingDatasteam)
                .registerProperty(npObservationsDatastream);
        etFeatureOfInterest
                .registerProperty(epIdFeature)
                .registerProperty(epName)
                .registerProperty(epDescription)
                .registerProperty(EP_ENCODINGTYPE)
                .registerProperty(epFeature)
                .registerProperty(EP_PROPERTIES)
                .registerProperty(npObservationsFeature);
        etHistoricalLocation
                .registerProperty(epIdHistLocation)
                .registerProperty(epTime)
                .registerProperty(npThingHistLoc)
                .registerProperty(npLocationsHistLoc);
        etLocation
                .registerProperty(epIdLocation)
                .registerProperty(epName)
                .registerProperty(epDescription)
                .registerProperty(EP_ENCODINGTYPE)
                .registerProperty(epLocation)
                .registerProperty(EP_PROPERTIES)
                .registerProperty(npHistoricalLocationsLocation)
                .registerProperty(npThingsLocation);
        etObservation
                .registerProperty(epIdObservation)
                .registerProperty(epPhenomenonTime)
                .registerProperty(epResultTime)
                .registerProperty(epResult)
                .registerProperty(epResultQuality)
                .registerProperty(epValidTime)
                .registerProperty(epParameters)
                .registerProperty(npDatastreamObservation)
                .registerProperty(npFeatureOfInterestObservation)
                .addOrderByDefault(new OrderBy(new Path(epPhenomenonTime)));
        etObservedProperty
                .registerProperty(epIdObsProp)
                .registerProperty(epName)
                .registerProperty(epDefinition)
                .registerProperty(epDescription)
                .registerProperty(EP_PROPERTIES)
                .registerProperty(npDatastreamsObsProp);
        etSensor
                .registerProperty(epIdSensor)
                .registerProperty(epName)
                .registerProperty(epDescription)
                .registerProperty(EP_ENCODINGTYPE)
                .registerProperty(epMetadata)
                .registerProperty(EP_PROPERTIES)
                .registerProperty(npDatastreamsSensor);
        etThing
                .registerProperty(epIdThing)
                .registerProperty(epName)
                .registerProperty(epDescription)
                .registerProperty(EP_PROPERTIES)
                .registerProperty(npLocationsThing)
                .registerProperty(npHistoricalLocationsThing)
                .registerProperty(npDatastreamsThing);

        if (pm instanceof JooqPersistenceManager ppm) {
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
            tableCollection.registerTable(etLocation, new TableImpLocations(dataTypeLctn, dataTypeFeat, this));
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

    @Override
    public Map<String, Object> createLiqibaseParams(PersistenceManager pm, Map<String, Object> target) {
        if (pm instanceof JooqPersistenceManager ppm) {
            ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_DATASTREAM, modelSettings.idTypeDatastream);
            ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_FEATURE, modelSettings.idTypeFeature);
            ppm.generateLiquibaseVariables(target, NAME_ET_FEATUREOFINTEREST, modelSettings.idTypeFeature);
            ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_HIST_LOCATION, modelSettings.idTypeHistLoc);
            ppm.generateLiquibaseVariables(target, NAME_ET_HISTORICALLOCATION, modelSettings.idTypeHistLoc);
            ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_LOCATION, modelSettings.idTypeLocation);
            ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_OBS_PROP, modelSettings.idTypeObsProp);
            ppm.generateLiquibaseVariables(target, NAME_ET_OBSERVEDPROPERTY, modelSettings.idTypeObsProp);
            ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_OBSERVATION, modelSettings.idTypeObservation);
            ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_SENSOR, modelSettings.idTypeSensor);
            ppm.generateLiquibaseVariables(target, NAME_LIQUIBASE_THING, modelSettings.idTypeThing);
        }
        return target;
    }

    @Override
    public String checkForUpgrades(Map<String, Object> liquibaseParams) {
        liquibaseParams.put(CHANGE_SET_NAME, "Plugin.CoreModel");
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof JooqPersistenceManager jpm) {
                return jpm.checkForUpgrades(LIQUIBASE_CHANGELOG_FILENAME, liquibaseParams);
            }
            return "Unknown persistence manager class";
        }
    }

    @Override
    public boolean doUpgrades(Writer out, Map<String, Object> liquibaseParams) throws UpgradeFailedException, IOException {
        liquibaseParams.put(CHANGE_SET_NAME, "Plugin.CoreModel");
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof JooqPersistenceManager jpm) {
                return jpm.doUpgrades(LIQUIBASE_CHANGELOG_FILENAME, liquibaseParams, out);
            }
            out.append("Unknown persistence manager class");
            return false;
        }
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
