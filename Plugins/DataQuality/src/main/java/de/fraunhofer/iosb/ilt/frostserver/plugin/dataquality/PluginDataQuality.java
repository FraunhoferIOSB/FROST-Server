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
package de.fraunhofer.iosb.ilt.frostserver.plugin.dataquality;

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
public class PluginDataQuality implements Plugin, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDataQuality.class.getName());
    private static final String INIT_FAILED = "PluginModelLoader must be enabled and initialised before the DataQuality plugin!";

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PDQ = "dataQuality.enable";

    private boolean enabled;

    public PluginDataQuality() {
        LOGGER.info("Creating new DataQuality Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PDQ, PluginDataQuality.class);
        if (enabled) {
            final PluginManager pluginManager = settings.getPluginManager();
            pluginManager.registerPlugin(this);
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.error(INIT_FAILED);
                throw new IllegalArgumentException(INIT_FAILED);
            }
            pml.addModelFile("plugindataquality/model/HistoricalQualityAnnotation.json");
            pml.addModelFile("plugindataquality/model/QualityAnnotation.json");
            pml.addModelFile("plugindataquality/model/QualityMeasure.json");
            pml.addModelFile("plugindataquality/model/QualityScheme.json");
            pml.addModelFile("plugindataquality/model/QualitySetup.json");
            pml.addLiquibaseFile("plugindataquality/liquibase/tables.xml");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
