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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import org.junit.jupiter.api.Test;

class LandingPageItemTest {

    /**
     * Test of generateFrom method, of class LandingPageItem.
     */
    @Test
    void testGenerateFrom() {
        EntityType et = new EntityType("Test", "Tests");
        String path = "http://myserver/myPath/";
        LandingPageItem expResult = new LandingPageItem()
                .setName("Tests")
                .setUrl(path + "Tests");
        LandingPageItem result = LandingPageItem.generateFrom(et, path);
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class LandingPageItem.
     */
    @Test
    void testGetName() {
        EntityType et = new EntityType("Test", "Tests");
        String path = "http://myserver/myPath/";
        LandingPageItem instance = LandingPageItem.generateFrom(et, path);
        String expResult = "Tests";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setName method, of class LandingPageItem.
     */
    @Test
    void testSetName() {
        EntityType et = new EntityType("Test", "Tests");
        String path = "http://myserver/myPath/";
        LandingPageItem instance = LandingPageItem.generateFrom(et, path);
        String expResult = "notTest";
        instance.setName(expResult);
        assertEquals(expResult, instance.getName());
    }

    /**
     * Test of getUrl method, of class LandingPageItem.
     */
    @Test
    void testGetUrl() {
        EntityType et = new EntityType("Test", "Tests");
        String path = "http://myserver/myPath/";
        LandingPageItem instance = LandingPageItem.generateFrom(et, path);
        String expResult = path + "Tests";
        String result = instance.getUrl();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUrl method, of class LandingPageItem.
     */
    @Test
    void testSetUrl() {
        EntityType et = new EntityType("Test", "Tests");
        String path = "http://myserver/myPath/";
        LandingPageItem instance = LandingPageItem.generateFrom(et, path);
        String expResult = "notTest";
        instance.setUrl(expResult);
        assertEquals(expResult, instance.getUrl());
    }

    /**
     * Test of getTitle and setTitle methods, of class LandingPageItem.
     */
    @Test
    void testSetGetTitle() {
        EntityType et = new EntityType("Test", "Tests");
        String path = "http://myserver/myPath/";
        String expResult = "notTest";
        LandingPageItem instance = LandingPageItem.generateFrom(et, path)
                .setTitle(expResult);
        String result = instance.getTitle();
        assertEquals(expResult, result);
    }

}
