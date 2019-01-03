package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordSensors<J> extends UpdatableRecordImpl<AbstractRecordSensors<J>> implements Record6<J, String, String, String, String, String> {

    private static final long serialVersionUID = -1389387958;

    /**
     * Setter for <code>public.SENSORS.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.SENSORS.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.SENSORS.DESCRIPTION</code>.
     */
    public final void setDescription(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.SENSORS.DESCRIPTION</code>.
     */
    public final String getDescription() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.SENSORS.ENCODING_TYPE</code>.
     */
    public final void setEncodingType(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.SENSORS.ENCODING_TYPE</code>.
     */
    public final String getEncodingType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.SENSORS.METADATA</code>.
     */
    public final void setMetadata(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.SENSORS.METADATA</code>.
     */
    public final String getMetadata() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.SENSORS.NAME</code>.
     */
    public final void setName(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.SENSORS.NAME</code>.
     */
    public final String getName() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.SENSORS.PROPERTIES</code>.
     */
    public final void setProperties(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.SENSORS.PROPERTIES</code>.
     */
    public final String getProperties() {
        return (String) get(5);
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
    // Record6 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row6<J, String, String, String, String, String> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row6<J, String, String, String, String, String> valuesRow() {
        return (Row6) super.valuesRow();
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
        return getEncodingType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component4() {
        return getMetadata();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component5() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component6() {
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
        return getEncodingType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value4() {
        return getMetadata();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value5() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value6() {
        return getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordSensors value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordSensors value2(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordSensors value3(String value) {
        setEncodingType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordSensors value4(String value) {
        setMetadata(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordSensors value5(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordSensors value6(String value) {
        setProperties(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordSensors values(J value1, String value2, String value3, String value4, String value5, String value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached SensorsRecord
     */
    public AbstractRecordSensors(AbstractTableSensors<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised SensorsRecord
     */
    public AbstractRecordSensors(AbstractTableSensors<J> table, J id, String description, String encodingType, String metadata, String name, String properties) {
        super(table);

        set(0, id);
        set(1, description);
        set(2, encodingType);
        set(3, metadata);
        set(4, name);
        set(5, properties);
    }
}
