package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidMultiDatastreams extends AbstractTableMultiDatastreams<UUID> {

    private static final long serialVersionUID = 560943996;

    /**
     * The reference instance of <code>public.MULTI_DATASTREAMS</code>
     */
    public static final TableUuidMultiDatastreams MULTI_DATASTREAMS = new TableUuidMultiDatastreams();

    /**
     * The column <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField(DSL.name("ID"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, UUID> sensorId = createField(DSL.name("SENSOR_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, UUID> thingId = createField(DSL.name("THING_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableUuidMultiDatastreams() {
        super();
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidMultiDatastreams(Name alias) {
        this(alias, MULTI_DATASTREAMS);
    }

    private TableUuidMultiDatastreams(Name alias, TableUuidMultiDatastreams aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    @Override
    public TableField<Record, UUID> getSensorId() {
        return sensorId;
    }

    @Override
    public TableField<Record, UUID> getThingId() {
        return thingId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidMultiDatastreams as(String alias) {
        return new TableUuidMultiDatastreams(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidMultiDatastreams as(Name alias) {
        return new TableUuidMultiDatastreams(alias, this);
    }

}
