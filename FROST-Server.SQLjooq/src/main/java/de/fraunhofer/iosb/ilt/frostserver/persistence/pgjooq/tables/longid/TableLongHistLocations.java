package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableHistLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongHistLocations extends AbstractTableHistLocations<Long> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.HIST_LOCATIONS</code>
     */
    public static final TableLongHistLocations HIST_LOCATIONS = new TableLongHistLocations();

    /**
     * The column <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final TableField<Record, Long> id = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"HIST_LOCATIONS_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, Long> thingId = createField(DSL.name("THING_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableLongHistLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.HIST_LOCATIONS</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableLongHistLocations(Name alias) {
        this(alias, HIST_LOCATIONS);
    }

    private TableLongHistLocations(Name alias, TableLongHistLocations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return id;
    }

    @Override
    public TableField<Record, Long> getThingId() {
        return thingId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongHistLocations as(String alias) {
        return new TableLongHistLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongHistLocations as(Name alias) {
        return new TableLongHistLocations(alias, this);
    }

}
