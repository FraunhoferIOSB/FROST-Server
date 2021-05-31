/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart.Content;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart.ContentIdPair;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart.HeaderUtils;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart.HttpContent;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart.MixedContent;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart.Part;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class BatchProcessorHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessorHelper.class);

    private BatchProcessorHelper() {
        // Utility class, not to be instantiated.
    }

    public static HttpContent processHttpRequest(Service service, HttpContent httpRequest, boolean inChangeSet) {
        String type = service.getRequestType(httpRequest.getMethod(), httpRequest.getPath());
        Version version = Version.forString(httpRequest.getVersion());
        ServiceRequest serviceRequest = new ServiceRequestBuilder(version).withRequestType(type)
                .withUrl(httpRequest.getPath() == null ? null : StringHelper.urlDecode(httpRequest.getPath()))
                .withContent(httpRequest.getData()).build();
        ServiceResponse<Object> serviceResponse = service.execute(serviceRequest);

        if (RequestTypeUtils.CREATE.equals(type)) {
            Object createdObject = serviceResponse.getResult();
            if (createdObject instanceof Entity) {
                Entity entity = (Entity) createdObject;
                String path = entity.getId().toString();
                httpRequest.setContentIdValue(path);
            }
        }
        HttpContent httpResponse = new HttpContent(serviceRequest.getVersion(), inChangeSet);
        if (inChangeSet) {
            httpResponse.setContentId(httpRequest.getContentId());
        }

        int statusCode = serviceResponse.getCode();
        httpResponse.setStatusLine(HeaderUtils.generateStatusLine(statusCode, "no text"));

        Map<String, String> headers = httpResponse.getHttpHeaders();
        serviceResponse.getHeaders().entrySet().forEach(x -> headers.put(x.getKey(), x.getValue()));

        String resultFormatted = serviceResponse.getResultFormatted();
        if (statusCode >= 200 && statusCode < 300) {
            if (!StringHelper.isNullOrEmpty(resultFormatted)) {
                headers.put("Content-Type", "application/json; charset=UTF-8");
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

    public static Content processChangeset(ServiceRequest batchRequest, Service service, MixedContent changeset) {
        if (changeset.isParseFailed()) {
            HttpContent content = new HttpContent(batchRequest.getVersion());
            for (String error : changeset.getErrors()) {
                content.addData(error);
                content.addData("\n");
            }
            content.setStatusLine(HeaderUtils.generateStatusLine(400, "Bad Request"));
            return content;
        }
        service.startTransaction();
        MixedContent mixedResponse = new MixedContent(batchRequest.getVersion(), service.getSettings(), true);
        List<Part> parts = changeset.getParts();
        List<ContentIdPair> contentIds = new ArrayList<>(parts.size());
        for (Part part : parts) {
            LOGGER.debug("SubPart: {}", part);
            Content content = part.getContent();
            if (content instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) content;
                httpContent.updateUsingContentIds(contentIds);

                HttpContent httpResponse = processHttpRequest(service, httpContent, true);
                if (httpResponse.isExecuteFailed()) {
                    service.rollbackTransaction();
                    return httpResponse;
                } else {
                    mixedResponse.addPart(new Part(batchRequest.getVersion(), service.getSettings(), true).setContent(httpResponse));
                }

                String contentId = httpContent.getContentId();
                String contentIdValue = httpContent.getContentIdValue();
                if (!StringHelper.isNullOrEmpty(contentId) && !StringHelper.isNullOrEmpty(contentIdValue)) {
                    contentIds.add(new ContentIdPair("$" + contentId, contentIdValue));
                }
            } else {
                LOGGER.warn("Only http requests allowed in changset. Found type: {}", content.getClass().getName());
            }
        }
        service.commitTransaction();
        return mixedResponse;
    }

    public static MixedContent processMultipartMixed(ServiceRequest batchRequest, Service service, MixedContent multipartMixedData) {
        Version batchVersion =batchRequest.getVersion();
        MixedContent mixedResponse = new MixedContent(batchVersion, service.getSettings(), false);
        for (Part part : multipartMixedData.getParts()) {
            LOGGER.debug("Part: {}", part);
            Content content = part.getContent();
            if (content instanceof MixedContent) {
                MixedContent changset = (MixedContent) content;
                Content changesetResponse = processChangeset(batchRequest, service, changset);
                mixedResponse.addPart(new Part(batchVersion, service.getSettings(), false).setContent(changesetResponse));
            } else if (content instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) content;
                HttpContent httpResponse = processHttpRequest(service, httpContent, false);
                mixedResponse.addPart(new Part(batchVersion, service.getSettings(), false).setContent(httpResponse));
            } else {
                LOGGER.warn("Invalid multipart-part type: {}", content.getClass().getName());
            }
        }
        return mixedResponse;
    }

}
