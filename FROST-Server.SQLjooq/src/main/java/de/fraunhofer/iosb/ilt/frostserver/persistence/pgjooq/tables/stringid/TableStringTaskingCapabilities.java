package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringTaskingCapabilities extends AbstractTableTaskingCapabilities<String> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.TASKINGCAPABILITIES</code>
     */
    public static final TableStringTaskingCapabilities TASKINGCAPABILITIES = new TableStringTaskingCapabilities();

    /**
     * The column <code>public.TASKINGCAPABILITIES.ID</code>.
     */
    public final TableField<Record, String> id = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.ACTUATOR_ID</code>.
     */
    public final TableField<Record, String> actuatorId = createField(DSL.name("ACTUATOR_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.THING_ID</code>.
     */
    public final TableField<Record, String> thingId = createField(DSL.name("THING_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

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
