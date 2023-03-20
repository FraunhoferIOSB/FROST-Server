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
package de.fraunhofer.iosb.ilt.frostserver.http.common;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.HEADER_ACCEPT;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.HEADER_PREFER;

import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
class HttpRequestDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestDecoder.class.getName());

    private HttpRequestDecoder() {
        // Not for public instantiation
    }

    static ServiceRequest serviceRequestFromHttpRequest(CoreSettings coreSettings, HttpServletRequest request) throws IOException {
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        }
        // request.getPathInfo() is decoded, breaking urls that contain //
        // (ids that are urls)
        final String requestURI = request.getRequestURI();
        final String contextPath = request.getContextPath();
        final String servletPath = request.getServletPath();
        final String fullPath = contextPath + servletPath;
        final String pathInfo;
        if (requestURI.startsWith(fullPath)) {
            pathInfo = StringHelper.urlDecode(requestURI.substring(fullPath.length()));
        } else {
            pathInfo = request.getPathInfo();
        }
        final int idxSlash2 = pathInfo.indexOf('/', 1);
        final String path;
        final Version version;
        if (idxSlash2 > 0) {
            path = pathInfo.substring(idxSlash2);
            version = coreSettings.getPluginManager().getVersion(pathInfo.substring(1, idxSlash2));
        } else {
            path = "";
            version = coreSettings.getPluginManager().getVersion(pathInfo.substring(1));
        }

        if (version == null) {
            return null;
        }

        final PluginService plugin = coreSettings.getPluginManager().getServiceForPath(version, path);
        if (plugin == null) {
            return null;
        }

        final String method = request.getMethod();
        String requestType = decodeRequestType(plugin, version, path, method, request);

        final Map<String, List<String>> parameterMap = UrlHelper.splitQuery(request.getQueryString());
        decodeAccepHeader(request, parameterMap);
        decodePreferHeader(request, parameterMap);

        final ServiceRequestBuilder serviceRequestBuilder = new ServiceRequestBuilder(coreSettings, version)
                .withRequestType(requestType)
                .withUrlPath(path)
                .withUrlQuery(request.getQueryString() != null
                        ? StringHelper.urlDecode(request.getQueryString())
                        : null)
                .withContent(request.getInputStream())
                .withContentType(request.getContentType())
                .withParameterMap(parameterMap)
                .withUserPrincipal(PrincipalExtended.fromPrincipal(request.getUserPrincipal()));

        Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            serviceRequestBuilder.withAttribute(name, request.getAttribute(name));
        }

        ServiceRequest serivceRequest = serviceRequestBuilder.build();

        logServiceRequest(request, serivceRequest);

        return serivceRequest;
    }

    public static void logServiceRequest(HttpServletRequest request, ServiceRequest serivceRequest) {
        if (LOGGER.isDebugEnabled()) {
            final StringBuilder headers = new StringBuilder();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    headers.append("\n - ").append(name).append(" -> ").append(values.nextElement());
                }
            }
            LOGGER.debug("Request: {} {} {}{}", request.getMethod(), request.getRequestURI(), serivceRequest.getUrlQuery(), headers);
        }
    }

    private static String decodeRequestType(final PluginService plugin, final Version version, final String path, final String method, HttpServletRequest request) throws IllegalArgumentException {
        final String requestType = plugin.getRequestTypeFor(version, path, HttpMethod.fromString(method), request.getContentType());
        if (requestType == null) {
            final String cleanedPath = StringHelper.cleanForLogging(path);
            LOGGER.error("Unhandled request; Method {}, path {}", method, cleanedPath);
            throw new IllegalArgumentException("Unhandled request; Method " + method + ", path " + cleanedPath);
        }
        return requestType;
    }

    private static void decodeAccepHeader(HttpServletRequest request, final Map<String, List<String>> parameterMap) {
        String accept = request.getHeader(HEADER_ACCEPT);
        if (accept != null) {
            parameterMap.putIfAbsent(HEADER_ACCEPT, Arrays.asList(accept));
        }
    }

    private static void decodePreferHeader(HttpServletRequest request, final Map<String, List<String>> parameterMap) {
        LinkedHashMap<String, String> prefer = new LinkedHashMap<>();
        for (Enumeration<String> en = request.getHeaders(HEADER_PREFER); en.hasMoreElements();) {
            UrlHelper.decodePrefer(en.nextElement(), prefer);
        }
        for (Map.Entry<String, String> entry : prefer.entrySet()) {
            parameterMap.putIfAbsent(entry.getKey(), Arrays.asList(entry.getValue()));
        }
    }
}
