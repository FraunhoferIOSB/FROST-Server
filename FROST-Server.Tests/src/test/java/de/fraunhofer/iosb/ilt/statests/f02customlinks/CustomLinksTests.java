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
package de.fraunhofer.iosb.ilt.statests.f02customlinks;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_ID;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_NAME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_EXTENSION;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_PLUGINS;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.CoreModelSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class CustomLinksTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLinksTests.class.getName());

    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> FEATURESOFINTEREST = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> THINGS = new ArrayList<>();

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        SERVER_PROPERTIES.put(CoreSettings.TAG_ALWAYS_ORDERBY_ID, "false");
        SERVER_PROPERTIES.put(PREFIX_EXTENSION + CoreSettings.TAG_CUSTOM_LINKS_ENABLE, "true");
        SERVER_PROPERTIES.put(PREFIX_EXTENSION + CoreSettings.TAG_CUSTOM_LINKS_RECURSE_DEPTH, "1");
        SERVER_PROPERTIES.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_THING, VALUE_ID_TYPE_LONG);
        SERVER_PROPERTIES.put(PREFIX_PLUGINS + CoreModelSettings.TAG_ID_TYPE_LOCATION, VALUE_ID_TYPE_LONG);
    }

    public CustomLinksTests(ServerVersion version) {
        super(version, SERVER_PROPERTIES);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        LOGGER.info("Cleaning up after version {}.", version.urlPart);
        cleanup();
    }

    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        THINGS.clear();
        FEATURESOFINTEREST.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        createLocations();
        createThings();
    }

    private static void createThings() throws ServiceFailureException {
        Entity thing1 = sMdl.newThing("Thing 1", "The first thing.");
        MapValue properties = CollectionsHelper.propertiesBuilder()
                .addItem("alternate.Location@iot.id", LOCATIONS.get(0).getPrimaryKeyValues()[0])
                .build();
        thing1.setProperty(EP_PROPERTIES, properties);
        thing1.addNavigationEntity(sMdl.npThingLocations, LOCATIONS.get(0));
        sSrvc.create(thing1);
        THINGS.add(thing1);

        Entity thing2 = sMdl.newThing("Thing 2", "The second thing.");
        properties = CollectionsHelper.propertiesBuilder()
                .addItem("parent.Thing@iot.id", thing1.getPrimaryKeyValues()[0])
                .addItem("alternate.Location@iot.id", LOCATIONS.get(0).getPrimaryKeyValues()[0])
                .build();
        thing2.setProperty(EP_PROPERTIES, properties);
        thing2.addNavigationEntity(sMdl.npThingLocations, LOCATIONS.get(1));
        sSrvc.create(thing2);
        THINGS.add(thing2);

        Entity thing3 = sMdl.newThing("Thing 3", "The third thing.");
        properties = CollectionsHelper.propertiesBuilder()
                .addItem("parent.Thing@iot.id", thing1.getPrimaryKeyValues()[0])
                .addItem("alternate.Location@iot.id", -1)
                .build();
        thing3.setProperty(EP_PROPERTIES, properties);
        thing3.addNavigationEntity(sMdl.npThingLocations, LOCATIONS.get(2));
        sSrvc.create(thing3);
        THINGS.add(thing3);

        Entity thing4 = sMdl.newThing("Thing 4", "The fourt thing.");
        properties = CollectionsHelper.propertiesBuilder()
                .addItem("parent.Thing@iot.id", thing2.getPrimaryKeyValues()[0])
                .build();
        thing4.setProperty(EP_PROPERTIES, properties);
        thing4.addNavigationEntity(sMdl.npThingLocations, LOCATIONS.get(3));
        sSrvc.create(thing4);
        THINGS.add(thing4);
    }

    private static void createLocations() throws ServiceFailureException {
        {
            Entity location = sMdl.newLocation("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
            sSrvc.create(location);
            LOCATIONS.add(location);
        }
        {
            Entity location = sMdl.newLocation("Location 2.0", "First Location of Thing 2.", "application/vnd.geo+json", new Point(9, 51));
            sSrvc.create(location);
            LOCATIONS.add(location);
        }
        {
            Entity location = sMdl.newLocation("Location 3.0", "First Location of Thing 3.", "application/vnd.geo+json", new Point(8, 50));
            sSrvc.create(location);
            LOCATIONS.add(location);
        }
        {
            Entity location = sMdl.newLocation("Location 4.0", "First Location of Thing 4.", "application/vnd.geo+json", new Point(9, 50));
            sSrvc.create(location);
            LOCATIONS.add(location);
        }
    }

    @Test
    void testCustomLinks1() throws ServiceFailureException {
        LOGGER.info("  testCustomLinks1");
        Entity thing = sSrvc.dao(sMdl.etThing).find(THINGS.get(1).getPrimaryKeyValues());
        Object navLink = thing.getProperty(EP_PROPERTIES).get("parent.Thing@iot.navigationLink");
        String expected = getServerSettings().getServiceRootUrl()
                + "/" + version.urlPart + "/Things("
                + Utils.quoteForUrl(THINGS.get(0).getPrimaryKeyValues()[0]) + ")";
        assertEquals(expected, navLink, "Custom link does not have (correct) navigationLink.");
        navLink = thing.getProperty(EP_PROPERTIES).get("alternate.Location@iot.navigationLink");
        expected = getServerSettings().getServiceRootUrl()
                + "/" + version.urlPart + "/Locations("
                + Utils.quoteForUrl(LOCATIONS.get(0).getPrimaryKeyValues()[0]) + ")";
        assertEquals(expected, navLink, "Custom link does not have (correct) navigationLink.");
    }

    @Test
    void testCustomLinksExpand() throws ServiceFailureException {
        LOGGER.info("  testCustomLinksExpand");
        Entity thing = sSrvc.dao(sMdl.etThing)
                .query()
                .filter("id eq " + Utils.quoteForUrl(THINGS.get(1).getPrimaryKeyValues()[0]))
                .expand("properties/parent.Thing,properties/alternate.Location")
                .first();
        String expected = getServerSettings().getServiceRootUrl()
                + "/" + version.urlPart + "/Things("
                + Utils.quoteForUrl(THINGS.get(0).getPrimaryKeyValues()[0]) + ")";
        Object navLink = thing.getProperty(EP_PROPERTIES).get("parent.Thing@iot.navigationLink");
        assertEquals(expected, navLink, "Custom link does not have (correct) navigationLink.");

        expected = THINGS.get(0).getProperty(EP_NAME);
        Object parent = thing.getProperty(EP_PROPERTIES).get("parent.Thing");
        if (!(parent instanceof Map)) {
            fail("parent.Thing did not expand.");
        }
        Object name = ((Map) parent).get("name");
        assertEquals(expected, name, "Custom link does not have (correct) navigationLink.");
    }

    @Test
    void testCustomLinksExpandSelect() throws ServiceFailureException {
        LOGGER.info("  testCustomLinksExpandSelect");
        Entity thing = sSrvc.dao(sMdl.etThing)
                .query()
                .filter("id eq " + Utils.quoteForUrl(THINGS.get(1).getPrimaryKeyValues()[0]))
                .expand("properties/parent.Thing($select=id),properties/alternate.Location($select=name)")
                .first();
        String expected = getServerSettings().getServiceRootUrl()
                + "/" + version.urlPart + "/Things("
                + Utils.quoteForUrl(THINGS.get(0).getPrimaryKeyValues()[0]) + ")";
        Object navLink = thing.getProperty(EP_PROPERTIES).get("parent.Thing@iot.navigationLink");
        assertEquals(expected, navLink, "Custom link does not have (correct) navigationLink.");

        Object parent = thing.getProperty(EP_PROPERTIES).get("parent.Thing");
        if (!(parent instanceof Map)) {
            fail("parent.Thing did not expand.");
        }
        Map<String, Object> properties = (Map<String, Object>) parent;
        assertFalse(properties.containsKey("name"), "Name should not have been expanded.");
        assertEquals(THINGS.get(0).getProperty(EP_ID), properties.get("@iot.id"), "id not correctly expanded.");
    }

    @Test
    void testCustomLinksExpandSelect2() throws ServiceFailureException {
        LOGGER.info("  testCustomLinksExpandSelect2");
        List<Entity> things = sSrvc.dao(sMdl.etThing)
                .query()
                .expand("properties/alternate.Location($select=name)")
                .orderBy("id asc")
                .list()
                .toList();
        String expected = LOCATIONS.get(0).getProperty(EP_NAME);
        Map<String, Object> alternateLocation = (Map<String, Object>) things.get(0).getProperty(EP_PROPERTIES).get("alternate.Location");
        assertEquals(expected, alternateLocation.get("name"), "Expanded custom Location does not have correct name.");
    }

}
