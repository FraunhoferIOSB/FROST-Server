/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.settings.BusSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;
import de.fraunhofer.iosb.ilt.frostserver.util.ProcessorHelper;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message bus implementation for in-JVM use.
 *
 * @author scf
 */
public class InternalMessageBus implements MessageBus, ConfigDefaults {

    @DefaultValueInt(2)
    public static final String TAG_WORKER_COUNT = "workerPoolSize";
    @DefaultValueInt(100)
    public static final String TAG_QUEUE_SIZE = "queueSize";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalMessageBus.class);

    private BlockingQueue<EntityChangedMessage> entityChangedMessageQueue;
    private ExecutorService entityChangedExecutorService;
    private final List<MessageListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void init(CoreSettings settings) {
        BusSettings busSettings = settings.getBusSettings();
        Settings customSettings = busSettings.getCustomSettings();
        int poolSize = customSettings.getInt(TAG_WORKER_COUNT, defaultValueInt(TAG_WORKER_COUNT));
        int queueSize = customSettings.getInt(TAG_QUEUE_SIZE, defaultValueInt(TAG_QUEUE_SIZE));

        entityChangedMessageQueue = new ArrayBlockingQueue<>(queueSize);
        entityChangedExecutorService = ProcessorHelper.createProcessors(
                poolSize,
                entityChangedMessageQueue,
                this::handleMessage,
                "IntBusPrc");
    }

    @Override
    public void stop() {
        entityChangedExecutorService.shutdown();
        try {
            if (entityChangedExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
                return;
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for shutdown.", ex);
            Thread.currentThread().interrupt();
        }
        List<Runnable> list = entityChangedExecutorService.shutdownNow();
        LOGGER.warn("There were {} messages left on the queue.", list.size());
    }

    @Override
    public void sendMessage(EntityChangedMessage message) {
        Entity entity = message.getEntity();
        EntityType entityType = entity.getEntityType();
        // We directly hand the entity on without serialization step.
        // The receivers expect the navigation entities to not be exportable.
        for (NavigationProperty property : entityType.getNavigationEntities()) {
            Object parentObject = entity.getProperty(property);
            if (parentObject instanceof Entity) {
                Entity parentEntity = (Entity) parentObject;
                parentEntity.setExportObject(false);
            }
        }
        if (!entityChangedMessageQueue.offer(message)) {
            LOGGER.error("Failed to add message to queue. Increase the queue size to allow a bigger buffer, or increase the worker pool size to empty the buffer quicker.");
        }
    }

    @Override
    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }

    private void handleMessage(EntityChangedMessage message) {
        for (MessageListener listener : listeners) {
            try {
                listener.messageReceived(message);
            } catch (Exception ex) {
                LOGGER.error("Listener threw exception on message reception.", ex);
            }
        }
    }
}
