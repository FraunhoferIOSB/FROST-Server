package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThingsLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringThingsLocations extends AbstractTableThingsLocations<String> {

    private static final long serialVersionUID = -1443552218;

    /**
     * The reference instance of <code>public.THINGS_LOCATIONS</code>
     */
    public static final TableStringThingsLocations THINGS_LOCATIONS = new TableStringThingsLocations();

    /**
     * @return The class holding records for this type
     */
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
     * The column <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, String> thingId = createField("THING_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, String> locationId = createField("LOCATION_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableStringThingsLocations() {
        this(DSL.name("THINGS_LOCATIONS"), null);
    }

    /**
     * Create an aliased <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableStringThingsLocations(String alias) {
        this(DSL.name(alias), THINGS_LOCATIONS);
    }

    /**
     * Create an aliased <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableStringThingsLocations(Name alias) {
        this(alias, THINGS_LOCATIONS);
    }

    private TableStringThingsLocations(Name alias, TableStringThingsLocations aliased) {
        super(alias, aliased);
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
