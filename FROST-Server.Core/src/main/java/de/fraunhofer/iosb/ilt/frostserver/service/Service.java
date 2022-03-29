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
package de.fraunhofer.iosb.ilt.frostserver.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.fge.jsonpatch.JsonPatch;
import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import static de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatDefault.DEFAULT_FORMAT_NAME;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.DELETE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.GET_CAPABILITIES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.READ;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_ALL;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGESET;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.CustomLinksHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
public class Service implements AutoCloseable {

    /**
     * The name of the server settings object in the index document.
     */
    public static final String KEY_SERVER_SETTINGS = "serverSettings";

    /**
     * The name of the list of implemented extensions in the server settings
     * object in the index document.
     */
    public static final String KEY_CONFORMANCE_LIST = "conformance";

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
    private static final String NOT_A_VALID_ID = "Not a valid id";
    private static final String POST_ONLY_ALLOWED_TO_COLLECTIONS = "POST only allowed to Collections.";
    private static final String COULD_NOT_PARSE_JSON = "Could not parse json.";
    private static final String FAILED_TO_UPDATE_ENTITY = "Failed to update entity.";
    private static final String NOTHING_FOUND_RESPONSE = "Nothing found.";

    private final CoreSettings settings;
    private PersistenceManager persistenceManager;
    private boolean transactionActive = false;

    public Service(CoreSettings settings) {
        this.settings = settings;
        PersistenceManagerFactory.init(settings);
    }

    public String getRequestType(HttpMethod method, String path) {
        PluginService plugin = settings.getPluginManager().getServiceForPath(path);
        String requestType = null;
        if (plugin == null) {
            switch (method) {
                case GET:
                    if (path.length() <= 6) {
                        // Only the version number in the path (/v1.0)
                        return GET_CAPABILITIES;
                    }
                    return READ;

                case PATCH:
                    return UPDATE_CHANGES;

                case POST:
                    return CREATE;

                case PUT:
                    return UPDATE_ALL;

                case DELETE:
                    return DELETE;

                default:
                    LOGGER.warn("Unknown method found: {}", method);
            }
        } else {
            requestType = plugin.getRequestTypeFor(path, method);
        }
        if (requestType == null) {
            final String cleanedPath = StringHelper.cleanForLogging(path);
            LOGGER.error("Unhandled request; Method {}, path {}", method, cleanedPath);
            throw new IllegalArgumentException("Unhandled request; Method " + method + ", path " + cleanedPath);
        }
        return requestType;
    }

    public <T> ServiceResponse<T> execute(ServiceRequest request) {
        String requestType = request.getRequestType();
        switch (requestType) {
            case GET_CAPABILITIES:
                return executeGetCapabilities(request);
            case CREATE:
                return executePost(request);
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
                PluginService plugin = settings.getPluginManager().getServiceForRequestType(requestType);
                if (plugin == null) {
                    return new ServiceResponse<>(500, "Illegal request type.");
                }
                return plugin.execute(this, request);
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
     * Notifies the backend that it is no longer needed.Call either commit, or
     * rollback before this.
     *
     */
    @Override
    public void close() {
        transactionActive = false;
        if (persistenceManager != null) {
            persistenceManager.close();
        }
    }

    public void maybeCommitAndClose() {
        if (!transactionActive) {
            getPm().commitAndClose();
            persistenceManager = null;
        }
    }

    public void maybeRollbackAndClose() {
        if (!transactionActive) {
            getPm().rollbackAndClose();
            persistenceManager = null;
        }
    }

    public PersistenceManager getPm() {
        if (persistenceManager == null) {
            persistenceManager = PersistenceManagerFactory.getInstance(settings).create();
        }
        return persistenceManager;
    }

    /**
     * @return the settings
     */
    public CoreSettings getSettings() {
        return settings;
    }

    private ServiceResponse executeGetCapabilities(ServiceRequest request) {
        ServiceResponse response = new ServiceResponse();
        Map<String, Object> result = new LinkedHashMap<>();
        Set<Extension> enabledSettings = settings.getEnabledExtensions();

        List<Map<String, String>> capList = new ArrayList<>();
        result.put("value", capList);
        try {
            for (EntityType entityType : EntityType.values()) {
                if (enabledSettings.contains(entityType.extension)) {
                    URL collectionUri = URI.create(
                            settings.getQueryDefaults().getServiceRootUrl()
                            + "/" + request.getVersion().urlPart
                            + "/" + entityType.plural).normalize().toURL();
                    capList.add(createCapability(entityType.plural, collectionUri));
                }
            }
        } catch (MalformedURLException ex) {
            LOGGER.error("Failed to build url.", ex);
            return errorResponse(response, 500, ex.getMessage());
        }

        if (request.getVersion() == Version.V_1_1) {
            Map<String, Object> serverSettings = new LinkedHashMap<>();
            result.put(KEY_SERVER_SETTINGS, serverSettings);

            Set<String> extensionList = new TreeSet<>();
            serverSettings.put(KEY_CONFORMANCE_LIST, extensionList);
            for (Extension setting : enabledSettings) {
                if (setting.isExposedFeature()) {
                    extensionList.addAll(setting.getRequirements());
                }
            }

            settings.getMqttSettings().fillServerSettings(serverSettings);
        }

        settings.getPluginManager().modifyServiceDocument(request, result);

        response.setCode(200);
        response.setResult(result);
        ResultFormatter formatter;
        try {
            formatter = settings.getFormatter(DEFAULT_FORMAT_NAME);
        } catch (IncorrectRequestException ex) {
            LOGGER.error("Formatter not available.", ex);
            return errorResponse(response, 500, "Failed to instantiate formatter");
        }
        response.setResultFormatted(formatter.format(null, null, result, settings.getQueryDefaults().useAbsoluteNavigationLinks()));
        response.setContentType(formatter.getContentType());
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
        } catch (RuntimeException e) {
            LOGGER.error("Failed to handle request (details in debug): {}", e.getMessage());
            LOGGER.debug("Exception:", e);
            if (pm != null) {
                pm.rollbackAndClose();
            }
            return errorResponse(response, 500, "Failed to execute query. See logs for details.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private <T> ServiceResponse<T> handleGet(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) {
        ResourcePath path;
        try {
            path = PathParser.parsePath(
                    pm.getIdManager(),
                    settings.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    request.getUrlPath());
        } catch (IllegalArgumentException e) {
            return errorResponse(response, 404, NOT_A_VALID_ID);
        } catch (IllegalStateException e) {
            return errorResponse(response, 404, NOT_A_VALID_ID + ": " + e.getMessage());
        }
        Query query;
        ResultFormatter formatter;
        try {
            query = QueryParser.parseQuery(request.getUrlQuery(), settings, path);
            query.validate();
            formatter = settings.getFormatter(query.getFormat());
            formatter.preProcessRequest(path, query);
        } catch (IllegalArgumentException | IncorrectRequestException ex) {
            return errorResponse(response, 400, ex.getMessage());
        }

        if (!pm.validatePath(path)) {
            maybeCommitAndClose();
            return errorResponse(response, 404, NOTHING_FOUND_RESPONSE);
        }
        T object;
        try {
            object = (T) pm.get(path, query);
        } catch (UnsupportedOperationException e) {
            LOGGER.error("Unsupported operation.", e);
            pm.rollbackAndClose();
            return errorResponse(response, 500, "Unsupported operation: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Illegal operation.", e);
            pm.rollbackAndClose();
            return errorResponse(response, 400, "Illegal operation: " + e.getMessage());
        } catch (ClassCastException e) {
            LOGGER.error("Result did not match expected format", e);
            pm.rollbackAndClose();
            return errorResponse(response, 500, "Illegal result type: " + e.getMessage());
        }
        maybeCommitAndClose();
        if (object == null) {
            if (path.isValue() || path.isEntityProperty()) {
                return successResponse(response, 204, "No Content");
            } else {
                return errorResponse(response, 404, NOTHING_FOUND_RESPONSE);
            }
        } else {
            response.setResult(object);
            response.setResultFormatted(formatter.format(path, query, object, settings.getQueryDefaults().useAbsoluteNavigationLinks()));
            response.setContentType(formatter.getContentType());
            response.setCode(200);
            return response;
        }
    }

    private <T> ServiceResponse<T> executePost(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        String urlPath = request.getUrlPath();
        if (urlPath == null || urlPath.equals("/")) {
            return errorResponse(response, 400, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }

        PersistenceManager pm = getPm();
        try {
            return handlePost(pm, urlPath, response, request);
        } catch (IOException | RuntimeException exc) {
            LOGGER.error("Failed to handle request (details in debug): {}", exc.getMessage());
            LOGGER.debug("Exception:", exc);
            if (pm != null) {
                pm.rollbackAndClose();
            }
            return errorResponse(response, 500, "Failed to store data.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private <T> ServiceResponse<T> handlePost(PersistenceManager pm, String urlPath, ServiceResponse<T> response, ServiceRequest request) throws IOException {
        ResourcePath path;
        try {
            path = PathParser.parsePath(
                    pm.getIdManager(),
                    settings.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    urlPath);
        } catch (IllegalArgumentException e) {
            return errorResponse(response, 404, NOT_A_VALID_ID);
        } catch (IllegalStateException e) {
            return errorResponse(response, 404, NOT_A_VALID_ID + ": " + e.getMessage());
        }
        if (!(path.getMainElement() instanceof PathElementEntitySet)) {
            return errorResponse(response, 400, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }
        if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
            return errorResponse(response, 400, "Not query options allowed on POST.");
        }

        if (!pm.validatePath(path)) {
            maybeCommitAndClose();
            return errorResponse(response, 404, NOTHING_FOUND_RESPONSE);
        }

        PathElementEntitySet mainSet = (PathElementEntitySet) path.getMainElement();
        EntityType type = mainSet.getEntityType();
        JsonReader entityParser = new JsonReader(pm.getIdManager().getIdClass());
        Entity entity;
        try {
            entity = entityParser.parseEntity(type.getImplementingClass(), request.getContent());
            entity.complete(mainSet);
            CustomLinksHelper.cleanPropertiesMap(pm.getCoreSettings(), entity);
        } catch (JsonParseException | JsonMappingException | IncompleteEntityException | IllegalStateException ex) {
            LOGGER.debug("Post failed: {}", ex.getMessage());
            LOGGER.trace("Exception:", ex);
            return errorResponse(response, 400, ex.getMessage());
        }

        try {
            if (!pm.insert(entity)) {
                LOGGER.debug("Failed to insert entity.");
                pm.rollbackAndClose();
                return errorResponse(response, 400, "Failed to insert entity.");
            }
            maybeCommitAndClose();
            String url = UrlHelper.generateSelfLink(path, entity);
            response.setResult((T) entity);
            response.setCode(201);
            response.addHeader("location", url);
            return response;
        } catch (IllegalArgumentException | IncompleteEntityException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 400, e.getMessage());
        }
    }

    private <T> ServiceResponse<T> executePatch(ServiceRequest request, boolean isChangeSet) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return errorResponse(response, 400, "PATCH only allowed on Entities.");
            }

            pm = getPm();
            if (isChangeSet) {
                return handleChangeSet(pm, request, response);
            }
            return handlePatch(pm, request, response);
        } catch (IncompleteEntityException | IOException | RuntimeException exc) {
            LOGGER.error("Failed to handle request (details in debug): {}", exc.getMessage());
            LOGGER.debug("Exception:", exc);
            if (pm != null) {
                pm.rollbackAndClose();
            }
            return errorResponse(response, 500, "Failed to store data.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private <T> ServiceResponse<T> handlePatch(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) throws IOException, IncompleteEntityException {
        PathElementEntity mainElement;
        Entity entity;
        try {
            mainElement = parsePathForPutPatch(pm, request);
            JsonReader entityParser = new JsonReader(pm.getIdManager().getIdClass());
            entity = entityParser.parseEntity(mainElement.getEntityType().getImplementingClass(), request.getContent());
            CustomLinksHelper.cleanPropertiesMap(pm.getCoreSettings(), entity);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid for patch.", exc);
            return errorResponse(response, 400, exc.getMessage());
        } catch (JsonParseException | JsonMappingException exc) {
            LOGGER.debug(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, 400, COULD_NOT_PARSE_JSON + " " + exc.getMessage());
        } catch (NoSuchEntityException exc) {
            return errorResponse(response, 404, exc.getMessage());
        }

        try {
            if (pm.update(mainElement, entity)) {
                maybeCommitAndClose();
                response.setCode(200);
                return response;
            } else {
                LOGGER.debug("Failed to patch entity.");
                pm.rollbackAndClose();
                return errorResponse(response, 400, "Failed to patch entity.");
            }
        } catch (IllegalArgumentException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 400, e.getMessage());
        }
    }

    private <T> ServiceResponse<T> handleChangeSet(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) throws IOException, IncompleteEntityException {
        PathElementEntity mainElement;
        JsonPatch jsonPatch;
        try {
            mainElement = parsePathForPutPatch(pm, request);
            jsonPatch = SimpleJsonMapper.getSimpleObjectMapper().readValue(request.getContent(), JsonPatch.class);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid.", exc);
            return errorResponse(response, 400, exc.getMessage());
        } catch (JsonParseException exc) {
            LOGGER.debug(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, 400, COULD_NOT_PARSE_JSON);
        } catch (NoSuchEntityException exc) {
            return errorResponse(response, 404, exc.getMessage());
        }

        try {
            if (pm.update(mainElement, jsonPatch)) {
                maybeCommitAndClose();
                return successResponse(response, 200, "JSON-Patch applied.");
            } else {
                LOGGER.debug(FAILED_TO_UPDATE_ENTITY);
                pm.rollbackAndClose();
                return errorResponse(response, 400, FAILED_TO_UPDATE_ENTITY);
            }
        } catch (IllegalArgumentException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 400, e.getMessage());
        }
    }

    private PathElementEntity parsePathForPutPatch(PersistenceManager pm, ServiceRequest request) throws NoSuchEntityException {
        ResourcePath path;
        try {
            path = PathParser.parsePath(
                    pm.getIdManager(),
                    settings.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    request.getUrlPath());
        } catch (IllegalArgumentException exc) {
            LOGGER.trace(NOT_A_VALID_ID, exc);
            throw new NoSuchEntityException(NOT_A_VALID_ID);
        } catch (IllegalStateException exc) {
            throw new NoSuchEntityException(NOT_A_VALID_ID + ": " + exc.getMessage());
        }

        if (!pm.validatePath(path)) {
            throw new NoSuchEntityException("No entity found for path.");
        }

        if (!(path.getMainElement() instanceof PathElementEntity) || path.getMainElement() != path.getLastElement()) {
            throw new IllegalArgumentException("PATCH & PUT only allowed on Entities.");
        }
        PathElementEntity mainElement = (PathElementEntity) path.getMainElement();
        if (mainElement.getId() == null) {
            throw new IllegalArgumentException("PATCH & PUT only allowed on Entities.");
        }
        if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
            throw new IllegalArgumentException("No query options allowed on PATCH & PUT.");
        }
        return mainElement;
    }

    private <T> ServiceResponse<T> executePut(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return errorResponse(response, 400, "PATCH only allowed on Entities.");
            }

            pm = getPm();
            return handlePut(pm, request, response);
        } catch (IncompleteEntityException | IOException | RuntimeException exc) {
            LOGGER.error("Failed to handle request (details in debug): {}", exc.getMessage());
            LOGGER.debug("Exception:", exc);
            if (pm != null) {
                pm.rollbackAndClose();
            }
            return errorResponse(response, 400, exc.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private <T> ServiceResponse<T> handlePut(PersistenceManager pm, ServiceRequest request, ServiceResponse<T> response) throws IOException, IncompleteEntityException {
        PathElementEntity mainElement;
        Entity entity;
        try {
            mainElement = parsePathForPutPatch(pm, request);

            JsonReader entityParser = new JsonReader(pm.getIdManager().getIdClass());
            entity = entityParser.parseEntity(mainElement.getEntityType().getImplementingClass(), request.getContent());
            entity.complete(true);
            CustomLinksHelper.cleanPropertiesMap(pm.getCoreSettings(), entity);
            entity.setEntityPropertiesSet(true, true);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid.", exc);
            return errorResponse(response, 400, exc.getMessage());
        } catch (JsonParseException | IncompleteEntityException exc) {
            LOGGER.error(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, 400, COULD_NOT_PARSE_JSON);
        } catch (NoSuchEntityException exc) {
            return errorResponse(response, 404, exc.getMessage());
        }

        try {
            if (pm.update(mainElement, entity)) {
                maybeCommitAndClose();
                return successResponse(response, 200, "Updated.");
            } else {
                LOGGER.debug(FAILED_TO_UPDATE_ENTITY);
                pm.rollbackAndClose();
                return errorResponse(response, 400, FAILED_TO_UPDATE_ENTITY);
            }
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 400, e.getMessage());
        }
    }

    private <T> ServiceResponse<T> executeDelete(ServiceRequest request) {
        if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
            return new ServiceResponse<T>().setStatus(400, "DELETE only allowed on Entities and Sets.");
        }

        ResourcePath path;
        try {
            path = PathParser.parsePath(
                    getPm().getIdManager(),
                    settings.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    request.getUrlPath());
        } catch (IllegalArgumentException e) {
            return new ServiceResponse<T>().setStatus(404, NOT_A_VALID_ID);
        } catch (IllegalStateException e) {
            return new ServiceResponse<T>().setStatus(404, NOT_A_VALID_ID + ": " + e.getMessage());
        }

        if ((path.getMainElement() instanceof PathElementEntity)) {
            return executeDeleteEntity(request, path);
        }
        if ((path.getMainElement() instanceof PathElementEntitySet)) {
            return executeDeleteEntitySet(request, path);
        }
        return new ServiceResponse<T>().setStatus(400, "Not a valid path for DELETE.");
    }

    private <T> ServiceResponse<T> executeDeleteEntity(ServiceRequest request, ResourcePath path) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            PathElementEntity mainEntity = (PathElementEntity) path.getMainElement();
            if (mainEntity != path.getLastElement()) {
                return errorResponse(response, 400, "DELETE not allowed on properties.");
            }
            if (mainEntity.getId() == null) {
                return errorResponse(response, 400, "No ID found.");
            }
            if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
                return errorResponse(response, 400, "No query options allowed on PATH when deleting an entity.");
            }

            pm = getPm();

            if (!pm.validatePath(path)) {
                maybeCommitAndClose();
                return errorResponse(response, 404, NOTHING_FOUND_RESPONSE);
            }

            return handleDelete(pm, mainEntity, response);
        } catch (Exception e) {
            LOGGER.error("", e);
            if (pm != null) {
                pm.rollbackAndClose();
            }
            return errorResponse(response, 400, e.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private <T> ServiceResponse<T> handleDelete(PersistenceManager pm, PathElementEntity mainEntity, ServiceResponse<T> response) {
        try {
            if (pm.delete(mainEntity)) {
                maybeCommitAndClose();
                response.setCode(200);
                return response;
            } else {
                LOGGER.debug("Failed to delete entity.");
                pm.rollbackAndClose();
                return errorResponse(response, 400, "Failed to delete entity.");
            }
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 404, e.getMessage());
        }
    }

    private <T> ServiceResponse<T> executeDeleteEntitySet(ServiceRequest request, ResourcePath path) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            PathElementEntitySet mainEntity = (PathElementEntitySet) path.getMainElement();
            if (mainEntity != path.getLastElement()) {
                return errorResponse(response, 400, "DELETE not allowed on properties.");
            }

            pm = getPm();

            if (!pm.validatePath(path)) {
                maybeCommitAndClose();
                return errorResponse(response, 404, NOTHING_FOUND_RESPONSE);
            }

            return handleDeleteSet(request, response, pm, path);
        } catch (Exception e) {
            LOGGER.error("", e);
            if (pm != null) {
                pm.rollbackAndClose();
            }
            return errorResponse(response, 400, e.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private <T> ServiceResponse<T> handleDeleteSet(ServiceRequest request, ServiceResponse<T> response, PersistenceManager pm, ResourcePath path) {
        Query query;
        try {
            query = QueryParser.parseQuery(request.getUrlQuery(), settings, path);
        } catch (IllegalArgumentException e) {
            return errorResponse(response, 404, "Failed to parse query: " + e.getMessage());
        }
        if (query.getCount().isPresent()) {
            return errorResponse(response, 400, "$count not allowed on delete requests.");
        }
        if (!query.getExpand().isEmpty()) {
            return errorResponse(response, 400, "$expand not allowed on delete requests.");
        }
        if (!query.getOrderBy().isEmpty()) {
            return errorResponse(response, 400, "$orderby not allowed on delete requests.");
        }
        if (query.getTop().isPresent()) {
            return errorResponse(response, 400, "$top not allowed on delete requests.");
        }
        if (query.getSkip().isPresent()) {
            return errorResponse(response, 400, "$skip not allowed on delete requests.");
        }

        try {
            pm.delete(path, query);
            maybeCommitAndClose();
            return successResponse(response, 200, "Deleted.");
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 404, e.getMessage());
        }
    }

    public static <T> ServiceResponse<T> successResponse(ServiceResponse<T> response, int code, String message) {
        return jsonResponse(response, "success", code, message);
    }

    public static <T> ServiceResponse<T> errorResponse(ServiceResponse<T> response, int code, String message) {
        return jsonResponse(response, "error", code, message);
    }

    public static <T> ServiceResponse<T> jsonResponse(ServiceResponse<T> response, String type, int code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", type);
        body.put("code", code);
        body.put("message", message);
        try {
            return response.setStatus(code, JsonWriter.writeObject(body));
        } catch (IOException ex) {
            LOGGER.error("Failed to serialise error response.", ex);
        }
        return response.setStatus(code, message);
    }

}
