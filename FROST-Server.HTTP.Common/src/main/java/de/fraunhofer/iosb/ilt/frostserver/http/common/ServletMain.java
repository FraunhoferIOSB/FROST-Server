/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginService;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORE_SETTINGS;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_TEXT_HTML;
import de.fraunhofer.iosb.ilt.frostserver.util.HttpMethod;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
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
        }
)
@MultipartConfig()
public class ServletMain extends HttpServlet {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletMain.class);
    public static final String ENCODING = "UTF-8";

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding(ENCODING);
        String pathInfo = request.getPathInfo();
        final CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(TAG_CORE_SETTINGS);
        if (StringHelper.isNullOrEmpty(pathInfo)) {
            try {
                response.sendRedirect(coreSettings.getQueryDefaults().getServiceRootUrl() + "/");
                return;
            } catch (IOException ex) {
                sendResponse(Service.errorResponse(null, 500, "Not Found"), response);
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
                sendResponse(Service.errorResponse(null, 500, "Not Found"), response);
                return;
            }
        }
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        try {
            ServiceRequest serviceRequest = serviceRequestFromHttpRequest(coreSettings, request);
            if (serviceRequest == null) {
                sendResponse(new ServiceResponseHttpServlet(response, 404, "Not Found"), response);
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
        try (Service service = new Service(coreSettings)) {
            ServiceRequest.LOCAL_REQUEST.set(serviceRequest);
            final ServiceResponseHttpServlet serviceResponse = new ServiceResponseHttpServlet(response);
            service.execute(serviceRequest, serviceResponse);
            sendResponse(serviceResponse, response);
            ServiceRequest.LOCAL_REQUEST.remove();
        } catch (Exception exc) {
            LOGGER.error("", exc);
            sendResponse(new ServiceResponseHttpServlet(response, 500, exc.getMessage()), response);
        }
    }

    private ServiceRequest serviceRequestFromHttpRequest(CoreSettings coreSettings, HttpServletRequest request) throws IOException {
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(ENCODING);
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
        final String requestType = plugin.getRequestTypeFor(version, path, HttpMethod.fromString(method), request.getContentType());
        if (requestType == null) {
            final String cleanedPath = StringHelper.cleanForLogging(path);
            LOGGER.error("Unhandled request; Method {}, path {}", method, cleanedPath);
            throw new IllegalArgumentException("Unhandled request; Method " + method + ", path " + cleanedPath);
        }

        final ServiceRequestBuilder serviceRequestBuilder = new ServiceRequestBuilder(version)
                .withRequestType(requestType)
                .withUrlPath(path)
                .withUrlQuery(request.getQueryString() != null
                        ? StringHelper.urlDecode(request.getQueryString())
                        : null)
                .withContent(request.getInputStream())
                .withContentType(request.getContentType())
                .withParameterMap(request.getParameterMap())
                .withUserPrincipal(request.getUserPrincipal());

        Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            serviceRequestBuilder.withAttribute(name, request.getAttribute(name));
        }

        return serviceRequestBuilder
                .build();
    }

    private void sendResponse(ServiceResponse serviceResponse, HttpServletResponse httpResponse) {
        try {
            if (!serviceResponse.isSuccessful() && !StringHelper.isNullOrEmpty(serviceResponse.getMessage())) {
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
