package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableTasks;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableLongTasks extends AbstractTableTasks<Long> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.TASKS</code>
     */
    public static final TableLongTasks TASKS = new TableLongTasks();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, Long> getId() {
        return id;
    }

    @Override
    public TableField<Record, Long> getTaskingCapabilityId() {
        return taskingCapabilityId;
    }

    /**
     * The column <code>public.TASKS.ID</code>.
     */
    public final TableField<Record, Long> id = createField("ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('\"TASKS_ID_seq\"'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, Long> taskingCapabilityId = createField("TASKINGCAPABILITY_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.TASKS</code> table reference
     */
    public TableLongTasks() {
        super();
    }

    /**
     * Create an aliased <code>public.TASKS</code> table reference
     */
    public TableLongTasks(String alias) {
        this(DSL.name(alias), TASKS);
    }

    /**
     * Create an aliased <code>public.TASKS</code> table reference
     */
    public TableLongTasks(Name alias) {
        this(alias, TASKS);
    }

    private TableLongTasks(Name alias, TableLongTasks aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongTasks as(String alias) {
        return new TableLongTasks(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableLongTasks as(Name alias) {
        return new TableLongTasks(alias, this);
    }

}
