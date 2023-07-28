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
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author jab
 */
public class ServiceRequestBuilder {

    private Optional<String> requestType;
    private Optional<String> url;
    private Optional<String> urlPath;
    private Optional<String> urlQuery;
    private Optional<String> contentString;
    private Optional<InputStream> contentBinary;
    private Optional<String> contentType;
    private Optional<Map<String, List<String>>> parameterMap;
    private final Map<String, Object> attributeMap = new HashMap<>();
    private final CoreSettings coreSettings;
    private final Version version;
    private Principal userPrincipal;

    public ServiceRequestBuilder(CoreSettings coreSettings, Version version) {
        this.coreSettings = coreSettings;
        this.version = version;
        this.requestType = Optional.empty();
        this.url = Optional.empty();
        this.urlPath = Optional.empty();
        this.urlQuery = Optional.empty();
        this.contentBinary = Optional.empty();
        this.contentType = Optional.empty();
        this.parameterMap = Optional.empty();
    }

    public ServiceRequestBuilder withUrl(String url) {
        this.url = Optional.ofNullable(url);
        return this;
    }

    public ServiceRequestBuilder withUrlPath(String urlPath) {
        this.urlPath = Optional.ofNullable(urlPath);
        return this;
    }

    public ServiceRequestBuilder withUrlQuery(String urlQuery) {
        this.urlQuery = Optional.ofNullable(urlQuery);
        return this;
    }

    public ServiceRequestBuilder withContent(InputStream content) {
        this.contentBinary = Optional.ofNullable(content);
        return this;
    }

    public ServiceRequestBuilder withContent(String content) {
        this.contentString = Optional.ofNullable(content);
        return this;
    }

    public ServiceRequestBuilder withContentType(String contentType) {
        this.contentType = Optional.ofNullable(contentType);
        return this;
    }

    public ServiceRequestBuilder withParameterMap(Map<String, List<String>> parameterMap) {
        this.parameterMap = Optional.ofNullable(parameterMap);
        return this;
    }

    public ServiceRequestBuilder withRequestType(String requestType) {
        this.requestType = Optional.of(requestType);
        return this;
    }

    public ServiceRequestBuilder withAttribute(String key, Object value) {
        attributeMap.put(key, value);
        return this;
    }

    public ServiceRequestBuilder withUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
        return this;
    }

    public ServiceRequest build() {
        ServiceRequest result = new ServiceRequest();
        result.setVersion(version);
        result.setCoreSettings(coreSettings);
        if (url.isPresent()) {
            result.setUrl(url.get());
        }
        if (urlPath.isPresent()) {
            result.setUrlPath(urlPath.get());
        }
        if (urlQuery.isPresent()) {
            result.setUrlQuery(urlQuery.get());
        }
        if (contentBinary.isPresent()) {
            result.setContent(contentBinary.get());
        } else if (contentString.isPresent()) {
            result.setContent(contentString.get());
        }
        if (contentType.isPresent()) {
            result.setContentType(contentType.get());
        }
        if (parameterMap.isPresent()) {
            result.setParameterMap(parameterMap.get());
        }
        if (requestType.isPresent()) {
            result.setRequestType(requestType.get());
        }
        result.setAttributeMap(attributeMap);
        result.setUserPrincipal(userPrincipal);
        return result;
    }
}
