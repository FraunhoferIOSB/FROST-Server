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
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_GEOJSONOBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_MAP;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_OBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEINSTANT;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEINTERVAL;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_TIMEVALUE;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_UOM;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
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
public class PluginCoreModel implements PluginRootDocument, PluginModel, ConfigDefaults, LiquibaseUser {

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

    public static final String NAME_EP_CREATIONTIME = "creationTime";
    public static final String NAME_EP_DESCRIPTION = "description";
    public static final String NAME_EP_DEFINITION = "definition";
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

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/plugincoremodel/tables";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginCoreModel.class.getName());

    public final EntityPropertyMain<TimeInstant> epCreationTime = new EntityPropertyMain<>(NAME_EP_CREATIONTIME, TYPE_REFERENCE_TIMEINSTANT);
    public final EntityPropertyMain<String> epDescription = new EntityPropertyMain<>(NAME_EP_DESCRIPTION, TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<String> epDefinition = new EntityPropertyMain<>(NAME_EP_DEFINITION, TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<Object> epFeature = new EntityPropertyMain<>(NAME_EP_FEATURE, null, true, false);
    public final EntityPropertyMain<Object> epLocation = new EntityPropertyMain<>(NAME_EP_LOCATION, null, true, false);
    public final EntityPropertyMain<String> epMetadata = new EntityPropertyMain<>(NAME_EP_METADATA, TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<String> epName = new EntityPropertyMain<>(NAME_EP_NAME, TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<String> epObservationType = new EntityPropertyMain<>(NAME_EP_OBSERVATIONTYPE, TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<GeoJsonObject> epObservedArea = new EntityPropertyMain<>(NAME_EP_OBSERVEDAREA, TYPE_REFERENCE_GEOJSONOBJECT);
    public final EntityPropertyMain<TimeValue> epPhenomenonTime = new EntityPropertyMain<>(NAME_EP_PHENOMENONTIME, TYPE_REFERENCE_TIMEVALUE);
    public final EntityPropertyMain<TimeInterval> epPhenomenonTimeDs = new EntityPropertyMain<>(NAME_EP_PHENOMENONTIME, TYPE_REFERENCE_TIMEINTERVAL);
    public final EntityPropertyMain<Map<String, Object>> epParameters = new EntityPropertyMain<>(NAME_EP_PARAMETERS, TYPE_REFERENCE_MAP, true, false);
    public final EntityPropertyMain<Object> epResult = new EntityPropertyMain<>(NAME_EP_RESULT, TYPE_REFERENCE_OBJECT, true, true);
    public final EntityPropertyMain<TimeInstant> epResultTime = new EntityPropertyMain<>(NAME_EP_RESULTTIME, TYPE_REFERENCE_TIMEINSTANT, false, true);
    public final EntityPropertyMain<TimeInterval> epResultTimeDs = new EntityPropertyMain<>(NAME_EP_RESULTTIME, TYPE_REFERENCE_TIMEINTERVAL, false, true);
    public final EntityPropertyMain<Object> epResultQuality = new EntityPropertyMain<>(NAME_EP_RESULTQUALITY, TYPE_REFERENCE_OBJECT, true, false);
    public final EntityPropertyMain<TimeInstant> epTime = new EntityPropertyMain<>(NAME_EP_TIME, TYPE_REFERENCE_TIMEINSTANT);
    public final EntityPropertyMain<UnitOfMeasurement> epUnitOfMeasurement = new EntityPropertyMain<>(NAME_EP_UNITOFMEASUREMENT, TYPE_REFERENCE_UOM, true, false);
    public final EntityPropertyMain<TimeInterval> epValidTime = new EntityPropertyMain<>(NAME_EP_VALIDTIME, TYPE_REFERENCE_TIMEINTERVAL);

    public final NavigationPropertyEntity npDatastream = new NavigationPropertyEntity(NAME_NP_DATASTREAM);
    public final NavigationPropertyEntitySet npDatastreams = new NavigationPropertyEntitySet(NAME_NP_DATASTREAMS);
    public final NavigationPropertyEntity npFeatureOfInterest = new NavigationPropertyEntity(NAME_NP_FEATUREOFINTEREST);
    public final NavigationPropertyEntitySet npFeaturesOfInterest = new NavigationPropertyEntitySet(NAME_NP_FEATURESOFINTEREST);
    public final NavigationPropertyEntity npHistoricalLocation = new NavigationPropertyEntity(NAME_NP_HISTORICALLOCATION);
    public final NavigationPropertyEntitySet npHistoricalLocations = new NavigationPropertyEntitySet(NAME_NP_HISTORICALLOCATIONS);
    public final NavigationPropertyEntity npLocation = new NavigationPropertyEntity(NAME_NP_LOCATION);
    public final NavigationPropertyEntitySet npLocations = new NavigationPropertyEntitySet(NAME_NP_LOCATIONS);
    public final NavigationPropertyEntity npObservation = new NavigationPropertyEntity(NAME_NP_OBSERVATION);
    public final NavigationPropertyEntitySet npObservations = new NavigationPropertyEntitySet(NAME_NP_OBSERVATIONS);
    public final NavigationPropertyEntity npObservedProperty = new NavigationPropertyEntity(NAME_NP_OBSERVEDPROPERTY);
    public final NavigationPropertyEntitySet npObservedProperties = new NavigationPropertyEntitySet(NAME_NP_OBSERVEDPROPERTIES);
    public final NavigationPropertyEntity npSensor = new NavigationPropertyEntity(NAME_NP_SENSOR);
    public final NavigationPropertyEntitySet npSensors = new NavigationPropertyEntitySet(NAME_NP_SENSORS);
    public final NavigationPropertyEntity npThing = new NavigationPropertyEntity(NAME_NP_THING);
    public final NavigationPropertyEntitySet npThings = new NavigationPropertyEntitySet(NAME_NP_THINGS);

    public final EntityType etThing = new EntityType(NAME_NP_THING, NAME_NP_THINGS);
    public final EntityType etSensor = new EntityType(NAME_NP_SENSOR, NAME_NP_SENSORS);
    public final EntityType etObservedProperty = new EntityType(NAME_NP_OBSERVEDPROPERTY, NAME_NP_OBSERVEDPROPERTIES);
    public final EntityType etObservation = new EntityType(NAME_NP_OBSERVATION, NAME_NP_OBSERVATIONS).addValidator((entity, entityPropertiesOnly) -> {
        if (entity.getProperty(epPhenomenonTime) == null) {
            entity.setProperty(epPhenomenonTime, TimeInstant.now());
        }
    });
    public final EntityType etLocation = new EntityType(NAME_NP_LOCATION, NAME_NP_LOCATIONS);
    public final EntityType etHistoricalLocation = new EntityType(NAME_NP_HISTORICALLOCATION, NAME_NP_HISTORICALLOCATIONS);
    public final EntityType etFeatureOfInterest = new EntityType(NAME_NP_FEATUREOFINTEREST, NAME_NP_FEATURESOFINTEREST);
    public final EntityType etDatastream = new EntityType(NAME_NP_DATASTREAM, NAME_NP_DATASTREAMS);

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_CORE_MODEL = "coreModel.enable";

    private static final List<String> REQUIREMENTS_CORE_MODEL = Arrays.asList(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel");

    private CoreSettings settings;
    private boolean enabled;
    private boolean fullyInitialised;

    public PluginCoreModel() {
        LOGGER.info("Creating new Core Model Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_CORE_MODEL, getClass());
        if (enabled) {
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
        ModelRegistry modelRegistry = settings.getModelRegistry();
        modelRegistry.registerEntityType(etDatastream);
        modelRegistry.registerEntityType(etFeatureOfInterest);
        modelRegistry.registerEntityType(etHistoricalLocation);
        modelRegistry.registerEntityType(etLocation);
        modelRegistry.registerEntityType(etObservation);
        modelRegistry.registerEntityType(etObservedProperty);
        modelRegistry.registerEntityType(etSensor);
        modelRegistry.registerEntityType(etThing);
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        LOGGER.info("Linking Core Model Types...");
        etDatastream
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(epObservationType, true)
                .registerProperty(epUnitOfMeasurement, true)
                .registerProperty(epObservedArea, false)
                .registerProperty(epPhenomenonTimeDs, false)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(epResultTimeDs, false)
                .registerProperty(npObservedProperty, true)
                .registerProperty(npSensor, true)
                .registerProperty(npThing, true)
                .registerProperty(npObservations, false);
        etFeatureOfInterest
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(epFeature, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npObservations, false);
        etHistoricalLocation
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epTime, true)
                .registerProperty(npThing, true)
                .registerProperty(npLocations, false);
        etLocation
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(epLocation, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npHistoricalLocations, false)
                .registerProperty(npThings, false);
        etObservation
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epPhenomenonTime, false)
                .registerProperty(epResultTime, false)
                .registerProperty(epResult, true)
                .registerProperty(epResultQuality, false)
                .registerProperty(epValidTime, false)
                .registerProperty(epParameters, false)
                .registerProperty(npDatastream, true)
                .registerProperty(npFeatureOfInterest, false);
        etObservedProperty
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epName, true)
                .registerProperty(epDefinition, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npDatastreams, false);
        etSensor
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(epMetadata, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npDatastreams, false);
        etThing
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(epName, true)
                .registerProperty(epDescription, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npLocations, false)
                .registerProperty(npHistoricalLocations, false)
                .registerProperty(npDatastreams, false);

        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            TableCollection tableCollection = ppm.getTableCollection();
            DataType idType = tableCollection.getIdType();
            tableCollection.registerTable(etDatastream, new TableImpDatastreams(idType, this));
            tableCollection.registerTable(etFeatureOfInterest, new TableImpFeatures(idType, this));
            tableCollection.registerTable(etHistoricalLocation, new TableImpHistLocations(idType, this));
            tableCollection.registerTable(etLocation, new TableImpLocations(idType, this));
            tableCollection.registerTable(new TableImpLocationsHistLocations<>(idType));
            tableCollection.registerTable(etObservation, new TableImpObservations(idType, this));
            tableCollection.registerTable(etObservedProperty, new TableImpObsProperties(idType, this));
            tableCollection.registerTable(etSensor, new TableImpSensors(idType, this));
            tableCollection.registerTable(etThing, new TableImpThings(idType, this));
            tableCollection.registerTable(new TableImpThingsLocations<>(idType));
        }
        fullyInitialised = true;
        return true;
    }

    @Override
    public String checkForUpgrades() {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof PostgresPersistenceManager) {
                PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
                String fileName = LIQUIBASE_CHANGELOG_FILENAME + ppm.getIdManager().getIdClass().getSimpleName() + ".xml";
                return ppm.checkForUpgrades(fileName);
            }
            return "Unknown persistence manager class";
        }
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof PostgresPersistenceManager) {
                PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
                String fileName = LIQUIBASE_CHANGELOG_FILENAME + ppm.getIdManager().getIdClass().getSimpleName() + ".xml";
                return ppm.doUpgrades(fileName, out);
            }
            out.append("Unknown persistence manager class");
            return false;
        }
    }

}
