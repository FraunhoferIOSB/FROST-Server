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

import static de.fraunhofer.iosb.ilt.frostserver.path.Version.builder;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NEXT_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.service.PluginManager.PATH_WILDCARD;
import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_EMPTY;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_ALL;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGESET;
import static de.fraunhofer.iosb.ilt.frostserver.service.Service.KEY_CONFORMANCE_LIST;
import static de.fraunhofer.iosb.ilt.frostserver.service.Service.KEY_SERVER_SETTINGS;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSONPATCH;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.REQUEST_PARAM_FORMAT;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.EditFeatures;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The API of STA version 1.0 and 1.1.
 */
public class PluginCoreService implements PluginRootDocument, PluginService, ConfigDefaults {

    private static final EditFeatures INSERT_STA_11 = new EditFeatures(true, false, false);
    private static final EditFeatures UPDATE_STA_11 = new EditFeatures(false, false, false);

    public static final String VERSION_STA_V10_NAME = "v1.0";
    public static final String VERSION_STA_V11_NAME = "v1.1";
    public static final Version V_1_0 = builder()
            .setUrlPart(VERSION_STA_V10_NAME)
            .setCountName(AT_IOT_COUNT)
            .setIdName(AT_IOT_ID)
            .setSelfLinkName(AT_IOT_SELF_LINK)
            .setNextLinkName(AT_IOT_NEXT_LINK)
            .setNavLinkName(AT_IOT_NAVIGATION_LINK)
            .setCreateFeatures(INSERT_STA_11)
            .setUpdateFeatures(UPDATE_STA_11)
            .registerSytheticProperty(ModelRegistry.EP_SELFLINK)
            .build();
    public static final Version V_1_1 = builder()
            .setUrlPart(VERSION_STA_V11_NAME)
            .setCountName(AT_IOT_COUNT)
            .setIdName(AT_IOT_ID)
            .setSelfLinkName(AT_IOT_SELF_LINK)
            .setNextLinkName(AT_IOT_NEXT_LINK)
            .setNavLinkName(AT_IOT_NAVIGATION_LINK)
            .setCreateFeatures(INSERT_STA_11)
            .setUpdateFeatures(UPDATE_STA_11)
            .registerSytheticProperty(ModelRegistry.EP_SELFLINK)
            .build();

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_CORE_SERVICE = "coreService.enable";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginCoreService.class.getName());

    private boolean enabled;
    private CoreSettings settings;

    @Override
    public InitResult init(CoreSettings settings) {
        final Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_CORE_SERVICE, PluginCoreService.class);
        if (enabled) {
            this.settings = settings;
            settings.getPluginManager().registerPlugin(this);
            settings.getPluginManager().registerPlugin(new PluginResultFormatSta());
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
        if (version != PluginCoreService.V_1_1) {
            return;
        }
        Map<String, Object> serverSettings = (Map<String, Object>) result.computeIfAbsent(KEY_SERVER_SETTINGS, t -> new LinkedHashMap<>());

        final Set<Extension> enabledSettings = settings.getEnabledExtensions();
        Set<String> extensionList = new TreeSet<>();
        serverSettings.put(KEY_CONFORMANCE_LIST, extensionList);
        for (Extension setting : enabledSettings) {
            if (setting.isExposedFeature()) {
                extensionList.addAll(setting.getRequirements());
            }
        }
        settings.getMqttSettings().fillServerSettings(serverSettings);
    }

    @Override
    public Collection<Version> getVersions() {
        return Arrays.asList(V_1_0, V_1_1);
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

            case HEAD:
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
                return mainService.execute(request, response);

            default:
                return mainService.execute(request, response);
        }
    }

}
