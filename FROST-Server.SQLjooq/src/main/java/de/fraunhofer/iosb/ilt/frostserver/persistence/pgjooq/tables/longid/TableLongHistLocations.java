package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableHistLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongHistLocations extends AbstractTableHistLocations<Long> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.HIST_LOCATIONS</code>
     */
    public static final TableLongHistLocations HIST_LOCATIONS = new TableLongHistLocations();

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
     * The column <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final TableField<Record, Long> id = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"HIST_LOCATIONS_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, Long> thingId = createField("THING_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableLongHistLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.HIST_LOCATIONS</code> table reference
     */
    public TableLongHistLocations(Name alias) {
        this(alias, HIST_LOCATIONS);
    }

    private TableLongHistLocations(Name alias, TableLongHistLocations aliased) {
        super(alias, aliased);
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
