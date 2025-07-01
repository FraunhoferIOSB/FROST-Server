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

import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings.TAG_ENABLE_CORE_MODEL;
import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.Plugin;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The core data model of STA Version 2.0.
 */
public class PluginCoreModelV2 implements Plugin, PluginRootDocument, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginCoreModelV2.class.getName());
    private static final List<String> REQUIREMENTS_CORE_MODEL = Arrays.asList(
            "http://www.opengis.net/spec/sensorthings/2.0/req-class/datamodel/core");

    public static final String CONFORMANCE_CLASS_CORE_MODEL = "http://www.opengis.net/spec/sensorthings/2.0/req-class/datamodel/core";

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PCMV2 = "coreModelV2.enable";

    private CoreSettings settings;
    private boolean enabled;

    @Override
    public InitResult init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        boolean enabledV1 = pluginSettings.getBoolean(TAG_ENABLE_CORE_MODEL, CoreModelSettings.class);
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PCMV2, PluginCoreModelV2.class);
        if (enabled) {
            if (enabledV1) {
                LOGGER.error("Can not enable both CoreModelV1 and CoreModelV2");
                return InitResult.INIT_FAILED;
            }
            final PluginManager pluginManager = settings.getPluginManager();
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);
            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled first, delaying initialisation...");
                return INIT_DELAY;
            }
            pml.addLiquibaseFile("plugincoremodelv2/liquibase/tables.xml")
                    .addModelFile("plugincoremodelv2/model/Datastream.json")
                    .addModelFile("plugincoremodelv2/model/Feature.json")
                    .addModelFile("plugincoremodelv2/model/FeatureType.json")
                    .addModelFile("plugincoremodelv2/model/HistoricalLocation.json")
                    .addModelFile("plugincoremodelv2/model/Location.json")
                    .addModelFile("plugincoremodelv2/model/Observation.json")
                    .addModelFile("plugincoremodelv2/model/ObservedProperty.json")
                    .addModelFile("plugincoremodelv2/model/Sensor.json")
                    .addModelFile("plugincoremodelv2/model/Thing.json")
                    .addConformanceItem(CONFORMANCE_CLASS_CORE_MODEL);
            pluginManager.registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        Version version = request.getVersion();
        if (version != PluginCoreServiceV2.VERSION_STA_2_0) {
            return;
        }
        Map<String, Object> serverSettings = (Map<String, Object>) result.computeIfAbsent(Service.KEY_SERVER_SETTINGS, t -> new LinkedHashMap<>());

        final Set<Extension> enabledSettings = settings.getEnabledExtensions();
        Set<String> extensionList = (Set<String>) serverSettings.computeIfAbsent(Service.KEY_CONFORMANCE_LIST, t -> new TreeSet<>());
        for (Extension setting : enabledSettings) {
            if (setting.isExposedFeature()) {
                extensionList.addAll(setting.getRequirements());
            }
        }

        settings.getMqttSettings().fillServerSettings(serverSettings);

        extensionList.addAll(REQUIREMENTS_CORE_MODEL);
    }

}
