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
package de.fraunhofer.iosb.ilt.sta.settings;

import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class SettingsTest {

    public SettingsTest() {
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
    public void testSettingsBase() {
        Properties properties = new Properties();
        properties.setProperty("property1", "value1");
        properties.setProperty("property2", "value2");
        properties.setProperty("prefix1.property1", "value3");
        properties.setProperty("prefix1.property3", "value4");
        properties.setProperty("prefix2.property1", "value5");
        properties.setProperty("prefix2.property4", "value6");
        Settings base = new Settings(properties);
        Settings prefix1 = new Settings(base.getProperties(), "prefix1.", false);
        Settings prefix2 = new Settings(base.getProperties(), "prefix2.", false);

        assertEquals(base.get("property1"), "value1");
        assertEquals(base.get("property2"), "value2");
        assertEquals(prefix1.get("property1"), "value3");
        assertEquals(prefix1.get("property3"), "value4");
        assertEquals(prefix2.get("property1"), "value5");
        assertEquals(prefix2.get("property4"), "value6");
        Assert.assertTrue(base.containsName("property1"));
        Assert.assertFalse(base.containsName("property3"));
        Assert.assertTrue(prefix1.containsName("property1"));
        Assert.assertFalse(prefix1.containsName("property2"));
        Assert.assertTrue(prefix1.containsName("property3"));
        Assert.assertTrue(prefix2.containsName("property1"));
        Assert.assertFalse(prefix2.containsName("property3"));
        Assert.assertTrue(prefix2.containsName("property4"));
    }

}
