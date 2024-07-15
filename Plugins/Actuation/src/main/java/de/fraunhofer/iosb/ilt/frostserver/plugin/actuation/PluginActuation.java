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
package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.LiquibaseHelper.CHANGE_SET_NAME;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.actuation.ActuationModelSettings.TAG_ENABLE_ACTUATION;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;
import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_OK;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.PluginCoreModelV2;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
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
import net.time4j.Moment;
import org.jooq.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PluginActuation implements PluginRootDocument, PluginModel, ConfigDefaults, LiquibaseUser {

    public static final String PLUGIN_NAME = "Plugin.Actuation";
    private static final String LIQUIBASE_CHANGELOG_FILENAME = "liquibase/pluginactuation/tables.xml";
    private static final String ACTUATOR = "Actuator";
    private static final String ACTUATORS = "Actuators";
    private static final String TASK = "Task";
    private static final String TASKS = "Tasks";
    private static final String TASKING_CAPABILITY = "TaskingCapability";
    private static final String TASKING_CAPABILITIES = "TaskingCapabilities";

    public static final String LIQUIBASE_NAME_TASKING_CAP = "TaskingCap";
    public static final String LIQUIBASE_NAME_TASK = "Task";
    public static final String LIQUIBASE_NAME_ACTUATOR = "Actuator";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginActuation.class.getName());

    public final EntityPropertyMain<Map<String, Object>> epTaskingParameters = new EntityPropertyMain<>("taskingParameters", TypeComplex.STA_MAP, true, false);
    private EntityPropertyMain<?> epIdActuator;
    private EntityPropertyMain<?> epIdTask;
    private EntityPropertyMain<?> epIdTaskingCap;

    public final NavigationPropertyEntity npActuatorTaskCap = new NavigationPropertyEntity(ACTUATOR, true);
    public final NavigationPropertyEntity npThingTaskCap = new NavigationPropertyEntity("Thing", true);
    public final NavigationPropertyEntitySet npTasksTaskCap = new NavigationPropertyEntitySet(TASKS);

    public final NavigationPropertyEntity npTaskingCapabilityTask = new NavigationPropertyEntity(TASKING_CAPABILITY, npTasksTaskCap, true);
    public final NavigationPropertyEntitySet npTaskingCapabilitiesActuator = new NavigationPropertyEntitySet(TASKING_CAPABILITIES, npActuatorTaskCap);
    public final NavigationPropertyEntitySet npTaskingCapabilitiesThing = new NavigationPropertyEntitySet(TASKING_CAPABILITIES, npThingTaskCap);

    public final EntityType etActuator = new EntityType(ACTUATOR, ACTUATORS);
    public final EntityType etTask = new EntityType(TASK, TASKS);
    public final EntityType etTaskingCapability = new EntityType(TASKING_CAPABILITY, TASKING_CAPABILITIES);

    private static final List<String> REQUIREMENTS_ACTUATION = Arrays.asList(
            "http://www.opengis.net/spec/iot_tasking/1.0/req/tasking-capability",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/task",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/actuator",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/create-tasks-via-mqtt",
            "http://www.opengis.net/spec/iot_tasking/1.0/req/receive-updates-via-mqtt");

    private CoreSettings settings;
    private ActuationModelSettings modelSettings;
    private boolean enabled;
    private boolean fullyInitialised;
    private boolean isVersion2;

    @Override
    public InitResult init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_ACTUATION, ActuationModelSettings.class);
        if (enabled) {
            final PluginManager pluginManager = settings.getPluginManager();
            modelSettings = new ActuationModelSettings(settings);
            boolean pcmV1 = pluginManager.isPluginEnabled(PluginCoreModel.class);
            boolean pcmV2 = pluginManager.isPluginEnabled(PluginCoreModelV2.class);
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            if (pcmV1 && !pcmV2) {
                LOGGER.info("Using STA Version 1 model.");
                isVersion2 = false;
            } else if (!pcmV1 && pcmV2) {
                LOGGER.info("Using STA Version 2 model.");
                isVersion2 = true;
                if (pml == null || !pml.isEnabled()) {
                    LOGGER.warn("PluginModelLoader must be enabled before the Actuation plugin, delaying initialisation...");
                    return INIT_DELAY;
                }
                pml.addLiquibaseFile("actuationv2/liquibase/tables.xml");
                pml.addModelFile("actuationv2/model/Actuator.json");
                pml.addModelFile("actuationv2/model/Task.json");
                pml.addModelFile("actuationv2/model/TaskingCapability.json");
            } else {
                LOGGER.warn("Either CoreModel or CoreModelV2 must be enabled, delaying initialisation...");
                return INIT_DELAY;
            }
            settings.getPluginManager().registerPlugin(this);
        }
        return INIT_OK;
    }

    @Override
    public boolean isFullyInitialised() {
        return fullyInitialised;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        if (isVersion2) {
            // Nothing to do if we run V2.
            return;
        }
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
        if (isVersion2) {
            // Nothing to do if we run V2.
            return;
        }
        LOGGER.info("Initialising Actuation Types...");
        ModelRegistry mr = settings.getModelRegistry();

        mr.registerEntityType(etActuator);
        mr.registerEntityType(etTask);
        mr.registerEntityType(etTaskingCapability);

        epIdActuator = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeActuator)).setAliases("id");
        epIdTask = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeTask)).setAliases("id");
        epIdTaskingCap = new EntityPropertyMain<>(AT_IOT_ID, mr.getPropertyType(modelSettings.idTypeTaskingCap)).setAliases("id");
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        if (isVersion2) {
            // Nothing to do if we run V2.
            return true;
        }
        LOGGER.info("Linking Actuation Types...");
        final PluginCoreModel pluginCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        if (pluginCoreModel == null || !pluginCoreModel.isFullyInitialised()) {
            return false;
        }
        etActuator
                .registerProperty(epIdActuator)
                .registerProperty(pluginCoreModel.epName)
                .registerProperty(pluginCoreModel.epDescription)
                .registerProperty(ModelRegistry.EP_ENCODINGTYPE)
                .registerProperty(pluginCoreModel.epMetadata)
                .registerProperty(ModelRegistry.EP_PROPERTIES)
                .registerProperty(npTaskingCapabilitiesActuator);
        etTask
                .registerProperty(epIdTask)
                .registerProperty(pluginCoreModel.epCreationTime)
                .registerProperty(epTaskingParameters)
                .registerProperty(npTaskingCapabilityTask)
                .addCreateValidator("AC-Task-CrationTime", entity -> {
                    if (entity.getProperty(pluginCoreModel.epCreationTime) == null) {
                        entity.setProperty(pluginCoreModel.epCreationTime, new TimeInstant(Moment.nowInSystemTime()));
                    }
                });
        etTaskingCapability
                .registerProperty(epIdTaskingCap)
                .registerProperty(pluginCoreModel.epName)
                .registerProperty(pluginCoreModel.epDescription)
                .registerProperty(ModelRegistry.EP_PROPERTIES)
                .registerProperty(epTaskingParameters)
                .registerProperty(npActuatorTaskCap)
                .registerProperty(npTasksTaskCap)
                .registerProperty(npThingTaskCap);
        pluginCoreModel.etThing.registerProperty(npTaskingCapabilitiesThing);

        if (pm instanceof JooqPersistenceManager ppm) {
            TableCollection tableCollection = ppm.getTableCollection();
            final DataType dataTypeActr = ppm.getDataTypeFor(modelSettings.idTypeActuator);
            final DataType dataTypeTask = ppm.getDataTypeFor(modelSettings.idTypeTask);
            final DataType dataTypeTCap = ppm.getDataTypeFor(modelSettings.idTypeTaskingCap);
            final DataType dataTypeThng = tableCollection.getTableForType(pluginCoreModel.etThing).getPkFields().get(0).getDataType();
            tableCollection.registerTable(etActuator, new TableImpActuators(dataTypeActr, this, pluginCoreModel));
            tableCollection.registerTable(etTask, new TableImpTasks(dataTypeTask, dataTypeTCap, this, pluginCoreModel));
            tableCollection.registerTable(etTaskingCapability, new TableImpTaskingCapabilities(dataTypeTCap, dataTypeActr, dataTypeThng, this, pluginCoreModel));
        }
        fullyInitialised = true;
        return true;
    }

    public Map<String, Object> createLiqibaseParams(JooqPersistenceManager ppm, Map<String, Object> target) {
        if (target == null) {
            target = new LinkedHashMap<>();
        }
        if (isVersion2) {
            // Nothing to do if we run V2.
            target.put(CHANGE_SET_NAME, PLUGIN_NAME);
            return target;
        }
        PluginCoreModel pCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        pCoreModel.createLiqibaseParams(ppm, target);
        ppm.generateLiquibaseVariables(target, LIQUIBASE_NAME_ACTUATOR, modelSettings.idTypeActuator);
        ppm.generateLiquibaseVariables(target, LIQUIBASE_NAME_TASK, modelSettings.idTypeTask);
        ppm.generateLiquibaseVariables(target, LIQUIBASE_NAME_TASKING_CAP, modelSettings.idTypeTaskingCap);
        target.put(CHANGE_SET_NAME, PLUGIN_NAME);

        return target;
    }

    @Override
    public String checkForUpgrades() {
        if (isVersion2) {
            // Nothing to do if we run V2.
            return "Up to date, no changes to apply: " + PLUGIN_NAME + ".\n";
        }
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof JooqPersistenceManager ppm) {
                return ppm.checkForUpgrades(LIQUIBASE_CHANGELOG_FILENAME, createLiqibaseParams(ppm, null));
            }
            return "Unknown persistence manager class";
        }
    }

    @Override
    public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
        if (isVersion2) {
            out.append("Up to date, no changes to apply: ").append(PLUGIN_NAME).append(".\n");
            // Nothing to do if we run V2.
            return true;
        }
        try (PersistenceManager pm = PersistenceManagerFactory.getInstance(settings).create()) {
            if (pm instanceof JooqPersistenceManager ppm) {
                return ppm.doUpgrades(LIQUIBASE_CHANGELOG_FILENAME, createLiqibaseParams(ppm, null), out);
            }
            out.append("Unknown persistence manager class");
            return false;
        }
    }

}
