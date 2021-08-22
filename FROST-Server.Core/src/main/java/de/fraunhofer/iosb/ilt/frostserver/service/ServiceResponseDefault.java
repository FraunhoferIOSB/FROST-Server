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
package de.fraunhofer.iosb.ilt.frostserver.service;

import de.fraunhofer.iosb.ilt.frostserver.formatter.FormatWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * A default service response that puts the results in a StringWriter.
 *
 * @author jab, scf
 */
public class ServiceResponseDefault implements ServiceResponse {

    /**
     * The non-formatted result.
     */
    private Object result;
    /**
     * The formatted result.
     */
    private StringWriter resultFormatted = new StringWriter();
    /**
     * The content type of the formatted result.
     */
    private String contentType;
    private int code;
    private String message;
    private final Map<String, String> headers;

    public ServiceResponseDefault() {
        this.headers = new HashMap<>();
    }

    public ServiceResponseDefault(int code, String message, Object result, FormatWriter resultFormatted) {
        this.headers = new HashMap<>();
        this.result = result;
        this.code = code;
        this.message = message;
    }

    public ServiceResponseDefault(int code, String message) {
        this.headers = new HashMap<>();
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public ServiceResponseDefault setCode(int code) {
        this.code = code;
        return this;
    }

    /**
     * The content type of the formatted result.
     *
     * @return the contentType
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * The content type of the formatted result.
     *
     * @param contentType the contentType to set
     */
    @Override
    public ServiceResponseDefault setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public ServiceResponseDefault setResult(Object result) {
        this.result = result;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ServiceResponseDefault setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public ServiceResponseDefault addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    @Override
    public ServiceResponse setStatus(int code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    @Override
    public StringWriter getWriter() {
        return resultFormatted;
    }

    @Override
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

}
