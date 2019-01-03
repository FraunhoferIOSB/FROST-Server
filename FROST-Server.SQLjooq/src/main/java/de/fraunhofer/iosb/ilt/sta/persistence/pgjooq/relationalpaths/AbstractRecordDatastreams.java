package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import java.time.OffsetDateTime;
import org.jooq.Record1;
import org.jooq.Record16;
import org.jooq.Row16;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordDatastreams<J> extends UpdatableRecordImpl<AbstractRecordDatastreams<J>> implements Record16<J, String, String, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, J, J, J, String, String, String, String, Object, String> {

    private static final long serialVersionUID = 230011824;

    /**
     * Setter for <code>public.DATASTREAMS.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.DATASTREAMS.DESCRIPTION</code>.
     */
    public final void setDescription(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.DESCRIPTION</code>.
     */
    public final String getDescription() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.DATASTREAMS.OBSERVATION_TYPE</code>.
     */
    public final void setObservationType(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.OBSERVATION_TYPE</code>.
     */
    public final String getObservationType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final void setPhenomenonTimeStart(OffsetDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.PHENOMENON_TIME_START</code>.
     */
    public final OffsetDateTime getPhenomenonTimeStart() {
        return (OffsetDateTime) get(3);
    }

    /**
     * Setter for <code>public.DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final void setPhenomenonTimeEnd(OffsetDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.PHENOMENON_TIME_END</code>.
     */
    public final OffsetDateTime getPhenomenonTimeEnd() {
        return (OffsetDateTime) get(4);
    }

    /**
     * Setter for <code>public.DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final void setResultTimeStart(OffsetDateTime value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.RESULT_TIME_START</code>.
     */
    public final OffsetDateTime getResultTimeStart() {
        return (OffsetDateTime) get(5);
    }

    /**
     * Setter for <code>public.DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final void setResultTimeEnd(OffsetDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.RESULT_TIME_END</code>.
     */
    public final OffsetDateTime getResultTimeEnd() {
        return (OffsetDateTime) get(6);
    }

    /**
     * Setter for <code>public.DATASTREAMS.SENSOR_ID</code>.
     */
    public final void setSensorId(J value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.SENSOR_ID</code>.
     */
    public final J getSensorId() {
        return (J) get(7);
    }

    /**
     * Setter for <code>public.DATASTREAMS.OBS_PROPERTY_ID</code>.
     */
    public final void setObsPropertyId(J value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.OBS_PROPERTY_ID</code>.
     */
    public final J getObsPropertyId() {
        return (J) get(8);
    }

    /**
     * Setter for <code>public.DATASTREAMS.THING_ID</code>.
     */
    public final void setThingId(J value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.THING_ID</code>.
     */
    public final J getThingId() {
        return (J) get(9);
    }

    /**
     * Setter for <code>public.DATASTREAMS.UNIT_NAME</code>.
     */
    public final void setUnitName(String value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.UNIT_NAME</code>.
     */
    public final String getUnitName() {
        return (String) get(10);
    }

    /**
     * Setter for <code>public.DATASTREAMS.UNIT_SYMBOL</code>.
     */
    public final void setUnitSymbol(String value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.UNIT_SYMBOL</code>.
     */
    public final String getUnitSymbol() {
        return (String) get(11);
    }

    /**
     * Setter for <code>public.DATASTREAMS.UNIT_DEFINITION</code>.
     */
    public final void setUnitDefinition(String value) {
        set(12, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.UNIT_DEFINITION</code>.
     */
    public final String getUnitDefinition() {
        return (String) get(12);
    }

    /**
     * Setter for <code>public.DATASTREAMS.NAME</code>.
     */
    public final void setName(String value) {
        set(13, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.NAME</code>.
     */
    public final String getName() {
        return (String) get(13);
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
        set(14, value);
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
        return get(14);
    }

    /**
     * Setter for <code>public.DATASTREAMS.PROPERTIES</code>.
     */
    public final void setProperties(String value) {
        set(15, value);
    }

    /**
     * Getter for <code>public.DATASTREAMS.PROPERTIES</code>.
     */
    public final String getProperties() {
        return (String) get(15);
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
    public final Row16<J, String, String, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, J, J, J, String, String, String, String, Object, String> fieldsRow() {
        return (Row16) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row16<J, String, String, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, J, J, J, String, String, String, String, Object, String> valuesRow() {
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
    public final String component2() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component3() {
        return getObservationType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component4() {
        return getPhenomenonTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component5() {
        return getPhenomenonTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component6() {
        return getResultTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime component7() {
        return getResultTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component8() {
        return getSensorId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component9() {
        return getObsPropertyId();
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
        return getUnitName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component12() {
        return getUnitSymbol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component13() {
        return getUnitDefinition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component14() {
        return getName();
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
    public final Object component15() {
        return getObservedArea();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component16() {
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
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value3() {
        return getObservationType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value4() {
        return getPhenomenonTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value5() {
        return getPhenomenonTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value6() {
        return getResultTimeStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OffsetDateTime value7() {
        return getResultTimeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value8() {
        return getSensorId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value9() {
        return getObsPropertyId();
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
        return getUnitName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value12() {
        return getUnitSymbol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value13() {
        return getUnitDefinition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value14() {
        return getName();
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
    public final Object value15() {
        return getObservedArea();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value16() {
        return getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value2(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value3(String value) {
        setObservationType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value4(OffsetDateTime value) {
        setPhenomenonTimeStart(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value5(OffsetDateTime value) {
        setPhenomenonTimeEnd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value6(OffsetDateTime value) {
        setResultTimeStart(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value7(OffsetDateTime value) {
        setResultTimeEnd(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value8(J value) {
        setSensorId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value9(J value) {
        setObsPropertyId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value10(J value) {
        setThingId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value11(String value) {
        setUnitName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value12(String value) {
        setUnitSymbol(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value13(String value) {
        setUnitDefinition(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value14(String value) {
        setName(value);
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
    public final AbstractRecordDatastreams<J> value15(Object value) {
        setObservedArea(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> value16(String value) {
        setProperties(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordDatastreams<J> values(J value1, String value2, String value3, OffsetDateTime value4, OffsetDateTime value5, OffsetDateTime value6, OffsetDateTime value7, J value8, J value9, J value10, String value11, String value12, String value13, String value14, Object value15, String value16) {
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
     * Create a detached DatastreamsRecord
     *
     * @param table The exact table implementation.
     */
    public AbstractRecordDatastreams(AbstractTableDatastreams<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised DatastreamsRecord
     */
    public AbstractRecordDatastreams(AbstractTableDatastreams<J> table, J id, String description, String observationType, OffsetDateTime phenomenonTimeStart, OffsetDateTime phenomenonTimeEnd, OffsetDateTime resultTimeStart, OffsetDateTime resultTimeEnd, J sensorId, J obsPropertyId, J thingId, String unitName, String unitSymbol, String unitDefinition, String name, Object observedArea, String properties) {
        super(table);

        set(0, id);
        set(1, description);
        set(2, observationType);
        set(3, phenomenonTimeStart);
        set(4, phenomenonTimeEnd);
        set(5, resultTimeStart);
        set(6, resultTimeEnd);
        set(7, sensorId);
        set(8, obsPropertyId);
        set(9, thingId);
        set(10, unitName);
        set(11, unitSymbol);
        set(12, unitDefinition);
        set(13, name);
        set(14, observedArea);
        set(15, properties);
    }
}
