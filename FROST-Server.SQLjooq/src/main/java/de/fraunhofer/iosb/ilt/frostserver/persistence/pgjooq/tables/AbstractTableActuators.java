package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableActuators<J> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The column <code>public.ACTUATORS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.ACTUATORS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> encodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.ACTUATORS.METADATA</code>.
     */
    public final TableField<Record, String> metadata = createField(DSL.name("METADATA"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.ACTUATORS.NAME</code>.
     */
    public final TableField<Record, String> name = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.ACTUATORS.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField(DSL.name("PROPERTIES"), SQLDataType.CLOB, this, "");

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
    public abstract TableField<Record, J> getId();

    @Override
    public abstract AbstractTableActuators<J> as(Name as);

    @Override
    public abstract AbstractTableActuators<J> as(String alias);

}
