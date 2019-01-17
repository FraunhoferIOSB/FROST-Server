package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordFeatures;
import org.geolatte.geom.Geometry;
import org.jooq.Field;

public class RecordLongFeatures extends AbstractRecordFeatures<Long> {

    private static final long serialVersionUID = 888576260;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongFeatures.FEATURES.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return TableLongFeatures.FEATURES.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return TableLongFeatures.FEATURES.encodingType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return TableLongFeatures.FEATURES.feature;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Geometry> field5() {
        return TableLongFeatures.FEATURES.geom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return TableLongFeatures.FEATURES.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return TableLongFeatures.FEATURES.properties;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached FeaturesRecord
     */
    public RecordLongFeatures() {
        super(TableLongFeatures.FEATURES);
    }

    /**
     * Create a detached, initialised FeaturesRecord
     */
    public RecordLongFeatures(Long id, String description, String encodingType, String feature, Object geom, String name, String properties) {
        super(TableLongFeatures.FEATURES);

        set(0, id);
        set(1, description);
        set(2, encodingType);
        set(3, feature);
        set(4, geom);
        set(5, name);
        set(6, properties);
    }
}
