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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

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
public final class MdsModelSettings implements ConfigDefaults {

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_MDS_MODEL = "multiDatastream.enable";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_MULTIDATASTREAM = "multiDatastream.idType.multiDatastream";

    public final String idTypeDefault;
    public final String idTypeMultiDatastream;

    public MdsModelSettings(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        idTypeDefault = pluginSettings.get(CoreModelSettings.TAG_ID_TYPE_DEFAULT, MdsModelSettings.class).toUpperCase();
        idTypeMultiDatastream = pluginSettings.get(TAG_ID_TYPE_MULTIDATASTREAM, idTypeDefault).toUpperCase();
    }
}
