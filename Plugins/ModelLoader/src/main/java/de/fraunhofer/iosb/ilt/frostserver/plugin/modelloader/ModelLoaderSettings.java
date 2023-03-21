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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader;

import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;

/**
 *
 * @author hylke
 */
public final class ModelLoaderSettings implements ConfigDefaults {

    public static final String PLUGIN_NAME = "modelLoader";

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_MODELLOADER = PLUGIN_NAME + ".enable";
    @DefaultValue("")
    public static final String TAG_MODEL_PATH = PLUGIN_NAME + ".modelPath";
    @DefaultValue("")
    public static final String TAG_MODEL_FILES = PLUGIN_NAME + ".modelFiles";
    @DefaultValue("")
    public static final String TAG_LIQUIBASE_PATH = PLUGIN_NAME + ".liquibasePath";
    @DefaultValue("")
    public static final String TAG_LIQUIBASE_FILES = PLUGIN_NAME + ".liquibaseFiles";

    public final String idTypeDefault;

    public ModelLoaderSettings(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        idTypeDefault = pluginSettings.get(CoreModelSettings.TAG_ID_TYPE_DEFAULT, CoreModelSettings.class).toUpperCase();
    }
}
