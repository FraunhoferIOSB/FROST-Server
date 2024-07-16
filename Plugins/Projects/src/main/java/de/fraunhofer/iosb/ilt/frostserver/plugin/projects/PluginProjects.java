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
package de.fraunhofer.iosb.ilt.frostserver.plugin.projects;

import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;

import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
import de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.Plugin;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin loader for the Projects plugin.
 */
public class PluginProjects implements Plugin, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginProjects.class.getName());

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PROJECTS = "projects.enable";

    @DefaultValueBoolean(true)
    public static final String TAG_DEFAULT_RULES = "projects.enableDefaultRules";

    private boolean enabled;

    @Override
    public InitResult init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PROJECTS, PluginProjects.class);
        if (enabled) {
            boolean rules = pluginSettings.getBoolean(TAG_DEFAULT_RULES, PluginProjects.class);
            final PluginManager pluginManager = settings.getPluginManager();
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            boolean pCoreModelV1 = pluginManager.isPluginEnabled(PluginCoreModel.class);
            boolean multiDatastream = pluginManager.isPluginEnabled(PluginMultiDatastream.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled before the Projects plugin, delaying initialisation...");
                return INIT_DELAY;
            }
            if (!pCoreModelV1) {
                LOGGER.warn("PluginCoreModelV1 must be enabled before the Projects plugin, delaying initialisation...");
                return INIT_DELAY;
            }
            pml.addLiquibaseFile("pluginprojects/sta1/liquibase/tables.xml");

            pml.addModelFile("pluginprojects/sta1/model/Project.json");
            pml.addModelFile("pluginprojects/sta1/model/Restricted.json");
            pml.addModelFile("pluginprojects/sta1/model/Role.json");
            pml.addModelFile("pluginprojects/sta1/model/User.json");
            pml.addModelFile("pluginprojects/sta1/model/UserProjectRole.json");
            if (multiDatastream) {
                pml.addModelFile("pluginprojects/sta1/model/RestrictedMd.json");
            }

            if (rules) {
                pml.addSecurityFile("pluginprojects/sta1/model/secDatastream.json");
                pml.addSecurityFile("pluginprojects/sta1/model/secFeature.json");
                pml.addSecurityFile("pluginprojects/sta1/model/secHistoricalLocation.json");
                pml.addSecurityFile("pluginprojects/sta1/model/secLocation.json");
                if (multiDatastream) {
                    pml.addSecurityFile("pluginprojects/model/secMultiDatastream.json");
                }
                pml.addSecurityFile("pluginprojects/sta1/security/secObservation.json");
                pml.addSecurityFile("pluginprojects/sta1/security/secProject.json");
                pml.addSecurityFile("pluginprojects/sta1/security/secRole.json");
                pml.addSecurityFile("pluginprojects/sta1/security/secSensor.json");
                pml.addSecurityFile("pluginprojects/sta1/security/secThing.json");
                pml.addSecurityFile("pluginprojects/sta1/security/secUser.json");
                pml.addSecurityFile("pluginprojects/sta1/security/secUserProjectRole.json");
            }
            pluginManager.registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
