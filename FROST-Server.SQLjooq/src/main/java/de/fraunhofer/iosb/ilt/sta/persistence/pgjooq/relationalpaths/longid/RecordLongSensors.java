package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import org.jooq.Field;

public class RecordLongSensors extends AbstractRecordSensors<Long> {

    private static final long serialVersionUID = -1389387958;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongSensors.SENSORS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return TableLongSensors.SENSORS.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return TableLongSensors.SENSORS.encodingType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return TableLongSensors.SENSORS.metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return TableLongSensors.SENSORS.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return TableLongSensors.SENSORS.properties;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached SensorsRecord
     */
    public RecordLongSensors() {
        super(TableLongSensors.SENSORS);
    }

    /**
     * Create a detached, initialised SensorsRecord
     */
    public RecordLongSensors(Long id, String description, String encodingType, String metadata, String name, String properties) {
        super(TableLongSensors.SENSORS);

        set(0, id);
        set(1, description);
        set(2, encodingType);
        set(3, metadata);
        set(4, name);
        set(5, properties);
    }
}
