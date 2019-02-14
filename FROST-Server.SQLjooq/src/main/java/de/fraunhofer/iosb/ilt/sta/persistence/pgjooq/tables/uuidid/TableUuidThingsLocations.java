package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThingsLocations;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableUuidThingsLocations extends AbstractTableThingsLocations<UUID> {

    private static final long serialVersionUID = -1443552218;

    /**
     * The reference instance of <code>public.THINGS_LOCATIONS</code>
     */
    public static final TableUuidThingsLocations THINGS_LOCATIONS = new TableUuidThingsLocations();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getLocationId() {
        return locationId;
    }

    @Override
    public TableField<Record, UUID> getThingId() {
        return thingId;
    }

    /**
     * The column <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final TableField<Record, UUID> thingId = createField("THING_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, UUID> locationId = createField("LOCATION_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * Create a <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableUuidThingsLocations() {
        this(DSL.name("THINGS_LOCATIONS"), null);
    }

    /**
     * Create an aliased <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableUuidThingsLocations(String alias) {
        this(DSL.name(alias), THINGS_LOCATIONS);
    }

    /**
     * Create an aliased <code>public.THINGS_LOCATIONS</code> table reference
     */
    public TableUuidThingsLocations(Name alias) {
        this(alias, THINGS_LOCATIONS);
    }

    private TableUuidThingsLocations(Name alias, TableUuidThingsLocations aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidThingsLocations as(String alias) {
        return new TableUuidThingsLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidThingsLocations as(Name alias) {
        return new TableUuidThingsLocations(alias, this);
    }

}
