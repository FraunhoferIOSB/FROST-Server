package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocationsHistLocations;
import org.jooq.Name;
import org.jooq.TableField;

import org.jooq.impl.DSL;

public class TableLongLocationsHistLocations extends AbstractTableLocationsHistLocations<Long> {

    private static final long serialVersionUID = -1022733888;

    /**
     * The reference instance of <code>public.LOCATIONS_HIST_LOCATIONS</code>
     */
    public static final TableLongLocationsHistLocations LOCATIONS_HIST_LOCATIONS = new TableLongLocationsHistLocations();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<RecordLongLocationsHistLocations> getRecordType() {
        return RecordLongLocationsHistLocations.class;
    }

    @Override
    public TableField<AbstractRecordLocationsHistLocations<Long>, Long> getLocationId() {
        return LOCATION_ID;
    }

    @Override
    public TableField<AbstractRecordLocationsHistLocations<Long>, Long> getHistLocationId() {
        return HIST_LOCATION_ID;
    }

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.LOCATION_ID</code>.
     */
    public final TableField<AbstractRecordLocationsHistLocations<Long>, Long> LOCATION_ID = createField("LOCATION_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.LOCATIONS_HIST_LOCATIONS.HIST_LOCATION_ID</code>.
     */
    public final TableField<AbstractRecordLocationsHistLocations<Long>, Long> HIST_LOCATION_ID = createField("HIST_LOCATION_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.LOCATIONS_HIST_LOCATIONS</code> table reference
     */
    public TableLongLocationsHistLocations() {
        super();
    }

    /**
     * Create an aliased <code>public.LOCATIONS_HIST_LOCATIONS</code> table
     * reference
     */
    public TableLongLocationsHistLocations(String alias) {
        this(DSL.name(alias), LOCATIONS_HIST_LOCATIONS);
    }

    /**
     * Create an aliased <code>public.LOCATIONS_HIST_LOCATIONS</code> table
     * reference
     */
    public TableLongLocationsHistLocations(Name alias) {
        this(alias, LOCATIONS_HIST_LOCATIONS);
    }

    private TableLongLocationsHistLocations(Name alias, TableLongLocationsHistLocations aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongLocationsHistLocations as(String alias) {
        return new TableLongLocationsHistLocations(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongLocationsHistLocations as(Name alias) {
        return new TableLongLocationsHistLocations(alias, this);
    }

}
