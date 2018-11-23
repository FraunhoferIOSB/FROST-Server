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

import com.google.common.base.Strings;
import de.fraunhofer.iosb.ilt.frostserver.http.common.multipart.BatchProcessor;
import de.fraunhofer.iosb.ilt.frostserver.http.common.multipart.MixedContent;
import de.fraunhofer.iosb.ilt.sta.service.RequestType;
import de.fraunhofer.iosb.ilt.sta.service.Service;
import de.fraunhofer.iosb.ilt.sta.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.sta.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.sta.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.TAG_CORE_SETTINGS;
import de.fraunhofer.iosb.ilt.sta.util.UrlHelper;
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
        urlPatterns = {"/v1.0", "/v1.0/*"},
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

    private void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding(ENCODING);
        String pathInfo = request.getPathInfo();
        if (Strings.isNullOrEmpty(pathInfo) || pathInfo.equals("/")) {
            executeService(RequestType.GET_CAPABILITIES, request, response);
        } else {
            executeService(RequestType.READ, request, response);
        }
    }

    private void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        String urlPath = request.getPathInfo();
        if (null == urlPath) {
            executeService(RequestType.CREATE, request, response);
        } else {
            switch (urlPath) {
                case "/CreateObservations":
                    executeService(RequestType.CREATE_OBSERVATIONS, request, response);
                    break;

                case "/$batch":
                    processBatchRequest(request, response);
                    break;

                default:
                    executeService(RequestType.CREATE, request, response);
                    break;
            }
        }
    }

    private void processPatchRequest(HttpServletRequest request, HttpServletResponse response) {
        executeService(RequestType.UPDATE_CHANGES, request, response);
    }

    private void processPutRequest(HttpServletRequest request, HttpServletResponse response) {
        executeService(RequestType.UPDATE_ALL, request, response);
    }

    private void processDeleteRequest(HttpServletRequest request, HttpServletResponse response) {
        executeService(RequestType.DELETE, request, response);
    }

    private void processBatchRequest(HttpServletRequest request, HttpServletResponse response) {
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        Service service = new Service(coreSettings);

        MixedContent multipartMixedData = new MixedContent(false);
        multipartMixedData.parse(request);
        MixedContent resultContent = BatchProcessor.processMultipartMixed(service, multipartMixedData);
        sendMixedResponse(resultContent, response);
    }

    private void sendMixedResponse(MixedContent multipartMixedData, HttpServletResponse httpResponse) {
        httpResponse.setStatus(200);
        multipartMixedData.getHeaders().entrySet().forEach(x -> httpResponse.setHeader(x.getKey(), x.getValue()));
        try {
            httpResponse.setCharacterEncoding(ENCODING);
            httpResponse.getWriter().write(multipartMixedData.getContent(false));
        } catch (IOException ex) {
            LOGGER.error("Error writing HTTP result", ex);
            httpResponse.setStatus(500);
        }
    }

    private void executeService(RequestType requestType, HttpServletRequest request, HttpServletResponse response) {
        try {
            CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
            Service service = new Service(coreSettings);
            sendResponse(service.execute(serviceRequestFromHttpRequest(request, requestType)), response);
        } catch (Exception exc) {
            LOGGER.error("", exc);
            sendResponse(new ServiceResponse(500, exc.getMessage()), response);
        }
    }

    private ServiceRequest serviceRequestFromHttpRequest(HttpServletRequest request, RequestType requestType) throws IOException {
        // request.getPathInfo() is decoded, breaking urls that contain //
        // (ids that are urls)
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String fullPath = contextPath + servletPath;
        String pathInfo;
        if (requestURI.startsWith(fullPath)) {
            pathInfo = UrlHelper.urlDecode(requestURI.substring(fullPath.length()));
        } else {
            pathInfo = request.getPathInfo();
        }

        return new ServiceRequestBuilder()
                .withRequestType(requestType)
                .withUrlPath(pathInfo)
                .withUrlQuery(request.getQueryString() != null
                        ? UrlHelper.urlDecode(request.getQueryString())
                        : null)
                .withContent(readRequestData(request.getReader()))
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
                httpResponse.setContentType("application/json");
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
        return "SensorThingsApi v1.0 servlet";
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
