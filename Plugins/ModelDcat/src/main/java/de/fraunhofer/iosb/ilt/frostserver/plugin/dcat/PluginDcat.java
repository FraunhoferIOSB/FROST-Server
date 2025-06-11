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
package de.fraunhofer.iosb.ilt.frostserver.plugin.dcat;

import static de.fraunhofer.iosb.ilt.frostserver.service.InitResult.INIT_DELAY;
import static de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod.GET;
import static de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod.HEAD;

import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.plugin.modelloader.PluginModelLoader;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginModel;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import java.util.Arrays;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin loader for the Projects plugin.
 */
public class PluginDcat implements PluginModel, PluginService, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDcat.class.getName());

    /**
     * The path for the DCAT entry.
     */
    public static final String PATH_GET_DCAT = "/dcat";

    /**
     * The RequestType definition for the get DCAT request type.
     */
    public static final String REQUEST_TYPE_GET_DCAT = "getDcat";

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_PROJECTS = "dcat.enable";

    @DefaultValueBoolean(true)
    public static final String TAG_DEFAULT_RULES = "dcat.enableDefaultRules";

    private boolean enabled;
    private boolean fullyInitialised;
    private CoreSettings coreSettings;

    @Override
    public InitResult init(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        Settings pluginSettings = coreSettings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_PROJECTS, PluginDcat.class);
        if (enabled) {
            final PluginManager pluginManager = coreSettings.getPluginManager();
            PluginModelLoader pml = pluginManager.getPlugin(PluginModelLoader.class);

            if (pml == null || !pml.isEnabled()) {
                LOGGER.warn("PluginModelLoader must be enabled before the DCAT plugin, delaying initialisation...");
                return INIT_DELAY;
            }

            pml.addLiquibaseFile("plugindcat/sta1/liquibase/tables.xml");

            pml.addModelFile("plugindcat/sta1/model/Agent.json");
            pml.addModelFile("plugindcat/sta1/model/DataService.json");
            pml.addModelFile("plugindcat/sta1/model/Dataset.json");
            pml.addModelFile("plugindcat/sta1/model/Distribution.json");
            pml.addModelFile("plugindcat/sta1/model/License.json");
            pml.addModelFile("plugindcat/sta1/model/Standard.json");

            pluginManager.registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void registerEntityTypes() {
        // Nothing to do here.
    }

    @Override
    public boolean linkEntityTypes(PersistenceManager pm) {
        fullyInitialised = true;
        return true;
    }

    @Override
    public boolean isFullyInitialised() {
        return fullyInitialised;
    }

    @Override
    public Collection<Version> getVersions() {
        return coreSettings.getPluginManager().getVersions().values();
    }

    @Override
    public Collection<String> getVersionedUrlPaths() {
        return Arrays.asList(PATH_GET_DCAT);
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Arrays.asList(REQUEST_TYPE_GET_DCAT);
    }

    @Override
    public String getRequestTypeFor(Version version, String path, HttpMethod method, String contentType) {
        if ((GET == method || HEAD == method) && path.equals(PATH_GET_DCAT)) {
            return REQUEST_TYPE_GET_DCAT;
        }
        return null;
    }

    @Override
    public ServiceResponse execute(Service mainService, ServiceRequest request, ServiceResponse response) {
        PersistenceManager pm = mainService.getPm();
        // http://localhost:8080/FROST-Server/v1.1/Datasets?$expand=Creators,Publisher,Distributions($expand=AccessServices,ConformsTo),DataServices,ConformsTo
        ResourcePath path = new ResourcePath(coreSettings.getQueryDefaults().getServiceRootUrl(), request.getVersion(), "/Datasets");
        Query query = new Query(coreSettings.getModelRegistry(), coreSettings.getQueryDefaults(), path, request.getUserPrincipal());
        EntitySet datasets = pm.get(path, query, EntitySet.class);
        return response;
    }

}
