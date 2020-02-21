package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThings;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidThings extends AbstractTableThings<UUID> {

    private static final long serialVersionUID = -729589982;

    /**
     * The reference instance of <code>public.THINGS</code>
     */
    public static final TableUuidThings THINGS = new TableUuidThings();

    /**
     * The column <code>public.THINGS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField(DSL.name("ID"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.UUID)), this, "");

    /**
     * Create a <code>public.THINGS</code> table reference
     */
    public TableUuidThings() {
        super();
    }

    /**
     * Create an aliased <code>public.THINGS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidThings(Name alias) {
        this(alias, THINGS);
    }

    private TableUuidThings(Name alias, TableUuidThings aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidThings as(String alias) {
        return new TableUuidThings(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidThings as(Name alias) {
        return new TableUuidThings(alias, this);
    }

}
