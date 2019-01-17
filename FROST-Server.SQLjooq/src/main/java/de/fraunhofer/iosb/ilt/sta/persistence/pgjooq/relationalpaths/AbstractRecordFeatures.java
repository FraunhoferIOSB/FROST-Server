package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import org.geolatte.geom.Geometry;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;

public abstract class AbstractRecordFeatures<J> extends UpdatableRecordImpl<AbstractRecordFeatures<J>> implements Record7<J, String, String, String, Geometry, String, String> {

    private static final long serialVersionUID = 888576260;

    /**
     * Setter for <code>public.FEATURES.ID</code>.
     */
    public final void setId(J value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.FEATURES.ID</code>.
     */
    public final J getId() {
        return (J) get(0);
    }

    /**
     * Setter for <code>public.FEATURES.DESCRIPTION</code>.
     */
    public final void setDescription(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.FEATURES.DESCRIPTION</code>.
     */
    public final String getDescription() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.FEATURES.ENCODING_TYPE</code>.
     */
    public final void setEncodingType(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.FEATURES.ENCODING_TYPE</code>.
     */
    public final String getEncodingType() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.FEATURES.FEATURE</code>.
     */
    public final void setFeature(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.FEATURES.FEATURE</code>.
     */
    public final String getFeature() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.FEATURES.GEOM</code>.
     */
    public final void setGeom(Geometry value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.FEATURES.GEOM</code>.
     */
    public final Geometry getGeom() {
        return (Geometry) get(4);
    }

    /**
     * Setter for <code>public.FEATURES.NAME</code>.
     */
    public final void setName(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.FEATURES.NAME</code>.
     */
    public final String getName() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.FEATURES.PROPERTIES</code>.
     */
    public final void setProperties(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.FEATURES.PROPERTIES</code>.
     */
    public final String getProperties() {
        return (String) get(6);
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
    // Record7 type implementation
    // -------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public final Row7<J, String, String, String, Geometry, String, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Row7<J, String, String, String, Geometry, String, String> valuesRow() {
        return (Row7) super.valuesRow();
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
        return getFeature();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Geometry component5() {
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
    public final String component7() {
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
        return getFeature();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Geometry value5() {
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
    public final String value7() {
        return getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordFeatures value1(J value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordFeatures value2(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordFeatures value3(String value) {
        setEncodingType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordFeatures value4(String value) {
        setFeature(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordFeatures value5(Geometry value) {
        setGeom(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordFeatures value6(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordFeatures value7(String value) {
        setProperties(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AbstractRecordFeatures values(J value1, String value2, String value3, String value4, Geometry value5, String value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached FeaturesRecord
     */
    public AbstractRecordFeatures(AbstractTableFeatures<J> table) {
        super(table);
    }

    /**
     * Create a detached, initialised FeaturesRecord
     */
    public AbstractRecordFeatures(AbstractTableFeatures<J> table, J id, String description, String encodingType, String feature, Geometry geom, String name, String properties) {
        super(table);

        set(0, id);
        set(1, description);
        set(2, encodingType);
        set(3, feature);
        set(4, geom);
        set(5, name);
        set(6, properties);
    }
}
