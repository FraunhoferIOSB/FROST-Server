package de.fraunhofer.iosb.ilt.frostserver.settings;

import de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_MAX_IN_FLIGHT;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_MQTT_BROKER;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_QOS_LEVEL;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_RECV_QUEUE_SIZE;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_RECV_WORKER_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_SEND_QUEUE_SIZE;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_SEND_WORKER_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.messagebus.MqttMessageBus.TAG_TOPIC_NAME;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigDefaultsTest {

    public ConfigDefaultsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDefaultValueLookupInteger() {
        MqttMessageBus b = new MqttMessageBus();
        // Test valid integer properties
        assertEquals(4, b.defaultValueInt(TAG_SEND_WORKER_COUNT));
        assertEquals(2, b.defaultValueInt(TAG_RECV_WORKER_COUNT));
        assertEquals(1000, b.defaultValueInt(TAG_SEND_QUEUE_SIZE));
        assertEquals(100, b.defaultValueInt(TAG_RECV_QUEUE_SIZE));
        assertEquals(2, b.defaultValueInt(TAG_QOS_LEVEL));
        assertEquals(500, b.defaultValueInt(TAG_MAX_IN_FLIGHT));
    }

    @Test
    public void testDefaultValueLookupString() {
        MqttMessageBus b = new MqttMessageBus();
        // Test valid string properties
        assertEquals("tcp://127.0.0.1:1884", b.defaultValue(TAG_MQTT_BROKER));
        assertEquals("FROST-Bus", b.defaultValue(TAG_TOPIC_NAME));

    }

    @Test
    public void testDefaultValueLookupBoolean() {
        // Test valid boolean properties
        CoreSettings c = new CoreSettings();
        assertEquals(true, c.defaultValueBoolean(CoreSettings.TAG_USE_ABSOLUTE_NAVIGATION_LINKS));
        assertEquals(false, c.defaultValueBoolean(CoreSettings.TAG_AUTH_ALLOW_ANON_READ));

    }

    @Test
    public void testDefaultValueLookupIntegerString() {
        MqttMessageBus b = new MqttMessageBus();
        // Test reading integer properties as strings
        assertEquals("4", b.defaultValue(TAG_SEND_WORKER_COUNT));
        assertEquals("2", b.defaultValue(TAG_RECV_WORKER_COUNT));
        assertEquals("1000", b.defaultValue(TAG_SEND_QUEUE_SIZE));
        assertEquals("100", b.defaultValue(TAG_RECV_QUEUE_SIZE));
        assertEquals("2", b.defaultValue(TAG_QOS_LEVEL));
        assertEquals("500", b.defaultValue(TAG_MAX_IN_FLIGHT));

    }

    @Test
    public void testDefaultValueLookupBooleanString() {
        // Test reading boolean properties as strings
        CoreSettings c = new CoreSettings();
        assertEquals(Boolean.TRUE.toString(), c.defaultValue(CoreSettings.TAG_USE_ABSOLUTE_NAVIGATION_LINKS));
        assertEquals(Boolean.FALSE.toString(), c.defaultValue(CoreSettings.TAG_AUTH_ALLOW_ANON_READ));

    }

    @Test
    public void testDefaultValueLookupInvalid() {
        MqttMessageBus b = new MqttMessageBus();
        // Test invalid properties
        try {
            b.defaultValueInt("NOT_A_VALID_INT_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        try {
            b.defaultValue("NOT_A_VALID_STR_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        try {
            b.defaultValueBoolean("NOT_A_VALID_BOOL_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }

    }

    @Test
    public void testDefaultValueLookupConfigTags() {
        MqttMessageBus b = new MqttMessageBus();
        // Test configTags
        Set<String> tags = new HashSet<>();
        tags.add(TAG_SEND_WORKER_COUNT);
        tags.add(TAG_RECV_WORKER_COUNT);
        tags.add(TAG_SEND_QUEUE_SIZE);
        tags.add(TAG_RECV_QUEUE_SIZE);
        tags.add(TAG_QOS_LEVEL);
        tags.add(TAG_MAX_IN_FLIGHT);
        tags.add(TAG_MQTT_BROKER);
        tags.add(TAG_TOPIC_NAME);
        assertEquals(tags, b.configTags());

    }

    @Test
    public void testDefaultValueLookupConfigDefaults() {
        MqttMessageBus b = new MqttMessageBus();
        // Test configDefaults
        Map<String, String> configDefaults = b.configDefaults();
        assertEquals("tcp://127.0.0.1:1884", configDefaults.get(TAG_MQTT_BROKER));
        assertEquals("FROST-Bus", configDefaults.get(TAG_TOPIC_NAME));
        assertEquals("4", configDefaults.get(TAG_SEND_WORKER_COUNT));
        assertEquals("2", configDefaults.get(TAG_RECV_WORKER_COUNT));
        assertEquals("1000", configDefaults.get(TAG_SEND_QUEUE_SIZE));
        assertEquals("100", configDefaults.get(TAG_RECV_QUEUE_SIZE));
        assertEquals("2", configDefaults.get(TAG_QOS_LEVEL));
        assertEquals("500", configDefaults.get(TAG_MAX_IN_FLIGHT));
    }

    @Test
    public void testDefaultValueLookupClassInteger() {
        Class c = MqttMessageBus.class;
        // Test valid integer properties
        assertEquals(4, ConfigUtils.getDefaultValueInt(c, TAG_SEND_WORKER_COUNT));
        assertEquals(2, ConfigUtils.getDefaultValueInt(c, TAG_RECV_WORKER_COUNT));
        assertEquals(1000, ConfigUtils.getDefaultValueInt(c, TAG_SEND_QUEUE_SIZE));
        assertEquals(100, ConfigUtils.getDefaultValueInt(c, TAG_RECV_QUEUE_SIZE));
        assertEquals(2, ConfigUtils.getDefaultValueInt(c, TAG_QOS_LEVEL));
        assertEquals(500, ConfigUtils.getDefaultValueInt(c, TAG_MAX_IN_FLIGHT));
    }

    @Test
    public void testDefaultValueLookupClassString() {
        Class c = MqttMessageBus.class;
        // Test valid string properties
        assertEquals("tcp://127.0.0.1:1884", ConfigUtils.getDefaultValue(c, TAG_MQTT_BROKER));
        assertEquals("FROST-Bus", ConfigUtils.getDefaultValue(c, TAG_TOPIC_NAME));
    }

    @Test
    public void testDefaultValueLookupClassBoolean() {
        Class c = MqttMessageBus.class;
        // Test valid boolean properties
        assertEquals(true, ConfigUtils.getDefaultValueBoolean(CoreSettings.class, CoreSettings.TAG_USE_ABSOLUTE_NAVIGATION_LINKS));
        assertEquals(false, ConfigUtils.getDefaultValueBoolean(CoreSettings.class, CoreSettings.TAG_AUTH_ALLOW_ANON_READ));

    }

    @Test
    public void testDefaultValueLookupClassIntegerString() {
        Class c = MqttMessageBus.class;
        // Test reading integer properties as strings
        assertEquals("4", ConfigUtils.getDefaultValue(c, TAG_SEND_WORKER_COUNT));
        assertEquals("2", ConfigUtils.getDefaultValue(c, TAG_RECV_WORKER_COUNT));
        assertEquals("1000", ConfigUtils.getDefaultValue(c, TAG_SEND_QUEUE_SIZE));
        assertEquals("100", ConfigUtils.getDefaultValue(c, TAG_RECV_QUEUE_SIZE));
        assertEquals("2", ConfigUtils.getDefaultValue(c, TAG_QOS_LEVEL));
        assertEquals("500", ConfigUtils.getDefaultValue(c, TAG_MAX_IN_FLIGHT));

    }

    @Test
    public void testDefaultValueLookupClassBooleanString() {
        Class c = MqttMessageBus.class;
        // Test reading boolean properties as strings
        assertEquals(Boolean.TRUE.toString(), ConfigUtils.getDefaultValue(CoreSettings.class, CoreSettings.TAG_USE_ABSOLUTE_NAVIGATION_LINKS));
        assertEquals(Boolean.FALSE.toString(), ConfigUtils.getDefaultValue(CoreSettings.class, CoreSettings.TAG_AUTH_ALLOW_ANON_READ));

    }

    @Test
    public void testDefaultValueLookupClassInvalid() {
        Class c = MqttMessageBus.class;
        // Test invalid properties
        try {
            ConfigUtils.getDefaultValueInt(c, "NOT_A_VALID_INT_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        try {
            ConfigUtils.getDefaultValue(c, "NOT_A_VALID_STR_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
        try {
            ConfigUtils.getDefaultValue(c, "NOT_A_VALID_STR_PROPERTY");
            Assert.fail("Should have thrown an exception for a non-existing default value.");
        } catch (IllegalArgumentException exc) {
            // This should happen.
        }
    }

    @Test
    public void testDefaultValueLookupClassConfigTags() {
        Class c = MqttMessageBus.class;
        // Test configTags
        Set<String> tags = new HashSet<>();
        tags.add(TAG_SEND_WORKER_COUNT);
        tags.add(TAG_RECV_WORKER_COUNT);
        tags.add(TAG_SEND_QUEUE_SIZE);
        tags.add(TAG_RECV_QUEUE_SIZE);
        tags.add(TAG_QOS_LEVEL);
        tags.add(TAG_MAX_IN_FLIGHT);
        tags.add(TAG_MQTT_BROKER);
        tags.add(TAG_TOPIC_NAME);
        assertEquals(tags, ConfigUtils.getConfigTags(c));
    }

    @Test
    public void testDefaultValueLookupClassConfigDefaults() {
        Class c = MqttMessageBus.class;
        // Test configDefaults
        Map<String, String> configDefaults = ConfigUtils.getConfigDefaults(c);
        assertEquals("tcp://127.0.0.1:1884", configDefaults.get(TAG_MQTT_BROKER));
        assertEquals("FROST-Bus", configDefaults.get(TAG_TOPIC_NAME));
        assertEquals("4", configDefaults.get(TAG_SEND_WORKER_COUNT));
        assertEquals("2", configDefaults.get(TAG_RECV_WORKER_COUNT));
        assertEquals("1000", configDefaults.get(TAG_SEND_QUEUE_SIZE));
        assertEquals("100", configDefaults.get(TAG_RECV_QUEUE_SIZE));
        assertEquals("2", configDefaults.get(TAG_QOS_LEVEL));
        assertEquals("500", configDefaults.get(TAG_MAX_IN_FLIGHT));
    }
}
