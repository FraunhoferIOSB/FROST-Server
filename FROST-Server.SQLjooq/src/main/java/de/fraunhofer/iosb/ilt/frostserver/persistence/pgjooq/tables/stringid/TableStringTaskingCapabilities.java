package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringTaskingCapabilities extends AbstractTableTaskingCapabilities<String> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.TASKINGCAPABILITIES</code>
     */
    public static final TableStringTaskingCapabilities TASKINGCAPABILITIES = new TableStringTaskingCapabilities();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    @Override
    public TableField<Record, String> getActuatorId() {
        return actuatorId;
    }

    @Override
    public TableField<Record, String> getThingId() {
        return thingId;
    }

    /**
     * The column <code>public.TASKINGCAPABILITIES.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.ACTUATOR_ID</code>.
     */
    public final TableField<Record, String> actuatorId = createField("ACTUATOR_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.THING_ID</code>.
     */
    public final TableField<Record, String> thingId = createField("THING_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.TASKINGCAPABILITIES</code> table reference
     */
    public TableStringTaskingCapabilities() {
        super();
    }

    /**
     * Create an aliased <code>public.TASKINGCAPABILITIES</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableStringTaskingCapabilities(Name alias) {
        this(alias, TASKINGCAPABILITIES);
    }

    private TableStringTaskingCapabilities(Name alias, TableStringTaskingCapabilities aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringTaskingCapabilities as(String alias) {
        return new TableStringTaskingCapabilities(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringTaskingCapabilities as(Name alias) {
        return new TableStringTaskingCapabilities(alias, this);
    }

}
