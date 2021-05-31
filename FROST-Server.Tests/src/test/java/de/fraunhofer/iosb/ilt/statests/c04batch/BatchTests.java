package de.fraunhofer.iosb.ilt.statests.c04batch;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Includes various tests of "A.4. SensorThings API Batch Request Extension
 * Tests" conformance class.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BatchTests extends AbstractTestClass {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchTests.class);
    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<ObservedProperty> OBSERVED_PROPS = new ArrayList<>();

    public BatchTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        try {
            createEntities();
        } catch (ServiceFailureException | URISyntaxException ex) {
            LOGGER.error("Failed to set up.", ex);
        }
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    @AfterClass
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        THINGS.clear();
        OBSERVED_PROPS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        for (int i = 0; i < 6; i++) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("int", i + 8);
            Thing thing = new Thing("Thing " + i, "It's a thing.");
            thing.setProperties(properties);
            service.create(thing);
            THINGS.add(thing);
        }
        ObservedProperty obsProp = new ObservedProperty("ObservedProperty 1", new URI("http://ucom.org/temperature"),
                "The temperature of the thing.");
        service.create(obsProp);
        OBSERVED_PROPS.add(obsProp);
    }

    /**
     * Test batch request body example from "OGC SensorThings API Part 1, Sensing
     * Version 1.1, 11.2.1. Batch request body example" except changes to get
     * reproducible test not depending on server generated id.
     */
    @Test
    public void test01BatchRequest() {
        LOGGER.info("  test01BatchRequest");
        String response = postBatch("batch_36522ad7-fc75-4b56-8c71-56071383e77b",
                "--batch_36522ad7-fc75-4b56-8c71-56071383e77b\r\n" + //
                        "Content-Type: application/http\r\n" + //
                        "\r\n" + //
                        "GET /" + version.urlPart + "/Things(" + THINGS.get(0).getId().getUrl()
                        + ")?$select=name HTTP/1.1\r\n" + //
                        "Host: localhost\r\n" + //
                        "\r\n" + //
                        "\r\n" + //
                        "--batch_36522ad7-fc75-4b56-8c71-56071383e77b\r\n" + //
                        "Content-Type: multipart/mixed;boundary=changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n" + //
                        "\r\n" + //
                        "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n" + //
                        "Content-Type: application/http\r\n" + //
                        "Content-ID: 1\r\n" + //
                        "\r\n" + //
                        "POST /" + version.urlPart + "/Things HTTP/1.1\r\n" + //
                        "Host: localhost\r\n" + //
                        "Content-Type: application/json\r\n" + //
                        "Content-Length: 36\r\n" + //
                        "\r\n" + //
                        "{\"name\":\"New\",\"description\":\"Thing\"}\r\n" + //
                        "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n" + //
                        "Content-Type: application/http\r\n" + //
                        "Content-ID: 2\r\n" + //
                        "\r\n" + //
                        "PATCH /" + version.urlPart + "/Things(" + THINGS.get(0).getId().getUrl() + ") HTTP/1.1\r\n" + //
                        "Host: localhost\r\n" + //
                        "Content-Type: application/json\r\n" + //
                        "Content-Length: 18\r\n" + //
                        "\r\n" + //
                        "{\"name\":\"Patched\"}\r\n" + //
                        "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd--\r\n" + //
                        "--batch_36522ad7-fc75-4b56-8c71-56071383e77b\r\n" + //
                        "Content-Type: application/http\r\n" + //
                        "\r\n" + //
                        "GET /" + version.urlPart + "/Things(null) HTTP/1.1\r\n" + //
                        "Host: localhost\r\n" + //
                        "\r\n" + //
                        "\r\n" + //
                        "--batch_36522ad7-fc75-4b56-8c71-56071383e77b--");
        String thingId = getLastestEntityIdForPath(EntityType.THING);
        String batchBoundary = response.split("\n", 2)[0];
        int mixedBoundaryStart = response.indexOf("boundary=") + 9;
        String mixedBoundary = response.substring(mixedBoundaryStart, mixedBoundaryStart + 40);
        // Note: using LF line terminator instead of recommended CRLF, see
        // https://datatracker.ietf.org/doc/html/rfc7230#section-3.5
        Assert.assertEquals(batchBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "\n" + //
                "http/1.1 200 no text\n" + //
                "Content-Type: application/json; charset=UTF-8\n" + //
                "\n" + //
                "{\"name\":\"Thing 0\"}\n" + //
                batchBoundary + "\n" + //
                "Content-Type: multipart/mixed; boundary=" + mixedBoundary + "\n" + //
                "\n" + //
                "--" + mixedBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "Content-ID: 1\n" + //
                "\n" + //
                "http/1.1 201 no text\n" + //
                "location: " + serverSettings.getServiceUrl(version) + "/Things(" + thingId + ")\n" + //
                "\n" + //
                "\n" + //
                "--" + mixedBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "Content-ID: 2\n" + //
                "\n" + //
                "http/1.1 200 no text\n" + // -
                "\n" + //
                "\n" + //
                "--" + mixedBoundary + "--\n" + //
                batchBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "\n" + //
                "http/1.1 404 no text\n" + //
                "\n" + //
                "{\"code\":404,\"type\":\"error\",\"message\":\"Not a valid id: Path is not valid.\"}\n" + //
                batchBoundary + "--", response);
    }

    /**
     * Test batch request body example from "OGC SensorThings API Part 1, Sensing
     * Version 1.1, 11.2.2. Referencing new entities in a change set example",
     * except:
     * <li>added Content-ID for second POST as per OData spec (From
     * http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793751
     * "In addition each request within a change set MUST specify a Content-ID
     * header with a value unique within the batch request.")
     * <li>missing mandatory Datastream fields.
     */
    @Test
    public void test02BatchRequestWithChangeSetReferencingNewEntities() {
        LOGGER.info("  test02BatchRequestWithChangeSetReferencingNewEntities");
        String post1 = "{\r\n" + //
                "  \"name\": \"DS18B20\",\r\n" + //
                "  \"description\": \"DS18B20 is an air temperature sensor\",\r\n" + //
                "  \"encodingType\": \"application/pdf\",\r\n" + //
                "  \"metadata\": \"http://datasheets.maxim-ic.com/en/ds/DS18B20.pdf\"\r\n" + //
                "}";
        String post2 = "{\r\n" + //
                "  \"name\": \"Temperature Thing 5\",\r\n" + //
                "  \"description\": \"The temperature of thing 5\",\r\n" + //
                "  \"unitOfMeasurement\": {\r\n" + //
                "    \"name\": \"degree Celsius\",\r\n" + //
                "    \"symbol\": \"°C\",\r\n" + //
                "    \"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\r\n" + //
                "  },\n" + //
                "  \"observationType\": \"http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement\",\r\n" + //
                "  \"ObservedProperty\": {\"@iot.id\": " + OBSERVED_PROPS.get(0).getId().getJson() + "},\r\n" + //
                "  \"Sensor\": {\"@iot.id\": \"$sensor1\"}\r\n" + //
                "}";
        String response = postBatch("batch_36522ad7-fc75-4b56-8c71-56071383e77b",
                "--batch_36522ad7-fc75-4b56-8c71-56071383e77b\r\n" + //
                        "Content-Type: multipart/mixed;boundary=changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n" + //
                        "\r\n" + //
                        "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n" + //
                        "Content-Type: application/http\r\n" + //
                        "Content-ID: sensor1\r\n" + //
                        "\r\n" + //
                        "POST /" + version.urlPart + "/Sensors HTTP/1.1\r\n" + //
                        "Host: localhost\r\n" + //
                        "Content-Type: application/json\r\n" + //
                        "Content-Length: " + post1.length() + "\r\n" + //
                        "\r\n" + //
                        post1 + "\r\n" + //
                        "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n" + //
                        "Content-Type: application/http\r\n" + //
                        "Content-ID: any\r\n" + //
                        "\r\n" + //
                        "POST /" + version.urlPart + "/Things(" + THINGS.get(0).getId().getUrl()
                        + ")/Datastreams HTTP/1.1\r\n" + //
                        "Host: localhost\r\n" + //
                        "Content-Type: application/json\r\n" + //
                        "Content-Length: " + post2.length() + "\r\n" + //
                        "\r\n" + //
                        post2 + "\r\n" + //
                        "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd--\r\n" + //
                        "--batch_36522ad7-fc75-4b56-8c71-56071383e77b--");

        String sensorId = getLastestEntityIdForPath(EntityType.SENSOR);
        String datastreamId = getLastestEntityIdForPath(EntityType.DATASTREAM);
        String batchBoundary = response.split("\n", 2)[0];
        int mixedBoundaryStart = response.indexOf("boundary=") + 9;
        String mixedBoundary = response.substring(mixedBoundaryStart, mixedBoundaryStart + 40);
        Assert.assertEquals(batchBoundary + "\n" + "Content-Type: multipart/mixed; boundary=" + mixedBoundary + "\n" + //
                "\n" + //
                "--" + mixedBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "Content-ID: sensor1\n" + //
                "\n" + //
                "http/1.1 201 no text\n" + //
                "location: " + serverSettings.getServiceUrl(version) + "/Sensors(" + sensorId + ")\n" + //
                "\n" + //
                "\n" + //
                "--" + mixedBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "Content-ID: any\n" + //
                "\n" + //
                "http/1.1 201 no text\n" + //
                "location: " + serverSettings.getServiceUrl(version) + "/Datastreams(" + datastreamId + ")\n" + //
                "\n" + //
                "\n" + //
                "--" + mixedBoundary + "--\n" + //
                batchBoundary + "--", response);
    }

    @Test
    public void test03BatchRequestWithEncodedCharsInUrl() {
        LOGGER.info("  test03BatchRequestWithEncodedCharsInUrl");

        String response = postBatch("batch_test", "--batch_test\r\n" + //
                "Content-Type: application/http\r\n" + //
                "\r\n" + //
                "GET Things?$filter=properties/int%20eq%2010&$select=name HTTP/1.1\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "--batch_test--");
        String batchBoundary = response.split("\n", 2)[0];
        Assert.assertEquals(batchBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "\n" + //
                "http/1.1 200 no text\n" + //
                "Content-Type: application/json; charset=UTF-8\n" + //
                "\n" + //
                "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 2\"}]}\n" + //
                batchBoundary + "--", response);
    }

    /**
     * Tests Absolute URI with schema, host, port, and absolute resource path.
     * Example:
     * 
     * GET https://host:1234/path/service/People(1) HTTP/1.1
     * 
     * See
     * http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793750
     **/
    @Test
    public void test04BatchRequestWithAbsoluteUri() {
        LOGGER.info("  test04BatchRequestWithAbsoluteUri");

        String response = postBatch("batch_test", "--batch_test\r\n" + //
                "Content-Type: application/http\r\n" + //
                "\r\n" + //
                "GET " + serverSettings.getServiceUrl(version)
                + "/Things?$filter=properties/int%20eq%2010&$select=name HTTP/1.1\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "--batch_test\r\n" + //
                "Content-Type: application/http\r\n" + //
                "\r\n" + //
                "GET " + serverSettings.getServiceUrl(version)
                + "/Things?$filter=properties/int%20eq%2011&$select=name HTTP/1.1\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "--batch_test--");
        String batchBoundary = response.split("\n", 2)[0];
        Assert.assertEquals(batchBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "\n" + //
                "http/1.1 200 no text\n" + //
                "Content-Type: application/json; charset=UTF-8\n" + //
                "\n" + //
                "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 2\"}]}\n" + //
                batchBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "\n" + //
                "http/1.1 200 no text\n" + //
                "Content-Type: application/json; charset=UTF-8\n" + //
                "\n" + //
                "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 3\"}]}\n" + //
                batchBoundary + "--", response);
    }

    /**
     * Tests Resource path relative to the batch request URI. Example:
     * 
     * GET People(1) HTTP/1.1
     * 
     * See
     * http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793750
     **/
    @Test
    public void test05BatchRequestWithResourcePathRelativeToBatchRequest() {
        LOGGER.info("  test05BatchRequestWithResourcePathRelativeToBatchRequest");

        String response = postBatch("batch_test", "--batch_test\r\n" + //
                "Content-Type: application/http\r\n" + //
                "\r\n" + //
                "GET Things?$filter=properties/int%20eq%2010&$select=name HTTP/1.1\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "--batch_test\r\n" + //
                "Content-Type: application/http\r\n" + //
                "\r\n" + //
                "GET Things?$filter=properties/int%20eq%2011&$select=name HTTP/1.1\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "--batch_test--");
        String batchBoundary = response.split("\n", 2)[0];
        Assert.assertEquals(batchBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "\n" + //
                "http/1.1 200 no text\n" + //
                "Content-Type: application/json; charset=UTF-8\n" + //
                "\n" + //
                "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 2\"}]}\n" + //
                batchBoundary + "\n" + //
                "Content-Type: application/http\n" + //
                "\n" + //
                "http/1.1 200 no text\n" + //
                "Content-Type: application/json; charset=UTF-8\n" + //
                "\n" + //
                "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 3\"}]}\n" + //
                batchBoundary + "--", response);
    }

    private String postBatch(String boundary, String body) {
        String urlString = serverSettings.getServiceUrl(version) + "/$batch";
        try {
            HttpResponse httpResponse = HTTPMethods.doPost(urlString, body, "multipart/mixed;boundary=" + boundary);
            Assert.assertEquals("Batch response should be 200", 200, httpResponse.code);
            return httpResponse.response;
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
            return null;
        }
    }

    private String getLastestEntityIdForPath(EntityType entityType) {
        EntityHelper entityHelper = new EntityHelper(serverSettings.getServiceUrl(version));
        Object id = entityHelper.getAnyEntity(entityType, "$orderBy=id%20desc", 1).get("@iot.id");
        if (id instanceof Number) {
            return id.toString();
        } else {
            return '\'' + id.toString() + '\'';
        }

    }
}
