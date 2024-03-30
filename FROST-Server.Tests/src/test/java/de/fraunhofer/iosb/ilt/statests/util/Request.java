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
package de.fraunhofer.iosb.ilt.statests.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.model.Expand;
import de.fraunhofer.iosb.ilt.statests.util.model.PathElement;
import de.fraunhofer.iosb.ilt.statests.util.model.Query;
import java.io.IOException;
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

    public JsonNode executeGet() {
        String fetchUrl = buildUrl();
        HttpResponse responseMap = HTTPMethods.doGet(fetchUrl);
        if (responseMap.code != 200) {
            String message = "Error during request: " + fetchUrl;
            assertEquals(200, responseMap.code, message);
        }
        JsonNode jsonResponse = null;
        try {
            jsonResponse = Utils.MAPPER.readTree(responseMap.response);
        } catch (IOException ex) {
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
