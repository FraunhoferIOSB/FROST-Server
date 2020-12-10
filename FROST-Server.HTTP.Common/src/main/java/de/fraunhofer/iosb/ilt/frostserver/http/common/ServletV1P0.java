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
package de.fraunhofer.iosb.ilt.frostserver.http.common;

import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
@WebServlet(
        name = "STA1.0",
        // This annotation MUST be kept aligned with the constant 
        // de.fraunhofer.iosb.ilt.frostserver.util.Contants.HTTP_URL_PATTERNS!
        urlPatterns = {"/v1.0", "/v1.0/*", "/v1.1", "/v1.1/*"},
        initParams = {
            @WebInitParam(name = "readonly", value = "false")
        }
)
@MultipartConfig()
public class ServletV1P0 extends HttpServlet {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletV1P0.class);
    private static final String ENCODING = "UTF-8";
    public static final String JSON_PATCH_CONTENT_TYPE = "application/json-patch+json";

    private void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding(ENCODING);
        String pathInfo = request.getPathInfo();
        if (StringHelper.isNullOrEmpty(pathInfo) || pathInfo.equals("/")) {
            executeService(RequestTypeUtils.GET_CAPABILITIES, request, response);
        } else {
            CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
            PluginService plugin = coreSettings.getPluginManager().getServiceForPath(pathInfo);
            if (plugin == null) {
                executeService(RequestTypeUtils.READ, request, response);
            } else {
                String requestType = plugin.getRequestTypeFor(pathInfo, HttpMethod.fromString(request.getMethod()));
                executeService(requestType, request, response);
            }

        }
    }

    private void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        if (null == pathInfo) {
            executeService(RequestTypeUtils.CREATE, request, response);
        } else {
            CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
            PluginService plugin = coreSettings.getPluginManager().getServiceForPath(pathInfo);
            if (plugin == null) {
                executeService(RequestTypeUtils.CREATE, request, response);
            } else {
                String requestType = plugin.getRequestTypeFor(pathInfo, HttpMethod.fromString(request.getMethod()));
                executeService(requestType, request, response);
            }
        }
    }

    private void processPatchRequest(HttpServletRequest request, HttpServletResponse response) {
        String[] split = request.getContentType().split(";", 2);
        if (split[0].startsWith(JSON_PATCH_CONTENT_TYPE)) {
            executeService(RequestTypeUtils.UPDATE_CHANGESET, request, response);
        } else {
            executeService(RequestTypeUtils.UPDATE_CHANGES, request, response);
        }
    }

    private void processPutRequest(HttpServletRequest request, HttpServletResponse response) {
        executeService(RequestTypeUtils.UPDATE_ALL, request, response);
    }

    private void processDeleteRequest(HttpServletRequest request, HttpServletResponse response) {
        executeService(RequestTypeUtils.DELETE, request, response);
    }

    private void executeService(String requestType, HttpServletRequest request, HttpServletResponse response) {
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        try (Service service = new Service(coreSettings)) {
            sendResponse(service.execute(serviceRequestFromHttpRequest(request, requestType)), response);
        } catch (Exception exc) {
            LOGGER.error("", exc);
            sendResponse(new ServiceResponse<>(500, exc.getMessage()), response);
        }
    }

    private ServiceRequest serviceRequestFromHttpRequest(HttpServletRequest request, String requestType) throws IOException {
        if(request.getCharacterEncoding() == null) {
            request.setCharacterEncoding("UTF-8");
        }
        // request.getPathInfo() is decoded, breaking urls that contain //
        // (ids that are urls)
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String fullPath = contextPath + servletPath;
        String pathInfo;
        if (requestURI.startsWith(fullPath)) {
            pathInfo = StringHelper.urlDecode(requestURI.substring(fullPath.length()));
        } else {
            pathInfo = request.getPathInfo();
        }

        // ServletPath is /vx.x
        Version version = Version.forString(servletPath.substring(1));
        return new ServiceRequestBuilder(version)
                .withRequestType(requestType)
                .withUrlPath(pathInfo)
                .withUrlQuery(request.getQueryString() != null
                        ? StringHelper.urlDecode(request.getQueryString())
                        : null)
                .withContent(readRequestData(request.getReader()))
                .withContentType(request.getContentType())
                .withParameterMap(request.getParameterMap())
                .build();
    }

    private void sendResponse(ServiceResponse<?> serviceResponse, HttpServletResponse httpResponse) {
        httpResponse.setStatus(serviceResponse.getCode());
        serviceResponse.getHeaders().entrySet().forEach(x -> httpResponse.setHeader(x.getKey(), x.getValue()));
        try {
            if (serviceResponse.getCode() >= 200
                    && serviceResponse.getCode() < 300
                    && serviceResponse.getResultFormatted() != null
                    && !serviceResponse.getResultFormatted().isEmpty()) {
                httpResponse.setContentType(serviceResponse.getContentType());
                httpResponse.setCharacterEncoding(ENCODING);
                httpResponse.getWriter().write(serviceResponse.getResultFormatted());

            } else if (serviceResponse.getMessage() != null
                    && !serviceResponse.getMessage().isEmpty()) {
                httpResponse.getWriter().write(serviceResponse.getMessage());
            }
        } catch (IOException ex) {
            LOGGER.error("Error writing HTTP result", ex);
            httpResponse.setStatus(500);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processPostRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        processPutRequest(request, response);
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) {
        processPatchRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        processDeleteRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "SensorThingsApi v1.0 and v1.1 servlet";
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            doPatch(request, response);
            return;
        }
        super.service(request, response);
    }

    private String readRequestData(BufferedReader reader) {
        return reader.lines().collect(Collectors.joining("\n"));
    }

}
