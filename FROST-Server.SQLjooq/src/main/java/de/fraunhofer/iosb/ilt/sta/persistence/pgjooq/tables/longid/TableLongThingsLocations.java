package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThingsLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongThingsLocations extends AbstractTableThingsLocations<Long> {

    private static final long serialVersionUID = -1443552218;

    /**
     * The reference instance of <code>public.THINGS_LOCATIONS</code>
     */
    public static final TableLongThingsLocations THINGS_LOCATIONS = new TableLongThingsLocations();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getLocationId() {
        return LOCATION_ID;
    }

    @Override
    public TableField<Record, Long> getThingId() {
        return THING_ID;
    }

    /**
     * The column <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, Long> THING_ID = createField("THING_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, Long> LOCATION_ID = createField("LOCATION_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableLongThingsLocations() {
        this(DSL.name("THINGS_LOCATIONS"), null);
    }

    /**
     * Create an aliased <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableLongThingsLocations(String alias) {
        this(DSL.name(alias), THINGS_LOCATIONS);
    }

    /**
     * Create an aliased <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableLongThingsLocations(Name alias) {
        this(alias, THINGS_LOCATIONS);
    }

    private TableLongThingsLocations(Name alias, TableLongThingsLocations aliased) {
        super(alias, aliased);
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
