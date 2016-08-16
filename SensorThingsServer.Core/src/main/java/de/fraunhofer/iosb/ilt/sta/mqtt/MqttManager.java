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

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.Subscription;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionFactory;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.EntityChangeListener;
import de.fraunhofer.iosb.ilt.sta.persistence.EntityChangedEvent;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.MqttSettings;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MqttManager implements SubscriptionListener, EntityChangeListener {

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
        if (entityChangedExecutorService != null) {
            try {
                /**
                 * no more events can be added to the queue so simple wait till
                 * it's empty.
                 */
                while (!entityChangedEventQueue.isEmpty()) {
                    Thread.sleep(100);
                }
                entityChangedExecutorService.shutdownNow();
                if (!entityChangedExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOGGER.debug("entityChangedExecutorService did not terminate");
                }
            } catch (InterruptedException ie) {
                entityChangedExecutorService.shutdownNow();
            }
        }
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
    private final MqttServer server;
    private final BlockingQueue<EntityChangedEvent> entityChangedEventQueue;
    private final ExecutorService entityChangedExecutorService;
    private boolean shutdown = false;

    class EntityChangedEventProcessor implements Runnable {

        @Override
        public void run() {
            LOGGER.debug("starting EntityChangedEventProcessorThread");
            while (!Thread.currentThread().isInterrupted()) {
                EntityChangedEvent e;
                try {
                    e = entityChangedEventQueue.take();
                    handleEntityChangedEvent(e);
                } catch (InterruptedException ex) {
                    LOGGER.debug("EntityChangedEventProcessor interrupted", ex);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    LOGGER.warn("Exception while executing EntityChangedEventProcessor", ex);
                }
            }
            LOGGER.debug("exiting EntityChangedEventProcessorThread");
        }

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
                LOGGER.error("error handling MQTT subscriptions");
            } finally {
                persistenceManager.close();
            }
        }

    }

    private MqttManager(CoreSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("setting must be non-null");
        }
        this.settings = settings;
        MqttSettings mqttSettings = settings.getMqttSettings();
        if (mqttSettings.isEnableMqtt()) {
            shutdown = false;
            this.server = MqttServerFactory.getInstance().get(settings);
            server.addSubscriptionListener(this);
            server.start();
            entityChangedEventQueue = new ArrayBlockingQueue<>(mqttSettings.getMessageQueueSize());
            entityChangedExecutorService = Executors.newFixedThreadPool(mqttSettings.getThreadPoolSize(),
                    (Runnable r) -> new Thread(threadGroup, r, "MqttManager EntityChangedEventProcessorThread"));
            for (int i = 0; i < mqttSettings.getThreadPoolSize(); i++) {
                entityChangedExecutorService.submit(new EntityChangedEventProcessor());
            }
        } else {
            entityChangedExecutorService = null;
            entityChangedEventQueue = new ArrayBlockingQueue<>(1);
            server = null;
        }
        SubscriptionFactory.init(settings);
    }

    private void entityChanged(EntityChangedEvent e) {
        if (shutdown) {
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
}
