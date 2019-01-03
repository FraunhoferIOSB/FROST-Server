package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordObsProperties<J> extends UpdatableRecordImpl<AbstractRecordObsProperties<J>> implements Record5<J, String, String, String, String> {

    private static final long serialVersionUID = -1104077494;

    /**
     * Setter for <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.OBS_PROPERTIES.NAME</code>.
     */
    public final void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.OBS_PROPERTIES.NAME</code>.
     */
    public final String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.OBS_PROPERTIES.DEFINITION</code>.
     */
    public final void setDefinition(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.OBS_PROPERTIES.DEFINITION</code>.
     */
    public final String getDefinition() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.OBS_PROPERTIES.DESCRIPTION</code>.
     */
    public final void setDescription(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.OBS_PROPERTIES.DESCRIPTION</code>.
     */
    public final String getDescription() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.OBS_PROPERTIES.PROPERTIES</code>.
     */
    public final void setProperties(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.OBS_PROPERTIES.PROPERTIES</code>.
     */
    public final String getProperties() {
        return (String) get(4);
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
    // Record5 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row5<J, String, String, String, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row5<J, String, String, String, String> valuesRow() {
        return (Row5) super.valuesRow();
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
        return getDefinition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component4() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component5() {
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
        return getDefinition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value4() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value5() {
        return getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObsProperties value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObsProperties value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObsProperties value3(String value) {
        setDefinition(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObsProperties value4(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObsProperties value5(String value) {
        setProperties(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordObsProperties values(J value1, String value2, String value3, String value4, String value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached ObsPropertiesRecord
     */
    public AbstractRecordObsProperties(AbstractTableObsProperties<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised ObsPropertiesRecord
     */
    public AbstractRecordObsProperties(AbstractTableObsProperties<J> table, J id, String name, String definition, String description, String properties) {
        super(table);

        set(0, id);
        set(1, name);
        set(2, definition);
        set(3, description);
        set(4, properties);
    }
}
