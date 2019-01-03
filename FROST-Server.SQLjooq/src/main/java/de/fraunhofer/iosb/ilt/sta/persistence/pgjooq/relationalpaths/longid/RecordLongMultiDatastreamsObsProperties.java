package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import org.jooq.Field;

public class RecordLongMultiDatastreamsObsProperties extends AbstractRecordMultiDatastreamsObsProperties<Long> {

    private static final long serialVersionUID = -372181033;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongMultiDatastreamsObsProperties.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return TableLongMultiDatastreamsObsProperties.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return TableLongMultiDatastreamsObsProperties.MULTI_DATASTREAMS_OBS_PROPERTIES.rank;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached MultiDatastreamsObsPropertiesRecord
     */
    public RecordLongMultiDatastreamsObsProperties() {
        super(TableLongMultiDatastreamsObsProperties.MULTI_DATASTREAMS_OBS_PROPERTIES);
    }

    /**
     * Create a detached, initialised MultiDatastreamsObsPropertiesRecord
     */
    public RecordLongMultiDatastreamsObsProperties(Long multiDatastreamId, Long obsPropertyId, Integer rank) {
        super(TableLongMultiDatastreamsObsProperties.MULTI_DATASTREAMS_OBS_PROPERTIES);

        set(0, multiDatastreamId);
        set(1, obsPropertyId);
        set(2, rank);
    }
}
