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
package de.fraunhofer.iosb.ilt.frostserver.settings;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_CORS_ENABLE;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_MAX_TOP;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_SERVICE_ROOT_URL;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
public class SettingsTest {

    @Test
    public void testSettingsBase() {
        Properties properties = new Properties();
        properties.setProperty("property1", "value1");
        properties.setProperty("property2", "value2");
        properties.setProperty("prefix1.property1", "value3");
        properties.setProperty("prefix1.property3", "value4");
        properties.setProperty("prefix2.property1", "value5");
        properties.setProperty("prefix2.property4", "value6");
        Settings base = new Settings(properties);
        Settings prefix1 = new Settings(base.getProperties(), "prefix1.", false, false);
        Settings prefix2 = new Settings(base.getProperties(), "prefix2.", false, false);

        assertEquals("value1", base.get("property1"));
        assertEquals("value2", base.get("property2"));
        assertEquals("value3", prefix1.get("property1"));
        assertEquals("value4", prefix1.get("property3"));
        assertEquals("value5", prefix2.get("property1"));
        assertEquals("value6", prefix2.get("property4"));

        assertTrue(base.containsName("property1"));
        assertFalse(base.containsName("property3"));
        assertTrue(prefix1.containsName("property1"));
        assertFalse(prefix1.containsName("property2"));
        assertTrue(prefix1.containsName("property3"));
        assertTrue(prefix2.containsName("property1"));
        assertFalse(prefix2.containsName("property3"));
        assertTrue(prefix2.containsName("property4"));
    }

    @Test
    public void testSettingsGetThere() {
        assertThrows(PropertyMissingException.class, () -> {
            Settings settings = new Settings();
            settings.get(TAG_SERVICE_ROOT_URL);
        });
    }

    @Test
    public void testSettingsGetIntThere() {
        assertThrows(PropertyMissingException.class, () -> {
            Settings settings = new Settings();
            settings.getInt(TAG_SERVICE_ROOT_URL);
        });
    }

    @Test
    public void testSettingsGetBooleanThere() {
        assertThrows(PropertyMissingException.class, () -> {
            Settings settings = new Settings();
            settings.getBoolean(TAG_SERVICE_ROOT_URL);
        });
    }

    @Test
    public void testSettingsGetDoubleThere() {
        assertThrows(PropertyMissingException.class, () -> {
            Settings settings = new Settings();
            settings.getDouble(TAG_SERVICE_ROOT_URL);
        });
    }

    @Test
    public void testSettingsGetLongThere() {
        assertThrows(PropertyMissingException.class, () -> {
            Settings settings = new Settings();
            settings.getLong(TAG_SERVICE_ROOT_URL);
        });
    }

    @Test
    public void testSettingsWithValues() {
        Properties properties = createValues();
        Settings settings = new Settings(properties);

        assertEquals("myString", settings.get(TAG_SERVICE_ROOT_URL));
        assertEquals("123", settings.get(TAG_MAX_TOP));
        assertEquals("true", settings.get(TAG_AUTH_ALLOW_ANON_READ));
        assertEquals("false", settings.get(TAG_CORS_ENABLE));

        assertEquals(false, settings.getBoolean(TAG_SERVICE_ROOT_URL));
        assertEquals(false, settings.getBoolean(TAG_MAX_TOP));
        assertEquals(true, settings.getBoolean(TAG_AUTH_ALLOW_ANON_READ));
        assertEquals(false, settings.getBoolean(TAG_CORS_ENABLE));

    }

    @Test
    public void testSettingsWithInvalidValues() {
        Properties properties = createValues();
        Settings settings = new Settings(properties);

        assertThrows(PropertyTypeException.class, () -> {
            settings.getInt(TAG_SERVICE_ROOT_URL);
        });

        assertEquals(123, settings.getInt(TAG_MAX_TOP));

        assertThrows(PropertyTypeException.class, () -> {
            settings.getInt(TAG_AUTH_ALLOW_ANON_READ);
        });

        assertThrows(PropertyTypeException.class, () -> {
            settings.getLong(TAG_SERVICE_ROOT_URL);
        });

        assertEquals(123L, settings.getLong(TAG_MAX_TOP));

        assertThrows(PropertyTypeException.class, () -> {
            settings.getLong(TAG_AUTH_ALLOW_ANON_READ);
        });
    }

    @Test
    public void testSettingsWithValuesDefaults() {
        Properties properties = createValues();
        Settings settings = new Settings(properties);

        assertEquals("myString", settings.get(TAG_SERVICE_ROOT_URL, "otherString"));
        assertEquals("123", settings.get(TAG_MAX_TOP, "otherString"));
        assertEquals("true", settings.get(TAG_AUTH_ALLOW_ANON_READ, "otherString"));
        assertEquals("false", settings.get(TAG_CORS_ENABLE, "otherString"));

        assertEquals(false, settings.getBoolean(TAG_SERVICE_ROOT_URL, true));
        assertEquals(false, settings.getBoolean(TAG_MAX_TOP, true));
        assertEquals(true, settings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, false));
        assertEquals(false, settings.getBoolean(TAG_CORS_ENABLE, true));

        assertEquals(456, settings.getInt(TAG_SERVICE_ROOT_URL, 456));
        assertEquals(123, settings.getInt(TAG_MAX_TOP, 456));
        assertEquals(456, settings.getInt(TAG_AUTH_ALLOW_ANON_READ, 456));
        assertEquals(456, settings.getInt(TAG_CORS_ENABLE, 456));

        assertEquals(456L, settings.getLong(TAG_SERVICE_ROOT_URL, 456L));
        assertEquals(123L, settings.getLong(TAG_MAX_TOP, 456L));
        assertEquals(456L, settings.getLong(TAG_AUTH_ALLOW_ANON_READ, 456L));
        assertEquals(456L, settings.getLong(TAG_CORS_ENABLE, 456L));

        assertEquals("myString", settings.get(TAG_SERVICE_ROOT_URL, CoreSettings.class));
        assertEquals(123, settings.getInt(TAG_MAX_TOP, CoreSettings.class));
        assertEquals(true, settings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class));
        assertEquals(false, settings.getBoolean(TAG_CORS_ENABLE, CoreSettings.class));

    }

    @Test
    public void testSettingsDefaults() {
        Settings settings = new Settings();

        assertEquals("myDefault", settings.get(TAG_SERVICE_ROOT_URL, "myDefault"));
        assertEquals(ConfigUtils.getDefaultValue(CoreSettings.class, TAG_SERVICE_ROOT_URL), settings.get(TAG_SERVICE_ROOT_URL, CoreSettings.class));

        assertEquals(123, settings.getInt(TAG_MAX_TOP, 123));
        assertEquals(ConfigUtils.getDefaultValueInt(CoreSettings.class, TAG_MAX_TOP), settings.getInt(TAG_MAX_TOP, CoreSettings.class));

        assertEquals(false, settings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, false));
        assertEquals(true, settings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, true));
        assertEquals(ConfigUtils.getDefaultValueBoolean(CoreSettings.class, TAG_AUTH_ALLOW_ANON_READ), settings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class));
        assertEquals(ConfigUtils.getDefaultValueBoolean(MqttSettings.class, MqttSettings.TAG_ENABLED), settings.getBoolean(MqttSettings.TAG_ENABLED, MqttSettings.class));

        assertEquals(ConfigUtils.getDefaultValue(CoreSettings.class, TAG_AUTH_ALLOW_ANON_READ), settings.get(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class));
        assertEquals(ConfigUtils.getDefaultValue(MqttSettings.class, MqttSettings.TAG_ENABLED), settings.get(MqttSettings.TAG_ENABLED, MqttSettings.class));
    }

    private Properties createValues() {
        Properties properties = new Properties();
        properties.put(TAG_SERVICE_ROOT_URL, "myString");
        properties.put(TAG_MAX_TOP, "123");
        properties.put(TAG_AUTH_ALLOW_ANON_READ, "true");
        properties.put(TAG_CORS_ENABLE, "false");
        return properties;
    }

}
