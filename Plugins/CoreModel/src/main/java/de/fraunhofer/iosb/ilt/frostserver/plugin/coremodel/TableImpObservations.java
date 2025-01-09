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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert.Phase.PRE_RELATIONS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ResultType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.time4j.Moment;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpObservations extends StaTableAbstract<TableImpObservations> {

    public static final String NAME_TABLE = "OBSERVATIONS";
    public static final String NAME_COL_DATASTREAMID = "DATASTREAM_ID";
    public static final String NAME_COL_FEATUREID = "FEATURE_ID";
    public static final String NAME_COL_ID = "ID";
    public static final String NAME_COL_PARAMETERS = "PARAMETERS";
    public static final String NAME_COL_PHENOMENONTIMESTART = "PHENOMENON_TIME_START";
    public static final String NAME_COL_PHENOMENONTIMEEND = "PHENOMENON_TIME_END";
    public static final String NAME_COL_RESULTTIME = "RESULT_TIME";
    public static final String NAME_COL_RESULTTYPE = "RESULT_TYPE";
    public static final String NAME_COL_RESULTJSON = "RESULT_JSON";
    public static final String NAME_COL_RESULTBOOLEAN = "RESULT_BOOLEAN";
    public static final String NAME_COL_RESULTNUMBER = "RESULT_NUMBER";
    public static final String NAME_COL_RESULTSTRING = "RESULT_STRING";
    public static final String NAME_COL_RESULTQUALITY = "RESULT_QUALITY";
    public static final String NAME_COL_VALIDTIMESTART = "VALID_TIME_START";
    public static final String NAME_COL_VALIDTIMEEND = "VALID_TIME_END";

    private static final long serialVersionUID = -1104422281;
    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpObservations.class.getName());

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, Moment> colPhenomenonTimeStart = createField(DSL.name(NAME_COL_PHENOMENONTIMESTART), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, Moment> colPhenomenonTimeEnd = createField(DSL.name(NAME_COL_PHENOMENONTIMEEND), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.OBSERVATIONS.RESULT_TIME</code>.
     */
    public final TableField<Record, Moment> colResultTime = createField(DSL.name(NAME_COL_RESULTTIME), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.OBSERVATIONS.RESULT_NUMBER</code>.
     */
    public final TableField<Record, Double> colResultNumber = createField(DSL.name(NAME_COL_RESULTNUMBER), SQLDataType.DOUBLE, this);

    /**
     * The column <code>public.OBSERVATIONS.RESULT_STRING</code>.
     */
    public final TableField<Record, String> colResultString = createField(DSL.name(NAME_COL_RESULTSTRING), SQLDataType.CLOB, this);

    /**
     * The column <code>public.OBSERVATIONS.RESULT_QUALITY</code>.
     */
    public final TableField<Record, JsonValue> colResultQuality = createField(DSL.name(NAME_COL_RESULTQUALITY), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());
    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_START</code>.
     */
    public final TableField<Record, Moment> colValidTimeStart = createField(DSL.name(NAME_COL_VALIDTIMESTART), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_END</code>.
     */
    public final TableField<Record, Moment> colValidTimeEnd = createField(DSL.name(NAME_COL_VALIDTIMEEND), SQLDataType.TIMESTAMP, this, "", MomentBinding.instance());

    /**
     * The column <code>public.OBSERVATIONS.PARAMETERS</code>.
     */
    public final TableField<Record, JsonValue> colParameters = createField(DSL.name(NAME_COL_PARAMETERS), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.OBSERVATIONS.RESULT_TYPE</code>.
     */
    public final TableField<Record, Short> colResultType = createField(DSL.name(NAME_COL_RESULTTYPE), SQLDataType.SMALLINT, this);

    /**
     * The column <code>public.OBSERVATIONS.RESULT_JSON</code>.
     */
    public final TableField<Record, JsonValue> colResultJson = createField(DSL.name(NAME_COL_RESULTJSON), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());
    /**
     * The column <code>public.OBSERVATIONS.RESULT_BOOLEAN</code>.
     */
    public final TableField<Record, Boolean> colResultBoolean = createField(DSL.name(NAME_COL_RESULTBOOLEAN), SQLDataType.BOOLEAN, this);

    /**
     * The column <code>public.OBSERVATIONS.ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name(NAME_COL_ID), getIdType(), this);

    /**
     * The column <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final TableField<Record, ?> colDatastreamId;

    /**
     * The column <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final TableField<Record, ?> colFeatureId;

    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.OBSERVATIONS</code> table reference.
     *
     * @param idType The (SQL)DataType of the ID column used in the database.
     * @param idTypeDs The (SQL)DataType of the DATASTREAM_ID column used in the
     * database.
     * @param idTypeFeature The (SQL)DataType of the FEATURE_ID column used in
     * the database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpObservations(DataType<?> idType, DataType<?> idTypeDs, DataType<?> idTypeFeature, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null, null);
        this.pluginCoreModel = pluginCoreModel;
        colDatastreamId = createField(DSL.name(NAME_COL_DATASTREAMID), idTypeDs);
        colFeatureId = createField(DSL.name(NAME_COL_FEATUREID), idTypeFeature);
    }

    private TableImpObservations(Name alias, TableImpObservations aliased, PluginCoreModel pluginCoreModel) {
        this(alias, aliased, aliased, pluginCoreModel);
    }

    private TableImpObservations(Name alias, TableImpObservations aliased, Table updatedSql, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.pluginCoreModel = pluginCoreModel;
        colDatastreamId = createField(DSL.name(NAME_COL_DATASTREAMID), aliased.colDatastreamId.getDataType());
        colFeatureId = createField(DSL.name(NAME_COL_FEATUREID), aliased.colFeatureId.getDataType());
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        final TableImpDatastreams datastreamsTable = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npDatastreamObservation, getThis(), datastreamsTable)
                .setSourceFieldAccessor(TableImpObservations::getDatastreamId)
                .setTargetFieldAccessor(TableImpDatastreams::getId));
        final TableImpFeatures featuresTable = tables.getTableForClass(TableImpFeatures.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npFeatureOfInterestObservation, getThis(), featuresTable)
                .setSourceFieldAccessor(TableImpObservations::getFeatureId)
                .setTargetFieldAccessor(TableImpFeatures::getId));
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpObservations::getId);
        pfReg.addEntryMap(pluginCoreModel.epParameters, table -> table.colParameters);
        pfReg.addEntry(pluginCoreModel.epPhenomenonTime,
                new ConverterTimeValue<>(pluginCoreModel.epPhenomenonTime, table -> table.colPhenomenonTimeStart, table -> table.colPhenomenonTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntry(pluginCoreModel.epResult,
                true,
                new ConverterRecordDeflt<>(
                        this::readResultFromDb,
                        this::handleResult,
                        (table, entity, updateFields, message) -> {
                            handleResult(table, entity, updateFields);
                            message.addField(pluginCoreModel.epResult);
                        }),
                new NFP<>("n", table -> table.colResultNumber),
                new NFP<>("b", table -> table.colResultBoolean),
                new NFP<>("s", table -> table.colResultString),
                new NFP<>("j", table -> table.colResultJson),
                new NFP<>("t", table -> table.colResultType));
        pfReg.addEntry(pluginCoreModel.epResultQuality, table -> table.colResultQuality,
                new ConverterRecordDeflt<>(
                        (TableImpObservations table, Record tuple, Entity entity, DataSize dataSize) -> {
                            JsonValue resultQuality = Utils.getFieldJsonValue(tuple, table.colResultQuality);
                            dataSize.increase(resultQuality.getStringLength());
                            entity.setProperty(pluginCoreModel.epResultQuality, resultQuality.getValue());
                        },
                        (table, entity, insertFields) -> insertFields.put(table.colResultQuality, EntityFactories.objectToJson(entity.getProperty(pluginCoreModel.epResultQuality))),
                        (table, entity, updateFields, message) -> {
                            updateFields.put(table.colResultQuality, EntityFactories.objectToJson(entity.getProperty(pluginCoreModel.epResultQuality)));
                            message.addField(pluginCoreModel.epResultQuality);
                        }));
        pfReg.addEntry(pluginCoreModel.epResultTime, table -> table.colResultTime,
                new ConverterTimeInstant<>(pluginCoreModel.epResultTime, table -> table.colResultTime));
        pfReg.addEntry(pluginCoreModel.epValidTime,
                new ConverterTimeInterval<>(pluginCoreModel.epValidTime, table -> table.colValidTimeStart, table -> table.colValidTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colValidTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colValidTimeEnd));
        pfReg.addEntry(pluginCoreModel.npFeatureOfInterestObservation, TableImpObservations::getFeatureId);
        pfReg.addEntry(pluginCoreModel.npDatastreamObservation, TableImpObservations::getDatastreamId);

        registerHookPreInsert(0, (phase, pm, entity, insertFields) -> {
            if (phase != PRE_RELATIONS) {
                return true;
            }
            Entity f = entity.getProperty(pluginCoreModel.npFeatureOfInterestObservation);
            if (f == null) {
                final Entity ds = entity.getProperty(pluginCoreModel.npDatastreamObservation);
                if (ds != null) {
                    f = generateFeatureOfInterest(pm, ds.getPrimaryKeyValues().get(0));
                    if (f == null) {
                        throw new IncompleteEntityException("No FeatureOfInterest provided, and none can be generated.");
                    }
                    entity.setProperty(pluginCoreModel.npFeatureOfInterestObservation, f);
                }
            }
            return true;
        });
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etObservation;
    }

    @Override
    public List<Field> getPkFields() {
        return Arrays.asList(colId);
    }

    public TableField<Record, ?> getId() {
        return colId;
    }

    public TableField<Record, ?> getDatastreamId() {
        return colDatastreamId;
    }

    public TableField<Record, ?> getFeatureId() {
        return colFeatureId;
    }

    @Override
    public TableImpObservations as(Name alias) {
        return new TableImpObservations(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpObservations asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new TableImpObservations(DSL.name(name), this, wrappedTable, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpObservations getThis() {
        return this;
    }

    public void handleResult(TableImpObservations table, Entity entity, Map<Field, Object> output) {
        Object result = entity.getProperty(pluginCoreModel.epResult);
        if (result instanceof Number number) {
            output.put(table.colResultType, ResultType.NUMBER.sqlValue());
            output.put(table.colResultString, result.toString());
            output.put(table.colResultNumber, number.doubleValue());
            output.put(table.colResultBoolean, null);
            output.put(table.colResultJson, null);
        } else if (result instanceof Boolean) {
            output.put(table.colResultType, ResultType.BOOLEAN.sqlValue());
            output.put(table.colResultString, result.toString());
            output.put(table.colResultBoolean, result);
            output.put(table.colResultNumber, null);
            output.put(table.colResultJson, null);
        } else if (result instanceof String) {
            output.put(table.colResultType, ResultType.STRING.sqlValue());
            output.put(table.colResultString, result.toString());
            output.put(table.colResultNumber, null);
            output.put(table.colResultBoolean, null);
            output.put(table.colResultJson, null);
        } else {
            output.put(table.colResultType, ResultType.OBJECT_ARRAY.sqlValue());
            output.put(table.colResultJson, EntityFactories.objectToJson(result));
            output.put(table.colResultString, null);
            output.put(table.colResultNumber, null);
            output.put(table.colResultBoolean, null);
        }
    }

    public void readResultFromDb(TableImpObservations table, Record tuple, Entity entity, DataSize dataSize) {
        Short resultTypeOrd = Utils.getFieldOrNull(tuple, table.colResultType);
        if (resultTypeOrd != null) {
            ResultType resultType = ResultType.fromSqlValue(resultTypeOrd);
            switch (resultType) {
                case BOOLEAN:
                    entity.setProperty(pluginCoreModel.epResult, Utils.getFieldOrNull(tuple, table.colResultBoolean));
                    break;

                case NUMBER:
                    handleNumber(table, tuple, entity);
                    break;

                case OBJECT_ARRAY:
                    JsonValue jsonData = Utils.getFieldJsonValue(tuple, table.colResultJson);
                    dataSize.increase(jsonData.getStringLength());
                    entity.setProperty(pluginCoreModel.epResult, jsonData.getValue());
                    break;

                case STRING:
                    String stringData = Utils.getFieldOrNull(tuple, table.colResultString);
                    dataSize.increase(stringData == null ? 0 : stringData.length());
                    entity.setProperty(pluginCoreModel.epResult, stringData);
                    break;

                default:
                    throw new IllegalStateException("Unhandled resultType: " + resultType);
            }
        }
    }

    private void handleNumber(TableImpObservations table, Record tuple, Entity entity) {
        try {
            entity.setProperty(pluginCoreModel.epResult, new BigDecimal(Utils.getFieldOrNull(tuple, table.colResultString)));
        } catch (NumberFormatException | NullPointerException e) {
            // It was not a Number? Use the double value.
            entity.setProperty(pluginCoreModel.epResult, Utils.getFieldOrNull(tuple, table.colResultNumber));
        }
    }

    public Entity generateFeatureOfInterest(JooqPersistenceManager pm, Object dsId) throws NoSuchEntityException, IncompleteEntityException {
        final DSLContext dslContext = pm.getDslContext();
        TableCollection tableCollection = getTables();
        TableImpLocations ql = tableCollection.getTableForClass(TableImpLocations.class);
        TableImpThingsLocations qtl = tableCollection.getTableForClass(TableImpThingsLocations.class);
        TableImpThings qt = tableCollection.getTableForClass(TableImpThings.class);
        TableImpDatastreams qd = tableCollection.getTableForClass(TableImpDatastreams.class);

        SelectConditionStep<Record3<Object, Object, String>> query = dslContext.select((TableField) ql.getId(), (TableField) ql.getGenFoiId(), ql.colEncodingType)
                .from(ql)
                .innerJoin(qtl).on(((TableField) ql.getId()).eq(qtl.getLocationId()))
                .innerJoin(qt).on(((TableField) qt.getId()).eq(qtl.getThingId()))
                .innerJoin(qd).on(((TableField) qd.getThingId()).eq(qt.getId()))
                .where(((TableField) qd.getId()).eq(dsId));
        return generateFeatureOfInterest(pm, query);
    }

    public Entity generateFeatureOfInterest(JooqPersistenceManager pm, ResultQuery<Record3<Object, Object, String>> locationQuery) throws NoSuchEntityException, IncompleteEntityException {
        TableImpLocations ql = getTables().getTableForClass(TableImpLocations.class);
        Result<Record3<Object, Object, String>> tuples = locationQuery.fetch();
        if (tuples.isEmpty()) {
            // No locations found.
            return null;
        }
        // See if any of the locations have a generated foi.
        // Also track if any of the location has a supported encoding type.
        Object genFoiId = null;
        Object locationId = null;
        for (Record tuple : tuples) {
            genFoiId = getFieldOrNull(tuple, ql.getGenFoiId());
            if (genFoiId != null) {
                break;
            }
            String encodingType = getFieldOrNull(tuple, ql.colEncodingType);
            if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
                locationId = getFieldOrNull(tuple, ql.getId());
            }
        }

        // Either genFoiId will have a value, if a generated foi was found,
        // Or locationId will have a value if a supported encoding type was found.
        if (genFoiId != null) {
            return new DefaultEntity(pluginCoreModel.etFeatureOfInterest, PkValue.of(genFoiId));
        } else if (locationId != null) {
            return generateFeature(pm, ql, locationId);
        } else {
            // Can not generate foi from Thing with no locations.
            return null;
        }
    }

    public Entity generateFeature(JooqPersistenceManager pm, TableImpLocations ql, Object locationId) throws IncompleteEntityException, DataAccessException, NoSuchEntityException {
        final DSLContext dslContext = pm.getDslContext();
        Field<?> fRestricted = ql.field("RESTRICTED");
        Property epRestricted = pluginCoreModel.etFeatureOfInterest.getProperty("restricted");

        SelectConditionStep<Record> query2;
        if (fRestricted == null || epRestricted == null) {
            query2 = dslContext.select((TableField) ql.getId(), ql.colEncodingType, ql.colLocation, ql.colName, ql.colDescription, ql.colProperties)
                    .from(ql)
                    .where(((TableField) ql.getId()).eq(locationId));
        } else {
            query2 = dslContext.select((TableField) ql.getId(), ql.colEncodingType, ql.colLocation, ql.colName, ql.colDescription, ql.colProperties, fRestricted)
                    .from(ql)
                    .where(((TableField) ql.getId()).eq(locationId));
        }
        Record tuple = query2.fetchOne();
        if (tuple == null) {
            // Can not generate foi from Thing with no locations.
            // Should not happen, since the query succeeded just before.
            return null;
        }
        String name = getFieldOrNull(tuple, ql.colName);
        String description = getFieldOrNull(tuple, ql.colDescription);
        JsonValue properties = getFieldOrNull(tuple, ql.colProperties);
        String encoding = getFieldOrNull(tuple, ql.colEncodingType);
        String locString = getFieldOrNull(tuple, ql.colLocation);
        Object locObject = Utils.jsonToTreeOrString(locString);
        Entity foi = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                .setProperty(pluginCoreModel.epName, name)
                .setProperty(pluginCoreModel.epDescription, description)
                .setProperty(ModelRegistry.EP_ENCODINGTYPE, encoding)
                .setProperty(pluginCoreModel.epFeature, locObject)
                .setProperty(ModelRegistry.EP_PROPERTIES, properties.getMapValue());
        if (fRestricted != null && epRestricted != null) {
            foi.setProperty(epRestricted, getFieldOrNull(tuple, fRestricted));
        }
        ModelRegistry mr = pm.getCoreSettings().getModelRegistry();
        EntityType etProject = mr.getEntityTypeForName("Project", true);
        if (etProject != null) {
            linkProjects(dslContext, locationId, etProject, foi);
        }

        // Switch to ADMIN user
        PrincipalExtended userPrincipal = PrincipalExtended.getLocalPrincipal();
        PrincipalExtended.setLocalPrincipal(PrincipalExtended.INTERNAL_ADMIN_PRINCIPAL);
        pm.insert(foi, UpdateMode.INSERT_STA_11);
        // Switch back to normal user
        PrincipalExtended.setLocalPrincipal(userPrincipal);

        Object foiId = foi.getPrimaryKeyValues().get(0);
        dslContext.update(ql)
                .set(((TableField) ql.getGenFoiId()), foiId)
                .where(((TableField) ql.getId()).eq(locationId))
                .execute();
        LOGGER.debug("Generated foi {} from Location {}.", foiId, locationId);
        return foi;
    }

    public void linkProjects(final DSLContext dslContext, Object locationId, EntityType etProject, Entity foi) {
        try {
            StaTable tlp = getTables().getTableForName("LOCATION_PROJECTS");
            NavigationPropertyMain.NavigationPropertyEntitySet npFeatureProjects = pluginCoreModel.etFeatureOfInterest.getNavigationPropertyEntitySet("Projects");
            if (tlp == null || npFeatureProjects == null) {
                LOGGER.error("Failed to link Projects to generated FoI, linktable LOCATION_PROJECTS not found.");
                return;
            }
            EntitySetImpl projects = new EntitySetImpl(npFeatureProjects);
            TableField fPid = (TableField) tlp.field("PROJECT_ID");
            TableField fLid = (TableField) tlp.field("LOCATION_ID");
            if (fPid == null || fLid == null) {
                LOGGER.error("Failed to link Projects to generated FoI, linktable LOCATION_PROJECTS does not have columns PROJECT_ID and LOCATION_ID.");
                return;
            }
            Result<Record1<Object>> projectIds = dslContext.select(fPid)
                    .from(tlp)
                    .where(fLid.eq(locationId))
                    .fetch();
            for (Record pidTuple : projectIds) {
                Object projectId = getFieldOrNull(pidTuple, fPid);
                projects.add(new DefaultEntity(etProject).setPrimaryKeyValues(PkValue.of(projectId)));
            }
            if (!projects.isEmpty()) {
                LOGGER.debug("Assigning {} projects to generated foi", projects.size());
                foi.setProperty(npFeatureProjects, projects);
            }

        } catch (RuntimeException ex) {
            LOGGER.error("Failed to link Projects to generated FoI.", ex);
        }
    }

}
