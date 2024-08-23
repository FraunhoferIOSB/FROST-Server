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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing;

import static de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod.POST;

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
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
import java.util.Map;
import java.util.Set;

/**
 * Plugin that handles batch requests.
 */
public class PluginBatchProcessing implements PluginService, PluginRootDocument, ConfigDefaults {

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_BATCH_PROCESSING = "batchProcessing.enable";

    private static final String REQUIREMENT_BATCH_PROCESSING = "http://www.opengis.net/spec/iot_sensing/1.1/req/batch-request/batch-request";
    private static final String REQUIREMENT_JSON_BATCH_PROCESSING = "https://fraunhoferiosb.github.io/FROST-Server/extensions/JsonBatchRequest.html";

    private CoreSettings settings;
    private boolean enabled;

    @Override
    public InitResult init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_BATCH_PROCESSING, getClass());
        if (enabled) {
            settings.getPluginManager().registerPlugin(this);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        Map<String, Object> serverSettings = (Map<String, Object>) result.get(Service.KEY_SERVER_SETTINGS);
        if (serverSettings == null) {
            // Nothing to add to.
            return;
        }
        Set<String> extensionList = (Set<String>) serverSettings.get(Service.KEY_CONFORMANCE_LIST);
        extensionList.add(REQUIREMENT_BATCH_PROCESSING);
        extensionList.add(REQUIREMENT_JSON_BATCH_PROCESSING);
    }

    @Override
    public Collection<Version> getVersions() {
        return settings.getPluginManager().getVersions().values();
    }

    @Override
    public Collection<String> getVersionedUrlPaths() {
        return Arrays.asList(ServiceBatchProcessing.PATH_POST_BATCH);
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Arrays.asList(ServiceBatchProcessing.REQUEST_TYPE_BATCH);
    }

    @Override
    public String getRequestTypeFor(Version version, String path, HttpMethod method, String contentType) {
        if (method.equals(POST) && path.equals(ServiceBatchProcessing.PATH_POST_BATCH)) {
            return ServiceBatchProcessing.REQUEST_TYPE_BATCH;
        }
        throw new IllegalArgumentException("Method " + method + "not valid for path " + path);
    }

    @Override
    public ServiceResponse execute(Service service, ServiceRequest request, ServiceResponse response) {
        return new ServiceBatchProcessing(settings)
                .executeBatchOperation(service, request, response);
    }
}
