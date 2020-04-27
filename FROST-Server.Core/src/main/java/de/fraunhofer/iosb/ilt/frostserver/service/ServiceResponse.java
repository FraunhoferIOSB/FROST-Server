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
package de.fraunhofer.iosb.ilt.frostserver.service;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jab, scf
 * @param <T> The type of the non-formatted result object.
 */
public class ServiceResponse<T> {

    /**
     * The non-formatted result.
     */
    private T result;
    /**
     * The formatted result.
     */
    private String resultFormatted;
    /**
     * The content type of the formatted result.
     */
    private String contentType;
    private int code;
    private String message;
    private final Map<String, String> headers;

    public ServiceResponse() {
        this.headers = new HashMap<>();
    }

    public ServiceResponse(int code, String message, T result, String resultFormatted) {
        this.headers = new HashMap<>();
        this.result = result;
        this.code = code;
        this.message = message;
        this.resultFormatted = resultFormatted;
    }

    public ServiceResponse(int code, String message) {
        this.headers = new HashMap<>();
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * The content type of the formatted result.
     *
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * The content type of the formatted result.
     *
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public ServiceResponse<T> setStatus(int code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    public String getResultFormatted() {
        return resultFormatted;
    }

    public void setResultFormatted(String resultFormatted) {
        this.resultFormatted = resultFormatted;
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

}
