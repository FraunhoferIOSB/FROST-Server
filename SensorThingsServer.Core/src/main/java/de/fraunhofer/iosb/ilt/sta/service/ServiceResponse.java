/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.service;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jab
 */
public class ServiceResponse<T> {

    private T result;
    private String resultFormatted;
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

    public T getResult() {
        return result;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setResult(T result) {
        this.result = result;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ServiceResponse setStatus(int code, String message) {
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
