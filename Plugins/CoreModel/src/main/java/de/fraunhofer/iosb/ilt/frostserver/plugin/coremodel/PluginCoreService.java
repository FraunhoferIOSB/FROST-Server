/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import static de.fraunhofer.iosb.ilt.frostserver.service.PluginManager.PATH_WILDCARD;
import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_EMPTY;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_ALL;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGESET;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSONPATCH;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.REQUEST_PARAM_FORMAT;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.Arrays;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class PluginCoreService implements PluginService, ConfigDefaults {

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_CORE_SERVICE = "coreService.enable";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginCoreService.class.getName());

    private boolean enabled;

    public PluginCoreService() {
        LOGGER.info("Creating new Core Service Plugin.");
    }

    @Override
    public void init(CoreSettings settings) {
        final Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_CORE_SERVICE, PluginCoreService.class);
        if (enabled) {
            settings.getPluginManager().registerPlugin(this);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<Version> getVersions() {
        return Arrays.asList(Version.V_1_0, Version.V_1_1);
    }

    @Override
    public boolean definesVersions() {
        return true;
    }

    @Override
    public Collection<String> getVersionedUrlPaths() {
        return Arrays.asList(PATH_WILDCARD);
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Arrays.asList(
                RequestTypeUtils.GET_CAPABILITIES,
                RequestTypeUtils.CREATE,
                RequestTypeUtils.DELETE,
                RequestTypeUtils.READ,
                RequestTypeUtils.UPDATE_ALL,
                RequestTypeUtils.UPDATE_CHANGES,
                RequestTypeUtils.UPDATE_CHANGESET);
    }

    @Override
    public String getRequestTypeFor(Version version, String path, HttpMethod method, String contentType) {
        switch (method) {
            case DELETE:
                return RequestTypeUtils.DELETE;
            case GET:
                if (path.isEmpty() || "/".equals(path)) {
                    return RequestTypeUtils.GET_CAPABILITIES;
                }
                return RequestTypeUtils.READ;
            case PATCH:
                if (!StringHelper.isNullOrEmpty(contentType) && contentType.startsWith(CONTENT_TYPE_APPLICATION_JSONPATCH)) {
                    return RequestTypeUtils.UPDATE_CHANGESET;
                }
                return RequestTypeUtils.UPDATE_CHANGES;
            case POST:
                return RequestTypeUtils.CREATE;
            case PUT:
                return RequestTypeUtils.UPDATE_ALL;
            default:
                return null;
        }
    }

    @Override
    public ServiceResponse execute(Service mainService, ServiceRequest request, ServiceResponse response) {
        switch (request.getRequestType()) {
            case CREATE:
            case UPDATE_ALL:
            case UPDATE_CHANGES:
            case UPDATE_CHANGESET:
                request.addParameterIfAbsent(REQUEST_PARAM_FORMAT, FORMAT_NAME_EMPTY);
                break;
            default:
            // Nothing to do.
        }
        return mainService.execute(request, response);
    }

}
