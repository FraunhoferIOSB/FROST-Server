package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordThings<J> extends UpdatableRecordImpl<AbstractRecordThings<J>> implements Record4<J, String, String, String> {

    private static final long serialVersionUID = 1006050775;

    /**
     * Setter for <code>public.THINGS.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.THINGS.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.THINGS.DESCRIPTION</code>.
     */
    public final void setDescription(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.THINGS.DESCRIPTION</code>.
     */
    public final String getDescription() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.THINGS.PROPERTIES</code>.
     */
    public final void setProperties(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.THINGS.PROPERTIES</code>.
     */
    public final String getProperties() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.THINGS.NAME</code>.
     */
    public final void setName(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.THINGS.NAME</code>.
     */
    public final String getName() {
        return (String) get(3);
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
    // Record4 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row4<J, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row4<J, String, String, String> valuesRow() {
        return (Row4) super.valuesRow();
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
        return getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component4() {
        return getName();
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
        return getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value4() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordThings value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordThings value2(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordThings value3(String value) {
        setProperties(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordThings value4(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordThings values(J value1, String value2, String value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached ThingsRecord
     */
    public AbstractRecordThings(AbstractTableThings<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised ThingsRecord
     */
    public AbstractRecordThings(AbstractTableThings<J> table, J id, String description, Object properties, String name) {
        super(table);

        set(0, id);
        set(1, description);
        set(2, properties);
        set(3, name);
    }
}
