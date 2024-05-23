/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelom;

import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
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
public class PluginModelOM implements Plugin, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginModelOM.class.getName());
    private static final String INIT_FAILED = "PluginModelLoader must be enabled and initialised before the ModelOM plugin!";

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PDQ = "modelOM.enable";

    private boolean enabled;

    public PluginModelOM() {
        LOGGER.info("Creating new ModelOM Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PDQ, PluginModelOM.class);
        if (enabled) {
            final PluginManager pluginManager = settings.getPluginManager();
            pluginManager.registerPlugin(this);
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.error(INIT_FAILED);
                throw new IllegalArgumentException(INIT_FAILED);
            }
            pml.addLiquibaseFile("pluginmodelom/liquibase/tables.xml");
            pml.addModelFile("pluginmodelom/model/Deployment.json");
            pml.addModelFile("pluginmodelom/model/ObservingProcedure.json");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
