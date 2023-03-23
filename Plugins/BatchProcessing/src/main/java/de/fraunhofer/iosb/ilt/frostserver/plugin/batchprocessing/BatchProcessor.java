/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CHARSET_UTF8;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Batch;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.BatchFactory;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Content;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.ContentIdPair;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Part;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Request;
import de.fraunhofer.iosb.ilt.frostserver.query.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 * @param <C> the type of content
 */
public class BatchProcessor<C extends Content> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessor.class);

    private final BatchFactory<C> batchFactory;

    public BatchProcessor(BatchFactory<C> batchFactory) {
        this.batchFactory = batchFactory;
    }

    public Request processHttpRequest(Service service, Request httpRequest, boolean inChangeSet) {
        final CoreSettings coreSettings = service.getSettings();
        final PluginManager pluginManager = coreSettings.getPluginManager();
        final Version version = pluginManager.getVersion(httpRequest.getVersion());
        final String type = service.getRequestType(
                httpRequest.getMethod(),
                version,
                httpRequest.getPath(),
                httpRequest.getHttpHeaders().get(CONTENT_TYPE));
        final ServiceRequest serviceRequest = new ServiceRequestBuilder(coreSettings, version)
                .withRequestType(type)
                .withUrl(httpRequest.getPath() == null ? null : StringHelper.urlDecode(httpRequest.getPath()))
                .withContent(httpRequest.getData())
                .withUserPrincipal(PrincipalExtended.fromPrincipal(httpRequest.getUserPrincipal()))
                .build();
        PluginService plugin = coreSettings.getPluginManager().getServiceForRequestType(serviceRequest.getVersion(), serviceRequest.getRequestType());
        final ServiceResponseDefault serviceResponse = new ServiceResponseDefault();
        if (plugin == null) {
            serviceResponse.setCode(500)
                    .setMessage("No plugin to handle requests of type " + serviceRequest.getRequestType() + " for version " + serviceRequest.getVersion());
        } else {
            plugin.execute(service, serviceRequest, serviceResponse);
        }

        if (RequestTypeUtils.CREATE.equals(type)) {
            Object createdObject = serviceResponse.getResult();
            if (createdObject instanceof Entity) {
                Entity entity = (Entity) createdObject;
                httpRequest.setContentIdValue(entity.getId());
            }
        }
        Request httpResponse = batchFactory.createRequest(serviceRequest.getVersion(), inChangeSet);
        httpResponse.setContentId(httpRequest.getContentId());
        int statusCode = serviceResponse.getCode();
        httpResponse.setStatus(statusCode, "no text");

        Map<String, String> headers = httpResponse.getHttpHeaders();
        serviceResponse.getHeaders().entrySet().forEach(x -> headers.put(x.getKey(), x.getValue()));

        String resultFormatted = serviceResponse.getWriter().toString();
        if (statusCode >= 200 && statusCode < 300) {
            if (!StringHelper.isNullOrEmpty(resultFormatted)) {
                headers.put("Content-Type", CONTENT_TYPE_APPLICATION_JSON + "; " + CHARSET_UTF8);
                httpResponse.addData(resultFormatted);
            }
        } else {
            httpResponse.setExecuteFailed(true);
            if (!StringHelper.isNullOrEmpty(serviceResponse.getMessage())) {
                httpResponse.addData(serviceResponse.getMessage());
            }
        }

        return httpResponse;
    }

    public Content processChangeset(ServiceRequest batchRequest, Service service, Batch changeset) {
        if (changeset.isParseFailed()) {
            Request content = batchFactory.createRequest(batchRequest.getVersion(), false);
            for (String error : (List<String>) changeset.getErrors()) {
                content.addData(error);
                content.addData("\n");
            }
            content.setStatus(400, "Bad Request");
            return content;
        }
        service.startTransaction(batchRequest.getUserPrincipal());
        Batch response = batchFactory.createBatch(batchRequest.getVersion(), service.getSettings(), true);
        List<Part> parts = changeset.getParts();
        List<ContentIdPair> contentIds = new ArrayList<>(parts.size());
        for (Part part : parts) {
            LOGGER.debug("SubPart: {}", part);
            Content content = part.getContent();
            if (content instanceof Request) {
                Request request = (Request) content;
                request.updateUsingContentIds(contentIds);
                request.setUserPrincipal(changeset.getUserPrincipal());
                Request httpResponse = processHttpRequest(service, request, true);
                if (httpResponse.isExecuteFailed()) {
                    service.rollbackTransaction();
                    return httpResponse;
                } else {
                    Part newPart = batchFactory.createPart(batchRequest.getVersion(), service.getSettings(), true, "");
                    newPart.setContent(httpResponse);
                    response.addPart(newPart);
                }

                String contentId = request.getContentId();
                Id contentIdValue = request.getContentIdValue();
                if (!StringHelper.isNullOrEmpty(contentId) && contentIdValue != null) {
                    contentIds.add(new ContentIdPair("$" + contentId, contentIdValue));
                }
            } else {
                LOGGER.warn("Only http requests allowed in changset. Found type: {}", content.getClass().getName());
            }
        }
        service.commitTransaction();
        return response;
    }

    public Batch<C> processBatch(ServiceRequest batchRequest, Service service, Batch<C> batch) {
        Version batchVersion = batchRequest.getVersion();
        Batch<C> batchResponse = batchFactory.createBatch(batchVersion, service.getSettings(), false);
        for (Part<C> part : batch.getParts()) {
            LOGGER.debug("Part: {}", part);
            Content content = part.getContent();
            if (content instanceof Batch) {
                Batch<C> changset = (Batch<C>) content;
                changset.setUserPrincipal(batchRequest.getUserPrincipal());
                Content changesetResponse = processChangeset(batchRequest, service, changset);
                Part newPart = batchFactory.createPart(batchVersion, service.getSettings(), false, "");
                newPart.setContent(changesetResponse);
                batchResponse.addPart(newPart);
            } else if (content instanceof Request) {
                Request request = (Request) content;
                request.setUserPrincipal(batchRequest.getUserPrincipal());
                Request httpResponse = processHttpRequest(service, request, false);
                Part newPart = batchFactory.createPart(batchVersion, service.getSettings(), false, "");
                newPart.setContent(httpResponse);
                batchResponse.addPart(newPart);
            } else {
                LOGGER.warn("Invalid part type: {}", content.getClass().getName());
            }
        }
        return batchResponse;
    }

}
