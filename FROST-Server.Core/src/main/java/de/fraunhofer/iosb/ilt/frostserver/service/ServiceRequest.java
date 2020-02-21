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

import de.fraunhofer.iosb.ilt.frostserver.settings.Version;

/**
 *
 * @author jab
 */
public class ServiceRequest {

    private String requestType;
    private String urlPath;
    private String urlQuery;
    private String content;
    private Version version;

    protected ServiceRequest() {
        // empty by design.
    }

    public String getRequestType() {
        return requestType;
    }

    public String getContent() {
        return content;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public String getUrlQuery() {
        return urlQuery;
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

    public String getUrl() {
        if (urlQuery == null || urlQuery.isEmpty()) {
            return urlPath;
        }
        return urlPath + "?" + urlQuery;
    }

    public void setRequestType(String requestType) {
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
