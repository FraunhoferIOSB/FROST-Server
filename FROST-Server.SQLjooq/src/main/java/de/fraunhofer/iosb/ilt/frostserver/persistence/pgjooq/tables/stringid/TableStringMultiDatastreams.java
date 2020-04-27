package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringMultiDatastreams extends AbstractTableMultiDatastreams<String> {

    private static final long serialVersionUID = 560943996;

    /**
     * The reference instance of <code>public.MULTI_DATASTREAMS</code>
     */
    public static final TableStringMultiDatastreams MULTI_DATASTREAMS = new TableStringMultiDatastreams();

    /**
     * The column <code>public.MULTI_DATASTREAMS.ID</code>.
     */
    public final TableField<Record, String> colId = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, String> colSensorId = createField(DSL.name("SENSOR_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.MULTI_DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, String> colThingId = createField(DSL.name("THING_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.MULTI_DATASTREAMS</code> table reference
     */
    public TableStringMultiDatastreams() {
        super();
    }

    /**
     * Create an aliased <code>public.MULTI_DATASTREAMS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringMultiDatastreams(Name alias) {
        this(alias, MULTI_DATASTREAMS);
    }

    private TableStringMultiDatastreams(Name alias, TableStringMultiDatastreams aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return colId;
    }

    @Override
    public TableField<Record, String> getSensorId() {
        return colSensorId;
    }

    @Override
    public TableField<Record, String> getThingId() {
        return colThingId;
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
