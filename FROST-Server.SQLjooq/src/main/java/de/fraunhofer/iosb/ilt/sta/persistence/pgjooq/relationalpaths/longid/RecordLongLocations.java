package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import org.jooq.Field;

public class RecordLongLocations extends AbstractRecordLocations<Long> {

    private static final long serialVersionUID = -486223814;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongLocations.LOCATIONS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return TableLongLocations.LOCATIONS.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return TableLongLocations.LOCATIONS.encodingType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return TableLongLocations.LOCATIONS.location;
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
    public Field<Object> field5() {
        return TableLongLocations.LOCATIONS.geom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return TableLongLocations.LOCATIONS.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field7() {
        return TableLongLocations.LOCATIONS.genFoiId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return TableLongLocations.LOCATIONS.properties;
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
    public RecordLongLocations(Long id, String description, String encodingType, String location, Object geom, String name, Long genFoiId, String properties) {
        super(TableLongLocations.LOCATIONS);

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
