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
package de.fraunhofer.iosb.ilt.sta.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.fge.jsonpatch.JsonPatch;
import de.fraunhofer.iosb.ilt.sta.formatter.DataArrayValue;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.builder.ObservationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.sta.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.util.ArrayValueHandlers;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.sta.util.UrlHelper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes SensorThings commands. Normally, each call of
 * {@link #execute(ServiceRequest)} runs in its own transaction (for back-ends
 * that support transactions). If a transaction is explicitly started with
 * {@link #startTransaction()}, then all subsequent calls to
 * {@link #execute(ServiceRequest)} will run in this transaction, until either
 * {@link #commitTransaction()} is called, or {@link #rollbackTransaction()} is
 * called, or a call to {@link #execute(ServiceRequest)} fails with an
 * exception.
 *
 * If a call to {@link #execute(ServiceRequest)} fails, the response will have a
 * status code that is not in the 200-299 range. A failed call will always
 * result in a transaction rollback, even when a transaction is explicitly
 * started.
 *
 * This class is not thread-safe.
 *
 * @author jab, scf
 */
public class Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
    private static final String NOT_A_VALID_ID = "Not a valid id";
    private static final String POST_ONLY_ALLOWED_TO_COLLECTIONS = "POST only allowed to Collections.";
    private static final String COULD_NOT_PARSE_JSON = "Could not parse json.";

    private final CoreSettings settings;
    private PersistenceManager persistenceManager;
    private boolean transactionActive = false;

    public Service(CoreSettings settings) {
        this.settings = settings;
        PersistenceManagerFactory.init(settings);
    }

    public <T> ServiceResponse<T> execute(ServiceRequest request) {
        switch (request.getRequestType()) {
            case GET_CAPABILITIES:
                return executeGetCapabilities(request);
            case CREATE:
                return executePost(request);
            case CREATE_OBSERVATIONS:
                return executeCreateObservations(request);
            case READ:
                return executeGet(request);
            case DELETE:
                return executeDelete(request);
            case UPDATE_ALL:
                return executePut(request);
            case UPDATE_CHANGES:
                return executePatch(request, false);
            case UPDATE_CHANGESET:
                return executePatch(request, true);
            default:
                return new ServiceResponse<>(500, "Illegal request type.");
        }
    }

    /**
     * Explicitly starts a transaction. All subsequent calls to
     * {@link #execute(ServiceRequest)} will run in this transaction, until
     * either {@link #commitTransaction()} is called, or
     * {@link #rollbackTransaction()} is called, or a call to
     * {@link #execute(ServiceRequest)} fails with an exception.
     *
     * After starting a transaction, it should be {@link #close()}d explicitly
     * too.
     *
     * @return this
     */
    public Service startTransaction() {
        transactionActive = true;
        return this;
    }

    /**
     * Commits and ends an explicitly started transaction. If there is no active
     * explicitly started transaction, this will do nothing,
     *
     * @return this
     */
    public Service commitTransaction() {
        transactionActive = false;
        getPm().commit();
        return this;
    }

    /**
     * Rolls-back and ends an explicitly started transaction. If there is no
     * active explicitly started transaction, this will do nothing,
     *
     * @return this
     */
    public Service rollbackTransaction() {
        transactionActive = false;
        getPm().rollback();
        return this;
    }

    /**
     * Notifies the backend that it is no longer needed. Call either commit, or
     * rollback before this.
     *
     * @return this
     */
    public Service close() {
        transactionActive = false;
        getPm().close();
        return this;
    }

    private void maybeCommitAndClose() {
        if (!transactionActive) {
            getPm().commitAndClose();
        }
    }

    private void maybeRollbackAndClose() {
        if (!transactionActive) {
            getPm().rollbackAndClose();
        }
    }

    private PersistenceManager getPm() {
        if (persistenceManager == null) {
            persistenceManager = PersistenceManagerFactory.getInstance().create();
        }
        return persistenceManager;
    }

    private ServiceResponse executeGetCapabilities(ServiceRequest request) {
        ServiceResponse response = new ServiceResponse();
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        List< Map<String, String>> capList = new ArrayList<>();
        result.put("value", capList);
        try {
            for (EntityType entityType : EntityType.values()) {
                capList.add(createCapability(entityType.plural, URI.create(settings.getServiceRootUrl() + "/" + entityType.plural).normalize().toURL()));
            }
            response.setCode(200);
            response.setResult(result);
            response.setResultFormatted(request.getFormatter().format(null, null, result, settings.isUseAbsoluteNavigationLinks()));
        } catch (MalformedURLException ex) {
            LOGGER.error("Failed to build url.", ex);
            return response.setStatus(500, ex.getMessage());
        }
        return response;
    }

    private Map<String, String> createCapability(String name, URL url) {
        Map<String, String> val = new HashMap<>();
        val.put("name", name);
        val.put("url", url.toString());
        return Collections.unmodifiableMap(val);
    }

    private <T> ServiceResponse<T> executeGet(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = getPm();
        try {
            return handleGet(pm, request, response);
        } catch (Exception e) {
            response.setStatus(500, "Failed to execute query. See logs for details.");
            LOGGER.error("", e);
            if (pm != null) {
                pm.rollbackAndClose();
            }
        } finally {
            maybeRollbackAndClose();
        }
        return response;
    }

    private <T> ServiceResponse<T> handleGet(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) {
        ResourcePath path;
        try {
            path = PathParser.parsePath(pm.getIdManager(), settings.getServiceRootUrl(), request.getUrlPath());
        } catch (IllegalArgumentException e) {
            response.setStatus(404, NOT_A_VALID_ID);
            return response;
        } catch (IllegalStateException e) {
            response.setStatus(404, NOT_A_VALID_ID + ": " + e.getMessage());
            return response;
        }
        Query query;
        try {
            query = QueryParser.parseQuery(request.getUrlQuery(), settings);
            query.validate(path);
        } catch (IllegalArgumentException ex) {
            response.setStatus(400, ex.getMessage());
            return response;
        }

        fixDataArrayRequests(query, path);

        if (!pm.validatePath(path)) {
            response.setStatus(404, "Nothing found.");
            maybeCommitAndClose();
            return response;
        }
        T object;
        try {
            object = (T) pm.get(path, query);
        } catch (UnsupportedOperationException e) {
            LOGGER.error("Unsupported operation.", e);
            response.setStatus(500, "Unsupported operation: " + e.getMessage());
            pm.rollbackAndClose();
            return response;
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Illegal operation.", e);
            response.setStatus(400, "Illegal operation: " + e.getMessage());
            pm.rollbackAndClose();
            return response;
        } catch (ClassCastException e) {
            LOGGER.error("Result did not match expected format", e);
            response.setStatus(500, "Illegal result type: " + e.getMessage());
            pm.rollbackAndClose();
            return response;
        }
        if (object == null) {
            if (path.isValue() || path.isEntityProperty()) {
                response.setStatus(204, "No Content");
            } else {
                response.setStatus(404, "Nothing found.");
            }
        } else {
            response.setResult(object);
            response.setResultFormatted(request.getFormatter().format(path, query, object, settings.isUseAbsoluteNavigationLinks()));
            response.setCode(200);
        }
        maybeCommitAndClose();
        return response;
    }

    private void fixDataArrayRequests(Query query, ResourcePath path) {
        // If DataArray is requested, and $select is used, make sure Datastream is in the $select.
        if ("dataarray".equalsIgnoreCase(query.getFormat()) && !query.getSelect().isEmpty()) {
            ResourcePathElement lastElement = path.getLastElement();
            if (lastElement instanceof EntitySetPathElement && ((EntitySetPathElement) lastElement).getEntityType() == EntityType.OBSERVATION) {
                query.getSelect().add(NavigationProperty.DATASTREAM);
            }
        }
    }

    private <T> ServiceResponse<T> executePost(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        String urlPath = request.getUrlPath();
        if (urlPath == null || urlPath.equals("/")) {
            return response.setStatus(400, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }

        PersistenceManager pm = getPm();
        try {
            return handlePost(pm, urlPath, response, request);
        } catch (Exception e) {
            LOGGER.error("", e);
            if (pm != null) {
                pm.rollbackAndClose();
            }
            return response.setStatus(500, "Failed to store data.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private <T> ServiceResponse<T> handlePost(PersistenceManager pm, String urlPath, ServiceResponse<T> response, ServiceRequest request) throws IOException {
        ResourcePath path;
        try {
            path = PathParser.parsePath(pm.getIdManager(), settings.getServiceRootUrl(), urlPath);
        } catch (IllegalArgumentException e) {
            return response.setStatus(404, NOT_A_VALID_ID);
        } catch (IllegalStateException e) {
            return response.setStatus(404, NOT_A_VALID_ID + ": " + e.getMessage());
        }
        if (!(path.getMainElement() instanceof EntitySetPathElement)) {
            return response.setStatus(400, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }
        if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
            return response.setStatus(400, "Not query options allowed on POST.");
        }

        EntitySetPathElement mainSet = (EntitySetPathElement) path.getMainElement();
        EntityType type = mainSet.getEntityType();
        EntityParser entityParser = new EntityParser(pm.getIdManager().getIdClass());
        Entity entity;
        try {
            entity = entityParser.parseEntity(type.getImplementingClass(), request.getContent());
            entity.complete(mainSet);
        } catch (JsonParseException | JsonMappingException | IncompleteEntityException | IllegalStateException ex) {
            LOGGER.debug("Post failed: {}", ex.getMessage());
            LOGGER.trace("Exception:", ex);
            return response.setStatus(400, ex.getMessage());
        }

        try {
            if (!pm.insert(entity)) {
                LOGGER.debug("Failed to insert entity.");
                pm.rollbackAndClose();
                return response.setStatus(400, "Failed to insert entity.");
            }
            maybeCommitAndClose();
            String url = UrlHelper.generateSelfLink(path, entity);
            response.setResult((T) entity);
            response.setCode(201);
            response.addHeader("location", url);
        } catch (IllegalArgumentException | IncompleteEntityException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            return response.setStatus(400, e.getMessage());
        }
        return response;
    }

    private <T> ServiceResponse<T> executeCreateObservations(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        String urlPath = request.getUrlPath();
        if (!("/CreateObservations".equals(urlPath))) {
            return response.setStatus(400, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }

        PersistenceManager pm = getPm();
        try {
            EntityParser entityParser = new EntityParser(pm.getIdManager().getIdClass());
            List<DataArrayValue> postData = entityParser.parseObservationDataArray(request.getContent());
            List<String> selfLinks = new ArrayList<>();
            for (DataArrayValue daValue : postData) {
                Datastream datastream = daValue.getDatastream();
                MultiDatastream multiDatastream = daValue.getMultiDatastream();
                List<ArrayValueHandlers.ArrayValueHandler> handlers = new ArrayList<>();
                for (String component : daValue.getComponents()) {
                    handlers.add(ArrayValueHandlers.getHandler(component));
                }
                handleDataArrayItems(handlers, daValue, datastream, multiDatastream, pm, selfLinks);
            }
            maybeCommitAndClose();
            response.setResultFormatted(request.getFormatter().format(null, null, selfLinks, settings.isUseAbsoluteNavigationLinks()));
            return response.setStatus(201, "Created");
        } catch (IllegalArgumentException | IOException e) {
            pm.rollbackAndClose();
            return response.setStatus(400, e.getMessage());
        }
    }

    private void handleDataArrayItems(List<ArrayValueHandlers.ArrayValueHandler> handlers, DataArrayValue daValue, Datastream datastream, MultiDatastream multiDatastream, PersistenceManager pm, List<String> selfLinks) {
        int compCount = handlers.size();
        for (List<Object> entry : daValue.getDataArray()) {
            try {
                ObservationBuilder obsBuilder = new ObservationBuilder();
                obsBuilder.setDatastream(datastream);
                obsBuilder.setMultiDatastream(multiDatastream);
                for (int i = 0; i < compCount; i++) {
                    handlers.get(i).handle(entry.get(i), obsBuilder);
                }
                Observation observation = obsBuilder.build();
                pm.insert(observation);
                String selfLink = UrlHelper.generateSelfLink(settings.getServiceRootUrl(), observation);
                selfLinks.add(selfLink);
            } catch (NoSuchEntityException | IncompleteEntityException | IllegalArgumentException exc) {
                LOGGER.debug("Failed to create entity", exc);
                selfLinks.add("error " + exc.getMessage());
            }
        }
    }

    private <T> ServiceResponse<T> executePatch(ServiceRequest request, boolean isChangeSet) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return response.setStatus(400, "PATCH only allowed on Entities.");
            }

            pm = getPm();
            if (isChangeSet) {
                return handleChangeSet(pm, request, response);
            }
            return handlePatch(pm, request, response);
        } catch (IncompleteEntityException | IOException | RuntimeException exc) {
            LOGGER.error("", exc);
            if (pm != null) {
                pm.rollbackAndClose();
            }
        } finally {
            maybeRollbackAndClose();
        }
        return response;
    }

    private <T> ServiceResponse<T> handlePatch(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) throws IOException, IncompleteEntityException {
        EntityPathElement mainElement;
        Entity entity;
        try {
            mainElement = parsePathForPutPatch(pm, request, response);
            EntityParser entityParser = new EntityParser(pm.getIdManager().getIdClass());
            entity = entityParser.parseEntity(mainElement.getEntityType().getImplementingClass(), request.getContent());
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid for patch.", exc);
            return response;
        } catch (JsonParseException exc) {
            LOGGER.debug(COULD_NOT_PARSE_JSON, exc);
            return response.setStatus(400, COULD_NOT_PARSE_JSON);
        }

        try {
            if (pm.update(mainElement, entity)) {
                maybeCommitAndClose();
                response.setCode(200);
            } else {
                LOGGER.debug("Failed to patch entity.");
                pm.rollbackAndClose();
            }
        } catch (IllegalArgumentException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            response.setStatus(400, e.getMessage());
        }
        return response;
    }

    private <T> ServiceResponse<T> handleChangeSet(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) throws IOException, IncompleteEntityException {
        EntityPathElement mainElement;
        JsonPatch jsonPatch;
        try {
            mainElement = parsePathForPutPatch(pm, request, response);
            jsonPatch = EntityParser.getSimpleObjectMapper().readValue(request.getContent(), JsonPatch.class);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid.", exc);
            return response;
        } catch (JsonParseException exc) {
            LOGGER.debug(COULD_NOT_PARSE_JSON, exc);
            return response.setStatus(400, COULD_NOT_PARSE_JSON);
        }

        try {
            if (pm.update(mainElement, jsonPatch)) {
                maybeCommitAndClose();
                response.setCode(200);
            } else {
                LOGGER.debug("Failed to update entity.");
                pm.rollbackAndClose();
            }
        } catch (IllegalArgumentException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            response.setStatus(400, e.getMessage());
        }
        return response;
    }

    private <T> EntityPathElement parsePathForPutPatch(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) {
        ResourcePath path;
        try {
            path = PathParser.parsePath(pm.getIdManager(), settings.getServiceRootUrl(), request.getUrlPath());
        } catch (IllegalArgumentException exc) {
            LOGGER.trace(NOT_A_VALID_ID, exc);
            response.setStatus(404, NOT_A_VALID_ID);
            throw new IllegalArgumentException(NOT_A_VALID_ID);
        } catch (IllegalStateException exc) {
            response.setStatus(404, NOT_A_VALID_ID + ": " + exc.getMessage());
            throw new IllegalArgumentException(NOT_A_VALID_ID);
        }
        if (!(path.getMainElement() instanceof EntityPathElement) || path.getMainElement() != path.getLastElement()) {
            response.setStatus(400, "PATCH & PUT only allowed on Entities.");
            throw new IllegalArgumentException(NOT_A_VALID_ID);
        }
        EntityPathElement mainElement = (EntityPathElement) path.getMainElement();
        if (mainElement.getId() == null) {
            response.setStatus(400, "PATCH & PUT only allowed on Entities.");
            throw new IllegalArgumentException(NOT_A_VALID_ID);
        }
        if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
            response.setStatus(400, "Not query options allowed on PATCH & PUT.");
            throw new IllegalArgumentException(NOT_A_VALID_ID);
        }
        return mainElement;
    }

    private <T> ServiceResponse<T> executePut(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return response.setStatus(400, "PATCH only allowed on Entities.");
            }

            pm = getPm();
            return handlePut(pm, request, response);
        } catch (Exception e) {
            LOGGER.error("", e);
            if (pm != null) {
                pm.rollbackAndClose();
            }
        } finally {
            maybeRollbackAndClose();
        }
        return response;
    }

    private <T> ServiceResponse<T> handlePut(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) throws IOException, IncompleteEntityException {
        EntityPathElement mainElement;
        Entity entity;
        try {
            mainElement = parsePathForPutPatch(pm, request, response);

            EntityParser entityParser = new EntityParser(pm.getIdManager().getIdClass());
            entity = entityParser.parseEntity(mainElement.getEntityType().getImplementingClass(), request.getContent());
            entity.complete(true);
            entity.setEntityPropertiesSet(true, true);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid.", exc);
            return response;
        } catch (JsonParseException | IncompleteEntityException e) {
            LOGGER.error(COULD_NOT_PARSE_JSON, e);
            return response.setStatus(400, COULD_NOT_PARSE_JSON);
        }

        try {
            if (pm.update(mainElement, entity)) {
                maybeCommitAndClose();
                response.setCode(200);
            } else {
                LOGGER.debug("Failed to update entity.");
                pm.rollbackAndClose();
            }
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            response.setStatus(400, e.getMessage());
        }
        return response;
    }

    private <T> ServiceResponse<T> executeDelete(ServiceRequest request) {
        if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
            return new ServiceResponse<>().setStatus(400, "DELETE only allowed on Entities and Sets.");
        }

        ResourcePath path;
        try {
            path = PathParser.parsePath(getPm().getIdManager(), settings.getServiceRootUrl(), request.getUrlPath());
        } catch (IllegalArgumentException e) {
            return new ServiceResponse<>().setStatus(404, NOT_A_VALID_ID);
        } catch (IllegalStateException e) {
            return new ServiceResponse<>().setStatus(404, NOT_A_VALID_ID + ": " + e.getMessage());
        }

        if ((path.getMainElement() instanceof EntityPathElement)) {
            return executeDeleteEntity(request, path);
        }
        if ((path.getMainElement() instanceof EntitySetPathElement)) {
            return executeDeleteEntitySet(request, path);
        }
        return new ServiceResponse<>().setStatus(400, "Not a valid path for DELETE.");
    }

    private <T> ServiceResponse<T> executeDeleteEntity(ServiceRequest request, ResourcePath path) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            EntityPathElement mainEntity = (EntityPathElement) path.getMainElement();
            if (mainEntity != path.getLastElement()) {
                return response.setStatus(400, "DELETE not allowed on properties.");
            }
            if (mainEntity.getId() == null) {
                return response.setStatus(400, "No ID found.");
            }
            if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
                return response.setStatus(400, "No query options allowed on PATH when deleting an entity.");
            }

            pm = getPm();
            return handleDelete(pm, mainEntity, response);
        } catch (Exception e) {
            LOGGER.error("", e);
            if (pm != null) {
                pm.rollbackAndClose();
            }
        } finally {
            maybeRollbackAndClose();
        }
        return response;
    }

    private <T> ServiceResponse<T> handleDelete(PersistenceManager pm, EntityPathElement mainEntity, ServiceResponse<T> response) {
        try {
            if (pm.delete(mainEntity)) {
                maybeCommitAndClose();
                response.setCode(200);
            } else {
                LOGGER.debug("Failed to delete entity.");
                pm.rollbackAndClose();
            }
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            response.setStatus(404, e.getMessage());
        }
        return response;
    }

    private <T> ServiceResponse<T> executeDeleteEntitySet(ServiceRequest request, ResourcePath path) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            EntitySetPathElement mainEntity = (EntitySetPathElement) path.getMainElement();
            if (mainEntity != path.getLastElement()) {
                return response.setStatus(400, "DELETE not allowed on properties.");
            }

            pm = getPm();

            return handleDeleteSet(request, response, pm, path);
        } catch (Exception e) {
            LOGGER.error("", e);
            if (pm != null) {
                pm.rollbackAndClose();
            }
        } finally {
            maybeRollbackAndClose();
        }
        return response;
    }

    private <T> ServiceResponse<T> handleDeleteSet(ServiceRequest request, ServiceResponse<T> response, PersistenceManager pm, ResourcePath path) {
        Query query;
        try {
            query = QueryParser.parseQuery(request.getUrlQuery(), settings);
        } catch (IllegalArgumentException e) {
            return response.setStatus(404, "Failed to parse query: " + e.getMessage());
        }
        if (query.getCount().isPresent()) {
            return response.setStatus(400, "$count not allowed on delete requests.");
        }
        if (!query.getExpand().isEmpty()) {
            return response.setStatus(400, "$expand not allowed on delete requests.");
        }
        if (!query.getOrderBy().isEmpty()) {
            return response.setStatus(400, "$orderby not allowed on delete requests.");
        }
        if (query.getTop().isPresent()) {
            return response.setStatus(400, "$top not allowed on delete requests.");
        }
        if (query.getSkip().isPresent()) {
            return response.setStatus(400, "$skip not allowed on delete requests.");
        }

        try {
            pm.delete(path, query);
            maybeCommitAndClose();
            response.setCode(200);
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            response.setStatus(404, e.getMessage());
        }
        return response;
    }
}
