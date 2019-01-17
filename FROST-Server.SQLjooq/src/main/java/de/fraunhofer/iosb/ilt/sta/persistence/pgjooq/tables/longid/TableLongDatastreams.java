package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableDatastreams;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongDatastreams extends AbstractTableDatastreams<Long> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.DATASTREAMS</code>
     */
    public static final TableLongDatastreams DATASTREAMS = new TableLongDatastreams();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return ID;
    }

    @Override
    public TableField<Record, Long> getSensorId() {
        return SENSOR_ID;
    }

    @Override
    public TableField<Record, Long> getObsPropertyId() {
        return OBS_PROPERTY_ID;
    }

    @Override
    public TableField<Record, Long> getThingId() {
        return THING_ID;
    }

    /**
     * The column <code>public.DATASTREAMS.ID</code>.
     */
    public final TableField<Record, Long> ID = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"DATASTREAMS_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.DATASTREAMS.SENSOR_ID</code>.
     */
    public final TableField<Record, Long> SENSOR_ID = createField("SENSOR_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.DATASTREAMS.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, Long> OBS_PROPERTY_ID = createField("OBS_PROPERTY_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.DATASTREAMS.THING_ID</code>.
     */
    public final TableField<Record, Long> THING_ID = createField("THING_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.DATASTREAMS</code> table reference
     */
    public TableLongDatastreams() {
        super();
    }

    /**
     * Create an aliased <code>public.DATASTREAMS</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableLongDatastreams(String alias) {
        this(DSL.name(alias), DATASTREAMS);
    }

    /**
     * Create an aliased <code>public.DATASTREAMS</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableLongDatastreams(Name alias) {
        this(alias, DATASTREAMS);
    }

    private TableLongDatastreams(Name alias, TableLongDatastreams aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongDatastreams as(String alias) {
        return new TableLongDatastreams(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongDatastreams as(Name alias) {
        return new TableLongDatastreams(alias, this);
    }

}
