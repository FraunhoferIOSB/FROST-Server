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
package de.fraunhofer.iosb.ilt.frostserver.service;

import static de.fraunhofer.iosb.ilt.frostserver.service.PluginResultFormat.FORMAT_NAME_DEFAULT;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.CREATE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.DELETE;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.GET_CAPABILITIES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.READ;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_ALL;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGES;
import static de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils.UPDATE_CHANGESET;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.REQUEST_PARAM_FORMAT;

import com.google.gson.JsonParseException;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityReference;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityReferenceList;
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
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.DuplicateIdException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.ForbiddenException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UnauthorizedException;
import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.snapshots.Unit;
import jakarta.json.JsonPatch;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

/**
 * Executes SensorThings commands. Normally, each call of
 * {@link #execute(ServiceRequest, ServiceResponse) } runs in its own
 * transaction (for back-ends that support transactions). If a transaction is
 * explicitly started with {@link #startTransaction(Principal) }, then all
 * subsequent calls to {@link #execute(ServiceRequest, ServiceResponse)} will
 * run in this transaction, until either {@link #commitTransaction()} is called,
 * or {@link #rollbackTransaction()} is called, or a call to
 * {@link #execute(ServiceRequest, ServiceResponse)} fails with an exception.
 *
 * If a call to {@link #execute(ServiceRequest, ServiceResponse)} fails, the
 * response will have a status code that is not in the 200-299 range. A failed
 * call will always result in a transaction rollback, even when a transaction is
 * explicitly started.
 *
 * This class is not thread-safe.
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
    private static final String LABEL_REQUEST_TYPE = "requestType";
    private static final String LABEL_RESPONSE_CODE = "responseCode";

    private static final Histogram REQUEST_DURATION = Histogram.builder()
            .name("service_request_duration_seconds")
            .help("Service request service time in seconds")
            .unit(Unit.SECONDS)
            .labelNames(LABEL_REQUEST_TYPE)
            .register();
    private static final Counter RESPONSE_CODE = Counter.builder()
            .name("service_request_response_code_total")
            .help("Service request response counts per response code")
            .labelNames(LABEL_RESPONSE_CODE)
            .register();

    private static final String EXCEPTION = "Exception:";
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
        return PluginManager.decodeRequestType(plugin, version, path, method, contentType);
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
        final ServiceResponse finalResponse = doExecute(request, response);
        RESPONSE_CODE.labelValues(Integer.toString(response.getCode())).inc();
        return finalResponse;
    }

    private ServiceResponse doExecute(ServiceRequest request, ServiceResponse response) {
        String requestType = request.getRequestType();
        try (Timer timer = REQUEST_DURATION.labelValues(requestType).startTimer()) {
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
                        return errorResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR, "Illegal request type.");
                    }
                    return plugin.execute(this, request, response);
            }
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
     * Notifies the backend that it is no longer needed. Call either commit, or
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
        final Version version = request.getVersion();
        final String serviceRootUrl = request.getQueryDefaults().getServiceRootUrl();
        final String path = URI.create(serviceRootUrl + '/' + request.getVersion().urlPart + '/')
                .normalize()
                .toString();

        final List<Map<String, String>> capList = new ArrayList<>();
        result.put("value", capList);
        for (EntityType entityType : modelRegistry.getEntityTypes(request.getUserPrincipal().isAdmin())) {
            String collectionUri = path + entityType.plural;
            capList.add(createCapability(entityType.plural, collectionUri));
        }

        settings.getPluginManager().modifyServiceDocument(request, result);

        response.setCode(HttpURLConnection.HTTP_OK);
        response.setResult(result);
        return formatResponse(request, response, result);
    }

    private ServiceResponse formatResponse(ServiceRequest request, ServiceResponse response, Object result) {
        ResultFormatter formatter;
        try {
            formatter = settings.getFormatter(request.getVersion(), FORMAT_NAME_DEFAULT);
        } catch (IncorrectRequestException ex) {
            LOGGER.error("Formatter not available.", ex);
            return errorResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to instantiate formatter");
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
            return errorResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to format");
        }
        return response;
    }

    private Map<String, String> createCapability(String name, String url) {
        Map<String, String> val = new HashMap<>();
        val.put("name", name);
        val.put("url", url);
        return Collections.unmodifiableMap(val);
    }

    private ServiceResponse executeGet(ServiceRequest request, ServiceResponse response) {
        PersistenceManager pm = getPm();
        try {
            return handleGet(pm, request, response);
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_UNAUTHORIZED, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            LOGGER.error(FAILED_TO_HANDLE_REQUEST_DETAILS_IN_DEBUG, e.getMessage());
            LOGGER.debug(EXCEPTION, e);
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to execute query. See logs for details.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handleGet(PersistenceManager pm, ServiceRequest request, ServiceResponse response) {
        final ResourcePath path;
        final Version version = request.getVersion();
        final QueryDefaults queryDefaults = request.getQueryDefaults();
        try {
            path = PathParser.parsePath(modelRegistry,
                    queryDefaults.getServiceRootUrl(), version,
                    request.getUrlPath(),
                    request.getUserPrincipal());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, ex.getMessage());
        }
        Query query;
        ResultFormatter formatter;
        try {
            query = QueryParser
                    .parseQuery(request.getUrlQuery(), queryDefaults, settings, path, request.getUserPrincipal())
                    .validate();
            settings.getPluginManager().parsedQuery(settings, request, query);
            formatter = settings.getFormatter(version, query.getFormat());
            formatter.preProcessRequest(path, query);
        } catch (IllegalArgumentException | IncorrectRequestException ex) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
        }

        if (!pm.validatePath(path)) {
            maybeCommitAndClose();
            return errorResponse(response, version.getCannedResponse(Version.CannedResponseType.NOTHING_FOUND));
        }
        try {
            if (request.isHead()) {
                // Execute the query to check if it works. Doesn't actually fetch data.
                query.setTop(0);
                pm.get(path, query);
                response.setMessage("");
                response.setCode(HttpURLConnection.HTTP_OK);
                return response;
            }
            Object object = pm.get(path, query);
            if (object == null) {
                if (path.isValue() || path.isEntityProperty()) {
                    return successResponse(response, HttpURLConnection.HTTP_NO_CONTENT, "No Content");
                } else {
                    return errorResponse(response, version.getCannedResponse(Version.CannedResponseType.NOTHING_FOUND));
                }
            } else {
                response.setResult(object);
                response.setCode(HttpURLConnection.HTTP_OK);
                return formatResponse(response, formatter, query, path, object);
            }
        } catch (UnsupportedOperationException e) {
            LOGGER.error("Unsupported operation.", e);
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR, "Unsupported operation: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.trace("Illegal operation.", e);
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Illegal operation: " + e.getMessage());
        } catch (ClassCastException e) {
            LOGGER.error("Result did not match expected format", e);
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR, "Illegal result type: " + e.getMessage());
        } finally {
            maybeCommitAndClose();
        }
    }

    private ServiceResponse executePost(ServiceRequest request, ServiceResponse response) {
        String urlPath = request.getUrlPath();
        if (urlPath == null || urlPath.equals("/")) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }
        ResourcePath path;
        try {
            path = PathParser.parsePath(
                    modelRegistry,
                    settings.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    urlPath,
                    request.getUserPrincipal());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, e.getMessage());
        }

        PersistenceManager pm = getPm();
        try {
            if (!pm.validatePath(path)) {
                maybeRollbackAndClose();
                return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, NOTHING_FOUND_RESPONSE);
            }
            if (path.isRef()) {
                return handlePostRef(pm, path, request, response);
            } else if (path.getMainElement() instanceof PathElementEntitySet) {
                return handlePostCollection(pm, path, request, response);
            } else {
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, POST_ONLY_ALLOWED_TO_COLLECTIONS);
            }
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_UNAUTHORIZED, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.debug(EXCEPTION, e);
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Incorrect request: " + e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error(FAILED_TO_HANDLE_REQUEST_DETAILS_IN_DEBUG, e.getMessage());
            LOGGER.debug(EXCEPTION, e);
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to store data.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handlePostRef(PersistenceManager pm, ResourcePath path, ServiceRequest request, ServiceResponse response) {
        // Add one reference to a collection:
        // POST Datastream(1)/ObservedProperties/$ref
        // {"@id": "ObservedProperties(2)"}
        PathElement mainElement = path.getMainElement();
        if (mainElement instanceof PathElementEntitySet mainSet) {
            try {
                EntityReference entityReference = request.getJsonReader().getMapper().readValue(request.getContentReader(), EntityReference.class);
                Entity targetEntity = entityReference.resolve(modelRegistry, request.getUserPrincipal().isAdmin());
                if (targetEntity.getEntityType() != mainSet.getEntityType()) {
                    throw new IllegalArgumentException("Can not add a " + targetEntity.getEntityType() + " to a " + mainSet.getEntityType() + " Set");
                }
                PathElement parent = mainSet.getParent();
                if (parent == null) {
                    throw new IllegalArgumentException("Set does not have a parent entity!");
                }
                if (parent instanceof PathElementEntity parentEntity) {
                    if (!parentEntity.primaryKeyFullySet()) {
                        throw new IllegalArgumentException("Can not resolve target set.");
                    }
                    NavigationPropertyEntitySet navProp = mainSet.getNavigationProperty();
                    pm.addRelation(parentEntity, navProp, targetEntity);
                    maybeCommitAndClose();
                }

            } catch (JacksonException ex) {
                throw new IllegalArgumentException("Failed to parse body as entity reference");
            } catch (NoSuchEntityException ex) {
                return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, NOTHING_FOUND_RESPONSE);
            }

            response.setCode(HttpURLConnection.HTTP_NO_CONTENT);
            return response;
        } else {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, POST_ONLY_ALLOWED_TO_COLLECTIONS);
        }
    }

    private ServiceResponse handlePostCollection(PersistenceManager pm, ResourcePath path, ServiceRequest request, ServiceResponse response) {
        final QueryDefaults queryDefaults = request.getQueryDefaults();
        Query query;
        ResultFormatter formatter;
        try {
            query = QueryParser
                    .parseQuery(request.getUrlQuery(), queryDefaults, settings, path, request.getUserPrincipal())
                    .validate();
            settings.getPluginManager().parsedQuery(settings, request, query);
            formatter = findFormatter(query, request);
        } catch (IllegalArgumentException | IncorrectRequestException ex) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
        }

        PathElementEntitySet mainSet = (PathElementEntitySet) path.getMainElement();
        EntityType type = mainSet.getEntityType();
        JsonReader jsonReader = request.getJsonReader();
        Entity entity;
        try {
            entity = jsonReader.parseEntity(type, request.getContentReader());

            if (mainSet.getParent() != null) {
                type.setParent(mainSet, entity);
            }
            type.validateCreate(entity);
            settings.getCustomLinksHelper().cleanPropertiesMap(entity);
        } catch (IncompleteEntityException | IllegalStateException | IOException ex) {
            LOGGER.trace("Post failed.", ex);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
        }

        try {
            if (!pm.insert(entity, request.getUpdateMode())) {
                LOGGER.debug("No need to insert entity.");
            }
            maybeCommitAndClose();

            entity.setQuery(query);
            response.setResult(entity);
            response.setCode(HttpURLConnection.HTTP_CREATED);
            if (query.getMetadata() != Metadata.OFF) {
                String url = UrlHelper.generateSelfLink(path, entity);
                response.addHeader(Constants.HEADER_LOCATION, url);
            }
            return formatResponse(response, formatter, query, path, entity);
        } catch (DuplicateIdException exc) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_CONFLICT, exc.getMessage());
        } catch (IllegalArgumentException | IncompleteEntityException | NoSuchEntityException exc) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, exc.getMessage());
        }
    }

    public ResultFormatter findFormatter(Query query, ServiceRequest request) throws IncorrectRequestException {
        ResultFormatter formatter;
        String format = query.getFormat();
        if (format == null) {
            format = request.getParameter(REQUEST_PARAM_FORMAT);
        }
        formatter = settings.getFormatter(request.getVersion(), format);
        return formatter;
    }

    private ServiceResponse executePatch(ServiceRequest request, ServiceResponse response, boolean isChangeSet) {
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "PATCH only allowed on Entities.");
            }

            pm = getPm();
            if (isChangeSet) {
                return handleChangeSet(pm, request, response);
            }
            return handlePatch(pm, request, response);
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_UNAUTHORIZED, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_FORBIDDEN, e.getMessage());
        } catch (IncompleteEntityException | IOException | RuntimeException exc) {
            LOGGER.error(FAILED_TO_HANDLE_REQUEST_DETAILS_IN_DEBUG, exc.getMessage());
            LOGGER.debug(EXCEPTION, exc);
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to store data.");
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handlePatch(PersistenceManager pm, ServiceRequest request, ServiceResponse response) throws IOException {
        PathElementEntity mainElement;
        Entity entity;
        try {
            ResourcePath path = parsePathForPutPatch(pm, request);
            PathElement me = path.getMainElement();
            if (me instanceof PathElementEntity mee) {
                mainElement = mee;
            } else {
                throw new IllegalArgumentException("PATCH only allowed on Entities.");
            }
            JsonReader entityParser = request.getJsonReader();
            entity = entityParser.parseEntity(mainElement.getEntityType(), request.getContentReader());
            settings.getCustomLinksHelper().cleanPropertiesMap(entity);
            entity.getEntityType().validateUpdate(entity);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid for patch.", exc);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, exc.getMessage());
        } catch (JsonParseException | JacksonException exc) {
            LOGGER.trace(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, COULD_NOT_PARSE_JSON + " " + exc.getMessage());
        } catch (IncompleteEntityException | NoSuchEntityException exc) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, exc.getMessage());
        }

        try {
            if (pm.update(mainElement, entity, request.getUpdateMode())) {
                maybeCommitAndClose();
                response.setCode(HttpURLConnection.HTTP_OK);
                return response;
            } else {
                pm.rollbackAndClose();
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to patch entity.");
            }
        } catch (DuplicateIdException exc) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_CONFLICT, exc.getMessage());
        } catch (IllegalArgumentException | IncompleteEntityException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }

    private ServiceResponse handleChangeSet(PersistenceManager pm, ServiceRequest request, ServiceResponse response) throws IOException, IncompleteEntityException {
        PathElementEntity mainElement;
        JsonPatch jsonPatch;
        try {
            ResourcePath path = parsePathForPutPatch(pm, request);
            PathElement me = path.getMainElement();
            if (me instanceof PathElementEntity mee) {
                mainElement = mee;
            } else {
                throw new IllegalArgumentException("PATCH only allowed on Entities.");
            }
            jsonPatch = SimpleJsonMapper.getSimpleObjectMapper().readValue(request.getContentReader(), JsonPatch.class);
        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid.", exc);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, exc.getMessage());
        } catch (JacksonException exc) {
            LOGGER.trace(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, COULD_NOT_PARSE_JSON);
        } catch (NoSuchEntityException exc) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, exc.getMessage());
        }

        try {
            if (pm.update(mainElement, jsonPatch)) {
                maybeCommitAndClose();
                return successResponse(response, HttpURLConnection.HTTP_OK, "JSON-Patch applied.");
            } else {
                pm.rollbackAndClose();
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, FAILED_TO_UPDATE_ENTITY);
            }
        } catch (DuplicateIdException exc) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_CONFLICT, exc.getMessage());
        } catch (IllegalArgumentException | NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }

    private ResourcePath parsePathForPutPatch(PersistenceManager pm, ServiceRequest request) throws NoSuchEntityException {
        ResourcePath path;
        try {
            path = PathParser.parsePath(
                    modelRegistry,
                    request.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    request.getUrlPath(),
                    request.getUserPrincipal());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new NoSuchEntityException(ex.getMessage());
        }

        if (!pm.validatePath(path)) {
            throw new NoSuchEntityException("No entity found for path.");
        }

        final PathElement mainElement = path.getMainElement();
        if (mainElement instanceof PathElementEntity pee) {
            if (pee != path.getLastElement() && !path.isRef()) {
                throw new IllegalArgumentException("PATCH & PUT only allowed on Entities or navigation references.");
            }
            if (!path.isRef() && !pee.primaryKeyFullySet()) {
                throw new IllegalArgumentException("Main element of PATCH & PUT must have a full primary key.");
            }
            return path;
        } else if (mainElement instanceof PathElementEntitySet pes) {
            if (pes != path.getLastElement() && !path.isRef()) {
                throw new IllegalArgumentException("PATCH & PUT only allowed on Entities or navigation references.");
            }
            return path;
        }
        throw new IllegalArgumentException("PATCH & PUT only allowed on Entities.");
    }

    private ServiceResponse executePut(ServiceRequest request, ServiceResponse response) {
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "PATCH only allowed on Entities.");
            }

            pm = getPm();
            return handlePut(pm, request, response);
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_UNAUTHORIZED, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_FORBIDDEN, e.getMessage());
        } catch (IOException | RuntimeException e) {
            LOGGER.trace("Failed to handle request", e);
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handlePut(PersistenceManager pm, ServiceRequest request, ServiceResponse response) throws IOException {
        // TODO: Replace all references in a set,
        //   PUT Datastream(1)/ObservedProperties/$ref
        //     {"value": [{ "@id": "ObservedProperties(2)" },{ "@id": "ObservedProperties(3)" }]}
        // or the one reference for non-sets:
        //   PUT Datastream(1)/Thing/$ref
        //     {"@id": "Things(2)"}
        try {
            ResourcePath path = parsePathForPutPatch(pm, request);
            if (path.isRef()) {
                return handlePutRef(pm, request, path, response);
            } else {
                return handlePutEntity(pm, request, path, response);
            }

        } catch (IllegalArgumentException exc) {
            LOGGER.trace("Path not valid.", exc);
            LOGGER.debug(EXCEPTION, exc);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, exc.getMessage());
        } catch (NoSuchEntityException exc) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, exc.getMessage());
        }
    }

    private ServiceResponse handlePutRef(PersistenceManager pm, ServiceRequest request, ResourcePath path, ServiceResponse response) {
        PathElement mainElement = path.getMainElement();
        if (mainElement instanceof PathElementEntitySet mainSet) {
            return handlePutRefSet(pm, request, mainSet, response);
        }
        if (mainElement instanceof PathElementEntity mainEntity) {
            return handlePutRefEntity(pm, request, mainEntity, response);
        }
        return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "PUT to ref only allowed on Entity or EntitySet");
    }

    private ServiceResponse handlePutRefSet(PersistenceManager pm, ServiceRequest request, PathElementEntitySet mainSet, ServiceResponse response) {
        //   PUT Datastream(1)/ObservedProperties/$ref
        //     {"value": [{ "@id": "ObservedProperties(2)" },{ "@id": "ObservedProperties(3)" }]}
        try {
            EntityReferenceList references = request.getJsonReader().getMapper().readValue(request.getContentReader(), EntityReferenceList.class);
            List<Entity> targetEntities = references.resolve(
                    mainSet.getEntityType(),
                    modelRegistry,
                    request.getUserPrincipal().isAdmin());

            PathElement parent = mainSet.getParent();
            if (parent == null) {
                throw new IllegalArgumentException("Set does not have a parent entity!");
            }
            if (parent instanceof PathElementEntity parentEntity) {
                if (!parentEntity.primaryKeyFullySet()) {
                    throw new IllegalArgumentException("Can not resolve target set.");
                }
                NavigationPropertyEntitySet navProp = mainSet.getNavigationProperty();
                pm.setRelation(parentEntity, navProp, targetEntities);
                response.setCode(HttpURLConnection.HTTP_NO_CONTENT);
                maybeCommitAndClose();
                return response;
            }
            throw new IllegalArgumentException("Can not resolve target set.");
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Failed to parse body as entity reference");
        } catch (NoSuchEntityException ex) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, NOTHING_FOUND_RESPONSE);
        }
    }

    private ServiceResponse handlePutRefEntity(PersistenceManager pm, ServiceRequest request, PathElementEntity mainEntity, ServiceResponse response) {
        //   PUT Datastream(1)/Thing/$ref
        //     {"@id": "Things(2)"}
        try {
            EntityReference entityReference = request.getJsonReader().getMapper().readValue(request.getContentReader(), EntityReference.class);
            Entity targetEntity = entityReference.resolve(modelRegistry, request.getUserPrincipal().isAdmin());
            if (targetEntity.getEntityType() != mainEntity.getEntityType()) {
                throw new IllegalArgumentException("Can not use a " + targetEntity.getEntityType() + " as a " + mainEntity.getEntityType() + " Set");
            }
            PathElement parent = mainEntity.getParent();
            if (parent == null) {
                throw new IllegalArgumentException("Entity does not have a parent entity!");
            }
            if (parent instanceof PathElementEntity parentEntity) {
                if (!parentEntity.primaryKeyFullySet()) {
                    throw new IllegalArgumentException("Can not resolve target set.");
                }
                NavigationPropertyEntity navProp = mainEntity.getNavigationProperty();
                pm.setRelation(parentEntity, navProp, targetEntity);
            }

            response.setCode(HttpURLConnection.HTTP_NO_CONTENT);
            maybeCommitAndClose();
            return response;
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Failed to parse body as entity reference");
        } catch (NoSuchEntityException ex) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, NOTHING_FOUND_RESPONSE);
        }
    }

    private ServiceResponse handlePutEntity(PersistenceManager pm, ServiceRequest request, ResourcePath path, ServiceResponse response) {
        PathElementEntity mainElement;
        Entity entity;

        PathElement me = path.getMainElement();
        if (me instanceof PathElementEntity mee) {
            mainElement = mee;
        } else {
            throw new IllegalArgumentException("PATCH only allowed on Entities.");
        }

        JsonReader entityParser = request.getJsonReader();
        try {
            entity = entityParser.parseEntity(mainElement.getEntityType(), request.getContentReader());
        } catch (IOException exc) {
            LOGGER.trace(COULD_NOT_PARSE_JSON, exc);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, COULD_NOT_PARSE_JSON);

        }

        try {
            entity.setEntityPropertiesSet(true, true);
            entity.validateUpdate();
            settings.getCustomLinksHelper().cleanPropertiesMap(entity);
            entity.setEntityPropertiesSet(true, true);

            if (pm.update(mainElement, entity, request.getUpdateMode())) {
                maybeCommitAndClose();
                return successResponse(response, HttpURLConnection.HTTP_OK, "Updated.");
            } else {
                pm.rollbackAndClose();
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, FAILED_TO_UPDATE_ENTITY);
            }
        } catch (DuplicateIdException exc) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_CONFLICT, exc.getMessage());
        } catch (NoSuchEntityException | IncompleteEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }

    private ServiceResponse executeDelete(ServiceRequest request, ServiceResponse response) {
        if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "DELETE only allowed on Entities and Sets.");
        }

        ResourcePath path;
        try {
            path = PathParser.parsePath(
                    modelRegistry,
                    request.getQueryDefaults().getServiceRootUrl(),
                    request.getVersion(),
                    request.getUrlPath(),
                    request.getUserPrincipal());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, ex.getMessage());
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
        return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Not a valid path for DELETE.");
    }

    private ServiceResponse executeDeleteEntity(ServiceRequest request, ServiceResponse response, ResourcePath path) {
        PersistenceManager pm = null;
        try {
            PathElementEntity mainEntity = (PathElementEntity) path.getMainElement();
            if (mainEntity != path.getLastElement()) {
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "DELETE not allowed on properties.");
            }
            if (!mainEntity.primaryKeyFullySet()) {
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "No ID found.");
            }
            if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "No query options allowed on PATH when deleting an entity.");
            }

            pm = getPm();

            if (!pm.validatePath(path)) {
                maybeCommitAndClose();
                return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, NOTHING_FOUND_RESPONSE);
            }

            return handleDeleteEntity(pm, mainEntity, response);
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_UNAUTHORIZED, e.getMessage());
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            LOGGER.trace("", e);
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handleDeleteEntity(PersistenceManager pm, PathElementEntity mainEntity, ServiceResponse response) {
        try {
            if (pm.delete(mainEntity)) {
                maybeCommitAndClose();
                response.setCode(HttpURLConnection.HTTP_OK);
                return response;
            } else {
                pm.rollbackAndClose();
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to delete entity.");
            }
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, e.getMessage());
        }
    }

    private ServiceResponse executeDeleteSet(ServiceRequest request, ServiceResponse response, ResourcePath path) {
        PersistenceManager pm = null;
        try {
            PathElementEntitySet mainEntity = (PathElementEntitySet) path.getMainElement();
            if (mainEntity != path.getLastElement()) {
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "DELETE not allowed on properties.");
            }

            pm = getPm();

            if (!pm.validatePath(path)) {
                maybeCommitAndClose();
                return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, NOTHING_FOUND_RESPONSE);
            }

            return handleDeleteSet(request, response, pm, path);
        } catch (ForbiddenException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_FORBIDDEN, e.getMessage());
        } catch (UnauthorizedException e) {
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            LOGGER.trace("", e);
            rollbackAndClose(pm);
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        } finally {
            maybeRollbackAndClose();
        }
    }

    private ServiceResponse handleDeleteSet(ServiceRequest request, ServiceResponse response, PersistenceManager pm, ResourcePath path) {
        Query query;
        try {
            query = QueryParser
                    .parseQuery(request.getUrlQuery(), request.getQueryDefaults(), settings, path, request.getUserPrincipal())
                    .validate();
            settings.getPluginManager().parsedQuery(settings, request, query);
        } catch (IllegalArgumentException e) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to parse query: " + e.getMessage());
        }
        if (query.getCount().isPresent()) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "$count not allowed on delete requests.");
        }
        if (!query.getExpand().isEmpty()) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "$expand not allowed on delete requests.");
        }
        if (query.getTop().isPresent()) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "$top not allowed on delete requests.");
        }
        if (query.getSkip().isPresent()) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "$skip not allowed on delete requests.");
        }

        try {
            pm.delete(path, query);
            maybeCommitAndClose();
            return successResponse(response, HttpURLConnection.HTTP_OK, "Deleted.");
        } catch (NoSuchEntityException e) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, e.getMessage());
        }
    }

    private static record LinkData(
            PathElementEntity sourceEntity,
            NavigationPropertyEntitySet npSet,
            NavigationPropertyEntity npEntity,
            PathElementEntity targetEntity,
            String message) {

        NavigationPropertyMain getNp() {
            if (npSet != null) {
                return npSet;
            } else {
                return npEntity;
            }
        }

        public boolean isEntity() {
            return npEntity != null;
        }

        public static LinkData ok(PathElementEntity sourceEntity, NavigationPropertyMain np, PathElementEntity targetEntity) {
            if (np instanceof NavigationPropertyEntitySet nps) {
                return new LinkData(sourceEntity, nps, null, targetEntity, null);
            }
            if (np instanceof NavigationPropertyEntity npe) {
                return new LinkData(sourceEntity, null, npe, targetEntity, null);
            }
            return new LinkData(null, null, null, null, "Unknown type of NavigationProperty");
        }

        public static LinkData error(String message) {
            return new LinkData(null, null, null, null, message);
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
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, NOTHING_FOUND_RESPONSE);
        }

        final List<PathElement> pathElements = path.getPathElements();
        if (pathElements.size() < 2) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Path must contain at least an Entity and a NavigationProperty to delete a reference.");
        }

        final LinkData linkData;
        final PathElement lastElement = path.getLastElement();
        if (lastElement instanceof PathElementEntitySet containingSet) {
            // Option 1
            linkData = parseForRefWithId(request, path, containingSet);
        } else if ((lastElement instanceof PathElementEntity lastEntity)) {
            // Option 2 or 3
            final int lastIdx = pathElements.size() - 1;
            PathElement precedingElement = pathElements.get(lastIdx - 1);
            if (precedingElement instanceof PathElementEntitySet containingSet) {
                NavigationPropertyEntitySet navigationProperty = containingSet.getNavigationProperty();
                PathElementEntity sourceEntity = containingSet.getParent();
                linkData = LinkData.ok(sourceEntity, navigationProperty, lastEntity);
            } else if (precedingElement instanceof PathElementEntity parentEntity) {
                NavigationPropertyEntity navigationProperty = lastEntity.getNavigationProperty();
                Entity target = getPm().get(path, null, Entity.class);
                if (target == null) {
                    // No target, so nothing to delete.
                    return successResponse(response, HttpURLConnection.HTTP_NO_CONTENT, "");
                }
                lastEntity.setPkValues(target.getPrimaryKeyValues());
                linkData = LinkData.ok(parentEntity, navigationProperty, lastEntity);
            } else {
                return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Not a valid DELETE-Reference action.");
            }

        } else {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Not a valid DELETE-Reference action.");
        }
        if (linkData.message != null) {
            return errorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, linkData.message);
        }
        if (!linkData.getNp().getEntityType().equals(linkData.targetEntity.getEntityType())) {
            return errorResponse(
                    response,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "Target Entity does not match NavigationProperty type: " + linkData.targetEntity.getEntityType().entityName + " != " + linkData.npSet.getEntityType().entityName);
        }

        try {
            pm.deleteRelation(linkData.sourceEntity, linkData.getNp(), linkData.targetEntity);
            maybeCommitAndClose();
            return successResponse(response, HttpURLConnection.HTTP_NO_CONTENT, "");
        } catch (IncompleteEntityException ex) {
            pm.rollbackAndClose();
            return errorResponse(response, HttpURLConnection.HTTP_BAD_METHOD, ex.getMessage());
        } catch (NoSuchEntityException ex) {
            return errorResponse(response, HttpURLConnection.HTTP_NOT_FOUND, ex.getMessage());
        }
    }

    /**
     * Parses
     * http://host/service/Categories(1)/Products/$ref?$id=../../Products(0)
     *
     * @param request the request
     * @param path the path
     * @param containingSet the set (Products in the example)
     * @return the LinkData
     */
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
        final QueryDefaults queryDefaults = request.getQueryDefaults();
        try {
            query = QueryParser
                    .parseQuery(request.getUrlQuery(), queryDefaults, settings, path, request.getUserPrincipal())
                    .validate();
            settings.getPluginManager().parsedQuery(settings, request, query);
        } catch (IllegalArgumentException ex) {
            return LinkData.error("Failed to parse query: " + ex.getMessage());
        }
        String targetUrl = query.getId();
        final String serviceRootUrl = queryDefaults.getServiceRootUrl();
        final Version version = request.getVersion();
        final String versionUrl = version.urlPart;
        if (!targetUrl.startsWith(serviceRootUrl)) {
            try {
                // id is a relative url, resolve against the request url.
                URI requestUri = URI.create(serviceRootUrl + '/' + versionUrl + request.getUrlPath());
                targetUrl = requestUri.resolve(targetUrl).toURL().toString();
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
        ResourcePath targetPath = PathParser.parsePath(modelRegistry, serviceRootUrl, version, targetUrl, request.getUserPrincipal());
        PathElement lastTargetElement = targetPath.getLastElement();
        PathElementEntity targetEntity;
        if (lastTargetElement instanceof PathElementEntity pathElementEntity) {
            targetEntity = pathElementEntity;
        } else {
            return LinkData.error("$id parameter does not point to an Entity.");
        }

        return LinkData.ok(sourceEntity, navigationProperty, targetEntity);
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
        if (code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
            LOGGER.debug("{} response: {}", code, message);
        }
        if (response == null) {
            response = new ServiceResponseDefault();
        }
        return jsonResponse(response, "error", code, message);
    }

    public static ServiceResponse jsonResponse(ServiceResponse response, String type, int code, String message) {
        String cleanMessage = StringHelper.cleanForOutput(message, HttpURLConnection.HTTP_OK);
        Map<String, Object> body = new HashMap<>();
        body.put("type", type);
        body.put("code", code);
        body.put("message", cleanMessage);
        try {
            return response.setStatus(code, JsonWriter.writeObject(body));
        } catch (IOException ex) {
            LOGGER.error("Failed to serialise error response.", ex);
        }
        return response.setStatus(code, cleanMessage);
    }

}
