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

import static de.fraunhofer.iosb.ilt.frostserver.http.common.HttpRequestDecoder.serviceRequestFromHttpRequest;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;

import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.snapshots.Unit;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main FROST Servlet.
 */
@WebServlet(
        name = "CoreServlet",
        urlPatterns = {"/"},
        initParams = {
            @WebInitParam(name = "readonly", value = "false")
        })
@MultipartConfig()
public class ServletMain extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletMain.class);
    private static final String NOT_FOUND = "{\"error\":\"Version Not Found\"}";

    private static final Histogram REQUEST_DURATION = Histogram.builder()
            .name("http_request_duration_seconds")
            .help("HTTP request service time in seconds")
            .unit(Unit.SECONDS)
            .labelNames("method")
            .register();
    private static final Counter RESPONSE_CODE = Counter.builder()
            .name("http_request_response_code_total")
            .help("HTTP request response counts per response code")
            .labelNames("responseCode")
            .register();

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        try (Timer timer = REQUEST_DURATION.labelValues(request.getMethod()).startTimer()) {
            executeRequest(request, response);
        }
        RESPONSE_CODE.labelValues(Integer.toString(response.getStatus())).inc();
    }

    private void executeRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String servletPath = request.getServletPath();
        final CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        if (StringHelper.isNullOrEmpty(servletPath)) {
            try {
                response.sendRedirect(coreSettings.getQueryDefaults().getServiceRootUrl() + "/");
                return;
            } catch (IOException ex) {
                sendResponse(Service.errorResponse(null, 500, NOT_FOUND), response);
                return;
            }
        }
        if (servletPath.equals("/")) {
            return;
        }
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        try {
            ServiceRequest serviceRequest = serviceRequestFromHttpRequest(coreSettings, request);
            if (serviceRequest == null) {
                sendResponse(new ServiceResponseHttpServlet(response, 404, NOT_FOUND), response);
                return;
            }
            executeService(coreSettings, serviceRequest, response);
        } catch (IllegalArgumentException exc) {
            sendResponse(new ServiceResponseHttpServlet(response, 400, exc.getMessage()), response);
        } catch (IOException exc) {
            LOGGER.info("Failed to execute request: {}", exc.getMessage());
            LOGGER.debug("Exception:", exc);
            sendResponse(new ServiceResponseHttpServlet(response, 500, exc.getMessage()), response);
        }
    }

    private void executeService(CoreSettings coreSettings, ServiceRequest serviceRequest, HttpServletResponse response) {
        try (Service service = new Service(coreSettings)) {
            ServiceRequest.setLocalRequest(serviceRequest);
            final ServiceResponseHttpServlet serviceResponse = new ServiceResponseHttpServlet(response);
            service.distributeRequest(serviceRequest, serviceResponse);
            sendResponse(serviceResponse, response);
        } catch (Exception exc) {
            LOGGER.error("", exc);
            sendResponse(new ServiceResponseHttpServlet(response, 500, exc.getMessage()), response);
        } finally {
            ServiceRequest.removeLocalRequest();
        }
    }

    private void sendResponse(ServiceResponse serviceResponse, HttpServletResponse httpResponse) {
        try {
            if (!serviceResponse.isSuccessful() && !StringHelper.isNullOrEmpty(serviceResponse.getMessage())) {
                httpResponse.setStatus(serviceResponse.getCode());
                httpResponse.getWriter().write(serviceResponse.getMessage());
            }
        } catch (IOException ex) {
            LOGGER.error("Error writing HTTP result", ex);
            httpResponse.setStatus(500);
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
        try {
            super.doHead(req, resp);
        } catch (ServletException | IOException ex) {
            LOGGER.error("Exception while calculating HEAD.", ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "FROST-Server Main Servlet";
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            doPatch(request, response);
            return;
        }
        super.service(request, response);
    }

}
