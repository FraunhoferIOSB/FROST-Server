/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author jab
 */
public class ServiceRequest {

    public static final ThreadLocal<ServiceRequest> LOCAL_REQUEST = new ThreadLocal<>();

    private String requestType;
    private String urlPath;
    private String urlQuery;
    private String contentString;
    private InputStream contentBinary;
    private Version version;
    private String contentType;
    private Map<String, List<String>> parameterMap;
    private Map<String, Object> attributeMap;
    private PrincipalExtended userPrincipal;
    private CoreSettings coreSettings;

    protected ServiceRequest() {
        // empty by design.
    }

    public Map<String, Object> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    protected void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
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
        return new BufferedReader(new InputStreamReader(contentBinary, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
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

    public void setContent(InputStream content) {
        this.contentBinary = content;
    }

    public void setContent(String content) {
        this.contentString = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, List<String>> getParameterMap() {
        if (parameterMap == null) {
            parameterMap = new HashMap<>();
        }
        return parameterMap;
    }

    public void setParameterMap(Map<String, List<String>> parameterMap) {
        this.parameterMap = parameterMap;
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

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getUrlQuery() {
        return urlQuery;
    }

    public void setUrlQuery(String urlQuery) {
        this.urlQuery = urlQuery;
    }

    public String getUrl() {
        if (urlQuery == null || urlQuery.isEmpty()) {
            return urlPath;
        }
        return urlPath + "?" + urlQuery;
    }

    public final void setUrl(String url) {
        if (url.contains("?")) {
            this.urlPath = url.substring(0, url.lastIndexOf('?'));
            this.urlQuery = url.substring(url.indexOf('?') + 1);
        } else {
            this.urlPath = url;
            this.urlQuery = null;
        }
    }

    public PrincipalExtended getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(PrincipalExtended userPrincipal) {
        this.userPrincipal = userPrincipal;
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
     */
    public void setVersion(Version version) {
        this.version = version;
    }

}
