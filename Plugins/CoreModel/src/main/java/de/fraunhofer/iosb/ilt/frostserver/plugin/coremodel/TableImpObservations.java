package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ResultType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpObservations<J extends Comparable> extends StaTableAbstract<J, TableImpObservations<J>> {

    private static final long serialVersionUID = -1104422281;
    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpObservations.class.getName());

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeStart = createField(DSL.name("PHENOMENON_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeEnd = createField(DSL.name("PHENOMENON_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.OBSERVATIONS.RESULT_TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTime = createField(DSL.name("RESULT_TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.OBSERVATIONS.RESULT_NUMBER</code>.
     */
    public final TableField<Record, Double> colResultNumber = createField(DSL.name("RESULT_NUMBER"), SQLDataType.DOUBLE, this);

    /**
     * The column <code>public.OBSERVATIONS.RESULT_STRING</code>.
     */
    public final TableField<Record, String> colResultString = createField(DSL.name("RESULT_STRING"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.OBSERVATIONS.RESULT_QUALITY</code>.
     */
    public final TableField<Record, JsonValue> colResultQuality = createField(DSL.name("RESULT_QUALITY"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());
    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colValidTimeStart = createField(DSL.name("VALID_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colValidTimeEnd = createField(DSL.name("VALID_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this);

    /**
     * The column <code>public.OBSERVATIONS.PARAMETERS</code>.
     */
    public final TableField<Record, JsonValue> colParameters = createField(DSL.name("PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());
    /**
     * The column <code>public.OBSERVATIONS.RESULT_TYPE</code>.
     */
    public final TableField<Record, Short> colResultType = createField(DSL.name("RESULT_TYPE"), SQLDataType.SMALLINT, this);

    /**
     * The column <code>public.OBSERVATIONS.RESULT_JSON</code>.
     */
    public final TableField<Record, JsonValue> colResultJson = createField(DSL.name("RESULT_JSON"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());
    /**
     * The column <code>public.OBSERVATIONS.RESULT_BOOLEAN</code>.
     */
    public final TableField<Record, Boolean> colResultBoolean = createField(DSL.name("RESULT_BOOLEAN"), SQLDataType.BOOLEAN, this);

    /**
     * The column <code>public.OBSERVATIONS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final TableField<Record, J> colDatastreamId = createField(DSL.name("DATASTREAM_ID"), getIdType(), this);

    /**
     * The column <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final TableField<Record, J> colFeatureId = createField(DSL.name("FEATURE_ID"), getIdType(), this);

    private final PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.OBSERVATIONS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpObservations(DataType<J> idType, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name("OBSERVATIONS"), null);
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpObservations(Name alias, TableImpObservations<J> aliased, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final TableImpDatastreams<J> datastreamsTable = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(getThis(), datastreamsTable, pluginCoreModel.etDatastream)
                .setSourceFieldAccessor(TableImpObservations::getDatastreamId)
                .setTargetFieldAccessor(TableImpDatastreams::getId)
        );
        final TableImpFeatures<J> featuresTable = tables.getTableForClass(TableImpFeatures.class);
        registerRelation(new RelationOneToMany<>(getThis(), featuresTable, pluginCoreModel.etFeatureOfInterest)
                .setSourceFieldAccessor(TableImpObservations::getFeatureId)
                .setTargetFieldAccessor(TableImpFeatures::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final ModelRegistry modelRegistry = getModelRegistry();
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpObservations::getId);
        pfReg.addEntryMap(pluginCoreModel.epParameters, table -> table.colParameters);
        pfReg.addEntry(pluginCoreModel.epPhenomenonTime,
                new ConverterTimeValue<>(pluginCoreModel.epPhenomenonTime, table -> table.colPhenomenonTimeStart, table -> table.colPhenomenonTimeEnd),
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntry(pluginCoreModel.epResult,
                new ConverterRecordDeflt<>(
                        (TableImpObservations<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            readResultFromDb(modelRegistry, table, tuple, entity, dataSize);
                        },
                        (table, entity, insertFields) -> {
                            handleResult(modelRegistry, table, insertFields, entity, true);
                        },
                        (table, entity, updateFields, message) -> {
                            handleResult(modelRegistry, table, updateFields, entity, true);
                            message.addField(pluginCoreModel.epResult);
                        }),
                new NFP<>("n", table -> table.colResultNumber),
                new NFP<>("b", table -> table.colResultBoolean),
                new NFP<>("s", table -> table.colResultString),
                new NFP<>("j", table -> table.colResultJson),
                new NFP<>("t", table -> table.colResultType));
        pfReg.addEntry(pluginCoreModel.epResultQuality, table -> table.colResultQuality,
                new ConverterRecordDeflt<>(
                        (TableImpObservations<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            JsonValue resultQuality = Utils.getFieldJsonValue(tuple, table.colResultQuality);
                            dataSize.increase(resultQuality.getStringLength());
                            entity.setProperty(pluginCoreModel.epResultQuality, resultQuality.getValue());
                        },
                        (table, entity, insertFields) -> {
                            insertFields.put(table.colResultQuality, EntityFactories.objectToJson(entity.getProperty(pluginCoreModel.epResultQuality)));
                        },
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
        pfReg.addEntry(pluginCoreModel.npFeatureOfInterest, TableImpObservations::getFeatureId, idManager);
        pfReg.addEntry(pluginCoreModel.npDatastream, TableImpObservations::getDatastreamId, idManager);

        registerHookPreInsert(0, (pm, entity, insertFields) -> {
            Entity f = entity.getProperty(pluginCoreModel.npFeatureOfInterest);
            if (f == null) {
                final Entity ds = entity.getProperty(pluginCoreModel.npDatastream);
                if (ds != null) {
                    f = generateFeatureOfInterest(entityFactories, pm, ds.getId());
                    if (f == null) {
                        throw new IncompleteEntityException("No FeatureOfInterest provided, and none can be generated.");
                    }
                    entity.setProperty(pluginCoreModel.npFeatureOfInterest, f);
                }
            }
        });
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etObservation;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    public TableField<Record, J> getDatastreamId() {
        return colDatastreamId;
    }

    public TableField<Record, J> getFeatureId() {
        return colFeatureId;
    }

    @Override
    public TableImpObservations<J> as(Name alias) {
        return new TableImpObservations<>(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpObservations<J> as(String alias) {
        return new TableImpObservations<>(DSL.name(alias), this, pluginCoreModel).initCustomFields();
    }

    @Override
    public PropertyFields<TableImpObservations<J>> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == pluginCoreModel.epParameters || mainEntityProperty == pluginCoreModel.epResultQuality) {
            PropertyFields<TableImpObservations<J>> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
            final Field mainField = mainPropertyFields.fields.values().iterator().next().get(getThis());

            JsonFieldFactory jsonFactory = jsonFieldFromPath(mainField, epCustomSelect);
            return propertyFieldForJsonField(jsonFactory, epCustomSelect);
        }
        return super.handleEntityPropertyCustomSelect(epCustomSelect);
    }

    @Override
    public TableImpObservations<J> getThis() {
        return this;
    }

    public <J extends Comparable<J>> void handleResult(ModelRegistry modelRegistry, TableImpObservations<J> table, Map<Field, Object> record, Entity entity, boolean isMultiDatastream) {
        Object result = entity.getProperty(pluginCoreModel.epResult);
        if (result instanceof Number) {
            record.put(table.colResultType, ResultType.NUMBER.sqlValue());
            record.put(table.colResultString, result.toString());
            record.put(table.colResultNumber, ((Number) result).doubleValue());
            record.put(table.colResultBoolean, null);
            record.put(table.colResultJson, null);
        } else if (result instanceof Boolean) {
            record.put(table.colResultType, ResultType.BOOLEAN.sqlValue());
            record.put(table.colResultString, result.toString());
            record.put(table.colResultBoolean, result);
            record.put(table.colResultNumber, null);
            record.put(table.colResultJson, null);
        } else if (result instanceof String) {
            record.put(table.colResultType, ResultType.STRING.sqlValue());
            record.put(table.colResultString, result.toString());
            record.put(table.colResultNumber, null);
            record.put(table.colResultBoolean, null);
            record.put(table.colResultJson, null);
        } else {
            record.put(table.colResultType, ResultType.OBJECT_ARRAY.sqlValue());
            record.put(table.colResultJson, EntityFactories.objectToJson(result));
            record.put(table.colResultString, null);
            record.put(table.colResultNumber, null);
            record.put(table.colResultBoolean, null);
        }
    }

    public <J extends Comparable<J>> void readResultFromDb(ModelRegistry modelRegistry, TableImpObservations<J> table, Record tuple, Entity entity, DataSize dataSize) {
        Short resultTypeOrd = Utils.getFieldOrNull(tuple, table.colResultType);
        if (resultTypeOrd != null) {
            ResultType resultType = ResultType.fromSqlValue(resultTypeOrd);
            switch (resultType) {
                case BOOLEAN:
                    entity.setProperty(pluginCoreModel.epResult, Utils.getFieldOrNull(tuple, table.colResultBoolean));
                    break;

                case NUMBER:
                    handleNumber(modelRegistry, table, tuple, entity);
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

    private <J extends Comparable> void handleNumber(ModelRegistry modelRegistry, TableImpObservations<J> table, Record tuple, Entity entity) {
        try {
            entity.setProperty(pluginCoreModel.epResult, new BigDecimal(Utils.getFieldOrNull(tuple, table.colResultString)));
        } catch (NumberFormatException | NullPointerException e) {
            // It was not a Number? Use the double value.
            entity.setProperty(pluginCoreModel.epResult, Utils.getFieldOrNull(tuple, table.colResultNumber));
        }
    }

    public Entity generateFeatureOfInterest(final EntityFactories<J> entityFactories, PostgresPersistenceManager<J> pm, Id datastreamId) throws NoSuchEntityException, IncompleteEntityException {
        final J dsId = (J) datastreamId.getValue();
        final DSLContext dslContext = pm.getDslContext();
        TableCollection<J> tableCollection = getTables();
        TableImpLocations<J> ql = tableCollection.getTableForClass(TableImpLocations.class);
        TableImpThingsLocations<J> qtl = tableCollection.getTableForClass(TableImpThingsLocations.class);
        TableImpThings<J> qt = tableCollection.getTableForClass(TableImpThings.class);
        TableImpDatastreams<J> qd = tableCollection.getTableForClass(TableImpDatastreams.class);

        SelectConditionStep<Record3<J, J, String>> query = dslContext.select(ql.getId(), ql.getGenFoiId(), ql.colEncodingType)
                .from(ql)
                .innerJoin(qtl).on(ql.getId().eq(qtl.getLocationId()))
                .innerJoin(qt).on(qt.getId().eq(qtl.getThingId()))
                .innerJoin(qd).on(qd.getThingId().eq(qt.getId()))
                .where(qd.getId().eq(dsId));
        return generateFeatureOfInterest(pm, query);
    }

    public Entity generateFeatureOfInterest(PostgresPersistenceManager<J> pm, ResultQuery<Record3<J, J, String>> locationQuery) throws NoSuchEntityException, IncompleteEntityException {
        final DSLContext dslContext = pm.getDslContext();
        TableImpLocations<J> ql = getTables().getTableForClass(TableImpLocations.class);
        Result<Record3<J, J, String>> tuples = locationQuery.fetch();
        if (tuples.isEmpty()) {
            // No locations found.
            return null;
        }
        // See if any of the locations have a generated foi.
        // Also track if any of the location has a supported encoding type.
        J genFoiId = null;
        J locationId = null;
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
        Entity foi;
        if (genFoiId != null) {
            foi = new DefaultEntity(pluginCoreModel.etFeatureOfInterest, pm.getEntityFactories().idFromObject(genFoiId));
        } else if (locationId != null) {
            SelectConditionStep<Record3<J, String, String>> query2 = dslContext.select(ql.getId(), ql.colEncodingType, ql.colLocation)
                    .from(ql)
                    .where(ql.getId().eq(locationId));
            Record tuple = query2.fetchOne();
            if (tuple == null) {
                // Can not generate foi from Thing with no locations.
                // Should not happen, since the query succeeded just before.
                return null;
            }
            String encoding = getFieldOrNull(tuple, ql.colEncodingType);
            String locString = getFieldOrNull(tuple, ql.colLocation);
            Object locObject = Utils.locationFromEncoding(encoding, locString);
            foi = new DefaultEntity(pluginCoreModel.etFeatureOfInterest)
                    .setProperty(pluginCoreModel.epName, "FoI for location " + locationId)
                    .setProperty(pluginCoreModel.epDescription, "Generated from location " + locationId)
                    .setProperty(ModelRegistry.EP_ENCODINGTYPE, encoding)
                    .setProperty(pluginCoreModel.epFeature, locObject);
            pm.insert(foi);
            J foiId = (J) foi.getId().getValue();
            dslContext.update(ql)
                    .set(ql.getGenFoiId(), (J) foi.getId().getValue())
                    .where(ql.getId().eq(locationId))
                    .execute();
            LOGGER.debug("Generated foi {} from Location {}.", foiId, locationId);
        } else {
            // Can not generate foi from Thing with no locations.
            return null;
        }
        return foi;
    }

}
