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
package de.fraunhofer.iosb.ilt.frostserver.plugin.batchprocessing.batch;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Individual request or response inside the bach request.
 */
public abstract class Request implements Content {

    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);
    private static final String VERSION_REGEX = "/v\\d\\.\\d(/|$)";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);
    private static final String SLASH = "/";

    protected String logIndent = "";
    protected HttpMethod method;
    protected String version;
    protected String path;

    /**
     * Batch request user.
     */
    protected Principal userPrincipal;

    protected final Map<String, List<String>> headersOuter = new HashMap<>();
    protected final Map<String, List<String>> headersInner = new HashMap<>();
    /**
     * Flag indicating there is a problem with the syntax of the content. If
     * this is a changeSet, then the entire changeSet will be discarded.
     */
    protected boolean parseFailed = false;
    protected boolean executeFailed = false;
    protected final List<String> errors = new ArrayList<>();

    protected final boolean requireContentId;
    protected String contentId;
    protected PkValue contentIdValue;
    protected EntityType entityType;
    protected final StringBuilder data = new StringBuilder();
    protected final Version batchVersion;

    protected Request(Version batchVersion) {
        this(batchVersion, false);
    }

    protected Request(Version batchVersion, boolean requireContentId) {
        this.batchVersion = batchVersion;
        this.requireContentId = requireContentId;
    }

    @Override
    public boolean isParseFailed() {
        return parseFailed;
    }

    public boolean isExecuteFailed() {
        return executeFailed;
    }

    public void setExecuteFailed(boolean executeFailed) {
        this.executeFailed = executeFailed;
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    /**
     * Get the path part of the http request.
     *
     * @return the URL part of the http request.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the data in the http request. This does not include the outer
     * headers, command, nor inner headers.
     *
     * @return The data in http request.
     */
    public String getData() {
        return data.toString();
    }

    public void addData(String appended) {
        this.data.append(appended);
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public PkValue getContentIdValue() {
        return contentIdValue;
    }

    public void setContentIdValue(PkValue contentIdValue) {
        this.contentIdValue = contentIdValue;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * Get the headers of the request inside the batch. These are not the same
     * as the batch headers.
     *
     * @return the headers of the individual batch request.
     */
    public Map<String, List<String>> getInnerHeaders() {
        return headersInner;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headersOuter;
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void setLogIndent(String logIndent) {
        this.logIndent = logIndent;
    }

    public void updateUsingContentIds(List<ContentIdPair> contentIds) {
        for (ContentIdPair pair : contentIds) {
            path = path.replace(pair.key, pair.keyToUrl());
            int keyIndex = 0;
            String quotedKey = '"' + pair.key + '"';
            String value = UrlHelper.quoteForJson(pair.value.get(0));
            while ((keyIndex = data.indexOf(quotedKey, keyIndex)) != -1) {
                data.replace(keyIndex, keyIndex + quotedKey.length(), value);
                keyIndex += value.length();
            }
        }
        LOGGER.debug("{}Using replaced path and data with content ids {}: {}, {}", logIndent, contentIds, path, data);
    }

    public abstract void setStatus(int code, String text);

    public void parseUrl(String fullUrl) {
        Matcher versionMatcher = VERSION_PATTERN.matcher(fullUrl);
        if (versionMatcher.find()) {
            int versionStart = versionMatcher.start() + 1;
            int versionEnd = versionMatcher.end();
            if (SLASH.equals(versionMatcher.group(1))) {
                version = fullUrl.substring(versionStart, versionEnd - 1);
                path = fullUrl.substring(versionEnd - 1);
            } else {
                version = fullUrl.substring(versionStart, versionEnd);
                path = SLASH;
            }
        } else {
            version = batchVersion.urlPart;
            path = SLASH + fullUrl;
        }
    }

}
