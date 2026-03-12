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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReaderDefault;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.settings.BusSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;
import de.fraunhofer.iosb.ilt.frostserver.util.ChangingStatusLogger;
import de.fraunhofer.iosb.ilt.frostserver.util.MetricsSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.Processor;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.ProcessorListStatus;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UnknownEntityTypeException;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import java.io.IOException;
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
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message bus implementation for out-of-JVM use. This uses an MQTT broker as
 * message bus.
 */
public class MqttMessageBus implements MessageBus, MqttCallback, ConfigDefaults {

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

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttMessageBus.class);

    private int sendPoolSize;
    private int sendQueueSize;
    private int recvPoolSize;
    private int recvQueueSize;

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

    private String broker;
    private MqttClient client;
    private String topicName;
    private int qosLevel;
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
                this::handleMessageReceived,
                "mqtt-BusR",
                recvProcessors);

        broker = customSettings.get(TAG_MQTT_BROKER, getClass());
        topicName = customSettings.get(TAG_TOPIC_NAME, getClass());
        qosLevel = customSettings.getInt(TAG_QOS_LEVEL, getClass());
        maxInFlight = customSettings.getInt(TAG_MAX_IN_FLIGHT, getClass());
        connect();

        formatter = JsonWriter.getObjectMapper();
        final ModelRegistry modelRegistry = settings.getModelRegistry();
        parser = new JsonReaderDefault(modelRegistry, true);

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

    private synchronized void connect() {
        if (client == null) {
            try {
                LOGGER.info("Creating new paho-client for broker: {} with client-id {}", broker, clientId);
                client = new MqttClient(broker, clientId, new MemoryPersistence());
                client.setCallback(this);
            } catch (MqttException ex) {
                LOGGER.error("Failed to create MQTT client to connect to broker: {}", broker);
                LOGGER.error("", ex);
                return;
            }
        }
        if (!client.isConnected()) {
            try {
                LOGGER.info("paho-client connecting to broker: {} with client-id {}", broker, clientId);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setAutomaticReconnect(true);
                connOpts.setCleanSession(false);
                connOpts.setKeepAliveInterval(30);
                connOpts.setConnectionTimeout(30);
                connOpts.setMaxInflight(maxInFlight);
                client.connect(connOpts);
                LOGGER.info("paho-client connected to broker");
            } catch (MqttException ex) {
                LOGGER.error("Failed to connect to broker: {}", broker);
                LOGGER.error("", ex);
                return;
            }
            if (!listeners.isEmpty()) {
                startListening();
            }
        }

    }

    private synchronized void disconnect() {
        listening = false;
        if (client == null) {
            return;
        }
        if (client.isConnected()) {
            try {
                LOGGER.info("paho-client disconnecting from broker: {}", broker);
                client.disconnect(1000);
            } catch (MqttException ex) {
                LOGGER.error("Exception disconnecting client.", ex);
            }
        }
        try {
            LOGGER.info("paho-client closing");
            client.close();
        } catch (MqttException ex) {
            LOGGER.error("Exception closing client.", ex);
        }
        client = null;
    }

    private synchronized void startListening() {
        try {
            LOGGER.info("paho-client subscribing to topic: {}", topicName);
            if (client == null || !client.isConnected()) {
                connect();
            }
            if (!listening) {
                client.subscribeWithResponse(topicName, qosLevel).setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        listening = true;
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        listening = false;
                    }
                });
            }
        } catch (MqttException ex) {
            LOGGER.error("Failed to start listening, removing client.", ex);
            MqttClient tempclient = client;
            client = null;
            try {
                tempclient.close(true);
            } catch (MqttException | RuntimeException ex1) {
                // nothing further to do.
            }
        }
    }

    private synchronized void stopListening() {
        if (!listening) {
            return;
        }
        try {
            LOGGER.info("paho-client unsubscribing from topic: {}", topicName);
            client.unsubscribe(topicName);
            listening = false;
        } catch (MqttException ex) {
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
            if (!client.isConnected()) {
                connect();
            }
            client.publish(topicName, bytes, qosLevel, false);
        } catch (MqttException | JsonProcessingException ex) {
            LOGGER.error("Failed to publish message to bus.", ex);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        if (listening) {
            LOGGER.warn("Connection to message bus lost (Stacktrace in DEBUG): {}.", cause.getMessage());
            LOGGER.debug("", cause);
            listening = false;
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws IOException {
        String serialisedEcMessage = new String(mqttMessage.getPayload(), StringHelper.UTF8);
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

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Nothing to do...
    }

    private void handleMessageReceived(EntityChangedMessage message) {
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
