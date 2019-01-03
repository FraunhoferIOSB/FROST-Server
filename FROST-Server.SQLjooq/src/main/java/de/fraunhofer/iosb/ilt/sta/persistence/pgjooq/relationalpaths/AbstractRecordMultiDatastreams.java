package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import java.time.OffsetDateTime;
import org.jooq.Record1;
import org.jooq.Record13;
import org.jooq.Row13;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordMultiDatastreams<J> extends UpdatableRecordImpl<AbstractRecordMultiDatastreams<J>> implements Record13<J, String, String, String, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, J, J, String, Object, String> {

    private static final long serialVersionUID = 265875599;

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.NAME</code>.
     */
    public final void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.NAME</code>.
     */
    public final String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.DESCRIPTION</code>.
     */
    public final void setDescription(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.DESCRIPTION</code>.
     */
    public final String getDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.OBSERVATION_TYPES</code>.
     */
    public final void setObservationTypes(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.OBSERVATION_TYPES</code>.
     */
    public final String getObservationTypes() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final void setPhenomenonTimeStart(OffsetDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final OffsetDateTime getPhenomenonTimeStart() {
        return (OffsetDateTime) get(4);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final void setPhenomenonTimeEnd(OffsetDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final OffsetDateTime getPhenomenonTimeEnd() {
        return (OffsetDateTime) get(5);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final void setResultTimeStart(OffsetDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final OffsetDateTime getResultTimeStart() {
        return (OffsetDateTime) get(6);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final void setResultTimeEnd(OffsetDateTime value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final OffsetDateTime getResultTimeEnd() {
        return (OffsetDateTime) get(7);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final void setSensorId(J value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final J getSensorId() {
        return (J) get(8);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final void setThingId(J value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final J getThingId() {
        return (J) get(9);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.UNIT_OF_MEASUREMENTS</code>.
     */
    public final void setUnitOfMeasurements(String value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.UNIT_OF_MEASUREMENTS</code>.
     */
    public final String getUnitOfMeasurements() {
        return (String) get(10);
    }

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    public final void setObservedArea(Object value) {
        set(11, value);
    }

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    public final Object getObservedArea() {
        return get(11);
    }

    /**
     * Setter for <code>public.MULTI_DATASTREAMS.PROPERTIES</code>.
     */
    public final void setProperties(String value) {
        set(12, value);
    }

    /**
     * Getter for <code>public.MULTI_DATASTREAMS.PROPERTIES</code>.
     */
    public final String getProperties() {
        return (String) get(12);
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
    // Record13 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row13<J, String, String, String, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, J, J, String, Object, String> fieldsRow() {
        return (Row13) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row13<J, String, String, String, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, J, J, String, Object, String> valuesRow() {
        return (Row13) super.valuesRow();
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
    public final String component2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component4() {
        return getObservationTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component5() {
        return getPhenomenonTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component6() {
        return getPhenomenonTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component7() {
        return getResultTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component8() {
        return getResultTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component9() {
        return getSensorId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component10() {
        return getThingId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component11() {
        return getUnitOfMeasurements();
    }

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    @Override
    public final Object component12() {
        return getObservedArea();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component13() {
        return getProperties();
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
    public final String value2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value4() {
        return getObservationTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value5() {
        return getPhenomenonTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value6() {
        return getPhenomenonTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value7() {
        return getResultTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value8() {
        return getResultTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value9() {
        return getSensorId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value10() {
        return getThingId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value11() {
        return getUnitOfMeasurements();
    }

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    @Override
    public final Object value12() {
        return getObservedArea();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value13() {
        return getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value3(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value4(String value) {
        setObservationTypes(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value5(OffsetDateTime value) {
        setPhenomenonTimeStart(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value6(OffsetDateTime value) {
        setPhenomenonTimeEnd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value7(OffsetDateTime value) {
        setResultTimeStart(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value8(OffsetDateTime value) {
        setResultTimeEnd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value9(J value) {
        setSensorId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value10(J value) {
        setThingId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value11(String value) {
        setUnitOfMeasurements(value);
        return this;
    }

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    @Override
    public final AbstractRecordMultiDatastreams value12(Object value) {
        setObservedArea(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams value13(String value) {
        setProperties(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordMultiDatastreams values(J value1, String value2, String value3, String value4, OffsetDateTime value5, OffsetDateTime value6, OffsetDateTime value7, OffsetDateTime value8, J value9, J value10, String value11, Object value12, String value13) {
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
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached MultiDatastreamsRecord
     */
    public AbstractRecordMultiDatastreams(AbstractTableMultiDatastreams<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised MultiDatastreamsRecord
     */
    public AbstractRecordMultiDatastreams(AbstractTableMultiDatastreams<J> table, J id, String name, String description, String observationTypes, OffsetDateTime phenomenonTimeStart, OffsetDateTime phenomenonTimeEnd, OffsetDateTime resultTimeStart, OffsetDateTime resultTimeEnd, J sensorId, J thingId, String unitOfMeasurements, Object observedArea, String properties) {
        super(table);

        set(0, id);
        set(1, name);
        set(2, description);
        set(3, observationTypes);
        set(4, phenomenonTimeStart);
        set(5, phenomenonTimeEnd);
        set(6, resultTimeStart);
        set(7, resultTimeEnd);
        set(8, sensorId);
        set(9, thingId);
        set(10, unitOfMeasurements);
        set(11, observedArea);
        set(12, properties);
    }
}
