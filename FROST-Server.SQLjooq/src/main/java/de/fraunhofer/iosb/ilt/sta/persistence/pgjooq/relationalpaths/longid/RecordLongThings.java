package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import org.jooq.Field;

public class RecordLongThings extends AbstractRecordThings<Long> {

    private static final long serialVersionUID = 1006050775;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongThings.THINGS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return TableLongThings.THINGS.description;
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
    public Field<String> field3() {
        return TableLongThings.THINGS.properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return TableLongThings.THINGS.name;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached ThingsRecord
     */
    public RecordLongThings() {
        super(TableLongThings.THINGS);
    }

    /**
     * Create a detached, initialised ThingsRecord
     */
    public RecordLongThings(Long id, String description, Object properties, String name) {
        super(TableLongThings.THINGS);

        set(0, id);
        set(1, description);
        set(2, properties);
        set(3, name);
    }
}
