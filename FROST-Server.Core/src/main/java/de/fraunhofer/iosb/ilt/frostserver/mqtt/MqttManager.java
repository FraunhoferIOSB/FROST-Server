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
package de.fraunhofer.iosb.ilt.frostserver.mqtt;

import static de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.Processor.Status.WAITING;
import static de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.Processor.Status.WORKING;

import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageListener;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
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
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.UnknownVersionException;
import de.fraunhofer.iosb.ilt.frostserver.util.ChangingStatusLogger;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper.Processor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Jacoby
 * @author scf
 */
public class MqttManager implements SubscriptionListener, MessageListener, EntityCreateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttManager.class);

    private final Map<EntityType, SubscriptionManager> subscriptions = new HashMap<>();
    private final CoreSettings settings;
    private final SubscriptionFactory subscriptionFactory;

    private MqttServer server;
    private BlockingQueue<EntityChangedMessage> entityChangedEventQueue;
    private ExecutorService entityChangedExecutorService;
    private final List<Processor<EntityChangedMessage>> entityChangedProcessors = new ArrayList<>();

    private BlockingQueue<EntityCreateEvent> entityCreateEventQueue;
    private ExecutorService entityCreateExecutorService;
    private final List<Processor<EntityCreateEvent>> entityCreateProcessors = new ArrayList<>();

    private final ChangingStatusLogger statusLogger = new ChangingStatusLogger(LOGGER);
    private final AtomicInteger topicCount = new AtomicInteger();
    private final AtomicInteger entityChangedQueueSize = new AtomicInteger();
    private final AtomicInteger entityCreateQueueSize = new AtomicInteger();
    private final LoggingStatus logStatus = new LoggingStatus(this::checkWorkers);

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
            entityChangedEventQueue = new ArrayBlockingQueue<>(mqttSettings.getSubscribeMessageQueueSize());
            // start watching for EntityChangedEvents
            entityChangedExecutorService = ProcessorHelper.createProcessors(
                    mqttSettings.getSubscribeThreadPoolSize(),
                    entityChangedEventQueue,
                    this::handleEntityChangedEvent,
                    "Mqtt-EntityChangedProcessor",
                    entityChangedProcessors);
            // start watching for EntityCreateEvents
            entityCreateEventQueue = new ArrayBlockingQueue<>(mqttSettings.getCreateMessageQueueSize());
            entityCreateExecutorService = ProcessorHelper.createProcessors(
                    mqttSettings.getCreateThreadPoolSize(),
                    entityCreateEventQueue,
                    this::handleEntityCreateEvent,
                    "Mqtt-EntityCreateProcessor",
                    entityCreateProcessors);
            // start MQTT server
            server = MqttServerFactory.getInstance().get(settings);
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
        logStatus.setEntityChangedQueueSize(entityChangedQueueSize.decrementAndGet());
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
            subscriptions.get(entityType).handleEntityChanged(persistenceManager, entity, fields);
        } catch (Exception ex) {
            LOGGER.error("error handling MQTT subscriptions", ex);
        }
    }

    public void notifySubscription(Subscription subscription, Entity entity) {
        final String topic = subscription.getTopic();
        try {
            String payload = subscription.formatMessage(entity);
            server.publish(topic, payload, settings.getMqttSettings().getQosLevel());
        } catch (IOException ex) {
            LOGGER.error("publishing to MQTT on topic '{}' failed", topic, ex);
        }
    }

    private void handleEntityCreateEvent(EntityCreateEvent e) {
        logStatus.setEntityCreateQueueSize(entityCreateQueueSize.decrementAndGet());
        final String topic = e.getTopic();
        final Version version;
        try {
            version = getVersionFromTopic(settings, topic);
        } catch (UnknownVersionException ex) {
            LOGGER.info("received message on topic '{}' which contains no version info.", topic);
            return;
        }

        final String url = topic.replaceFirst(version.urlPart, "");
        try (Service service = new Service(settings)) {
            final ServiceResponseDefault serviceResponse = new ServiceResponseDefault();
            final ServiceRequest serviceRequest = new ServiceRequestBuilder(settings, version)
                    .withRequestType(RequestTypeUtils.CREATE)
                    .withContent(e.getPayload())
                    .withUrlPath(url)
                    .withUserPrincipal(e.getPrincipal())
                    .build();
            ServiceRequest.setLocalRequest(serviceRequest);
            service.execute(serviceRequest, serviceResponse);
            ServiceRequest.removeLocalRequest();
            if (!serviceResponse.isSuccessful()) {
                LOGGER.error("Creating entity via MQTT failed (topic: {}, payload: {}, code: {}, message: {})",
                        topic, e.getPayload(), serviceResponse.getCode(), serviceResponse.getMessage());
            }
        }
    }

    private void entityChanged(EntityChangedMessage e) {
        if (shutdown || !enabledMqtt) {
            return;
        }
        if (entityChangedEventQueue.offer(e)) {
            logStatus.setEntityChangedQueueSize(entityChangedQueueSize.incrementAndGet());
        } else {
            LOGGER.warn("EntityChangedevent discarded because message queue is full {}! Increase mqtt.SubscribeMessageQueueSize and/or mqtt.SubscribeThreadPoolSize.", entityChangedEventQueue.size());
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
            logStatus.setEntityCreateQueueSize(entityCreateQueueSize.incrementAndGet());
        } else {
            LOGGER.warn("EntityCreateEvent discarded because message queue is full {}! Increase mqtt.SubscribeMessageQueueSize and/or mqtt.SubscribeThreadPoolSize", entityCreateEventQueue.size());
        }
    }

    private void checkWorkers() {
        int cngWaiting = 0;
        int cngWorking = 0;
        int cngBroken = 0;
        int crtWaiting = 0;
        int crtWorking = 0;
        int crtBroken = 0;
        Instant threshold = Instant.now().minus(2, ChronoUnit.SECONDS);
        for (Processor<EntityChangedMessage> processor : entityChangedProcessors) {
            switch (processor.getStatus()) {
                case WAITING:
                    cngWaiting++;
                    break;

                case WORKING:
                    if (!processor.isFine(threshold)) {
                        cngBroken++;
                    } else {
                        cngWorking++;
                    }
                    break;

                default:
                    LOGGER.trace("Worker not started.");
            }
        }
        for (Processor<EntityCreateEvent> processor : entityCreateProcessors) {
            switch (processor.getStatus()) {
                case WAITING:
                    crtWaiting++;
                    break;

                case WORKING:
                    if (!processor.isFine(threshold)) {
                        crtBroken++;
                    } else {
                        crtWorking++;
                    }
                    break;

                default:
                    LOGGER.trace("Worker not started.");
            }
        }
        logStatus.setEntityChangedWaiting(cngWaiting)
                .setEntityChangedWorking(cngWorking)
                .setEntityChangedBad(cngBroken)
                .setEntityCreateWaiting(crtWaiting)
                .setEntityCreateWorking(crtWorking)
                .setEntityCreateBad(crtBroken);
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
        public final Object[] status;
        private final Runnable processor;

        public LoggingStatus(Runnable processor) {
            super(MESSAGE, new Object[9]);
            status = getCurrentParams();
            Arrays.setAll(status, (int i) -> 0);
            this.processor = processor;
        }

        @Override
        public void process() {
            processor.run();
        }

        public LoggingStatus setEntityCreateQueueSize(Integer size) {
            status[0] = size;
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

        public LoggingStatus setEntityChangedQueueSize(Integer size) {
            status[4] = size;
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

        public LoggingStatus setTopicCount(Integer count) {
            status[8] = count;
            return this;
        }

    }
}
