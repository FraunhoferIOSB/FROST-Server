package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocationsHistLocations;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringLocationsHistLocations extends AbstractTableLocationsHistLocations<String> {

    private static final long serialVersionUID = -1022733888;

    /**
     * The reference instance of <code>public.LOCATIONS_HIST_LOCATIONS</code>
     */
    public static final TableStringLocationsHistLocations LOCATIONS_HIST_LOCATIONS = new TableStringLocationsHistLocations();

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
    public TableField<Record, String> getHistLocationId() {
        return histLocationId;
    }

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<Record, String> locationId = createField("LOCATION_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.HIST_LOCATION_ID</code>.
     */
    public final TableField<Record, String> histLocationId = createField("HIST_LOCATION_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.LOCATIONS_HIST_LOCATIONS</code> table reference
     */
    public TableStringLocationsHistLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.LOCATIONS_HIST_LOCATIONS</code> table
     * reference
     */
    public TableStringLocationsHistLocations(String alias) {
        this(DSL.name(alias), LOCATIONS_HIST_LOCATIONS);
    }

    /**
     * Create an aliased <code>public.LOCATIONS_HIST_LOCATIONS</code> table
     * reference
     */
    public TableStringLocationsHistLocations(Name alias) {
        this(alias, LOCATIONS_HIST_LOCATIONS);
    }

    private TableStringLocationsHistLocations(Name alias, TableStringLocationsHistLocations aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringLocationsHistLocations as(String alias) {
        return new TableStringLocationsHistLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringLocationsHistLocations as(Name alias) {
        return new TableStringLocationsHistLocations(alias, this);
    }

}
