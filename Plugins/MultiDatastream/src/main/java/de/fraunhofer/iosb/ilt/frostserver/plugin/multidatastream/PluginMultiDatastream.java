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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
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
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PluginMultiDatastream implements PluginRootDocument, PluginModel, ConfigDefaults, LiquibaseUser {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/pluginmultidatastream/tables";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginMultiDatastream.class.getName());

    private static final String MODEL_PATH = "pluginmultidatastream";
    private static final String[] MODEL_FILES = new String[]{
        "MultiDatastream.json"
    };

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_MDS_MODEL = "multiDatastream.enable";

    private static final List<String> REQUIREMENTS_MDS_MODEL = Arrays.asList(
            "http://www.opengis.net/spec/iot_sensing/1.1/req/multi-datastream"
    );

    private CoreSettings settings;
    private boolean enabled;
    private boolean fullyInitialised;
    private List<DefModel> modelDefinitions = new ArrayList<>();

    public PluginMultiDatastream() {
        LOGGER.info("Creating new MultiDatastream++ Plugin. {}", ValidatorsHooks.ValidatorMultiDatastream.class.getName());
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_MDS_MODEL, getClass());
        if (enabled) {
            settings.getPluginManager().registerPlugin(this);
            for (String fileName : MODEL_FILES) {
                loadModelFile(fileName);
            }
        }
    }

    private void loadModelFile(String fileName) {
        final String fullPath = MODEL_PATH + "/" + fileName;
        LOGGER.info("Loading model definition from {}", fullPath);
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fullPath);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DefModel modelDefinition = objectMapper.readValue(stream, DefModel.class);
            modelDefinition.init();
            modelDefinitions.add(modelDefinition);
        } catch (IOException ex) {
            LOGGER.error("Failed to load model definition", ex);
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
        extensionList.addAll(REQUIREMENTS_MDS_MODEL);
    }

    @Override
    public void registerEntityTypes() {
        ModelRegistry modelRegistry = settings.getModelRegistry();
        for (DefModel modelDefinition : modelDefinitions) {
            modelDefinition.registerEntityTypes(modelRegistry);
        }
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        LOGGER.info("Initialising OMS Model Types...");
        final ModelRegistry modelRegistry = settings.getModelRegistry();
        for (DefModel modelDefinition : modelDefinitions) {
            modelDefinition.linkEntityTypes(modelRegistry);
            if (pm != null) {
                pm.addModelMapping(modelDefinition);
            }
        }

        // Make DATASTREAM optional.
        final EntityType etObservation = modelRegistry.getEntityTypeForName("Observation");
        final EntityType etObservedProperty = modelRegistry.getEntityTypeForName("ObservedProperty");
        final NavigationPropertyMain npDatastream = etObservation.getNavigationProperty("Datastream");
        etObservation.registerProperty(npDatastream, false);
        etObservation.addValidator(new ValidatorsHooks.ValidatorObservations());

        if (pm instanceof PostgresPersistenceManager) {
            final PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            final StaMainTable obsPropsTable = ppm.getTableCollection().getTableForType(etObservedProperty);
            final StaMainTable observationsTable = ppm.getTableCollection().getTableForType(etObservation);

            // Register hooks to alter behaviour of other tables
            obsPropsTable.registerHookPreInsert(-1, new ValidatorsHooks.HookPreInsertObservedProperty());
            obsPropsTable.registerHookPreUpdate(-1, new ValidatorsHooks.HookPreUpdateObservedProperty());
            obsPropsTable.registerHookPreDelete(-1, new ValidatorsHooks.HookPreDeleteObservedProperty());
            // On insert Observation
            observationsTable.registerHookPreInsert(-1, new ValidatorsHooks.HookPreInsertObservation());
            // On update, make sure we still have either a DS or MDS, but not both.
            observationsTable.registerHookPreUpdate(-1, new ValidatorsHooks.HookPreUpdateObservation());
        }

        // Done, release the model definition.
        modelDefinitions = null;
        fullyInitialised = true;
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
