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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import static de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry.EP_ID;
import static de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry.EP_SELFLINK;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_LIST_STRING;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PluginMultiDatastream implements PluginRootDocument, PluginModel, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginMultiDatastream.class.getName());

    public final EntityPropertyMain<List<String>> EP_MULTIOBSERVATIONDATATYPES = new EntityPropertyMain<>("MultiObservationDataTypes", TYPE_REFERENCE_LIST_STRING);
    public final EntityPropertyMain<List<UnitOfMeasurement>> EP_UNITOFMEASUREMENTS = new EntityPropertyMain<>("UnitOfMeasurements", TypeReferencesHelper.TYPE_REFERENCE_LIST_UOM, true, false);

    public final NavigationPropertyMain.NavigationPropertyEntity NP_MULTIDATASTREAM = new NavigationPropertyMain.NavigationPropertyEntity("MultiDatastream");
    public final NavigationPropertyMain.NavigationPropertyEntitySet NP_MULTIDATASTREAMS = new NavigationPropertyMain.NavigationPropertyEntitySet("MultiDatastreams");

    public final EntityType MULTI_DATASTREAM = new EntityType("MultiDatastream", "MultiDatastreams");

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_MULTI_DATASTREAM = "multiDatastream.enable";

    private static final List<String> REQUIREMENTS_MULTIDATASTREAM = Arrays.asList(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream"
    );

    private CoreSettings settings;

    public PluginMultiDatastream() {
        LOGGER.info("Creating new MultiDatastream Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        boolean enabled = pluginSettings.getBoolean(TAG_ENABLE_MULTI_DATASTREAM, getClass());
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
        extensionList.addAll(REQUIREMENTS_MULTIDATASTREAM);
    }

    @Override
    public void registerProperties() {
        ModelRegistry modelRegistry = settings.getModelRegistry();
        modelRegistry.registerEntityProperty(EP_MULTIOBSERVATIONDATATYPES);
        modelRegistry.registerEntityProperty(EP_UNITOFMEASUREMENTS);
        modelRegistry.registerNavProperty(NP_MULTIDATASTREAM);
        modelRegistry.registerNavProperty(NP_MULTIDATASTREAMS);
    }

    @Override
    public boolean registerEntityTypes(PersistenceManager pm) {
        LOGGER.info("Initialising MultiDatastream Types...");
        final ModelRegistry modelRegistry = settings.getModelRegistry();
        final PluginCoreModel pluginCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        modelRegistry.registerEntityType(MULTI_DATASTREAM)
                .registerProperty(EP_ID, false)
                .registerProperty(EP_SELFLINK, false)
                .registerProperty(pluginCoreModel.EP_NAME, true)
                .registerProperty(pluginCoreModel.EP_DESCRIPTION, true)
                .registerProperty(pluginCoreModel.EP_OBSERVATIONTYPE, false)
                .registerProperty(EP_MULTIOBSERVATIONDATATYPES, true)
                .registerProperty(EP_UNITOFMEASUREMENTS, true)
                .registerProperty(pluginCoreModel.EP_OBSERVEDAREA, false)
                .registerProperty(pluginCoreModel.EP_PHENOMENONTIME_DS, false)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(pluginCoreModel.EP_RESULTTIME_DS, false)
                .registerProperty(pluginCoreModel.NP_OBSERVEDPROPERTIES, false)
                .registerProperty(pluginCoreModel.NP_SENSOR, true)
                .registerProperty(pluginCoreModel.NP_THING, true)
                .registerProperty(pluginCoreModel.NP_OBSERVATIONS, false)
                .addValidator((entity, entityPropertiesOnly) -> {
                    List<UnitOfMeasurement> unitOfMeasurements = entity.getProperty(EP_UNITOFMEASUREMENTS);
                    List<String> multiObservationDataTypes = entity.getProperty(EP_MULTIOBSERVATIONDATATYPES);
                    EntitySet observedProperties = entity.getProperty(pluginCoreModel.NP_OBSERVEDPROPERTIES);
                    if (unitOfMeasurements == null || unitOfMeasurements.size() != multiObservationDataTypes.size()) {
                        throw new IllegalArgumentException("Size of list of unitOfMeasurements (" + unitOfMeasurements.size() + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
                    }
                    if (!entityPropertiesOnly && observedProperties == null || observedProperties.size() != multiObservationDataTypes.size()) {
                        final int opSize = observedProperties == null ? 0 : observedProperties.size();
                        throw new IllegalArgumentException("Size of list of observedProperties (" + opSize + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
                    }
                    String observationType = entity.getProperty(pluginCoreModel.EP_OBSERVATIONTYPE);
                    if (observationType == null || !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
                        throw new IllegalArgumentException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
                    }
                });
        // Register multiDatastream on existing entities.
        pluginCoreModel.THING
                .registerProperty(NP_MULTIDATASTREAMS, false);
        pluginCoreModel.OBSERVED_PROPERTY
                .registerProperty(NP_MULTIDATASTREAMS, false);
        pluginCoreModel.SENSOR
                .registerProperty(NP_MULTIDATASTREAMS, false);
        // Now make DATASTREAM optional and register a validator that checks if
        // either Datastream or MultiDatastream is set.
        pluginCoreModel.OBSERVATION
                .registerProperty(pluginCoreModel.NP_DATASTREAM, false)
                .registerProperty(NP_MULTIDATASTREAM, false)
                .addValidator((entity, entityPropertiesOnly) -> {
                    if (!entityPropertiesOnly) {
                        Entity datastream = entity.getProperty(pluginCoreModel.NP_DATASTREAM);
                        Entity multiDatastream = entity.getProperty(NP_MULTIDATASTREAM);
                        if (datastream != null && multiDatastream != null) {
                            throw new IllegalArgumentException("Observation can not have both a Datasteam and a MultiDatastream.");
                        }
                        if (datastream == null && multiDatastream == null) {
                            throw new IncompleteEntityException("Observation must have either a Datasteam or a MultiDatastream.");
                        }
                        if (multiDatastream != null) {
                            Object result = entity.getProperty(pluginCoreModel.EP_RESULT);
                            if (!(result instanceof List)) {
                                throw new IllegalArgumentException("Observation in a MultiDatastream must have an Array result.");
                            }
                        }
                    }
                });

        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            TableCollection tableCollection = ppm.getTableCollection();
            DataType idType = tableCollection.getIdType();
            tableCollection.registerTable(MULTI_DATASTREAM, new TableImpMultiDatastreams(idType, this, pluginCoreModel));
            tableCollection.registerTable(new TableImpMultiDatastreamsObsProperties<>(idType));
        }
        return true;
    }

}
