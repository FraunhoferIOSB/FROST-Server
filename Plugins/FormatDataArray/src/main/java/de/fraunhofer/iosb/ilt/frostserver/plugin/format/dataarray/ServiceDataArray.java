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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray;

import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_DEFAULT;

import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.json.DataArrayDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
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
    private final PluginCoreModel pluginCoreModel;
    private final ArrayValueHandlers arrayValueHandlers;
    private final NavigationPropertyEntity npMultiDatastream;

    public ServiceDataArray(CoreSettings settings) {
        this.settings = settings;
        pluginCoreModel = settings.getPluginManager().getPlugin(PluginCoreModel.class);
        npMultiDatastream = (NavigationPropertyEntity) settings.getModelRegistry()
                .getEntityTypeForName("Observation")
                .getNavigationProperty("MultiDatastream");
        arrayValueHandlers = new ArrayValueHandlers();
    }

    public ServiceResponse executeCreateObservations(final Service service, final ServiceRequest request, final ServiceResponse response) {
        final Version version = request.getVersion();
        final PersistenceManager pm = service.getPm();
        try {
            final ModelRegistry modelRegistry = settings.getModelRegistry();
            final QueryDefaults queryDefaults = request.getQueryDefaults();
            Query query = QueryParser.parseQuery(request.getUrlQuery(), queryDefaults, modelRegistry, null, request.getUserPrincipal());
            JsonReaderDefault entityParser = new JsonReaderDefault(modelRegistry, request.getUserPrincipal());
            List<DataArrayValue> postData = DataArrayDeserializer.deserialize(request.getContentReader(), entityParser, settings);
            List<String> selfLinks = new ArrayList<>();
            for (DataArrayValue daValue : postData) {
                Entity datastream = daValue.getDatastream();
                Entity multiDatastream = daValue.getMultiDatastream();
                List<ArrayValueHandlers.ArrayValueHandler> handlers = new ArrayList<>();
                for (String component : daValue.getComponents()) {
                    handlers.add(arrayValueHandlers.getHandler(settings, component));
                }
                handleDataArrayItems(query, version, handlers, daValue, datastream, multiDatastream, pm, selfLinks);
            }
            service.maybeCommitAndClose();
            ResultFormatter formatter = settings.getFormatter(version, FORMAT_NAME_DEFAULT);
            response.setContentType(formatter.getContentType());
            response.setCode(201);
            formatter.format(null, query, selfLinks, settings.getQueryDefaults().useAbsoluteNavigationLinks())
                    .writeFormatted(response.getWriter());

            return response;
        } catch (IllegalArgumentException | IOException e) {
            pm.rollbackAndClose();
            return Service.errorResponse(response, 400, e.getMessage());
        } catch (RuntimeException e) {
            pm.rollbackAndClose();
            LOGGER.error("CreateObservations failed.", e);
            return Service.errorResponse(response, 500, e.getMessage());
        } catch (IncorrectRequestException ex) {
            LOGGER.error("Formatter not available.", ex);
            return Service.errorResponse(response, 500, "Failed to instantiate formatter");
        } finally {
            service.maybeRollbackAndClose();
        }
    }

    private void handleDataArrayItems(Query query, Version version, List<ArrayValueHandlers.ArrayValueHandler> handlers, DataArrayValue daValue, Entity datastream, Entity multiDatastream, PersistenceManager pm, List<String> selfLinks) {
        final String serviceRootUrl = settings.getQueryDefaults().getServiceRootUrl();
        int compCount = handlers.size();
        for (List<Object> entry : daValue.getDataArray()) {
            try {
                Entity observation = new DefaultEntity(pluginCoreModel.etObservation);
                if (datastream != null) {
                    observation.setProperty(pluginCoreModel.npDatastreamObservation, datastream);
                } else {
                    if (npMultiDatastream == null) {
                        throw new IllegalArgumentException("No Datastream found and MultiDatastream plugin not enabled.");
                    }
                    observation.setProperty(npMultiDatastream, multiDatastream);
                }
                for (int i = 0; i < compCount; i++) {
                    handlers.get(i).handle(entry.get(i), observation);
                }
                pm.insert(observation, UpdateMode.INSERT_STA_11);
                if (query.getMetadata() == Metadata.OFF) {
                    selfLinks.add("");
                } else {
                    String selfLink = UrlHelper.generateSelfLink(serviceRootUrl, version, observation);
                    selfLinks.add(selfLink);
                }
            } catch (NoSuchEntityException | IncompleteEntityException | IllegalArgumentException exc) {
                LOGGER.debug("Failed to create entity", exc);
                selfLinks.add("error " + exc.getMessage());
            }
        }
    }
}
