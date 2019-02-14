package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongTaskingCapabilities extends AbstractTableTaskingCapabilities<Long> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.TASKINGCAPABILITIES</code>
     */
    public static final TableLongTaskingCapabilities TASKINGCAPABILITIES = new TableLongTaskingCapabilities();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return id;
    }

    @Override
    public TableField<Record, Long> getActuatorId() {
        return actuatorId;
    }

    @Override
    public TableField<Record, Long> getThingId() {
        return thingId;
    }

    /**
     * The column <code>public.TASKINGCAPABILITIES.ID</code>.
     */
    public final TableField<Record, Long> id = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"TASKINGCAPABILITIES_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.ACTUATOR_ID</code>.
     */
    public final TableField<Record, Long> actuatorId = createField("ACTUATOR_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.THING_ID</code>.
     */
    public final TableField<Record, Long> thingId = createField("THING_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.TASKINGCAPABILITIES</code> table reference
     */
    public TableLongTaskingCapabilities() {
        super();
    }

    /**
     * Create an aliased <code>public.TASKINGCAPABILITIES</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableLongTaskingCapabilities(String alias) {
        this(DSL.name(alias), TASKINGCAPABILITIES);
    }

    /**
     * Create an aliased <code>public.TASKINGCAPABILITIES</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableLongTaskingCapabilities(Name alias) {
        this(alias, TASKINGCAPABILITIES);
    }

    private TableLongTaskingCapabilities(Name alias, TableLongTaskingCapabilities aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongTaskingCapabilities as(String alias) {
        return new TableLongTaskingCapabilities(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongTaskingCapabilities as(Name alias) {
        return new TableLongTaskingCapabilities(alias, this);
    }

}
