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

public abstract class AbstractTableMultiDatastreams<J> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = 560943996;

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getSensorId();

    public abstract TableField<Record, J> getThingId();

    /**
     * The column <code>public.MULTI_DATASTREAMS.NAME</code>.
     */
    public final TableField<Record, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVATION_TYPES</code>.
     */
    public final TableField<Record, String> observationTypes = createField("OBSERVATION_TYPES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> phenomenonTimeStart = createField("PHENOMENON_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> phenomenonTimeEnd = createField("PHENOMENON_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final TableField<Record, OffsetDateTime> resultTimeStart = createField("RESULT_TIME_START", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final TableField<Record, OffsetDateTime> resultTimeEnd = createField("RESULT_TIME_END", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.UNIT_OF_MEASUREMENTS</code>.
     */
    public final TableField<Record, String> unitOfMeasurements = createField("UNIT_OF_MEASUREMENTS", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.OBSERVED_AREA</code>.
     */
    public final TableField<Record, Geometry> observedArea = createField("OBSERVED_AREA", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"geometry\""), this, "", new PostGisGeometryBinding());

    /**
     * A helper field for getting the observedArea
     */
    public final Field<String> observedAreaText = DSL.field("text(?)", String.class, observedArea);

    /**
     * The column <code>public.MULTI_DATASTREAMS.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

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

    @Override
    public abstract AbstractTableMultiDatastreams<J> as(Name as);

    @Override
    public abstract AbstractTableMultiDatastreams<J> as(String alias);

}
