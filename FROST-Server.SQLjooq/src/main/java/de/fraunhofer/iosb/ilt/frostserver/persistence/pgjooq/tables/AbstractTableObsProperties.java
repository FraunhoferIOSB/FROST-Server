package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableObsProperties<J> extends TableImpl<Record> implements StaTable<J> {

    private static final long serialVersionUID = -1873692390;

    public abstract TableField<Record, J> getId();

    /**
     * The column <code>public.OBS_PROPERTIES.NAME</code>.
     */
    public final TableField<Record, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.DEFINITION</code>.
     */
    public final TableField<Record, String> definition = createField("DEFINITION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.OBS_PROPERTIES.PROPERTIES</code>.
     */
    public final TableField<Record, String> properties = createField("PROPERTIES", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference
     */
    protected AbstractTableObsProperties() {
        this(DSL.name("OBS_PROPERTIES"), null);
    }

    protected AbstractTableObsProperties(Name alias, AbstractTableObsProperties<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableObsProperties(Name alias, AbstractTableObsProperties<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableObsProperties<J> as(Name as);

    @Override
    public abstract AbstractTableObsProperties<J> as(String alias);

}
