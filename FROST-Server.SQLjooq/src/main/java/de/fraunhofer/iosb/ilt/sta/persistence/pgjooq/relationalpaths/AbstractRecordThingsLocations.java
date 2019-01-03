package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordThingsLocations<J> extends UpdatableRecordImpl<AbstractRecordThingsLocations<J>> implements Record2<J, J> {

    private static final long serialVersionUID = 64948310;

    /**
     * Setter for <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final void setThingId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.THINGS_LOCATIONS.THING_ID</code>.
     */
    public final J getThingId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final void setLocationId(J value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.THINGS_LOCATIONS.LOCATION_ID</code>.
     */
    public final J getLocationId() {
        return (J) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Record2<J, J> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row2<J, J> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row2<J, J> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component1() {
        return getThingId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component2() {
        return getLocationId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value1() {
        return getThingId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value2() {
        return getLocationId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordThingsLocations value1(J value) {
        setThingId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordThingsLocations value2(J value) {
        setLocationId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordThingsLocations values(J value1, J value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached ThingsLocationsRecord
     */
    public AbstractRecordThingsLocations(AbstractTableThingsLocations<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised ThingsLocationsRecord
     */
    public AbstractRecordThingsLocations(AbstractTableThingsLocations<J> table, J thingId, J locationId) {
        super(table);

        set(0, thingId);
        set(1, locationId);
    }
}
