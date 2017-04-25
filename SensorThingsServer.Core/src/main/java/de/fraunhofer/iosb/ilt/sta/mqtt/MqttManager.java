/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.mqtt;

import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.mqtt.create.EntityCreateListener;
import de.fraunhofer.iosb.ilt.sta.mqtt.create.ObservationCreateEvent;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.Subscription;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionFactory;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.EntityChangeListener;
import de.fraunhofer.iosb.ilt.sta.persistence.EntityChangedEvent;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.sta.service.RequestType;
import de.fraunhofer.iosb.ilt.sta.service.Service;
import de.fraunhofer.iosb.ilt.sta.service.ServiceRequestBuilder;
import de.fraunhofer.iosb.ilt.sta.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.sta.util.ProcessorHelper;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MqttManager implements SubscriptionListener, EntityChangeListener, EntityCreateListener {

    private static MqttManager instance;
    private static final Charset ENCODING = Charset.forName("UTF-8");
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttManager.class);
    private final static ThreadGroup threadGroup = new ThreadGroup("MqttManager EntityChangedExecutorService Threads");

    public static synchronized void init(CoreSettings settings) {
        if (instance == null) {
            instance = new MqttManager(settings);
        }
    }

    public static void shutdown() {
        if (instance != null) {
            instance.doShutdown();
        }
    }

    private void doShutdown() {
        shutdown = true;
        ProcessorHelper.shutdownProcessors(entityChangedExecutorService, entityChangedEventQueue, 10, TimeUnit.SECONDS);
        ProcessorHelper.shutdownProcessors(observationCreateExecutorService, observationCreateEventQueue, 10, TimeUnit.SECONDS);
        if (server != null) {
            server.stop();
        }
    }

    public static MqttManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MqttManager is not initialized! Call init() before accessing the instance.");
        }
        return instance;
    }
    private final ConcurrentMap<EntityType, List<Subscription>> subscriptions = new ConcurrentHashMap<>();
    private final CoreSettings settings;
    private MqttServer server;
    private BlockingQueue<EntityChangedEvent> entityChangedEventQueue;
    private ExecutorService entityChangedExecutorService;
    private BlockingQueue<ObservationCreateEvent> observationCreateEventQueue;
    private ExecutorService observationCreateExecutorService;
    private boolean enabledMqtt = false;
    private boolean shutdown = false;

    private void handleEntityChangedEvent(EntityChangedEvent e) {
        // check if there is any subscription, if not do not publish at all
        if (!subscriptions.containsKey(e.getNewEntity().getEntityType())) {
            return;
        }
        PersistenceManager persistenceManager = PersistenceManagerFactory.getInstance().create();
        try {
            // for each subscription on EntityType check match
            for (Subscription subscription : subscriptions.get(e.getNewEntity().getEntityType())) {
                if (subscription.matches(persistenceManager, e.getOldEntity(), e.getNewEntity())) {
                    Entity realEntity = persistenceManager.getEntityById(settings.getServiceRootUrl(), e.getNewEntity().getEntityType(), e.getNewEntity().getId());
                    try {
                        String payload = subscription.formatMessage(realEntity);
                        server.publish(subscription.getTopic(), payload.getBytes(ENCODING), settings.getMqttSettings().getQosLevel());
                    } catch (IOException ex) {
                        LOGGER.error("publishing to MQTT on topic '" + subscription.getTopic() + "' failed", ex);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("error handling MQTT subscriptions", ex);
        } finally {
            persistenceManager.close();
        }
        return;
    }

    private void handleObservationCreateEvent(ObservationCreateEvent e) {
        // check path?
        if (!e.getTopic().endsWith("Observations")) {
            LOGGER.info("received message on topic '{}' which is no valid topic to create an observation.");
            return;
        }
        String url = e.getTopic().replaceFirst(settings.getApiVersion(), "");
        ServiceResponse<Observation> response = new Service(settings).execute(new ServiceRequestBuilder()
                .withRequestType(RequestType.Create)
                .withContent(e.getPayload())
                .withUrlPath(url)
                .build());
        if (response.isSuccessful()) {
            LOGGER.info("Observation (ID {}) created via MQTT", response.getResult().getId().getValue());
        } else {
            LOGGER.error("Creating observation via MQTT failed (topic: {}, payload: {}, code: {}, message: {})",
                    e.getTopic(), e.getPayload(), response.getCode(), response.getMessage());
        }
    }

    private MqttManager(CoreSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("setting must be non-null");
        }
        this.settings = settings;
        init();
    }

    private void init() {
        MqttSettings mqttSettings = settings.getMqttSettings();
        if (mqttSettings.isEnableMqtt()) {
            enabledMqtt = true;
            shutdown = false;
            entityChangedEventQueue = new ArrayBlockingQueue<>(mqttSettings.getSubscribeMessageQueueSize());
            // start watching for EntityChangedEvents
            entityChangedExecutorService = ProcessorHelper.createProcessors(
                    mqttSettings.getSubscribeThreadPoolSize(),
                    entityChangedEventQueue,
                    x -> handleEntityChangedEvent(x),
                    "MqttManager EntityChangedEventProcessor");
            // start watching for ObservationCreateEvents
            observationCreateEventQueue = new ArrayBlockingQueue<>(mqttSettings.getCreateMessageQueueSize());
            observationCreateExecutorService = ProcessorHelper.createProcessors(
                    mqttSettings.getCreateThreadPoolSize(),
                    observationCreateEventQueue,
                    x -> handleObservationCreateEvent(x),
                    "MqttManager ObservationCreateEventProcessor");
            // start MQTT server
            server = MqttServerFactory.getInstance().get(settings);
            server.addSubscriptionListener(this);
            server.addEntityCreateListener(this);
            server.start();

        } else {
            enabledMqtt = false;
            entityChangedExecutorService = null;
            entityChangedEventQueue = new ArrayBlockingQueue<>(1);
            observationCreateExecutorService = null;
            observationCreateEventQueue = new ArrayBlockingQueue<>(1);
            server = null;
        }

        SubscriptionFactory.init(settings);
    }

    private void entityChanged(EntityChangedEvent e) {
        if (shutdown || !enabledMqtt) {
            return;
        }
        if (!entityChangedEventQueue.offer(e)) {
            LOGGER.warn("EntityChangedevent discarded because message queue is full! Eventually the message queue size should be increased.");
        }
    }

    @Override
    public void onSubscribe(SubscriptionEvent e) {
        Subscription subscription = SubscriptionFactory.getInstance().get(e.getTopic());
        if (!subscriptions.containsKey(subscription.getEntityType())) {
            subscriptions.put(subscription.getEntityType(), new CopyOnWriteArrayList<>());
        }
        subscriptions.get(subscription.getEntityType()).add(subscription);
    }

    @Override
    public void onUnsubscribe(SubscriptionEvent e) {
        Subscription subscription = SubscriptionFactory.getInstance().get(e.getTopic());
        if (subscriptions.containsKey(subscription.getEntityType())) {
            subscriptions.get(subscription.getEntityType()).remove(subscription);
        }
    }

    @Override
    public void entityInserted(EntityChangedEvent e) {
        entityChanged(e);
    }

    @Override
    public void entityDeleted(EntityChangedEvent e) {

    }

    @Override
    public void entityUpdated(EntityChangedEvent e) {
        entityChanged(e);
    }

    @Override
    public void onObservationCreate(ObservationCreateEvent e) {
        if (shutdown || !enabledMqtt) {
            return;
        }
        if (!observationCreateEventQueue.offer(e)) {
            LOGGER.warn("ObservationCreateEvent discarded because message queue is full! Eventually the message queue size should be increased.");
        }
    }
}
