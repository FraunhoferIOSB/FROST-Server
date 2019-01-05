package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.TableField;

import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

public abstract class AbstractTableThings<J> extends TableImpl<AbstractRecordThings<J>> implements StaTable<J, AbstractRecordThings<J>> {

    private static final long serialVersionUID = -729589982;

    public abstract TableField<AbstractRecordThings<J>, J> getId();

    /**
     * The column <code>public.THINGS.DESCRIPTION</code>.
     */
    public final TableField<AbstractRecordThings<J>, String> description = createField("DESCRIPTION", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    public final TableField<AbstractRecordThings<J>, Object> properties = createField("PROPERTIES", org.jooq.impl.DefaultDataType.getDefaultDataType("\"pg_catalog\".\"jsonb\""), this, "");

    /**
     * The column <code>public.THINGS.NAME</code>.
     */
    public final TableField<AbstractRecordThings<J>, String> name = createField("NAME", org.jooq.impl.SQLDataType.CLOB.defaultValue(org.jooq.impl.DSL.field("'no name'::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * Create a <code>public.THINGS</code> table reference
     */
    protected AbstractTableThings() {
        this(DSL.name("THINGS"), null);
    }

    protected AbstractTableThings(Name alias, AbstractTableThings<J> aliased) {
        this(alias, aliased, null);
    }

    protected AbstractTableThings(Name alias, AbstractTableThings<J> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    @Override
    public abstract AbstractTableThings<J> as(Name as);

    @Override
    public abstract AbstractTableThings<J> as(String alias);

}
