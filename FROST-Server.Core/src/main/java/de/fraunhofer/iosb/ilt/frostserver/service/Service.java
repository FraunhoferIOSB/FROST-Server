/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_DEFAULT;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.DELETE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.GET_CAPABILITIES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.READ;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_ALL;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGESET;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.REQUEST_PARAM_FORMAT;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.ForbiddenException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UnauthorizedException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.Principal;
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
 * {@link #execute(ServiceRequest, ServiceResponse) } runs in its own
 * transaction (for back-ends that support transactions). If a transaction is
 * explicitly started with {@link #startTransaction()}, then all subsequent
 * calls to {@link #execute(ServiceRequest, ServiceResponse)} will run in this
 * transaction, until either {@link #commitTransaction()} is called, or
 * {@link #rollbackTransaction()} is called, or a call to
 * {@link #execute(ServiceRequest, ServiceResponse)} fails with an exception.
 *
 * If a call to {@link #execute(ServiceRequest, ServiceResponse)} fails, the
 * response will have a status code that is not in the 200-299 range. A failed
 * call will always result in a transaction rollback, even when a transaction is
 * explicitly started.
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
    private static final String EXCEPTION = "Exception:";
    private static final String NOT_A_VALID_PATH = "Not a valid path";
    private static final String POST_ONLY_ALLOWED_TO_COLLECTIONS = "POST only allowed to Collections.";
    private static final String COULD_NOT_PARSE_JSON = "Could not parse json.";
    private static final String FAILED_TO_HANDLE_REQUEST_DETAILS_IN_DEBUG = "Failed to handle request (details in debug): {}";
    private static final String FAILED_TO_UPDATE_ENTITY = "Failed to update entity.";
    private static final String NOTHING_FOUND_RESPONSE = "Nothing found.";

    private final CoreSettings settings;
    private final ModelRegistry modelRegistry;
    private PersistenceManager persistenceManager;
    private boolean transactionActive = false;

    public Service(CoreSettings settings) {
        this.settings = settings;
        modelRegistry = settings.getModelRegistry();
        PersistenceManagerFactory.init(settings);
    }

    public String getRequestType(HttpMethod method, Version version, String path, String contentType) {
        PluginService plugin = settings.getPluginManager().getServiceForPath(version, path);
        String requestType = null;
        if (plugin != null) {
            requestType = plugin.getRequestTypeFor(version, path, method, contentType);
        }
        if (requestType == null) {
            final String cleanedPath = StringHelper.cleanForLogging(path);
            LOGGER.error("Unhandled request; Method {}, path {}", method, cleanedPath);
            throw new IllegalArgumentException("Unhandled request; Method " + method + ", path " + cleanedPath);
        }
        return requestType;
    }

    /**
     * Execute the given request, and put the result in the given response
     * object.
     *
     * @param request the request to execute.
     * @param response the response object to put the result in. If null, a new
     * {@link ServiceResponseDefault} is created.
     * @return the service response passed, or a new one.
     */
    public ServiceResponse execute(ServiceRequest request, ServiceResponse response) {
        if (!transactionActive) {
            getPm().setRole(request.getUserPrincipal());
        }
        if (response == null) {
            response = new ServiceResponseDefault();
        }
        String requestType = request.getRequestType();
        switch (requestType) {
            case GET_CAPABILITIES:
                return executeGetCapabilities(request, response);
            case CREATE:
                return executePost(request, response);
            case READ:
                return executeGet(request, response);
            case DELETE:
                return executeDelete(request, response);
            case UPDATE_ALL:
                return executePut(request, response);
            case UPDATE_CHANGES:
                return executePatch(request, response, false);
            case UPDATE_CHANGESET:
                return executePatch(request, response, true);
            default:
                PluginService plugin = settings.getPluginManager().getServiceForRequestType(request.getVersion(), requestType);
                if (plugin == null) {
                    return errorResponse(response, 500, "Illegal request type.");
                }
                return plugin.execute(this, request, response);
        }
    }

    /**
     * Explicitly starts a transaction.
     *
     * All subsequent calls to {@link #execute(ServiceRequest, ServiceResponse)}
     * will run in this transaction, until either {@link #commitTransaction()}
     * is called, or {@link #rollbackTransaction()} is called, or a call to
     * {@link #execute(ServiceRequest, ServiceResponse)} fails with an
     * exception.
     *
     * After starting a transaction, it should be {@link #close()}d explicitly
     * too.
     *
     * @param user The user to use for the transaction.
     * @return this
     */
    public Service startTransaction(Principal user) {
        getPm().setRole(user);
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

    public void rollbackAndClose(PersistenceManager pm) {
        if (pm != null) {
            pm.rollbackAndClose();
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

    private ServiceResponse executeGetCapabilities(ServiceRequest request, ServiceResponse response) {
        final Map<String, Object> result = new LinkedHashMap<>();
        final Set<Extension> enabledSettings = settings.getEnabledExtensions();
        final Version version = request.getVersion();

        final List<Map<String, String>> capList = new ArrayList<>();
        result.put("value", capList);
        try {
            for (EntityType entityType : modelRegistry.getEntityTypes()) {
                URL collectionUri = URI.create(settings.getQueryDefaults().getServiceRootUrl()
                        + "/" + version.urlPart
                        + "/" + entityType.plural).normalize().toURL();
                capList.add(createCapability(entityType.plural, collectionUri));
            }
        } catch (MalformedURLException ex) {
            LOGGER.error("Failed to build url.", ex);
            return errorResponse(response, 500, ex.getMessage());
        }

        if (version == Version.V_1_1) {
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
        return formatResponse(request, response, result);
    }

    private ServiceResponse formatResponse(ServiceRequest request, ServiceResponse response, Object result) {
        ResultFormatter formatter;
        try {
            formatter = settings.getFormatter(request.getVersion(), FORMAT_NAME_DEFAULT);
        } catch (IncorrectRequestException ex) {
            LOGGER.error("Formatter not available.", ex);
            return errorResponse(response, 500, "Failed to instantiate formatter");
        }
        return formatResponse(response, formatter, null, null, result);
    }

    private ServiceResponse formatResponse(ServiceResponse response, ResultFormatter formatter, Query query, ResourcePath path, Object result) {
        response.setContentType(formatter.getContentType());
        try {
            formatter.format(path, query, result, settings.getQueryDefaults().useAbsoluteNavigationLinks())
                    .writeFormatted(response.getWriter());
        } catch (IOException ex) {
            LOGGER.error("Formatter not available.", ex);
            return errorResponse(response, 500, "Failed to format");
        }
        return response;
    }

    private Map<String, String> createCapability(String name, URL url) {
        Map<String, String> val = new HashMap<>();
        val.put("name", name);
        val.put("url", url.toString());
        return Collections.unmodifiableMap(val);
    }

    private ServiceResponse executeGet(ServiceRequest request, ServiceResponse response) {
        PersistenceManager pm = getPm();
        try {
            return handleGet(pm, request, response);
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 401, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 403, e.getMessage());
        } catch (Exception e) {
            LOGGER.error(FAILED_TO_HANDLE_REQUEST_DETAILS_IN_DEBUG, e.getMessage());
            LOGGER.debug(EXCEPTION, e);
            rollbackAndClose(pm);
            return errorResponse(response, 500, "Failed to execute query. See logs for details.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handleGet(PersistenceManager pm, ServiceRequest request, ServiceResponse response) {
        final ResourcePath path;
        final Version version = request.getVersion();
        try {
            path = PathParser.parsePath(modelRegistry,
                    settings.getQueryDefaults().getServiceRootUrl(), version,
                    request.getUrlPath());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorResponse(response, 404, NOT_A_VALID_PATH + ": " + e.getMessage());
        }
        Query query;
        ResultFormatter formatter;
        try {
            query = QueryParser
                    .parseQuery(request.getUrlQuery(), settings, path)
                    .validate();
            settings.getPluginManager().parsedQuery(settings, request, query);
            formatter = settings.getFormatter(version, query.getFormat());
            formatter.preProcessRequest(path, query);
        } catch (IllegalArgumentException | IncorrectRequestException ex) {
            return errorResponse(response, 400, ex.getMessage());
        }

        if (!pm.validatePath(path)) {
            maybeCommitAndClose();
            return errorResponse(response, version.getCannedResponse(Version.CannedResponseType.NOTHING_FOUND));
        }
        try {
            Object object = pm.get(path, query);
            if (object == null) {
                if (path.isValue() || path.isEntityProperty()) {
                    return successResponse(response, 204, "No Content");
                } else {
                    return errorResponse(response, version.getCannedResponse(Version.CannedResponseType.NOTHING_FOUND));
                }
            } else {
                response.setResult(object);
                response.setCode(200);
                return formatResponse(response, formatter, query, path, object);
            }
        } catch (UnsupportedOperationException e) {
            LOGGER.error("Unsupported operation.", e);
            pm.rollbackAndClose();
            return errorResponse(response, 500, "Unsupported operation: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.trace("Illegal operation.", e);
            pm.rollbackAndClose();
            return errorResponse(response, 400, "Illegal operation: " + e.getMessage());
        } catch (ClassCastException e) {
            LOGGER.error("Result did not match expected format", e);
            pm.rollbackAndClose();
            return errorResponse(response, 500, "Illegal result type: " + e.getMessage());
        } finally {
            maybeCommitAndClose();
        }
    }

    private ServiceResponse executePost(ServiceRequest request, ServiceResponse response) {
        String urlPath = request.getUrlPath();
        if (urlPath == null || urlPath.equals("/")) {
            return errorResponse(response, 400, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }

        PersistenceManager pm = getPm();
        try {
            return handlePost(pm, urlPath, response, request);
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 401, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 400, "Incorrect request: " + e.getMessage());
        } catch (IOException | RuntimeException e) {
            LOGGER.error(FAILED_TO_HANDLE_REQUEST_DETAILS_IN_DEBUG, e.getMessage());
            LOGGER.debug(EXCEPTION, e);
            rollbackAndClose(pm);
            return errorResponse(response, 500, "Failed to store data.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handlePost(PersistenceManager pm, String urlPath, ServiceResponse response, ServiceRequest request) throws IOException {
        ResourcePath path;
        final Version version = request.getVersion();
        try {
            path = PathParser.parsePath(
                    modelRegistry,
                    settings.getQueryDefaults().getServiceRootUrl(),
                    version,
                    urlPath);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorResponse(response, 404, NOT_A_VALID_PATH + ": " + e.getMessage());
        }
        if (!(path.getMainElement() instanceof PathElementEntitySet)) {
            return errorResponse(response, 400, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }

        Query query;
        ResultFormatter formatter;
        try {
            query = QueryParser
                    .parseQuery(request.getUrlQuery(), settings, path)
                    .validate();
            settings.getPluginManager().parsedQuery(settings, request, query);
            formatter = findFormatter(query, request, version);
        } catch (IllegalArgumentException | IncorrectRequestException ex) {
            return errorResponse(response, 400, ex.getMessage());
        }

        if (!pm.validatePath(path)) {
            maybeCommitAndClose();
            return errorResponse(response, 404, NOTHING_FOUND_RESPONSE);
        }

        PathElementEntitySet mainSet = (PathElementEntitySet) path.getMainElement();
        EntityType type = mainSet.getEntityType();
        JsonReader jsonReader = new JsonReader(modelRegistry);
        Entity entity;
        try {
            entity = jsonReader.parseEntity(type, request.getContentReader());
            entity.complete(mainSet);
            settings.getCustomLinksHelper().cleanPropertiesMap(entity);
        } catch (JsonParseException | JsonMappingException | IncompleteEntityException | IllegalStateException ex) {
            LOGGER.trace("Post failed.", ex);
            return errorResponse(response, 400, ex.getMessage());
        }

        try {
            if (!pm.insert(entity)) {
                LOGGER.debug("No need to insert entity.");
            }
            maybeCommitAndClose();

            entity.setQuery(query);
            response.setResult(entity);
            response.setCode(201);
            if (query.getMetadata() != Metadata.OFF) {
                String url = UrlHelper.generateSelfLink(null, path, entity);
                response.addHeader(Constants.HEADER_LOCATION, url);
            }
            return formatResponse(response, formatter, query, path, entity);
        } catch (IllegalArgumentException | IncompleteEntityException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 400, e.getMessage());
        }
    }

    public ResultFormatter findFormatter(Query query, ServiceRequest request, Version version) throws IncorrectRequestException {
        ResultFormatter formatter;
        String format = query.getFormat();
        if (format == null) {
            format = request.getParameter(REQUEST_PARAM_FORMAT);
        }
        formatter = settings.getFormatter(version, format);
        return formatter;
    }

    private ServiceResponse executePatch(ServiceRequest request, ServiceResponse response, boolean isChangeSet) {
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
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 401, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 403, e.getMessage());
        } catch (IncompleteEntityException | IOException | RuntimeException exc) {
            LOGGER.error(FAILED_TO_HANDLE_REQUEST_DETAILS_IN_DEBUG, exc.getMessage());
            LOGGER.debug(EXCEPTION, exc);
            rollbackAndClose(pm);
            return errorResponse(response, 500, "Failed to store data.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handlePatch(PersistenceManager pm, ServiceRequest request, ServiceResponse response) throws IOException {
        PathElementEntity mainElement;
        Entity entity;
        try {
            mainElement = parsePathForPutPatch(pm, request);
            JsonReader entityParser = new JsonReader(modelRegistry);
            entity = entityParser.parseEntity(mainElement.getEntityType(), request.getContentReader());
            settings.getCustomLinksHelper().cleanPropertiesMap(entity);
            entity.getEntityType().validateUpdate(entity);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid for patch.", exc);
            return errorResponse(response, 400, exc.getMessage());
        } catch (JsonParseException | JsonMappingException exc) {
            LOGGER.trace(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, 400, COULD_NOT_PARSE_JSON + " " + exc.getMessage());
        } catch (IncompleteEntityException | NoSuchEntityException exc) {
            return errorResponse(response, 404, exc.getMessage());
        }

        try {
            if (pm.update(mainElement, entity)) {
                maybeCommitAndClose();
                response.setCode(200);
                return response;
            } else {
                pm.rollbackAndClose();
                return errorResponse(response, 400, "Failed to patch entity.");
            }
        } catch (IllegalArgumentException | IncompleteEntityException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 400, e.getMessage());
        }
    }

    private ServiceResponse handleChangeSet(PersistenceManager pm, ServiceRequest request, ServiceResponse response) throws IOException, IncompleteEntityException {
        PathElementEntity mainElement;
        JsonPatch jsonPatch;
        try {
            mainElement = parsePathForPutPatch(pm, request);
            jsonPatch = SimpleJsonMapper.getSimpleObjectMapper().readValue(request.getContentReader(), JsonPatch.class);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid.", exc);
            return errorResponse(response, 400, exc.getMessage());
        } catch (JsonParseException exc) {
            LOGGER.trace(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, 400, COULD_NOT_PARSE_JSON);
        } catch (NoSuchEntityException exc) {
            return errorResponse(response, 404, exc.getMessage());
        }

        try {
            if (pm.update(mainElement, jsonPatch)) {
                maybeCommitAndClose();
                return successResponse(response, 200, "JSON-Patch applied.");
            } else {
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
                    modelRegistry,
                    settings.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    request.getUrlPath());
        } catch (IllegalArgumentException | IllegalStateException exc) {
            throw new NoSuchEntityException(NOT_A_VALID_PATH + ": " + exc.getMessage());
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

    private ServiceResponse executePut(ServiceRequest request, ServiceResponse response) {
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return errorResponse(response, 400, "PATCH only allowed on Entities.");
            }

            pm = getPm();
            return handlePut(pm, request, response);
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 401, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 403, e.getMessage());
        } catch (IncompleteEntityException | IOException | RuntimeException e) {
            LOGGER.trace("Failed to handle request", e);
            rollbackAndClose(pm);
            return errorResponse(response, 400, e.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handlePut(PersistenceManager pm, ServiceRequest request, ServiceResponse response) throws IOException, IncompleteEntityException {
        PathElementEntity mainElement;
        Entity entity;
        try {
            mainElement = parsePathForPutPatch(pm, request);

            JsonReader entityParser = new JsonReader(modelRegistry);
            entity = entityParser.parseEntity(mainElement.getEntityType(), request.getContentReader());
            entity.complete(true);
            settings.getCustomLinksHelper().cleanPropertiesMap(entity);
            entity.setEntityPropertiesSet(true, true);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid.", exc);
            return errorResponse(response, 400, exc.getMessage());
        } catch (JsonParseException | IncompleteEntityException exc) {
            LOGGER.trace(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, 400, COULD_NOT_PARSE_JSON);
        } catch (NoSuchEntityException exc) {
            return errorResponse(response, 404, exc.getMessage());
        }

        try {
            if (pm.update(mainElement, entity)) {
                maybeCommitAndClose();
                return successResponse(response, 200, "Updated.");
            } else {
                pm.rollbackAndClose();
                return errorResponse(response, 400, FAILED_TO_UPDATE_ENTITY);
            }
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 400, e.getMessage());
        }
    }

    private ServiceResponse executeDelete(ServiceRequest request, ServiceResponse response) {
        if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
            return errorResponse(response, 400, "DELETE only allowed on Entities and Sets.");
        }

        ResourcePath path;
        try {
            path = PathParser.parsePath(
                    modelRegistry,
                    settings.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    request.getUrlPath());
        } catch (IllegalArgumentException | IllegalStateException exc) {
            return errorResponse(response, 404, NOT_A_VALID_PATH + ": " + exc.getMessage());
        }

        if (path.isRef()) {
            return executeDeleteRef(request, response, path);
        }
        if ((path.getMainElement() instanceof PathElementEntity)) {
            return executeDeleteEntity(request, response, path);
        }
        if (settings.isFilterDeleteEnabled() && (path.getMainElement() instanceof PathElementEntitySet)) {
            return executeDeleteSet(request, response, path);
        }
        return errorResponse(response, 400, "Not a valid path for DELETE.");
    }

    private ServiceResponse executeDeleteEntity(ServiceRequest request, ServiceResponse response, ResourcePath path) {
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

            return handleDeleteEntity(pm, mainEntity, response);
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 401, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 403, e.getMessage());
        } catch (Exception e) {
            LOGGER.trace("", e);
            rollbackAndClose(pm);
            return errorResponse(response, 400, e.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handleDeleteEntity(PersistenceManager pm, PathElementEntity mainEntity, ServiceResponse response) {
        try {
            if (pm.delete(mainEntity)) {
                maybeCommitAndClose();
                response.setCode(200);
                return response;
            } else {
                pm.rollbackAndClose();
                return errorResponse(response, 400, "Failed to delete entity.");
            }
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, 404, e.getMessage());
        }
    }

    private ServiceResponse executeDeleteSet(ServiceRequest request, ServiceResponse response, ResourcePath path) {
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
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 403, e.getMessage());
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, 401, e.getMessage());
        } catch (Exception e) {
            LOGGER.trace("", e);
            rollbackAndClose(pm);
            return errorResponse(response, 400, e.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handleDeleteSet(ServiceRequest request, ServiceResponse response, PersistenceManager pm, ResourcePath path) {
        Query query;
        try {
            query = QueryParser
                    .parseQuery(request.getUrlQuery(), settings, path)
                    .validate();
            settings.getPluginManager().parsedQuery(settings, request, query);
        } catch (IllegalArgumentException e) {
            return errorResponse(response, 400, "Failed to parse query: " + e.getMessage());
        }
        if (query.getCount().isPresent()) {
            return errorResponse(response, 400, "$count not allowed on delete requests.");
        }
        if (!query.getExpand().isEmpty()) {
            return errorResponse(response, 400, "$expand not allowed on delete requests.");
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

    private static record LinkData(
            PathElementEntity sourceEntity,
            NavigationPropertyEntitySet navigationProperty,
            PathElementEntity targetEntity,
            String message) {

        public static LinkData ok(PathElementEntity sourceEntity, NavigationPropertyEntitySet navigationProperty, PathElementEntity targetEntity) {
            return new LinkData(sourceEntity, navigationProperty, targetEntity, null);
        }

        public static LinkData error(String message) {
            return new LinkData(null, null, null, message);
        }
    }

    private ServiceResponse executeDeleteRef(ServiceRequest request, ServiceResponse response, ResourcePath path) {
        // Three Options:
        // 1. DELETE http://host/service/Categories(1)/Products/$ref?$id=../../Products(0)
        // 2. DELETE http://host/service/Categories(1)/Products(0)/$ref
        // 3. DELETE http://host/service/Products(0)/Category/$ref  (1-to-many can be deleted from the other side)
        final PersistenceManager pm = getPm();
        if (!pm.validatePath(path)) {
            maybeRollbackAndClose();
            return errorResponse(response, 404, NOTHING_FOUND_RESPONSE);
        }

        final List<PathElement> pathElements = path.getPathElements();
        if (pathElements.size() < 2) {
            return errorResponse(response, 400, "Path must contain at least an Entity and a NavigationProperty to delete a reference.");
        }

        final LinkData linkData;
        final PathElement lastElement = path.getLastElement();
        if (lastElement instanceof PathElementEntitySet containingSet) {
            // Option 1
            linkData = parseForRefWithId(request, path, containingSet);
        } else if ((lastElement instanceof PathElementEntity peEntity)) {
            // Option 2 or 3
            linkData = parseForRefInPath(path, peEntity);
        } else {
            return errorResponse(response, 400, "Not a valid DELETE-Reference action.");
        }
        if (linkData.message != null) {
            return errorResponse(response, 400, linkData.message);
        }
        if (!linkData.navigationProperty.getEntityType().equals(linkData.targetEntity.getEntityType())) {
            return errorResponse(response, 400, "Target Entity does not match NavigationProperty type: " + linkData.targetEntity.getEntityType().entityName + " != " + linkData.navigationProperty.getEntityType().entityName);
        }

        try {
            pm.deleteRelation(linkData.sourceEntity, linkData.navigationProperty, linkData.targetEntity);
            maybeCommitAndClose();
            return successResponse(response, 204, "");
        } catch (IncompleteEntityException ex) {
            pm.rollbackAndClose();
            return errorResponse(response, 405, ex.getMessage());
        } catch (NoSuchEntityException ex) {
            return errorResponse(response, 404, ex.getMessage());
        }
    }

    private LinkData parseForRefWithId(ServiceRequest request, ResourcePath path, PathElementEntitySet containingSet) {
        PathElementEntity sourceEntity;
        NavigationPropertyEntitySet navigationProperty;
        final List<PathElement> pathElements = path.getPathElements();
        final PathElement precedingElement = pathElements.get(pathElements.size() - 2);
        if (precedingElement instanceof PathElementEntity) {
            navigationProperty = containingSet.getNavigationProperty();
            sourceEntity = containingSet.getParent();
        } else {
            return LinkData.error("NavigationProperty must be preceded by an Entity.");
        }
        Query query;
        try {
            query = QueryParser
                    .parseQuery(request.getUrlQuery(), settings, path)
                    .validate();
            settings.getPluginManager().parsedQuery(settings, request, query);
        } catch (IllegalArgumentException ex) {
            return LinkData.error("Failed to parse query: " + ex.getMessage());
        }
        String targetUrl = query.getId();
        final String serviceRootUrl = settings.getQueryDefaults().getServiceRootUrl();
        final Version version = request.getVersion();
        final String versionUrl = version.urlPart;
        if (!targetUrl.startsWith(serviceRootUrl)) {
            try {
                // id is a relative url, resolve against the request url.
                URL requestUrl = new URL(serviceRootUrl + '/' + versionUrl + request.getUrlPath());
                targetUrl = new URL(requestUrl, targetUrl).toString();
            } catch (MalformedURLException ex) {
                return LinkData.error("Failed to parse URL in $id: " + ex.getMessage());
            }
        }
        if (!targetUrl.startsWith(serviceRootUrl)) {
            return LinkData.error("$id parameter must be a relative URL or an absolute URL in this service (Thus start with '" + serviceRootUrl + "'.");
        }
        targetUrl = targetUrl.substring(serviceRootUrl.length() + 1);
        if (!targetUrl.startsWith(versionUrl)) {
            return LinkData.error("$id parameter must use the same version as the request ('" + versionUrl + "').");
        }
        targetUrl = targetUrl.substring(versionUrl.length());
        ResourcePath targetPath = PathParser.parsePath(modelRegistry, serviceRootUrl, version, targetUrl);
        PathElement lastTargetElement = targetPath.getLastElement();
        PathElementEntity targetEntity;
        if (lastTargetElement instanceof PathElementEntity pathElementEntity) {
            targetEntity = pathElementEntity;
        } else {
            return LinkData.error("$id parameter does not point to an Entity.");
        }

        return LinkData.ok(sourceEntity, navigationProperty, targetEntity);
    }

    private LinkData parseForRefInPath(ResourcePath path, PathElementEntity lastElement) {
        PathElementEntity sourceEntity;
        NavigationPropertyEntitySet navigationProperty;
        final List<PathElement> pathElements = path.getPathElements();
        final int lastIdx = pathElements.size() - 1;
        PathElement precedingElement = pathElements.get(lastIdx - 1);
        if (precedingElement instanceof PathElementEntitySet peEntitySet) {
            PathElementEntitySet containingSet = peEntitySet;
            navigationProperty = containingSet.getNavigationProperty();
            sourceEntity = containingSet.getParent();
        } else {
            return LinkData.error("Not a valid DELETE-Reference action.");
        }
        if (sourceEntity.getId() == null || lastElement.getId() == null) {
            return LinkData.error("Could not find Id for source or target entity.");
        }
        return LinkData.ok(sourceEntity, navigationProperty, lastElement);
    }

    public static ServiceResponse successResponse(ServiceResponse response, Version.CannedResponse cr) {
        return successResponse(response, cr.code, cr.message);
    }

    public static ServiceResponse successResponse(ServiceResponse response, int code, String message) {
        return jsonResponse(response, "success", code, message);
    }

    public static ServiceResponse errorResponse(ServiceResponse response, Version.CannedResponse cr) {
        return errorResponse(response, cr.code, cr.message);
    }

    public static ServiceResponse errorResponse(ServiceResponse response, int code, String message) {
        if (code < 500) {
            LOGGER.debug("{} response: {}", code, message);
        }
        if (response == null) {
            response = new ServiceResponseDefault();
        }
        return jsonResponse(response, "error", code, message);
    }

    public static ServiceResponse jsonResponse(ServiceResponse response, String type, int code, String message) {
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
