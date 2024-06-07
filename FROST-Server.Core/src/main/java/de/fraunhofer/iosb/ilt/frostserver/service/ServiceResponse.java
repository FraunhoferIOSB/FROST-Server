/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import java.io.Writer;
import java.util.List;
import java.util.Map;

public interface ServiceResponse {

    /**
     * The content type of the formatted result.
     *
     * @return the contentType
     */
    public String getContentType();

    /**
     * The content type of the formatted result.
     *
     * @param contentType the contentType to set
     * @return this
     */
    public ServiceResponse setContentType(String contentType);

    public Map<String, List<String>> getHeaders();

    public ServiceResponse addHeader(String key, String value);

    /**
     * Adds all values to the given header.
     *
     * @param name The name of the header to set.
     * @param values The values to set the header to.
     * @return this-
     */
    public ServiceResponse addHeaders(String name, List<String> values);

    /**
     * Sets the header with the given key to the given value. Overwrites any
     * existing header(s) with the given key.
     *
     * @param name The name of the header to set.
     * @param value The value to set the header to.
     * @return this.
     */
    public ServiceResponse setHeader(String name, String value);

    public int getCode();

    public ServiceResponse setCode(int code);

    public String getMessage();

    public ServiceResponse setMessage(String message);

    public ServiceResponse setStatus(int code, String message);

    public Object getResult();

    public ServiceResponse setResult(Object result);

    /**
     * Get the writer that the formatted result should be written to.
     *
     * @return the writer that the formatted result should be written to.
     */
    public Writer getWriter();

    public boolean isSuccessful();

}
