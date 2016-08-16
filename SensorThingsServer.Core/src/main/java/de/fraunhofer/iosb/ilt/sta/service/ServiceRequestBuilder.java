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

import de.fraunhofer.iosb.ilt.sta.formatter.DefaultResultFormater;
import de.fraunhofer.iosb.ilt.sta.formatter.ResultFormatter;
import java.util.Optional;

/**
 *
 * @author jab
 */
public class ServiceRequestBuilder {

    private Optional<RequestType> requestType;
    private Optional<String> url;
    private Optional<String> urlPath;
    private Optional<String> urlQuery;
    private Optional<String> content;
    private ResultFormatter formatter;

    public ServiceRequestBuilder() {
        this.requestType = Optional.empty();
        this.url = Optional.empty();
        this.urlPath = Optional.empty();
        this.urlQuery = Optional.empty();
        this.content = Optional.empty();
        this.formatter = new DefaultResultFormater();
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

    public ServiceRequestBuilder withContent(String content) {
        this.content = Optional.ofNullable(content);
        return this;
    }

    public ServiceRequestBuilder withFormatter(ResultFormatter formatter) {
        this.formatter = formatter;
        return this;
    }

    public ServiceRequestBuilder withRequestType(RequestType requestType) {
        this.requestType = Optional.of(requestType);
        return this;
    }

    public ServiceRequest build() {
        ServiceRequest result = new ServiceRequest();
        if (url.isPresent()) {
            result.setUrl(url.get());
        }
        if (urlPath.isPresent()) {
            result.setUrlPath(urlPath.get());
        }
        if (urlQuery.isPresent()) {
            result.setUrlQuery(urlQuery.get());
        }
        if (content.isPresent()) {
            result.setContent(content.get());
        }
        if (requestType.isPresent()) {
            result.setRequestType(requestType.get());
        }
        result.setFormatter(formatter);
        return result;
    }
}
