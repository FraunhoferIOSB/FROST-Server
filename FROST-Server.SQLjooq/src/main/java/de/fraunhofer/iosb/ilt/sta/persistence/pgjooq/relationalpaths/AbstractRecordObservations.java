package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import java.time.OffsetDateTime;
import org.jooq.Record1;
import org.jooq.Record16;
import org.jooq.Row16;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordObservations<J> extends UpdatableRecordImpl<AbstractRecordObservations<J>> implements Record16<J, OffsetDateTime, OffsetDateTime, OffsetDateTime, Double, String, String, OffsetDateTime, OffsetDateTime, String, J, J, Short, String, Boolean, J> {

    private static final long serialVersionUID = -1063247356;

    /**
     * Setter for <code>public.OBSERVATIONS.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.PHENOMENON_TIME_START</code>.
     */
    public final void setPhenomenonTimeStart(OffsetDateTime value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.PHENOMENON_TIME_START</code>.
     */
    public final OffsetDateTime getPhenomenonTimeStart() {
        return (OffsetDateTime) get(1);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.PHENOMENON_TIME_END</code>.
     */
    public final void setPhenomenonTimeEnd(OffsetDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.PHENOMENON_TIME_END</code>.
     */
    public final OffsetDateTime getPhenomenonTimeEnd() {
        return (OffsetDateTime) get(2);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.RESULT_TIME</code>.
     */
    public final void setResultTime(OffsetDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.RESULT_TIME</code>.
     */
    public final OffsetDateTime getResultTime() {
        return (OffsetDateTime) get(3);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.RESULT_NUMBER</code>.
     */
    public final void setResultNumber(Double value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.RESULT_NUMBER</code>.
     */
    public final Double getResultNumber() {
        return (Double) get(4);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.RESULT_STRING</code>.
     */
    public final void setResultString(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.RESULT_STRING</code>.
     */
    public final String getResultString() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.RESULT_QUALITY</code>.
     */
    public final void setResultQuality(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.RESULT_QUALITY</code>.
     */
    public final String getResultQuality() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.VALID_TIME_START</code>.
     */
    public final void setValidTimeStart(OffsetDateTime value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.VALID_TIME_START</code>.
     */
    public final OffsetDateTime getValidTimeStart() {
        return (OffsetDateTime) get(7);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.VALID_TIME_END</code>.
     */
    public final void setValidTimeEnd(OffsetDateTime value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.VALID_TIME_END</code>.
     */
    public final OffsetDateTime getValidTimeEnd() {
        return (OffsetDateTime) get(8);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.PARAMETERS</code>.
     */
    public final void setParameters(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.PARAMETERS</code>.
     */
    public final String getParameters() {
        return (String) get(9);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final void setDatastreamId(J value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.DATASTREAM_ID</code>.
     */
    public final J getDatastreamId() {
        return (J) get(10);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final void setFeatureId(J value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.FEATURE_ID</code>.
     */
    public final J getFeatureId() {
        return (J) get(11);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.RESULT_TYPE</code>.
     */
    public final void setResultType(Short value) {
        set(12, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.RESULT_TYPE</code>.
     */
    public final Short getResultType() {
        return (Short) get(12);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.RESULT_JSON</code>.
     */
    public final void setResultJson(String value) {
        set(13, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.RESULT_JSON</code>.
     */
    public final String getResultJson() {
        return (String) get(13);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.RESULT_BOOLEAN</code>.
     */
    public final void setResultBoolean(Boolean value) {
        set(14, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.RESULT_BOOLEAN</code>.
     */
    public final Boolean getResultBoolean() {
        return (Boolean) get(14);
    }

    /**
     * Setter for <code>public.OBSERVATIONS.MULTI_DATASTREAM_ID</code>.
     */
    public final void setMultiDatastreamId(J value) {
        set(15, value);
    }

    /**
     * Getter for <code>public.OBSERVATIONS.MULTI_DATASTREAM_ID</code>.
     */
    public final J getMultiDatastreamId() {
        return (J) get(15);
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
    // Record16 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row16<J, OffsetDateTime, OffsetDateTime, OffsetDateTime, Double, String, String, OffsetDateTime, OffsetDateTime, String, J, J, Short, String, Boolean, J> fieldsRow() {
        return (Row16) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row16<J, OffsetDateTime, OffsetDateTime, OffsetDateTime, Double, String, String, OffsetDateTime, OffsetDateTime, String, J, J, Short, String, Boolean, J> valuesRow() {
        return (Row16) super.valuesRow();
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
        return getPhenomenonTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component3() {
        return getPhenomenonTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component4() {
        return getResultTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Double component5() {
        return getResultNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component6() {
        return getResultString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component7() {
        return getResultQuality();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component8() {
        return getValidTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component9() {
        return getValidTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component10() {
        return getParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component11() {
        return getDatastreamId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component12() {
        return getFeatureId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Short component13() {
        return getResultType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component14() {
        return getResultJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Boolean component15() {
        return getResultBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component16() {
        return getMultiDatastreamId();
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
        return getPhenomenonTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value3() {
        return getPhenomenonTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value4() {
        return getResultTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Double value5() {
        return getResultNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value6() {
        return getResultString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value7() {
        return getResultQuality();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value8() {
        return getValidTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value9() {
        return getValidTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value10() {
        return getParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value11() {
        return getDatastreamId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value12() {
        return getFeatureId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Short value13() {
        return getResultType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value14() {
        return getResultJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Boolean value15() {
        return getResultBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value16() {
        return getMultiDatastreamId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value2(OffsetDateTime value) {
        setPhenomenonTimeStart(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value3(OffsetDateTime value) {
        setPhenomenonTimeEnd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value4(OffsetDateTime value) {
        setResultTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value5(Double value) {
        setResultNumber(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value6(String value) {
        setResultString(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value7(String value) {
        setResultQuality(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value8(OffsetDateTime value) {
        setValidTimeStart(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value9(OffsetDateTime value) {
        setValidTimeEnd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value10(String value) {
        setParameters(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value11(J value) {
        setDatastreamId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value12(J value) {
        setFeatureId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value13(Short value) {
        setResultType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value14(String value) {
        setResultJson(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value15(Boolean value) {
        setResultBoolean(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations value16(J value) {
        setMultiDatastreamId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObservations values(J value1, OffsetDateTime value2, OffsetDateTime value3, OffsetDateTime value4, Double value5, String value6, String value7, OffsetDateTime value8, OffsetDateTime value9, String value10, J value11, J value12, Short value13, String value14, Boolean value15, J value16) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        value12(value12);
        value13(value13);
        value14(value14);
        value15(value15);
        value16(value16);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached ObservationsRecord
     */
    public AbstractRecordObservations(AbstractTableObservations<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised ObservationsRecord
     */
    public AbstractRecordObservations(AbstractTableObservations<J> table, J id, OffsetDateTime phenomenonTimeStart, OffsetDateTime phenomenonTimeEnd, OffsetDateTime resultTime, Double resultNumber, String resultString, String resultQuality, OffsetDateTime validTimeStart, OffsetDateTime validTimeEnd, String parameters, J datastreamId, J featureId, Short resultType, String resultJson, Boolean resultBoolean, J multiDatastreamId) {
        super(table);

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
