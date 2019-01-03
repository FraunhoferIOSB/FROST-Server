package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import java.time.OffsetDateTime;
import org.jooq.Field;

public class RecordLongMultiDatastreams extends AbstractRecordMultiDatastreams<Long> {

    private static final long serialVersionUID = 265875599;

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.observationTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field5() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.phenomenonTimeStart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field6() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.phenomenonTimeEnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field7() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.resultTimeStart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<OffsetDateTime> field8() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.resultTimeEnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field9() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.SENSOR_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field10() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.THING_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field11() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.unitOfMeasurements;
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
    public Field<Object> field12() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.observedArea;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field13() {
        return TableLongMultiDatastreams.MULTI_DATASTREAMS.properties;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    /**
     * Create a detached MultiDatastreamsRecord
     */
    public RecordLongMultiDatastreams() {
        super(TableLongMultiDatastreams.MULTI_DATASTREAMS);
    }

    /**
     * Create a detached, initialised MultiDatastreamsRecord
     */
    public RecordLongMultiDatastreams(Long id, String name, String description, String observationTypes, OffsetDateTime phenomenonTimeStart, OffsetDateTime phenomenonTimeEnd, OffsetDateTime resultTimeStart, OffsetDateTime resultTimeEnd, Long sensorId, Long thingId, String unitOfMeasurements, Object observedArea, String properties) {
        super(TableLongMultiDatastreams.MULTI_DATASTREAMS);

        set(0, id);
        set(1, name);
        set(2, description);
        set(3, observationTypes);
        set(4, phenomenonTimeStart);
        set(5, phenomenonTimeEnd);
        set(6, resultTimeStart);
        set(7, resultTimeEnd);
        set(8, sensorId);
        set(9, thingId);
        set(10, unitOfMeasurements);
        set(11, observedArea);
        set(12, properties);
    }
}
