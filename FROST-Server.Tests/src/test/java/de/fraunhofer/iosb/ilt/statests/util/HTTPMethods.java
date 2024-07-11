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
package de.fraunhofer.iosb.ilt.statests.util;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.CONTENT_TYPE_APPLICATION_JSONPATCH;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.apache.http.Consts;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sending HTTP Methods: GET, POST, PUT, PATCH, and DELETE
 */
public class HTTPMethods {

    public static final ContentType APPLICATION_JSON_PATCH = ContentType.create(CONTENT_TYPE_APPLICATION_JSONPATCH, Consts.UTF_8);
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPMethods.class);

    private static int countDelete = 0;
    private static int countGet = 0;
    private static int countPost = 0;
    private static int countPatch = 0;
    private static int countPut = 0;

    public static class HttpResponse {

        public final int code;
        public String response;

        public HttpResponse(int code) {
            this.code = code;
        }

        public HttpResponse(int code, String response) {
            this.code = code;
            this.response = response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        @Override
        public String toString() {
            return "HttpResponse: " + code + " " + response;
        }
    }

    public static void resetStats() {
        countDelete = 0;
        countGet = 0;
        countPatch = 0;
        countPost = 0;
        countPut = 0;
    }

    public static void logStats() {
        LOGGER.info("Calls: {} Get, {} Post, {} Patch, {} Put, {} Delete", countGet, countPost, countPatch, countPut, countDelete);
    }

    /**
     * Send HTTP GET request to the urlString and return response code and
     * response body
     *
     * @param urlString The URL that the GET request should be sent to
     * @return response-code and response(response body) of the HTTP GET in the
     * MAP format. If the response is not 200, the response(response body) will
     * be empty.
     */
    public static HttpResponse doGet(String urlString) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return doGet(httpClient, urlString);
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
        }
        return null;
    }

    /**
     * Send HTTP GET request to the urlString, using the given HttpClient and
     * return response code and response body
     *
     * @param httpClient the HttpClient to use.
     * @param urlString The URL that the GET request should be sent to
     * @return response-code and response(response body) of the HTTP GET in the
     * MAP format. If the response is not 200, the response(response body) will
     * be empty.
     * @throws ParseException
     * @throws IOException
     */
    public static HttpResponse doGet(final CloseableHttpClient httpClient, String urlString) throws ParseException, IOException {
        LOGGER.debug("Getting: {}", urlString);
        countGet++;
        HttpGet request = new HttpGet(urlString);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpResponse result = new HttpResponse(response.getStatusLine().getStatusCode());
            if (result.code == 200) {
                result.setResponse(EntityUtils.toString(response.getEntity()));
            } else {
                result.setResponse("");
            }
            return result;
        }
    }

    public static HttpResponse doGet(final SensorThingsService service, String urlString) throws ParseException, IOException {
        LOGGER.debug("Getting: {}", urlString);
        countGet++;
        HttpGet request = new HttpGet(urlString);
        try (CloseableHttpResponse response = service.execute(request)) {
            HttpResponse result = new HttpResponse(response.getStatusLine().getStatusCode());
            if (result.code == 200) {
                result.setResponse(EntityUtils.toString(response.getEntity()));
            } else {
                result.setResponse("");
            }
            return result;
        }
    }

    private static String responseToString(HttpURLConnection connection) throws IOException {
        final InputStream is = connection.getInputStream();
        if (is.available() == 0) {
            return "";
        }
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private static String errorToString(HttpURLConnection connection) throws IOException {
        final InputStream es = connection.getErrorStream();
        if (es.available() == 0) {
            return "";
        }
        try (Scanner scanner = new Scanner(es, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    /**
     * Send HTTP POST request to the urlString with postBody and return response
     * code and response body, with application/json content type
     *
     * @param urlString The URL that the POST request should be sent to
     * @param postBody The body of the POST request
     * @return response-code and response of the HTTP POST in the MAP format. If
     * the response is 201, the response will contain the self-link to the
     * created entity. Otherwise, it will be empty String.
     */
    public static HttpResponse doPost(String urlString, String postBody) {
        return doPost(urlString, postBody, CONTENT_TYPE_APPLICATION_JSON);
    }

    /**
     * Send HTTP POST request to the urlString with postBody and return response
     * code and response body
     *
     * @param urlString The URL that the POST request should be sent to
     * @param postBody The body of the POST request
     * @param contentType The POST request content type header value
     * @return response-code and response of the HTTP POST in the MAP format. If
     * the response is 201, the response will contain the self-link to the
     * created entity from location header if present, or the response string.
     * If response is 200, it will be the HTTP response body String.
     */
    public static HttpResponse doPost(String urlString, String postBody, String contentType) {
        HttpURLConnection connection = null;
        try {
            LOGGER.debug("Posting: {}", urlString);
            countPost++;
            //Create connection
            URL url = new URI(urlString).toURL();
            byte[] postData = postBody.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            connection.setUseCaches(false);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            HttpResponse result = new HttpResponse(connection.getResponseCode());
            switch (connection.getResponseCode()) {
                case 201:
                    String locationHeader = connection.getHeaderField("location");
                    if (locationHeader == null || locationHeader.isEmpty()) {
                        result.setResponse(responseToString(connection));
                    } else {
                        result.setResponse(locationHeader);
                    }
                    break;
                case 200:
                    result.setResponse(responseToString(connection));
                    break;
                case 400:
                case 500:
                    result.setResponse(errorToString(connection));
                    break;
                default:
                    result.setResponse("");
                    break;
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static HttpResponse doPost(SensorThingsService service, String urlString, String postBody, String contentType) {
        try {
            LOGGER.debug("Posting: {}", urlString);
            countPost++;

            HttpPost httpPost = new HttpPost(urlString);
            httpPost.setEntity(new StringEntity(postBody));
            httpPost.setHeader("Content-Type", contentType);
            httpPost.setHeader("charset", "utf-8");

            org.apache.http.HttpResponse httpResponse = service.execute(httpPost);
            final int statusCode = httpResponse.getStatusLine().getStatusCode();

            HttpResponse result = new HttpResponse(statusCode);
            switch (statusCode) {
                case 201:
                    String locationHeader = httpResponse.getFirstHeader("location").getValue();
                    if (locationHeader == null || locationHeader.isEmpty()) {
                        result.setResponse(EntityUtils.toString(httpResponse.getEntity()));
                    } else {
                        result.setResponse(locationHeader);
                    }
                    break;
                case 200:
                    result.setResponse(EntityUtils.toString(httpResponse.getEntity()));
                    break;
                default:
                    result.setResponse("");
                    break;
            }
            return result;
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Exception: ", e);
            return null;
        } finally {
        }
    }

    /**
     * Send HTTP PUT request to the urlString with putBody and return response
     * code and response body
     *
     * @param urlString The URL that the PUT request should be sent to
     * @param putBody The body of the PUT request
     * @return response-code and response(response body) of the HTTP PUT in the
     * MAP format. If the response is not 200, the response(response body) will
     * be empty.
     */
    public static HttpResponse doPut(String urlString, String putBody) {
        LOGGER.debug("Putting: {}", urlString);
        countPut++;
        HttpResponse result;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI uri = new URI(urlString);
            HttpPut request = new HttpPut(uri);
            StringEntity params = new StringEntity(putBody, ContentType.APPLICATION_JSON);
            request.setEntity(params);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                result = new HttpResponse(response.getStatusLine().getStatusCode());
                if (response.getEntity() == null) {
                    result.setResponse("");
                } else {
                    result.setResponse(EntityUtils.toString(response.getEntity()));
                }
            }
        } catch (IOException | URISyntaxException | RuntimeException e) {
            LOGGER.error("Exception: ", e);
            return null;
        }
        return result;
    }

    /**
     * Send HTTP DELETE request to the urlString and return response code
     *
     * @param urlString The URL that the DELETE request should be sent to
     * @return response-code of the HTTP DELETE in the MAP format. The MAP
     * contains an empty response, in order to be consistent with what other
     * HTTP requests return.
     */
    public static HttpResponse doDelete(String urlString) {
        HttpResponse result;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI uri = new URI(urlString);
            HttpDelete request = new HttpDelete(uri);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                result = new HttpResponse(response.getStatusLine().getStatusCode());
                if (response.getEntity() == null) {
                    result.setResponse("");
                } else {
                    result.setResponse(EntityUtils.toString(response.getEntity()));
                }
            }
        } catch (IOException | URISyntaxException | RuntimeException e) {
            LOGGER.error("Exception: ", e);
            return null;
        }
        return result;
    }

    /**
     * Send HTTP PATCH request to the urlString with patchBody and return
     * response code and response body
     *
     * @param urlString The URL that the PATCH request should be sent to
     * @param patchBody The body of the PATCH request
     * @return response-code and response(response body) of the HTTP PATCH in
     * the MAP format. If the response is not 200, the response(response body)
     * will be empty.
     */
    public static HttpResponse doPatch(String urlString, String patchBody) {
        URI uri = null;
        try {
            LOGGER.debug("Patching: {}", urlString);
            countPatch++;
            uri = new URI(urlString);

            HttpResponse result;
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPatch request = new HttpPatch(uri);
                StringEntity params = new StringEntity(patchBody, ContentType.APPLICATION_JSON);
                request.setEntity(params);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    result = new HttpResponse(response.getStatusLine().getStatusCode());
                    if (response.getEntity() == null) {
                        result.setResponse("");
                    } else {
                        result.setResponse(EntityUtils.toString(response.getEntity()));
                    }
                }
            }
            return result;
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Exception: ", e);
        }
        return null;
    }

    /**
     * Send HTTP PATCH request to the urlString with patchBody and return
     * response code and response body
     *
     * @param urlString The URL that the PATCH request should be sent to
     * @param patchBody The body of the PATCH request
     * @return response-code and response(response body) of the HTTP PATCH in
     * the MAP format. If the response is not 200, the response(response body)
     * will be empty.
     */
    public static HttpResponse doJsonPatch(String urlString, String patchBody) {
        URI uri;
        LOGGER.debug("Patching: {}", urlString);
        countPatch++;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            uri = new URI(urlString);
            HttpPatch request = new HttpPatch(uri);
            StringEntity params = new StringEntity(patchBody, APPLICATION_JSON_PATCH);
            request.setEntity(params);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpResponse result = new HttpResponse(response.getStatusLine().getStatusCode());
                if (response.getEntity() == null) {
                    result.setResponse("");
                } else {
                    result.setResponse(EntityUtils.toString(response.getEntity()));
                }
                return result;
            }
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Failed to send JSON Patch.", e);
        }
        return null;
    }

    /**
     * Parse a selfLink or Location response header and return the id.
     *
     * @param selfLink The selfLink to parse.
     * @return The id found in the selfLink.
     */
    public static Object idFromSelfLink(String selfLink) {
        String idString = selfLink.substring(selfLink.indexOf('(') + 1, selfLink.indexOf(')'));
        if (idString.startsWith("'") && idString.endsWith("'")) {
            return idString.substring(1, idString.length() - 1);
        }
        try {
            return Long.valueOf(idString);
        } catch (NumberFormatException ex) {
            fail("Failed to parse returned ID (" + idString + "). String IDs must start and end with a single quote (').");
        }
        return idString;
    }
}
