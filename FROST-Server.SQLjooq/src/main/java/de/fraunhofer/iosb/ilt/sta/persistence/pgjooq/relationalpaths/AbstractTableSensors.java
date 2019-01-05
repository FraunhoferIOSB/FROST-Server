package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.TableField;

import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableSensors<J> extends TableImpl<AbstractRecordSensors<J>> implements StaTable<J, AbstractRecordSensors<J>> {

    private static final long serialVersionUID = 1850108682;

    public abstract TableField<AbstractRecordSensors<J>, J> getId();

    /**
     * The column <code>public.SENSORS.DESCRIPTION</code>.
     */
    public final TableField<AbstractRecordSensors<J>, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.SENSORS.ENCODING_TYPE</code>.
     */
    public final TableField<AbstractRecordSensors<J>, String> encodingType = createField("ENCODING_TYPE", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.SENSORS.METADATA</code>.
     */
    public final TableField<AbstractRecordSensors<J>, String> metadata = createField("METADATA", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.SENSORS.NAME</code>.
     */
    public final TableField<AbstractRecordSensors<J>, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB.defaultValue(org.jooq.impl.DSL.field("'no name'::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * The column <code>public.SENSORS.PROPERTIES</code>.
     */
    public final TableField<AbstractRecordSensors<J>, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    protected AbstractTableSensors() {
        this(DSL.name("SENSORS"), null);
    }

    protected AbstractTableSensors(Name alias, AbstractTableSensors<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableSensors(Name alias, AbstractTableSensors<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableSensors<J> as(Name as);

    @Override
    public abstract AbstractTableSensors<J> as(String alias);

}
