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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray;

import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.json.DataArrayResultSerializer;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.json.DataArrayValueSerializer;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat;
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
 *
 * @author scf
 */
public class PluginResultFormatDataArray implements PluginResultFormat, PluginService, PluginRootDocument, ConfigDefaults {

    @DefaultValueBoolean(true)
    public static final String TAG_ENABLE_DATA_ARRAY = "dataArray.enable";

    private static final String REQUIREMENT_DATA_ARRAY = "http://www.opengis.net/spec/iot_sensing/1.1/req/data-array/data-array";
    /**
     * The "name" of the dataArray resultFormatter.
     */
    public static final String DATA_ARRAY_FORMAT_NAME = "dataArray";

    private static boolean modifiedEntityFormatter = false;

    private CoreSettings settings;
    private boolean enabled;

    @Override
    public void init(CoreSettings settings) {
        this.settings = settings;
        Settings pluginSettings = settings.getPluginSettings();
        boolean enabled = pluginSettings.getBoolean(TAG_ENABLE_DATA_ARRAY, getClass());
        if (enabled) {
            settings.getPluginManager().registerPlugin(this);
            modifyEntityFormatter();
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<String> getFormatNames() {
        return Arrays.asList(DATA_ARRAY_FORMAT_NAME);
    }

    @Override
    public ResultFormatter getResultFormatter() {
        return new ResultFormatterDataArray(settings);
    }

    @Override
    public void modifyServiceDocument(ServiceRequest request, Map<String, Object> result) {
        Map<String, Object> serverSettings = (Map<String, Object>) result.get(Service.KEY_SERVER_SETTINGS);
        if (serverSettings == null) {
            // Nothing to add to.
            return;
        }
        Set<String> extensionList = (Set<String>) serverSettings.get(Service.KEY_CONFORMANCE_LIST);
        extensionList.add(REQUIREMENT_DATA_ARRAY);
    }

    @Override
    public Collection<String> getUrlPaths() {
        return Arrays.asList(ServiceDataArray.PATH_CREATE_OBSERVATIONS);
    }

    @Override
    public Collection<String> getRequestTypes() {
        return Arrays.asList(ServiceDataArray.REQUEST_TYPE_CREATE_OBSERVATIONS);
    }

    @Override
    public String getRequestTypeFor(String path, HttpMethod method) {
        if (HttpMethod.POST.equals(method) && ServiceDataArray.PATH_CREATE_OBSERVATIONS.equals(path)) {
            return ServiceDataArray.REQUEST_TYPE_CREATE_OBSERVATIONS;
        }
        throw new IllegalArgumentException("Method " + method + "not valid for path " + path);
    }

    @Override
    public ServiceResponse execute(Service service, ServiceRequest request, ServiceResponse response) {
        return new ServiceDataArray(settings).executeCreateObservations(service, request, response);
    }

    public static void modifyEntityFormatter() {
        if (modifiedEntityFormatter) {
            return;
        }
        modifiedEntityFormatter = true;

        // TODO: this should be an officially registerd method.
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataArrayValue.class, new DataArrayValueSerializer());
        module.addSerializer(DataArrayResult.class, new DataArrayResultSerializer());
        JsonWriter.getObjectMapper().registerModule(module);
    }

}
