package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThingsLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongThingsLocations extends AbstractTableThingsLocations<Long> {

    private static final long serialVersionUID = -1443552218;

    /**
     * The reference instance of <code>public.THINGS_LOCATIONS</code>
     */
    public static final TableLongThingsLocations THINGS_LOCATIONS = new TableLongThingsLocations();

    /**
     * The column <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, Long> colThingId = createField(DSL.name("THING_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, Long> colLocationId = createField(DSL.name("LOCATION_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableLongThingsLocations() {
        this(DSL.name("THINGS_LOCATIONS"), null);
    }

    /**
     * Create an aliased <code>public.THINGS_LOCATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableLongThingsLocations(Name alias) {
        this(alias, THINGS_LOCATIONS);
    }

    private TableLongThingsLocations(Name alias, TableLongThingsLocations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getLocationId() {
        return colLocationId;
    }

    @Override
    public TableField<Record, Long> getThingId() {
        return colThingId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongThingsLocations as(String alias) {
        return new TableLongThingsLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongThingsLocations as(Name alias) {
        return new TableLongThingsLocations(alias, this);
    }

}
