package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import java.time.OffsetDateTime;
import org.jooq.Field;

public class RecordLongHistLocations extends AbstractRecordHistLocations<Long> {

    private static final long serialVersionUID = 1511620203;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongHistLocations.HIST_LOCATIONS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field2() {
        return TableLongHistLocations.HIST_LOCATIONS.time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return TableLongHistLocations.HIST_LOCATIONS.THING_ID;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached HistLocationsRecord
     */
    public RecordLongHistLocations() {
        super(TableLongHistLocations.HIST_LOCATIONS);
    }

    /**
     * Create a detached, initialised HistLocationsRecord
     */
    public RecordLongHistLocations(Long id, OffsetDateTime time, Long thingId) {
        super(TableLongHistLocations.HIST_LOCATIONS);

        set(0, id);
        set(1, time);
        set(2, thingId);
    }
}
