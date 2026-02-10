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

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_DEFINITION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_NAME;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_VALIDTIME;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.json.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.swecommon.util.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for GET/POST/PUT/PATCH on $ref urls. Works on the V2 data model.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ReferenceTests11 extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceTests11.class);

    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> FEATURES = new ArrayList<>();
    private static final List<Entity> FEATURE_TYPES = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> OPROPS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
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

    public ReferenceTests11() {
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
            Entity feature = sMdl.newFeature("Feature 2", "The second Features", new Point(9.0, 50.0));
            sSrvc.create(feature);
            FEATURES.add(feature);
        }
        {
            Entity feature = sMdl.newFeature("Feature 3", "The third Features", new Point(9.0, 51.0));
            sSrvc.create(feature);
            FEATURES.add(feature);
        }
        {
            Entity featureType = newFeatureType("Feature Type 1", "The first Feature Type");
            sSrvc.create(featureType);
            FEATURE_TYPES.add(featureType);
        }
        {
            Entity featureType = newFeatureType("Feature Type 2", "The second Feature Type");
            sSrvc.create(featureType);
            FEATURE_TYPES.add(featureType);
        }
        {
            Entity featureType = newFeatureType("Feature Type 3", "The third Feature Type");
            sSrvc.create(featureType);
            FEATURE_TYPES.add(featureType);
        }
        ZonedDateTime startTime = ZonedDateTime.parse("2016-01-01T01:00:00.000Z");
        createObservationSet(sSrvc, DATASTREAMS.get(0), 0, startTime, null, 10, OBSERVATIONS);
    }

    /**
     * Tests if we can PUT on Datastream(x)/UltimateFeatureOfInterest/$ref.
     */
    @Test
    void test01_editRefEntity() throws ServiceFailureException, JsonPointerException, IOException {
        LOGGER.info("  test01_editRefEntity");
        final Entity ds0 = DATASTREAMS.get(0);
        putEntityRefAndTest(ds0, sMdl.npDatastreamProximateFoi, FEATURES.get(0), true);
        putEntityRefAndTest(ds0, sMdl.npDatastreamProximateFoi, FEATURES.get(1), false);

        String selfLinkSrc = ds0.getSelfLink();
        String refLink = selfLinkSrc + "/" + sMdl.npDatastreamProximateFoi.getName() + "/$ref";
        {
            HttpResponse response = HTTPMethods.doDelete(refLink);
            Assertions.assertEquals(204, response.code);
            Entity target = ds0.withOnlyPk()
                    .getProperty(sMdl.npDatastreamProximateFoi);
            Assertions.assertNull(target);
        }
        {
            // Doing it again should be a no-op
            HttpResponse response = HTTPMethods.doDelete(refLink);
            Assertions.assertEquals(204, response.code);
        }
    }

    private void putEntityRefAndTest(Entity source, NavigationPropertyEntity np, Entity target, boolean abs) throws ServiceFailureException, JsonProcessingException {
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

    /**
     * Tests if we can PUT on Feature(x)/FeaturesTypes/$ref.
     */
    @Test
    void test02_editRefEntitySet() throws ServiceFailureException, JsonPointerException, IOException {
        LOGGER.info("  test02_editRefEntitySet");
        final Entity f0 = FEATURES.get(0);
        putEntitySetRefsAndTest(f0, sMdl.npFeatureFeatureTypes, true, getFromList(FEATURE_TYPES, 0));
        putEntitySetRefsAndTest(f0, sMdl.npFeatureFeatureTypes, false, getFromList(FEATURE_TYPES, 1, 2));
        putEntitySetRefsAndTest(f0, sMdl.npFeatureFeatureTypes, true, getFromList(FEATURE_TYPES, 1));

        String selfLinkSrc = f0.getSelfLink();
        String refLink = selfLinkSrc + "/" + sMdl.npFeatureFeatureTypes.getName() + "(" + Utils.quoteForUrl(FEATURE_TYPES.get(1).getPrimaryKeyValues().get(0)) + ")" + "/$ref";
        {
            HttpResponse response = HTTPMethods.doDelete(refLink);
            Assertions.assertEquals(204, response.code);
            EntityUtils.testFilterResults(f0.dao(sMdl.npFeatureFeatureTypes), "", Collections.emptyList());
        }
        {
            // Doing it again should give a reference-not-found
            HttpResponse response = HTTPMethods.doDelete(refLink);
            Assertions.assertEquals(404, response.code);
        }
    }

    @Test
    void test03_editRefEntitySet() throws ServiceFailureException, JsonPointerException, IOException {
        LOGGER.info("  test03_editRefEntitySet");
        final Entity f0 = FEATURES.get(0);
        putEntitySetRefsAndTest(f0, sMdl.npFeatureFeatureTypes, false, getFromList(FEATURE_TYPES, 0, 1, 2));

        String selfLinkSrc = f0.getSelfLink();
        {
            String refLink = selfLinkSrc + "/" + sMdl.npFeatureFeatureTypes.getName() + "/$ref?$id=../../" + FEATURE_TYPES.get(1).getSelfLink(false);
            HttpResponse response = HTTPMethods.doDelete(refLink);
            Assertions.assertEquals(204, response.code);
            EntityUtils.testFilterResults(f0.dao(sMdl.npFeatureFeatureTypes), "", getFromList(FEATURE_TYPES, 0, 2));
        }
        {
            String refLink = selfLinkSrc + "/" + sMdl.npFeatureFeatureTypes.getName() + "/$ref?$id=" + FEATURE_TYPES.get(0).getSelfLink(true);
            HttpResponse response = HTTPMethods.doDelete(refLink);
            Assertions.assertEquals(204, response.code);
            EntityUtils.testFilterResults(f0.dao(sMdl.npFeatureFeatureTypes), "", getFromList(FEATURE_TYPES, 2));
        }
        // Adding the ony that is already there is a no-op (204 no content)
        postEntitySetRefAndTest(f0, sMdl.npFeatureFeatureTypes, false, FEATURE_TYPES.get(2), getFromList(FEATURE_TYPES, 2));
        // Adding a new one.
        postEntitySetRefAndTest(f0, sMdl.npFeatureFeatureTypes, false, FEATURE_TYPES.get(1), getFromList(FEATURE_TYPES, 1, 2));
        // Adding a new one.
        postEntitySetRefAndTest(f0, sMdl.npFeatureFeatureTypes, true, FEATURE_TYPES.get(0), getFromList(FEATURE_TYPES, 0, 1, 2));
    }

    private void putEntitySetRefsAndTest(Entity source, NavigationPropertyEntitySet np, boolean abs, List<Entity> targets) throws ServiceFailureException, JsonProcessingException {
        String selfLinkSrc = source.getSelfLink();
        String refLink = selfLinkSrc += "/" + np.getName() + "/$ref";

        List<Map<String, Object>> selfLinkList = new ArrayList<>();
        for (var target : targets) {
            selfLinkList.add(
                    CollectionsHelper.propertiesBuilder()
                            .addItem("@id", target.getSelfLink(abs))
                            .buildMap());
        }

        String body = SimpleJsonMapper.getSimpleObjectMapper()
                .writeValueAsString(
                        CollectionsHelper.propertiesBuilder()
                                .addItem("value", selfLinkList)
                                .buildMap());
        HttpResponse response = HTTPMethods.doPut(refLink, body);
        Assertions.assertEquals(204, response.code);

        EntityUtils.testFilterResults(source.dao(np), "", targets);
    }

    private void postEntitySetRefAndTest(Entity source, NavigationPropertyEntitySet np, boolean abs, Entity target, List<Entity> expected) throws ServiceFailureException, JsonProcessingException {
        String selfLinkSrc = source.getSelfLink();
        String refLink = selfLinkSrc += "/" + np.getName() + "/$ref";

        Map<String, Object> data = CollectionsHelper.propertiesBuilder()
                .addItem("@id", target.getSelfLink(abs))
                .buildMap();
        String body = SimpleJsonMapper.getSimpleObjectMapper()
                .writeValueAsString(data);
        HttpResponse response = HTTPMethods.doPost(refLink, body);
        Assertions.assertEquals(204, response.code);

        EntityUtils.testFilterResults(source.dao(np), "", expected);
    }

    //To remove once added to FROST-Client-Dynamic
    public static Entity newFeatureType() {
        return new Entity(sMdl.etFeatureType);
    }

    public static Entity newFeatureType(Object id) {
        return new Entity(sMdl.etFeatureType)
                .setPrimaryKeyValues(PkValue.of(id));
    }

    public static Entity newFeatureType(String name, String description) {
        return newFeatureType()
                .setProperty(EP_NAME, name)
                .setProperty(EP_DESCRIPTION, description);
    }

    public static Entity newFeatureType(String name, String description, String definition) {
        return newFeatureType(name, description)
                .setProperty(EP_DEFINITION, definition);
    }

    public static void createObservationSet(SensorThingsService srvc, Entity datastream, long resultStart, ZonedDateTime phenomenonTimeStart, TimeInterval validTimeStart, long count, List<Entity> registry) throws ServiceFailureException {
        for (int i = 0; i < count; i++) {
            ZonedDateTime phenTime = phenomenonTimeStart.plus(i, ChronoUnit.HOURS);
            if (validTimeStart != null) {
                TimeInterval validTime = TimeInterval.create(
                        validTimeStart.getStart().plus(count, TimeUnit.HOURS),
                        validTimeStart.getEnd().plus(count, TimeUnit.HOURS));
                createObservation(srvc, datastream, resultStart + i, phenTime, validTime, registry);
            } else {
                createObservation(srvc, datastream, resultStart + i, phenTime, null, registry);
            }
        }
    }

    public static Entity createObservation(SensorThingsService srvc, Entity datastream, long result, ZonedDateTime phenomenonTime, TimeInterval validTime, List<Entity> registry) throws ServiceFailureException {
        int idx = registry.size();
        MapValue properties = new MapValue();
        properties.put("idx", idx);
        Entity obs = sMdl.newObservation(result, phenomenonTime, datastream)
                .setProperty(EP_VALIDTIME, validTime)
                .setProperty(EP_PROPERTIES, properties);
        srvc.create(obs);
        registry.add(obs);
        return obs;
    }

}
