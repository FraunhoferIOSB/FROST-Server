/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.Batch;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch.BatchFactory;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.json.JsonBatchFactory;
import de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.multipart.MultipartFactory;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the service requests for the DataArray plugin. This is the request to
 * /$batch.
 *
 * @author scf
 */
public class ServiceBatchProcessing {

    /**
     * The path for the CreateObservations request type..
     */
    public static final String PATH_POST_BATCH = "/$batch";

    /**
     * The RequestType definition for the CreateObservations request type.
     */
    public static final String REQUEST_TYPE_BATCH = "batchProcess";

    private static final Map<String, BatchFactory<?>> CONTENT_TYPE_TO_FACTORY = new HashMap<>();
    static {
        for (BatchFactory<?> factory : Arrays.asList(new MultipartFactory(), new JsonBatchFactory())) {
            CONTENT_TYPE_TO_FACTORY.put(factory.getContentType(), factory);
        }
    }

    private final CoreSettings settings;

    public ServiceBatchProcessing(CoreSettings settings) {
        this.settings = settings;
    }

    public ServiceResponse<String> executeBatchOperation(final Service service, final ServiceRequest request) {
        BatchFactory<?> batchFactory = CONTENT_TYPE_TO_FACTORY
                .get(request.getContentType().split(";", 2)[0].toLowerCase());
        if (batchFactory == null) {
            throw new IllegalArgumentException("Invalid Content-Type: " + request.getContentType());
        }
        Batch<?> batch = batchFactory.createBatch(request.getVersion(), settings, false);
        batch.parse(request);
        Batch<?> resultContent = new BatchProcessor(batchFactory).processBatch(request, service,
                batch);
        return sendResponse(resultContent);
    }

    private ServiceResponse<String> sendResponse(Batch batch) {
        final ServiceResponse<String> response = new ServiceResponse<>();
        response.setCode(200);
        batch.getHeaders().entrySet().forEach(x -> response.addHeader(x.getKey(), x.getValue()));
        response.setResultFormatted(batch.getContent(false));
        return response;
    }
}
