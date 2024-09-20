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
package de.fraunhofer.iosb.ilt.frostserver.util;

/**
 * A list of string constants.
 */
public class Constants {

    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_TYPE_MULTIPART_MIXED = "multipart/mixed";
    public static final String CONTENT_TYPE_APPLICATION_GEOJSON = "application/geo+json";
    public static final String CONTENT_TYPE_APPLICATION_HTTP = "application/http";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_APPLICATION_JSONPATCH = "application/json-patch+json";
    public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
    public static final String CONTENT_TYPE_JSON = "json";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    public static final String CHARSET_UTF8 = "charset=UTF-8";

    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_LOCATION = "Location";
    public static final String HEADER_PREFER = "Prefer";

    public static final String TAG_PREFER_RETURN = "return";

    public static final String REQUEST_PARAM_FORMAT = "$format";

    public static final String VALUE_ID_TYPE_LONG = "LONG";
    public static final String VALUE_ID_TYPE_STRING = "STRING";
    public static final String VALUE_ID_TYPE_UUID = "UUID";
    public static final String VALUE_RETURN_REPRESENTATION = "representation";
    public static final String VALUE_RETURN_MINIMAL = "minimal";

    public static final String NOT_IMPLEMENTED_MULTI_VALUE_PK = "Multi-valued primary keys are not implemented yet.";

    private Constants() {
        // Utility class, not to be instantiated.
    }

}
