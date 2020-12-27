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

    public final EntityPropertyMain<Map<String, Object>> EP_TASKINGPARAMETERS = new EntityPropertyMain<>("taskingParameters", TYPE_REFERENCE_MAP, true, false);

    public final NavigationPropertyEntity NP_ACTUATOR = new NavigationPropertyEntity("Actuator");
    public final NavigationPropertyEntitySet NP_ACTUATORS = new NavigationPropertyEntitySet("Actuators");
    public final NavigationPropertyEntity NP_TASK = new NavigationPropertyEntity("Task");
    public final NavigationPropertyEntitySet NP_TASKS = new NavigationPropertyEntitySet("Tasks");
    public final NavigationPropertyEntity NP_TASKINGCAPABILITY = new NavigationPropertyEntity("TaskingCapability");
    public final NavigationPropertyEntitySet NP_TASKINGCAPABILITIES = new NavigationPropertyEntitySet("TaskingCapabilities");

    public final EntityType ACTUATOR = new EntityType("Actuator", "Actuators");
    public final EntityType TASK = new EntityType("Task", "Tasks");
    public final EntityType TASKING_CAPABILITY = new EntityType("TaskingCapability", "TaskingCapabilities");

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

    public PluginActuation() {
        LOGGER.info("Creating new Actuation Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        boolean enabled = pluginSettings.getBoolean(TAG_ENABLE_ACTUATION, getClass());
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
        extensionList.addAll(REQUIREMENTS_ACTUATION);
    }

    @Override
    public void registerProperties() {
        ModelRegistry modelRegistry = settings.getModelRegistry();
        modelRegistry.registerEntityProperty(EP_TASKINGPARAMETERS);
        modelRegistry.registerNavProperty(NP_ACTUATOR);
        modelRegistry.registerNavProperty(NP_ACTUATORS);
        modelRegistry.registerNavProperty(NP_TASK);
        modelRegistry.registerNavProperty(NP_TASKS);
        modelRegistry.registerNavProperty(NP_TASKINGCAPABILITY);
        modelRegistry.registerNavProperty(NP_TASKINGCAPABILITIES);
    }

    @Override
    public boolean registerEntityTypes(PersistenceManager pm) {
        LOGGER.info("Initialising Actuation Types...");
        final ModelRegistry modelRegistry = settings.getModelRegistry();
        final PluginCoreModel pluginCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        modelRegistry.registerEntityType(ACTUATOR)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(pluginCoreModel.EP_NAME, true)
                .registerProperty(pluginCoreModel.EP_DESCRIPTION, true)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE, true)
                .registerProperty(pluginCoreModel.EP_METADATA, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(NP_TASKINGCAPABILITIES, false);
        modelRegistry.registerEntityType(TASK)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(pluginCoreModel.EP_CREATIONTIME, false)
                .registerProperty(EP_TASKINGPARAMETERS, true)
                .registerProperty(NP_TASKINGCAPABILITY, true);
        modelRegistry.registerEntityType(TASKING_CAPABILITY)
                .registerProperty(ModelRegistry.EP_ID, false)
                .registerProperty(ModelRegistry.EP_SELFLINK, false)
                .registerProperty(pluginCoreModel.EP_NAME, true)
                .registerProperty(pluginCoreModel.EP_DESCRIPTION, true)
                .registerProperty(ModelRegistry.EP_PROPERTIES, false)
                .registerProperty(EP_TASKINGPARAMETERS, true)
                .registerProperty(NP_ACTUATOR, true)
                .registerProperty(NP_TASKS, false)
                .registerProperty(pluginCoreModel.NP_THING, true);
        pluginCoreModel.THING.registerProperty(NP_TASKINGCAPABILITIES, false);

        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            TableCollection tableCollection = ppm.getTableCollection();
            DataType idType = tableCollection.getIdType();
            tableCollection.registerTable(ACTUATOR, new TableImpActuators(idType, this, pluginCoreModel));
            tableCollection.registerTable(TASK, new TableImpTasks(idType, this, pluginCoreModel));
            tableCollection.registerTable(TASKING_CAPABILITY, new TableImpTaskingCapabilities(idType, this, pluginCoreModel));
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
