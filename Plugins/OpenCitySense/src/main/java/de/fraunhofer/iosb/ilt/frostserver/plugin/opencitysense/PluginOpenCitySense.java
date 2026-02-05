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
package de.fraunhofer.iosb.ilt.frostserver.plugin.opencitysense;

import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;
import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_OK;

import de.fraunhofer.iosb.ilt.frostserver.plugin.actuation.PluginActuation;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.PluginCoreModelV2;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelom.PluginModelOM;
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
 * Plugin loader for the Relations Model plugin.
 */
public class PluginOpenCitySense implements Plugin, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginOpenCitySense.class.getName());

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PDQ = "openCitySense.enable";

    private boolean enabled;

    @Override
    public InitResult init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PDQ, PluginOpenCitySense.class);
        if (enabled) {
            final PluginManager pluginManager = settings.getPluginManager();
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled first, delaying initialisation...");
                return INIT_DELAY;
            }
            boolean pCoreModelV1 = pluginManager.isPluginEnabled(PluginCoreModel.class);
            boolean pCoreModelV2 = pluginManager.isPluginEnabled(PluginCoreModelV2.class);
            boolean pActuation = pluginManager.isPluginEnabled(PluginActuation.class);
            boolean pModelOM_V2 = pluginManager.isPluginEnabled(PluginModelOM.class);
            if (pCoreModelV1 && !pCoreModelV2) {
                // Dealing with Model v1.1
                if (!pActuation) {
                    LOGGER.warn("Actuation plugin must be enabled, delaying initialisation! (plugins_actuation_enable=true)");
                    return INIT_DELAY;
                }
                pml.addLiquibaseFile("pluginopencitysense/sta1/liquibase/tables.xml");
                pml.addModelFile("pluginopencitysense/sta1/model/Configuration.json");
                pml.addModelFile("pluginopencitysense/sta1/model/Decoder.json");
                pml.addModelFile("pluginopencitysense/sta1/model/DeviceModel.json");
                pml.addModelFile("pluginopencitysense/sta1/model/DeviceSecret.json");
                pml.addModelFile("pluginopencitysense/sta1/model/Sensor.json");
                pml.addModelFile("pluginopencitysense/sta1/model/Thing.json");
                pml.addModelFile("pluginopencitysense/sta1/model/Task.json");
            } else if (!pCoreModelV1 && pCoreModelV2) {
                // Dealing with Model v2.0
                if (!pActuation) {
                    LOGGER.warn("Actuation plugin must be enabled, delaying initialisation! (plugins_actuation_enable=true)");
                    return INIT_DELAY;
                }
                if (!pModelOM_V2) {
                    LOGGER.warn("Model-OM plugin must be enabled, delaying initialisation! (plugins_modelOM_enable=true)");
                    return INIT_DELAY;
                }
                pml.addLiquibaseFile("pluginopencitysense/sta2/liquibase/tables.xml");
                pml.addModelFile("pluginopencitysense/sta2/model/Configuration.json");
                pml.addModelFile("pluginopencitysense/sta2/model/Decoder.json");
                pml.addModelFile("pluginopencitysense/sta2/model/DeviceModel.json");
                pml.addModelFile("pluginopencitysense/sta2/model/DeviceSecret.json");
                pml.addModelFile("pluginopencitysense/sta2/model/Thing.json");
            } else {
                LOGGER.warn("Either CoreModel with Actuation extension or CoreModelV2 with OM and Actuation Extensions must be enabled, delaying initialisation...");
                return INIT_DELAY;
            }
            pluginManager.registerPlugin(this);
        }
        return INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
