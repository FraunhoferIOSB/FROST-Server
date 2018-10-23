/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.http.common.multipart;

import com.google.common.base.Strings;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.service.RequestType;
import de.fraunhofer.iosb.ilt.sta.service.Service;
import de.fraunhofer.iosb.ilt.sta.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.sta.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.sta.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.sta.util.UrlHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class BatchProcessor {

    private BatchProcessor() {
        // Utility class, not to be instantiated.
    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessor.class);

    public static HttpContent processHttpRequest(Service service, HttpContent httpRequest, boolean inChangeSet) {
        RequestType type = httpRequest.getRequestType();
        ServiceRequest serviceRequest = new ServiceRequestBuilder()
                .withRequestType(type)
                .withUrl(httpRequest.getUrl())
                .withContent(httpRequest.getData())
                .build();
        ServiceResponse<Object> serviceResponse = service.execute(serviceRequest);

        if (type == RequestType.CREATE) {
            Object createdObject = serviceResponse.getResult();
            if (createdObject instanceof Entity) {
                Entity entity = (Entity) createdObject;
                String path = UrlHelper.generateSelfLink("", entity);
                httpRequest.setContentIdValue(path);
            }
        }
        HttpContent httpResponse = new HttpContent(inChangeSet);
        if (inChangeSet) {
            httpResponse.setContentId(httpRequest.getContentId());
        }

        int statusCode = serviceResponse.getCode();
        httpResponse.setStatusLine(Headers.generateStatusLine(statusCode));

        Map<String, String> headers = httpResponse.getHttpHeaders();
        serviceResponse.getHeaders().entrySet().forEach(x -> headers.put(x.getKey(), x.getValue()));

        String resultFormatted = serviceResponse.getResultFormatted();
        if (statusCode >= 200 && statusCode < 300) {
            if (!Strings.isNullOrEmpty(resultFormatted)) {
                headers.put("Content-Type", "application/json; charset=UTF-8");
                httpResponse.addData(resultFormatted);
            }
        } else {
            httpResponse.setExecuteFailed(true);
            if (!Strings.isNullOrEmpty(serviceResponse.getMessage())) {
                httpResponse.addData(serviceResponse.getMessage());
            }
        }

        return httpResponse;
    }

    public static Content processChangeset(Service service, MixedContent changeset) {
        if (changeset.isParseFailed()) {
            HttpContent content = new HttpContent();
            for (String error : changeset.getErrors()) {
                content.addData(error);
                content.addData("\n");
            }
            content.setStatusLine(Headers.generateStatusLine(HttpStatus.SC_BAD_REQUEST));
            return content;
        }
        service.startTransaction();
        MixedContent mixedResponse = new MixedContent(true);
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
                    return httpResponse;
                } else {
                    mixedResponse.addPart(new Part(true).setContent(httpResponse));
                }

                String contentId = httpContent.getContentId();
                String contentIdValue = httpContent.getContentIdValue();
                if (!Strings.isNullOrEmpty(contentId) && !Strings.isNullOrEmpty(contentIdValue)) {
                    contentIds.add(new ContentIdPair("$" + contentId, contentIdValue));
                }
            } else {
                LOGGER.warn("Only http requests allowed in changset. Found type: {}", content.getClass().getName());
            }
        }
        return mixedResponse;
    }

    public static MixedContent processMultipartMixed(Service service, MixedContent multipartMixedData) {
        MixedContent mixedResponse = new MixedContent(false);
        for (Part part : multipartMixedData.getParts()) {
            LOGGER.debug("Part: {}", part);
            Content content = part.getContent();
            if (content instanceof MixedContent) {
                MixedContent changset = (MixedContent) content;
                Content changesetResponse = processChangeset(service, changset);
                mixedResponse.addPart(new Part(false).setContent(changesetResponse));
            } else if (content instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) content;
                HttpContent httpResponse = processHttpRequest(service, httpContent, false);
                mixedResponse.addPart(new Part(false).setContent(httpResponse));
            } else {
                LOGGER.warn("Invalid multipart-part type: {}", content.getClass().getName());
            }
        }
        return mixedResponse;
    }

}
