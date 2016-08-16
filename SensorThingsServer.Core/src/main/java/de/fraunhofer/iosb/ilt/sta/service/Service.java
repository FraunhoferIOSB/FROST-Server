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
package de.fraunhofer.iosb.ilt.sta.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.fraunhofer.iosb.ilt.sta.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.id.LongId;
import de.fraunhofer.iosb.ilt.sta.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.sta.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.sta.util.UrlHelper;
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
 *
 * @author jab
 */
public class Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
    private final CoreSettings settings;

    public Service(CoreSettings settings) {
        this.settings = settings;
        PersistenceManagerFactory.init(settings);
    }

    public <T> ServiceResponse<T> execute(ServiceRequest request) throws MalformedURLException {
        switch (request.getRequestType()) {
            case GetCapabilities:
                return executeGetCapabilities(request);
            case Create:
                return executePost(request);
            case Read:
                return executeGet(request);
            case Delete:
                return executeDelete(request);
            case UpdateAll:
                return executePut(request);
            case UpdateChanges:
                return executePatch(request);
            default:
                return new ServiceResponse<>(500, "Illegal request type.");
        }
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
        PersistenceManager pm = null;
        try {

            ResourcePath path;
            try {
                path = PathParser.parsePath(settings.getServiceRootUrl(), request.getUrlPath());
            } catch (NumberFormatException e) {
                return response.setStatus(404, "Not a valid id.");
            } catch (IllegalStateException e) {
                return response.setStatus(404, "Not a valid id: " + e.getMessage());
            }
            Query query = null;
            try {
                query = QueryParser.parseQuery(request.getUrlQuery(), settings);
            } catch (IllegalArgumentException e) {
                return response.setStatus(404, "Invalid query: " + e.getMessage());
            }

            pm = PersistenceManagerFactory.getInstance().create();
            if (!pm.validatePath(path)) {
                response.setStatus(404, "Nothing found.");
                pm.commitAndClose();
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
                LOGGER.error("Illegal operation.", e);
                response.setStatus(500, "Illegal operation: " + e.getMessage());
                pm.rollbackAndClose();
                return response;
            } catch (ClassCastException e) {
                LOGGER.error("Result did not match expected format", e);
                response.setStatus(500, "Illegal result type: " + e.getMessage());
                pm.rollbackAndClose();
                return response;
            }
            if (object == null) {
                response.setStatus(404, "Nothing found.");
                pm.commitAndClose();
                return response;
            }
            response.setResult(object);
            response.setResultFormatted(request.getFormatter().format(path, query, object, settings.isUseAbsoluteNavigationLinks()));
            response.setCode(200);
            pm.commitAndClose();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (pm != null) {
                pm.rollbackAndClose();
            }
        }
        return response;
    }

    private <T> ServiceResponse<T> executePost(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return response.setStatus(400, "POST only allowed to Collections.");
            }
            ResourcePath path;
            try {
                path = PathParser.parsePath(settings.getServiceRootUrl(), request.getUrlPath());
            } catch (NumberFormatException e) {
                return response.setStatus(404, "Not a valid id.");
            } catch (IllegalStateException e) {
                return response.setStatus(404, "Not a valid id: " + e.getMessage());
            }
            if (!(path.getMainElement() instanceof EntitySetPathElement)) {
                return response.setStatus(400, "POST only allowed to Collections.");
            }
            if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
                return response.setStatus(400, "Not query options allowed on POST.");
            }

            EntitySetPathElement mainSet = (EntitySetPathElement) path.getMainElement();
            EntityType type = mainSet.getEntityType();
            EntityParser entityParser = new EntityParser(LongId.class);
            Entity entity;
            try {
                entity = entityParser.parseEntity(type.getImplementingClass(), request.getContent());
                entity.complete(mainSet);
            } catch (JsonMappingException | IncompleteEntityException | IllegalStateException ex) {
                LOGGER.debug("Post failed.", ex.getMessage());
                LOGGER.debug("Exception:", ex);
                return response.setStatus(400, ex.getMessage());
            }

            pm = PersistenceManagerFactory.getInstance().create();
            try {
                if (pm.insert(entity)) {
                    pm.commitAndClose();
                    String url = UrlHelper.generateSelfLink(path, entity);
                    response.setCode(201);
                    response.addHeader("location", url);
                } else {
                    LOGGER.debug("Failed to insert entity.");
                    pm.rollbackAndClose();
                    return response.setStatus(400, "Failed to insert entity.");
                }
            } catch (IncompleteEntityException | NoSuchEntityException e) {
                pm.rollbackAndClose();
                return response.setStatus(400, e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (pm != null) {
                pm.rollbackAndClose();
            }
        }
        return response;
    }

    private <T> ServiceResponse<T> executePatch(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return response.setStatus(400, "PATCH only allowed on Entities.");
            }
            ResourcePath path;
            try {
                path = PathParser.parsePath(settings.getServiceRootUrl(), request.getUrlPath());
            } catch (NumberFormatException e) {
                return response.setStatus(404, "Not a valid id.");
            } catch (IllegalStateException e) {
                return response.setStatus(404, "Not a valid id: " + e.getMessage());
            }
            if (!(path.getMainElement() instanceof EntityPathElement) || path.getMainElement() != path.getLastElement()) {
                return response.setStatus(400, "PATCH only allowed on Entities.");
            }
            EntityPathElement mainEntity = (EntityPathElement) path.getMainElement();
            if (mainEntity.getId() == null) {
                return response.setStatus(400, "PATCH only allowed on Entities.");
            }
            if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
                return response.setStatus(400, "Not query options allowed on PACTH.");
            }
            EntityParser entityParser = new EntityParser(LongId.class);
            EntityType type = mainEntity.getEntityType();
            Entity entity;
            try {
                entity = entityParser.parseEntity(type.getImplementingClass(), request.getContent());
            } catch (JsonParseException | IncompleteEntityException e) {
                LOGGER.error("Could not parse json.", e);
                return response.setStatus(400, "Could not parse json.");
            }

            pm = PersistenceManagerFactory.getInstance().create();
            try {

                if (pm.update(mainEntity, entity)) {
                    pm.commitAndClose();
                    response.setCode(200);
                } else {
                    LOGGER.debug("Failed to update entity.");
                    pm.rollbackAndClose();
                }
            } catch (NoSuchEntityException e) {
                pm.rollbackAndClose();
                response.setStatus(400, e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (pm != null) {
                pm.rollbackAndClose();
            }
        }
        return response;
    }

    private <T> ServiceResponse<T> executePut(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return response.setStatus(400, "PATCH only allowed on Entities.");
            }
            ResourcePath path;
            try {
                path = PathParser.parsePath(settings.getServiceRootUrl(), request.getUrlPath());
            } catch (NumberFormatException e) {
                return response.setStatus(404, "Not a valid id.");
            } catch (IllegalStateException e) {
                return response.setStatus(404, "Not a valid id: " + e.getMessage());
            }
            if (!(path.getMainElement() instanceof EntityPathElement) || path.getMainElement() != path.getLastElement()) {
                return response.setStatus(400, "PATCH only allowed on Entities.");
            }
            EntityPathElement mainEntity = (EntityPathElement) path.getMainElement();
            if (mainEntity.getId() == null) {
                return response.setStatus(400, "PATCH only allowed on Entities.");
            }
            if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
                return response.setStatus(400, "Not query options allowed on PACTH.");
            }
            EntityParser entityParser = new EntityParser(LongId.class);
            EntityType type = mainEntity.getEntityType();
            Entity entity;
            try {
                entity = entityParser.parseEntity(type.getImplementingClass(), request.getContent());
                entity.complete(true);
                entity.setEntityPropertiesSet();
            } catch (JsonParseException | IncompleteEntityException e) {
                LOGGER.error("Could not parse json.", e);
                return response.setStatus(400, "Could not parse json.");
            }

            pm = PersistenceManagerFactory.getInstance().create();
            try {

                if (pm.update(mainEntity, entity)) {
                    pm.commitAndClose();
                    response.setCode(200);
                } else {
                    LOGGER.debug("Failed to update entity.");
                    pm.rollbackAndClose();
                }
            } catch (NoSuchEntityException e) {
                pm.rollbackAndClose();
                response.setStatus(400, e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (pm != null) {
                pm.rollbackAndClose();
            }
        }
        return response;
    }

    private <T> ServiceResponse<T> executeDelete(ServiceRequest request) {
        ServiceResponse<T> response = new ServiceResponse<>();
        PersistenceManager pm = null;
        try {
            if (request.getUrlPath() == null || request.getUrlPath().equals("/")) {
                return response.setStatus(400, "DELETE only allowed on Entities.");
            }
            ResourcePath path;
            try {
                path = PathParser.parsePath(settings.getServiceRootUrl(), request.getUrlPath());
            } catch (NumberFormatException e) {
                return response.setStatus(404, "Not a valid id.");
            } catch (IllegalStateException e) {
                return response.setStatus(404, "Not a valid id: " + e.getMessage());
            }
            if (!(path.getMainElement() instanceof EntityPathElement)) {
                return response.setStatus(400, "DELETE only allowed on Entities.");
            }
            if (path.getMainElement() != path.getLastElement()) {
                return response.setStatus(400, "DELETE only allowed on Entities.");
            }
            EntityPathElement mainEntity = (EntityPathElement) path.getMainElement();
            if (mainEntity.getId() == null) {
                return response.setStatus(400, "DELETE only allowed on Entities.");
            }
            if (request.getUrlQuery() != null && !request.getUrlQuery().isEmpty()) {
                return response.setStatus(400, "Not query options allowed on PACTH.");
            }

            pm = PersistenceManagerFactory.getInstance().create();
            try {

                if (pm.delete(mainEntity)) {
                    pm.commitAndClose();
                    response.setCode(200);
                } else {
                    LOGGER.debug("Failed to delete entity.");
                    pm.rollbackAndClose();
                }
            } catch (NoSuchEntityException e) {
                pm.rollbackAndClose();
                response.setStatus(404, e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (pm != null) {
                pm.rollbackAndClose();
            }
        }
        return response;
    }
}
