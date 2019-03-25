package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableTaskingCapabilities<J> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = -1460005950;

    @Override
    public abstract TableField<Record, J> getId();

    public abstract TableField<Record, J> getActuatorId();

    public abstract TableField<Record, J> getThingId();

    /**
     * The column <code>public.TASKINGCAPABILITIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.NAME</code>.
     */
    public final TableField<Record, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB.defaultValue(org.jooq.impl.DSL.field("'no name'::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.PROPERTIES</code>.
     */
    public final TableField<Record, String> taskingParameters = createField("TASKING_PARAMETERS", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.TASKINGCAPABILITIES</code> table reference
     */
    protected AbstractTableTaskingCapabilities() {
        this(DSL.name("TASKINGCAPABILITIES"), null);
    }

    protected AbstractTableTaskingCapabilities(Name alias, AbstractTableTaskingCapabilities<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableTaskingCapabilities(Name alias, AbstractTableTaskingCapabilities<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableTaskingCapabilities<J> as(String alias);

    @Override
    public abstract AbstractTableTaskingCapabilities<J> as(Name as);

}
