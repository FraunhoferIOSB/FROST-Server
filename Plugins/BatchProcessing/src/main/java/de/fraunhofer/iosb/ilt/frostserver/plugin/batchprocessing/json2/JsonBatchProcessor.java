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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json2;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CHARSET_UTF8;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.ContentIdPair;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Streaming processor for JSON batch requests. JSON batch requests are much
 * simpler than multipart requests, since they are not nested.
 */
public class JsonBatchProcessor implements Iterator<JsonBatchResultItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonBatchProcessor.class.getName());

    private final Service service;
    final ServiceRequest request;
    final ServiceResponse response;

    private JsonParser parser;
    private JsonBatchResultItem next;
    private String currentGroup = "";
    private Map<String, ContentIdPair> ids = new HashMap<>();

    public JsonBatchProcessor(Service service, ServiceRequest request, ServiceResponse response) {
        this.service = service;
        this.request = request;
        this.response = response;
    }

    public JsonBatchResponse processRequest() {
        try {
            parser = request.getJsonReader()
                    .getMapper()
                    .createParser(request.getContentReader());
            JsonToken currentToken = parser.nextToken();
            while (currentToken != null) {
                if (currentToken == JsonToken.FIELD_NAME) {
                    String fieldName = parser.currentName();
                    if ("requests".equalsIgnoreCase(fieldName)) {
                        currentToken = parser.nextToken();
                        if (currentToken != JsonToken.START_ARRAY) {
                            throw new IllegalArgumentException("requests must be an array, found " + currentToken);
                        }
                        // We are now positioned in the JSON stream to read the first request.
                        JsonBatchResponse result = new JsonBatchResponse().setResponses(this);
                        response.setResult(result);
                        return result;
                    }
                }
                currentToken = parser.nextToken();
            }
        } catch (IOException | RuntimeException ex) {
            close();
            throw new IllegalArgumentException("Failed to read input");
        }
        throw new IllegalArgumentException("No requests found in input");
    }

    private void close() {
        if (parser != null) {
            try {
                parser.close();
            } catch (IOException ex) {
                LOGGER.warn("Failed to close parser!", ex);
            }
        }
    }

    public void processNextRequest() {
        try {
            if (parser.nextToken() == JsonToken.START_OBJECT) {
                JsonBatchRequestItem item = parser.readValueAs(JsonBatchRequestItem.class);
                if (!StringUtils.equals(currentGroup, item.getAtomicityGroup())) {
                    service.commitTransaction();
                    service.startTransaction(request.getUserPrincipal());
                }

                next = processRequestItem(service, item);

            }
        } catch (IOException ex) {
            LOGGER.debug("Failed to parse json.", ex);
        }
    }

    public JsonBatchResultItem processRequestItem(Service service, JsonBatchRequestItem requestItem) {
        final CoreSettings coreSettings = service.getSettings();
        final Version version = request.getVersion();

        String path = requestItem.getUrl();
        final PluginService plugin = coreSettings.getPluginManager().getServiceForPath(version, path);
        if (plugin == null) {
            return new JsonBatchResultItem()
                    .setId(requestItem.getId())
                    .setStatus(400)
                    .setBody("Unknown URL: " + path);
        }
        final String method = requestItem.getMethod();
        String requestType = PluginManager.decodeRequestType(plugin, version, path, method, requestItem.getContentType());

        final ServiceRequest serviceRequest = new ServiceRequest()
                .setCoreSettings(coreSettings)
                .setVersion(version)
                .setRequestType(requestType)
                .setUpdateMode(RequestTypeUtils.CREATE.equals(requestType) ? UpdateMode.INSERT_STA_11 : UpdateMode.UPDATE_STA_11)
                .setUrl(path)
                .setContent(requestItem.getBody())
                .setUserPrincipal(request.getUserPrincipal());

        final ServiceResponseDefault serviceResponse = new ServiceResponseDefault();
        plugin.execute(service, serviceRequest, serviceResponse);
        JsonBatchResultItem result = new JsonBatchResultItem();
        if (RequestTypeUtils.CREATE.equals(requestType)) {
            Object createdObject = serviceResponse.getResult();
            if (createdObject instanceof Entity entity) {
                result.setIdValue(entity.getPrimaryKeyValues());
                result.setEntityType(entity.getEntityType());
            }
        }
        final int statusCode = serviceResponse.getCode();
        result.setId(requestItem.getId())
                .setStatus(statusCode)
                .addHeaders(serviceResponse.getHeaders());

        String resultFormatted = serviceResponse.getWriter().toString();
        if (statusCode >= 200 && statusCode < 300) {
            if (!StringHelper.isNullOrEmpty(resultFormatted)) {
                result.setHeader(Constants.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON + "; " + CHARSET_UTF8);
                result.setBody(resultFormatted);
            }
        } else {
            if (!StringHelper.isNullOrEmpty(serviceResponse.getMessage())) {
                result.setBody(serviceResponse.getMessage());
            }
        }

        return result;
    }

    @Override
    public boolean hasNext() {
        if (next == null) {
            processNextRequest();
        }
        return next != null;
    }

    @Override
    public JsonBatchResultItem next() {
        if (!hasNext()) {
            return null;
        }
        JsonBatchResultItem myNext = next;
        next = null;
        return myNext;
    }

}
