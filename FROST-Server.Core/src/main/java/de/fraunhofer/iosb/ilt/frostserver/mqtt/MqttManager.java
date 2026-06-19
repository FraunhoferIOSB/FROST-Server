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
package de.fraunhofer.iosb.ilt.frostserver.mqtt;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.MQTT_TOPIC_REQUEST;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.MQTT_USER_PROPERTY_NAME_TYPE;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.MQTT_USER_PROPERTY_NAME_URL;

import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageListener;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage.Type;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.EntityCreateEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.EntityCreateListener;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.Subscription;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionFactory;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.UnknownVersionException;
import de.fraunhofer.iosb.ilt.frostserver.util.ChangingStatusLogger;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.Processor;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.ProcessorListStatus;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the matching of data changes to MQTT subscriptions.
 */
public class MqttManager implements SubscriptionListener, MessageListener, EntityCreateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttManager.class);

    /**
     * Listeners for integration-test use only. Not thread safe.
     */
    private static List<SubscriptionListener> testListeners;

    private final Map<EntityType, SubscriptionManager> subscriptions = new HashMap<>();
    private final CoreSettings settings;
    private final SubscriptionFactory subscriptionFactory;

    private MqttServer server;

    private int entityChangedQueueSize;
    private int entityCreateQueueSize;

    private BlockingQueue<EntityChangedMessage> entityChangedEventQueue;
    private ExecutorService entityChangedExecutorService;
    private final List<Processor<EntityChangedMessage>> entityChangedProcessors = new ArrayList<>();

    private BlockingQueue<EntityCreateEvent> entityCreateEventQueue;
    private ExecutorService entityCreateExecutorService;
    private final List<Processor<EntityCreateEvent>> entityCreateProcessors = new ArrayList<>();

    private final ChangingStatusLogger statusLogger = new ChangingStatusLogger(LOGGER);
    private final AtomicInteger topicCount = new AtomicInteger();
    private final AtomicInteger entityChangedQueueCount = new AtomicInteger();
    private final AtomicInteger entityCreateQueueCount = new AtomicInteger();
    private long lastChangedOverrun = 0;
    private long lastCreateOverrun = 0;

    private LoggingStatus logStatus;

    private boolean enabledMqtt = false;
    private boolean shutdown = false;

    public MqttManager(CoreSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("setting must be non-null");
        }
        this.settings = settings;
        subscriptionFactory = new SubscriptionFactory(settings);

        init();
    }

    private void init() {
        final ModelRegistry modelRegistry = settings.getModelRegistry();
        for (EntityType entityType : modelRegistry.getEntityTypes()) {
            subscriptions.put(entityType, new SubscriptionManager(entityType, this, topicCount));
        }

        MqttSettings mqttSettings = settings.getMqttSettings();
        if (mqttSettings.isEnableMqtt()) {
            enabledMqtt = true;
            shutdown = false;

            int entityChangedPoolSize = mqttSettings.getSubscribeThreadPoolSize();
            int entityCreatePoolSize = mqttSettings.getCreateThreadPoolSize();
            entityChangedQueueSize = mqttSettings.getSubscribeMessageQueueSize();
            entityCreateQueueSize = mqttSettings.getCreateMessageQueueSize();
            logStatus = new LoggingStatus(this, this::checkWorkers, settings.getMetricsSettings().isEnabled());

            entityChangedEventQueue = new ArrayBlockingQueue<>(entityChangedQueueSize);
            // start watching for EntityChangedEvents
            entityChangedExecutorService = ProcessorHelper.createProcessors(
                    entityChangedPoolSize,
                    entityChangedEventQueue,
                    this::handleEntityChangedEvent,
                    "Mqtt-EntityChangedProcessor",
                    entityChangedProcessors);
            // start watching for EntityCreateEvents
            entityCreateEventQueue = new ArrayBlockingQueue<>(entityCreateQueueSize);
            entityCreateExecutorService = ProcessorHelper.createProcessors(
                    entityCreatePoolSize,
                    entityCreateEventQueue,
                    this::handleEntityCreateEvent,
                    "Mqtt-EntityCreateProcessor",
                    entityCreateProcessors);
            // start MQTT server
            server = MqttServerFactory.get(settings);
            server.addSubscriptionListener(this);
            server.addEntityCreateListener(this);
            server.start();
            long queueLoggingInterval = settings.getSettings().getInt(CoreSettings.TAG_QUEUE_LOGGING_INTERVAL, CoreSettings.class);
            if (queueLoggingInterval > 0) {
                statusLogger
                        .setLogIntervalMs(queueLoggingInterval)
                        .addLogStatus(logStatus)
                        .start();
            }
        } else {
            enabledMqtt = false;
            entityChangedExecutorService = null;
            entityChangedEventQueue = new ArrayBlockingQueue<>(1);
            entityCreateExecutorService = null;
            entityCreateEventQueue = new ArrayBlockingQueue<>(1);
            server = null;
        }
    }

    public void shutdown() {
        shutdown = true;
        statusLogger.stop();
        ProcessorHelper.shutdownProcessors(entityChangedExecutorService, entityChangedEventQueue, 10, TimeUnit.SECONDS);
        ProcessorHelper.shutdownProcessors(entityCreateExecutorService, entityCreateEventQueue, 10, TimeUnit.SECONDS);
        if (server != null) {
            server.stop();
        }
    }

    private void handleEntityChangedEvent(EntityChangedMessage message) {
        logStatus.setEntityChangedQueueCount(entityChangedQueueCount.decrementAndGet());
        final EntityChangedMessage.Type eventType = message.getEventType();
        EntityType entityType = message.getEntityType();
        LOGGER.trace("Received a {} message for a {}.", eventType, entityType);
        if (eventType == EntityChangedMessage.Type.DELETE) {
            // v1.0 does not do delete notification.
            return;
        }
        // check if there is any subscription, if not do not publish at all
        if (!subscriptions.containsKey(entityType)) {
            return;
        }

        Entity entity = message.getEntity();
        Set<Property> fields = message.getFields();
        try (PersistenceManager persistenceManager = PersistenceManagerFactory.getInstance(settings).create()) {
            subscriptions.get(entityType)
                    .handleEntityChanged(persistenceManager, entity, message.getEventType(), fields);
        } catch (Exception ex) {
            LOGGER.error("error handling MQTT subscriptions", ex);
        }
    }

    public void notifySubscription(Subscription subscription, Entity entity, Type changeType) {
        final String topic = subscription.getTopic();
        try {
            String payload = subscription.formatMessage(entity);
            Map<String, String> userProps = new HashMap<>();
            userProps.put("type", changeType.label);
            server.publish(topic, payload, settings.getMqttSettings().getQosLevel(), null, userProps, null);
        } catch (IOException ex) {
            LOGGER.error("publishing to MQTT on topic '{}' failed", topic, ex);
        }
    }

    private void handleEntityCreateEvent(EntityCreateEvent e) {
        logStatus.setEntityCreateQueueCount(entityCreateQueueCount.decrementAndGet());
        final String topic = e.getTopic();
        final Version version;
        try {
            version = getVersionFromTopic(settings, topic);
        } catch (UnknownVersionException ex) {
            LOGGER.info("received message on topic '{}' which contains no version info.", topic);
            return;
        }
        final String path = topic.replaceFirst(version.urlPart, "");

        try (Service service = new Service(settings)) {
            final ServiceResponseDefault serviceResponse = new ServiceResponseDefault();
            if (path.equals(MQTT_TOPIC_REQUEST)) {
                String url = e.getUserProperty(MQTT_USER_PROPERTY_NAME_URL);
                LOGGER.info("Original url:  {}", url);
                url = StringUtils.removeStart(url, '/');
                url = Strings.CS.removeStart(url, version.urlPart);
                if (!url.startsWith("/")) {
                    url = '/' + url;
                }
                LOGGER.info("Rewritten url: {}", url);
                RequestTypeUtils.Type_23019 type = RequestTypeUtils.Type_23019.of(e.getUserProperty(MQTT_USER_PROPERTY_NAME_TYPE));

                final ServiceRequest serviceRequest = new ServiceRequest()
                        .setCoreSettings(settings)
                        .setVersion(version)
                        .setRequestType(type.requestType)
                        .setContentType(e.getContentType())
                        .setContent(e.getPayload())
                        .setUrlPath(url)
                        .setUserPrincipal(e.getPrincipal());

                try {
                    ServiceRequest.setLocalRequest(serviceRequest);
                    service.execute(serviceRequest, serviceResponse);
                } finally {
                    ServiceRequest.removeLocalRequest();
                }
                final String responseTopic = e.getResponseTopic();
                if (!StringHelper.isNullOrEmpty(responseTopic)) {
                    Map<String, String> responseProps = new HashMap<>();
                    responseProps.put("status", Integer.toString(serviceResponse.getCode()));
                    server.publish(responseTopic, serviceResponse.getFormattedResult(), 2, serviceResponse.getContentType(), responseProps, e.getCorrelationData());
                }
                if (!serviceResponse.isSuccessful()) {
                    LOGGER.info("Failed to execute request (topic: {}, url: {}, payload: {}, code: {}, message: {})",
                            topic, url, e.getPayload(), serviceResponse.getCode(), serviceResponse.getMessage());
                }
            } else {
                final ServiceRequest serviceRequest = new ServiceRequest()
                        .setCoreSettings(settings)
                        .setVersion(version)
                        .setRequestType(RequestTypeUtils.CREATE)
                        .setContent(e.getPayload())
                        .setUrlPath(path)
                        .setUserPrincipal(e.getPrincipal());
                try {
                    ServiceRequest.setLocalRequest(serviceRequest);
                    service.execute(serviceRequest, serviceResponse);
                } finally {
                    ServiceRequest.removeLocalRequest();
                }
                if (!serviceResponse.isSuccessful()) {
                    LOGGER.info("Creating entity via MQTT failed (topic: {}, payload: {}, code: {}, message: {})",
                            topic, e.getPayload(), serviceResponse.getCode(), serviceResponse.getMessage());
                }
            }
        }
    }

    private void entityChanged(EntityChangedMessage e) {
        if (shutdown || !enabledMqtt) {
            return;
        }
        if (entityChangedEventQueue.offer(e)) {
            logStatus.setEntityChangedQueueCount(entityChangedQueueCount.incrementAndGet());
        } else {
            logStatus.addChangedOverrun();
            long now = System.currentTimeMillis();
            if (now - lastChangedOverrun > 200) {
                lastChangedOverrun = now;
                LOGGER.warn(
                        "EntityChangedevent discarded because message queue is full {}! Increase mqtt.SubscribeMessageQueueSize and/or mqtt.SubscribeThreadPoolSize.",
                        entityChangedEventQueue.size());
            }
        }
    }

    @Override
    public void onSubscribe(SubscriptionEvent e) {
        Subscription subscription = subscriptionFactory.get(e.getTopic());
        if (subscription == null) {
            // Not a valid topic.
            return;
        }

        subscriptions.get(subscription.getEntityType())
                .addSubscription(subscription);
        logStatus.setTopicCount(topicCount.get());
        fireTestSubscriptionAdded(e);
    }

    @Override
    public void onUnsubscribe(SubscriptionEvent e) {
        Subscription subscription = subscriptionFactory.get(e.getTopic());
        if (subscription == null) {
            // Not a valid topic.
            return;
        }
        subscriptions.get(subscription.getEntityType())
                .removeSubscription(subscription);
        logStatus.setTopicCount(topicCount.get());
    }

    @Override
    public void messageReceived(EntityChangedMessage message) {
        entityChanged(message);
    }

    @Override
    public void onEntityCreate(EntityCreateEvent e) {
        if (shutdown || !enabledMqtt) {
            return;
        }
        if (entityCreateEventQueue.offer(e)) {
            logStatus.setEntityCreateQueueCount(entityCreateQueueCount.incrementAndGet());
        } else {
            logStatus.addCreateOverrun();
            long now = System.currentTimeMillis();
            if (now - lastCreateOverrun > 200) {
                lastCreateOverrun = now;
                LOGGER.warn(
                        "EntityCreateEvent discarded because message queue is full {}! Increase mqtt.SubscribeMessageQueueSize and/or mqtt.SubscribeThreadPoolSize",
                        entityCreateEventQueue.size());
            }
        }
    }

    private void checkWorkers() {
        Instant threshold = Instant.now().minus(2, ChronoUnit.SECONDS);
        ProcessorListStatus cngStatus = ProcessorHelper.checkStatus(entityChangedProcessors, threshold);
        ProcessorListStatus crtStatus = ProcessorHelper.checkStatus(entityCreateProcessors, threshold);

        logStatus.setEntityChangedWaiting(cngStatus.countWaiting())
                .setEntityChangedWorking(cngStatus.countWorking())
                .setEntityChangedBad(cngStatus.countBroken())
                .setEntityCreateWaiting(crtStatus.countWaiting())
                .setEntityCreateWorking(crtStatus.countWorking())
                .setEntityCreateBad(crtStatus.countBroken());
    }

    public static Version getVersionFromTopic(CoreSettings settings, String topic) throws UnknownVersionException {
        int pos = topic.indexOf('/');
        if (pos == -1) {
            throw new UnknownVersionException("Could not find version in topic " + topic);
        }
        String versionString = topic.substring(0, pos);
        Version version = settings.getPluginManager().getVersion(versionString);
        if (version == null) {
            throw new UnknownVersionException("Could not find version in topic " + topic);
        }
        return version;
    }

    private static class LoggingStatus extends ChangingStatusLogger.ChangingStatusDefault {

        public static final String MESSAGE = "entityCreateQueue: {} [{}, {}, {}] entityChangedQueue: {} [{}, {}, {}] topics: {}";

        public static final String CHANGED = "Changed";
        public static final String CREATE = "Create";

        public static final String DEAD = "Dead";
        public static final String WORKING = "Working";
        public static final String WAITING = "Waiting";

        public final Object[] status;
        private final Runnable processor;

        private int changedQueueCountMax = 0;
        private int createQueueCountMax = 0;

        private boolean metrics;
        private Counter queueOverrunCounter;
        private CounterDataPoint queueOverrunCreate;
        private CounterDataPoint queueOverrunChanged;
        private Gauge topicCount;

        public LoggingStatus(MqttManager parent, Runnable processor, boolean metrics) {
            super(MESSAGE, new Object[9]);
            status = getCurrentParams();
            Arrays.setAll(status, (int i) -> 0);
            this.processor = processor;
            this.metrics = metrics;
            if (metrics) {
                initMetrics(parent);
            }
        }

        private void initMetrics(MqttManager parent) {
            GaugeWithCallback.builder()
                    .name("mqtt_manager_queue_fill")
                    .help("Fill level of the Queue (0 - 1)")
                    .labelNames("queue_name")
                    .callback(cb -> {
                        cb.call((1.0 * (Integer) status[0] / parent.entityChangedQueueSize), CHANGED);
                        cb.call((1.0 * (Integer) status[4] / parent.entityCreateQueueSize), CREATE);
                    })
                    .register();
            GaugeWithCallback.builder()
                    .name("mqtt_manager_queue_fill_max")
                    .help("Maximum fill level of the Queue since last call (0 - 1)")
                    .labelNames("queue_name")
                    .callback(cb -> {
                        cb.call(1.0 * changedQueueCountMax / parent.entityChangedQueueSize, CHANGED);
                        changedQueueCountMax = 0;
                        cb.call(1.0 * createQueueCountMax / parent.entityCreateQueueSize, CREATE);
                        createQueueCountMax = 0;
                    })
                    .register();
            GaugeWithCallback.builder()
                    .name("mqtt_manager_worker_status")
                    .help("Overview of what workers do")
                    .labelNames("queue_name", "worker_status")
                    .callback(cb -> {
                        process();
                        cb.call((Integer) status[1], CHANGED, WAITING);
                        cb.call((Integer) status[2], CHANGED, WORKING);
                        cb.call((Integer) status[3], CHANGED, DEAD);
                        cb.call((Integer) status[5], CREATE, WAITING);
                        cb.call((Integer) status[6], CREATE, WORKING);
                        cb.call((Integer) status[7], CREATE, DEAD);
                    })
                    .register();

            queueOverrunCounter = Counter.builder()
                    .name("mqtt_manager_queue_overruns")
                    .help("Number of items dropped because the queue was full")
                    .labelNames("queue_name")
                    .register();
            queueOverrunCounter.initLabelValues(CREATE);
            queueOverrunCounter.initLabelValues(CHANGED);
            queueOverrunCreate = queueOverrunCounter.labelValues(CREATE);
            queueOverrunChanged = queueOverrunCounter.labelValues(CHANGED);

            topicCount = Gauge.builder()
                    .name("mqtt_manager_topics")
                    .help("Number of distinct topics that have subscriptions.")
                    .register();
        }

        @Override
        public void process() {
            processor.run();
        }

        public LoggingStatus setEntityCreateQueueCount(int count) {
            status[0] = count;
            if (metrics && count > createQueueCountMax) {
                createQueueCountMax = count;
            }
            return this;
        }

        public LoggingStatus setEntityCreateWaiting(Integer size) {
            status[1] = size;
            return this;
        }

        public LoggingStatus setEntityCreateWorking(Integer size) {
            status[2] = size;
            return this;
        }

        public LoggingStatus setEntityCreateBad(Integer size) {
            status[3] = size;
            return this;
        }

        public void addCreateOverrun() {
            if (metrics) {
                queueOverrunCreate.inc();
            }
        }

        public LoggingStatus setEntityChangedQueueCount(int count) {
            status[4] = count;
            if (metrics && count > changedQueueCountMax) {
                changedQueueCountMax = count;
            }
            return this;
        }

        public LoggingStatus setEntityChangedWaiting(Integer size) {
            status[5] = size;
            return this;
        }

        public LoggingStatus setEntityChangedWorking(Integer size) {
            status[6] = size;
            return this;
        }

        public LoggingStatus setEntityChangedBad(Integer size) {
            status[7] = size;
            return this;
        }

        public void addChangedOverrun() {
            if (metrics) {
                queueOverrunChanged.inc();
            }
        }

        public LoggingStatus setTopicCount(int count) {
            status[8] = count;
            if (metrics) {
                topicCount.set(count);
            }
            return this;
        }

    }

    private static void fireTestSubscriptionAdded(SubscriptionEvent s) {
        if (testListeners == null) {
            return;
        }
        for (SubscriptionListener l : testListeners) {
            l.onSubscribe(s);
        }

    }

    /**
     * For test use only.
     *
     * @param l the listener to add.
     */
    public static void addTestSubscriptionListener(SubscriptionListener l) {
        if (testListeners == null) {
            testListeners = new ArrayList<>();
        }
        testListeners.add(l);
    }

    /**
     * For test use only.
     *
     * @param l the listener to remove.
     */
    public static void removeTestSubscriptionListener(SubscriptionListener l) {
        if (testListeners == null) {
            return;
        }
        testListeners.remove(l);
    }

    /**
     * For test use only.
     */
    public static void clearTestSubscriptionListeners() {
        testListeners = null;
    }

}
