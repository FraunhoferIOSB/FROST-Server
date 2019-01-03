package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import java.time.OffsetDateTime;
import org.jooq.Field;

public class RecordLongObservations extends AbstractRecordObservations<Long> {

    private static final long serialVersionUID = -1063247356;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongObservations.OBSERVATIONS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field2() {
        return TableLongObservations.OBSERVATIONS.phenomenonTimeStart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field3() {
        return TableLongObservations.OBSERVATIONS.phenomenonTimeEnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field4() {
        return TableLongObservations.OBSERVATIONS.resultTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field5() {
        return TableLongObservations.OBSERVATIONS.resultNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return TableLongObservations.OBSERVATIONS.resultString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return TableLongObservations.OBSERVATIONS.resultQuality;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field8() {
        return TableLongObservations.OBSERVATIONS.validTimeStart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field9() {
        return TableLongObservations.OBSERVATIONS.validTimeEnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field10() {
        return TableLongObservations.OBSERVATIONS.parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field11() {
        return TableLongObservations.OBSERVATIONS.DATASTREAM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field12() {
        return TableLongObservations.OBSERVATIONS.FEATURE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Short> field13() {
        return TableLongObservations.OBSERVATIONS.resultType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field14() {
        return TableLongObservations.OBSERVATIONS.resultJson;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field15() {
        return TableLongObservations.OBSERVATIONS.resultBoolean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field16() {
        return TableLongObservations.OBSERVATIONS.MULTI_DATASTREAM_ID;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached ObservationsRecord
     */
    public RecordLongObservations() {
        super(TableLongObservations.OBSERVATIONS);
    }

    /**
     * Create a detached, initialised ObservationsRecord
     */
    public RecordLongObservations(Long id, OffsetDateTime phenomenonTimeStart, OffsetDateTime phenomenonTimeEnd, OffsetDateTime resultTime, Double resultNumber, String resultString, String resultQuality, OffsetDateTime validTimeStart, OffsetDateTime validTimeEnd, String parameters, Long datastreamId, Long featureId, Short resultType, String resultJson, Boolean resultBoolean, Long multiDatastreamId) {
        super(TableLongObservations.OBSERVATIONS);

        set(0, id);
        set(1, phenomenonTimeStart);
        set(2, phenomenonTimeEnd);
        set(3, resultTime);
        set(4, resultNumber);
        set(5, resultString);
        set(6, resultQuality);
        set(7, validTimeStart);
        set(8, validTimeEnd);
        set(9, parameters);
        set(10, datastreamId);
        set(11, featureId);
        set(12, resultType);
        set(13, resultJson);
        set(14, resultBoolean);
        set(15, multiDatastreamId);
    }
}
