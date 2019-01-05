package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.TableField;

import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableDatastreams<J> extends TableImpl<AbstractRecordDatastreams<J>> implements StaTable<J, AbstractRecordDatastreams<J>> {

    private static final long serialVersionUID = -1460005950;

    @Override
    public abstract TableField<AbstractRecordDatastreams<J>, J> getId();

    public abstract TableField<AbstractRecordDatastreams<J>, J> getSensorId();

    public abstract TableField<AbstractRecordDatastreams<J>, J> getObsPropertyId();

    public abstract TableField<AbstractRecordDatastreams<J>, J> getThingId();

    /**
     * The column <code>public.DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.DATASTREAMS.OBSERVATION_TYPE</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, String> observationType = createField("OBSERVATION_TYPE", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, OffsetDateTime> phenomenonTimeStart = createField("PHENOMENON_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, OffsetDateTime> phenomenonTimeEnd = createField("PHENOMENON_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, OffsetDateTime> resultTimeStart = createField("RESULT_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, OffsetDateTime> resultTimeEnd = createField("RESULT_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_NAME</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, String> unitName = createField("UNIT_NAME", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_SYMBOL</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, String> unitSymbol = createField("UNIT_SYMBOL", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_DEFINITION</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, String> unitDefinition = createField("UNIT_DEFINITION", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.NAME</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB.defaultValue(org.jooq.impl.DSL.field("'no name'::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    public final TableField<AbstractRecordDatastreams<J>, Object> observedArea = createField("OBSERVED_AREA", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"geometry\""), this, "");

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> observedAreaText = DSL.field("text(?)", String.class, observedArea);

    /**
     * The column <code>public.DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<AbstractRecordDatastreams<J>, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.DATASTREAMS</code> table reference
     */
    protected AbstractTableDatastreams() {
        this(DSL.name("DATASTREAMS"), null);
    }

    protected AbstractTableDatastreams(Name alias, AbstractTableDatastreams<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableDatastreams(Name alias, AbstractTableDatastreams<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableDatastreams<J> as(String alias);

    @Override
    public abstract AbstractTableDatastreams<J> as(Name as);

}
