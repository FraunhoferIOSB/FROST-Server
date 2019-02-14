package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostGisGeometryBinding;
import java.time.OffsetDateTime;
import org.geolatte.geom.Geometry;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableDatastreams<J> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = -1460005950;

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getSensorId();

    public abstract TableField<Record, J> getObsPropertyId();

    public abstract TableField<Record, J> getThingId();

    /**
     * The column <code>public.DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.DATASTREAMS.OBSERVATION_TYPE</code>.
     */
    public final TableField<Record, String> observationType = createField("OBSERVATION_TYPE", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> phenomenonTimeStart = createField("PHENOMENON_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> phenomenonTimeEnd = createField("PHENOMENON_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> resultTimeStart = createField("RESULT_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> resultTimeEnd = createField("RESULT_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_NAME</code>.
     */
    public final TableField<Record, String> unitName = createField("UNIT_NAME", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_SYMBOL</code>.
     */
    public final TableField<Record, String> unitSymbol = createField("UNIT_SYMBOL", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.UNIT_DEFINITION</code>.
     */
    public final TableField<Record, String> unitDefinition = createField("UNIT_DEFINITION", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.DATASTREAMS.NAME</code>.
     */
    public final TableField<Record, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB.defaultValue(org.jooq.impl.DSL.field("'no name'::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.DATASTREAMS.OBSERVED_AREA</code>.
     */
    public final TableField<Record, Geometry> observedArea = createField("OBSERVED_AREA", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"geometry\""), this, "", new PostGisGeometryBinding());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> observedAreaText = DSL.field("text(?)", String.class, observedArea);

    /**
     * The column <code>public.DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

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
