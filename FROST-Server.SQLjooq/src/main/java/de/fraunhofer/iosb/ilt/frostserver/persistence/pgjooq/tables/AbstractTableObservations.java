package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableObservations<J extends Comparable> extends StaTableAbstract<J, Observation, AbstractTableObservations<J>> {

    private static final long serialVersionUID = -1104422281;

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeStart = createField(DSL.name("PHENOMENON_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colPhenomenonTimeEnd = createField(DSL.name("PHENOMENON_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> colResultTime = createField(DSL.name("RESULT_TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_NUMBER</code>.
     */
    public final TableField<Record, Double> colResultNumber = createField(DSL.name("RESULT_NUMBER"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_STRING</code>.
     */
    public final TableField<Record, String> colResultString = createField(DSL.name("RESULT_STRING"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_QUALITY</code>.
     */
    public final TableField<Record, JsonValue> colResultQuality = createField(DSL.name("RESULT_QUALITY"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());
    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> colValidTimeStart = createField(DSL.name("VALID_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> colValidTimeEnd = createField(DSL.name("VALID_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.PARAMETERS</code>.
     */
    public final TableField<Record, JsonValue> colParameters = createField(DSL.name("PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());
    /**
     * The column <code>public.OBSERVATIONS.RESULT_TYPE</code>.
     */
    public final TableField<Record, Short> colResultType = createField(DSL.name("RESULT_TYPE"), SQLDataType.SMALLINT, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_JSON</code>.
     */
    public final TableField<Record, JsonValue> colResultJson = createField(DSL.name("RESULT_JSON"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());
    /**
     * The column <code>public.OBSERVATIONS.RESULT_BOOLEAN</code>.
     */
    public final TableField<Record, Boolean> colResultBoolean = createField(DSL.name("RESULT_BOOLEAN"), SQLDataType.BOOLEAN, this, "");

    /**
     * Create a <code>public.OBSERVATIONS</code> table reference
     */
    protected AbstractTableObservations() {
        this(DSL.name("OBSERVATIONS"), null);
    }

    protected AbstractTableObservations(Name alias, AbstractTableObservations<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableObservations(Name alias, AbstractTableObservations<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(getThis(), tables.getTableDatastreams(), EntityType.DATASTREAM)
                        .setSourceFieldAccessor(AbstractTableObservations::getDatastreamId)
                        .setTargetFieldAccessor(AbstractTableDatastreams::getId)
        );

        registerRelation(
                new RelationOneToMany<>(getThis(), tables.getTableMultiDatastreams(), EntityType.MULTIDATASTREAM)
                        .setSourceFieldAccessor(AbstractTableObservations::getMultiDatastreamId)
                        .setTargetFieldAccessor(AbstractTableMultiDatastreams::getId)
        );

        registerRelation(
                new RelationOneToMany<>(getThis(), tables.getTableFeatures(), EntityType.FEATUREOFINTEREST)
                        .setSourceFieldAccessor(AbstractTableObservations::getFeatureId)
                        .setTargetFieldAccessor(AbstractTableFeatures::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntry(EntityPropertyMain.ID, AbstractTableObservations<J>::getId,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    entity.setId(idManager.fromObject(tuple.get(table.getId())));
                });
        pfReg.addEntry(EntityPropertyMain.SELFLINK, AbstractTableObservations<J>::getId,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    entity.setId(idManager.fromObject(tuple.get(table.getId())));
                });
        pfReg.addEntry(EntityPropertyMain.PARAMETERS, table -> table.colParameters,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    JsonValue props = Utils.getFieldJsonValue(tuple, table.colParameters);
                    dataSize.increase(props.getStringLength());
                    entity.setParameters(props.getMapValue());
                });
        pfReg.addEntry(EntityPropertyMain.PHENOMENONTIME,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    entity.setPhenomenonTime(Utils.valueFromTimes(
                            tuple.get(table.colPhenomenonTimeStart),
                            tuple.get(table.colPhenomenonTimeEnd)));
                },
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd));
        pfReg.addEntry(EntityPropertyMain.RESULT,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    Utils.readResultFromDb(table, tuple, entity, dataSize);
                },
                new NFP<>("n", table -> table.colResultNumber),
                new NFP<>("b", table -> table.colResultBoolean),
                new NFP<>("s", table -> table.colResultString),
                new NFP<>("j", table -> table.colResultJson),
                new NFP<>("t", table -> table.colResultType));
        pfReg.addEntry(EntityPropertyMain.RESULTQUALITY, table -> table.colResultQuality,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    JsonValue resultQuality = Utils.getFieldJsonValue(tuple, table.colResultQuality);
                    dataSize.increase(resultQuality.getStringLength());
                    entity.setResultQuality(resultQuality.getValue());
                });
        pfReg.addEntry(EntityPropertyMain.RESULTTIME, table -> table.colResultTime,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    entity.setResultTime(Utils.instantFromTime(tuple.get(table.colResultTime)));
                });
        pfReg.addEntry(EntityPropertyMain.VALIDTIME,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    entity.setValidTime(Utils.intervalFromTimes(
                            tuple.get(table.colValidTimeStart),
                            tuple.get(table.colValidTimeEnd)));
                },
                new NFP<>(KEY_TIME_INTERVAL_START, table -> table.colValidTimeStart),
                new NFP<>(KEY_TIME_INTERVAL_END, table -> table.colValidTimeEnd));
        pfReg.addEntry(NavigationPropertyMain.FEATUREOFINTEREST, AbstractTableObservations::getFeatureId,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    entity.setFeatureOfInterest(entityFactories.featureOfInterestFromId(tuple, table.getFeatureId()));
                });
        pfReg.addEntry(NavigationPropertyMain.DATASTREAM, AbstractTableObservations::getDatastreamId,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    entity.setDatastream(entityFactories.datastreamFromId(tuple, table.getDatastreamId()));
                });
        pfReg.addEntry(NavigationPropertyMain.MULTIDATASTREAM, AbstractTableObservations::getMultiDatastreamId,
                (AbstractTableObservations<J> table, Record tuple, Observation entity, DataSize dataSize) -> {
                    entity.setMultiDatastream(entityFactories.multiDatastreamFromId(tuple, table.getMultiDatastreamId()));
                });
    }

    @Override
    public Observation newEntity() {
        return new Observation();
    }

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getDatastreamId();

    public abstract TableField<Record, J> getFeatureId();

    public abstract TableField<Record, J> getMultiDatastreamId();

    @Override
    public abstract AbstractTableObservations<J> as(Name as);

    @Override
    public abstract AbstractTableObservations<J> as(String alias);

    @Override
    public AbstractTableObservations<J> getThis() {
        return this;
    }
}
