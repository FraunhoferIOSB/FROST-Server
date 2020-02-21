package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThingsLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringThingsLocations extends AbstractTableThingsLocations<String> {

    private static final long serialVersionUID = -1443552218;

    /**
     * The reference instance of <code>public.THINGS_LOCATIONS</code>
     */
    public static final TableStringThingsLocations THINGS_LOCATIONS = new TableStringThingsLocations();

    /**
     * The column <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, String> thingId = createField(DSL.name("THING_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, String> locationId = createField(DSL.name("LOCATION_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableStringThingsLocations() {
        this(DSL.name("THINGS_LOCATIONS"), null);
    }

    /**
     * Create an aliased <code>public.THINGS_LOCATIONS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringThingsLocations(Name alias) {
        this(alias, THINGS_LOCATIONS);
    }

    private TableStringThingsLocations(Name alias, TableStringThingsLocations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getLocationId() {
        return locationId;
    }

    @Override
    public TableField<Record, String> getThingId() {
        return thingId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringThingsLocations as(String alias) {
        return new TableStringThingsLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringThingsLocations as(Name alias) {
        return new TableStringThingsLocations(alias, this);
    }

}
