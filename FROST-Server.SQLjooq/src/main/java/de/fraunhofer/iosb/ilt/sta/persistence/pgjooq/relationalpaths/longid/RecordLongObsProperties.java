package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import org.jooq.Field;

public class RecordLongObsProperties extends AbstractRecordObsProperties<Long> {

    private static final long serialVersionUID = -1104077494;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongObsProperties.OBS_PROPERTIES.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return TableLongObsProperties.OBS_PROPERTIES.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return TableLongObsProperties.OBS_PROPERTIES.definition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return TableLongObsProperties.OBS_PROPERTIES.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return TableLongObsProperties.OBS_PROPERTIES.properties;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached ObsPropertiesRecord
     */
    public RecordLongObsProperties() {
        super(TableLongObsProperties.OBS_PROPERTIES);
    }

    /**
     * Create a detached, initialised ObsPropertiesRecord
     */
    public RecordLongObsProperties(Long id, String name, String definition, String description, String properties) {
        super(TableLongObsProperties.OBS_PROPERTIES);

        set(0, id);
        set(1, name);
        set(2, definition);
        set(3, description);
        set(4, properties);
    }
}
