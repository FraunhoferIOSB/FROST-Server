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

import de.fraunhofer.iosb.ilt.sta.formatter.ResultFormatter;

/**
 *
 * @author jab
 */
public class ServiceRequest {

    private RequestType requestType;
    private String urlPath;
    private String urlQuery;
    private String content;
    private ResultFormatter formatter;

    protected ServiceRequest() {

    }

    public ServiceRequest(RequestType requestType, String url, ResultFormatter formatter, String content) {
        this.requestType = requestType;
        this.content = content;
        this.formatter = formatter;
        setUrl(url);
    }

    public ServiceRequest(RequestType requestType, String urlPath, String urlQuery, ResultFormatter formatter, String content) {
        this.requestType = requestType;
        this.urlPath = urlPath;
        this.urlQuery = urlQuery;
        this.content = content;
        this.formatter = formatter;
    }

    public ServiceRequest(RequestType requestType, String url, ResultFormatter formatter) {
        this.requestType = requestType;
        this.content = null;
        this.formatter = formatter;
        setUrl(url);
    }

    public ServiceRequest(RequestType requestType, String urlPath, String urlQuery, ResultFormatter formatter) {
        this.requestType = requestType;
        this.urlPath = urlPath;
        this.urlQuery = urlQuery;
        this.formatter = formatter;
        this.content = null;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getContent() {
        return content;
    }

    public ResultFormatter getFormatter() {
        return formatter;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public String getUrlQuery() {
        return urlQuery;
    }

    public void setUrl(String url) {
        if (url.contains("?")) {
            this.urlPath = url.substring(0, url.lastIndexOf('?'));
            this.urlQuery = url.substring(url.indexOf('?') + 1);
        } else {
            this.urlPath = url;
            this.urlQuery = null;
        }
    }

    public String getUrl() {
        if (urlQuery == null || urlQuery.isEmpty()) {
            return urlPath;
        }
        return urlPath + "?" + urlQuery;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public void setUrlQuery(String urlQuery) {
        this.urlQuery = urlQuery;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFormatter(ResultFormatter formatter) {
        this.formatter = formatter;
    }
}
