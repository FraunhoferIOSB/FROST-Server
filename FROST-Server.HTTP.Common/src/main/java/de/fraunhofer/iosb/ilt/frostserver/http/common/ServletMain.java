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
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_TEXT_HTML;

import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
        name = "CoreServlet",
        urlPatterns = {"/*"},
        initParams = {
            @WebInitParam(name = "readonly", value = "false")
        })
@MultipartConfig()
public class ServletMain extends HttpServlet {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletMain.class);
    private static final String NOT_FOUND = "{\"error\":\"Version Not Found\"}";

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String pathInfo = request.getPathInfo();
        final CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        if (StringHelper.isNullOrEmpty(pathInfo)) {
            try {
                response.sendRedirect(coreSettings.getQueryDefaults().getServiceRootUrl() + "/");
                return;
            } catch (IOException ex) {
                sendResponse(Service.errorResponse(null, 500, NOT_FOUND), response);
                return;
            }
        }
        if (pathInfo.equals("/")) {
            try (InputStream in = getServletContext().getResourceAsStream("/index.html")) {
                response.setContentType(CONTENT_TYPE_TEXT_HTML);
                ServletOutputStream out = response.getOutputStream();
                in.transferTo(out);
                return;
            } catch (IOException exc) {
                sendResponse(Service.errorResponse(null, 500, NOT_FOUND), response);
                return;
            }
        }
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        try {
            ServiceRequest serviceRequest = serviceRequestFromHttpRequest(coreSettings, request);
            if (serviceRequest == null) {
                sendResponse(new ServiceResponseHttpServlet(response, 404, NOT_FOUND), response);
                return;
            }
            executeService(serviceRequest, request, response);
        } catch (IllegalArgumentException exc) {
            sendResponse(new ServiceResponseHttpServlet(response, 400, exc.getMessage()), response);
        } catch (IOException exc) {
            sendResponse(new ServiceResponseHttpServlet(response, 500, exc.getMessage()), response);
        }
    }

    private void executeService(final ServiceRequest serviceRequest, HttpServletRequest request, HttpServletResponse response) {
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        PluginService plugin = coreSettings.getPluginManager().getServiceForRequestType(serviceRequest.getVersion(), serviceRequest.getRequestType());
        if (plugin == null) {
            sendResponse(Service.errorResponse(null, 500, "Illegal request type."), response);
            return;
        }
        try (Service service = new Service(coreSettings)) {
            ServiceRequest.setLocalRequest(serviceRequest);
            final ServiceResponseHttpServlet serviceResponse = new ServiceResponseHttpServlet(response);
            plugin.execute(service, serviceRequest, serviceResponse);
            sendResponse(serviceResponse, response);
            ServiceRequest.removeLocalRequest();
        } catch (Exception exc) {
            LOGGER.error("", exc);
            sendResponse(new ServiceResponseHttpServlet(response, 500, exc.getMessage()), response);
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
