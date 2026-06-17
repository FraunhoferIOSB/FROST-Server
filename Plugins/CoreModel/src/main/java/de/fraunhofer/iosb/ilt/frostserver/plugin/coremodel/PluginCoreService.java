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
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.EditFeatures;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.settings.Settings;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueBoolean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
            .registerSytheticProperty(StandardProperties.EP_SELFLINK)
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
            .registerSytheticProperty(StandardProperties.EP_SELFLINK)
            .build();

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_CORE_SERVICE = "coreService.enable";

    private static final Map<Extension, List<String>> CONFORMANCE_BY_EXTENSION = new HashMap<>();

    static {
        CONFORMANCE_BY_EXTENSION.put(Extension.CORE, Arrays.asList(
                "http://www.opengis.net/spec/iot_sensing/1.1/req/resource-path/resource-path-to-entities",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete",
                "https://fraunhoferiosb.github.io/FROST-Server/extensions/DeepSelect.html",
                "https://fraunhoferiosb.github.io/FROST-Server/extensions/SelectDistinct.html",
                "https://fraunhoferiosb.github.io/FROST-Server/extensions/ResponseMetadata.html"));
        CONFORMANCE_BY_EXTENSION.put(Extension.MQTT, Arrays.asList(
                "http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/receive-updates-via-mqtt/receive-updates"));
        CONFORMANCE_BY_EXTENSION.put(Extension.MQTT_EXPAND, Arrays.asList(
                "https://fraunhoferiosb.github.io/FROST-Server/extensions/MqttExpand.html"));
        CONFORMANCE_BY_EXTENSION.put(Extension.MQTT_FILTER, Arrays.asList(
                "https://fraunhoferiosb.github.io/FROST-Server/extensions/MqttFilter.html"));
        CONFORMANCE_BY_EXTENSION.put(Extension.FILTERED_DELETES, Arrays.asList(
                "https://fraunhoferiosb.github.io/FROST-Server/extensions/FilteredDelete.html"));
        CONFORMANCE_BY_EXTENSION.put(Extension.ENTITY_LINKING, Arrays.asList(
                "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#NavigationLinks",
                "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Expand",
                "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Filter"));
    }

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
        if (version != PluginCoreService.V_1_0 && version != PluginCoreService.V_1_1) {
            return;
        }

        String path = request.getQueryDefaults().getServiceRootUrl()
                + '/' + request.getVersion().urlPart
                + '/';
        final List<Map<String, String>> entitySets = new ArrayList<>();
        result.put("value", entitySets);
        final ModelRegistry mr = request.getCoreSettings().getModelRegistry();
        for (EntityType entityType : mr.getEntityTypes(request.getUserPrincipal().isAdmin())) {
            String collectionUri = path + entityType.plural;
            entitySets.add(createCapability(entityType.plural, collectionUri));
        }

        if (version != PluginCoreService.V_1_1) {
            return;
        }
        Map<String, Object> serverSettings = (Map<String, Object>) result.computeIfAbsent(KEY_SERVER_SETTINGS, t -> new LinkedHashMap<>());

        final Set<Extension> enabledSettings = settings.getEnabledExtensions();
        Set<String> extensionList = new TreeSet<>();
        serverSettings.put(KEY_CONFORMANCE_LIST, extensionList);
        for (Extension setting : enabledSettings) {
            if (setting.isExposedFeature()) {
                final List<String> confList = CONFORMANCE_BY_EXTENSION.get(setting);
                if (!StringHelper.isNullOrEmpty(confList)) {
                    extensionList.addAll(confList);
                }
            }
        }
        addMqttData(serverSettings);
    }

    private Map<String, String> createCapability(String name, String url) {
        Map<String, String> val = new HashMap<>();
        val.put("name", name);
        val.put("url", url);
        return Collections.unmodifiableMap(val);
    }

    private void addMqttData(Map<String, Object> target) {
        final MqttSettings mqttSettings = settings.getMqttSettings();
        boolean enableMqtt = mqttSettings.isEnableMqtt();
        if (enableMqtt) {
            List<String> endpoints = mqttSettings.getEndpoints();
            for (String requirement : CONFORMANCE_BY_EXTENSION.get(Extension.MQTT)) {
                Map<String, Object> mqttData = new HashMap<>();
                mqttData.put("endpoints", endpoints);
                target.put(requirement, mqttData);
            }
        }
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
                request.addParameter(REQUEST_PARAM_FORMAT, FORMAT_NAME_EMPTY);
                return mainService.execute(request, response);

            default:
                return mainService.execute(request, response);
        }
    }

}
