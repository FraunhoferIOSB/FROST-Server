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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing;

import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;

/**
 *
 * @author scf
 */
public class PluginBatchProcessing implements PluginService, PluginRootDocument, ConfigDefaults {

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_BATCH_PROCESSING = "batchProcessing.enable";

    private static final String REQUIREMENT_BATCH_PROCESSING = "http://www.opengis.net/spec/iot_sensing/1.1/req/batch-request/batch-request";

    private CoreSettings settings;

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        boolean enabled = pluginSettings.getBoolean(TAG_ENABLE_BATCH_PROCESSING, getClass());
        if (enabled) {
            settings.getPluginManager().registerPlugin(this);
        }
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
    }

    @Override
    public Collection<String> getUrlPaths() {
        return Arrays.asList(ServiceBatchProcessing.PATH_POST_BATCH);
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Arrays.asList(ServiceBatchProcessing.REQUEST_TYPE_BATCH);
    }

    @Override
    public String getRequestTypeFor(String path, HttpMethod method) {
        switch (method) {
            case POST:
                if (path.equals(ServiceBatchProcessing.PATH_POST_BATCH)) {
                    return ServiceBatchProcessing.REQUEST_TYPE_BATCH;
                }

            default:
                throw new IllegalArgumentException("Method " + method + "not valid for path " + path);
        }
    }

    @Override
    public ServiceResponse execute(Service service, ServiceRequest request) {
        ServiceResponse<String> response = new ServiceBatchProcessing(settings).executeBatchOperation(service, request);
        return response;
    }
}
