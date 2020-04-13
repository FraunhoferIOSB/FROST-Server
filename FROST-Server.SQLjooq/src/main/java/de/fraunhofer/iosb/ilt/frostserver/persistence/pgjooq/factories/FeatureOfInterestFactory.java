/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableFeatures;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <J> The type of the ID fields.
 */
public class FeatureOfInterestFactory<J extends Comparable> implements EntityFactory<FeatureOfInterest, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureOfInterestFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableFeatures<J> table;

    public FeatureOfInterestFactory(EntityFactories<J> factories, AbstractTableFeatures<J> table) {
        this.entityFactories = factories;
        this.table = table;
    }

    @Override
    public FeatureOfInterest create(Record record, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        FeatureOfInterest entity = new FeatureOfInterest();
        J id = getFieldOrNull(record, table.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setName(getFieldOrNull(record, table.name));
        entity.setDescription(getFieldOrNull(record, table.description));
        String encodingType = getFieldOrNull(record, table.encodingType);
        entity.setEncodingType(encodingType);
        if (select.isEmpty() || select.contains(EntityProperty.FEATURE)) {
            String locationString = getFieldOrNull(record, table.feature);
            dataSize.increase(locationString == null ? 0 : locationString.length());
            entity.setFeature(Utils.locationFromEncoding(encodingType, locationString));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = getFieldOrNull(record, table.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, FeatureOfInterest foi) throws IncompleteEntityException {
        // No linked entities to check first.
        Map<Field, Object> insert = new HashMap<>();
        insert.put(table.name, foi.getName());
        insert.put(table.description, foi.getDescription());
        insert.put(table.properties, EntityFactories.objectToJson(foi.getProperties()));

        String encodingType = foi.getEncodingType();
        insert.put(table.encodingType, encodingType);
        EntityFactories.insertGeometry(insert, table.feature, table.geom, encodingType, foi.getFeature());

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), foi);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J generatedId = result.component1();
        LOGGER.debug("Inserted FeatureOfInterest. Created id = {}.", generatedId);
        foi.setId(entityFactories.idFromObject(generatedId));
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, FeatureOfInterest foi, J foiId) throws NoSuchEntityException, IncompleteEntityException {
        DSLContext dslContext = pm.getDslContext();
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        updateName(foi, update, message);
        updateDescription(foi, update, message);
        updateProperties(foi, update, message);
        updateFeatureAndEncoding(foi, update, message, dslContext, foiId);

        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(foiId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating FeatureOfInterest {} caused {} rows to change!", foiId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingObservations(foi, pm, dslContext, foiId);

        LOGGER.debug("Updated FeatureOfInterest {}", foiId);
        return message;
    }

    private void updateName(FeatureOfInterest foi, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (foi.isSetName()) {
            if (foi.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.name, foi.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void updateDescription(FeatureOfInterest foi, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (foi.isSetDescription()) {
            if (foi.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.description, foi.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateProperties(FeatureOfInterest foi, Map<Field, Object> update, EntityChangedMessage message) {
        if (foi.isSetProperties()) {
            update.put(table.properties, EntityFactories.objectToJson(foi.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateFeatureAndEncoding(FeatureOfInterest foi, Map<Field, Object> update, EntityChangedMessage message, DSLContext dslContext, J foiId) throws IncompleteEntityException {
        if (foi.isSetEncodingType() && foi.getEncodingType() == null) {
            throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
        }
        if (foi.isSetFeature() && foi.getFeature() == null) {
            throw new IncompleteEntityException("feature" + CAN_NOT_BE_NULL);
        }
        if (foi.isSetEncodingType() && foi.getEncodingType() != null && foi.isSetFeature() && foi.getFeature() != null) {
            String encodingType = foi.getEncodingType();
            update.put(table.encodingType, encodingType);
            EntityFactories.insertGeometry(update, table.feature, table.geom, encodingType, foi.getFeature());
            message.addField(EntityProperty.ENCODINGTYPE);
            message.addField(EntityProperty.FEATURE);
        } else if (foi.isSetEncodingType() && foi.getEncodingType() != null) {
            String encodingType = foi.getEncodingType();
            update.put(table.encodingType, encodingType);
            message.addField(EntityProperty.ENCODINGTYPE);
        } else if (foi.isSetFeature() && foi.getFeature() != null) {
            String encodingType = dslContext.select(table.encodingType)
                    .from(table)
                    .where(table.getId().eq(foiId))
                    .fetchOne(table.encodingType);
            Object parsedObject = EntityFactories.reParseGeometry(encodingType, foi.getFeature());
            EntityFactories.insertGeometry(update, table.feature, table.geom, encodingType, parsedObject);
            message.addField(EntityProperty.FEATURE);
        }
    }

    private void linkExistingObservations(FeatureOfInterest foi, PostgresPersistenceManager<J> pm, DSLContext dslContext, J foiId) throws NoSuchEntityException {
        // Link existing Observations to the FeatureOfInterest.
        for (Observation o : foi.getObservations()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J obsId = (J) o.getId().getValue();
            AbstractTableObservations<J> obsTable = entityFactories.tableCollection.tableObservations;
            long oCount = dslContext.update(obsTable)
                    .set(obsTable.getFeatureId(), foiId)
                    .where(obsTable.getId().eq(obsId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned FeatureOfInterest {} to Observation {}.", foiId, obsId);
            }
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.getDslContext()
                .delete(table)
                .where(table.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("FeatureOfInterest " + entityId + " not found.");
        }
        // Delete references to the FoI in the Locations table.
        AbstractTableLocations<J> tLoc = entityFactories.tableCollection.tableLocations;
        pm.getDslContext()
                .update(tLoc)
                .set(tLoc.getGenFoiId(), (J) null)
                .where(tLoc.getGenFoiId().eq(entityId))
                .execute();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FEATUREOFINTEREST;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return table.getId();
    }

}
