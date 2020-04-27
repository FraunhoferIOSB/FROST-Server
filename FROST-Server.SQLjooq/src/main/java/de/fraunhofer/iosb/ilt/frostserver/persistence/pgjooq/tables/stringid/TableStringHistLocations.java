package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableHistLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringHistLocations extends AbstractTableHistLocations<String> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.HIST_LOCATIONS</code>
     */
    public static final TableStringHistLocations HIST_LOCATIONS = new TableStringHistLocations();

    /**
     * The column <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final TableField<Record, String> colId = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, String> colThingId = createField(DSL.name("THING_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableStringHistLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.HIST_LOCATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringHistLocations(Name alias) {
        this(alias, HIST_LOCATIONS);
    }

    private TableStringHistLocations(Name alias, TableStringHistLocations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return colId;
    }

    @Override
    public TableField<Record, String> getThingId() {
        return colThingId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringHistLocations as(String alias) {
        return new TableStringHistLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringHistLocations as(Name alias) {
        return new TableStringHistLocations(alias, this);
    }

}
