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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray;

import static de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatDefault.DEFAULT_FORMAT_NAME;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.DataArrayValue.LIST_OF_DATAARRAYVALUE;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.ArrayValueHandlers;
import de.fraunhofer.iosb.ilt.frostserver.util.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the service requests for the DataArray plugin. This is the request to
 * /CreateObservations.
 *
 * @author scf
 */
public class ServiceDataArray {

    /**
     * The path for the CreateObservations request type..
     */
    public static final String PATH_CREATE_OBSERVATIONS = "/CreateObservations";

    /**
     * The RequestType definition for the CreateObservations request type.
     */
    public static final String REQUEST_TYPE_CREATE_OBSERVATIONS = "createObservations";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDataArray.class);

    private final CoreSettings settings;

    public ServiceDataArray(CoreSettings settings) {
        this.settings = settings;
    }

    public <T> ServiceResponse<T> executeCreateObservations(final Service service, final ServiceRequest request) {
        final ServiceResponse<T> response = new ServiceResponse<>();
        final String serviceRootUrl = settings.getServiceRootUrl(request.getVersion());
        final PersistenceManager pm = service.getPm();
        try {
            EntityParser entityParser = new EntityParser(pm.getIdManager().getIdClass());
            List<DataArrayValue> postData = entityParser.parseObject(LIST_OF_DATAARRAYVALUE, request.getContent());
            List<String> selfLinks = new ArrayList<>();
            for (DataArrayValue daValue : postData) {
                Datastream datastream = daValue.getDatastream();
                MultiDatastream multiDatastream = daValue.getMultiDatastream();
                List<ArrayValueHandlers.ArrayValueHandler> handlers = new ArrayList<>();
                for (String component : daValue.getComponents()) {
                    handlers.add(ArrayValueHandlers.getHandler(component));
                }
                handleDataArrayItems(serviceRootUrl, handlers, daValue, datastream, multiDatastream, pm, selfLinks);
            }
            service.maybeCommitAndClose();
            ResultFormatter formatter = settings.getFormatter(DEFAULT_FORMAT_NAME);
            response.setResultFormatted(formatter.format(null, null, selfLinks, settings.isUseAbsoluteNavigationLinks()));
            response.setContentType(formatter.getContentType());
            return Service.successResponse(response, 201, "Created");
        } catch (IllegalArgumentException | IOException e) {
            pm.rollbackAndClose();
            return Service.errorResponse(response, 400, e.getMessage());
        } catch (RuntimeException e) {
            pm.rollbackAndClose();
            return Service.errorResponse(response, 500, e.getMessage());
        } catch (IncorrectRequestException ex) {
            LOGGER.error("Formatter not available.", ex);
            return Service.errorResponse(response, 500, "Failed to instantiate formatter");
        } finally {
            service.maybeRollbackAndClose();
        }
    }

    private void handleDataArrayItems(String serviceRootUrl, List<ArrayValueHandlers.ArrayValueHandler> handlers, DataArrayValue daValue, Datastream datastream, MultiDatastream multiDatastream, PersistenceManager pm, List<String> selfLinks) {
        int compCount = handlers.size();
        for (List<Object> entry : daValue.getDataArray()) {
            try {
                Observation observation = new Observation();
                observation.setDatastream(datastream);
                observation.setMultiDatastream(multiDatastream);
                for (int i = 0; i < compCount; i++) {
                    handlers.get(i).handle(entry.get(i), observation);
                }

                pm.insert(observation);
                String selfLink = UrlHelper.generateSelfLink(serviceRootUrl, observation);
                selfLinks.add(selfLink);
            } catch (NoSuchEntityException | IncompleteEntityException | IllegalArgumentException exc) {
                LOGGER.debug("Failed to create entity", exc);
                selfLinks.add("error " + exc.getMessage());
            }
        }
    }
}
