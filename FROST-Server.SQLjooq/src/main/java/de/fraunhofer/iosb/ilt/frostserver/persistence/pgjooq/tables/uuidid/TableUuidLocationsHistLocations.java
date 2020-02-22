package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocationsHistLocations;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidLocationsHistLocations extends AbstractTableLocationsHistLocations<UUID> {

    private static final long serialVersionUID = -1022733888;

    /**
     * The reference instance of <code>public.LOCATIONS_HIST_LOCATIONS</code>
     */
    public static final TableUuidLocationsHistLocations LOCATIONS_HIST_LOCATIONS = new TableUuidLocationsHistLocations();

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, UUID> locationId = createField(DSL.name("LOCATION_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.HIST_LOCATION_ID</code>.
     */
    public final TableField<Record, UUID> histLocationId = createField(DSL.name("HIST_LOCATION_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * Create a <code>public.LOCATIONS_HIST_LOCATIONS</code> table reference
     */
    public TableUuidLocationsHistLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.LOCATIONS_HIST_LOCATIONS</code> table
     * reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidLocationsHistLocations(Name alias) {
        this(alias, LOCATIONS_HIST_LOCATIONS);
    }

    private TableUuidLocationsHistLocations(Name alias, TableUuidLocationsHistLocations aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getLocationId() {
        return locationId;
    }

    @Override
    public TableField<Record, UUID> getHistLocationId() {
        return histLocationId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidLocationsHistLocations as(String alias) {
        return new TableUuidLocationsHistLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidLocationsHistLocations as(Name alias) {
        return new TableUuidLocationsHistLocations(alias, this);
    }

}
