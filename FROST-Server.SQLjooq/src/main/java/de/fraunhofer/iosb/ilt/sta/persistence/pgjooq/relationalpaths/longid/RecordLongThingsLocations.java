package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import org.jooq.Field;

public class RecordLongThingsLocations extends AbstractRecordThingsLocations<Long> {

    private static final long serialVersionUID = 64948310;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongThingsLocations.THINGS_LOCATIONS.THING_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return TableLongThingsLocations.THINGS_LOCATIONS.LOCATION_ID;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached ThingsLocationsRecord
     */
    public RecordLongThingsLocations() {
        super(TableLongThingsLocations.THINGS_LOCATIONS);
    }

    /**
     * Create a detached, initialised ThingsLocationsRecord
     */
    public RecordLongThingsLocations(Long thingId, Long locationId) {
        super(TableLongThingsLocations.THINGS_LOCATIONS);

        set(0, thingId);
        set(1, locationId);
    }
}
