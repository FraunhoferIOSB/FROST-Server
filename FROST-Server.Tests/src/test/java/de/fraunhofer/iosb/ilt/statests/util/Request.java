/*
 * Copyright 2016 Open Geospatial Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.statests.util;

import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hylke van der Schaaf
 */
public class Request extends Expand {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    private String baseUrl;
    private String lastUrl;

    public Request() {
    }

    public Request(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Request reNest() {
        getQuery().reNestExpands();
        return this;
    }

    @Override
    public boolean isToplevel() {
        return true;
    }

    @Override
    public String toString() {
        if (lastUrl == null) {
            buildUrl();
        }
        return lastUrl;
    }

    public String getLastUrl() {
        return lastUrl;
    }

    public String buildUrl() {
        StringBuilder urlString = new StringBuilder(baseUrl);
        for (PathElement element : getPath()) {
            urlString.append('/').append(element.toString());
        }
        final Query query = getQuery();
        if (!query.isEmpty()) {
            urlString.append('?').append(query.toString(false));
        }
        lastUrl = urlString.toString();
        return lastUrl;
    }

    public JSONObject executeGet() {
        String fetchUrl = buildUrl();
        HttpResponse responseMap = HTTPMethods.doGet(fetchUrl);
        if (responseMap.code != 200) {
            String message = "Error during request: " + fetchUrl;
            assertEquals(200, responseMap.code, message);
        }
        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(responseMap.response);
        } catch (JSONException ex) {
            LOGGER.error("Failed to parse response for request: " + fetchUrl, ex);
            fail("Failed to parse response for request: " + fetchUrl);
        }
        return jsonResponse;
    }

    @Override
    public Request clone() {
        Request clone = (Request) super.clone();
        clone.baseUrl = baseUrl;
        return clone;
    }

}
