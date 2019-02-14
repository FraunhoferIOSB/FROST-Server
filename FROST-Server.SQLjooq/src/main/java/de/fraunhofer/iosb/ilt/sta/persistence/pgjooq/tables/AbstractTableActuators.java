package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableActuators<J> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = 1850108682;

    @Override
    public abstract TableField<Record, J> getId();

    /**
     * The column <code>public.ACTUATORS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.ACTUATORS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> encodingType = createField("ENCODING_TYPE", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.ACTUATORS.METADATA</code>.
     */
    public final TableField<Record, String> metadata = createField("METADATA", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.ACTUATORS.NAME</code>.
     */
    public final TableField<Record, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB.defaultValue(org.jooq.impl.DSL.field("'no name'::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.ACTUATORS.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.ACTUATORS</code> table reference
     */
    protected AbstractTableActuators() {
        this(DSL.name("ACTUATORS"), null);
    }

    protected AbstractTableActuators(Name alias, AbstractTableActuators<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableActuators(Name alias, AbstractTableActuators<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableActuators<J> as(Name as);

    @Override
    public abstract AbstractTableActuators<J> as(String alias);

}
