/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.statests.f02customlinks;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
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

    public static class Implementation10 extends CustomLinksTests {

        public Implementation10() {
            super(ServerVersion.v_1_0);
        }

    }

    public static class Implementation11 extends CustomLinksTests {

        public Implementation11() {
            super(ServerVersion.v_1_1);
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLinksTests.class.getName());

    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<FeatureOfInterest> FEATURESOFINTEREST = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<Thing> THINGS = new ArrayList<>();
    private static final Properties SERVER_PROPERTIES = new Properties();

    static {
        SERVER_PROPERTIES.put(CoreSettings.PREFIX_EXTENSION + CoreSettings.TAG_CUSTOM_LINKS_ENABLE, "true");
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
        EntityUtils.deleteAll(version, serverSettings, service);
        THINGS.clear();
        FEATURESOFINTEREST.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        createThings();
        createLocations();
    }

    private static void createThings() throws ServiceFailureException {
        Thing thing1 = new Thing("Thing 1", "The first thing.");
        Map<String, Object> properties = new HashMap<>();
        thing1.setProperties(properties);
        service.create(thing1);
        THINGS.add(thing1);

        Thing thing2 = new Thing("Thing 2", "The second thing.");
        properties = new HashMap<>();
        properties.put("parent.Thing@iot.id", thing1.getId().getValue());
        thing2.setProperties(properties);
        service.create(thing2);
        THINGS.add(thing2);

        Thing thing3 = new Thing("Thing 3", "The third thing.");
        properties = new HashMap<>();
        properties.put("parent.Thing@iot.id", thing1.getId().getValue());
        thing3.setProperties(properties);
        service.create(thing3);
        THINGS.add(thing3);

        Thing thing4 = new Thing("Thing 4", "The fourt thing.");
        properties = new HashMap<>();
        properties.put("parent.Thing@iot.id", thing2.getId().getValue());
        thing4.setProperties(properties);
        service.create(thing4);
        THINGS.add(thing4);
    }

    private static void createLocations() throws ServiceFailureException {
        Point gjo = new Point(8, 51);
        Location location = new Location("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", gjo);
        location.getThings().addAll(THINGS);
        service.create(location);
        LOCATIONS.add(location);
    }

    @Test
    void testCustomLinks1() throws ServiceFailureException {
        LOGGER.info("  testCustomLinks1");
        Thing thing = service.things().find(THINGS.get(1).getId());
        Object navLink = thing.getProperties().get("parent.Thing@iot.navigationLink");
        String expected = getServerSettings().getServiceRootUrl()
                + "/" + version.urlPart + "/Things("
                + THINGS.get(0).getId().getUrl() + ")";
        assertEquals(expected, navLink, "Custom link does not have (correct) navigationLink.");
    }

    @Test
    void testCustomLinks2() throws ServiceFailureException {
        LOGGER.info("  testCustomLinks2");
        Thing thing = service.things()
                .query()
                .filter("id eq " + THINGS.get(1).getId().getUrl())
                .expand("properties/parent.Thing")
                .first();
        String expected = getServerSettings().getServiceRootUrl()
                + "/" + version.urlPart + "/Things("
                + THINGS.get(0).getId().getUrl() + ")";
        Object navLink = thing.getProperties().get("parent.Thing@iot.navigationLink");
        assertEquals(expected, navLink, "Custom link does not have (correct) navigationLink.");

        expected = THINGS.get(0).getName();
        Object parent = thing.getProperties().get("parent.Thing");
        if (!(parent instanceof Map)) {
            fail("parent.Thing did not expand.");
        }
        Object name = ((Map) parent).get("name");
        assertEquals(expected, name, "Custom link does not have (correct) navigationLink.");
    }

}
