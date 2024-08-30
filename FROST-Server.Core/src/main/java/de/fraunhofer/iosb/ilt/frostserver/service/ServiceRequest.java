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

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract request for the Service.
 */
public class ServiceRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRequest.class);
    private static final ThreadLocal<ServiceRequest> LOCAL_REQUEST = new ThreadLocal<>();

    private String requestType;
    private String urlPath;
    private String urlQuery;
    private String contentString;
    private InputStream contentBinary;
    private Version version;
    private String contentType;
    private Map<String, List<String>> parameterMap;
    private Map<String, Object> attributeMap = new HashMap<>();
    private PrincipalExtended userPrincipal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
    private CoreSettings coreSettings;
    private QueryDefaults queryDefaults;
    private UpdateMode updateMode;
    private JsonReader jsonReader;

    public Map<String, Object> getAttributeMap() {
        return attributeMap;
    }

    public ServiceRequest setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
        return this;
    }

    public ServiceRequest setAttribute(String key, Object value) {
        attributeMap.put(key, value);
        return this;
    }

    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    public ServiceRequest setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        if (queryDefaults == null) {
            queryDefaults = coreSettings.getQueryDefaults();
        }
        return this;
    }

    public String getRequestType() {
        return requestType;
    }

    public ServiceRequest setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    /**
     * Get the content as a String.
     *
     * @return the content as a String.
     */
    public String getContentString() {
        if (contentString != null) {
            return contentString;
        }
        try {
            return IOUtils.toString(contentBinary, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.debug("Failed to convert input to a string", ex);
            LOGGER.error("Failed to convert input to a string: {}", ex.getMessage());
            throw new IllegalStateException("Failed to read input.");
        }
    }

    /**
     * Get the content as a character stream, through a Reader.
     *
     * @return The content in a Reader.
     */
    public Reader getContentReader() {
        if (contentString != null) {
            return new StringReader(contentString);
        }
        return new BufferedReader(new InputStreamReader(contentBinary, StandardCharsets.UTF_8));
    }

    /**
     * Get the content as a (binary) InputStream.
     *
     * @return The content as InputStream.
     */
    public InputStream getContentStream() {
        if (contentString != null) {
            return new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        }
        return contentBinary;
    }

    public ServiceRequest setContent(InputStream content) {
        this.contentBinary = content;
        return this;
    }

    public ServiceRequest setContent(String content) {
        this.contentString = content;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public ServiceRequest setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public UpdateMode getUpdateMode() {
        return updateMode;
    }

    public ServiceRequest setUpdateMode(UpdateMode updateMode) {
        this.updateMode = updateMode;
        return this;
    }

    public Map<String, List<String>> getParameterMap() {
        if (parameterMap == null) {
            parameterMap = new HashMap<>();
        }
        return parameterMap;
    }

    public ServiceRequest setParameterMap(Map<String, List<String>> parameterMap) {
        this.parameterMap = parameterMap;
        return this;
    }

    public String getParameter(String parameter) {
        return getParameter(parameter, null);
    }

    public String getParameter(String parameter, String dflt) {
        if (parameterMap == null) {
            return dflt;
        }
        List<String> list = parameterMap.get(parameter);
        if (list == null || list.isEmpty()) {
            return dflt;
        }
        return list.get(0);
    }

    public ServiceRequest addParameterIfAbsent(String name, String value) {
        getParameterMap().putIfAbsent(name, Arrays.asList(value));
        return this;
    }

    public QueryDefaults getQueryDefaults() {
        return queryDefaults;
    }

    public ServiceRequest setQueryDefaults(QueryDefaults queryDefaults) {
        this.queryDefaults = queryDefaults;
        return this;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public ServiceRequest setUrlPath(String urlPath) {
        this.urlPath = urlPath;
        return this;
    }

    public String getUrlQuery() {
        return urlQuery;
    }

    public ServiceRequest setUrlQuery(String urlQuery) {
        this.urlQuery = urlQuery;
        return this;
    }

    public String getUrl() {
        if (urlQuery == null || urlQuery.isEmpty()) {
            return urlPath;
        }
        return urlPath + "?" + urlQuery;
    }

    public final ServiceRequest setUrl(String url) {
        if (url.contains("?")) {
            this.urlPath = url.substring(0, url.lastIndexOf('?'));
            this.urlQuery = url.substring(url.indexOf('?') + 1);
        } else {
            this.urlPath = url;
            this.urlQuery = null;
        }
        return this;
    }

    public JsonReader getJsonReader() {
        if (jsonReader == null) {
            jsonReader = new JsonReaderDefault(coreSettings.getModelRegistry(), userPrincipal);
        }
        return jsonReader;
    }

    public ServiceRequest setJsonReader(JsonReader jsonReader) {
        this.jsonReader = jsonReader;
        return this;
    }

    public PrincipalExtended getUserPrincipal() {
        return userPrincipal;
    }

    public ServiceRequest setUserPrincipal(PrincipalExtended userPrincipal) {
        this.userPrincipal = userPrincipal;
        return this;
    }

    /**
     * Get the API version for this request.
     *
     * @return the API version for this request.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Set the API version for this request.
     *
     * @param version the API version for this request.
     * @return this.
     */
    public ServiceRequest setVersion(Version version) {
        this.version = version;
        return this;
    }

    public static ServiceRequest getLocalRequest() {
        return LOCAL_REQUEST.get();
    }

    public static void setLocalRequest(ServiceRequest localRequest) {
        LOCAL_REQUEST.set(localRequest);
        PrincipalExtended.setLocalPrincipal(localRequest.getUserPrincipal());
    }

    public static void removeLocalRequest() {
        LOCAL_REQUEST.remove();
        PrincipalExtended.removeLocalPrincipal();
    }

}
