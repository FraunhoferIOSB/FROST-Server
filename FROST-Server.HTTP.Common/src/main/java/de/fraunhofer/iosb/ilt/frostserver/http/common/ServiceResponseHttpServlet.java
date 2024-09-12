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

import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service response wrapping a HttpServletResponse, supporting streaming. When
 * using Writer to write data, it is sent directly to the client.
 */
public class ServiceResponseHttpServlet implements ServiceResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceResponseHttpServlet.class.getName());
    private final HttpServletResponse httpResponse;
    private Object result;
    private int code;
    private String message;
    private final Map<String, List<String>> headers = new HashMap<>();

    public ServiceResponseHttpServlet(HttpServletResponse httpResponse, int code, String message) {
        this.httpResponse = httpResponse;
        this.httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        this.code = code;
        this.message = message;
    }

    public ServiceResponseHttpServlet(HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
        this.httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
    }

    @Override
    public String getContentType() {
        return httpResponse.getContentType();
    }

    @Override
    public ServiceResponseHttpServlet setContentType(String contentType) {
        httpResponse.setContentType(contentType);
        return this;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public ServiceResponseHttpServlet addHeader(String name, String value) {
        headers.computeIfAbsent(name, t -> new ArrayList<>()).add(value);
        httpResponse.addHeader(name, value);
        return this;
    }

    @Override
    public ServiceResponseHttpServlet addHeaders(String name, List<String> values) {
        headers.computeIfAbsent(name, t -> new ArrayList<>()).addAll(values);
        for (String value : values) {
            httpResponse.addHeader(name, value);
        }
        return this;
    }

    @Override
    public ServiceResponseHttpServlet setHeader(String name, String value) {
        headers.put(name, Arrays.asList(value));
        httpResponse.setHeader(name, value);
        return this;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public ServiceResponseHttpServlet setCode(int code) {
        this.code = code;
        httpResponse.setStatus(code);
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ServiceResponseHttpServlet setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public ServiceResponseHttpServlet setStatus(int code, String message) {
        setCode(code);
        setMessage(message);
        return this;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public ServiceResponseHttpServlet setResult(Object result) {
        this.result = result;
        return this;
    }

    @Override
    public Writer getWriter() {
        try {
            return httpResponse.getWriter();
        } catch (IOException ex) {
            LOGGER.error("Failed to get Writer", ex);
            return null;
        }
    }

    @Override
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

}
