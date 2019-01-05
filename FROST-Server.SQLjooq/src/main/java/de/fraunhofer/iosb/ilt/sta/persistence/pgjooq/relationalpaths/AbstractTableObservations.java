package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.TableField;

import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableObservations<J> extends TableImpl<AbstractRecordObservations<J>> implements StaTable<J, AbstractRecordObservations<J>> {

    private static final long serialVersionUID = -1104422281;

    public abstract TableField<AbstractRecordObservations<J>, J> getId();

    public abstract TableField<AbstractRecordObservations<J>, J> getDatastreamId();

    public abstract TableField<AbstractRecordObservations<J>, J> getFeatureId();

    public abstract TableField<AbstractRecordObservations<J>, J> getMultiDatastreamId();

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<AbstractRecordObservations<J>, OffsetDateTime> phenomenonTimeStart = createField("PHENOMENON_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<AbstractRecordObservations<J>, OffsetDateTime> phenomenonTimeEnd = createField("PHENOMENON_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_TIME</code>.
     */
    public final TableField<AbstractRecordObservations<J>, OffsetDateTime> resultTime = createField("RESULT_TIME", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_NUMBER</code>.
     */
    public final TableField<AbstractRecordObservations<J>, Double> resultNumber = createField("RESULT_NUMBER", org.jooq.impl.SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_STRING</code>.
     */
    public final TableField<AbstractRecordObservations<J>, String> resultString = createField("RESULT_STRING", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_QUALITY</code>.
     */
    public final TableField<AbstractRecordObservations<J>, String> resultQuality = createField("RESULT_QUALITY", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_START</code>.
     */
    public final TableField<AbstractRecordObservations<J>, OffsetDateTime> validTimeStart = createField("VALID_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.VALID_TIME_END</code>.
     */
    public final TableField<AbstractRecordObservations<J>, OffsetDateTime> validTimeEnd = createField("VALID_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.OBSERVATIONS.PARAMETERS</code>.
     */
    public final TableField<AbstractRecordObservations<J>, String> parameters = createField("PARAMETERS", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_TYPE</code>.
     */
    public final TableField<AbstractRecordObservations<J>, Short> resultType = createField("RESULT_TYPE", org.jooq.impl.SQLDataType.SMALLINT, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_JSON</code>.
     */
    public final TableField<AbstractRecordObservations<J>, String> resultJson = createField("RESULT_JSON", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBSERVATIONS.RESULT_BOOLEAN</code>.
     */
    public final TableField<AbstractRecordObservations<J>, Boolean> resultBoolean = createField("RESULT_BOOLEAN", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

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
    public abstract AbstractTableObservations<J> as(Name as);

    @Override
    public abstract AbstractTableObservations<J> as(String alias);

}
