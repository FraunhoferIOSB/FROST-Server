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

import static de.fraunhofer.iosb.ilt.frostserver.path.Version.builder;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData.PARAM_METADATA;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData.PATH_METADATA;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.odata.PluginOData.REQUEST_TYPE_METADATA;
import static de.fraunhofer.iosb.ilt.frostserver.service.PluginManager.PATH_WILDCARD;
import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_EMPTY;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_ALL;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGESET;
import static de.fraunhofer.iosb.ilt.frostserver.service.Service.KEY_CONFORMANCE_LIST;
import static de.fraunhofer.iosb.ilt.frostserver.service.Service.KEY_FUNCTIONS;
import static de.fraunhofer.iosb.ilt.frostserver.service.Service.KEY_SERVER_SETTINGS;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSONPATCH;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.REQUEST_PARAM_FORMAT;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.TAG_PREFER_RETURN;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.URI_PATH_SEP;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.EditFeatures;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.MetaDataGenerator;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.deserialize.JsonReaderOData;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata.CsdlDocument.ODataVersion;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize.JsonWriterOdata401;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.Constant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Operator;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginRootDocument;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.settings.ConfigProvider;
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
 * The API of STA version 2.0.
 */
public class PluginCoreServiceV2 extends ConfigProvider<PluginCoreServiceV2> implements PluginRootDocument, PluginService {

    private static final EditFeatures INSERT_STA_20 = new EditFeatures(true, false, false);
    private static final EditFeatures UPDATE_STA_20 = new EditFeatures(true, true, true);

    public static final String VERSION_STA_V20_NAME = "v2.0";

    public static final Version VERSION_STA_2_0 = builder()
            .setUrlPart(VERSION_STA_V20_NAME)
            .setCountName(JsonWriterOdata401.AT_COUNT)
            .setIdName("id")
            .setSelfLinkName(JsonWriterOdata401.AT_ID)
            .setNextLinkName(JsonWriterOdata401.AT_NEXT_LINK)
            .setNavLinkName(JsonWriterOdata401.AT_NAVIGATION_LINK)
            .setCreateFeatures(INSERT_STA_20)
            .setUpdateFeatures(UPDATE_STA_20)
            .registerSytheticProperty(ModelRegistry.EP_SELFLINK)
            .build();

    public static final String SETTINGS_NAMESPACE = "coreServiceV2.";

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE = "enable";

    private CoreSettings coreSettings;
    private boolean enabled;
    private final List<String> functionNames = new ArrayList<>();

    private static final Map<Extension, List<String>> CONFORMANCE_BY_EXTENSION = new HashMap<>();
    private static final String REQ_CLASS_API_CUD = "http://www.opengis.net/spec/sensorthings/2.0/req-class/api/cud";
    private static final String REQ_CLASS_API_READ = "http://www.opengis.net/spec/sensorthings/2.0/req-class/api/read";
    private static final String REQ_CLASS_BINDING_HTTP = "http://www.opengis.net/spec/sensorthings/2.0/req-class/binding/http";
    private static final String REQ_CLASS_BINDING_MQTT = "http://www.opengis.net/spec/sensorthings/2.0/req-class/binding/mqtt";

    static {
        CONFORMANCE_BY_EXTENSION.put(Extension.CORE,
                Arrays.asList(REQ_CLASS_API_READ,
                        REQ_CLASS_API_CUD,
                        "http://www.opengis.net/spec/sensorthings/2.0/req/api/read/options/select_distinct",
                        "http://www.opengis.net/spec/sensorthings/2.0/req/api/cud/deep_update",
                        "http://www.opengis.net/spec/sensorthings/2.0/req/api/cud/json_patch",
                        "http://www.opengis.net/spec/sensorthings/2.0/req/api/cud/replace",
                        REQ_CLASS_BINDING_HTTP,
                        "http://www.opengis.net/spec/sensorthings/2.0/req/binding/http/request_response"));
        CONFORMANCE_BY_EXTENSION.put(Extension.MQTT,
                Arrays.asList(
                        REQ_CLASS_BINDING_MQTT,
                        "http://www.opengis.net/spec/sensorthings/2.0/req/binding/mqtt/request_response",
                        "http://www.opengis.net/spec/sensorthings/2.0/req/binding/mqtt/pub_sub",
                        "http://www.opengis.net/spec/sensorthings/2.0/req/binding/mqtt/pub_sub/select",
                        "http://www.opengis.net/spec/sensorthings/2.0/req/binding/mqtt/simple_create"));
        CONFORMANCE_BY_EXTENSION.put(Extension.MQTT_EXPAND,
                Arrays.asList(
                        "http://www.opengis.net/spec/sensorthings/2.0/req/binding/mqtt/pub_sub/expand"));
        CONFORMANCE_BY_EXTENSION.put(Extension.MQTT_FILTER,
                Arrays.asList(
                        "http://www.opengis.net/spec/sensorthings/2.0/req/binding/mqtt/pub_sub/filter"));
        CONFORMANCE_BY_EXTENSION.put(Extension.FILTERED_DELETES,
                Arrays.asList(
                        "http://www.opengis.net/spec/sensorthings/2.0/req/api/cud/filtered_delete"));
        CONFORMANCE_BY_EXTENSION.put(Extension.ENTITY_LINKING,
                Arrays.asList(
                        "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#NavigationLinks",
                        "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Expand",
                        "https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Filter"));
    }

    @Override
    public InitResult init(CoreSettings settings) {
        this.coreSettings = settings;
        setSettings(settings.getPluginSettings().getSubSettings(SETTINGS_NAMESPACE));
        enabled = getBoolean(TAG_ENABLE);
        if (enabled) {
            settings.getPluginManager().registerPlugin(this);
            settings.getPluginManager().registerPlugin(new PluginResultFormatV2());
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<Version> getVersions() {
        return Arrays.asList(VERSION_STA_2_0);
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
                RequestTypeUtils.UPDATE_CHANGESET,
                REQUEST_TYPE_METADATA);
    }

    @Override
    public String getRequestTypeFor(Version version, String path, HttpMethod method, String contentType) {
        if (version != VERSION_STA_2_0) {
            return null;
        }
        if (path.startsWith(PATH_METADATA)) {
            return REQUEST_TYPE_METADATA;
        }
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
        response.addHeader("OData-Version", "4.01");
        request.setJsonReader(new JsonReaderOData(request.getCoreSettings().getModelRegistry(), request.getUserPrincipal()));
        switch (request.getRequestType()) {
            case REQUEST_TYPE_METADATA:
                return new MetaDataGenerator(coreSettings)
                        .setVersion(ODataVersion.V4_01)
                        .setJsonDefault(true)
                        .generateMetaData(request, response);

            case CREATE:
            case UPDATE_ALL:
            case UPDATE_CHANGES:
            case UPDATE_CHANGESET:
                if (Constants.VALUE_RETURN_MINIMAL.equalsIgnoreCase(request.getParameter(TAG_PREFER_RETURN))) {
                    request.addParameterIfAbsent(REQUEST_PARAM_FORMAT, FORMAT_NAME_EMPTY);
                }
                return mainService.execute(request, response);

            default:
                return mainService.execute(request, response);
        }
    }

    @Override
    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        Version version = request.getVersion();
        if (version != VERSION_STA_2_0) {
            return;
        }

        String path = request.getQueryDefaults().getServiceRootUrl()
                + '/' + request.getVersion().urlPart
                + '/';
        result.put(JsonWriterOdata401.AT_CONTEXT, path + PARAM_METADATA);

        Map<String, Object> serverSettings = (Map<String, Object>) result.computeIfAbsent(KEY_SERVER_SETTINGS, t -> new LinkedHashMap<>());

        final Set<Extension> enabledSettings = coreSettings.getEnabledExtensions();
        Set<String> extensionList = (Set<String>) serverSettings.computeIfAbsent(KEY_CONFORMANCE_LIST, t -> new TreeSet<>());
        for (Extension setting : enabledSettings) {
            if (setting.isExposedFeature()) {
                final List<String> confList = CONFORMANCE_BY_EXTENSION.get(setting);
                if (!StringHelper.isNullOrEmpty(confList)) {
                    extensionList.addAll(confList);
                }
            }
        }
        if (functionNames.isEmpty()) {
            for (var function : coreSettings.getFunctionRegistry().getExpressions()) {
                if (function instanceof Operator) {
                    continue;
                }
                if (function instanceof Constant) {
                    continue;
                }
                functionNames.add(function.getName());
            }
            Collections.sort(functionNames);
        }
        serverSettings.put(KEY_FUNCTIONS, functionNames);
        // ToDo: endpoint bindings
        // ToDo: endpoint Settings

        addHttpEndpoint(serverSettings);
        addMqttEndpoint(serverSettings);
    }

    private void addHttpEndpoint(Map<String, Object> target) {
        Map<String, Object> mqttData = new HashMap<>();
        final String endpointUrl = coreSettings.getQueryDefaults().getServiceRootUrl() + URI_PATH_SEP + VERSION_STA_V20_NAME + URI_PATH_SEP;
        List<String> endpoints = Arrays.asList(endpointUrl);
        mqttData.put("endpoints", endpoints);
        target.put(REQ_CLASS_BINDING_HTTP, mqttData);
    }

    private void addMqttEndpoint(Map<String, Object> target) {
        final MqttSettings mqttSettings = coreSettings.getMqttSettings();
        boolean enableMqtt = mqttSettings.isEnableMqtt();
        if (enableMqtt) {
            List<String> endpoints = mqttSettings.getEndpoints();
            Map<String, Object> mqttData = new HashMap<>();
            mqttData.put("endpoints", endpoints);
            target.put(REQ_CLASS_BINDING_MQTT, mqttData);
        }
    }

}
