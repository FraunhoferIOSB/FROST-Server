package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableDatastreams;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringDatastreams extends AbstractTableDatastreams<String> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.DATASTREAMS</code>
     */
    public static final TableStringDatastreams DATASTREAMS = new TableStringDatastreams();

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
    public TableField<Record, String> getObsPropertyId() {
        return ObsPropertyId;
    }

    @Override
    public TableField<Record, String> getThingId() {
        return thingId;
    }

    /**
     * The column <code>public.DATASTREAMS.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, String> sensorId = createField("SENSOR_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.DATASTREAMS.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, String> ObsPropertyId = createField("OBS_PROPERTY_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, String> thingId = createField("THING_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

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
    public TableStringDatastreams(String alias) {
        this(DSL.name(alias), DATASTREAMS);
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
