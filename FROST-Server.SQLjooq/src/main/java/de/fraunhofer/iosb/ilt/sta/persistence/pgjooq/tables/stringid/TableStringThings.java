package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThings;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringThings extends AbstractTableThings<String> {

    private static final long serialVersionUID = -729589982;

    /**
     * The reference instance of <code>public.THINGS</code>
     */
    public static final TableStringThings THINGS = new TableStringThings();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    /**
     * The column <code>public.THINGS.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.THINGS</code> table reference
     */
    public TableStringThings() {
        super();
    }

    /**
     * Create an aliased <code>public.THINGS</code> table reference
     */
    public TableStringThings(String alias) {
        this(DSL.name(alias), THINGS);
    }

    /**
     * Create an aliased <code>public.THINGS</code> table reference
     */
    public TableStringThings(Name alias) {
        this(alias, THINGS);
    }

    private TableStringThings(Name alias, TableStringThings aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringThings as(String alias) {
        return new TableStringThings(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringThings as(Name alias) {
        return new TableStringThings(alias, this);
    }

}
