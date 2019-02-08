package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringMultiDatastreams extends AbstractTableMultiDatastreams<String> {

    private static final long serialVersionUID = 560943996;

    /**
     * The reference instance of <code>public.MULTI_DATASTREAMS</code>
     */
    public static final TableStringMultiDatastreams MULTI_DATASTREAMS = new TableStringMultiDatastreams();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    @Override
    public TableField<Record, String> getSensorId() {
        return sensorId;
    }

    @Override
    public TableField<Record, String> getThingId() {
        return thingId;
    }

    /**
     * The column <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, String> sensorId = createField("SENSOR_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, String> thingId = createField("THING_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableStringMultiDatastreams() {
        super();
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableStringMultiDatastreams(String alias) {
        this(DSL.name(alias), MULTI_DATASTREAMS);
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableStringMultiDatastreams(Name alias) {
        this(alias, MULTI_DATASTREAMS);
    }

    private TableStringMultiDatastreams(Name alias, TableStringMultiDatastreams aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringMultiDatastreams as(String alias) {
        return new TableStringMultiDatastreams(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringMultiDatastreams as(Name alias) {
        return new TableStringMultiDatastreams(alias, this);
    }

}
