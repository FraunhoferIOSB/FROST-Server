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
package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_MAP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
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
import org.jooq.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PluginActuation implements PluginRootDocument, PluginModel, ConfigDefaults, LiquibaseUser {

    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/pluginactuation/tables";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginActuation.class.getName());

    public final EntityPropertyMain<Map<String, Object>> epTaskingParameters = new EntityPropertyMain<>("taskingParameters", TYPE_REFERENCE_MAP, true, false);

    public final NavigationPropertyEntity npActuator = new NavigationPropertyEntity("Actuator");
    public final NavigationPropertyEntitySet npActuators = new NavigationPropertyEntitySet("Actuators");
    public final NavigationPropertyEntity npTask = new NavigationPropertyEntity("Task");
    public final NavigationPropertyEntitySet npTasks = new NavigationPropertyEntitySet("Tasks");
    public final NavigationPropertyEntity npTaskingCapability = new NavigationPropertyEntity("TaskingCapability");
    public final NavigationPropertyEntitySet npTaskingCapabilities = new NavigationPropertyEntitySet("TaskingCapabilities");

    public final EntityType etActuator = new EntityType("Actuator", "Actuators");
    public final EntityType etTask = new EntityType("Task", "Tasks");
    public final EntityType etTaskingCapability = new EntityType("TaskingCapability", "TaskingCapabilities");

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_ACTUATION = "actuation.enable";

    private static final List<String> REQUIREMENTS_ACTUATION = Arrays.asList(
            "http://www.opengis.net/spec/iot_tasking/1.0/req/tasking-capability",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/task",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/actuator",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks-via-mqtt",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/receive-updates-via-mqtt");

    private CoreSettings settings;
    private boolean enabled;
    private boolean fullyInitialised;

    public PluginActuation() {
        LOGGER.info("Creating new Actuation Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_ACTUATION, getClass());
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
        extensionList.addAll(REQUIREMENTS_ACTUATION);
    }

    @Override
    public void registerEntityTypes() {
        LOGGER.info("Initialising Actuation Types...");
        ModelRegistry modelRegistry = settings.getModelRegistry();
        modelRegistry.registerEntityType(etActuator);
        modelRegistry.registerEntityType(etTask);
        modelRegistry.registerEntityType(etTaskingCapability);
    }

    @Override
    public void registerProperties() {
        LOGGER.info("Initialising Actuation Properties...");
        ModelRegistry modelRegistry = settings.getModelRegistry();
        modelRegistry.registerEntityProperty(epTaskingParameters);
        modelRegistry.registerNavProperty(npActuator);
        modelRegistry.registerNavProperty(npActuators);
        modelRegistry.registerNavProperty(npTask);
        modelRegistry.registerNavProperty(npTasks);
        modelRegistry.registerNavProperty(npTaskingCapability);
        modelRegistry.registerNavProperty(npTaskingCapabilities);
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        LOGGER.info("Linking Actuation Types...");
        final PluginCoreModel pluginCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        if (pluginCoreModel == null || !pluginCoreModel.isFullyInitialised()) {
            return false;
        }
        etActuator
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(pluginCoreModel.epName, true)
                .registerProperty(pluginCoreModel.epDescription, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(pluginCoreModel.epMetadata, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(npTaskingCapabilities, false);
        etTask
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(pluginCoreModel.epCreationTime, false)
                .registerProperty(epTaskingParameters, true)
                .registerProperty(npTaskingCapability, true);
        etTaskingCapability
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(pluginCoreModel.epName, true)
                .registerProperty(pluginCoreModel.epDescription, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(epTaskingParameters, true)
                .registerProperty(npActuator, true)
                .registerProperty(npTasks, false)
                .registerProperty(pluginCoreModel.npThing, true);
        pluginCoreModel.etThing.registerProperty(npTaskingCapabilities, false);

        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            TableCollection tableCollection = ppm.getTableCollection();
            DataType idType = tableCollection.getIdType();
            tableCollection.registerTable(etActuator, new TableImpActuators(idType, this, pluginCoreModel));
            tableCollection.registerTable(etTask, new TableImpTasks(idType, this, pluginCoreModel));
            tableCollection.registerTable(etTaskingCapability, new TableImpTaskingCapabilities(idType, this, pluginCoreModel));
        }
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
