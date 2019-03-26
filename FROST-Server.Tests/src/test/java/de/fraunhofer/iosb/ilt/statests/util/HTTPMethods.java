package de.fraunhofer.iosb.ilt.statests.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sending HTTP Methods: GET, POST, PUT, PATCH, and DELETE
 */
public class HTTPMethods {

    public static final ContentType APPLICATION_JSON_PATCH = ContentType.create("application/json-patch+json", Consts.UTF_8);
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPMethods.class);

    /**
     * Send HTTP GET request to the urlString and return response code and
     * response body
     *
     * @param urlString The URL that the GET request should be sent to
     * @return response-code and response(response body) of the HTTP GET in the
     * MAP format. If the response is not 200, the response(response body) will
     * be empty.
     */
    public static Map<String, Object> doGet(String urlString) {
        HttpURLConnection connection = null;
        try {
            LOGGER.info("Getting: {}", urlString);
            //Create connection
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("response-code", connection.getResponseCode());
            if (connection.getResponseCode() == 200) {
                result.put("response", responseToString(connection));
            } else {
                result.put("response", "");
            }
            return result;
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

        }
    }

    private static String responseToString(HttpURLConnection connection) throws IOException {
        //Get Response
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        return response.toString();
    }

    /**
     * Send HTTP POST request to the urlString with postBody and return response
     * code and response body
     *
     * @param urlString The URL that the POST request should be sent to
     * @param postBody The body of the POST request
     * @return response-code and response of the HTTP POST in the MAP format. If
     * the response is 201, the response will contain the self-link to the
     * created entity. Otherwise, it will be empty String.
     */
    public static Map<String, Object> doPost(String urlString, String postBody) {
        HttpURLConnection connection = null;
        try {
            LOGGER.info("Posting: {}", urlString);
            //Create connection
            URL url = new URL(urlString);
            byte[] postData = postBody.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            connection.setUseCaches(false);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("response-code", connection.getResponseCode());
            if (connection.getResponseCode() == 201) {
                String locationHeader = connection.getHeaderField("location");
                if (locationHeader == null || locationHeader.isEmpty()) {
                    result.put("response", responseToString(connection));
                } else {
                    result.put("response", locationHeader);
                }
            } else {
                result.put("response", "");
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
    public static Map<String, Object> doPut(String urlString, String putBody) {
        HttpURLConnection connection = null;
        try {
            LOGGER.info("Putting: {}", urlString);
            //Create connection
            URI uri = new URI(urlString);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPut request = new HttpPut(uri);
            StringEntity params = new StringEntity(putBody, ContentType.APPLICATION_JSON);
            request.setEntity(params);
            CloseableHttpResponse response = httpClient.execute(request);
            Map<String, Object> result = new HashMap<>();
            result.put("response-code", response.getStatusLine().getStatusCode());
            result.put("response", EntityUtils.toString(response.getEntity()));
            response.close();
            httpClient.close();
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

    /**
     * Send HTTP DELETE request to the urlString and return response code
     *
     * @param urlString The URL that the DELETE request should be sent to
     * @return response-code of the HTTP DELETE in the MAP format. The MAP
     * contains an empty response, in order to be consistent with what other
     * HTTP requests return.
     */
    public static Map<String, Object> doDelete(String urlString) {
        HttpURLConnection connection = null;
        try {
            LOGGER.info("Deleting: {}", urlString);
            //Create connection
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty(
                    "Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("DELETE");
            connection.connect();

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("response-code", connection.getResponseCode());
            result.put("response", "");

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
    public static Map<String, Object> doPatch(String urlString, String patchBody) {
        URI uri = null;
        try {
            LOGGER.info("Patching: {}", urlString);
            uri = new URI(urlString);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPatch request = new HttpPatch(uri);
            StringEntity params = new StringEntity(patchBody, ContentType.APPLICATION_JSON);
            request.setEntity(params);
            CloseableHttpResponse response = httpClient.execute(request);
            Map<String, Object> result = new HashMap<>();
            result.put("response-code", response.getStatusLine().getStatusCode());
            result.put("response", EntityUtils.toString(response.getEntity()));
            response.close();
            httpClient.close();
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
    public static Map<String, Object> doJsonPatch(String urlString, String patchBody) {
        URI uri;
        LOGGER.info("Patching: {}", urlString);
        Map<String, Object> result;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            uri = new URI(urlString);
            HttpPatch request = new HttpPatch(uri);
            StringEntity params = new StringEntity(patchBody, APPLICATION_JSON_PATCH);
            request.setEntity(params);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                result = new HashMap<>();
                result.put("response-code", response.getStatusLine().getStatusCode());
                result.put("response", EntityUtils.toString(response.getEntity()));
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
        String idString = selfLink.substring(selfLink.indexOf("(") + 1, selfLink.indexOf(")"));
        if (idString.startsWith("'") && idString.endsWith("'")) {
            return idString.substring(1, idString.length() - 1);
        }
        try {
            return Long.parseLong(idString);
        } catch (NumberFormatException ex) {
            Assert.fail("Failed to parse returned ID (" + idString + "). String IDs must start and end with a single quote (').");
        }
        return idString;
    }
}
