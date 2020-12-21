package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpLocations;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpObservations;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpSensors;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpThings;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpThingsLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.util.GeoHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.geojson.GeoJsonObject;
import org.geolatte.geom.Geometry;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.SelectConditionStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpMultiDatastreams<J extends Comparable> extends StaTableAbstract<J, TableImpMultiDatastreams<J>> {

    private static final long serialVersionUID = 560943996;
    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpMultiDatastreams.class.getName());
    /**
     * The column <code>public.MULTI_DATASTREAMS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVATION_TYPES</code>.
     */
    public final TableField<Record, JsonValue> colObservationTypes = createField(DSL.name("OBSERVATION_TYPES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeStart = createField(DSL.name("PHENOMENON_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeEnd = createField(DSL.name("PHENOMENON_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeStart = createField(DSL.name("RESULT_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTimeEnd = createField(DSL.name("RESULT_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.UNIT_OF_MEASUREMENTS</code>.
     */
    public final TableField<Record, JsonValue> colUnitOfMeasurements = createField(DSL.name("UNIT_OF_MEASUREMENTS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());
    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVED_AREA</code>.
     */
    public final TableField<Record, Geometry> colObservedArea = createField(DSL.name("OBSERVED_AREA"), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", new PostGisGeometryBinding());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> colObservedAreaText = DSL.field("ST_AsGeoJSON(?)", String.class, colObservedArea);

    /**
     * The column <code>public.MULTI_DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, J> colSensorId = createField(DSL.name("SENSOR_ID"), getIdType(), this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, J> colThingId = createField(DSL.name("THING_ID"), getIdType(), this);

    private final PluginMultiDatastream pluginMultiDatastream;
    private final PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableImpMultiDatastreams(DataType<J> idType, PluginMultiDatastream pMultiDs, PluginCoreModel pCoreModel) {
        super(idType, DSL.name("MULTI_DATASTREAMS"), null);
        this.pluginMultiDatastream = pMultiDs;
        this.pluginCoreModel = pCoreModel;
    }

    private TableImpMultiDatastreams(Name alias, TableImpMultiDatastreams<J> aliased, PluginMultiDatastream pMultiDs, PluginCoreModel pCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginMultiDatastream = pMultiDs;
        this.pluginCoreModel = pCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final ModelRegistry modelRegistry = getModelRegistry();
        final TableImpThings<J> thingsTable = tables.getTableForClass(TableImpThings.class);
        registerRelation(new RelationOneToMany<>(this, thingsTable, pluginCoreModel.THING)
                .setSourceFieldAccessor(TableImpMultiDatastreams::getThingId)
                .setTargetFieldAccessor(TableImpThings::getId)
        );
        final TableImpSensors<J> sensorsTable = tables.getTableForClass(TableImpSensors.class);
        registerRelation(new RelationOneToMany<>(this, sensorsTable, pluginCoreModel.SENSOR)
                .setSourceFieldAccessor(TableImpMultiDatastreams::getSensorId)
                .setTargetFieldAccessor(TableImpSensors::getId)
        );
        final TableImpMultiDatastreamsObsProperties<J> tableMdOp = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
        final TableImpObsProperties<J> tableObsProp = tables.getTableForClass(TableImpObsProperties.class);
        registerRelation(new RelationManyToManyOrdered<>(this, tableMdOp, tableObsProp, pluginCoreModel.OBSERVED_PROPERTY)
                .setOrderFieldAcc((TableImpMultiDatastreamsObsProperties<J> table) -> table.colRank)
                .setAlwaysDistinct(true)
                .setSourceFieldAcc(TableImpMultiDatastreams::getId)
                .setSourceLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getMultiDatastreamId)
                .setTargetLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getObsPropertyId)
                .setTargetFieldAcc(TableImpObsProperties::getId)
        );
        final TableImpObservations<J> tableObs = tables.getTableForClass(TableImpObservations.class);
        registerRelation(new RelationOneToMany<>(this, tableObs, pluginCoreModel.OBSERVATION, true)
                .setSourceFieldAccessor(TableImpMultiDatastreams::getId)
                .setTargetFieldAccessor(TableImpObservations::getMultiDatastreamId)
        );

        // Now we register the inverse relation on Observations
        final TableImpObservations<J> observationsTable = tables.getTableForClass(TableImpObservations.class);
        observationsTable.registerRelation(new RelationOneToMany<>(observationsTable, getThis(), pluginMultiDatastream.MULTI_DATASTREAM)
                .setSourceFieldAccessor(TableImpObservations::getMultiDatastreamId)
                .setTargetFieldAccessor(TableImpMultiDatastreams::getId)
        );
        // Now we register the inverse relation on ObservedProperties
        final TableImpMultiDatastreamsObsProperties<J> tableMDsOpsProp = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
        final TableImpObsProperties<J> tableObsProps = tables.getTableForClass(TableImpObsProperties.class);
        tableObsProps.registerRelation(new RelationManyToManyOrdered<>(tableObsProps, tableMDsOpsProp, getThis(), pluginMultiDatastream.MULTI_DATASTREAM)
                .setOrderFieldAcc((TableImpMultiDatastreamsObsProperties<J> table) -> table.colRank)
                .setSourceFieldAcc(TableImpObsProperties::getId)
                .setSourceLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getObsPropertyId)
                .setTargetLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getMultiDatastreamId)
                .setTargetFieldAcc(TableImpMultiDatastreams::getId)
        );
        // Now we register the inverse relation on Sensors
        final TableImpSensors<J> tableSensors = tables.getTableForClass(TableImpSensors.class);
        tableSensors.registerRelation(new RelationOneToMany<>(tableSensors, getThis(), pluginMultiDatastream.MULTI_DATASTREAM, true)
                .setSourceFieldAccessor(TableImpSensors::getId)
                .setTargetFieldAccessor(TableImpMultiDatastreams::getSensorId)
        );
        // Now we register the inverse relation on Things
        final TableImpThings<J> tableThings = tables.getTableForClass(TableImpThings.class);
        tableThings.registerRelation(new RelationOneToMany<>(tableThings, getThis(), pluginMultiDatastream.MULTI_DATASTREAM, true)
                .setSourceFieldAccessor(TableImpThings::getId)
                .setTargetFieldAccessor(TableImpMultiDatastreams::getThingId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final TableCollection<J> tables = getTables();
        final ModelRegistry modelRegistry = getModelRegistry();
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpMultiDatastreams::getId);
        pfReg.addEntryString(pluginCoreModel.EP_NAME, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.EP_DESCRIPTION, table -> table.colDescription);
        pfReg.addEntry(pluginCoreModel.EP_OBSERVATIONTYPE, null,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (TableImpMultiDatastreams<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            entity.setProperty(pluginCoreModel.EP_OBSERVATIONTYPE, "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation");
                        }, null, null));
        pfReg.addEntry(pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES, table -> table.colObservationTypes,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (TableImpMultiDatastreams<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, table.colObservationTypes);
                            List<String> observationTypes = fieldJsonValue.getValue(Utils.TYPE_LIST_STRING);
                            dataSize.increase(fieldJsonValue.getStringLength());
                            entity.setProperty(pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES, observationTypes);
                        },
                        (table, entity, insertFields) -> {
                            insertFields.put(table.colObservationTypes, new JsonValue(entity.getProperty(pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES)));
                        },
                        (table, entity, updateFields, message) -> {
                            updateFields.put(table.colObservationTypes, new JsonValue(entity.getProperty(pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES)));
                            message.addField(pluginMultiDatastream.EP_MULTIOBSERVATIONDATATYPES);
                        }));
        pfReg.addEntry(pluginCoreModel.EP_OBSERVEDAREA,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (table, tuple, entity, dataSize) -> {
                            String observedArea = tuple.get(table.colObservedAreaText);
                            if (observedArea != null) {
                                try {
                                    GeoJsonObject area = GeoHelper.parseGeoJson(observedArea);
                                    entity.setProperty(pluginCoreModel.EP_OBSERVEDAREA, area);
                                } catch (IOException e) {
                                    // It's not a polygon, probably a point or a line.
                                }
                            }
                        }, null, null),
                new NFP<>("s", table -> table.colObservedAreaText));
        pfReg.addEntryNoSelect(pluginCoreModel.EP_OBSERVEDAREA, "g", table -> table.colObservedArea);
        pfReg.addEntry(pluginCoreModel.EP_PHENOMENONTIME_DS,
                new PropertyFieldRegistry.ConverterTimeInterval<>(pluginCoreModel.EP_PHENOMENONTIME_DS, table -> table.colPhenomenonTimeStart, table -> table.colPhenomenonTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.EP_RESULTTIME_DS,
                new PropertyFieldRegistry.ConverterTimeInterval<>(pluginCoreModel.EP_PHENOMENONTIME_DS, table -> table.colResultTimeStart, table -> table.colResultTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd));
        pfReg.addEntry(pluginMultiDatastream.EP_UNITOFMEASUREMENTS, table -> table.colUnitOfMeasurements,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (TableImpMultiDatastreams<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, table.colUnitOfMeasurements);
                            dataSize.increase(fieldJsonValue.getStringLength());
                            List<UnitOfMeasurement> units = fieldJsonValue.getValue(Utils.TYPE_LIST_UOM);
                            entity.setProperty(pluginMultiDatastream.EP_UNITOFMEASUREMENTS, units);
                        },
                        (table, entity, insertFields) -> {
                            insertFields.put(table.colUnitOfMeasurements, new JsonValue(entity.getProperty(pluginMultiDatastream.EP_UNITOFMEASUREMENTS)));
                        },
                        (table, entity, updateFields, message) -> {
                            updateFields.put(table.colUnitOfMeasurements, new JsonValue(entity.getProperty(pluginMultiDatastream.EP_UNITOFMEASUREMENTS)));
                            message.addField(pluginMultiDatastream.EP_UNITOFMEASUREMENTS);
                        }));
        pfReg.addEntry(pluginCoreModel.NP_SENSOR, TableImpMultiDatastreams::getSensorId, idManager);
        pfReg.addEntry(pluginCoreModel.NP_THING, TableImpMultiDatastreams::getThingId, idManager);
        pfReg.addEntry(pluginCoreModel.NP_OBSERVEDPROPERTIES, TableImpMultiDatastreams::getId, idManager);
        pfReg.addEntry(pluginCoreModel.NP_OBSERVATIONS, TableImpMultiDatastreams::getId, idManager);

        // We register a navigationProperty on the Things, Sensors, ObservedProperties and Observations tables.
        TableImpThings<J> thingsTable = tables.getTableForClass(TableImpThings.class);
        thingsTable.getPropertyFieldRegistry()
                .addEntry(pluginMultiDatastream.NP_MULTIDATASTREAMS, TableImpThings::getId, idManager);
        TableImpSensors<J> sensorsTable = tables.getTableForClass(TableImpSensors.class);
        sensorsTable.getPropertyFieldRegistry()
                .addEntry(pluginMultiDatastream.NP_MULTIDATASTREAMS, TableImpSensors::getId, idManager);
        TableImpObsProperties<J> obsPropsTable = tables.getTableForClass(TableImpObsProperties.class);
        obsPropsTable.getPropertyFieldRegistry()
                .addEntry(pluginMultiDatastream.NP_MULTIDATASTREAMS, TableImpObsProperties::getId, idManager);
        TableImpObservations<J> observationsTable = tables.getTableForClass(TableImpObservations.class);
        observationsTable.getPropertyFieldRegistry()
                .addEntry(pluginMultiDatastream.NP_MULTIDATASTREAM, TableImpObservations::getMultiDatastreamId, idManager);

        // Register hooks to alter behaviour of other tables
        obsPropsTable.registerHookPreInsert(-1, (pm, entity, insertFields) -> {
            EntitySet mds = entity.getProperty(pluginMultiDatastream.NP_MULTIDATASTREAMS);
            if (mds != null && !mds.isEmpty()) {
                throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
            }
        });
        obsPropsTable.registerHookPreUpdate(-1, (pm, entity, entityId) -> {
            EntitySet mds = entity.getProperty(pluginMultiDatastream.NP_MULTIDATASTREAMS);
            if (mds != null && !mds.isEmpty()) {
                throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
            }
        });
        obsPropsTable.registerHookPreDelete(-1, (pm, entityId) -> {
            // Delete all MultiDatastreams that link to this ObservedProperty.
            // Must happen first, since the links in the link table would be gone otherwise.
            TableImpMultiDatastreams<J> tMd = tables.getTableForClass(TableImpMultiDatastreams.class);
            TableImpMultiDatastreamsObsProperties<J> tMdOp = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
            long count = pm.getDslContext()
                    .delete(tMd)
                    .where(
                            tMd.getId().in(
                                    DSL.select(tMdOp.getMultiDatastreamId()).from(tMdOp).where(tMdOp.getObsPropertyId().eq(entityId))
                            ))
                    .execute();
            LOGGER.debug("Deleted {} MultiDatastreams.", count);
        });
        // On insert Observation
        observationsTable.registerHookPreInsert(-1, (pm, entity, insertFields) -> {
            final Entity ds = entity.getProperty(pluginCoreModel.NP_DATASTREAM);
            final Entity mds = entity.getProperty(pluginMultiDatastream.NP_MULTIDATASTREAM);
            if (ds != null && mds != null) {
                throw new IncompleteEntityException("Can not have both Datastream and MultiDatastream.");
            } else if (ds == null && mds != null) {
                Object result = entity.getProperty(pluginCoreModel.EP_RESULT);
                if (!(result instanceof List)) {
                    throw new IllegalArgumentException("Multidatastream only accepts array results.");
                }
                List list = (List) result;
                J mdsId = (J) mds.getId().getValue();
                TableImpMultiDatastreamsObsProperties<J> tableMdsOps = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
                Integer count = pm.getDslContext()
                        .selectCount()
                        .from(tableMdsOps)
                        .where(tableMdsOps.getMultiDatastreamId().eq(mdsId))
                        .fetchOne().component1();
                if (count != list.size()) {
                    throw new IllegalArgumentException("Size of result array (" + list.size() + ") must match number of observed properties (" + count + ") in the MultiDatastream.");
                }
                Entity f = entity.getProperty(pluginCoreModel.NP_FEATUREOFINTEREST);
                if (f == null) {
                    f = generateFeatureOfInterest(entityFactories, pm, mds.getId());
                    if (f != null) {
                        entity.setProperty(pluginCoreModel.NP_FEATUREOFINTEREST, f);
                    }
                }
            } else if (ds == null) {
                throw new IncompleteEntityException("Missing Datastream or MultiDatastream.");
            }
        });
        // On update, make sure we still have either a DS or MDS, but not both.
        observationsTable.registerHookPreUpdate(-1, (pm, entity, entityId) -> {
            Entity oldObservation = pm.get(pluginCoreModel.OBSERVATION, entityFactories.idFromObject(entityId));
            boolean newHasDatastream = checkDatastreamSet(oldObservation, entity, pm);
            boolean newHasMultiDatastream = checkMultiDatastreamSet(oldObservation, entity, pm);
            if (newHasDatastream == newHasMultiDatastream) {
                throw new IllegalArgumentException("Observation must have either a Datastream or a MultiDatastream.");
            }
        });
    }

    @Override
    public EntityType getEntityType() {
        return pluginMultiDatastream.MULTI_DATASTREAM;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    public TableField<Record, J> getSensorId() {
        return colSensorId;
    }

    public TableField<Record, J> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpMultiDatastreams<J> as(Name alias) {
        return new TableImpMultiDatastreams<>(alias, this, pluginMultiDatastream, pluginCoreModel);
    }

    @Override
    public TableImpMultiDatastreams<J> as(String alias) {
        return new TableImpMultiDatastreams<>(DSL.name(alias), this, pluginMultiDatastream, pluginCoreModel);
    }

    @Override
    public TableImpMultiDatastreams<J> getThis() {
        return this;
    }

    public Entity generateFeatureOfInterest(final EntityFactories<J> entityFactories, PostgresPersistenceManager<J> pm, Id datastreamId) throws NoSuchEntityException, IncompleteEntityException {
        final J dsId = (J) datastreamId.getValue();
        final DSLContext dslContext = pm.getDslContext();
        TableCollection<J> tableCollection = getTables();
        TableImpLocations<J> tl = tableCollection.getTableForClass(TableImpLocations.class);
        TableImpThingsLocations<J> ttl = tableCollection.getTableForClass(TableImpThingsLocations.class);
        TableImpThings<J> tt = tableCollection.getTableForClass(TableImpThings.class);
        TableImpMultiDatastreams<J> tmd = tableCollection.getTableForClass(TableImpMultiDatastreams.class);

        SelectConditionStep<Record3<J, J, String>> query = dslContext.select(tl.getId(), tl.getGenFoiId(), tl.colEncodingType)
                .from(tl)
                .innerJoin(ttl).on(tl.getId().eq(ttl.getLocationId()))
                .innerJoin(tt).on(tt.getId().eq(ttl.getThingId()))
                .innerJoin(tmd).on(tmd.getThingId().eq(tt.getId()))
                .where(tmd.getId().eq(dsId));
        TableImpObservations<J> tblObs = tableCollection.getTableForClass(TableImpObservations.class);
        return tblObs.generateFeatureOfInterest(pm, query);
    }

    private boolean checkMultiDatastreamSet(Entity oldObservation, Entity newObservation, PostgresPersistenceManager<J> pm) throws IncompleteEntityException {
        if (newObservation.isSetProperty(pluginMultiDatastream.NP_MULTIDATASTREAM)) {
            final Entity mds = newObservation.getProperty(pluginMultiDatastream.NP_MULTIDATASTREAM);
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
        Entity mds = oldObservation.getProperty(pluginMultiDatastream.NP_MULTIDATASTREAM);
        return mds != null;
    }

    private boolean checkDatastreamSet(Entity oldObservation, Entity newObservation, PostgresPersistenceManager<J> pm) throws IncompleteEntityException {
        final ModelRegistry modelRegistry = getModelRegistry();
        if (newObservation.isSetProperty(pluginCoreModel.NP_DATASTREAM)) {
            final Entity ds = newObservation.getProperty(pluginCoreModel.NP_DATASTREAM);
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
        Entity ds = oldObservation.getProperty(pluginCoreModel.NP_DATASTREAM);
        return ds != null;
    }
}
