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
public final class ActuationModelSettings implements ConfigDefaults {

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_ACTUATION = "actuation.enable";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_ACTUATOR = "actuation.idType.actuator";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_TASK = "actuation.idType.task";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_TASKINGCAP = "actuation.idType.taskingCapability";

    public final String idTypeDefault;
    public final String idTypeActuator;
    public final String idTypeTask;
    public final String idTypeTaskingCap;

    public ActuationModelSettings(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        idTypeDefault = pluginSettings.get(CoreModelSettings.TAG_ID_TYPE_DEFAULT, CoreModelSettings.class).toUpperCase();
        idTypeActuator = pluginSettings.get(TAG_ID_TYPE_ACTUATOR, idTypeDefault).toUpperCase();
        idTypeTask = pluginSettings.get(TAG_ID_TYPE_TASK, idTypeDefault).toUpperCase();
        idTypeTaskingCap = pluginSettings.get(TAG_ID_TYPE_TASKINGCAP, idTypeDefault).toUpperCase();
    }
}
