package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import org.jooq.Field;

public class RecordLongLocationsHistLocations extends AbstractRecordLocationsHistLocations<Long> {

    private static final long serialVersionUID = -1432180691;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongLocationsHistLocations.LOCATIONS_HIST_LOCATIONS.LOCATION_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return TableLongLocationsHistLocations.LOCATIONS_HIST_LOCATIONS.HIST_LOCATION_ID;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached LocationsHistLocationsRecord
     */
    public RecordLongLocationsHistLocations() {
        super(TableLongLocationsHistLocations.LOCATIONS_HIST_LOCATIONS);
    }

    /**
     * Create a detached, initialised LocationsHistLocationsRecord
     */
    public RecordLongLocationsHistLocations(Long locationId, Long histLocationId) {
        super(TableLongLocationsHistLocations.LOCATIONS_HIST_LOCATIONS);

        set(0, locationId);
        set(1, histLocationId);
    }
}
