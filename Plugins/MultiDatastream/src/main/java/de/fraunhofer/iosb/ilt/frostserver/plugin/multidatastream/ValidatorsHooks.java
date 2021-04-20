/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityValidator;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreDelete;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreUpdate;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel.NAME_EP_OBSERVATIONTYPE;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel.NAME_EP_RESULT;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel.NAME_LINKTABLE_THINGS_LOCATIONS;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel.NAME_NP_DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel.NAME_NP_FEATUREOFINTEREST;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel.NAME_NP_OBSERVEDPROPERTIES;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpLocations;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpLocations.NAME_COL_ENCODING_TYPE;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpLocations.NAME_COL_GEN_FOI_ID;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpObservations;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpThings;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpThingsLocations;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_COL_LT_MULTIDATASTREAMID;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_COL_LT_OBSPROPERTYID;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_COL_MD_THINGID;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_EP_MULTIOBSERVATIONDATATYPES;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_EP_UNITOFMEASUREMENTS;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_ET_MULTIDATASTREAM;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_LINKTABLE_MDS_OBSPROPS;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_NP_MULTIDATASTREAM;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_NP_MULTIDATASTREAMS;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream.PluginMultiDatastream.NAME_TABLE_MD;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ValidatorsHooks {

    private ValidatorsHooks() {
        // Utility Class
    }

    public static class ValidatorMultiDatastream implements EntityValidator {

        private EntityPropertyMain<List<UnitOfMeasurement>> epUnitOfMeasurements;
        private EntityPropertyMain<List<String>> epMultiObservationDataTypes;
        private EntityPropertyMain<String> epObservationType;
        private NavigationPropertyEntitySet npObservedProperties;

        @Override
        public void validate(Entity entity, boolean entityPropertiesOnly) throws IncompleteEntityException {
            if (epUnitOfMeasurements == null) {
                final ModelRegistry modelRegistry = entity.getEntityType().getModelRegistry();
                final EntityType etMultiDatastream = modelRegistry.getEntityTypeForName(NAME_ET_MULTIDATASTREAM);
                epUnitOfMeasurements = etMultiDatastream.getEntityProperty(NAME_EP_UNITOFMEASUREMENTS);
                epMultiObservationDataTypes = etMultiDatastream.getEntityProperty(NAME_EP_MULTIOBSERVATIONDATATYPES);
                epObservationType = etMultiDatastream.getEntityProperty(NAME_EP_OBSERVATIONTYPE);
                npObservedProperties = (NavigationPropertyEntitySet) etMultiDatastream.getNavigationProperty(NAME_NP_OBSERVEDPROPERTIES);
            }
            List<UnitOfMeasurement> unitOfMeasurements = entity.getProperty(epUnitOfMeasurements);
            List<String> multiObservationDataTypes = entity.getProperty(epMultiObservationDataTypes);
            EntitySet observedProperties = entity.getProperty(npObservedProperties);
            final int countUoM = unitOfMeasurements == null ? 0 : unitOfMeasurements.size();
            if (countUoM != multiObservationDataTypes.size()) {
                throw new IllegalArgumentException("Size of list of unitOfMeasurements (" + countUoM + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
            }
            if (!entityPropertiesOnly && observedProperties == null || observedProperties.size() != multiObservationDataTypes.size()) {
                final int opSize = observedProperties == null ? 0 : observedProperties.size();
                throw new IllegalArgumentException("Size of list of observedProperties (" + opSize + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
            }
            String observationType = entity.getProperty(epObservationType);
            if (observationType == null || !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
                throw new IllegalArgumentException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
            }
        }
    }

    public static class ValidatorObservations implements EntityValidator {

        private EntityPropertyMain<Object> epResult;
        private NavigationPropertyEntity npMultiDatastream;
        private NavigationPropertyEntity npDatastream;

        @Override
        public void validate(Entity entity, boolean entityPropertiesOnly) throws IncompleteEntityException {
            if (epResult == null) {
                final EntityType etObservation = entity.getEntityType();
                epResult = etObservation.getEntityProperty(NAME_EP_RESULT);
                npMultiDatastream = (NavigationPropertyEntity) etObservation.getNavigationProperty(NAME_NP_MULTIDATASTREAM);
                npDatastream = (NavigationPropertyEntity) etObservation.getNavigationProperty(NAME_NP_DATASTREAM);
            }
            if (!entityPropertiesOnly) {
                Entity datastream = entity.getProperty(npDatastream);
                Entity multiDatastream = entity.getProperty(npMultiDatastream);
                if (datastream != null && multiDatastream != null) {
                    throw new IllegalArgumentException("Observation can not have both a Datasteam and a MultiDatastream.");
                }
                if (datastream == null && multiDatastream == null) {
                    throw new IncompleteEntityException("Observation must have either a Datasteam or a MultiDatastream.");
                }
                if (multiDatastream != null) {
                    Object result = entity.getProperty(epResult);
                    if (!(result instanceof List)) {
                        throw new IllegalArgumentException("Observation in a MultiDatastream must have an Array result.");
                    }
                }
            }
        }
    }

    public static class HookPreInsertObservedProperty implements HookPreInsert {

        private NavigationPropertyMain.NavigationPropertyEntitySet npMultiDatastreams;

        @Override
        public void insertIntoDatabase(PostgresPersistenceManager pm, Entity entity, Map insertFields) throws NoSuchEntityException, IncompleteEntityException {
            if (npMultiDatastreams == null) {
                npMultiDatastreams = (NavigationPropertyEntitySet) entity.getEntityType().getNavigationProperty(NAME_NP_MULTIDATASTREAMS);
            }
            EntitySet mds = entity.getProperty(npMultiDatastreams);
            if (mds != null && !mds.isEmpty()) {
                throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
            }
        }
    }

    public static class HookPreUpdateObservedProperty implements HookPreUpdate {

        private NavigationPropertyMain.NavigationPropertyEntitySet npMultiDatastreams;

        @Override
        public void updateInDatabase(PostgresPersistenceManager pm, Entity entity, Comparable entityId) throws NoSuchEntityException, IncompleteEntityException {
            if (npMultiDatastreams == null) {
                npMultiDatastreams = (NavigationPropertyEntitySet) entity.getEntityType().getNavigationProperty(NAME_NP_MULTIDATASTREAMS);
            }
            EntitySet mds = entity.getProperty(npMultiDatastreams);
            if (mds != null && !mds.isEmpty()) {
                throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
            }
        }
    }

    public static class HookPreDeleteObservedProperty implements HookPreDelete {

        private static final Logger LOGGER = LoggerFactory.getLogger(HookPreDeleteObservedProperty.class.getName());

        private EntityType etMultiDatastream;
        private int idxColMultiDsId;
        private int idxColObsPropId;

        @Override
        public void delete(PostgresPersistenceManager pm, Comparable entityId) throws NoSuchEntityException {
            // Delete all MultiDatastreams that link to this ObservedProperty.
            // Must happen first, since the links in the link table would be gone otherwise.
            final TableCollection tables = pm.getTableCollection();
            final StaTable tMdOp = tables.getTableForName(NAME_LINKTABLE_MDS_OBSPROPS);
            if (etMultiDatastream == null) {
                etMultiDatastream = pm.getCoreSettings().getModelRegistry().getEntityTypeForName(NAME_ET_MULTIDATASTREAM);
                idxColMultiDsId = tMdOp.indexOf(NAME_COL_LT_MULTIDATASTREAMID);
                idxColObsPropId = tMdOp.indexOf(NAME_COL_LT_OBSPROPERTYID);
            }
            final StaMainTable tMd = tables.getTableForType(etMultiDatastream);
            long count = pm.getDslContext()
                    .delete(tMd)
                    .where(
                            tMd.getId().in(
                                    DSL.select(tMdOp.field(idxColMultiDsId)).from(tMdOp).where(tMdOp.field(idxColObsPropId).eq(entityId))
                            ))
                    .execute();
            LOGGER.debug("Deleted {} MultiDatastreams.", count);
        }
    }

    public static class HookPreInsertObservation implements HookPreInsert {

        private FeatureGenerater foiGen;
        private EntityPropertyMain<Object> epResult;
        private NavigationPropertyMain.NavigationPropertyEntity npDatastream;
        private NavigationPropertyMain.NavigationPropertyEntity npMultiDatastream;
        private NavigationPropertyMain.NavigationPropertyEntity npFeatureOfInterest;
        private int idxColMultiDsId;

        @Override
        public void insertIntoDatabase(PostgresPersistenceManager pm, Entity entity, Map insertFields) throws NoSuchEntityException, IncompleteEntityException {
            if (npDatastream == null) {
                foiGen = new FeatureGenerater();
                final EntityType etObservation = entity.getEntityType();
                epResult = etObservation.getEntityProperty(NAME_EP_RESULT);
                npDatastream = (NavigationPropertyEntity) etObservation.getNavigationProperty(NAME_NP_DATASTREAM);
                npMultiDatastream = (NavigationPropertyEntity) etObservation.getNavigationProperty(NAME_NP_MULTIDATASTREAM);
                npFeatureOfInterest = (NavigationPropertyEntity) etObservation.getNavigationProperty(NAME_NP_FEATUREOFINTEREST);
                final TableCollection tables = pm.getTableCollection();
                final StaTable tMdOp = tables.getTableForName(NAME_LINKTABLE_MDS_OBSPROPS);
                idxColMultiDsId = tMdOp.indexOf(NAME_COL_LT_MULTIDATASTREAMID);
            }
            final Entity ds = entity.getProperty(npDatastream);
            final Entity mds = entity.getProperty(npMultiDatastream);
            if (ds != null && mds != null) {
                throw new IncompleteEntityException("Can not have both Datastream and MultiDatastream.");
            } else if (ds == null && mds != null) {
                final TableCollection tables = pm.getTableCollection();
                final Object result = entity.getProperty(epResult);
                if (!(result instanceof List)) {
                    throw new IllegalArgumentException("Multidatastream only accepts array results.");
                }
                final List list = (List) result;
                final Object mdsId = mds.getId().getValue();
                final StaTable tMdsOp = tables.getTableForName(NAME_LINKTABLE_MDS_OBSPROPS);
                final Integer count = pm.getDslContext()
                        .selectCount()
                        .from(tMdsOp)
                        .where(tMdsOp.field(idxColMultiDsId).eq(mdsId))
                        .fetchOne().component1();
                if (count != list.size()) {
                    throw new IllegalArgumentException("Size of result array (" + list.size() + ") must match number of observed properties (" + count + ") in the MultiDatastream.");
                }
                Entity f = entity.getProperty(npFeatureOfInterest);
                if (f == null) {
                    f = foiGen.generateFeatureOfInterest(pm.getEntityFactories(), pm, mds.getId());
                    if (f != null) {
                        entity.setProperty(npFeatureOfInterest, f);
                    }
                }
            } else if (ds == null) {
                throw new IncompleteEntityException("Missing Datastream or MultiDatastream.");
            }
        }
    }

    public static class HookPreUpdateObservation implements HookPreUpdate {

        private NavigationPropertyEntity npMultiDatastream;
        private NavigationPropertyEntity npDatastream;

        @Override
        public void updateInDatabase(PostgresPersistenceManager pm, Entity entity, Comparable entityId) throws NoSuchEntityException, IncompleteEntityException {
            if (npDatastream == null) {
                npDatastream = (NavigationPropertyEntity) entity.getEntityType().getNavigationProperty(NAME_NP_DATASTREAM);
                npMultiDatastream = (NavigationPropertyEntity) entity.getEntityType().getNavigationProperty(NAME_NP_MULTIDATASTREAM);
            }
            Entity oldObservation = pm.get(entity.getEntityType(), pm.getEntityFactories().idFromObject(entityId));
            boolean newHasDatastream = checkDatastreamSet(oldObservation, entity, pm);
            boolean newHasMultiDatastream = checkMultiDatastreamSet(oldObservation, entity, pm);
            if (newHasDatastream == newHasMultiDatastream) {
                throw new IllegalArgumentException("Observation must have either a Datastream or a MultiDatastream.");
            }
        }

        private boolean checkMultiDatastreamSet(Entity oldObservation, Entity newObservation, PostgresPersistenceManager pm) throws IncompleteEntityException {
            if (newObservation.isSetProperty(npMultiDatastream)) {
                final Entity mds = newObservation.getProperty(npMultiDatastream);
                if (mds == null) {
                    // MultiDatastream explicitly set to null, to remove old value.
                    return false;
                } else {
                    if (!pm.getEntityFactories().entityExists(pm, mds)) {
                        throw new IncompleteEntityException("MultiDatastream not found.");
                    }
                    return true;
                }
            }
            Entity mds = oldObservation.getProperty(npMultiDatastream);
            return mds != null;
        }

        private boolean checkDatastreamSet(Entity oldObservation, Entity newObservation, PostgresPersistenceManager pm) throws IncompleteEntityException {
            if (newObservation.isSetProperty(npDatastream)) {
                final Entity ds = newObservation.getProperty(npDatastream);
                if (ds == null) {
                    // MultiDatastream explicitly set to null, to remove old value.
                    return false;
                } else {
                    if (!pm.getEntityFactories().entityExists(pm, ds)) {
                        throw new IncompleteEntityException("Datastream not found.");
                    }
                    return true;
                }
            }
            Entity ds = oldObservation.getProperty(npDatastream);
            return ds != null;
        }
    }

    private static class FeatureGenerater {

        public Entity generateFeatureOfInterest(final EntityFactories entityFactories, PostgresPersistenceManager pm, Id datastreamId) throws NoSuchEntityException, IncompleteEntityException {
            final Object dsId = datastreamId.getValue();
            final DSLContext dslContext = pm.getDslContext();
            TableCollection tableCollection = pm.getTableCollection();
            StaMainTable tl = (StaMainTable) tableCollection.getTableForName(TableImpLocations.NAME_TABLE);
            StaTable ttl = tableCollection.getTableForName(NAME_LINKTABLE_THINGS_LOCATIONS);
            StaMainTable tt = (StaMainTable) tableCollection.getTableForName(TableImpThings.NAME_TABLE);
            StaMainTable tmd = (StaMainTable) tableCollection.getTableForName(NAME_TABLE_MD);
            TableImpObservations tblObs = (TableImpObservations) tableCollection.getTableForClass(TableImpObservations.class);

            SelectConditionStep<Record3<?, ?, String>> query = dslContext.select(tl.getId(), tl.field(NAME_COL_GEN_FOI_ID), tl.field(NAME_COL_ENCODING_TYPE))
                    .from(tl)
                    .innerJoin(ttl).on(tl.getId().eq(ttl.field(TableImpThingsLocations.NAME_COL_TL_LOCATIONID)))
                    .innerJoin(tt).on(tt.getId().eq(ttl.field(TableImpThingsLocations.NAME_COL_TL_THINGID)))
                    .innerJoin(tmd).on(tmd.field(NAME_COL_MD_THINGID).eq(tt.getId()))
                    .where(tmd.getId().eq(dsId));
            return tblObs.generateFeatureOfInterest(pm, query);
        }
    }

}
