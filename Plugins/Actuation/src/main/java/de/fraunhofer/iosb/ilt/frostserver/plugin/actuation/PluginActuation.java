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
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_MAP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.DataType;

/**
 *
 * @author scf
 */
public class PluginActuation implements PluginRootDocument, PluginModel, ConfigDefaults {

    public static final EntityPropertyMain<Map<String, Object>> EP_TASKINGPARAMETERS = new EntityPropertyMain<>("taskingParameters", TYPE_REFERENCE_MAP, true, false);

    public static final NavigationPropertyEntity NP_ACTUATOR = new NavigationPropertyMain.NavigationPropertyEntity("Actuator");
    public static final NavigationPropertyEntitySet NP_ACTUATORS = new NavigationPropertyMain.NavigationPropertyEntitySet("Actuators");
    public static final NavigationPropertyEntity NP_TASK = new NavigationPropertyMain.NavigationPropertyEntity("Task");
    public static final NavigationPropertyEntitySet NP_TASKS = new NavigationPropertyMain.NavigationPropertyEntitySet("Tasks");
    public static final NavigationPropertyEntity NP_TASKINGCAPABILITY = new NavigationPropertyMain.NavigationPropertyEntity("TaskingCapability");
    public static final NavigationPropertyEntitySet NP_TASKINGCAPABILITIES = new NavigationPropertyMain.NavigationPropertyEntitySet("TaskingCapabilities");

    public static final EntityType ACTUATOR = new EntityType("Actuator", "Actuators");
    public static final EntityType TASK = new EntityType("Task", "Tasks");
    public static final EntityType TASKING_CAPABILITY = new EntityType("TaskingCapability", "TaskingCapabilities");

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
        EntityPropertyMain.registerProperty(EP_TASKINGPARAMETERS);
        NavigationPropertyMain.registerProperty(NP_ACTUATOR);
        NavigationPropertyMain.registerProperty(NP_ACTUATORS);
        NavigationPropertyMain.registerProperty(NP_TASK);
        NavigationPropertyMain.registerProperty(NP_TASKS);
        NavigationPropertyMain.registerProperty(NP_TASKINGCAPABILITY);
        NavigationPropertyMain.registerProperty(NP_TASKINGCAPABILITIES);
    }

    @Override
    public boolean registerEntityTypes(PersistenceManager pm) {
        EntityType.registerEntityType(ACTUATOR)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.ENCODINGTYPE, true)
                .registerProperty(EntityPropertyMain.METADATA, true)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(NP_TASKINGCAPABILITIES, false);
        EntityType.registerEntityType(TASK)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.CREATIONTIME, false)
                .registerProperty(EP_TASKINGPARAMETERS, true)
                .registerProperty(NP_TASKINGCAPABILITY, true);
        EntityType.registerEntityType(TASKING_CAPABILITY)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(EP_TASKINGPARAMETERS, true)
                .registerProperty(NP_ACTUATOR, true)
                .registerProperty(NP_TASKS, false)
                .registerProperty(NavigationPropertyMain.THING, true);
        EntityType.THING.registerProperty(NP_TASKINGCAPABILITIES, false);

        if (pm instanceof PostgresPersistenceManager) {
            PostgresPersistenceManager ppm = (PostgresPersistenceManager) pm;
            TableCollection tableCollection = ppm.getTableCollection();
            DataType idType = tableCollection.getIdType();
            tableCollection.registerTable(ACTUATOR, TableImpActuators.getInstance(idType));
            tableCollection.registerTable(TASK, TableImpTasks.getInstance(idType));
            tableCollection.registerTable(TASKING_CAPABILITY, TableImpTaskingCapabilities.getInstance(idType));
        }
        return true;
    }

}
