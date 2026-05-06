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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata;

import static de.fraunhofer.iosb.ilt.frostserver.service.PluginManager.PATH_WILDCARD;
import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_EMPTY;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.GET_CAPABILITIES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_ALL;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGESET;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSONPATCH;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.REQUEST_PARAM_FORMAT;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.TAG_PREFER_RETURN;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.EditFeatures;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.deserialize.JsonReaderOData;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.metadata.CsdlDocument.ODataVersion;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize.JsonWriterOdata40;
import de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize.JsonWriterOdata401;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.settings.Settings;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueBoolean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

/**
 *
 * @author hylke
 */
public class PluginOData implements PluginService, ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginOData.class.getName());

    private static final EditFeatures INSERT_ODATA_40 = new EditFeatures(true, false, false);
    private static final EditFeatures UPDATE_ODATA_40 = new EditFeatures(false, false, false);
    private static final EditFeatures INSERT_ODATA_401 = INSERT_ODATA_40;
    private static final EditFeatures UPDATE_ODATA_401 = new EditFeatures(true, true, true);

    public static final String VERSION_ODATA_40_NAME = "ODATA_4.0";
    public static final String VERSION_ODATA_401_NAME = "ODATA_4.01";

    public static final Version VERSION_ODATA_40 = Version.builder()
            .setUrlPart(VERSION_ODATA_40_NAME)
            .setCountName(JsonWriterOdata40.AT_COUNT)
            .setIdName("id")
            .setSelfLinkName(JsonWriterOdata40.AT_ID)
            .setNextLinkName(JsonWriterOdata40.AT_NEXT_LINK)
            .setNavLinkName(JsonWriterOdata40.AT_NAVIGATION_LINK)
            .setCreateFeatures(INSERT_ODATA_40)
            .setUpdateFeatures(UPDATE_ODATA_40)
            .registerSytheticProperty(JsonWriterOdata40.AT_ID, StandardProperties.EP_SELFLINK)
            .addResponse(Version.CannedResponseType.NOTHING_FOUND, new Version.CannedResponse(204, "No Content"))
            .build();
    public static final Version VERSION_ODATA_401 = Version.builder()
            .setUrlPart(VERSION_ODATA_401_NAME)
            .setCountName(JsonWriterOdata401.AT_COUNT)
            .setIdName("id")
            .setSelfLinkName(JsonWriterOdata401.AT_ID)
            .setNextLinkName(JsonWriterOdata401.AT_NEXT_LINK)
            .setNavLinkName(JsonWriterOdata401.AT_NAVIGATION_LINK)
            .setCreateFeatures(INSERT_ODATA_401)
            .setUpdateFeatures(UPDATE_ODATA_401)
            .registerSytheticProperty(JsonWriterOdata401.AT_ID, StandardProperties.EP_SELFLINK)
            .addResponse(Version.CannedResponseType.NOTHING_FOUND, new Version.CannedResponse(204, "No Content"))
            .build();

    public static final String PARAM_METADATA = "$metadata";
    public static final String PATH_METADATA = '/' + PARAM_METADATA;
    public static final String REQUEST_TYPE_METADATA = PARAM_METADATA;

    @DefaultValueBoolean(false)
    public static final String TAG_ENABLE_ODATA = "odata.enable";

    private CoreSettings settings;
    private boolean enabled;

    @Override
    public InitResult init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        enabled = pluginSettings.getBoolean(TAG_ENABLE_ODATA, getClass());
        if (enabled) {
            settings.getPluginManager().registerPlugin(this);
            new PluginResultFormatOData().init(settings);
        }
        return InitResult.INIT_OK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<Version> getVersions() {
        return Arrays.asList(VERSION_ODATA_40, VERSION_ODATA_401);
    }

    @Override
    public boolean definesVersions() {
        return true;
    }

    @Override
    public Collection<String> getVersionedUrlPaths() {
        return Arrays.asList(PATH_METADATA, PATH_WILDCARD);
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
        if (version != VERSION_ODATA_40 && version != VERSION_ODATA_401) {
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
        ODataVersion version;
        boolean isOdata401 = false;
        if (request.getVersion() == VERSION_ODATA_40) {
            version = ODataVersion.V4_0;
        } else {
            isOdata401 = true;
            version = ODataVersion.V4_01;
        }
        response.addHeader("OData-Version", version.name);
        request.setJsonReader(new JsonReaderOData(request.getCoreSettings().getModelRegistry(), request.getVersion(), request.getUserPrincipal()));
        switch (request.getRequestType()) {
            case REQUEST_TYPE_METADATA:
                return new MetaDataGenerator(settings)
                        .setVersion(version)
                        .generateMetaData(request, response);

            case GET_CAPABILITIES:
                return executeGetCapabilities(request, response);

            case CREATE:
                if (Constants.VALUE_RETURN_MINIMAL.equalsIgnoreCase(request.getParameter(TAG_PREFER_RETURN))) {
                    request.addParameterIfAbsent(REQUEST_PARAM_FORMAT, FORMAT_NAME_EMPTY);
                }
                request.setUpdateMode(isOdata401 ? INSERT_ODATA_401 : INSERT_ODATA_40);
                return mainService.execute(request, response);

            case UPDATE_ALL:
            case UPDATE_CHANGES:
            case UPDATE_CHANGESET:
                if (Constants.VALUE_RETURN_MINIMAL.equalsIgnoreCase(request.getParameter(TAG_PREFER_RETURN))) {
                    request.addParameterIfAbsent(REQUEST_PARAM_FORMAT, FORMAT_NAME_EMPTY);
                }
                request.setUpdateMode(isOdata401 ? UPDATE_ODATA_401 : UPDATE_ODATA_40);
                return mainService.execute(request, response);

            default:
                return mainService.execute(request, response);
        }
    }

    private ServiceResponse executeGetCapabilities(ServiceRequest request, ServiceResponse response) {
        Map<String, Object> result = new LinkedHashMap<>();
        ModelRegistry modelRegistry = settings.getModelRegistry();

        String path = request.getQueryDefaults().getServiceRootUrl()
                + '/' + request.getVersion().urlPart
                + '/';
        if (request.getVersion() == VERSION_ODATA_40) {
            result.put(JsonWriterOdata40.AT_CONTEXT, path + PARAM_METADATA);
        } else {
            result.put(JsonWriterOdata401.AT_CONTEXT, path + PARAM_METADATA);
        }

        List<LandingPageItem> entitySetList = new ArrayList<>();
        result.put("value", entitySetList);
        for (EntityType entityType : modelRegistry.getEntityTypes(request.getUserPrincipal().isAdmin())) {
            entitySetList.add(new LandingPageItem().generateFrom(entityType, path));
        }

        settings.getPluginManager().modifyServiceDocument(request, result);
        try {
            SimpleJsonMapper.getSimpleObjectMapper().writeValue(response.getWriter(), result);
        } catch (JacksonException ex) {
            LOGGER.error("Failed to generate index document", ex);
        }

        response.setCode(200);
        response.setResult(result);

        return response;
    }

}
