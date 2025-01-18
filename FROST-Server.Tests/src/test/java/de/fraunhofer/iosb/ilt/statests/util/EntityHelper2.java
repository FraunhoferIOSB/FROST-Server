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
package de.fraunhofer.iosb.ilt.statests.util;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_DEFINITION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_ENCODINGTYPE;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_NAME;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_FEATUREOFINTEREST;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_HISTORICALLOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_LOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_OBSERVATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_OBSERVEDPROPERTY;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_SENSOR;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_THING;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_FEATURE;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_LOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_METADATA;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_OBSERVATIONTYPE;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_PARAMETERS;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_PHENOMENONTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_PHENOMENONTIMEDS;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULT;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULTTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULTTIMEDS;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_TIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_UNITOFMEASUREMENT;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_VALIDTIME;
import static de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper.propertiesBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntityType;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11MultiDatastream;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Tasking;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostclient.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityHelper2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHelper2.class);

    public final Map<EntityType, List<Entity>> entities = new HashMap<>();

    private final SensorThingsService sSrvc;
    private final SensorThingsV11Sensing sMdl;
    private final SensorThingsV11MultiDatastream mMdl;
    private final SensorThingsV11Tasking tMdl;

    public EntityHelper2(SensorThingsService sSrvc) {
        this.sSrvc = sSrvc;
        sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
        mMdl = sSrvc.getModel(SensorThingsV11MultiDatastream.class);
        tMdl = sSrvc.getModel(SensorThingsV11Tasking.class);
    }

    public void clearCaches() {
        entities.clear();
    }

    /**
     * Add the given entity to the entity cache.
     *
     * @param entity the entity to add to the cache.
     */
    public void cache(Entity entity) {
        getCache(entity.getEntityType()).add(entity);
    }

    /**
     * Get the cache for the given entity type. If no cache exists yet, a new
     * one is created. This method never returns null.
     *
     * @param et the entity type to get the cache for.
     * @return the requested entity cache.
     */
    public List<Entity> getCache(EntityType et) {
        return entities.computeIfAbsent(et, t -> new ArrayList<>());
    }

    /**
     * Get the given cached entity of the given entity type, or null if there
     * are no cached entities of the given type.
     *
     * @param et The type of entity to get.
     * @param idx The index to fetch.
     * @return the requested entity, or null.
     */
    public Entity getCache(EntityType et, int idx) {
        var list = entities.computeIfAbsent(et, t -> new ArrayList<>());
        if (list.isEmpty()) {
            return null;
        }
        if (idx > list.size() - 1) {
            return null;
        }
        return list.get(idx);
    }

    public EntityHelper2 setCache(EntityType et, List<Entity> cache) {
        entities.put(et, cache);
        return this;
    }

    /**
     * Returns half of all entity properties of the given Entity Type.
     *
     * @param entityType The entity type to get the entity properties for.
     * @param even If true, return the even-half of the properties, otherwise
     * the odd-half.
     * @return a list with the property names of half of the entity properties.
     */
    public List<String> getSelectedProperties(EntityType entityType, boolean even) {
        List<EntityPropertyMain> allProperties = new ArrayList<>(entityType.getEntityProperties());
        List<String> selectedProperties = new ArrayList<>(allProperties.size() / 2);
        for (int i = even ? 0 : 1; i < allProperties.size(); i += 2) {
            selectedProperties.add(allProperties.get(i).getName());
        }
        return selectedProperties;
    }

    /**
     * Fetches the given entity fresh from the server, returning the returned
     * JSON.
     *
     * @param entity The entity to fetch freshly.
     * @return the JSON representation of the entity as it is currently stored
     * on the server.
     */
    public JsonNode getEntity(Entity entity) {
        return getEntity(entity.getEntityType(), entity.getPrimaryKeyValues());
    }

    public JsonNode getEntity(Entity entity, List<String> select) {
        return EntityHelper2.this.getEntityJson(entity.getEntityType(), entity.getPrimaryKeyValues(), null, select, null, null);
    }

    public JsonNode getEntity(Entity entity, List<String> select, String expand) {
        return EntityHelper2.this.getEntityJson(entity.getEntityType(), entity.getPrimaryKeyValues(), null, select, expand, null);
    }

    public JsonNode getEntity(Entity entity, NavigationProperty np) {
        return EntityHelper2.this.getEntityJson(entity.getEntityType(), entity.getPrimaryKeyValues(), np, null, null, null);
    }

    public JsonNode getEntity(Entity entity, NavigationProperty np, List<String> select, String expand, String orderby) {
        return EntityHelper2.this.getEntityJson(entity.getEntityType(), entity.getPrimaryKeyValues(), np, select, expand, orderby);
    }

    public JsonNode getEntity(EntityType entityType) {
        return EntityHelper2.this.getEntityJson(entityType, null, null, null, null, null);
    }

    public JsonNode getEntity(EntityType entityType, PkValue pk) {
        return EntityHelper2.this.getEntityJson(entityType, pk, null, null, null, null);
    }

    public String createSelect(List<String> select) {
        boolean first = true;
        String result = "$select=";
        for (var item : select) {
            if (first) {
                result += item;
                first = false;
            } else {
                result += ',' + item;
            }
        }
        return result;
    }

    public String createUrl(EntityType target) {
        return sSrvc.getVersion().getUrlPart()
                + '/' + target.getMainSetName();
    }

    public String createUrl(EntityType target, Object id, String subPath) {
        return sSrvc.getVersion().getUrlPart()
                + '/' + target.getMainSetName()
                + '(' + Utils.quoteForUrl(id) + ')'
                + subPath;
    }

    public String createUrl(Entity target) {
        return sSrvc.getVersion().getUrlPart()
                + '/' + target.getEntityType().getMainSetName()
                + '(' + Utils.quoteForUrl(target.getPrimaryKeyValues().get(0)) + ')';
    }

    public Entity getEntityWithRetry(EntityType entityType, String filter, String expand, int retries) throws ServiceFailureException {
        int retry = 0;
        while (retry < retries) {
            Query query = sSrvc.dao(entityType).query();
            if (!StringHelper.isNullOrEmpty(filter)) {
                query.filter(filter);
            }
            if (!StringHelper.isNullOrEmpty(expand)) {
                query.expand(expand);
            }
            Entity entity = query.first();
            if (entity != null) {
                return entity;
            }
            retry++;
            LOGGER.debug("No data yet. Retries: {}, URL: {}", retry, entityType);
            MqttHelper2.waitMillis(MqttHelper2.WAIT_AFTER_INSERT);
        }
        LOGGER.info("Failed to read an entity from url after {} tries: {}", retries, entityType);
        return null;
    }

    public JsonNode getEntityJsonWithRetry(EntityType entityType, String expand, int retries) {
        return getEntityJsonWithRetry(entityType, null, expand, retries);
    }

    public JsonNode getEntityJsonWithRetry(EntityType entityType, String filter, String expand, int retries) {
        int retry = 0;
        while (retry < retries) {
            JsonNode entity = EntityHelper2.this.getEntityJson(entityType, filter, expand);
            if (entity != null) {
                return entity;
            }
            retry++;
            LOGGER.debug("No data yet. Retries: {}, URL: {}", retry, entityType);
            MqttHelper2.waitMillis(MqttHelper2.WAIT_AFTER_INSERT);
        }
        LOGGER.info("Failed to read an entity from url after {} tries: {}", retries, entityType);
        return null;
    }

    public JsonNode getEntityJson(EntityType entityType, String expand) {
        return getEntityJson(entityType, null, null, null, null, expand, null);
    }

    public JsonNode getEntityJson(EntityType entityType, String filter, String expand) {
        return getEntityJson(entityType, null, null, filter, null, expand, null);
    }

    public JsonNode getEntityJson(EntityType entityType, PkValue pk, NavigationProperty np, List<String> select, String expand, String orderby) {
        return getEntityJson(entityType, pk, np, null, select, expand, orderby);
    }

    public JsonNode getEntityJson(EntityType entityType, PkValue pk, NavigationProperty np, String filter, List<String> select, String expand, String orderby) {
        if (pk != null && pk.size() == 0) {
            return null;
        }
        String query = "";
        char join = '?';
        if (!StringHelper.isNullOrEmpty(expand)) {
            if (expand.startsWith("$")) {
                query += '?' + expand;
            } else {
                query += "?$expand=" + expand;
            }
            join = '&';
        }
        if (!StringHelper.isNullOrEmpty(filter)) {
            query += join + "$filter=" + URLEncoder.encode(filter, StandardCharsets.UTF_8);
            join = '&';
        }
        if (!StringHelper.isNullOrEmpty(select)) {
            query += join + createSelect(select);
            join = '&';
        }
        if (!StringHelper.isNullOrEmpty(orderby)) {
            query += join + "$orderby=" + orderby;
        }
        String urlString = sSrvc.getBaseUrl().toString()
                + entityType.mainSet;
        if (pk != null) {
            urlString += '(' + Utils.quoteForUrl(pk.get(0)) + ')';
        }
        if (np != null) {
            urlString += '/' + np.getName();
        }
        urlString += query;
        try {
            final HTTPMethods.HttpResponse result = HTTPMethods.doGet(sSrvc, urlString);
            if (StringHelper.isNullOrEmpty(result.response)) {
                return null;
            }
            final JsonNode tree = Utils.MAPPER.readTree(result.response);
            JsonNode value = tree.get("value");
            if (value == null) {
                return tree;
            } else {
                return value.get(0);
            }
        } catch (IOException e) {
            LOGGER.error("Exception:", e);
            fail("An Exception occurred during testing!: " + e.getMessage());
            return null;
        }
    }

    public List<List<NavigationProperty>> findPathsTo(EntityType et, boolean set, int maxDepth) {
        return findPathsTo(et, set, maxDepth, new ArrayList<>());
    }

    public List<List<NavigationProperty>> findPathsTo(EntityType et, boolean set, int maxDepth, List<NavigationProperty> target) {
        List<List<NavigationProperty>> result = new ArrayList<>();
        for (var np : et.getNavigationProperties()) {
            final EntityType sourceType = np.getEntityType();
            final NavigationProperty npInverse = np.getInverse();
            if (npInverse.isEntitySet() == set && !getCache(sourceType).isEmpty()) {
                List<NavigationProperty> copy = new ArrayList<>(target);
                copy.add(np);
                result.add(copy);
                LOGGER.debug(StringUtils.leftPad("{}", target.size() * 2 + 8), np.getName());
                if (maxDepth > 0) {
                    result.addAll(findPathsTo(np.getEntityType(), false, maxDepth - 1, copy));
                }
            }
        }
        return result;
    }

    public Entity newObservation(Entity datastream) {
        var list = getCache(sMdl.etObservation);
        Entity obs = sMdl.newObservation(list.size())
                .setProperty(sMdl.npObservationDatastream, datastream)
                .setProperty(EP_PHENOMENONTIME, TimeValue.create(Instant.parse("2015-03-01T00:40:00.000Z")))
                .setProperty(EP_RESULTTIME, TimeInstant.parse("2015-03-01T00:40:00.000Z"))
                .setProperty(EP_VALIDTIME, TimeInterval.create(
                        Instant.parse("2016-01-01T02:01:01+01:00"),
                        Instant.parse("2016-01-02T00:59:59+01:00")))
                .setProperty(EP_PARAMETERS, propertiesBuilder()
                        .addItem("param1", "some value1")
                        .addItem("param2", "some value2")
                        .buildMap());
        list.add(obs);
        return obs;
    }

    public Entity createObservation(Entity datastream) throws ServiceFailureException {
        Entity obs = newObservation(datastream);
        sSrvc.create(obs);
        return obs;
    }

    public Entity newObservation(Entity datastream, Entity feature) {
        return newObservation(datastream)
                .setProperty(sMdl.npObservationFeatureofinterest, feature);
    }

    public Entity createObservation(Entity datastream, Entity feature) throws ServiceFailureException {
        Entity obs = newObservation(datastream, feature);
        sSrvc.create(obs);
        return obs;
    }

    public Entity newThing() {
        final Entity newThing = sMdl.newThing("Test Thing", "This is a Test Thing");
        getCache(sMdl.etThing).add(newThing);
        return newThing;
    }

    public Entity createThing() throws ServiceFailureException {
        Entity newThing = newThing();
        sSrvc.create(newThing);
        return newThing;
    }

    public Entity newSensor() {
        final Entity newSensor = sMdl.newSensor(
                "Fuguro Barometer 1",
                "Our first Fuguro Barometer",
                "http://schema.org/description",
                "Barometer");
        getCache(sMdl.etSensor).add(newSensor);
        return newSensor;
    }

    public Entity createSensor() throws ServiceFailureException {
        Entity newSensor = newSensor();
        sSrvc.create(newSensor);
        return newSensor;
    }

    public Entity newLocation() {
        final Entity newLocation = sMdl.newLocation(
                "Rhine",
                "The river Thine",
                new Point(-32.01, 50.05));
        getCache(sMdl.etLocation).add(newLocation);
        return newLocation;
    }

    public Entity newLocation(Entity thing) {
        final Entity newLocation = newLocation()
                .addNavigationEntity(sMdl.npLocationThings, thing);
        return newLocation;
    }

    public Entity createLocation() throws ServiceFailureException {
        Entity newLocation = newLocation();
        sSrvc.create(newLocation);
        return newLocation;
    }

    public Entity createLocation(Entity thing) throws ServiceFailureException {
        Entity newLocation = newLocation(thing);
        sSrvc.create(newLocation);
        return newLocation;
    }

    public Entity newObservedProperty() {
        final Entity newObservedProperty = sMdl.newObservedProperty(
                "Dewpoint temperature",
                "http://dbpedia.org/page/Dew_point",
                "The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form.");
        getCache(sMdl.etObservedProperty).add(newObservedProperty);
        return newObservedProperty;
    }

    public Entity createObservedProperty() throws ServiceFailureException {
        Entity newObservedProperty = newObservedProperty();
        sSrvc.create(newObservedProperty);
        return newObservedProperty;
    }

    public Entity newFeatureOfInterest() {
        final Entity newFeatureOfInterest = sMdl.newFeatureOfInterest(
                "Weather Station 1",
                "The weather station in my garden.",
                new Point(10.0, 10.0));
        getCache(sMdl.etFeatureOfInterest).add(newFeatureOfInterest);
        return newFeatureOfInterest;
    }

    public Entity createFeatureOfInterest() throws ServiceFailureException {
        Entity newFeatureOfInterest = newFeatureOfInterest();
        sSrvc.create(newFeatureOfInterest);
        return newFeatureOfInterest;
    }

    public Entity newDatastream(Entity observedProperty, Entity sensor) {
        final Entity newDatastream = sMdl.newDatastream(
                "test datastream",
                "A datatream for testing",
                new UnitOfMeasurement("Celcius", "degC", "http://qudt.org/vocab/unit#DegreeCelsius"))
                .setProperty(EP_PHENOMENONTIMEDS, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"))
                .setProperty(EP_RESULTTIMEDS, TimeInterval.parse("2014-03-01T13:00:00Z/2015-05-11T15:30:00Z"))
                .setProperty(sMdl.npDatastreamSensor, sensor)
                .setProperty(sMdl.npDatastreamObservedproperty, observedProperty);
        getCache(sMdl.etDatastream).add(newDatastream);
        return newDatastream;
    }

    public Entity newDatastream(Entity thing, Entity observedProperty, Entity sensor) {
        final Entity newDatastream = newDatastream(observedProperty, sensor)
                .setProperty(sMdl.npDatastreamThing, thing);
        return newDatastream;
    }

    public Entity createDatastream(Entity thing, Entity observedProperty, Entity sensor) throws ServiceFailureException {
        final Entity newDatastream = newDatastream(thing, observedProperty, sensor);
        sSrvc.create(newDatastream);
        return newDatastream;
    }

    public Entity newHistoricalLocation(Entity thing, Entity location) {
        final Entity newHistoricalLocation = sMdl.newHistoricalLocation()
                .setProperty(EP_TIME, TimeInstant.parse("2015-03-01T00:40:00.000Z"))
                .setProperty(sMdl.npHistlocThing, thing)
                .addNavigationEntity(sMdl.npHistlocLocations, location);
        getCache(sMdl.etHistoricalLocation).add(newHistoricalLocation);
        return newHistoricalLocation;
    }

    public Entity createHistoricalLocation(Entity thing, Entity location) throws ServiceFailureException {
        Entity newHistoricalLocation = newHistoricalLocation(thing, location);
        sSrvc.create(newHistoricalLocation);
        return newHistoricalLocation;
    }

    public Entity patchEntity(Entity original) throws ServiceFailureException {
        Entity copy = original.withOnlyPk();
        changeEntity(copy);
        sSrvc.update(copy);
        return copy;
    }

    public List<String> changeEntity(Entity original) throws IllegalArgumentException {
        switch (original.getEntityType().entityName) {
            case NAME_THING:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName());

            case NAME_SENSOR:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_ENCODINGTYPE, "Updated Encoding")
                        .setProperty(EP_METADATA, "Updated Metadata");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_ENCODINGTYPE.getName(),
                        EP_METADATA.getName());

            case NAME_LOCATION:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_ENCODINGTYPE, "Updated Encoding")
                        .setProperty(EP_LOCATION, "Updated Location");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_ENCODINGTYPE.getName(),
                        EP_LOCATION.getName());

            case NAME_OBSERVEDPROPERTY:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_DEFINITION, "Updated Definition");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_DEFINITION.getName());

            case NAME_FEATUREOFINTEREST:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_ENCODINGTYPE, "Updated Encoding")
                        .setProperty(EP_FEATURE, "Updated Feature");
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_ENCODINGTYPE.getName(),
                        EP_FEATURE.getName());

            case NAME_DATASTREAM:
                original.setProperty(EP_NAME, "UpdatedName")
                        .setProperty(EP_DESCRIPTION, "Updated Description")
                        .setProperty(EP_OBSERVATIONTYPE, "Updated Description")
                        .setProperty(EP_UNITOFMEASUREMENT, new UnitOfMeasurement("Updated Name", "Updated Symbol", "Updated Definition"));
                return Arrays.asList(
                        EP_NAME.getName(),
                        EP_DESCRIPTION.getName(),
                        EP_OBSERVATIONTYPE.getName(),
                        EP_UNITOFMEASUREMENT.getName());

            case NAME_OBSERVATION:
                original.setProperty(EP_RESULT, 43)
                        .setProperty(EP_PHENOMENONTIME, TimeValue.create(Instant.parse("2016-03-01T00:40:00.000Z")))
                        .setProperty(EP_RESULTTIME, TimeInstant.parse("2016-03-01T00:40:00.000Z"))
                        .setProperty(EP_VALIDTIME, TimeInterval.create(
                                Instant.parse("2017-01-01T02:01:01+01:00"),
                                Instant.parse("2017-01-02T00:59:59+01:00")));
                return Arrays.asList(
                        EP_RESULT.getName(),
                        EP_PHENOMENONTIME.getName(),
                        EP_RESULTTIME.getName(),
                        EP_VALIDTIME.getName());

            case NAME_HISTORICALLOCATION:
                original.setProperty(EP_TIME, TimeInstant.parse("2016-03-01T00:40:00.000Z"));
                return Arrays.asList(
                        EP_TIME.getName());

            default:
                throw new IllegalArgumentException("Don't know how to patch a " + original.getEntityType());

        }
    }

    public Entity putEntity(Entity original) throws ServiceFailureException {
        changeEntity(original);
        sendHttpPutEntity(original);
        return original;
    }

    public JsonNode sendHttpPutEntity(Entity entity) {
        EntityType entityType = entity.getEntityType();
        PkValue pk = entity.getPrimaryKeyValues();
        String urlString = sSrvc.getBaseUrl().toString()
                + '/' + entityType.mainSet
                + '(' + Utils.quoteForUrl(pk.get(0)) + ')';
        try {
            String data = JsonWriter.writeEntity(entity);
            HTTPMethods.HttpResponse responseMap = HTTPMethods.doPut(urlString, data);
            int responseCode = responseMap.code;
            String message = "Error during updating(PUT) of entity " + entityType.entityName + ": " + responseMap.response;
            assertEquals(200, responseCode, message);
            responseMap = HTTPMethods.doGet(urlString);
            JsonNode result = Utils.MAPPER.readTree(responseMap.response);
            return result;

        } catch (IOException e) {
            LOGGER.error("Exception:", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
            return null;
        }
    }

}
