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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelsampling;

import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;

import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
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
 * Plugin loader for the Sampling Model plugin.
 */
public class PluginModelSampling implements Plugin, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginModelSampling.class.getName());

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PDQ = "modelSampling.enable";

    private boolean enabled;

    @Override
    public InitResult init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PDQ, PluginModelSampling.class);
        if (enabled) {
            final PluginManager pluginManager = settings.getPluginManager();
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled first, delaying initialisation...");
                return INIT_DELAY;
            }
            pml.addLiquibaseFile("pluginmodelsampling/liquibase/tables.xml");
            pml.addModelFile("pluginmodelsampling/model/PreparationProcedure.json");
            pml.addModelFile("pluginmodelsampling/model/PreparationStep.json");
            pml.addModelFile("pluginmodelsampling/model/Sampler.json");
            pml.addModelFile("pluginmodelsampling/model/Sampling.json");
            pml.addModelFile("pluginmodelsampling/model/SamplingProcedure.json");
            pluginManager.registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
