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
package de.fraunhofer.iosb.ilt.sta;

import com.google.common.base.Strings;
import de.fraunhofer.iosb.ilt.sta.multipart.BatchProcessor;
import de.fraunhofer.iosb.ilt.sta.multipart.MixedContent;
import de.fraunhofer.iosb.ilt.sta.service.RequestType;
import de.fraunhofer.iosb.ilt.sta.service.Service;
import de.fraunhofer.iosb.ilt.sta.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.sta.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.sta.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
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
public class Servlet_1_0 extends HttpServlet {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Servlet_1_0.class);
    private static final Charset ENCODING = Charset.forName("UTF-8");

    private void processGetRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        if (Strings.isNullOrEmpty(pathInfo) || pathInfo.equals("/")) {
            executeService(RequestType.GetCapabilities, request, response);
        } else {
            executeService(RequestType.Read, request, response);
        }
    }

    private void processPostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String urlPath = request.getPathInfo();
        if (null == urlPath) {
            executeService(RequestType.Create, request, response);
        } else {
            switch (urlPath) {
                case "/CreateObservations":
                    executeService(RequestType.CreateObservations, request, response);
                    break;

                case "/$batch":
                    processBatchRequest(request, response);
                    break;

                default:
                    executeService(RequestType.Create, request, response);
                    break;
            }
        }
    }

    private void processPatchRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        executeService(RequestType.UpdateChanges, request, response);
    }

    private void processPutRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        executeService(RequestType.UpdateAll, request, response);
    }

    private void processDeleteRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        executeService(RequestType.Delete, request, response);
    }

    private void processBatchRequest(HttpServletRequest request, HttpServletResponse response) {
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(ContextListener.TAG_CORE_SETTINGS);
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
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(multipartMixedData.getContent(false));
        } catch (IOException ex) {
            LOGGER.error("Error writing HTTP result", ex);
            httpResponse.setStatus(500);
        }
    }

    private void executeService(RequestType requestType, HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, IOException {
        try {
            CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(ContextListener.TAG_CORE_SETTINGS);
            Service service = new Service(coreSettings);
            sendResponse(service.execute(serviceRequestFromHttpRequest(request, requestType)), response);
        } catch (Exception e) {
            LOGGER.error("", e);
            sendResponse(new ServiceResponse(500, e.getMessage()), response);
        }
    }

    private ServiceRequest serviceRequestFromHttpRequest(HttpServletRequest request, RequestType requestType) throws UnsupportedEncodingException, IOException {
        return new ServiceRequestBuilder()
                .withRequestType(requestType)
                .withUrlPath(request.getPathInfo() != null
                        ? URLDecoder.decode(request.getPathInfo(), ENCODING.name())
                        : null)
                .withUrlQuery(request.getQueryString() != null
                        ? URLDecoder.decode(request.getQueryString(), ENCODING.name())
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
                httpResponse.setCharacterEncoding("UTF-8");
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processGetRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processPostRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processPutRequest(request, response);
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processPatchRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    private String readRequestData(BufferedReader reader) throws IOException {
        return reader.lines().collect(Collectors.joining("\n"));
    }
}
