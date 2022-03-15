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

import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ServiceResponseHttpServlet implements ServiceResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceResponseHttpServlet.class.getName());
    private final HttpServletResponse httpResponse;
    private Object result;
    private int code;
    private String message;
    private final Map<String, String> headers = new HashMap<>();

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
    public ServiceResponse setContentType(String contentType) {
        httpResponse.setContentType(contentType);
        return this;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public ServiceResponse addHeader(String key, String value) {
        headers.put(key, value);
        httpResponse.setHeader(key, value);
        return this;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public ServiceResponse setCode(int code) {
        this.code = code;
        httpResponse.setStatus(code);
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ServiceResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public ServiceResponse setStatus(int code, String message) {
        setCode(code);
        setMessage(message);
        return this;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public ServiceResponse setResult(Object result) {
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
