package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public abstract class AbstractTableObservations<J extends Comparable> extends StaTableAbstract<J> {

    private static final long serialVersionUID = -1104422281;

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> phenomenonTimeStart = createField(DSL.name("PHENOMENON_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> phenomenonTimeEnd = createField(DSL.name("PHENOMENON_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_TIME</code>.
     */
    public final TableField<Record, OffsetDateTime> resultTime = createField(DSL.name("RESULT_TIME"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_NUMBER</code>.
     */
    public final TableField<Record, Double> resultNumber = createField(DSL.name("RESULT_NUMBER"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_STRING</code>.
     */
    public final TableField<Record, String> resultString = createField(DSL.name("RESULT_STRING"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_QUALITY</code>.
     */
    public final TableField<Record, String> resultQuality = createField(DSL.name("RESULT_QUALITY"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> validTimeStart = createField(DSL.name("VALID_TIME_START"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> validTimeEnd = createField(DSL.name("VALID_TIME_END"), SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.PARAMETERS</code>.
     */
    public final TableField<Record, String> parameters = createField(DSL.name("PARAMETERS"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_TYPE</code>.
     */
    public final TableField<Record, Short> resultType = createField(DSL.name("RESULT_TYPE"), SQLDataType.SMALLINT, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_JSON</code>.
     */
    public final TableField<Record, String> resultJson = createField(DSL.name("RESULT_JSON"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_BOOLEAN</code>.
     */
    public final TableField<Record, Boolean> resultBoolean = createField(DSL.name("RESULT_BOOLEAN"), SQLDataType.BOOLEAN, this, "");

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
                new RelationOneToMany<>(this, tables.getTableDatastreams(), EntityType.DATASTREAM)
                        .setSourceFieldAccessor(AbstractTableObservations::getDatastreamId)
                        .setTargetFieldAccessor(AbstractTableDatastreams::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableMultiDatastreams(), EntityType.MULTIDATASTREAM)
                        .setSourceFieldAccessor(AbstractTableObservations::getMultiDatastreamId)
                        .setTargetFieldAccessor(AbstractTableMultiDatastreams::getId)
        );

        registerRelation(
                new RelationOneToMany<>(this, tables.getTableFeatures(), EntityType.FEATUREOFINTEREST)
                        .setSourceFieldAccessor(AbstractTableObservations::getFeatureId)
                        .setTargetFieldAccessor(AbstractTableFeatures::getId)
        );
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

}
