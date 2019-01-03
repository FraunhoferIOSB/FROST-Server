package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordLocations<J> extends UpdatableRecordImpl<AbstractRecordLocations<J>> implements Record8<J, String, String, String, Object, String, J, String> {

    private static final long serialVersionUID = -486223814;

    /**
     * Setter for <code>public.LOCATIONS.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.LOCATIONS.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.LOCATIONS.DESCRIPTION</code>.
     */
    public final void setDescription(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.LOCATIONS.DESCRIPTION</code>.
     */
    public final String getDescription() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.LOCATIONS.ENCODING_TYPE</code>.
     */
    public final void setEncodingType(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.LOCATIONS.ENCODING_TYPE</code>.
     */
    public final String getEncodingType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.LOCATIONS.LOCATION</code>.
     */
    public final void setLocation(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.LOCATIONS.LOCATION</code>.
     */
    public final String getLocation() {
        return (String) get(3);
    }

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    public final void setGeom(Object value) {
        set(4, value);
    }

    /**
     * @deprecated Unknown data type. Please define an explicit
     * {@link org.jooq.Binding} to specify how this type should be handled.
     * Deprecation can be turned off using
     * {@literal <deprecationOnUnknownTypes/>} in your code generator
     * configuration.
     */
    @java.lang.Deprecated
    public final Object getGeom() {
        return get(4);
    }

    /**
     * Setter for <code>public.LOCATIONS.NAME</code>.
     */
    public final void setName(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.LOCATIONS.NAME</code>.
     */
    public final String getName() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final void setGenFoiId(J value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final J getGenFoiId() {
        return (J) get(6);
    }

    /**
     * Setter for <code>public.LOCATIONS.PROPERTIES</code>.
     */
    public final void setProperties(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.LOCATIONS.PROPERTIES</code>.
     */
    public final String getProperties() {
        return (String) get(7);
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
    // Record8 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row8<J, String, String, String, Object, String, J, String> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row8<J, String, String, String, Object, String, J, String> valuesRow() {
        return (Row8) super.valuesRow();
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
        return getLocation();
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
    public final Object component5() {
        return getGeom();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component6() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J component7() {
        return getGenFoiId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String component8() {
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
        return getLocation();
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
    public final Object value5() {
        return getGeom();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value6() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final J value7() {
        return getGenFoiId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String value8() {
        return getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordLocations value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordLocations value2(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordLocations value3(String value) {
        setEncodingType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordLocations value4(String value) {
        setLocation(value);
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
    public final AbstractRecordLocations value5(Object value) {
        setGeom(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordLocations value6(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordLocations value7(J value) {
        setGenFoiId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordLocations value8(String value) {
        setProperties(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordLocations values(J value1, String value2, String value3, String value4, Object value5, String value6, J value7, String value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached LocationsRecord
     */
    public AbstractRecordLocations(AbstractTableLocations<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised LocationsRecord
     */
    public AbstractRecordLocations(AbstractTableLocations<J> table, J id, String description, String encodingType, String location, Object geom, String name, J genFoiId, String properties) {
        super(table);

        set(0, id);
        set(1, description);
        set(2, encodingType);
        set(3, location);
        set(4, geom);
        set(5, name);
        set(6, genFoiId);
        set(7, properties);
    }
}
