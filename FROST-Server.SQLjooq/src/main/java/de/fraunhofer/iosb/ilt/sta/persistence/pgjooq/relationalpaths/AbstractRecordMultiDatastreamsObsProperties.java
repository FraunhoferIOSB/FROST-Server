package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordMultiDatastreamsObsProperties<J> extends UpdatableRecordImpl<AbstractRecordMultiDatastreamsObsProperties<J>> implements Record3<J, J, Integer> {

    private static final long serialVersionUID = -372181033;

    /**
     * Setter for
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final void setMultiDatastreamId(J value) {
        set(0, value);
    }

    /**
     * Getter for
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final J getMultiDatastreamId() {
        return (J) get(0);
    }

    /**
     * Setter for
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final void setObsPropertyId(J value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final J getObsPropertyId() {
        return (J) get(1);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.RANK</code>.
     */
    public final void setRank(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.RANK</code>.
     */
    public final Integer getRank() {
        return (Integer) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Record3<J, J, Integer> key() {
        return (Record3) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row3<J, J, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row3<J, J, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component1() {
        return getMultiDatastreamId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component2() {
        return getObsPropertyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer component3() {
        return getRank();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value1() {
        return getMultiDatastreamId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value2() {
        return getObsPropertyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer value3() {
        return getRank();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreamsObsProperties value1(J value) {
        setMultiDatastreamId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreamsObsProperties value2(J value) {
        setObsPropertyId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreamsObsProperties value3(Integer value) {
        setRank(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreamsObsProperties values(J value1, J value2, Integer value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached MultiDatastreamsObsPropertiesRecord
     */
    public AbstractRecordMultiDatastreamsObsProperties(AbstractTableMultiDatastreamsObsProperties<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised MultiDatastreamsObsPropertiesRecord
     */
    public AbstractRecordMultiDatastreamsObsProperties(AbstractTableMultiDatastreamsObsProperties<J> table, J multiDatastreamId, J obsPropertyId, Integer rank) {
        super(table);

        set(0, multiDatastreamId);
        set(1, obsPropertyId);
        set(2, rank);
    }
}
