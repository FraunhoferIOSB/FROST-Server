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
package de.fraunhofer.iosb.ilt.statests.c04batch;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_NAME;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.utils.StringHelper.formatKeyValuesForUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper2;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import de.fraunhofer.iosb.ilt.statests.util.model.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.model.IdType;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Includes various tests of "A.4. SensorThings API Batch Request Extension
 * Tests" conformance class.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class BatchTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchTests.class);
    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> OBSERVED_PROPS = new ArrayList<>();
    private static final Map<EntityType, IdType> ID_TYPES = new HashMap<>();
    private final ObjectMapper mapper;

    private static EntityHelper2 eh2;

    public BatchTests(ServerVersion version) {
        super(version);
        mapper = new ObjectMapper();
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        eh2 = new EntityHelper2(sSrvc);
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

    @AfterAll
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
            MapValue properties = CollectionsHelper.propertiesBuilder()
                    .addItem("int", i + 8)
                    .build();
            Entity thing = sMdl.newThing("Thing " + i, "It's a thing.");
            thing.setProperty(EP_PROPERTIES, properties);
            sSrvc.create(thing);
            THINGS.add(thing);
        }
        Entity obsProp = sMdl.newObservedProperty("ObservedProperty 1", "http://ucom.org/temperature",
                "The temperature of the thing.");
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        ID_TYPES.put(EntityType.THING, IdType.findFor(THINGS.get(0).getPrimaryKeyValues().get(0)));
        ID_TYPES.put(EntityType.OBSERVED_PROPERTY, IdType.findFor(OBSERVED_PROPS.get(0).getPrimaryKeyValues().get(0)));
    }

    /**
     * Test batch request body example from "OGC SensorThings API Part 1,
     * Sensing Version 1.1, 11.2.1. Batch request body example" except changes
     * to get reproducible test not depending on server generated id.
     */
    @Test
    void test01BatchRequest() {
        LOGGER.info("  test01BatchRequest");
        final String batchContent = "--batch_36522ad7-fc75-4b56-8c71-56071383e77b\r\n"
                + "Content-Type: application/http\r\n"
                + "\r\n"
                + "GET /" + version.urlPart + "/Things(" + formatKeyValuesForUrl(THINGS.get(0)) + ")?$select=name HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "\r\n"
                + "\r\n"
                + "--batch_36522ad7-fc75-4b56-8c71-56071383e77b\r\n"
                + "Content-Type: multipart/mixed;boundary=changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n"
                + "\r\n"
                + "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n"
                + "Content-Type: application/http\r\n"
                + "Content-ID: 1\r\n"
                + "\r\n"
                + "POST /" + version.urlPart + "/Things HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: 36\r\n"
                + "\r\n"
                + "{\"name\":\"New\",\"description\":\"Thing\"}\r\n"
                + "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n"
                + "Content-Type: application/http\r\n"
                + "Content-ID: 2\r\n"
                + "\r\n"
                + "PATCH /" + version.urlPart + "/Things(" + formatKeyValuesForUrl(THINGS.get(0)) + ") HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: 18\r\n"
                + "\r\n"
                + "{\"name\":\"Patched\"}\r\n"
                + "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd--\r\n"
                + "--batch_36522ad7-fc75-4b56-8c71-56071383e77b\r\n"
                + "Content-Type: application/http\r\n"
                + "\r\n"
                + "GET /" + version.urlPart + "/Things(" + Utils.quoteForUrl(ID_TYPES.get(EntityType.THING).generateUnlikely()) + ") HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "\r\n"
                + "\r\n"
                + "--batch_36522ad7-fc75-4b56-8c71-56071383e77b--";
        String response = postBatch("batch_36522ad7-fc75-4b56-8c71-56071383e77b", batchContent);
        String thingId = Utils.quoteForUrl(eh2.getEntityJson(sMdl.etThing, "$orderby=id%20desc").get("@iot.id"));
        String batchBoundary = response.split("\n", 2)[0];
        int mixedBoundaryStart = response.indexOf("boundary=") + 9;
        String mixedBoundary = response.substring(mixedBoundaryStart, mixedBoundaryStart + 40);
        // Note: using LF line terminator instead of recommended CRLF, see
        // https://datatracker.ietf.org/doc/html/rfc7230#section-3.5
        assertEquals(batchBoundary + "\n"
                + "Content-Type: application/http\n"
                + "\n"
                + "http/1.1 200 no text\n"
                + "Content-Type: application/json; charset=UTF-8\n"
                + "\n"
                + "{\"name\":\"Thing 0\"}\n"
                + batchBoundary + "\n"
                + "Content-Type: multipart/mixed; boundary=" + mixedBoundary + "\n"
                + "\n"
                + "--" + mixedBoundary + "\n"
                + "Content-Type: application/http\n"
                + "Content-ID: 1\n"
                + "\n"
                + "http/1.1 201 no text\n"
                + "Location: " + serverSettings.getServiceUrl(version) + "/Things(" + thingId + ")\n"
                + "\n"
                + "\n"
                + "--" + mixedBoundary + "\n"
                + "Content-Type: application/http\n"
                + "Content-ID: 2\n"
                + "\n"
                + "http/1.1 200 no text\n"
                + "\n"
                + "\n"
                + "--" + mixedBoundary + "--\n"
                + batchBoundary + "\n"
                + "Content-Type: application/http\n"
                + "\n"
                + "http/1.1 404 no text\n"
                + "\n"
                + "{\"code\":404,\"type\":\"error\",\"message\":\"Not Found\"}\n"
                + batchBoundary + "--", response);
    }

    /**
     * Test batch request body example from "OGC SensorThings API Part 1,
     * Sensing Version 1.1, 11.2.2. Referencing new entities in a change set
     * example", except:
     * <li>added Content-ID for second POST as per OData spec (From
     * http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793751
     * "In addition each request within a change set MUST specify a Content-ID
     * header with a value unique within the batch request.")
     * <li>missing mandatory Datastream fields.
     */
    @Test
    void test02BatchRequestWithChangeSetReferencingNewEntities() {
        LOGGER.info("  test02BatchRequestWithChangeSetReferencingNewEntities");

        String post1 = "{\r\n"
                + "  \"name\": \"DS18B20\",\r\n"
                + "  \"description\": \"DS18B20 is an air temperature sensor\",\r\n"
                + "  \"encodingType\": \"application/pdf\",\r\n"
                + "  \"metadata\": \"http://datasheets.maxim-ic.com/en/ds/DS18B20.pdf\"\r\n"
                + "}";
        String post2 = "{\r\n"
                + "  \"name\": \"Temperature Thing 5\",\r\n"
                + "  \"description\": \"The temperature of thing 5\",\r\n"
                + "  \"unitOfMeasurement\": {\r\n"
                + "    \"name\": \"degree Celsius\",\r\n"
                + "    \"symbol\": \"°C\",\r\n"
                + "    \"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\r\n"
                + "  },\n"
                + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement\",\r\n"
                + "  \"ObservedProperty\": {\"@iot.id\": " + Utils.quoteForJson(OBSERVED_PROPS.get(0).getPrimaryKeyValues().get(0)) + "},\r\n"
                + "  \"Sensor\": {\"@iot.id\": \"$sensor1\"}\r\n"
                + "}";
        final String batchContent = "--batch_36522ad7-fc75-4b56-8c71-56071383e77b\r\n"
                + "Content-Type: multipart/mixed;boundary=changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n"
                + "\r\n"
                + "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n"
                + "Content-Type: application/http\r\n"
                + "Content-ID: sensor1\r\n"
                + "\r\n"
                + "POST /" + version.urlPart + "/Sensors HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + post1.length() + "\r\n"
                + "\r\n"
                + post1 + "\r\n"
                + "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd\r\n"
                + "Content-Type: application/http\r\n"
                + "Content-ID: any\r\n"
                + "\r\n"
                + "POST /" + version.urlPart + "/Things(" + formatKeyValuesForUrl(THINGS.get(0)) + ")/Datastreams HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + post2.length() + "\r\n"
                + "\r\n"
                + post2 + "\r\n"
                + "--changeset_77162fcd-b8da-41ac-a9f8-9357efbbd--\r\n"
                + "--batch_36522ad7-fc75-4b56-8c71-56071383e77b--";
        String response = postBatch("batch_36522ad7-fc75-4b56-8c71-56071383e77b", batchContent);

        String sensorId = Utils.quoteForUrl(eh2.getEntity(sMdl.etSensor).get("@iot.id"));
        String datastreamId = Utils.quoteForUrl(eh2.getEntity(sMdl.etDatastream).get("@iot.id"));
        String batchBoundary = response.split("\n", 2)[0];
        int mixedBoundaryStart = response.indexOf("boundary=") + 9;
        String mixedBoundary = response.substring(mixedBoundaryStart, mixedBoundaryStart + 40);
        assertEquals(batchBoundary + "\n" + "Content-Type: multipart/mixed; boundary=" + mixedBoundary + "\n"
                + "\n"
                + "--" + mixedBoundary + "\n"
                + "Content-Type: application/http\n"
                + "Content-ID: sensor1\n"
                + "\n"
                + "http/1.1 201 no text\n"
                + "Location: " + serverSettings.getServiceUrl(version) + "/Sensors(" + sensorId + ")\n"
                + "\n"
                + "\n"
                + "--" + mixedBoundary + "\n"
                + "Content-Type: application/http\n"
                + "Content-ID: any\n"
                + "\n"
                + "http/1.1 201 no text\n"
                + "Location: " + serverSettings.getServiceUrl(version) + "/Datastreams(" + datastreamId + ")\n"
                + "\n"
                + "\n"
                + "--" + mixedBoundary + "--\n"
                + batchBoundary + "--", response);
    }

    @Test
    void test03BatchRequestWithEncodedCharsInUrl() {
        LOGGER.info("  test03BatchRequestWithEncodedCharsInUrl");

        String response = postBatch("batch_test", "--batch_test\r\n"
                + "Content-Type: application/http\r\n"
                + "\r\n"
                + "GET Things?$count=true&$filter=properties/int%20eq%2010&$select=name HTTP/1.1\r\n"
                + "\r\n"
                + "\r\n"
                + "--batch_test--");
        String batchBoundary = response.split("\n", 2)[0];
        assertEquals(batchBoundary + "\n"
                + "Content-Type: application/http\n"
                + "\n"
                + "http/1.1 200 no text\n"
                + "Content-Type: application/json; charset=UTF-8\n"
                + "\n"
                + "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 2\"}]}\n"
                + batchBoundary + "--", response);
    }

    /**
     * Tests Absolute URI with schema, host, port, and absolute resource path.
     * Example:
     *
     * GET https://host:1234/path/service/People(1) HTTP/1.1
     *
     * See
     * http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793750
     *
     */
    @Test
    void test04BatchRequestWithAbsoluteUri() {
        LOGGER.info("  test04BatchRequestWithAbsoluteUri");

        String response = postBatch("batch_test", "--batch_test\r\n"
                + "Content-Type: application/http\r\n"
                + "\r\n"
                + "GET " + serverSettings.getServiceUrl(version)
                + "/Things?$count=true&$filter=properties/int%20eq%2010&$select=name HTTP/1.1\r\n"
                + "\r\n"
                + "\r\n"
                + "--batch_test\r\n"
                + "Content-Type: application/http\r\n"
                + "\r\n"
                + "GET " + serverSettings.getServiceUrl(version)
                + "/Things?$count=true&$filter=properties/int%20eq%2011&$select=name HTTP/1.1\r\n"
                + "\r\n"
                + "\r\n"
                + "--batch_test--");
        String batchBoundary = response.split("\n", 2)[0];
        assertEquals(batchBoundary + "\n"
                + "Content-Type: application/http\n"
                + "\n"
                + "http/1.1 200 no text\n"
                + "Content-Type: application/json; charset=UTF-8\n"
                + "\n"
                + "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 2\"}]}\n"
                + batchBoundary + "\n"
                + "Content-Type: application/http\n"
                + "\n"
                + "http/1.1 200 no text\n"
                + "Content-Type: application/json; charset=UTF-8\n"
                + "\n"
                + "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 3\"}]}\n"
                + batchBoundary + "--", response);
    }

    /**
     * Tests Resource path relative to the batch request URI. Example:
     *
     * GET People(1) HTTP/1.1
     *
     * See
     * http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793750
     *
     */
    @Test
    void test05BatchRequestWithResourcePathRelativeToBatchRequest() {
        LOGGER.info("  test05BatchRequestWithResourcePathRelativeToBatchRequest");

        String response = postBatch("batch_test", "--batch_test\r\n"
                + "Content-Type: application/http\r\n"
                + "\r\n"
                + "GET Things?$count=true&$filter=properties/int%20eq%2010&$select=name HTTP/1.1\r\n"
                + "\r\n"
                + "\r\n"
                + "--batch_test\r\n"
                + "Content-Type: application/http\r\n"
                + "\r\n"
                + "GET Things?$count=true&$filter=properties/int%20eq%2011&$select=name HTTP/1.1\r\n"
                + "\r\n"
                + "\r\n"
                + "--batch_test--");
        String batchBoundary = response.split("\n", 2)[0];
        assertEquals(batchBoundary + "\n"
                + "Content-Type: application/http\n"
                + "\n"
                + "http/1.1 200 no text\n"
                + "Content-Type: application/json; charset=UTF-8\n"
                + "\n"
                + "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 2\"}]}\n"
                + batchBoundary + "\n"
                + "Content-Type: application/http\n"
                + "\n"
                + "http/1.1 200 no text\n"
                + "Content-Type: application/json; charset=UTF-8\n"
                + "\n"
                + "{\"@iot.count\":1,\"value\":[{\"name\":\"Thing 3\"}]}\n"
                + batchBoundary + "--", response);
    }

    @Test
    void test06JsonBatchRequest() throws ServiceFailureException {
        LOGGER.info("  test06JsonBatchRequest");
        String request = """
                {
                    "requests":[{
                        "id": "0",
                        "method": "get",
                        "url": "Things($thing0)?$select=name"
                    },{
                        "id": "1",
                        "atomicityGroup": "group1",
                        "method": "post",
                        "url": "Things",
                        "body": {"name":"New","description":"Thing"}
                    },{
                        "id": "2",
                        "atomicityGroup": "group1",
                        "method": "patch",
                        "url": "Things($thing0)",
                        "body": {"name":"Json Patched"}
                    },{
                        "id": "3",
                        "atomicityGroup": "group1",
                        "method": "patch",
                        "headers": {
                            "content-type": "application/json-patch+json"
                        },
                        "url": "Things($thing1)",
                        "body": [
                          { "op": "replace", "path": "/name", "value": "Thing 1 Updated" },
                          { "op": "add", "path": "/properties/new", "value": "Changes" }
                        ]
                    },{
                        "id": "4",
                        "method": "get",
                        "url": "Things(null)"
                    }]}""";
        request = StringUtils.replace(request, "$thing0", formatKeyValuesForUrl(THINGS.get(0)));
        request = StringUtils.replace(request, "$thing1", formatKeyValuesForUrl(THINGS.get(1)));
        String response = postBatch(null, request);
        String thingId = Utils.quoteForUrl(eh2.getEntityJson(sMdl.etThing, "$orderby=id%20desc").get("@iot.id"));

        try {
            String expResponse = """
                    {"responses":[
                        {"id":"0","status":200,"body":{"name":"Patched"}},
                        {"id":"1","status":201,"location":"$serviceUrl/Things($newThingId)"},
                        {"id":"2","status":200},
                        {"id":"3","status":200},
                        {"id":"4","status":404,"body":{"code":404,"type":"error","message":"Not a valid id: Path is not valid."}}
                    ]}""";
            expResponse = StringUtils.replace(expResponse, "$serviceUrl", serverSettings.getServiceUrl(version));
            expResponse = StringUtils.replace(expResponse, "$newThingId", thingId);
            BatchResponseJson expected = mapper.readValue(expResponse, BatchResponseJson.class);
            BatchResponseJson actual = mapper.readValue(response, BatchResponseJson.class);
            assertEquals(expected, actual, "Response not as expected.");

            Entity updatedThing1 = service.service.dao(sMdl.etThing).find(THINGS.get(1).getPrimaryKeyValues());
            assertEquals("Thing 1 Updated", updatedThing1.getProperty(EP_NAME));
            assertEquals("Changes", updatedThing1.getProperty(EP_PROPERTIES).get("new"));
        } catch (JsonProcessingException ex) {
            fail("Failed to parse response as json.", ex);
        }

    }

    @Test
    void test07JsonBatchRequestWithChangeSetReferencingNewEntitiesInBody() {
        LOGGER.info("  test07JsonBatchRequestWithChangeSetReferencingNewEntitiesInBody");
        String post1 = """
                {
                  "name": "DS18B20",
                  "description": "DS18B20 is an air temperature sensor",
                  "encodingType": "application/pdf",
                  "metadata": "http://datasheets.maxim-ic.com/en/ds/DS18B20.pdf"
                }""";
        String post2 = """
                {\r
                  "name": "Temperature Thing 5",
                  "description": "The temperature of thing 5",
                  "unitOfMeasurement": {
                    "name": "degree Celsius",
                    "symbol": "\u00b0C",
                    "definition": "http://unitsofmeasure.org/ucum.html#para-30"
                  },
                  "observationType": "http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement",
                  "ObservedProperty": {"@iot.id": $ObservedProperty0},
                  "Sensor": {"@iot.id": "$sensor1"}
                }""";
        post2 = StringUtils.replace(post2, "$ObservedProperty0", Utils.quoteForJson(OBSERVED_PROPS.get(0).getPrimaryKeyValues().get(0)));
        String request = """
                {
                    "requests":[{
                        "id": "sensor1",
                        "atomicityGroup": "group1",
                        "method": "post",
                        "url": "Sensors",
                        "body":$post1
                    },{
                        "id": "any",
                        "atomicityGroup": "group1",
                        "method": "post",
                        "url": "Things($thing0)/Datastreams",
                        "body": $post2
                    }]
                }""";
        request = StringUtils.replace(request, "$post1", post1);
        request = StringUtils.replace(request, "$post2", post2);
        request = StringUtils.replace(request, "$thing0", formatKeyValuesForUrl(THINGS.get(0)));
        String response = postBatch(null, request);

        String sensorId = Utils.quoteForUrl(eh2.getEntityJson(sMdl.etSensor, "$orderby=id%20desc").get("@iot.id"));
        String datastreamId = Utils.quoteForUrl(eh2.getEntityJson(sMdl.etDatastream, "$orderby=id%20desc").get("@iot.id"));

        try {
            BatchResponseJson expected = mapper.readValue("{\"responses\":["
                    + "{\"id\":\"sensor1\","
                    + "\"status\":201,"
                    + "\"location\":\"" + serverSettings.getServiceUrl(version) + "/Sensors(" + sensorId + ")\"},"
                    + "{\"id\":\"any\",\"status\":201,\"location\":\"" + serverSettings.getServiceUrl(version)
                    + "/Datastreams(" + datastreamId + ")\"}"
                    + "]}", BatchResponseJson.class);
            BatchResponseJson actual = mapper.readValue(response, BatchResponseJson.class);
            assertEquals(expected, actual, "Response not as expected.");
        } catch (JsonProcessingException ex) {
            fail("Failed to parse response as json.", ex);
        }
    }

    @Test
    void test08JsonBatchRequestWithChangeSetReferencingNewEntitiesInUrl() {
        LOGGER.info("  test08JsonBatchRequestWithChangeSetReferencingNewEntitiesInUrl");
        String post1 = """
                {
                  "name": "DS18B20",
                  "description": "DS18B20 is an air temperature sensor",
                  "encodingType": "application/pdf",
                  "metadata": "http://datasheets.maxim-ic.com/en/ds/DS18B20.pdf"
                }""";
        String post2 = """
                {\r
                  "name": "Temperature Thing 5",
                  "description": "The temperature of thing 5",
                  "unitOfMeasurement": {
                    "name": "degree Celsius",
                    "symbol": "\u00b0C",
                    "definition": "http://unitsofmeasure.org/ucum.html#para-30"
                  },
                  "observationType": "http://www.opengis.net/def/observationType/OGCOM/2.0/OM_Measurement",
                  "ObservedProperty": {"@iot.id": $ObservedProperty0},
                  "Thing": {"@iot.id": $thing0}
                }""";
        post2 = StringUtils.replace(post2, "$ObservedProperty0", Utils.quoteForJson(OBSERVED_PROPS.get(0).getPrimaryKeyValues().get(0)));
        post2 = StringUtils.replace(post2, "$thing0", Utils.quoteForJson(THINGS.get(0).getPrimaryKeyValues().get(0)));
        String request = """
                {
                    "requests":[{
                        "id": "sensor1",
                        "atomicityGroup": "group1",
                        "method": "post",
                        "url": "Sensors",
                        "body":$post1
                    },{
                        "id": "any",
                        "atomicityGroup": "group1",
                        "method": "post",
                        "url": "$sensor1/Datastreams",
                        "body": $post2
                    }]
                }""";
        request = StringUtils.replace(request, "$post1", post1);
        request = StringUtils.replace(request, "$post2", post2);
        String response = postBatch(null, request);

        String sensorId = Utils.quoteForUrl(eh2.getEntityJson(sMdl.etSensor, "$orderby=id%20desc").get("@iot.id"));
        String datastreamId = Utils.quoteForUrl(eh2.getEntityJson(sMdl.etDatastream, "$orderby=id%20desc").get("@iot.id"));

        try {
            BatchResponseJson expected = mapper.readValue("{\"responses\":["
                    + "{\"id\":\"sensor1\","
                    + "\"status\":201,"
                    + "\"location\":\"" + serverSettings.getServiceUrl(version) + "/Sensors(" + sensorId + ")\"},"
                    + "{\"id\":\"any\",\"status\":201,\"location\":\"" + serverSettings.getServiceUrl(version)
                    + "/Datastreams(" + datastreamId + ")\"}"
                    + "]}", BatchResponseJson.class);
            BatchResponseJson actual = mapper.readValue(response, BatchResponseJson.class);
            assertEquals(expected, actual, "Response not as expected.");
        } catch (JsonProcessingException ex) {
            fail("Failed to parse response as json.", ex);
        }
    }

    private String postBatch(String boundary, String body) {
        String urlString = serverSettings.getServiceUrl(version) + "/$batch";
        HttpResponse httpResponse = HTTPMethods.doPost(urlString, body,
                boundary == null ? "application/json" : "multipart/mixed;boundary=" + boundary);
        assertEquals(200, httpResponse.code, "Batch response should be 200");
        return httpResponse.response;
    }

}
