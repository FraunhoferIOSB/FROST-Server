package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import org.geolatte.geom.Geometry;
import org.jooq.Field;

public class RecordLongLocations extends AbstractRecordLocations<Long> {

    private static final long serialVersionUID = -486223814;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return TableLongLocations.LOCATIONS.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return TableLongLocations.LOCATIONS.encodingType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return TableLongLocations.LOCATIONS.location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Geometry> field4() {
        return TableLongLocations.LOCATIONS.geom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return TableLongLocations.LOCATIONS.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return TableLongLocations.LOCATIONS.properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field7() {
        return TableLongLocations.LOCATIONS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field8() {
        return TableLongLocations.LOCATIONS.genFoiId;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached LocationsRecord
     */
    public RecordLongLocations() {
        super(TableLongLocations.LOCATIONS);
    }

    /**
     * Create a detached, initialised LocationsRecord
     */
    public RecordLongLocations(String description, String encodingType, String location, Geometry geom, String name, String properties, Long id, Long genFoiId) {
        super(TableLongLocations.LOCATIONS);

        set(0, description);
        set(1, encodingType);
        set(2, location);
        set(3, geom);
        set(4, name);
        set(5, properties);
        set(6, id);
        set(7, genFoiId);
    }
}
