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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;

/**
 *
 * @author hylke
 */
public final class CoreModelSettings implements ConfigDefaults {

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_CORE_MODEL = "coreModel.enable";
    @DefaultValueBoolean(false)
    public static final String TAG_CAN_EDIT_DS_PHENTIME = "coreModel.editableDsProperties";
    @DefaultValue("LONG")
    public static final String TAG_ID_TYPE_DEFAULT = "coreModel.idType";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_DATASTREAM = "coreModel.idType.datastream";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_FEATURE = "coreModel.idType.feature";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_HIST_LOCATION = "coreModel.idType.historicalLocation";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_LOCATION = "coreModel.idType.location";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_OBS_PROPERTY = "coreModel.idType.observedProperty";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_OBSERVATION = "coreModel.idType.observation";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_SENSOR = "coreModel.idType.sensor";
    @DefaultValue("")
    public static final String TAG_ID_TYPE_THING = "coreModel.idType.thing";

    public final String idTypeDefault;
    public final String idTypeDatastream;
    public final String idTypeFeature;
    public final String idTypeHistLoc;
    public final String idTypeLocation;
    public final String idTypeObsProp;
    public final String idTypeObservation;
    public final String idTypeSensor;
    public final String idTypeThing;
    public final boolean dsPropsEditable;

    public CoreModelSettings(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        idTypeDefault = pluginSettings.get(TAG_ID_TYPE_DEFAULT, CoreModelSettings.class).toUpperCase();
        idTypeDatastream = pluginSettings.get(TAG_ID_TYPE_DATASTREAM, idTypeDefault).toUpperCase();
        idTypeFeature = pluginSettings.get(TAG_ID_TYPE_FEATURE, idTypeDefault).toUpperCase();
        idTypeHistLoc = pluginSettings.get(TAG_ID_TYPE_HIST_LOCATION, idTypeDefault).toUpperCase();
        idTypeLocation = pluginSettings.get(TAG_ID_TYPE_LOCATION, idTypeDefault).toUpperCase();
        idTypeObsProp = pluginSettings.get(TAG_ID_TYPE_OBS_PROPERTY, idTypeDefault).toUpperCase();
        idTypeObservation = pluginSettings.get(TAG_ID_TYPE_OBSERVATION, idTypeDefault).toUpperCase();
        idTypeSensor = pluginSettings.get(TAG_ID_TYPE_SENSOR, idTypeDefault).toUpperCase();
        idTypeThing = pluginSettings.get(TAG_ID_TYPE_THING, idTypeDefault).toUpperCase();
        dsPropsEditable = pluginSettings.getBoolean(TAG_CAN_EDIT_DS_PHENTIME, CoreModelSettings.class);
    }
}
