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

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/plugincoremodel/tables";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginCoreModel.class.getName());

    public final EntityPropertyMain<TimeInstant> EP_CREATIONTIME = new EntityPropertyMain<>("creationTime", TYPE_REFERENCE_TIMEINSTANT);
    public final EntityPropertyMain<String> EP_DESCRIPTION = new EntityPropertyMain<>("description", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<String> EP_DEFINITION = new EntityPropertyMain<>("definition", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<Object> EP_FEATURE = new EntityPropertyMain<>("feature", null, true, false);
    public final EntityPropertyMain<Object> EP_LOCATION = new EntityPropertyMain<>("Location", null, true, false);
    public final EntityPropertyMain<String> EP_METADATA = new EntityPropertyMain<>("Metadata", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<String> EP_NAME = new EntityPropertyMain<>("Name", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<String> EP_OBSERVATIONTYPE = new EntityPropertyMain<>("ObservationType", TYPE_REFERENCE_STRING);
    public final EntityPropertyMain<GeoJsonObject> EP_OBSERVEDAREA = new EntityPropertyMain<>("ObservedArea", TYPE_REFERENCE_GEOJSONOBJECT);
    public final EntityPropertyMain<TimeValue> EP_PHENOMENONTIME = new EntityPropertyMain<>("PhenomenonTime", TYPE_REFERENCE_TIMEVALUE);
    public final EntityPropertyMain<TimeInterval> EP_PHENOMENONTIME_DS = new EntityPropertyMain<>("PhenomenonTime", TYPE_REFERENCE_TIMEINTERVAL);
    public final EntityPropertyMain<Map<String, Object>> EP_PARAMETERS = new EntityPropertyMain<>("Parameters", TYPE_REFERENCE_MAP, true, false);
    public final EntityPropertyMain<Object> EP_RESULT = new EntityPropertyMain<>("Result", TYPE_REFERENCE_OBJECT, true, true);
    public final EntityPropertyMain<TimeInstant> EP_RESULTTIME = new EntityPropertyMain<>("ResultTime", TYPE_REFERENCE_TIMEINSTANT, false, true);
    public final EntityPropertyMain<TimeInterval> EP_RESULTTIME_DS = new EntityPropertyMain<>("ResultTime", TYPE_REFERENCE_TIMEINTERVAL, false, true);
    public final EntityPropertyMain<Object> EP_RESULTQUALITY = new EntityPropertyMain<>("ResultQuality", TYPE_REFERENCE_OBJECT, true, false);
    public final EntityPropertyMain<TimeInstant> EP_TIME = new EntityPropertyMain<>("Time", TYPE_REFERENCE_TIMEINSTANT);
    public final EntityPropertyMain<UnitOfMeasurement> EP_UNITOFMEASUREMENT = new EntityPropertyMain<>("UnitOfMeasurement", TYPE_REFERENCE_UOM, true, false);
    public final EntityPropertyMain<TimeInterval> EP_VALIDTIME = new EntityPropertyMain<>("ValidTime", TYPE_REFERENCE_TIMEINTERVAL);

    public final NavigationPropertyEntity NP_DATASTREAM = new NavigationPropertyEntity("Datastream");
    public final NavigationPropertyEntitySet NP_DATASTREAMS = new NavigationPropertyEntitySet("Datastreams");
    public final NavigationPropertyEntity NP_FEATUREOFINTEREST = new NavigationPropertyEntity("FeatureOfInterest");
    public final NavigationPropertyEntitySet NP_FEATURESOFINTEREST = new NavigationPropertyEntitySet("FeaturesOfInterest");
    public final NavigationPropertyEntity NP_HISTORICALLOCATION = new NavigationPropertyEntity("HistoricalLocation");
    public final NavigationPropertyEntitySet NP_HISTORICALLOCATIONS = new NavigationPropertyEntitySet("HistoricalLocations");
    public final NavigationPropertyEntity NP_LOCATION = new NavigationPropertyEntity("Location");
    public final NavigationPropertyEntitySet NP_LOCATIONS = new NavigationPropertyEntitySet("Locations");
    public final NavigationPropertyEntity NP_OBSERVATION = new NavigationPropertyEntity("Observation");
    public final NavigationPropertyEntitySet NP_OBSERVATIONS = new NavigationPropertyEntitySet("Observations");
    public final NavigationPropertyEntity NP_OBSERVEDPROPERTY = new NavigationPropertyEntity("ObservedProperty");
    public final NavigationPropertyEntitySet NP_OBSERVEDPROPERTIES = new NavigationPropertyEntitySet("ObservedProperties");
    public final NavigationPropertyEntity NP_SENSOR = new NavigationPropertyEntity("Sensor");
    public final NavigationPropertyEntitySet NP_SENSORS = new NavigationPropertyEntitySet("Sensors");
    public final NavigationPropertyEntity NP_THING = new NavigationPropertyEntity("Thing");
    public final NavigationPropertyEntitySet NP_THINGS = new NavigationPropertyEntitySet("Things");

    public final EntityType THING = new EntityType("Thing", "Things");
    public final EntityType SENSOR = new EntityType("Sensor", "Sensors");
    public final EntityType OBSERVED_PROPERTY = new EntityType("ObservedProperty", "ObservedProperties");
    public final EntityType OBSERVATION = new EntityType("Observation", "Observations");
    public final EntityType LOCATION = new EntityType("Location", "Locations");
    public final EntityType HISTORICAL_LOCATION = new EntityType("HistoricalLocation", "HistoricalLocations");
    public final EntityType FEATURE_OF_INTEREST = new EntityType("FeatureOfInterest", "FeaturesOfInterest");
    public final EntityType DATASTREAM = new EntityType("Datastream", "Datastreams");

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_CORE_MODEL = "coreModel.enable";

    private static final List<String> REQUIREMENTS_CORE_MODEL = Arrays.asList(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel");

    private CoreSettings settings;

    public PluginCoreModel() {
        LOGGER.info("Creating new Core Model Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        boolean enabled = pluginSettings.getBoolean(TAG_ENABLE_CORE_MODEL, getClass());
        if (enabled) {
            settings.getPluginManager().registerPlugin(this);
        }
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
    public void registerProperties() {
        ModelRegistry modelRegistry = settings.getModelRegistry();
        modelRegistry.registerEntityProperty(EP_CREATIONTIME);
        modelRegistry.registerEntityProperty(EP_DEFINITION);
        modelRegistry.registerEntityProperty(EP_DESCRIPTION);
        modelRegistry.registerEntityProperty(ModelRegistry.EP_ENCODINGTYPE);
        modelRegistry.registerEntityProperty(EP_FEATURE);
        modelRegistry.registerEntityProperty(ModelRegistry.EP_ID);
        modelRegistry.registerEntityProperty(EP_LOCATION);
        modelRegistry.registerEntityProperty(EP_METADATA);
        modelRegistry.registerEntityProperty(EP_NAME);
        modelRegistry.registerEntityProperty(EP_OBSERVATIONTYPE);
        modelRegistry.registerEntityProperty(EP_OBSERVEDAREA);
        modelRegistry.registerEntityProperty(EP_PARAMETERS);
        modelRegistry.registerEntityProperty(EP_PHENOMENONTIME);
        modelRegistry.registerEntityProperty(ModelRegistry.EP_PROPERTIES);
        modelRegistry.registerEntityProperty(EP_RESULT);
        modelRegistry.registerEntityProperty(EP_RESULTQUALITY);
        modelRegistry.registerEntityProperty(EP_RESULTTIME);
        modelRegistry.registerEntityProperty(ModelRegistry.EP_SELFLINK);
        modelRegistry.registerEntityProperty(EP_TIME);
        modelRegistry.registerEntityProperty(EP_UNITOFMEASUREMENT);
        modelRegistry.registerEntityProperty(EP_VALIDTIME);

        modelRegistry.registerNavProperty(NP_DATASTREAM);
        modelRegistry.registerNavProperty(NP_DATASTREAMS);
        modelRegistry.registerNavProperty(NP_FEATUREOFINTEREST);
        modelRegistry.registerNavProperty(NP_FEATURESOFINTEREST);
        modelRegistry.registerNavProperty(NP_HISTORICALLOCATION);
        modelRegistry.registerNavProperty(NP_HISTORICALLOCATIONS);
        modelRegistry.registerNavProperty(NP_LOCATION);
        modelRegistry.registerNavProperty(NP_LOCATIONS);
        modelRegistry.registerNavProperty(NP_OBSERVATION);
        modelRegistry.registerNavProperty(NP_OBSERVATIONS);
        modelRegistry.registerNavProperty(NP_OBSERVEDPROPERTIES);
        modelRegistry.registerNavProperty(NP_OBSERVEDPROPERTY);
        modelRegistry.registerNavProperty(NP_SENSOR);
        modelRegistry.registerNavProperty(NP_SENSORS);
        modelRegistry.registerNavProperty(NP_THING);
        modelRegistry.registerNavProperty(NP_THINGS);
    }

    @Override
    public boolean registerEntityTypes(PersistenceManager pm) {
        LOGGER.info("Initialising Core Model Types...");
        ModelRegistry modelRegistry = settings.getModelRegistry();
        modelRegistry.registerEntityType(DATASTREAM)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(EP_OBSERVATIONTYPE, true)
                .registerProperty(EP_UNITOFMEASUREMENT, true)
                .registerProperty(EP_OBSERVEDAREA, false)
                .registerProperty(EP_PHENOMENONTIME_DS, false)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(EP_RESULTTIME_DS, false)
                .registerProperty(NP_OBSERVEDPROPERTY, true)
                .registerProperty(NP_SENSOR, true)
                .registerProperty(NP_THING, true)
                .registerProperty(NP_OBSERVATIONS, false);
        modelRegistry.registerEntityType(FEATURE_OF_INTEREST)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(EP_FEATURE, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_OBSERVATIONS, false);
        modelRegistry.registerEntityType(HISTORICAL_LOCATION)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_TIME, true)
                .registerProperty(NP_THING, true)
                .registerProperty(NP_LOCATIONS, false);
        modelRegistry.registerEntityType(LOCATION)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(EP_LOCATION, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_HISTORICALLOCATIONS, false)
                .registerProperty(NP_THINGS, false);
        modelRegistry.registerEntityType(OBSERVATION)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_PHENOMENONTIME, false)
                .registerProperty(EP_RESULTTIME, false)
                .registerProperty(EP_RESULT, true)
                .registerProperty(EP_RESULTQUALITY, false)
                .registerProperty(EP_VALIDTIME, false)
                .registerProperty(EP_PARAMETERS, false)
                .registerProperty(NP_DATASTREAM, true)
                .registerProperty(NP_FEATUREOFINTEREST, false);
        modelRegistry.registerEntityType(OBSERVED_PROPERTY)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DEFINITION, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_DATASTREAMS, false);
        modelRegistry.registerEntityType(SENSOR)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(EP_METADATA, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_DATASTREAMS, false);
        modelRegistry.registerEntityType(THING)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(EP_NAME, true)
                .registerProperty(EP_DESCRIPTION, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_LOCATIONS, false)
                .registerProperty(NP_HISTORICALLOCATIONS, false)
                .registerProperty(NP_DATASTREAMS, false);

        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            TableCollection tableCollection = ppm.getTableCollection();
            DataType idType = tableCollection.getIdType();
            tableCollection.registerTable(DATASTREAM, new TableImpDatastreams(idType, this));
            tableCollection.registerTable(FEATURE_OF_INTEREST, new TableImpFeatures(idType, this));
            tableCollection.registerTable(HISTORICAL_LOCATION, new TableImpHistLocations(idType, this));
            tableCollection.registerTable(LOCATION, new TableImpLocations(idType, this));
            tableCollection.registerTable(new TableImpLocationsHistLocations<>(idType));
            tableCollection.registerTable(OBSERVATION, new TableImpObservations(idType, this));
            tableCollection.registerTable(OBSERVED_PROPERTY, new TableImpObsProperties(idType, this));
            tableCollection.registerTable(SENSOR, new TableImpSensors(idType, this));
            tableCollection.registerTable(THING, new TableImpThings(idType, this));
            tableCollection.registerTable(new TableImpThingsLocations<>(idType));
        }
        return true;
    }

    @Override
    public String checkForUpgrades() {
        PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create();
        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            String fileName = LIQUIBASE_CHANGELOG_FILENAME + ppm.getIdManager().getIdClass().getSimpleName() + ".xml";
            return ppm.checkForUpgrades(fileName);
        }
        return "Unknown persistence manager class";
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create();
        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            String fileName = LIQUIBASE_CHANGELOG_FILENAME + ppm.getIdManager().getIdClass().getSimpleName() + ".xml";
            return ppm.doUpgrades(fileName, out);
        }
        out.append("Unknown persistence manager class");
        return false;
    }

}
