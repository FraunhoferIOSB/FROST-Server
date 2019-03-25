package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThings;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableUuidThings extends AbstractTableThings<UUID> {

    private static final long serialVersionUID = -729589982;

    /**
     * The reference instance of <code>public.THINGS</code>
     */
    public static final TableUuidThings THINGS = new TableUuidThings();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    /**
     * The column <code>public.THINGS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.UUID)), this, "");

    /**
     * Create a <code>public.THINGS</code> table reference
     */
    public TableUuidThings() {
        super();
    }

    /**
     * Create an aliased <code>public.THINGS</code> table reference
     */
    public TableUuidThings(String alias) {
        this(DSL.name(alias), THINGS);
    }

    /**
     * Create an aliased <code>public.THINGS</code> table reference
     */
    public TableUuidThings(Name alias) {
        this(alias, THINGS);
    }

    private TableUuidThings(Name alias, TableUuidThings aliased) {
        super(alias, aliased);
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
