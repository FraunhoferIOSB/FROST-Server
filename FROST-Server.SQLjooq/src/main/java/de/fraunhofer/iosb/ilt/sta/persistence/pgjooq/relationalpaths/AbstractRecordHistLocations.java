package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import java.time.OffsetDateTime;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordHistLocations<J> extends UpdatableRecordImpl<AbstractRecordHistLocations<J>> implements Record3<J, OffsetDateTime, J> {

    private static final long serialVersionUID = 1511620203;

    /**
     * Setter for <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.HIST_LOCATIONS.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.HIST_LOCATIONS.TIME</code>.
     */
    public final void setTime(OffsetDateTime value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.HIST_LOCATIONS.TIME</code>.
     */
    public final OffsetDateTime getTime() {
        return (OffsetDateTime) get(1);
    }

    /**
     * Setter for <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final void setThingId(J value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.HIST_LOCATIONS.THING_ID</code>.
     */
    public final J getThingId() {
        return (J) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Record1<J> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row3<J, OffsetDateTime, J> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row3<J, OffsetDateTime, J> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component2() {
        return getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component3() {
        return getThingId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value2() {
        return getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value3() {
        return getThingId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordHistLocations value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordHistLocations value2(OffsetDateTime value) {
        setTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordHistLocations value3(J value) {
        setThingId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordHistLocations values(J value1, OffsetDateTime value2, J value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached HistLocationsRecord
     */
    public AbstractRecordHistLocations(AbstractTableHistLocations<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised HistLocationsRecord
     */
    public AbstractRecordHistLocations(AbstractTableHistLocations<J> table, J id, OffsetDateTime time, J thingId) {
        super(table);

        set(0, id);
        set(1, time);
        set(2, thingId);
    }
}
