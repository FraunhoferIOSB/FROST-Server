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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2;

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
 *
 * @author scf
 */
public class PluginCoreModelV2 implements Plugin, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginCoreModelV2.class.getName());

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PCMV2 = "coreModelV2.enable";

    private boolean enabled;

    @Override
    public InitResult init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PCMV2, PluginCoreModelV2.class);
        if (enabled) {
            final PluginManager pluginManager = settings.getPluginManager();
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled first, delaying initialisation...");
                return INIT_DELAY;
            }
            pml.addLiquibaseFile("plugincoremodelv2/liquibase/tables.xml");
            pml.addModelFile("plugincoremodelv2/model/Datastream.json");
            pml.addModelFile("plugincoremodelv2/model/Feature.json");
            pml.addModelFile("plugincoremodelv2/model/FeatureType.json");
            pml.addModelFile("plugincoremodelv2/model/HistoricalLocation.json");
            pml.addModelFile("plugincoremodelv2/model/Location.json");
            pml.addModelFile("plugincoremodelv2/model/Observation.json");
            pml.addModelFile("plugincoremodelv2/model/ObservedProperty.json");
            pml.addModelFile("plugincoremodelv2/model/Sensor.json");
            pml.addModelFile("plugincoremodelv2/model/Thing.json");
            pluginManager.registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
