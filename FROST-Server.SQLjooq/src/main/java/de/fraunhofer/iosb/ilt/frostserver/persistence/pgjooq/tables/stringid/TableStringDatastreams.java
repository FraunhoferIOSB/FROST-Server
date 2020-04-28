package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableDatastreams;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringDatastreams extends AbstractTableDatastreams<String> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.DATASTREAMS</code>
     */
    public static final TableStringDatastreams DATASTREAMS = new TableStringDatastreams();

    /**
     * The column <code>public.DATASTREAMS.ID</code>.
     */
    public final TableField<Record, String> colId = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, String> colSensorId = createField(DSL.name("SENSOR_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.DATASTREAMS.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, String> colObsPropertyId = createField(DSL.name("OBS_PROPERTY_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, String> colThingId = createField(DSL.name("THING_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.DATASTREAMS</code> table reference
     */
    public TableStringDatastreams() {
        super();
    }

    /**
     * Create an aliased <code>public.DATASTREAMS</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableStringDatastreams(Name alias) {
        this(alias, DATASTREAMS);
    }

    private TableStringDatastreams(Name alias, TableStringDatastreams aliased) {
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
    public TableField<Record, String> getObsPropertyId() {
        return colObsPropertyId;
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
    public TableStringDatastreams as(String alias) {
        return new TableStringDatastreams(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringDatastreams as(Name alias) {
        return new TableStringDatastreams(alias, this);
    }

}
