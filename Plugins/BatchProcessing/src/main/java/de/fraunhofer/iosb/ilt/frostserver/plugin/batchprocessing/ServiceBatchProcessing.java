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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_MULTIPART_MIXED;

import de.fraunhofer.iosb.ilt.frostserver.formatter.FormatWriterGeneric;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatterDefault;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Batch;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.BatchFactory;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json.JsonBatchProcessor;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json.JsonBatchResponse;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart.MultipartFactory;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the service requests for the DataArray plugin. This is the request to
 * /$batch.
 */
public class ServiceBatchProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBatchProcessing.class.getName());
    /**
     * The path for the CreateObservations request type..
     */
    public static final String PATH_POST_BATCH = "/$batch";

    /**
     * The RequestType definition for the CreateObservations request type.
     */
    public static final String REQUEST_TYPE_BATCH = "batchProcess";

    private final CoreSettings settings;

    public ServiceBatchProcessing(final CoreSettings settings) {
        this.settings = settings;
    }

    public ServiceResponse executeBatchOperation(final Service service, final ServiceRequest request, final ServiceResponse response) {
        String ct = request.getContentType().split(";", 2)[0].toLowerCase();
        switch (ct) {
            case CONTENT_TYPE_MULTIPART_MIXED:
                BatchFactory<?> batchFactory = new MultipartFactory();
                Batch<?> batch = batchFactory.createBatch(request.getVersion(), settings, false);
                batch.parse(request);
                Batch<?> resultContent = new BatchProcessor(batchFactory).processBatch(request, service, batch);
                return sendResponse(resultContent, response);

            case CONTENT_TYPE_APPLICATION_JSON:
                JsonBatchResponse batchResponse = new JsonBatchProcessor(service, request, response)
                        .processRequest();
                try {
                    new ResultFormatterDefault()
                            .format(null, null, batchResponse, false)
                            .writeFormatted(response.getWriter());
                } catch (IOException ex) {
                    LOGGER.error("Failed to format", ex);
                    throw new IllegalArgumentException("Failed to execute batch.");
                }
                return response;

            default:
                throw new IllegalArgumentException("Invalid Content-Type: " + request.getContentType());
        }
    }

    private ServiceResponse sendResponse(final Batch batch, final ServiceResponse response) {
        response.setCode(200);
        batch.getHeaders().entrySet().forEach(x -> response.addHeaders(x.getKey(), x.getValue()));
        try {
            new FormatWriterGeneric(batch.getContent(false)).writeFormatted(response.getWriter());
        } catch (IOException ex) {
            LOGGER.error("Failed to format", ex);
        }
        return response;
    }
}
