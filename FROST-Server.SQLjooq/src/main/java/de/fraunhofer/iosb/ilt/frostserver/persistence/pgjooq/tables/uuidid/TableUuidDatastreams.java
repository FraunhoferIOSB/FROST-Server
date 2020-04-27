package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableDatastreams;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidDatastreams extends AbstractTableDatastreams<UUID> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.DATASTREAMS</code>
     */
    public static final TableUuidDatastreams DATASTREAMS = new TableUuidDatastreams();

    /**
     * The column <code>public.DATASTREAMS.ID</code>.
     */
    public final TableField<Record, UUID> colId = createField(DSL.name("ID"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, UUID> colSensorId = createField(DSL.name("SENSOR_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.DATASTREAMS.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, UUID> colObsPropertyId = createField(DSL.name("OBS_PROPERTY_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, UUID> colThingId = createField(DSL.name("THING_ID"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * Create a <code>public.DATASTREAMS</code> table reference
     */
    public TableUuidDatastreams() {
        super();
    }

    /**
     * Create an aliased <code>public.DATASTREAMS</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableUuidDatastreams(Name alias) {
        this(alias, DATASTREAMS);
    }

    private TableUuidDatastreams(Name alias, TableUuidDatastreams aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return colId;
    }

    @Override
    public TableField<Record, UUID> getObsPropertyId() {
        return colObsPropertyId;
    }

    @Override
    public TableField<Record, UUID> getSensorId() {
        return colSensorId;
    }

    @Override
    public TableField<Record, UUID> getThingId() {
        return colThingId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidDatastreams as(String alias) {
        return new TableUuidDatastreams(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidDatastreams as(Name alias) {
        return new TableUuidDatastreams(alias, this);
    }

}
