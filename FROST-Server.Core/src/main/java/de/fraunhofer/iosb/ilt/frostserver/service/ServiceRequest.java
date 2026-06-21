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
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SequencedSet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract request for the Service.
 */
public class ServiceRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRequest.class);
    private static final ThreadLocal<ServiceRequest> LOCAL_REQUEST = new ThreadLocal<>();
    private static final UrlPrefixGenerator PREFIX_GEN_DEFAULT = r -> r.getQueryDefaults().getServiceRootUrl() + '/' + r.getVersion().urlPart + '/';

    private String requestType;
    private boolean head;
    private Version version;
    private String urlPath;
    private String urlQuery;
    private String contentType;
    private String contentString;
    private InputStream contentBinary;
    private Map<String, SequencedSet<String>> parameterMap;
    private PrincipalExtended userPrincipal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
    private CoreSettings coreSettings;
    private QueryDefaults queryDefaults;
    private JsonReader jsonReader;
    private UrlPrefixGenerator prefixGen = PREFIX_GEN_DEFAULT;

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
     * Indicates the read request is a HEAD request and does not expect data in
     * the response.
     *
     * @return true if it is a HEAD request.
     */
    public boolean isHead() {
        return head;
    }

    public ServiceRequest setHead(boolean head) {
        this.head = head;
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

    public Map<String, SequencedSet<String>> getParameterMap() {
        if (parameterMap == null) {
            parameterMap = new HashMap<>();
        }
        return parameterMap;
    }

    public ServiceRequest setParameterMap(Map<String, SequencedSet<String>> parameterMap) {
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
        SequencedSet<String> list = parameterMap.get(parameter);
        if (list == null || list.isEmpty()) {
            return dflt;
        }
        return list.getFirst();
    }

    public ServiceRequest setParameter(String name, String value) {
        final LinkedHashSet<String> newSet = new LinkedHashSet<>();
        newSet.add(value);
        getParameterMap().put(name, newSet);
        return this;
    }

    public ServiceRequest addParameter(String name, String value) {
        getParameterMap().computeIfAbsent(name, t -> new LinkedHashSet<>())
                .add(value);
        return this;
    }

    public UrlPrefixGenerator getPrefixGen() {
        return prefixGen;
    }

    public ServiceRequest setPrefixGen(UrlPrefixGenerator prefixGen) {
        this.prefixGen = prefixGen;
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
            jsonReader = new JsonReaderDefault(coreSettings.getModelRegistry(), version, userPrincipal);
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

    public String getUrlPrefix() {
        return prefixGen.getUrlPrefix(this);
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

    /**
     * Create a new Path with the settings of this request.
     *
     * @param path The path-string to create the path with.
     * @return a new ResourcePath.
     */
    public ResourcePath newPath(String path) {
        return PathParser.parsePath(coreSettings.getModelRegistry(), queryDefaults.getServiceRootUrl(), version, path);
    }

    /**
     * Create a new Query with the settings and user of this request.
     *
     * @return a new Query.
     */
    public Query newQuery() {
        return new Query(coreSettings.getModelRegistry(), coreSettings.getQueryDefaults(), userPrincipal);
    }

    /**
     * Generates the URL Prefix as it should be used for this request.
     */
    public static interface UrlPrefixGenerator {

        /**
         * Generate the prefix for the given request. The result will end with a
         * slash, or be empty. It may include the version number.
         *
         * @param request the request to generate the urlPrefix for.
         * @return the prefix to use for URLs.
         */
        public String getUrlPrefix(ServiceRequest request);
    }
}
