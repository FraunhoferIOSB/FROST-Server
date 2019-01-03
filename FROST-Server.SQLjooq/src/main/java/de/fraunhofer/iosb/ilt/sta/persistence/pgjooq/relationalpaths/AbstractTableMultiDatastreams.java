package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.codegen.maven.example.Public;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableMultiDatastreams<J> extends TableImpl<AbstractRecordMultiDatastreams<J>> implements StaTable<J, AbstractRecordMultiDatastreams<J>> {

    private static final long serialVersionUID = 560943996;

    public abstract TableField<AbstractRecordMultiDatastreams<J>, J> getId();

    public abstract TableField<AbstractRecordMultiDatastreams<J>, J> getSensorId();

    public abstract TableField<AbstractRecordMultiDatastreams<J>, J> getThingId();

    /**
     * The column <code>public.MULTI_DATASTREAMS.NAME</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVATION_TYPES</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, String> observationTypes = createField("OBSERVATION_TYPES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, OffsetDateTime> phenomenonTimeStart = createField("PHENOMENON_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, OffsetDateTime> phenomenonTimeEnd = createField("PHENOMENON_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, OffsetDateTime> resultTimeStart = createField("RESULT_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, OffsetDateTime> resultTimeEnd = createField("RESULT_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.UNIT_OF_MEASUREMENTS</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, String> unitOfMeasurements = createField("UNIT_OF_MEASUREMENTS", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    public final TableField<AbstractRecordMultiDatastreams<J>, Object> observedArea = createField("OBSERVED_AREA", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"geometry\""), this, "");

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> observedAreaText = DSL.field("text(?)", String.class, observedArea);

    /**
     * The column <code>public.MULTI_DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<AbstractRecordMultiDatastreams<J>, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference
     */
    protected AbstractTableMultiDatastreams() {
        this(DSL.name("MULTI_DATASTREAMS"), null);
    }

    protected AbstractTableMultiDatastreams(Name alias, AbstractTableMultiDatastreams<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableMultiDatastreams(Name alias, AbstractTableMultiDatastreams<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public abstract AbstractTableMultiDatastreams<J> as(Name as);

    @Override
    public abstract AbstractTableMultiDatastreams<J> as(String alias);

}
