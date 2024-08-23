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
package de.fraunhofer.iosb.ilt.frostserver.http.common;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.HEADER_ACCEPT;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.HEADER_PREFER;

import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigProvider;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
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
 * Decodes HttpServletRequest requests from Tomcat into ServiceRequest requests
 * for the FROST Service.
 */
public class HttpRequestDecoder extends ConfigProvider<HttpRequestDecoder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestDecoder.class.getName());

    @DefaultValueBoolean(false)
    public static final String TAG_AUTODETECT_ROOT_URL = "autodetectRootUrl";
    @DefaultValueBoolean(false)
    public static final String TAG_USE_X_HEADERS = "useXForwardedHeaders";
    @DefaultValue("X-Forwarded-Host")
    public static final String TAG_HEADER_XF_HOST = "XForwardedHostHeader";
    @DefaultValue("X-Forwarded-Path")
    public static final String TAG_HEADER_XF_PATH = "XForwardedPathHeader";
    @DefaultValue("X-Forwarded-Port")
    public static final String TAG_HEADER_XF_PORT = "XForwardedPortHeader";
    @DefaultValue("X-Forwarded-Proto")
    public static final String TAG_HEADER_XF_PROTO = "XForwardedProtoHeader";

    private final CoreSettings coreSettings;
    private final boolean autodetectRootUrl;
    private final boolean useXHeaders;
    private String headerXfHost;
    private String headerXfPath;
    private String headerXfPort;
    private String headerXfProto;

    private HttpRequestDecoder(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
        setSettings(coreSettings.getHttpSettings().getSubSettings("requestDecoder."));
        String serviceRootUrl = coreSettings.getQueryDefaults().getServiceRootUrl();
        boolean myAutodetectRootUrl = getBoolean(TAG_AUTODETECT_ROOT_URL);
        if (!myAutodetectRootUrl && StringHelper.isNullOrEmpty(serviceRootUrl)) {
            LOGGER.warn("serviceRootUrl is not set and autodetectRootUrl is false! Setting autodetectRootUrl to true!");
            autodetectRootUrl = true;
        } else {
            autodetectRootUrl = myAutodetectRootUrl;
        }
        useXHeaders = getBoolean(TAG_USE_X_HEADERS);
        if (useXHeaders) {
            headerXfHost = get(TAG_HEADER_XF_HOST);
            headerXfPath = get(TAG_HEADER_XF_PATH);
            headerXfPort = get(TAG_HEADER_XF_PORT);
            headerXfProto = get(TAG_HEADER_XF_PROTO);
        }
    }

    public ServiceRequest serviceRequestFromHttpRequest(HttpServletRequest request) throws IOException {
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        }
        // request.getPathInfo() is decoded, breaking urls that contain //
        // (ids that are urls)
        final String requestURI = request.getRequestURI();
        final String contextPath = request.getContextPath();
        final String servletPath = request.getServletPath();
        final String basePath = contextPath + servletPath;
        final String pathInfo;
        if (requestURI.startsWith(basePath)) {
            pathInfo = StringHelper.urlDecode(requestURI.substring(basePath.length()));
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

        QueryDefaults queryDefaults = coreSettings.getQueryDefaults().copy();
        if (autodetectRootUrl) {
            String detectedRootUrl = generateRootUrl(request, version, basePath);
            LOGGER.debug("Detected serviceRootURL: {}", detectedRootUrl);
            queryDefaults.setServiceRootUrl(detectedRootUrl);
        }

        final PluginService plugin = coreSettings.getPluginManager().getServiceForPath(version, path);
        if (plugin == null) {
            return null;
        }

        final String method = request.getMethod();
        String requestType = PluginManager.decodeRequestType(plugin, version, path, method, request.getContentType());

        final Map<String, List<String>> parameterMap = UrlHelper.splitQuery(request.getQueryString());
        decodeAccepHeader(request, parameterMap);
        decodePreferHeader(request, parameterMap);

        final ServiceRequest serviceRequest = new ServiceRequest()
                .setCoreSettings(coreSettings)
                .setQueryDefaults(queryDefaults)
                .setVersion(version)
                .setRequestType(requestType)
                .setUrlPath(path)
                .setUrlQuery(request.getQueryString() != null
                        ? StringHelper.urlDecode(request.getQueryString())
                        : null)
                .setContent(request.getInputStream())
                .setContentType(request.getContentType())
                .setParameterMap(parameterMap)
                .setUpdateMode(RequestTypeUtils.CREATE.equals(requestType) ? UpdateMode.INSERT_STA_11 : UpdateMode.UPDATE_STA_11)
                .setUserPrincipal(PrincipalExtended.fromPrincipal(request.getUserPrincipal()));

        Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            serviceRequest.setAttribute(name, request.getAttribute(name));
        }

        logServiceRequest(request, serviceRequest);

        return serviceRequest;
    }

    private String generateRootUrl(HttpServletRequest request, Version version, String reqBasePath) {
        if (useXHeaders) {
            String xfHost = request.getHeader(headerXfHost);
            String xfProto = request.getHeader(headerXfProto);
            String xfPort = request.getHeader(headerXfPort);
            String xfPath = request.getHeader(headerXfPath);
            String basePath;
            if (!StringHelper.isNullOrEmpty(xfPath)) {
                basePath = xfPath;
            } else {
                basePath = reqBasePath;
            }
            int versionIdx = xfPath.indexOf(version.urlPart);
            if (versionIdx > 0) {
                basePath = xfPath.substring(0, versionIdx - 1);
            }
            return xfProto + "://" + xfHost + ":" + xfPort + basePath;
        } else {
            final StringBuffer requestURL = request.getRequestURL();
            int versionIdx = requestURL.indexOf(version.urlPart);
            return requestURL.substring(0, versionIdx - 1);
        }
    }

    @Override
    public HttpRequestDecoder getThis() {
        return this;
    }

    static ServiceRequest serviceRequestFromHttpRequest(CoreSettings coreSettings, HttpServletRequest request) throws IOException {
        final Object requestDecoder = coreSettings.getRequestDecoder();
        if (requestDecoder instanceof HttpRequestDecoder hrd) {
            return hrd.serviceRequestFromHttpRequest(request);
        }
        LOGGER.info("Creating new HttpRequestDecoder");
        HttpRequestDecoder hrd = new HttpRequestDecoder(coreSettings);
        coreSettings.setRequestDecoder(hrd);
        return hrd.serviceRequestFromHttpRequest(request);
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
