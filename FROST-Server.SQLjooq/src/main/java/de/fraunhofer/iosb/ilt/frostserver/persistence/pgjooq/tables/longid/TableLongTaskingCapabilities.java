package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongTaskingCapabilities extends AbstractTableTaskingCapabilities<Long> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.TASKINGCAPABILITIES</code>
     */
    public static final TableLongTaskingCapabilities TASKINGCAPABILITIES = new TableLongTaskingCapabilities();

    /**
     * The column <code>public.TASKINGCAPABILITIES.ID</code>.
     */
    public final TableField<Record, Long> colId = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"TASKINGCAPABILITIES_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.ACTUATOR_ID</code>.
     */
    public final TableField<Record, Long> colActuatorId = createField(DSL.name("ACTUATOR_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.THING_ID</code>.
     */
    public final TableField<Record, Long> colThingId = createField(DSL.name("THING_ID"), SQLDataType.BIGINT.nullable(false), this, "");

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
    public TableLongTaskingCapabilities(Name alias) {
        this(alias, TASKINGCAPABILITIES);
    }

    private TableLongTaskingCapabilities(Name alias, TableLongTaskingCapabilities aliased) {
        super(alias, aliased);
    }

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return colId;
    }

    @Override
    public TableField<Record, Long> getActuatorId() {
        return colActuatorId;
    }

    @Override
    public TableField<Record, Long> getThingId() {
        return colThingId;
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
