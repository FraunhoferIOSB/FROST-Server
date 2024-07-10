/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert.Phase.PRE_RELATIONS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpLocations;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpObservations;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpSensors;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpThings;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpThingsLocations;
import de.fraunhofer.iosb.ilt.frostserver.util.GeoHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.io.IOException;
import java.util.List;
import net.time4j.Moment;
import org.geojson.GeoJsonObject;
import org.geolatte.geom.Geometry;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpMultiDatastreams extends StaTableAbstract<TableImpMultiDatastreams> {

    private static final long serialVersionUID = 560943996;
    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpMultiDatastreams.class.getName());
    private static final String DEF_COMPLEX_OBSERVATION = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation";

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
    public final TableField<Record, JsonValue> colObservationTypes = createField(DSL.name("OBSERVATION_TYPES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, Moment> colPhenomenonTimeStart = createField(DSL.name("PHENOMENON_TIME_START"), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, Moment> colPhenomenonTimeEnd = createField(DSL.name("PHENOMENON_TIME_END"), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, Moment> colResultTimeStart = createField(DSL.name("RESULT_TIME_START"), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, Moment> colResultTimeEnd = createField(DSL.name("RESULT_TIME_END"), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.MULTI_DATASTREAMS.UNIT_OF_MEASUREMENTS</code>.
     */
    public final TableField<Record, JsonValue> colUnitOfMeasurements = createField(DSL.name("UNIT_OF_MEASUREMENTS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());
    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVED_AREA</code>.
     */
    public final TableField<Record, Geometry> colObservedArea = createField(DSL.name("OBSERVED_AREA"), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", PostGisGeometryBinding.instance());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> colObservedAreaText = DSL.field("ST_AsGeoJSON(?)", String.class, colObservedArea);

    /**
     * The column <code>public.MULTI_DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name("ID"), getIdType().nullable(false), this);

    /**
     * The column <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, ?> colSensorId;

    /**
     * The column <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, ?> colThingId;

    private final transient PluginMultiDatastream pluginMultiDatastream;
    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param idTypeSnsr The (SQL)DataType of the SENSOR_ID column used in the
     * database.
     * @param idTypeTng The (SQL)DataType of the THING_ID column used in the
     * database.
     * @param pMultiDs the multiDatastream plugin this table belongs to.
     * @param pCoreModel the coreModel plugin that this data model links to.
     */
    public TableImpMultiDatastreams(DataType<?> idType, DataType<?> idTypeSnsr, DataType<?> idTypeTng, PluginMultiDatastream pMultiDs, PluginCoreModel pCoreModel) {
        super(idType, DSL.name("MULTI_DATASTREAMS"), null, null);
        this.pluginMultiDatastream = pMultiDs;
        this.pluginCoreModel = pCoreModel;
        colSensorId = createField(DSL.name("SENSOR_ID"), idTypeSnsr.nullable(false));
        colThingId = createField(DSL.name("THING_ID"), idTypeTng.nullable(false));
    }

    private TableImpMultiDatastreams(Name alias, TableImpMultiDatastreams aliased, PluginMultiDatastream pMultiDs, PluginCoreModel pCoreModel) {
        this(alias, aliased, aliased, pMultiDs, pCoreModel);
    }

    private TableImpMultiDatastreams(Name alias, TableImpMultiDatastreams aliased, Table updatedSql, PluginMultiDatastream pMultiDs, PluginCoreModel pCoreModel) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.pluginMultiDatastream = pMultiDs;
        this.pluginCoreModel = pCoreModel;
        colSensorId = createField(DSL.name("SENSOR_ID"), aliased.colSensorId.getDataType().nullable(false));
        colThingId = createField(DSL.name("THING_ID"), aliased.colThingId.getDataType().nullable(false));
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        final TableImpThings thingsTable = tables.getTableForClass(TableImpThings.class);
        registerRelation(new RelationOneToMany<>(pluginMultiDatastream.npThingMDs, this, thingsTable)
                .setSourceFieldAccessor(TableImpMultiDatastreams::getThingId)
                .setTargetFieldAccessor(TableImpThings::getId));
        final TableImpSensors sensorsTable = tables.getTableForClass(TableImpSensors.class);
        registerRelation(new RelationOneToMany<>(pluginMultiDatastream.npSensorMDs, this, sensorsTable)
                .setSourceFieldAccessor(TableImpMultiDatastreams::getSensorId)
                .setTargetFieldAccessor(TableImpSensors::getId));
        final TableImpMultiDatastreamsObsProperties tableMdOp = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
        final TableImpObsProperties tableObsProp = tables.getTableForClass(TableImpObsProperties.class);
        registerRelation(new RelationManyToManyOrdered<>(pluginMultiDatastream.npObservedPropertiesMDs, this, tableMdOp, tableObsProp, true)
                .setOrderFieldAcc((TableImpMultiDatastreamsObsProperties table) -> table.colRank)
                .setAlwaysDistinct(true)
                .setSourceFieldAcc(TableImpMultiDatastreams::getId)
                .setSourceLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getMultiDatastreamId)
                .setTargetLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getObsPropertyId)
                .setTargetFieldAcc(TableImpObsProperties::getId));

        // we have registered the MULTI_DATA column on the Observations table.
        final TableImpObservations observationsTable = tables.getTableForClass(TableImpObservations.class);
        final int obsMultiDsIdIdx = observationsTable.indexOf("MULTI_DATASTREAM_ID");

        final TableImpObservations tableObs = tables.getTableForClass(TableImpObservations.class);
        registerRelation(new RelationOneToMany<>(pluginMultiDatastream.npObservationsMDs, this, tableObs)
                .setSourceFieldAccessor(TableImpMultiDatastreams::getId)
                .setTargetFieldAccessor(table -> (TableField<Record, ?>) table.field(obsMultiDsIdIdx)));

        // Now we register the inverse relation on Observations
        observationsTable.registerRelation(new RelationOneToMany<>(pluginMultiDatastream.npMultiDatastreamObservation, observationsTable, getThis())
                .setSourceFieldAccessor(table -> (TableField<Record, ?>) table.field(obsMultiDsIdIdx))
                .setTargetFieldAccessor(TableImpMultiDatastreams::getId));
        // Now we register the inverse relation on ObservedProperties
        final TableImpMultiDatastreamsObsProperties tableMDsOpsProp = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
        final TableImpObsProperties tableObsProps = tables.getTableForClass(TableImpObsProperties.class);
        tableObsProps.registerRelation(new RelationManyToManyOrdered<>(pluginMultiDatastream.npMultiDatastreamsObsProp, tableObsProps, tableMDsOpsProp, getThis(), false)
                .setOrderFieldAcc((TableImpMultiDatastreamsObsProperties table) -> table.colRank)
                .setSourceFieldAcc(TableImpObsProperties::getId)
                .setSourceLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getObsPropertyId)
                .setTargetLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getMultiDatastreamId)
                .setTargetFieldAcc(TableImpMultiDatastreams::getId));
        // Now we register the inverse relation on Sensors
        final TableImpSensors tableSensors = tables.getTableForClass(TableImpSensors.class);
        tableSensors.registerRelation(new RelationOneToMany<>(pluginMultiDatastream.npMultiDatastreamsSensor, tableSensors, getThis())
                .setSourceFieldAccessor(TableImpSensors::getId)
                .setTargetFieldAccessor(TableImpMultiDatastreams::getSensorId));
        // Now we register the inverse relation on Things
        final TableImpThings tableThings = tables.getTableForClass(TableImpThings.class);
        tableThings.registerRelation(new RelationOneToMany<>(pluginMultiDatastream.npMultiDatastreamsThing, tableThings, getThis())
                .setSourceFieldAccessor(TableImpThings::getId)
                .setTargetFieldAccessor(TableImpMultiDatastreams::getThingId));
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        final TableCollection tables = getTables();
        pfReg.addEntryId(TableImpMultiDatastreams::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntry(pluginCoreModel.epObservationType, null,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (TableImpMultiDatastreams table, Record tuple, Entity entity, DataSize dataSize) -> entity.setProperty(pluginCoreModel.epObservationType, DEF_COMPLEX_OBSERVATION),
                        null, null));
        pfReg.addEntry(pluginMultiDatastream.epMultiObservationDataTypes, table -> table.colObservationTypes,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (TableImpMultiDatastreams table, Record tuple, Entity entity, DataSize dataSize) -> {
                            final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, table.colObservationTypes);
                            List<String> observationTypes = fieldJsonValue.getValue(Utils.TYPE_LIST_STRING);
                            dataSize.increase(fieldJsonValue.getStringLength());
                            entity.setProperty(pluginMultiDatastream.epMultiObservationDataTypes, observationTypes);
                        },
                        (table, entity, insertFields) -> insertFields.put(table.colObservationTypes, new JsonValue(entity.getProperty(pluginMultiDatastream.epMultiObservationDataTypes))),
                        (table, entity, updateFields, message) -> {
                            updateFields.put(table.colObservationTypes, new JsonValue(entity.getProperty(pluginMultiDatastream.epMultiObservationDataTypes)));
                            message.addField(pluginMultiDatastream.epMultiObservationDataTypes);
                        }));
        pfReg.addEntry(pluginCoreModel.epObservedArea,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (table, tuple, entity, dataSize) -> {
                            String observedArea = tuple.get(table.colObservedAreaText);
                            if (observedArea != null) {
                                try {
                                    GeoJsonObject area = GeoHelper.parseGeoJson(observedArea);
                                    entity.setProperty(pluginCoreModel.epObservedArea, area);
                                } catch (IOException e) {
                                    // It's not a polygon, probably a point or a line.
                                }
                            }
                        }, null, null),
                new NFP<>("s", table -> table.colObservedAreaText));
        pfReg.addEntryNoSelect(pluginCoreModel.epObservedArea, "g", table -> table.colObservedArea);
        pfReg.addEntry(pluginCoreModel.epPhenomenonTimeDs,
                new PropertyFieldRegistry.ConverterTimeInterval<>(pluginCoreModel.epPhenomenonTimeDs, table -> table.colPhenomenonTimeStart, table -> table.colPhenomenonTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.epResultTimeDs,
                new PropertyFieldRegistry.ConverterTimeInterval<>(pluginCoreModel.epResultTimeDs, table -> table.colResultTimeStart, table -> table.colResultTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd));
        pfReg.addEntry(pluginMultiDatastream.getEpUnitOfMeasurements(), table -> table.colUnitOfMeasurements,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (TableImpMultiDatastreams table, Record tuple, Entity entity, DataSize dataSize) -> {
                            final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, table.colUnitOfMeasurements);
                            dataSize.increase(fieldJsonValue.getStringLength());
                            List<UnitOfMeasurement> units = fieldJsonValue.getValue(Utils.TYPE_LIST_UOM);
                            entity.setProperty(pluginMultiDatastream.getEpUnitOfMeasurements(), units);
                        },
                        (table, entity, insertFields) -> insertFields.put(table.colUnitOfMeasurements, new JsonValue(entity.getProperty(pluginMultiDatastream.getEpUnitOfMeasurements()))),
                        (table, entity, updateFields, message) -> {
                            updateFields.put(table.colUnitOfMeasurements, new JsonValue(entity.getProperty(pluginMultiDatastream.getEpUnitOfMeasurements())));
                            message.addField(pluginMultiDatastream.getEpUnitOfMeasurements());
                        }));
        pfReg.addEntry(pluginMultiDatastream.npSensorMDs, TableImpMultiDatastreams::getSensorId);
        pfReg.addEntry(pluginMultiDatastream.npThingMDs, TableImpMultiDatastreams::getThingId);
        pfReg.addEntry(pluginMultiDatastream.npObservedPropertiesMDs, TableImpMultiDatastreams::getId);
        pfReg.addEntry(pluginMultiDatastream.npObservationsMDs, TableImpMultiDatastreams::getId);

        // We register a navigationProperty on the Things, Sensors, ObservedProperties and Observations tables.
        TableImpThings thingsTable = tables.getTableForClass(TableImpThings.class);
        thingsTable.getPropertyFieldRegistry()
                .addEntry(pluginMultiDatastream.npMultiDatastreamsThing, TableImpThings::getId);
        TableImpSensors sensorsTable = tables.getTableForClass(TableImpSensors.class);
        sensorsTable.getPropertyFieldRegistry()
                .addEntry(pluginMultiDatastream.npMultiDatastreamsSensor, TableImpSensors::getId);
        TableImpObsProperties obsPropsTable = tables.getTableForClass(TableImpObsProperties.class);
        obsPropsTable.getPropertyFieldRegistry()
                .addEntry(pluginMultiDatastream.npMultiDatastreamsObsProp, TableImpObsProperties::getId);

        // we need to register the MULTI_DATA column on the Observations table.
        final TableImpObservations observationsTable = tables.getTableForClass(TableImpObservations.class);
        final int obsMultiDsIdIdx = observationsTable.registerField(DSL.name("MULTI_DATASTREAM_ID"), getIdType());
        observationsTable.getPropertyFieldRegistry().addEntry(pluginMultiDatastream.npMultiDatastreamObservation,
                table -> (TableField<Record, ?>) table.field(obsMultiDsIdIdx));

        // Register hooks to alter behaviour of other tables
        obsPropsTable.registerHookPreInsert(-1,
                (phase, pm, entity, insertFields) -> {
                    if (phase != PRE_RELATIONS) {
                        return true;
                    }
                    EntitySet mds = entity.getProperty(pluginMultiDatastream.npMultiDatastreamsObsProp);
                    if (mds != null && !mds.isEmpty()) {
                        throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
                    }
                    return true;
                });
        obsPropsTable.registerHookPreUpdate(-1,
                (pm, entity, entityId) -> {
                    EntitySet mds = entity.getProperty(pluginMultiDatastream.npMultiDatastreamsObsProp);
                    if (mds != null && !mds.isEmpty()) {
                        throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
                    }
                });
        obsPropsTable.registerHookPreDelete(-1, (pm, entityId) -> {
            // Delete all MultiDatastreams that link to this ObservedProperty.
            // Must happen first, since the links in the link table would be gone otherwise.
            TableImpMultiDatastreams tMd = tables.getTableForClass(TableImpMultiDatastreams.class);
            TableImpMultiDatastreamsObsProperties tMdOp = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
            long count = pm.getDslContext()
                    .delete(tMd)
                    .where(
                            ((TableField) tMd.getId()).in(
                                    DSL.select(tMdOp.getMultiDatastreamId())
                                            .from(tMdOp)
                                            .where(((TableField) tMdOp.getObsPropertyId()).eq(entityId))))
                    .execute();
            LOGGER.debug("Deleted {} MultiDatastreams.", count);
        });
        // On insert Observation
        observationsTable.registerHookPreInsert(-1, (phase, pm, entity, insertFields) -> {
            if (phase != PRE_RELATIONS) {
                return true;
            }
            final Entity ds = entity.getProperty(pluginCoreModel.npDatastreamObservation);
            final Entity mds = entity.getProperty(pluginMultiDatastream.npMultiDatastreamObservation);
            if (ds != null && mds != null) {
                throw new IncompleteEntityException("Can not have both Datastream and MultiDatastream.");
            } else if (ds == null && mds != null) {
                Object result = entity.getProperty(pluginCoreModel.epResult);
                if (!(result instanceof List)) {
                    throw new IllegalArgumentException("Multidatastream only accepts array results.");
                }
                List list = (List) result;
                Object mdsId = mds.getId().getValue();
                TableImpMultiDatastreamsObsProperties tableMdsOps = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
                Integer count = pm.getDslContext()
                        .selectCount()
                        .from(tableMdsOps)
                        .where(((TableField) tableMdsOps.getMultiDatastreamId()).eq(mdsId))
                        .fetchOne().component1();
                if (count != list.size()) {
                    throw new IllegalArgumentException("Size of result array (" + list.size() + ") must match number of observed properties (" + count + ") in the MultiDatastream.");
                }
                Entity f = entity.getProperty(pluginCoreModel.npFeatureOfInterestObservation);
                if (f == null) {
                    f = generateFeatureOfInterest(pm, mds.getId());
                    if (f != null) {
                        entity.setProperty(pluginCoreModel.npFeatureOfInterestObservation, f);
                    }
                }
            } else if (ds == null) {
                throw new IncompleteEntityException("Missing Datastream or MultiDatastream.");
            }
            return true;
        });
        // On update, make sure we still have either a DS or MDS, but not both.
        observationsTable.registerHookPreUpdate(-1, (pm, entity, entityId) -> {
            Entity oldObservation = pm.get(pluginCoreModel.etObservation, ParserUtils.idFromObject(entityId));
            boolean newHasDatastream = checkDatastreamSet(oldObservation, entity, pm);
            boolean newHasMultiDatastream = checkMultiDatastreamSet(oldObservation, entity, pm);
            if (newHasDatastream == newHasMultiDatastream) {
                throw new IllegalArgumentException("Observation must have either a Datastream or a MultiDatastream.");
            }
        });
    }

    @Override
    public EntityType getEntityType() {
        return pluginMultiDatastream.etMultiDatastream;
    }

    @Override
    public TableField<Record, ?> getId() {
        return colId;
    }

    public TableField<Record, ?> getSensorId() {
        return colSensorId;
    }

    public TableField<Record, ?> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpMultiDatastreams as(Name alias) {
        return new TableImpMultiDatastreams(alias, this, pluginMultiDatastream, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpMultiDatastreams asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new TableImpMultiDatastreams(DSL.name(name), this, wrappedTable, pluginMultiDatastream, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpMultiDatastreams getThis() {
        return this;
    }

    public Entity generateFeatureOfInterest(JooqPersistenceManager pm, Id datastreamId) throws NoSuchEntityException, IncompleteEntityException {
        final Object dsId = datastreamId.getValue();
        final DSLContext dslContext = pm.getDslContext();
        TableCollection tableCollection = getTables();
        TableImpLocations tl = tableCollection.getTableForClass(TableImpLocations.class);
        TableImpThingsLocations ttl = tableCollection.getTableForClass(TableImpThingsLocations.class);
        TableImpThings tt = tableCollection.getTableForClass(TableImpThings.class);
        TableImpMultiDatastreams tmd = tableCollection.getTableForClass(TableImpMultiDatastreams.class);

        SelectConditionStep<Record3<Object, Object, String>> query = dslContext.select((TableField) tl.getId(), (TableField) tl.getGenFoiId(), tl.colEncodingType)
                .from(tl)
                .innerJoin(ttl).on(((TableField) tl.getId()).eq(ttl.getLocationId()))
                .innerJoin(tt).on(((TableField) tt.getId()).eq(ttl.getThingId()))
                .innerJoin(tmd).on(((TableField) tmd.getThingId()).eq(tt.getId()))
                .where(((TableField) tmd.getId()).eq(dsId));
        TableImpObservations tblObs = tableCollection.getTableForClass(TableImpObservations.class);
        return tblObs.generateFeatureOfInterest(pm, query);
    }

    private boolean checkMultiDatastreamSet(Entity oldObservation, Entity newObservation, JooqPersistenceManager pm) throws IncompleteEntityException {
        if (newObservation.isSetProperty(pluginMultiDatastream.npMultiDatastreamObservation)) {
            final Entity mds = newObservation.getProperty(pluginMultiDatastream.npMultiDatastreamObservation);
            if (mds == null) {
                // MultiDatastream explicitly set to null, to remove old value.
                return false;
            } else {
                final boolean userIsAdmin = PrincipalExtended.getLocalPrincipal().isAdmin();
                if (!pm.getEntityFactories().entityExists(pm, mds, userIsAdmin)) {
                    throw new IncompleteEntityException("MultiDatastream not found.");
                }
                return true;
            }
        }
        Entity mds = oldObservation.getProperty(pluginMultiDatastream.npMultiDatastreamObservation);
        return mds != null;
    }

    private boolean checkDatastreamSet(Entity oldObservation, Entity newObservation, JooqPersistenceManager pm) throws IncompleteEntityException {
        if (newObservation.isSetProperty(pluginCoreModel.npDatastreamObservation)) {
            final Entity ds = newObservation.getProperty(pluginCoreModel.npDatastreamObservation);
            if (ds == null) {
                // MultiDatastream explicitly set to null, to remove old value.
                return false;
            } else {
                final boolean userIsAdmin = PrincipalExtended.getLocalPrincipal().isAdmin();
                if (!pm.getEntityFactories().entityExists(pm, ds, userIsAdmin)) {
                    throw new IncompleteEntityException("Datastream not found.");
                }
                return true;
            }
        }
        Entity ds = oldObservation.getProperty(pluginCoreModel.npDatastreamObservation);
        return ds != null;
    }
}
