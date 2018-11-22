package de.fraunhofer.iosb.ilt.sta.settings;

import de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

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
    public void testDefaultValueLookup() {
        MqttMessageBus b = new MqttMessageBus();
        // Test valid integer properties
        assertEquals(2, b.defaultValueInt("TAG_SEND_WORKER_COUNT"));
        assertEquals(2, b.defaultValueInt("TAG_RECV_WORKER_COUNT"));
        assertEquals(100, b.defaultValueInt("TAG_SEND_QUEUE_SIZE"));
        assertEquals(100, b.defaultValueInt("TAG_RECV_QUEUE_SIZE"));
        assertEquals(2, b.defaultValueInt("TAG_QOS_LEVEL"));
        assertEquals(50, b.defaultValueInt("TAG_MAX_IN_FLIGHT"));
        // Test valid string properties
        assertEquals("tcp://127.0.0.1:1884", b.defaultValue("TAG_MQTT_BROKER"));
        assertEquals("FROST-Bus", b.defaultValue("TAG_TOPIC_NAME"));
        // Test reading integer properties as strings
        assertEquals("2", b.defaultValue("TAG_SEND_WORKER_COUNT"));
        assertEquals("2", b.defaultValue("TAG_RECV_WORKER_COUNT"));
        assertEquals("100", b.defaultValue("TAG_SEND_QUEUE_SIZE"));
        assertEquals("100", b.defaultValue("TAG_RECV_QUEUE_SIZE"));
        assertEquals("2", b.defaultValue("TAG_QOS_LEVEL"));
        assertEquals("50", b.defaultValue("TAG_MAX_IN_FLIGHT"));
        // Test invalid properties
        assertEquals(0, b.defaultValueInt("NOT_A_VALID_INT_PROPERTY"));
        assertEquals("", b.defaultValue("NOT_A_VALID_STR_PROPERTY"));
        // Test configTags
        Set<String> tags = new HashSet<>();
        tags.add("TAG_SEND_WORKER_COUNT");
        tags.add("TAG_RECV_WORKER_COUNT");
        tags.add("TAG_SEND_QUEUE_SIZE");
        tags.add("TAG_RECV_QUEUE_SIZE");
        tags.add("TAG_QOS_LEVEL");
        tags.add("TAG_MAX_IN_FLIGHT");
        tags.add("TAG_MQTT_BROKER");
        tags.add("TAG_TOPIC_NAME");
        assertTrue(tags.equals(b.configTags()));
    }
}
