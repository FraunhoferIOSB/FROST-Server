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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories;

import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <J> The type of the ID fields.
 */
public class FeatureOfInterestFactory<J> implements EntityFactory<FeatureOfInterest, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureOfInterestFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableFeatures<J> qInstance;
    private final QCollection<J> qCollection;

    public FeatureOfInterestFactory(EntityFactories<J> factories, AbstractTableFeatures<J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public FeatureOfInterest create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        FeatureOfInterest entity = new FeatureOfInterest();
        J id = entityFactories.getIdFromRecord(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setName(tuple.get(qInstance.name));
        entity.setDescription(tuple.get(qInstance.description));
        String encodingType = tuple.get(qInstance.encodingType);
        entity.setEncodingType(encodingType);
        if (select.isEmpty() || select.contains(EntityProperty.FEATURE)) {
            String locationString = tuple.get(qInstance.feature);
            dataSize.increase(locationString == null ? 0 : locationString.length());
            entity.setFeature(Utils.locationFromEncoding(encodingType, locationString));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, FeatureOfInterest foi) throws IncompleteEntityException {
        // No linked entities to check first.
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableFeatures<J> qfoi = qCollection.qFeatures;
        AbstractRecordFeatures<J> newFoi = dslContext.newRecord(qfoi);
        newFoi.set(qfoi.name, foi.getName());
        newFoi.set(qfoi.description, foi.getDescription());
        newFoi.set(qfoi.properties, EntityFactories.objectToJson(foi.getProperties()));

        String encodingType = foi.getEncodingType();
        newFoi.set(qfoi.encodingType, encodingType);
        // TODO: This will probably need a Binding
        // EntityFactories.insertGeometry(insert, qfoi.feature, qfoi.geom, encodingType, foi.getFeature());

        entityFactories.insertUserDefinedId(pm, newFoi, qfoi.getId(), foi);

        newFoi.store();
        J generatedId = newFoi.getId();
        LOGGER.debug("Inserted FeatureOfInterest. Created id = {}.", generatedId);
        foi.setId(entityFactories.idFromObject(generatedId));
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, FeatureOfInterest foi, J foiId) throws NoSuchEntityException, IncompleteEntityException {
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableFeatures<J> qfoi = qCollection.qFeatures;
        AbstractRecordFeatures<J> update = dslContext.newRecord(qfoi);
        EntityChangedMessage message = new EntityChangedMessage();

        updateName(foi, update, qfoi, message);
        updateDescription(foi, update, qfoi, message);
        updateProperties(foi, update, qfoi, message);
        updateFeatureAndEncoding(foi, update, qfoi, message, dslContext, foiId);
        update.setId(foiId);

        long count = 0;
        if (update.changed()) {
            count = update.store();
        }
        if (count > 1) {
            LOGGER.error("Updating FeatureOfInterest {} caused {} rows to change!", foiId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingObservations(foi, pm, dslContext, foiId);

        LOGGER.debug("Updated FeatureOfInterest {}", foiId);
        return message;
    }

    private void updateName(FeatureOfInterest foi, AbstractRecordFeatures<J> update, AbstractTableFeatures<J> qfoi, EntityChangedMessage message) throws IncompleteEntityException {
        if (foi.isSetName()) {
            if (foi.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qfoi.name, foi.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void updateDescription(FeatureOfInterest foi, AbstractRecordFeatures<J> update, AbstractTableFeatures<J> qfoi, EntityChangedMessage message) throws IncompleteEntityException {
        if (foi.isSetDescription()) {
            if (foi.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qfoi.description, foi.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateProperties(FeatureOfInterest foi, AbstractRecordFeatures<J> update, AbstractTableFeatures<J> qfoi, EntityChangedMessage message) {
        if (foi.isSetProperties()) {
            update.set(qfoi.properties, EntityFactories.objectToJson(foi.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateFeatureAndEncoding(FeatureOfInterest foi, AbstractRecordFeatures<J> update, AbstractTableFeatures<J> qfoi, EntityChangedMessage message, DSLContext qFactory, J foiId) throws IncompleteEntityException {
        if (foi.isSetEncodingType() && foi.getEncodingType() == null) {
            throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
        }
        if (foi.isSetFeature() && foi.getFeature() == null) {
            throw new IncompleteEntityException("feature" + CAN_NOT_BE_NULL);
        }
        if (foi.isSetEncodingType() && foi.getEncodingType() != null && foi.isSetFeature() && foi.getFeature() != null) {
            String encodingType = foi.getEncodingType();
            update.set(qfoi.encodingType, encodingType);
            // TODO: This will probably need a Binding
            // EntityFactories.insertGeometry(update, qfoi.feature, qfoi.geom, encodingType, foi.getFeature());
            message.addField(EntityProperty.ENCODINGTYPE);
            message.addField(EntityProperty.FEATURE);
        } else if (foi.isSetEncodingType() && foi.getEncodingType() != null) {
            String encodingType = foi.getEncodingType();
            update.set(qfoi.encodingType, encodingType);
            message.addField(EntityProperty.ENCODINGTYPE);
        } else if (foi.isSetFeature() && foi.getFeature() != null) {
            String encodingType = qFactory.select(qfoi.encodingType)
                    .from(qfoi)
                    .where(qfoi.getId().eq(foiId))
                    .fetchOne(qfoi.encodingType);
            Object parsedObject = EntityFactories.reParseGeometry(encodingType, foi.getFeature());
            // TODO: This will probably need a Binding
            // EntityFactories.insertGeometry(update, qfoi.feature, qfoi.geom, encodingType, parsedObject);
            message.addField(EntityProperty.FEATURE);
        }
    }

    private void linkExistingObservations(FeatureOfInterest foi, PostgresPersistenceManager<J> pm, DSLContext qFactory, J foiId) throws NoSuchEntityException {
        // Link existing Observations to the FeatureOfInterest.
        for (Observation o : foi.getObservations()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J obsId = (J) o.getId().getValue();
            AbstractTableObservations<J> qo = qCollection.qObservations;
            long oCount = qFactory.update(qo)
                    .set(qo.getFeatureId(), foiId)
                    .where(qo.getId().eq(obsId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned FeatureOfInterest {} to Observation {}.", foiId, obsId);
            }
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createDdslContext()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("FeatureOfInterest " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FEATUREOFINTEREST;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return qInstance.getId();
    }

}
