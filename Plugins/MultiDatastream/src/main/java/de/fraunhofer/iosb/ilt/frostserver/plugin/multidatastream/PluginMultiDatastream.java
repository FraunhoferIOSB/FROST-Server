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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import static de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry.EP_SELFLINK;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_LIST_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive.EDM_STRING;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimpleSet;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The V1.1 multiDatastream plugin.
 */
public class PluginMultiDatastream implements PluginRootDocument, PluginModel, ConfigDefaults, LiquibaseUser {

    private static final String IS_NOT_EQUAL = ") is not equal to size of multiObservationDataTypes (";
    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/pluginmultidatastream/tables.xml";
    private static final String MULTI_DATASTREAM = "MultiDatastream";
    private static final String MULTI_DATASTREAMS = "MultiDatastreams";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginMultiDatastream.class.getName());

    public final EntityPropertyMain<List<String>> epMultiObservationDataTypes = new EntityPropertyMain<>("multiObservationDataTypes", new TypeSimpleSet(EDM_STRING, TYPE_REFERENCE_LIST_STRING), true, false);
    private EntityPropertyMain<List<UnitOfMeasurement>> epUnitOfMeasurements;
    private EntityPropertyMain<?> epIdMultiDatastream;

    public final NavigationPropertyEntity npMultiDatastreamObservation = new NavigationPropertyEntity(MULTI_DATASTREAM, false);
    public final NavigationPropertyEntitySet npObservationsMDs = new NavigationPropertyEntitySet("Observations", npMultiDatastreamObservation);

    public final NavigationPropertyEntitySet npMultiDatastreamsObsProp = new NavigationPropertyEntitySet(MULTI_DATASTREAMS);
    public final NavigationPropertyEntitySet npObservedPropertiesMDs = new NavigationPropertyEntitySet("ObservedProperties", npMultiDatastreamsObsProp);

    public final NavigationPropertyEntitySet npMultiDatastreamsThing = new NavigationPropertyEntitySet(MULTI_DATASTREAMS);
    public final NavigationPropertyEntity npThingMDs = new NavigationPropertyEntity("Thing", npMultiDatastreamsThing, true);

    public final NavigationPropertyEntitySet npMultiDatastreamsSensor = new NavigationPropertyEntitySet(MULTI_DATASTREAMS);
    public final NavigationPropertyEntity npSensorMDs = new NavigationPropertyEntity("Sensor", npMultiDatastreamsSensor, true);

    public final EntityType etMultiDatastream = new EntityType(MULTI_DATASTREAM, MULTI_DATASTREAMS);

    private static final List<String> REQUIREMENTS_MDS_MODEL = Arrays.asList(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream");

    private CoreSettings settings;
    private MdsModelSettings modelSettings;
    private boolean enabled;
    private boolean fullyInitialised;

    @Override
    public InitResult init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(MdsModelSettings.TAG_ENABLE_MDS_MODEL, MdsModelSettings.class);
        if (enabled) {
            modelSettings = new MdsModelSettings(settings);
            settings.getPluginManager().registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        Map<String, Object> serverSettings = (Map<String, Object>) result.get(Service.KEY_SERVER_SETTINGS);
        if (serverSettings == null) {
            // Nothing to add to.
            return;
        }
        Set<String> extensionList = (Set<String>) serverSettings.get(Service.KEY_CONFORMANCE_LIST);
        extensionList.addAll(REQUIREMENTS_MDS_MODEL);
    }

    @Override
    public void registerEntityTypes() {
        LOGGER.info("Initialising MultiDatastream Types...");
        ModelRegistry mr = settings.getModelRegistry();

        mr.registerEntityType(etMultiDatastream);
        epIdMultiDatastream = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeMultiDatastream)).setAliases("id");
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
    public boolean linkEntityTypes(PersistenceManager pm) {
        LOGGER.info("Linking MultiDatastream Types...");
        final PluginCoreModel pluginCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        if (pluginCoreModel == null || !pluginCoreModel.isFullyInitialised()) {
            return false;
        }
        epUnitOfMeasurements = new EntityPropertyMain<>("unitOfMeasurements", new TypeSimpleSet(pluginCoreModel.getEptUom(), TypeReferencesHelper.TYPE_REFERENCE_LIST_UOM), true, false);
        etMultiDatastream
                .registerProperty(epIdMultiDatastream)
                .registerProperty(EP_SELFLINK)
                .registerProperty(pluginCoreModel.epName)
                .registerProperty(pluginCoreModel.epDescription)
                .registerProperty(pluginCoreModel.epObservationType)
                .registerProperty(epMultiObservationDataTypes)
                .registerProperty(epUnitOfMeasurements)
                .registerProperty(pluginCoreModel.epObservedArea)
                .registerProperty(pluginCoreModel.epPhenomenonTimeDs)
                .registerProperty(ModelRegistry.EP_PROPERTIES)
                .registerProperty(pluginCoreModel.epResultTimeDs)
                .registerProperty(npObservedPropertiesMDs)
                .registerProperty(npSensorMDs)
                .registerProperty(npThingMDs)
                .registerProperty(npObservationsMDs)
                .addCreateValidator("MD-Properties", entity -> {
                    List<UnitOfMeasurement> unitOfMeasurements = entity.getProperty(epUnitOfMeasurements);
                    List<String> multiObservationDataTypes = entity.getProperty(epMultiObservationDataTypes);
                    EntitySet observedProperties = entity.getProperty(npObservedPropertiesMDs);
                    if (unitOfMeasurements == null || unitOfMeasurements.size() != multiObservationDataTypes.size()) {
                        throw new IllegalArgumentException("Size of list of unitOfMeasurements (" + (unitOfMeasurements == null ? "null" : unitOfMeasurements.size()) + IS_NOT_EQUAL + multiObservationDataTypes.size() + ").");
                    }
                    if (observedProperties == null || observedProperties.size() != multiObservationDataTypes.size()) {
                        final int opSize = observedProperties == null ? 0 : observedProperties.size();
                        throw new IllegalArgumentException("Size of list of observedProperties (" + opSize + IS_NOT_EQUAL + multiObservationDataTypes.size() + ").");
                    }
                    String observationType = entity.getProperty(pluginCoreModel.epObservationType);
                    if (observationType == null || !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
                        throw new IllegalArgumentException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
                    }
                })
                .addUpdateValidator("MD-Properties", entity -> {
                    List<UnitOfMeasurement> unitOfMeasurements = entity.getProperty(epUnitOfMeasurements);
                    List<String> multiObservationDataTypes = entity.getProperty(epMultiObservationDataTypes);
                    if (unitOfMeasurements != null && multiObservationDataTypes != null && unitOfMeasurements.size() != multiObservationDataTypes.size()) {
                        throw new IllegalArgumentException("Size of list of unitOfMeasurements (" + unitOfMeasurements.size() + IS_NOT_EQUAL + multiObservationDataTypes.size() + ").");
                    }
                    String observationType = entity.getProperty(pluginCoreModel.epObservationType);
                    if (observationType != null && !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
                        throw new IllegalArgumentException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
                    }
                });
        // Register multiDatastream on existing entities.
        pluginCoreModel.etThing
                .registerProperty(npMultiDatastreamsThing);
        pluginCoreModel.etObservedProperty
                .registerProperty(npMultiDatastreamsObsProp);
        pluginCoreModel.etSensor
                .registerProperty(npMultiDatastreamsSensor);
        // Now make DATASTREAM optional and register a validator that checks if
        // either Datastream or MultiDatastream is set.
        pluginCoreModel.npDatastreamObservation.setRequired(false);
        pluginCoreModel.etObservation
                .registerProperty(npMultiDatastreamObservation)
                .addCreateValidator("MD-Obs-DsOrMds", entity -> {
                    Entity datastream = entity.getProperty(pluginCoreModel.npDatastreamObservation);
                    Entity multiDatastream = entity.getProperty(npMultiDatastreamObservation);
                    if (datastream != null && multiDatastream != null) {
                        throw new IllegalArgumentException("Observation can not have both a Datasteam and a MultiDatastream.");
                    }
                    if (datastream == null && multiDatastream == null) {
                        throw new IncompleteEntityException("Observation must have either a Datasteam or a MultiDatastream.");
                    }
                    if (multiDatastream != null) {
                        Object result = entity.getProperty(pluginCoreModel.epResult);
                        if (!(result instanceof List)) {
                            throw new IllegalArgumentException("Observation in a MultiDatastream must have an Array result.");
                        }
                    }
                });

        if (pm instanceof JooqPersistenceManager ppm) {
            final TableCollection tableCollection = ppm.getTableCollection();
            final DataType dataTypeMds = ppm.getDataTypeFor(modelSettings.idTypeMultiDatastream);
            final DataType dataTypeObsProp = tableCollection.getTableForType(pluginCoreModel.etObservedProperty).getPkFields().get(0).getDataType();
            tableCollection.registerTable(etMultiDatastream, new TableImpMultiDatastreams(dataTypeMds, this, pluginCoreModel));
            tableCollection.registerTable(new TableImpMultiDatastreamsObsProperties(dataTypeMds, dataTypeObsProp));
        }
        fullyInitialised = true;
        return true;
    }

    public Map<String, Object> createLiqibaseParams(JooqPersistenceManager ppm, Map<String, Object> target) {
        if (target == null) {
            target = new LinkedHashMap<>();
        }
        PluginCoreModel pCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        pCoreModel.createLiqibaseParams(ppm, target);
        ppm.generateLiquibaseVariables(target, MULTI_DATASTREAM, modelSettings.idTypeMultiDatastream);

        return target;
    }

    @Override
    public String checkForUpgrades() {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof JooqPersistenceManager ppm) {
                return ppm.checkForUpgrades(LIQUIBASE_CHANGELOG_FILENAME, createLiqibaseParams(ppm, null));
            }
            return "Unknown persistence manager class";
        }
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof JooqPersistenceManager ppm) {
                return ppm.doUpgrades(LIQUIBASE_CHANGELOG_FILENAME, createLiqibaseParams(ppm, null), out);
            }
            out.append("Unknown persistence manager class");
            return false;
        }
    }

    /**
     * @return the entity property UnitOfMeasurements
     */
    public EntityPropertyMain<List<UnitOfMeasurement>> getEpUnitOfMeasurements() {
        return epUnitOfMeasurements;
    }

    /**
     * @return the entity property IdMultiDatastream
     */
    public EntityPropertyMain<?> getEpIdMultiDatastream() {
        return epIdMultiDatastream;
    }

}
