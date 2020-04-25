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
package de.fraunhofer.iosb.ilt.frostserver.plugin.openapi;

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
import static de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod.GET;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author scf
 */
public class PluginOpenApi implements PluginService, PluginRootDocument, ConfigDefaults {

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_OPENAPI = "openApi.enable";

    private static final String REQUIREMENT_OPENAPI = "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/OpenApi/OpenApi.md";

    @Override
    public void init(CoreSettings settings) {
        Settings pluginSettings = settings.getPluginSettings();
        boolean enabled = pluginSettings.getBoolean(TAG_ENABLE_OPENAPI, getClass());
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
        extensionList.add(REQUIREMENT_OPENAPI);
    }

    @Override
    public Collection<String> getUrlPaths() {
        return Arrays.asList(ServiceOpenApi.PATH_GET_OPENAPI_SPEC);
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Arrays.asList(ServiceOpenApi.REQUEST_TYPE_GET_OPENAPI_SPEC);
    }

    @Override
    public String getRequestTypeFor(String path, HttpMethod method) {
        if (GET.equals(method) && path.equals(ServiceOpenApi.PATH_GET_OPENAPI_SPEC)) {
            return ServiceOpenApi.REQUEST_TYPE_GET_OPENAPI_SPEC;
        }
        throw new IllegalArgumentException("Method " + method + "not valid for path " + path);
    }

    @Override
    public ServiceResponse execute(Service service, ServiceRequest request) {
        return new ServiceOpenApi().executeRequest(service, request);
    }
}
