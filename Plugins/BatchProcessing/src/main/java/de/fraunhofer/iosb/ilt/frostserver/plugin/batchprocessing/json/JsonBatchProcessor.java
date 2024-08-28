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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
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
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Streaming processor for JSON batch requests. JSON batch requests are much
 * simpler than multipart requests, since they are not nested.
 */
public class JsonBatchProcessor implements Iterator<JsonBatchResultItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonBatchProcessor.class.getName());
    private static final String REFERENCE_URL_REGEX = "^" + Pattern.quote("$") + "([a-zA-Z0-9_.:,;-]+)";
    private static final Pattern REFERENCE_URL_PATTERN = Pattern.compile(REFERENCE_URL_REGEX);
    private static final String REFERENCE_JSON_REGEX = Pattern.quote("\"$") + "([a-zA-Z0-9_.:,;-]+)" + Pattern.quote("\"");
    private static final Pattern REFERENCE_JSON_PATTERN = Pattern.compile(REFERENCE_JSON_REGEX);

    private final Service service;
    private final ServiceRequest request;
    private final ServiceResponse response;

    private JsonParser parser;
    private JsonBatchResultItem next;

    private String currentGroup = "";
    private boolean groupFailed = false;
    private final Map<String, ContentIdPair> ids = new HashMap<>();

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
        if (parser == null) {
            return;
        }
        try {
            if (parser.nextToken() == JsonToken.START_OBJECT) {
                JsonBatchRequestItem item = parser.readValueAs(JsonBatchRequestItem.class);
                final String itemGroup = item.getAtomicityGroup();
                if (!StringUtils.equals(currentGroup, itemGroup)) {
                    service.commitTransaction();
                    service.startTransaction(request.getUserPrincipal());
                    currentGroup = itemGroup;
                    ids.clear();
                    groupFailed = false;
                }
                if (groupFailed) {
                    next = new JsonBatchResultItem()
                            .setStatus(400)
                            .setBodyNotFormatted("Skipped due to previous failure in atomicityGroup.");
                } else {
                    next = processRequestItem(service, item);
                }
                if (next.isFailed()) {
                    groupFailed = true;
                    service.rollbackTransaction();
                }
            } else if (parser.currentToken() == JsonToken.END_ARRAY) {
                // We're done.
                if (!groupFailed) {
                    service.commitTransaction();
                }
            }
        } catch (IOException ex) {
            LOGGER.debug("Failed to parse json.", ex);
            parser = null;
            next = new JsonBatchResultItem()
                    .setStatus(400)
                    .setBodyNotFormatted("Failed to parse json: " + ex.getMessage());
        }
    }

    private String replaceIdsUrl(String url) {
        Matcher matcher = REFERENCE_URL_PATTERN.matcher(url);
        if (matcher.matches()) {
            String name = matcher.group(1);
            ContentIdPair pair = ids.get(name);
            if (pair == null) {
                LOGGER.debug("Not a match: {}", matcher.group(0));
                return '/' + url;
            } else {
                String value = pair.selfLink();
                return value + url.substring(matcher.end(0));
            }
        }
        return '/' + url;
    }

    private String replaceIdsJson(String body) {
        if (StringHelper.isNullOrEmpty(body)) {
            return body;
        }
        Matcher matcher = REFERENCE_JSON_PATTERN.matcher(body);
        StringBuilder result = new StringBuilder();
        int idx = 0;
        while (matcher.find()) {
            result.append(body.substring(idx, matcher.start(0)));
            String name = matcher.group(1);
            ContentIdPair pair = ids.get(name);
            if (pair == null) {
                LOGGER.debug("Not a match: {}", matcher.group(0));
                result.append(matcher.group(0));
            } else {
                String value = UrlHelper.quoteForJson(pair.value.get(0));
                result.append(value);
            }
            idx = matcher.end(0);
        }
        result.append(body.substring(idx));
        return result.toString();
    }

    public JsonBatchResultItem processRequestItem(Service service, JsonBatchRequestItem requestItem) {
        final CoreSettings coreSettings = service.getSettings();
        final Version version = request.getVersion();
        final String requestId = requestItem.getId();
        String path = requestItem.getUrl();
        path = replaceIdsUrl(path);
        if (!requestItem.matchesIfCondition(ids)) {
            return new JsonBatchResultItem()
                    .setId(requestId)
                    .setStatus(200)
                    .setBodyNotFormatted("Skipped due to if.");
        }

        final PluginService plugin = coreSettings.getPluginManager().getServiceForPath(version, path);
        if (plugin == null) {
            return new JsonBatchResultItem()
                    .setId(requestId)
                    .setStatus(400)
                    .setBodyNotFormatted("Unknown URL: " + path);
        }
        final String method = requestItem.getMethod();
        String requestType = PluginManager.decodeRequestType(plugin, version, path, method, requestItem.getContentType());
        final String requestBody = replaceIdsJson(requestItem.getBody());

        final ServiceRequest serviceRequest = new ServiceRequest()
                .setCoreSettings(coreSettings)
                .setVersion(version)
                .setRequestType(requestType)
                .setUpdateMode(RequestTypeUtils.CREATE.equals(requestType) ? UpdateMode.INSERT_STA_11 : UpdateMode.UPDATE_STA_11)
                .setUrl(path)
                .setContent(requestBody)
                .setUserPrincipal(request.getUserPrincipal());

        final ServiceResponseDefault serviceResponse = new ServiceResponseDefault();
        plugin.execute(service, serviceRequest, serviceResponse);

        final int statusCode = serviceResponse.getCode();
        JsonBatchResultItem result = new JsonBatchResultItem()
                .setId(requestId)
                .setStatus(statusCode)
                .addHeaders(serviceResponse.getHeaders());

        final String contentType = serviceResponse.getContentType();
        final String responseContent = serviceResponse.getWriter().toString();
        if (contentType == null || CONTENT_TYPE_APPLICATION_JSON.equalsIgnoreCase(contentType.split(";", 2)[0])) {
            result.setBodyJsonFormatted(responseContent);
        } else {
            result.setHeader(CONTENT_TYPE, contentType);
            result.setBodyNotFormatted(responseContent);
        }

        if (RequestTypeUtils.READ.equals(requestType) && (!requestId.startsWith("-"))) {
            Object item = serviceResponse.getResult();
            if (item instanceof Entity entity) {
                storeEntityId(entity, result, requestId);
            } else if (item instanceof EntitySet set) {
                storeEntityId(set.first(), result, requestId);
            }
        }
        if (RequestTypeUtils.CREATE.equals(requestType)) {
            Object createdObject = serviceResponse.getResult();
            if (createdObject instanceof Entity entity) {
                storeEntityId(entity, result, requestId);
            }
        }

        return result;
    }

    public void storeEntityId(Entity entity, JsonBatchResultItem result, final String requestId) {
        if (entity == null) {
            return;
        }
        final PkValue pkValues = entity.getPrimaryKeyValues();
        final EntityType entityType = entity.getEntityType();
        result.setIdValue(pkValues);
        result.setEntityType(entityType);
        if (!requestId.startsWith("-")) {
            ids.put(requestId, new ContentIdPair(requestId, pkValues, entityType));
        }
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
