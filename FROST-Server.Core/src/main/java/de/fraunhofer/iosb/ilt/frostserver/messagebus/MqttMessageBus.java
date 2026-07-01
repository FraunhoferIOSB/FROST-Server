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
package de.fraunhofer.iosb.ilt.frostserver.messagebus;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_BUS;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.BusSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.ChangingStatusLogger;
import de.fraunhofer.iosb.ilt.frostserver.util.MetricsSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.Processor;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.ProcessorListStatus;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UnknownEntityTypeException;
import de.fraunhofer.iosb.ilt.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.settings.Settings;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueInt;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

/**
 * A message bus implementation for out-of-JVM use. This uses an MQTT broker as
 * message bus.
 */
public class MqttMessageBus implements MessageBus, ConfigDefaults {

    @DefaultValueInt(2)
    public static final String TAG_SEND_WORKER_COUNT = "sendWorkerPoolSize";
    @DefaultValueInt(2)
    public static final String TAG_RECV_WORKER_COUNT = "recvWorkerPoolSize";
    @DefaultValueInt(100)
    public static final String TAG_SEND_QUEUE_SIZE = "sendQueueSize";
    @DefaultValueInt(100)
    public static final String TAG_RECV_QUEUE_SIZE = "recvQueueSize";
    @DefaultValue("tcp://127.0.0.1:1884")
    public static final String TAG_MQTT_BROKER = "mqttBroker";
    @DefaultValue("FROST-Bus")
    public static final String TAG_TOPIC_NAME = "topicName";
    @DefaultValueInt(2)
    public static final String TAG_QOS_LEVEL = "qosLevel";
    @DefaultValueInt(50)
    public static final String TAG_MAX_IN_FLIGHT = "maxInFlight";

    @DefaultValueInt(10)
    public static final String TAG_CONNECT_TIMEOUT_SECONDS = "connectTimeout";

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttMessageBus.class);

    private int sendPoolSize;
    private int sendQueueSize;
    private int recvPoolSize;
    private int recvQueueSize;

    private int connectTimeoutSeconds;

    private final String clientId = "FROST-MQTT-Bus-" + UUID.randomUUID();

    private BlockingQueue<EntityChangedMessage> sendQueue;
    private ExecutorService sendService;
    private final List<Processor<EntityChangedMessage>> sendProcessors = new ArrayList<>();

    private BlockingQueue<EntityChangedMessage> recvQueue;
    private ExecutorService recvService;
    private final List<Processor<EntityChangedMessage>> recvProcessors = new ArrayList<>();

    private ScheduledExecutorService maintenanceTimer;
    private final List<MessageListener> listeners = new CopyOnWriteArrayList<>();

    private final ChangingStatusLogger statusLogger = new ChangingStatusLogger(LOGGER);
    private final AtomicInteger recvQueueCount = new AtomicInteger();
    private final AtomicInteger sendQueueCount = new AtomicInteger();
    private long lastSendOverrun = 0;
    private long lastRecvOverrun = 0;
    private LoggingStatus logStatus;

    private URI brokerUri;
    private Mqtt5AsyncClient client;
    private String topicName;
    private MqttQos qosLevel;
    private int maxInFlight;
    private boolean listening = false;

    private ObjectMapper formatter;
    private JsonReaderDefault parser;

    @Override
    public void init(CoreSettings settings) {
        BusSettings busSettings = settings.getBusSettings();
        Settings customSettings = busSettings.getCustomSettings();
        MetricsSettings metricsSettings = settings.getMetricsSettings();

        sendPoolSize = customSettings.getInt(TAG_SEND_WORKER_COUNT, getClass());
        sendQueueSize = customSettings.getInt(TAG_SEND_QUEUE_SIZE, getClass());
        recvPoolSize = customSettings.getInt(TAG_RECV_WORKER_COUNT, getClass());
        recvQueueSize = customSettings.getInt(TAG_RECV_QUEUE_SIZE, getClass());
        logStatus = new LoggingStatus(this, this::checkWorkers, metricsSettings.isEnabled());

        connectTimeoutSeconds = customSettings.getInt(TAG_CONNECT_TIMEOUT_SECONDS, getClass());

        sendQueue = new ArrayBlockingQueue<>(sendQueueSize);
        sendService = ProcessorHelper.createProcessors(
                sendPoolSize,
                sendQueue,
                this::handleMessageSent,
                "mqtt-BusS",
                sendProcessors);

        recvQueue = new ArrayBlockingQueue<>(recvQueueSize);
        recvService = ProcessorHelper.createProcessors(
                recvPoolSize,
                recvQueue,
                this::handleReceivedMessage,
                "mqtt-BusR",
                recvProcessors);

        String brokerString = customSettings.get(TAG_MQTT_BROKER, getClass());
        try {
            brokerUri = new URI(brokerString);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Failed to convert to URI", ex);
        }
        topicName = customSettings.get(TAG_TOPIC_NAME, getClass());
        int qosLevelInt = customSettings.getInt(TAG_QOS_LEVEL, getClass());
        qosLevel = MqttQos.fromCode(qosLevelInt);
        if (qosLevel == null) {
            qosLevel = MqttQos.EXACTLY_ONCE;
        }
        maxInFlight = customSettings.getInt(TAG_MAX_IN_FLIGHT, getClass());
        connect();

        formatter = JsonWriter.getObjectMapper();
        final ModelRegistry modelRegistry = settings.getModelRegistry();
        parser = new JsonReaderDefault(modelRegistry, Version.INTERNAL, true);

        long queueLoggingInterval = settings.getSettings().getInt(CoreSettings.TAG_QUEUE_LOGGING_INTERVAL, CoreSettings.class);
        if (queueLoggingInterval > 0) {
            statusLogger
                    .setLogIntervalMs(queueLoggingInterval)
                    .addLogStatus(logStatus)
                    .start();
        }
        maintenanceTimer = Executors.newSingleThreadScheduledExecutor();
        maintenanceTimer.scheduleWithFixedDelay(this::connect, 60, 20, TimeUnit.SECONDS);
    }

    public void connectComplete(MqttClientConnectedContext context) {
        LOGGER.info("Connected to MQTT message bus.");
        if (!listeners.isEmpty()) {
            startListening();
        }
    }

    private synchronized void connect() {
        final String host = brokerUri.getHost();
        final int port = brokerUri.getPort();
        if (client == null) {
            LOGGER.info("Creating new hivemq-client for host: {}:{} with client-id {}", host, port, clientId);
            client = Mqtt5Client.builder()
                    .identifier(clientId)
                    .serverHost(host)
                    .serverPort(port)
                    .automaticReconnectWithDefaultConfig()
                    .addConnectedListener(this::connectComplete)
                    .addDisconnectedListener(this::connectionLost)
                    .transportConfig()
                    .socketConnectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                    .mqttConnectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                    .applyTransportConfig()
                    .buildAsync();
            client.publishes(MqttGlobalPublishFilter.ALL, this::messageArrived);
        }
        if (!client.getState().isConnected()) {
            try {
                LOGGER.info("hivemq-client connecting to broker: {}:{} with client-id {}", host, port, clientId);
                client.connectWith()
                        .cleanStart(false)
                        .keepAlive(30)
                        .restrictions()
                        .receiveMaximum(maxInFlight)
                        .sendMaximum(maxInFlight)
                        .applyRestrictions()
                        .send();
            } catch (RuntimeException ex) {
                LOGGER.error("Failed to connect to broker: {}", brokerUri);
                LOGGER.error("", ex);
            }
        }

    }

    private synchronized void disconnect() {
        listening = false;
        if (client == null) {
            return;
        }
        if (client.getState().isConnected()) {
            try {
                LOGGER.info("hivemq-client disconnecting from broker: {}", brokerUri);
                client.toBlocking()
                        .disconnectWith()
                        .send();
            } catch (RuntimeException ex) {
                LOGGER.error("Exception disconnecting client.", ex);
            }
        }
        client = null;
    }

    private synchronized void startListening() {
        try {
            if (client == null || !client.getState().isConnected()) {
                connect();
            }
            LOGGER.info("hivemq-client subscribing to topic: {}", topicName);
            client.subscribeWith()
                    .topicFilter(topicName)
                    .qos(qosLevel)
                    .send()
                    .thenApply((ack) -> {
                        switch (ack.getReasonCodes().getFirst()) {
                            case GRANTED_QOS_0:
                            case GRANTED_QOS_1:
                            case GRANTED_QOS_2:
                                LOGGER.info("hivemq-client succesfully subscribed to topic: {}", topicName);
                                listening = true;
                                break;

                            default:
                                LOGGER.warn("hivemq-client failed to subscribe to topic: {}. Reason: {}", topicName, ack);
                                listening = false;
                        }
                        return null;
                    });
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to start listening, removing client.", ex);
            Mqtt5AsyncClient tempclient = client;
            client = null;
            try {
                tempclient.disconnect();
            } catch (RuntimeException ex1) {
                // nothing further to do.
            }
        }
    }

    private synchronized void stopListening() {
        try {
            LOGGER.info("hivemq-client unsubscribing from topic: {}", topicName);
            client.toBlocking()
                    .unsubscribeWith()
                    .topicFilter(topicName)
                    .send();
            listening = false;
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to stop listening.", ex);
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Message bus shutting down.");
        stopListening();
        disconnect();
        if (maintenanceTimer != null) {
            maintenanceTimer.shutdownNow();
        }
        ProcessorHelper.shutdownProcessors(sendService, sendQueue, 10, TimeUnit.SECONDS);
        ProcessorHelper.shutdownProcessors(recvService, recvQueue, 10, TimeUnit.SECONDS);
        statusLogger.stop();
        LOGGER.info("Message bus closed.");
    }

    @Override
    public void sendMessage(EntityChangedMessage message) {
        if (sendQueue.offer(message)) {
            logStatus.setSendQueueCount(sendQueueCount.incrementAndGet());
        } else {
            logStatus.addSendOverrun();
            long now = System.currentTimeMillis();
            if (now - lastSendOverrun > 200) {
                lastSendOverrun = now;
                LOGGER.error(
                        "Failed to add message to send-queue. Increase {}{} (currently {}) to allow a bigger buffer, or increase {}{} (currently {}) to empty the buffer quicker.",
                        PREFIX_BUS, TAG_SEND_QUEUE_SIZE, sendQueueSize, PREFIX_BUS, TAG_SEND_WORKER_COUNT, sendPoolSize);
            }
        }
    }

    @Override
    public synchronized void addMessageListener(MessageListener listener) {
        listeners.add(listener);
        if (!listening) {
            startListening();
        }
    }

    @Override
    public synchronized void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            stopListening();
        }
    }

    private void handleMessageSent(EntityChangedMessage message) {
        logStatus.setSendQueueCount(sendQueueCount.decrementAndGet());
        try {
            String serialisedMessage = formatter.writeValueAsString(message);
            byte[] bytes = serialisedMessage.getBytes(StringHelper.UTF8);
            if (!client.getState().isConnected()) {
                connect();
            }
            client.publishWith()
                    .topic(topicName)
                    .payload(bytes)
                    .qos(qosLevel)
                    .retain(false)
                    .send();
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to publish message to bus.", ex);
        }
    }

    public void connectionLost(MqttClientDisconnectedContext context) {
        LOGGER.warn("Connection to message bus lost (Stacktrace in DEBUG): {}.", context.getCause().getMessage());
        LOGGER.debug("", context.getCause());
        listening = false;
    }

    public void messageArrived(Mqtt5Publish mqttMessage) {
        String serialisedEcMessage = new String(mqttMessage.getPayloadAsBytes(), StringHelper.UTF8);
        LOGGER.trace("Received: {}", serialisedEcMessage);
        EntityChangedMessage ecMessage;
        try {
            ecMessage = parser.parseObject(EntityChangedMessage.class, serialisedEcMessage);
        } catch (UnknownEntityTypeException ex) {
            LOGGER.debug("Failed to decode due to unknown entity type", ex);
            return;
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Failed to decode message from bus. Details in DEBUG.");
            LOGGER.debug("Failed to decode message: {}", serialisedEcMessage, ex);
            return;
        } catch (IOException | RuntimeException ex) {
            LOGGER.warn("Non-JSON received on bus.", ex);
            return;
        }
        if (recvQueue.offer(ecMessage)) {
            logStatus.setRecvQueueCount(recvQueueCount.incrementAndGet());
        } else {
            logStatus.addRecvOverrun();
            long now = System.currentTimeMillis();
            if (now - lastRecvOverrun > 200) {
                lastRecvOverrun = now;
                LOGGER.error(
                        "Failed to add message to receive-queue. Increase {}{} (currently {}) to allow a bigger buffer, or increase {}{} (currently {}) to empty the buffer quicker.",
                        PREFIX_BUS, TAG_RECV_QUEUE_SIZE, recvQueueSize, PREFIX_BUS, TAG_RECV_WORKER_COUNT, recvPoolSize);
            }
        }
    }

    private void handleReceivedMessage(EntityChangedMessage message) {
        logStatus.setRecvQueueCount(recvQueueCount.decrementAndGet());
        for (MessageListener listener : listeners) {
            try {
                listener.messageReceived(message);
            } catch (Exception ex) {
                LOGGER.error("Listener threw exception on message reception.", ex);
            }
        }
    }

    private void checkWorkers() {
        Instant threshold = Instant.now().minus(2, ChronoUnit.SECONDS);
        ProcessorListStatus recvStatus = ProcessorHelper.checkStatus(recvProcessors, threshold);
        ProcessorListStatus sendStatus = ProcessorHelper.checkStatus(sendProcessors, threshold);
        logStatus.setRecvWaiting(recvStatus.countWaiting())
                .setRecvWorking(recvStatus.countWorking())
                .setRecvBad(recvStatus.countBroken())
                .setSendWaiting(sendStatus.countWaiting())
                .setSendWorking(sendStatus.countWorking())
                .setSendBad(sendStatus.countBroken());
    }

    private static class LoggingStatus extends ChangingStatusLogger.ChangingStatusDefault {

        public static final String MESSAGE = "RecvQueue: {} [{}, {}, {}] SendQueue: {} [{}, {}, {}] ";
        public static final String LABEL_QUEUE_NAME = "queue_name";
        public static final String LABEL_SEND = "Send";
        public static final String LABEL_RECEIVE = "Receive";
        public static final String DEAD = "Dead";
        public static final String WORKING = "Working";
        public static final String WAITING = "Waiting";

        public final Object[] status;
        private final Runnable processor;

        private int recvQueueCountMax = 0;
        private int sendQueueCountMax = 0;

        private final boolean metrics;
        private CounterDataPoint queueOverrunRecv;
        private CounterDataPoint queueOverrunSend;

        public LoggingStatus(MqttMessageBus parent, Runnable processor, boolean metrics) {
            super(MESSAGE, new Object[8]);
            status = getCurrentParams();
            Arrays.setAll(status, (int i) -> 0);
            this.processor = processor;

            this.metrics = metrics;
            if (metrics) {
                initMetrics(parent);
            }
        }

        private void initMetrics(MqttMessageBus parent) {
            GaugeWithCallback.builder()
                    .name("message_bus_queue_fill")
                    .help("Fill level of the Queue (0 - 1)")
                    .labelNames("queue_name")
                    .callback(cb -> {
                        cb.call((1.0 * (Integer) status[0] / parent.recvQueueSize), LABEL_RECEIVE);
                        cb.call((1.0 * (Integer) status[4] / parent.sendQueueSize), LABEL_SEND);
                    })
                    .register();
            GaugeWithCallback.builder()
                    .name("message_bus_queue_fill_max")
                    .help("Maximum fill level of the Queue since last call (0 - 1)")
                    .labelNames("queue_name")
                    .callback(cb -> {
                        cb.call(1.0 * recvQueueCountMax / parent.recvQueueSize, LABEL_RECEIVE);
                        recvQueueCountMax = 0;
                        cb.call(1.0 * sendQueueCountMax / parent.sendQueueSize, LABEL_SEND);
                        sendQueueCountMax = 0;
                    })
                    .register();
            GaugeWithCallback.builder()
                    .name("message_bus_worker_status")
                    .help("Overview of what workers do")
                    .labelNames("queue_name", "worker_status")
                    .callback(cb -> {
                        process();
                        cb.call((Integer) status[1], LABEL_RECEIVE, WAITING);
                        cb.call((Integer) status[2], LABEL_RECEIVE, WORKING);
                        cb.call((Integer) status[3], LABEL_RECEIVE, DEAD);
                        cb.call((Integer) status[5], LABEL_SEND, WAITING);
                        cb.call((Integer) status[6], LABEL_SEND, WORKING);
                        cb.call((Integer) status[7], LABEL_SEND, DEAD);
                    })
                    .register();

            Counter queueOverrunCounter = Counter.builder()
                    .name("message_bus_queue_overruns")
                    .help("Number of items dropped because the queue was full")
                    .labelNames(LABEL_QUEUE_NAME)
                    .register();
            queueOverrunCounter.initLabelValues(LABEL_RECEIVE);
            queueOverrunCounter.initLabelValues(LABEL_SEND);
            queueOverrunRecv = queueOverrunCounter.labelValues(LABEL_RECEIVE);
            queueOverrunSend = queueOverrunCounter.labelValues(LABEL_SEND);
        }

        @Override
        public void process() {
            processor.run();
        }

        public LoggingStatus setRecvQueueCount(Integer count) {
            status[0] = count;
            if (metrics && count > recvQueueCountMax) {
                recvQueueCountMax = count;
            }
            return this;
        }

        public LoggingStatus setRecvWaiting(Integer size) {
            status[1] = size;
            return this;
        }

        public LoggingStatus setRecvWorking(Integer size) {
            status[2] = size;
            return this;
        }

        public LoggingStatus setRecvBad(Integer size) {
            status[3] = size;
            return this;
        }

        public void addRecvOverrun() {
            if (metrics) {
                queueOverrunRecv.inc();
            }
        }

        public LoggingStatus setSendQueueCount(Integer count) {
            status[4] = count;
            if (metrics && count > sendQueueCountMax) {
                sendQueueCountMax = count;
            }
            return this;
        }

        public LoggingStatus setSendWaiting(Integer size) {
            status[5] = size;
            return this;
        }

        public LoggingStatus setSendWorking(Integer size) {
            status[6] = size;
            return this;
        }

        public LoggingStatus setSendBad(Integer size) {
            status[7] = size;
            return this;
        }

        public void addSendOverrun() {
            if (metrics) {
                queueOverrunSend.inc();
            }
        }

    }

}
