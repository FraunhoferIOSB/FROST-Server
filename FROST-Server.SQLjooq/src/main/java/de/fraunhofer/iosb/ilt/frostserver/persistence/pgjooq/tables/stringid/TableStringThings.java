package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThings;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringThings extends AbstractTableThings<String> {

    private static final long serialVersionUID = -729589982;

    /**
     * The reference instance of <code>public.THINGS</code>
     */
    public static final TableStringThings THINGS = new TableStringThings();

    /**
     * The column <code>public.THINGS.ID</code>.
     */
    public final TableField<Record, String> id = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.THINGS</code> table reference
     */
    public TableStringThings() {
        super();
    }

    /**
     * Create an aliased <code>public.THINGS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringThings(Name alias) {
        this(alias, THINGS);
    }

    private TableStringThings(Name alias, TableStringThings aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
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
