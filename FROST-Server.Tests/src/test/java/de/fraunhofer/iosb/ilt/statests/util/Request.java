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

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
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
        urlString.append('?').append(getQuery().toString(false));
        lastUrl = urlString.toString();
        return lastUrl;
    }

    public JSONObject executeGet() {
        String fetchUrl = buildUrl();
        Map<String, Object> responseMap = HTTPMethods.doGet(fetchUrl);
        String response = responseMap.get("response").toString();
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        if (responseCode != 200) {
            String message = "Error during request: " + fetchUrl;
            Assert.assertEquals(message, 200, responseCode);
        }
        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(response);
        } catch (JSONException ex) {
            LOGGER.error("Failed to parse response for request: " + fetchUrl, ex);
            Assert.fail("Failed to parse response for request: " + fetchUrl);
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
