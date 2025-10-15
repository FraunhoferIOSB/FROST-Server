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
package de.fraunhofer.iosb.ilt.statests.v2cud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.json.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core;
import de.fraunhofer.iosb.ilt.frostclient.models.swecommon.util.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for GET/POST/PUT/PATCH on $ref urls. Works on the V2 data model.
 */
public class ReferenceTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceTests.class);

    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> FEATURES = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> OPROPS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> THINGS = new ArrayList<>();
    private static SensorThingsV20Core sMdl;

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        SERVER_PROPERTIES.put("plugins.modelLoader.enable", "true");
        SERVER_PROPERTIES.put("plugins.coreModel.enable", "false");
        SERVER_PROPERTIES.put("plugins.coreModel.idType", "LONG");
        SERVER_PROPERTIES.put("plugins.coreService.enable", "true");
        SERVER_PROPERTIES.put("plugins.coreModelV2.enable", "true");
    }

    public ReferenceTests() {
        super(ServerVersion.v_1_1, SERVER_PROPERTIES);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV20Core.class);
        EntityUtils.deleteAll(sSrvc);
        createEntities();
    }

    @Override
    protected SensorThingsService createService() throws MalformedURLException, URISyntaxException {
        return new SensorThingsService(new SensorThingsV20Core())
                .setBaseUrl(new URI(serverSettings.getServiceUrl(version)).toURL())
                .init();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(sSrvc);
        DATASTREAMS.clear();
        FEATURES.clear();
        LOCATIONS.clear();
        OPROPS.clear();
        SENSORS.clear();
        THINGS.clear();
    }

    /**
     * This method is run after all the tests of this class is run and clean the
     * database.
     *
     * @throws
     * de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException
     */
    @AfterAll
    public static void deleteEverything() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        {
            Entity thing = sMdl.newThing("Thing 1", "The first thing.");
            sSrvc.create(thing);
            THINGS.add(thing);
        }
        {
            Entity location = sMdl.newLocation("Location Des Dings von ILT", "First Location of Thing 1.", "application/vnd.geo+json", new Point(8, 49));
            location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
            sSrvc.create(location);
            LOCATIONS.add(location);
        }
        {
            Entity sensor1 = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
            sSrvc.create(sensor1);
            SENSORS.add(sensor1);
        }
        {
            Entity sensor2 = sMdl.newSensor("Sensor 2", "The second sensor", "text", "Some metadata.");
            sSrvc.create(sensor2);
            SENSORS.add(sensor2);
        }
        Entity obsProp1 = sMdl.newObservedProperty("Temperature", "http://ucom.org/temperature", "The temperature of the thing.");
        sSrvc.create(obsProp1);
        OPROPS.add(obsProp1);

        Entity obsProp2 = sMdl.newObservedProperty("Humidity", "http://ucom.org/humidity", "The humidity of the thing.");
        sSrvc.create(obsProp2);
        OPROPS.add(obsProp2);

        {
            Entity datastream1 = sMdl.newDatastream(
                    "Datastream Temp",
                    "The temperature of thing 1, sensor 1.",
                    obsProp1.getSelfLink(false),
                    new UnitOfMeasurement().setLabel("degree celcius").setSymbol("°C").setCode("ucum:T"));
            datastream1.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
            datastream1.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());
            sSrvc.create(datastream1);
            DATASTREAMS.add(datastream1);
        }
        {
            Entity datastream2 = sMdl.newDatastream(
                    "Datastream LF",
                    "The humidity of thing 1, sensor 2.",
                    obsProp2.getSelfLink(false),
                    new UnitOfMeasurement().setLabel("relative humidity").setSymbol("%").setCode("ucum:Humidity"))
                    .setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk())
                    .setProperty(sMdl.npDatastreamSensor, SENSORS.get(1).withOnlyPk());
            sSrvc.create(datastream2);
            DATASTREAMS.add(datastream2);
        }
        {
            Entity feature = sMdl.newFeature("Feature 1", "The first Features", new Point(8.0, 50.0));
            sSrvc.create(feature);
            FEATURES.add(feature);
        }
        {
            Entity feature = sMdl.newFeature("Feature 2", "The second Features", new Point(9.0, 51.0));
            sSrvc.create(feature);
            FEATURES.add(feature);
        }
    }

    /**
     * Tests if we can PUT on Datastream(x)/UltimateFeatureOfInterest/$ref.
     */
    @Test
    void editRefEntity01() throws ServiceFailureException, JsonPointerException, IOException {
        LOGGER.info("  editRefEntity01");
        final Entity ds0 = DATASTREAMS.get(0);
        linkAndTestEntityRef(ds0, sMdl.npDatastreamUltimateFoi, FEATURES.get(0), true);
        linkAndTestEntityRef(ds0, sMdl.npDatastreamUltimateFoi, FEATURES.get(1), false);

        String selfLinkSrc = ds0.getSelfLink();
        String refLink = selfLinkSrc += "/" + sMdl.npDatastreamUltimateFoi.getName() + "/$ref";
        {
            HttpResponse response = HTTPMethods.doDelete(refLink);
            Assertions.assertEquals(204, response.code);
            Entity target = ds0.withOnlyPk()
                    .getProperty(sMdl.npDatastreamUltimateFoi);
            Assertions.assertNull(target);
        }
        {
            // Doing it again should be a no-op
            HttpResponse response = HTTPMethods.doDelete(refLink);
            Assertions.assertEquals(204, response.code);
        }
    }

    private void linkAndTestEntityRef(Entity source, NavigationPropertyEntity np, Entity target, boolean abs) throws ServiceFailureException, JsonProcessingException {
        String selfLinkSrc = source.getSelfLink();
        String refLink = selfLinkSrc += "/" + np.getName() + "/$ref";
        String selfLinkTrgt = target.getSelfLink(abs);
        String body = SimpleJsonMapper.getSimpleObjectMapper()
                .writeValueAsString(
                        CollectionsHelper.propertiesBuilder()
                                .addItem("@id", selfLinkTrgt)
                                .buildMap());
        HttpResponse response = HTTPMethods.doPut(refLink, body);
        Assertions.assertEquals(204, response.code);

        PkValue pkFeature = source.withOnlyPk()
                .getProperty(np)
                .getPrimaryKeyValues();
        Assertions.assertEquals(target.getPrimaryKeyValues(), pkFeature);
    }

}
