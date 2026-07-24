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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.json.serialize.JsonWriter;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntitySet;
import de.fraunhofer.iosb.ilt.frostclient.model.EntityType;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostclient.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

public abstract class EntityHelperAbstract {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHelperAbstract.class);

    public final Map<EntityType, List<Entity>> entities = new HashMap<>();

    protected final SensorThingsService sSrvc;

    public EntityHelperAbstract(SensorThingsService sSrvc) {
        this.sSrvc = sSrvc;
    }

    public final void clearCaches() {
        entities.clear();
    }

    /**
     * Add the given entity to the entity cache.
     *
     * @param entity the entity to add to the cache.
     */
    public final void cache(Entity entity) {
        getCache(entity.getType()).add(entity);
    }

    /**
     * Get the cache for the given entity type. If no cache exists yet, a new
     * one is created. This method never returns null.
     *
     * @param et the entity type to get the cache for.
     * @return the requested entity cache.
     */
    public final List<Entity> getCache(EntityType et) {
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
    public final Entity getCache(EntityType et, int idx) {
        var list = entities.computeIfAbsent(et, t -> new ArrayList<>());
        if (list.isEmpty()) {
            return null;
        }
        if (idx > list.size() - 1) {
            return null;
        }
        return list.get(idx);
    }

    public final EntityHelperAbstract setCache(EntityType et, List<Entity> cache) {
        entities.put(et, cache);
        return this;
    }

    public static List<String> selectAll(EntityType et) {
        return et.getEntityProperties().stream().map(p -> p.getName()).toList();
    }

    public List<String> selectAllWithId(EntityType et) {
        List<String> list = new ArrayList<>();
        list.add(sSrvc.getVersion().selfLinkName);
        list.addAll(selectAll(et));
        return list;
    }

    /**
     * Returns half of all entity properties of the given Entity Type.
     *
     * @param entityType The entity type to get the entity properties for.
     * @param even If true, return the even-half of the properties, otherwise
     * the odd-half.
     * @return a list with the property names of half of the entity properties.
     */
    public final List<String> getSelectedProperties(EntityType entityType, boolean even) {
        List<EntityPropertyMain> allProperties = new ArrayList<>(entityType.getEntityProperties());
        List<String> selectedProperties = new ArrayList<>(allProperties.size() / 2);
        for (int i = even ? 0 : 1; i < allProperties.size(); i += 2) {
            selectedProperties.add(allProperties.get(i).getName());
        }
        return selectedProperties;
    }

    public final SensorThingsService getsSrvc() {
        return sSrvc;
    }

    /**
     * Fetches the given entity fresh from the server, returning the returned
     * JSON.
     *
     * @param entity The entity to fetch freshly.
     * @param modifiers A set of modifier to apply to the returned String before
     * parsing.
     * @return the JSON representation of the entity as it is currently stored
     * on the server.
     */
    public final JsonNode getEntity(Entity entity, StringModifier... modifiers) {
        return getEntityJson(entity.getType(), entity.getPrimaryKeyValues(), null, null, null, null, modifiers);
    }

    public final JsonNode getEntity(Entity entity, List<String> select, StringModifier... modifiers) {
        return getEntityJson(entity.getType(), entity.getPrimaryKeyValues(), null, select, null, null, modifiers);
    }

    public final JsonNode getEntity(Entity entity, List<String> select, String expand, StringModifier... modifiers) {
        return getEntityJson(entity.getType(), entity.getPrimaryKeyValues(), null, select, expand, null, modifiers);
    }

    public final JsonNode getEntity(Entity entity, NavigationProperty np, StringModifier... modifiers) {
        return getEntityJson(entity.getType(), entity.getPrimaryKeyValues(), np, null, null, null, modifiers);
    }

    public final JsonNode getEntity(Entity entity, NavigationProperty np, List<String> select, StringModifier... modifiers) {
        return getEntityJson(entity.getType(), entity.getPrimaryKeyValues(), np, select, null, null, modifiers);
    }

    public final JsonNode getEntity(Entity entity, NavigationProperty np, List<String> select, String expand, String orderby, StringModifier... modifiers) {
        return getEntityJson(entity.getType(), entity.getPrimaryKeyValues(), np, select, expand, orderby, modifiers);
    }

    public final JsonNode getEntity(EntityType entityType, StringModifier... modifiers) {
        return getEntityJson(entityType, null, null, null, null, null, modifiers);
    }

    public final JsonNode getEntity(EntityType entityType, PkValue pk, StringModifier... modifiers) {
        return getEntityJson(entityType, pk, null, null, null, null, modifiers);
    }

    public final String createSelect(List<String> select) {
        boolean first = true;
        StringBuilder result = new StringBuilder("$select=");
        for (var item : select) {
            if (first) {
                result.append(item);
                first = false;
            } else {
                result.append(',').append(item);
            }
        }
        return result.toString();
    }

    public final String createUrl(EntityType target) {
        return sSrvc.getVersion().getUrlPart()
                + '/' + target.getMainSetName();
    }

    public final String createUrl(EntityType target, Object id, String subPath) {
        return sSrvc.getVersion().getUrlPart()
                + '/' + target.getMainSetName()
                + '(' + Utils.quoteForUrl(id) + ')'
                + subPath;
    }

    public final String createUrl(Entity target) {
        return sSrvc.getVersion().getUrlPart()
                + '/' + target.getType().getMainSetName()
                + '(' + Utils.quoteForUrl(target.getPrimaryKeyValues().get(0)) + ')';
    }

    public final Entity getEntityWithRetry(EntityType entityType, String filter, String expand, int retries) throws ServiceFailureException {
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
                LOGGER.debug("Found entity of type {} after {} tries with filter {}: {}", entityType, retry, filter, entity);
                return entity;
            }
            retry++;
            LOGGER.debug("No data yet. Retries: {}, URL: {}", retry, entityType);
            MqttHelper11.waitMillis(MqttHelper11.WAIT_AFTER_INSERT);
        }
        LOGGER.debug("Failed to read an entity of type {} after {} tries with filter {}", entityType, retries, filter);
        return null;
    }

    public final JsonNode getEntityJsonWithRetry(EntityType entityType, String expand, int retries, StringModifier... modifiers) {
        return getEntityJsonWithRetry(entityType, null, expand, retries, modifiers);
    }

    public final JsonNode getEntityJsonWithRetry(EntityType entityType, String filter, String expand, int retries, StringModifier... modifiers) {
        int retry = 0;
        while (retry < retries) {
            JsonNode entity = getEntityJson(entityType, null, null, filter, null, expand, null, modifiers);
            if (entity != null) {
                return entity;
            }
            retry++;
            // We have to retry, thus decrement the count by one.
            HTTPMethods.decrementCountGet();
            LOGGER.debug("No data yet. Retries: {}, URL: {}", retry, entityType);
            MqttHelper11.waitMillis(MqttHelper11.WAIT_AFTER_INSERT);
        }
        // All attempts failed and were not counted. Do count one!
        HTTPMethods.incrementCountGet();
        LOGGER.debug("Failed to read an entity from url after {} tries: {}", retries, entityType);
        return null;
    }

    public final JsonNode getEntityJson(EntityType entityType, String expand, StringModifier... modifiers) {
        return getEntityJson(entityType, null, null, null, null, expand, null, modifiers);
    }

    public final JsonNode getEntityJson(EntityType entityType, List<String> select, String filter, String expand, StringModifier... modifiers) {
        return getEntityJson(entityType, null, null, filter, select, expand, null, modifiers);
    }

    public final JsonNode getEntityJson(EntityType entityType, PkValue pk, String expand, StringModifier... modifiers) {
        return getEntityJson(entityType, pk, null, selectAllWithId(entityType), expand, null, modifiers);
    }

    public final JsonNode getEntityJson(EntityType entityType, PkValue pk, List<String> select, String expand, StringModifier... modifiers) {
        return getEntityJson(entityType, pk, null, select, expand, null, modifiers);
    }

    public final JsonNode getEntityJson(EntityType entityType, PkValue pk, NavigationProperty np, List<String> select, String expand, String orderby, StringModifier... modifiers) {
        return getEntityJson(entityType, pk, np, null, select, expand, orderby, modifiers);
    }

    public final JsonNode getEntityJson(EntityType entityType, PkValue pk, NavigationProperty np, String filter, List<String> select, String expand, String orderby, StringModifier... modifiers) {
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
            String data = result.response;
            if (StringHelper.isNullOrEmpty(data)) {
                return null;
            }
            for (var modifier : modifiers) {
                data = modifier.modify(data);
            }
            final JsonNode tree = Utils.MAPPER.readTree(data);
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

    public final List<List<String>> findPathsTo(Entity e, boolean set, int maxDepth) throws ServiceFailureException {
        return findPathsTo(e, set, maxDepth, new ArrayList<>(), new ArrayList<>());
    }

    private List<List<String>> findPathsTo(Entity e, boolean set, int maxDepth, List<String> base, List<List<String>> target) throws ServiceFailureException {
        EntityType et = e.getType();
        if (target == null) {
            target = new ArrayList<>();
        }
        for (var np : et.getNavigationEntities()) {
            Entity sourceEntity = e.getProperty(np);
            if (sourceEntity != null) {
                findPathsTo(sourceEntity, np, set, maxDepth, base, target);
            }
        }
        for (var np : et.getNavigationSets()) {
            EntitySet entitySet = e.getProperty(np);
            final Iterator<Entity> iterator = entitySet.iterator();
            if (iterator.hasNext()) {
                findPathsTo(iterator.next(), np, set, maxDepth, base, target);
            }
        }
        return target;
    }

    private void findPathsTo(Entity sourceEntity, NavigationProperty np, boolean set, int maxDepth, List<String> base, List<List<String>> target) throws ServiceFailureException {
        final NavigationProperty npInverse = np.getInverse();
        if (npInverse.isEntitySet() == set) {
            List<String> copy = new ArrayList<>(base);
            String item = npInverse.getName();
            copy.add(item);
            LOGGER.debug(StringUtils.leftPad("{}", copy.size() * 2 + 8), item);
            target.add(copy);
            if (maxDepth > 0) {
                findPathsTo(sourceEntity, false, maxDepth - 1, copy, target);
            }
            final String finalItem = sourceEntity.getSelfLink(false);
            copy.add(finalItem);
            LOGGER.debug(StringUtils.leftPad("{}", copy.size() * 2 + 8), finalItem);
        }
    }

    public final Entity patchEntity(Entity original) throws ServiceFailureException {
        Entity copy = original.withOnlyPk();
        changeEntity(copy);
        sSrvc.update(copy);
        return copy;
    }

    public abstract List<String> changeEntity(Entity original) throws IllegalArgumentException;

    public final Entity putEntity(Entity original) {
        changeEntity(original);
        sendHttpPutEntity(original);
        return original;
    }

    public final JsonNode sendHttpPutEntity(Entity entity) {
        EntityType entityType = entity.getType();
        PkValue pk = entity.getPrimaryKeyValues();
        String urlString = sSrvc.getBaseUrl().toString()
                + entityType.mainSet
                + '(' + Utils.quoteForUrl(pk.get(0)) + ')';
        try {
            String data = JsonWriter.writeEntity(sSrvc.getVersion(), entity);
            HTTPMethods.HttpResponse responseMap = HTTPMethods.doPut(urlString, data);
            int responseCode = responseMap.code;
            String message = "Error during updating(PUT) of entity " + entityType.getName() + ": " + responseMap.response;
            assertEquals(200, responseCode, message);
            responseMap = HTTPMethods.doGet(urlString);
            return Utils.MAPPER.readTree(responseMap.response);

        } catch (JacksonException e) {
            LOGGER.error("Exception:", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
            return null;
        }
    }

    public abstract Entity newObservation(Entity datastream);

    public abstract Entity createObservation(Entity datastream) throws ServiceFailureException;

    public abstract Entity newObservation(Entity datastream, Entity feature);

    public abstract Entity createObservation(Entity datastream, Entity feature) throws ServiceFailureException;

    public abstract Entity newThing();

    public abstract Entity createThing() throws ServiceFailureException;

    public abstract Entity newSensor();

    public abstract Entity createSensor() throws ServiceFailureException;

    public abstract Entity newLocation();

    public abstract Entity newLocation(Entity thing);

    public abstract Entity createLocation() throws ServiceFailureException;

    public abstract Entity createLocation(Entity thing) throws ServiceFailureException;

    public abstract Entity newObservedProperty();

    public abstract Entity createObservedProperty() throws ServiceFailureException;

    public abstract Entity newFeatureOfInterest(int idx);

    public abstract Entity createFeatureOfInterest(int idx) throws ServiceFailureException;

    public abstract Entity newDatastream(Entity observedProperty, Entity sensor);

    public abstract Entity newDatastream(Entity thing, Entity observedProperty, Entity sensor);

    public abstract Entity createDatastream(Entity thing, Entity observedProperty, Entity sensor) throws ServiceFailureException;

    public abstract Entity newHistoricalLocation(Entity thing, Entity location);

    public abstract Entity createHistoricalLocation(Entity thing, Entity location) throws ServiceFailureException;

    public static class EntityCreateInfo {

        public final EntityType et;
        public final int count;

        public EntityCreateInfo(EntityType et, int count) {
            this.et = et;
            this.count = count;
        }

        public static EntityCreateInfo of(EntityType et, int count) {
            return new EntityCreateInfo(et, count);
        }

        @Override
        public String toString() {
            return et.name + ":" + count;
        }

    }

    public static interface StringModifier {

        public String modify(String input);
    }
}
