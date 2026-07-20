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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.settings.ConfigProvider;
import de.fraunhofer.iosb.ilt.settings.Settings;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueBoolean;

/**
 * The settings, with defaults, for the ModelLoader.
 */
public final class ModelLoaderSettings extends ConfigProvider<ModelLoaderSettings> {

    public static final String PLUGIN_NAME = "modelLoader.";

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_MODELLOADER = "enable";
    @DefaultValue("")
    public static final String TAG_MODEL_PATH = "modelPath";
    @DefaultValue("")
    public static final String TAG_MODEL_FILES = "modelFiles";
    @DefaultValue("")
    public static final String TAG_LIQUIBASE_PATH = "liquibasePath";
    @DefaultValue("")
    public static final String TAG_LIQUIBASE_FILES = "liquibaseFiles";
    @DefaultValue("")
    public static final String TAG_SECURITY_PATH = "securityPath";
    @DefaultValue("")
    public static final String TAG_SECURITY_FILES = "securityFiles";
    @DefaultValue("")
    public static final String TAG_METADATA_DATA = "metadataData";
    @DefaultValue("")
    public static final String TAG_METADATA_PATH = "metadataPath";
    @DefaultValue("")
    public static final String TAG_METADATA_FILES = "metadataFiles";

    public ModelLoaderSettings(CoreSettings cs) {
        super(cs.getPluginSettings().getSubSettings(PLUGIN_NAME));
    }

    public ModelLoaderSettings(Settings settings) {
        super(settings);
    }

}
