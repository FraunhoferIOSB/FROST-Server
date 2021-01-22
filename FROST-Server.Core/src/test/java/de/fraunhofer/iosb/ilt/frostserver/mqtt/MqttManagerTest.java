/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import com.github.fge.jsonpatch.JsonPatch;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.EntityCreateListener;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManagerLong;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.UnknownVersionException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UpgradeFailedException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MqttManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttManagerTest.class.getName());
    private static final int REPEAT_COUNT = 0;
    private static final int MESSAGE_COUNT = 20000;

    @Test
    public void testVersionParse() throws UnknownVersionException {
        Assert.assertEquals(Version.V_1_0, MqttManager.getVersionFromTopic("v1.0/Observations"));
        Assert.assertEquals(Version.V_1_1, MqttManager.getVersionFromTopic("v1.1/Observations"));
    }

    @Test(expected = UnknownVersionException.class)
    public void testVersionParseFail() throws UnknownVersionException {
        MqttManager.getVersionFromTopic("v1.9/Observations");
    }

    @Test
    public void testMqttManager() throws InterruptedException {
        Properties properties = new Properties();
        properties.put(CoreSettings.TAG_SERVICE_ROOT_URL, "http://localhost/");
        properties.put(CoreSettings.TAG_TEMP_PATH, "/tmp/");
        properties.put(CoreSettings.TAG_QUEUE_LOGGING_INTERVAL, "0");
        properties.put(CoreSettings.PREFIX_MQTT + MqttSettings.TAG_IMPLEMENTATION_CLASS, TestMqttServer.class.getName());
        properties.put(CoreSettings.PREFIX_MQTT + MqttSettings.TAG_SUBSCRIBE_MESSAGE_QUEUE_SIZE, "20000");
        properties.put(CoreSettings.PREFIX_MQTT + MqttSettings.TAG_SUBSCRIBE_THREAD_POOL_SIZE, "10");
        properties.put(CoreSettings.PREFIX_PERSISTENCE + PersistenceSettings.TAG_IMPLEMENTATION_CLASS, DummyPersistenceManager.class.getName());
        CoreSettings coreSettings = new CoreSettings(properties);
        MqttManager.init(coreSettings);
        MqttManager mqttManager = MqttManager.getInstance();
        List<TestMqttServer> mqttServers = TestMqttServerRegister.getInstance().getServers();
        Assert.assertEquals(1, mqttServers.size());

        testTopics(mqttServers, mqttManager, 100, MESSAGE_COUNT);

        for (int i = 0; i < REPEAT_COUNT; i++) {
            testTopics(mqttServers, mqttManager);
        }

    }

    private void testTopics(List<TestMqttServer> mqttServers, MqttManager mqttManager) throws InterruptedException {
        testTopics(mqttServers, mqttManager, 10, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 100, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 500, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 1000, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 2000, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 2500, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 5000, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 7500, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 10000, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 12500, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 15000, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 17500, MESSAGE_COUNT);
        testTopics(mqttServers, mqttManager, 20000, MESSAGE_COUNT);
    }

    private void testTopics(List<TestMqttServer> mqttServers, MqttManager mqttManager, int subscriptionCount, int publishCount) throws InterruptedException {
        TestMqttServer mqttServer = mqttServers.get(0);
        for (int i = 0; i < subscriptionCount; i++) {
            String topic = "v1.1/Datastreams(" + i + ")/Observations";
            mqttServer.subscribe(topic);
        }

        final CountDownLatch barrier = new CountDownLatch(publishCount);
        final AtomicInteger publishedCount = new AtomicInteger();
        mqttServer.addPublishListener((topic) -> {
            publishedCount.incrementAndGet();
            barrier.countDown();
        });

        Calendar start = Calendar.getInstance();
        int topicId = 0;
        for (int pubId = 0; pubId < publishCount; pubId++) {
            EntityChangedMessage ecm = new EntityChangedMessage()
                    .setEventType(EntityChangedMessage.Type.CREATE)
                    .setEntity(
                            new Observation(new IdLong(pubId))
                                    .setResult(pubId)
                                    .setPhenomenonTime(new TimeInstant(DateTime.now()))
                                    .setDatastream(new Datastream(new IdLong(topicId)))
                    );
            topicId++;
            if (topicId >= subscriptionCount) {
                topicId = 0;
            }
            mqttManager.messageReceived(ecm);
        }
        barrier.await(15, TimeUnit.SECONDS);
        Calendar end = Calendar.getInstance();
        LOGGER.info("subscribed to, {}, {}, {}, ms", subscriptionCount, publishedCount.get(), (end.getTimeInMillis() - start.getTimeInMillis()));
        mqttServer.unsubscribeAll();
    }

    public static final class TestMqttServerRegister {

        private static TestMqttServerRegister instance;

        private static synchronized TestMqttServerRegister getInstance() {
            if (instance == null) {
                instance = new TestMqttServerRegister();
            }
            return instance;
        }

        private final List<TestMqttServer> servers = Collections.synchronizedList(new ArrayList<>());

        private TestMqttServerRegister() {
        }

        private void add(TestMqttServer server) {
            servers.add(server);
        }

        private boolean remove(TestMqttServer server) {
            return servers.remove(server);
        }

        public List<TestMqttServer> getServers() {
            return servers;
        }

    }

    public static final class TestMqttServer implements MqttServer {

        private final List<SubscriptionListener> subscriptionListeners = new CopyOnWriteArrayList<>();
        private final List<EntityCreateListener> entityCreateListeners = new CopyOnWriteArrayList<>();
        private final List<PublishListener> publishListeners = new CopyOnWriteArrayList<>();
        private final List<String> topics = new ArrayList<>();

        @Override
        public void init(CoreSettings settings) {
            TestMqttServerRegister.getInstance().add(this);
        }

        @Override

        public void start() {
        }

        @Override
        public void stop() {
        }

        public void subscribe(String topic) {
            topics.add(topic);
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(topic);
            for (SubscriptionListener l : subscriptionListeners) {
                l.onSubscribe(subscriptionEvent);
            }
        }

        public void unsubscribe(String topic) {
            topics.remove(topic);
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(topic);
            for (SubscriptionListener l : subscriptionListeners) {
                l.onUnsubscribe(subscriptionEvent);
            }
        }

        public void unsubscribeAll() {
            for (String topic : topics) {
                SubscriptionEvent subscriptionEvent = new SubscriptionEvent(topic);
                for (SubscriptionListener l : subscriptionListeners) {
                    l.onUnsubscribe(subscriptionEvent);
                }
            }
            topics.clear();
        }

        @Override
        public void publish(String topic, byte[] payload, int qos) {
            for (PublishListener listener : publishListeners) {
                listener.publish(topic);
            }
        }

        @Override
        public void addSubscriptionListener(SubscriptionListener listener) {
            subscriptionListeners.add(listener);
        }

        @Override
        public void removeSubscriptionListener(SubscriptionListener listener) {
            subscriptionListeners.remove(listener);
        }

        @Override
        public void addEntityCreateListener(EntityCreateListener listener) {
            entityCreateListeners.add(listener);
        }

        @Override
        public void removeEntityCreateListener(EntityCreateListener listener) {
            entityCreateListeners.remove(listener);
        }

        public void addPublishListener(PublishListener listener) {
            publishListeners.add(listener);
        }

        public void removePublishListener(PublishListener listener) {
            publishListeners.remove(listener);
        }

        public void removePublishListeners() {
            publishListeners.clear();
        }

    }

    public static interface PublishListener {

        public void publish(String topic);
    }

    public static final class DummyPersistenceManager implements PersistenceManager {

        private CoreSettings coreSettings;
        private final IdManager idManager = new IdManagerLong();

        @Override
        public IdManager getIdManager() {
            return idManager;
        }

        @Override
        public boolean validatePath(ResourcePath path) {
            return true;
        }

        @Override
        public boolean insert(Entity entity) throws NoSuchEntityException, IncompleteEntityException {
            return true;
        }

        @Override
        public Entity get(EntityType entityType, Id id) {
            return null;
        }

        @Override
        public Object get(ResourcePath path, Query query) {
            return null;
        }

        @Override
        public boolean delete(PathElementEntity pathElement) throws NoSuchEntityException {
            return true;
        }

        @Override
        public void delete(ResourcePath path, Query query) throws NoSuchEntityException {
        }

        @Override
        public boolean update(PathElementEntity pathElement, Entity entity) throws NoSuchEntityException, IncompleteEntityException {
            return true;
        }

        @Override
        public boolean update(PathElementEntity pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException {
            return true;
        }

        @Override
        public List<EntityChangedMessage> getEntityChangedMessages() {
            return Collections.emptyList();
        }

        @Override
        public void init(CoreSettings settings) {
            coreSettings = settings;
        }

        @Override
        public CoreSettings getCoreSettings() {
            return coreSettings;
        }

        @Override
        public void commit() {
        }

        @Override
        public void rollback() {
        }

        @Override
        public void close() {
        }

        @Override
        public String checkForUpgrades() {
            return "";
        }

        @Override
        public boolean doUpgrades(Writer out) throws UpgradeFailedException, IOException {
            return true;
        }

    }
}
