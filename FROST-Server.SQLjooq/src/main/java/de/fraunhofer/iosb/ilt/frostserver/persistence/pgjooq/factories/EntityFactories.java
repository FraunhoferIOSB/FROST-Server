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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.GeoJsonSerializer;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.UTC;
import static de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils.idFromObject;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.jackson.CrsType;
import org.geolatte.common.dataformats.json.jackson.JsonException;
import org.geolatte.geom.Geometry;
import org.joda.time.Interval;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 */
public class EntityFactories {

    public static final String CAN_NOT_BE_NULL = " can not be null.";
    public static final String CHANGED_MULTIPLE_ROWS = "Update changed multiple rows.";
    public static final String NO_ID_OR_NOT_FOUND = " with no id or non existing.";
    public static final String CREATED_HL = "Created historicalLocation {}";
    public static final String LINKED_L_TO_HL = "Linked location {} to historicalLocation {}.";
    public static final String UNLINKED_L_FROM_T = "Unlinked {} locations from Thing {}.";
    public static final String LINKED_L_TO_T = "Linked Location {} to Thing {}.";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityFactories.class);
    private static final Field NULL_FIELD = DSL.field("null", Object.class);

    private static ObjectMapper formatter;

    private final ModelRegistry modelRegistry;
    private final TableCollection tableCollection;

    public EntityFactories(ModelRegistry modelRegistry, TableCollection tableCollection) {
        this.modelRegistry = modelRegistry;
        this.tableCollection = tableCollection;

    }

    public ModelRegistry getModelRegistry() {
        return modelRegistry;
    }

    public TableCollection getTableCollection() {
        return tableCollection;
    }

    public static Entity entityFromId(EntityType entityType, Record tuple, Field<?> path) {
        return entityFromId(entityType, getFieldOrNull(tuple, path));
    }

    public static Entity entityFromId(EntityType entityType, Object id) {
        if (id == null) {
            return null;
        }
        return new DefaultEntity(entityType, idFromObject(id));
    }

    public void insertUserDefinedId(PostgresPersistenceManager pm, Map<Field, Object> clause, Field<?> idField, Entity entity) throws IncompleteEntityException {
        if (pm.useClientSuppliedId(entity)) {
            pm.modifyClientSuppliedId(entity);
            clause.put(idField, entity.getId().getValue());
        }
    }

    /**
     * Throws an exception if the entity has an id, but does not exist or if the
     * entity can not be created.
     *
     * @param pm the persistenceManager
     * @param e The Entity to check.
     * @throws NoSuchEntityException If the entity has an id, but does not
     * exist.
     * @throws IncompleteEntityException If the entity has no id, but is not
     * complete and can thus not be created.
     */
    public void entityExistsOrCreate(PostgresPersistenceManager pm, Entity e) throws NoSuchEntityException, IncompleteEntityException {
        if (e == null) {
            throw new NoSuchEntityException("No entity!");
        }

        if (e.getId() == null) {
            e.complete();
            // no id but complete -> create
            pm.insert(e);
            return;
        }

        if (entityExists(pm, e)) {
            return;
        }

        // check if this is an incomplete entity
        try {
            e.complete();
        } catch (IncompleteEntityException exc) {
            // not complete and link entity does not exist
            throw new NoSuchEntityException("No such entity '" + e.getEntityType() + "' with id " + e.getId().getValue());
        }

        // complete with id -> create
        pm.insert(e);
    }

    public boolean entityExists(PostgresPersistenceManager pm, EntityType type, Id entityId) {
        Object id = entityId.getValue();
        StaMainTable<?> table = tableCollection.getTableForType(type);

        DSLContext dslContext = pm.getDslContext();

        Integer count = dslContext.selectCount()
                .from(table)
                .where(table.getId().equal(id))
                .fetchOne()
                .component1();

        if (count > 1) {
            LOGGER.error("More than one instance of {} with id {}.", type, id);
        }
        return count > 0;

    }

    public boolean entityExists(PostgresPersistenceManager pm, Entity e) {
        if (e == null || e.getId() == null) {
            return false;
        }
        return entityExists(pm, e.getEntityType(), e.getId());
    }

    public static void insertTimeValue(Map<Field, Object> clause, Field<OffsetDateTime> startField, Field<OffsetDateTime> endField, TimeValue time) {
        if (time.isInstant()) {
            TimeInstant timeInstant = time.getInstant();
            insertTimeInstant(clause, endField, timeInstant);
            insertTimeInstant(clause, startField, timeInstant);
        } else if (time.isInterval()) {
            TimeInterval timeInterval = time.getInterval();
            insertTimeInterval(clause, startField, endField, timeInterval);
        }
    }

    public static void insertTimeInstant(Map<Field, Object> clause, Field<OffsetDateTime> field, TimeInstant time) {
        if (time == null) {
            return;
        }
        clause.put(field, time.getOffsetDateTime());
    }

    public static void insertTimeInterval(Map<Field, Object> clause, Field<OffsetDateTime> startField, Field<OffsetDateTime> endField, TimeInterval time) {
        if (time == null) {
            return;
        }
        Interval interval = time.getInterval();
        clause.put(startField, OffsetDateTime.ofInstant(Instant.ofEpochMilli(interval.getStartMillis()), UTC));
        clause.put(endField, OffsetDateTime.ofInstant(Instant.ofEpochMilli(interval.getEndMillis()), UTC));
    }

    /**
     * Sets both the geometry and location in the clause.
     *
     * @param clause The insert or update clause to add to.
     * @param locationPath The path to the location column.
     * @param geomPath The path to the geometry column.
     * @param encodingType The encoding type.
     * @param location The location.
     */
    public static void insertGeometry(Map<Field, Object> clause, Field<String> locationPath, Field<? extends Object> geomPath, String encodingType, final Object location) {
        if (encodingType == null && location instanceof GeoJsonObject) {
            encodingType = GeoJsonDeserializier.APPLICATION_GEOJSON;
        }
        if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
            insertGeometryKnownEncoding(location, clause, geomPath, locationPath);
        } else {
            String json;
            json = objectToJson(location);
            clause.put(geomPath, NULL_FIELD);
            if (locationPath != null) {
                clause.put(locationPath, json);
            }
        }
    }

    private static void insertGeometryKnownEncoding(final Object location, Map<Field, Object> clause, Field<? extends Object> geomPath, Field<String> locationPath) {
        String locJson;
        try {
            locJson = new GeoJsonSerializer().serialize(location);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to store.", ex);
            throw new IllegalArgumentException("encoding specifies geoJson, but location not parsable as such.");
        }

        // Postgres does not support Feature.
        Object geoLocation = location;
        if (location instanceof Feature) {
            geoLocation = ((Feature) location).getGeometry();
        }
        // Ensure the geoJson has a crs, otherwise Postgres complains.
        if (geoLocation instanceof GeoJsonObject) {
            GeoJsonObject geoJsonObject = (GeoJsonObject) geoLocation;
            Crs crs = geoJsonObject.getCrs();
            if (crs == null) {
                crs = new Crs();
                crs.setType(CrsType.name);
                crs.getProperties().put("name", "EPSG:4326");
                geoJsonObject.setCrs(crs);
            }
        }
        String geoJson;
        try {
            geoJson = new GeoJsonSerializer().serialize(geoLocation);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to store.", ex);
            throw new IllegalArgumentException("encoding specifies geoJson, but location not parsable as such.");
        }

        try {
            // geojson.jackson allows invalid polygons, geolatte catches those.
            Utils.getGeoJsonMapper().fromJson(geoJson, Geometry.class);
        } catch (JsonException ex) {
            throw new IllegalArgumentException("Invalid geoJson: " + ex.getMessage());
        }
        final String template = "ST_Force2D(ST_Transform(ST_GeomFromGeoJSON({0}), 4326))";
        clause.put(geomPath, DSL.field(template, Object.class, geoJson));
        if (locationPath != null) {
            clause.put(locationPath, locJson);
        }
    }

    public static Object reParseGeometry(String encodingType, Object object) {
        String json = objectToJson(object);
        return Utils.locationFromEncoding(encodingType, json);
    }

    public static String objectToJson(JsonValue jsonValue) {
        return objectToJson(jsonValue.getValue());
    }

    public static String objectToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return getFormatter().writeValueAsString(object);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not serialise object.", ex);
        }
    }

    public static ObjectMapper getFormatter() {
        if (formatter == null) {
            formatter = SimpleJsonMapper.getSimpleObjectMapper();
        }
        return formatter;
    }

}
