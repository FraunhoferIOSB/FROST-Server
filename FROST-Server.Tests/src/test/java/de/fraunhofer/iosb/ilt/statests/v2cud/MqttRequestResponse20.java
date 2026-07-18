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
package de.fraunhofer.iosb.ilt.statests.v2cud;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_VALIDTIME;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.swecommon.util.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Tests for the geospatial functions.
 */
public class MqttRequestResponse20 extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttRequestResponse20.class);

    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> FEATURES = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> THINGS = new ArrayList<>();
    private static SensorThingsV20Core sMdl;

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        SERVER_PROPERTIES.put("plugins.modelLoader.enable", "true");
        SERVER_PROPERTIES.put("plugins.coreModel.enable", "false");
        SERVER_PROPERTIES.put("plugins.coreModel.idType", "LONG");
        SERVER_PROPERTIES.put("plugins.coreService.enable", "true");
        SERVER_PROPERTIES.put("plugins.coreModelV2.enable", "true");
    }

    public MqttRequestResponse20() {
        super(ServerVersion.V_2_0, SERVER_PROPERTIES);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV20Core.class);
        createEntities();
    }

    @Override
    protected SensorThingsService createService() throws MalformedURLException, URISyntaxException {
        return new SensorThingsService(new SensorThingsV20Core())
                .setBaseUrl(new URI(serverSettings.getServiceUrl(version)).toURL())
                .init();
    }

    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(sSrvc);
        THINGS.clear();
        FEATURES.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        createThings();
        createSensor();
        createObsProp();
        createDatastreams();
        createLocation0();

    }

    private static void createThings() throws ServiceFailureException {
        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 2", "The second thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 3", "The third thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 4", "The fourt thing.");
        sSrvc.create(thing);
        THINGS.add(thing);
    }

    private static void createSensor() throws ServiceFailureException {
        Entity sensor = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);
    }

    private static void createObsProp() throws ServiceFailureException, URISyntaxException {
        Entity obsProp = sMdl.newObservedProperty("Temperature", "http://ucom.org/temperature", "The temperature of the thing.");
        sSrvc.create(obsProp);
        O_PROPS.add(obsProp);
    }

    private static void createDatastreams() throws ServiceFailureException {
        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.",
                O_PROPS.get(0).getSelfLink(false),
                new UnitOfMeasurement()
                        .setLabel("degree celcius")
                        .setSymbol("°C")
                        .setCode("ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0).asReference());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).asReference());
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 2", "The temperature of thing 2, sensor 1.",
                O_PROPS.get(0).getSelfLink(false),
                new UnitOfMeasurement()
                        .setLabel("degree celcius")
                        .setSymbol("°C")
                        .setCode("ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(1).asReference());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).asReference());
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 3", "The temperature of thing 3, sensor 1.",
                O_PROPS.get(0).getSelfLink(false),
                new UnitOfMeasurement()
                        .setLabel("degree celcius")
                        .setSymbol("°C")
                        .setCode("ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(2).asReference());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).asReference());
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 4", "The temperature of thing 4, sensor 1.",
                O_PROPS.get(0).getSelfLink(false),
                new UnitOfMeasurement()
                        .setLabel("degree celcius")
                        .setSymbol("°C")
                        .setCode("ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(3).asReference());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).asReference());
        datastream.addNavigationEntity(sMdl.npDatastreamObservedproperties, O_PROPS.get(0).asReference());
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);
    }

    private static void createLocation0() throws ServiceFailureException {
        // Locations 0
        Point gjo = new Point(8, 51);
        Entity location = sMdl.newLocation("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0).asReference());
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 0", "This should be FoI #0.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);

        Entity o = sMdl.newObservation(1, ZonedDateTime.parse("2016-01-01T01:01:01.000Z"), DATASTREAMS.get(0).asReference())
                .setProperty(sMdl.npObservationProximateFoi, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-01T01:01:01.000Z"), Instant.parse("2016-01-01T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    /**
     * Test the geo.distance filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void test01_ReadLandingPage() throws ServiceFailureException {
        LOGGER.info("  test01_ReadLandingPage");
        URI mqttUri = URI.create(serverSettings.getMqttUrl());
        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(mqttUri.getHost())
                .serverPort(mqttUri.getPort())
                .buildBlocking();
        Mqtt5ConnAck connAck = client.connectWith()
                .cleanStart(true)
                .restrictions()
                .requestResponseInformation(true)
                .applyRestrictions()
                .send();
        Optional<MqttUtf8String> responseInformation = connAck.getResponseInformation();
        if (responseInformation.isEmpty()) {
            Assertions.fail("No response information.");
        }
        String responseTopicBase = responseInformation.get().toString();
        LOGGER.info("Got response topic: {}", responseTopicBase);
        PublishAnalyser pa = new PublishAnalyser();

        client.toAsync().subscribeWith()
                .topicFilter(responseTopicBase)
                .callback(pa)
                .send();

        Mqtt5PublishResult pubRes = client.publishWith()
                .topic("v2.0/$request")
                .responseTopic(responseTopicBase)
                .correlationData(pa.expect(this::analyseLandingPage))
                .userProperties()
                .add("url", "v2.0")
                .add("type", "read")
                .applyUserProperties()
                .qos(MqttQos.EXACTLY_ONCE)
                .send();
        Assertions.assertFalse(pubRes.getError().isPresent(), "Request failed to send");
        Awaitility.await()
                .atMost(Durations.ONE_SECOND)
                .untilTrue(pa.allDone);
        Assertions.assertEquals(0, pa.getFailCount().get(), "Not all messages are correct.");
        client.disconnect();
    }

    public boolean analyseLandingPage(Mqtt5Publish publish) {
        Optional<MqttUtf8String> contentType = publish.getContentType();
        Assertions.assertTrue(contentType.isPresent(), "Content type is missing on message.");
        Assertions.assertEquals(Constants.CONTENT_TYPE_APPLICATION_JSON, contentType.get().toString(), "Wrong content type on message.");

        JsonNode tree = Utils.MAPPER.readTree(publish.getPayloadAsBytes());

        JsonNode context = tree.get("@context");
        Assertions.assertTrue(context != null && context.isString(), "Field @context is missing, or not a string.");
        Assertions.assertEquals("v2.0/$metadata", context.stringValue(), "Incorrect context url");

        return true;
    }

    @Test
    void test02_ReadThings() throws ServiceFailureException {
        LOGGER.info("  test02_ReadThings");
        URI mqttUri = URI.create(serverSettings.getMqttUrl());
        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(mqttUri.getHost())
                .serverPort(mqttUri.getPort())
                .buildBlocking();
        Mqtt5ConnAck connAck = client.connectWith()
                .cleanStart(true)
                .restrictions()
                .requestResponseInformation(true)
                .applyRestrictions()
                .send();
        Optional<MqttUtf8String> responseInformation = connAck.getResponseInformation();
        if (responseInformation.isEmpty()) {
            Assertions.fail("No response information.");
        }
        String responseTopicBase = responseInformation.get().toString();
        LOGGER.info("Got response topic: {}", responseTopicBase);
        PublishAnalyser pa = new PublishAnalyser();

        client.toAsync().subscribeWith()
                .topicFilter(responseTopicBase)
                .callback(pa)
                .send();

        Mqtt5PublishResult pubRes = client.publishWith()
                .topic("v2.0/$request")
                .responseTopic(responseTopicBase)
                .correlationData(pa.expect(this::analyseThingsResponse))
                .userProperties()
                .add("url", "v2.0/Things?$count=true")
                .add("type", "read")
                .applyUserProperties()
                .qos(MqttQos.EXACTLY_ONCE)
                .send();
        Assertions.assertFalse(pubRes.getError().isPresent(), "Request failed to send");
        Awaitility.await()
                .atMost(Durations.ONE_SECOND)
                .untilTrue(pa.allDone);
        Assertions.assertEquals(0, pa.getFailCount().get(), "Not all messages are correct.");
        client.disconnect();
    }

    public boolean analyseThingsResponse(Mqtt5Publish publish) {
        Optional<MqttUtf8String> contentType = publish.getContentType();
        Assertions.assertTrue(contentType.isPresent(), "Content type is missing on message.");
        Assertions.assertEquals(Constants.CONTENT_TYPE_APPLICATION_JSON, contentType.get().toString(), "Wrong content type on message.");

        JsonNode tree = Utils.MAPPER.readTree(publish.getPayloadAsBytes());

        JsonNode context = tree.get("@context");
        Assertions.assertNotNull(context, "Field @context is missing.");
        Assertions.assertTrue(context.isString(), "Field @context is not a string.");
        Assertions.assertEquals("v2.0/$metadata#Things", context.stringValue(), "Incorrect context url");

        JsonNode count = tree.get("@count");
        Assertions.assertNotNull(count, "Field @count is missing.");
        Assertions.assertTrue(count.isIntegralNumber(), "Field @context is not an Integral number.");
        Assertions.assertEquals(4, count.asLong(), "Incorrect context url");

        JsonNode value = tree.get("value");
        Assertions.assertNotNull(value, "Response did not contain a value field.");
        Assertions.assertTrue(value.isArray(), "Value field must be an array");

        return true;
    }

    public static final String decodePayload(Mqtt5Publish publish) {
        return new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
    }

    public static class PublishAnalyser implements Consumer<Mqtt5Publish> {

        private static final Random random = new Random();
        private final Map<BigInteger, DataAnalyser> analysers = new HashMap<>();
        private final AtomicInteger activeCount = new AtomicInteger();
        private final AtomicInteger goodCount = new AtomicInteger();
        private final AtomicInteger failCount = new AtomicInteger();
        private final AtomicInteger unexpectedCount = new AtomicInteger();
        private final AtomicBoolean allDone = new AtomicBoolean();

        public void expect(byte[] corrData, DataAnalyser analyser) {
            analysers.put(new BigInteger(corrData), analyser);
            activeCount.incrementAndGet();
            allDone.set(false);
        }

        public byte[] expect(DataAnalyser analyser) {
            byte[] corrData = randomCorrelationData(4);
            expect(corrData, analyser);
            return corrData;
        }

        public AtomicInteger getActiveCount() {
            return activeCount;
        }

        public AtomicInteger getFailCount() {
            return failCount;
        }

        public AtomicInteger getUnexpectedCount() {
            return unexpectedCount;
        }

        public AtomicBoolean getAllDone() {
            return allDone;
        }

        @Override
        public void accept(Mqtt5Publish p) {
            final Optional<ByteBuffer> correlationData = p.getCorrelationData();
            if (correlationData.isEmpty()) {
                LOGGER.error("Received a publish without correlation data: {}", p);
                unexpectedCount.incrementAndGet();
                return;
            }
            final byte[] corrData = asByteArray(correlationData.get());
            DataAnalyser analyser = analysers.get(new BigInteger(corrData));
            if (analyser == null) {
                LOGGER.error("Received a publish with unknown correlation data: {}", p);
                unexpectedCount.incrementAndGet();
                return;
            }
            try {
                if (analyser.analyse(p)) {
                    LOGGER.info("Received a publish expected data: {}", p);
                    goodCount.incrementAndGet();
                } else {
                    LOGGER.error("Received a publish unexpected data: {}", p);
                    failCount.incrementAndGet();
                }
            } catch (AssertionFailedError e) {
                // The test used assertions, and failed.
                LOGGER.info("Analyser failed: {}", e.toString(), e);
                failCount.incrementAndGet();
            }
            if (activeCount.decrementAndGet() == 0) {
                allDone.set(true);
            }
        }

        public static byte[] randomCorrelationData(int size) {
            byte[] target = new byte[size];
            random.nextBytes(target);
            return target;
        }

    }

    public static interface DataAnalyser {

        public boolean analyse(Mqtt5Publish data);
    }

    public static byte[] asByteArray(ByteBuffer byteBuffer) {
        byte[] arr = new byte[byteBuffer.remaining()];
        byteBuffer.get(arr);
        return arr;
    }

}
