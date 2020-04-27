package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.longid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableLongTasks extends AbstractTableTasks<Long> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.TASKS</code>
     */
    public static final TableLongTasks TASKS = new TableLongTasks();

    /**
     * The column <code>public.TASKS.ID</code>.
     */
    public final TableField<Record, Long> colId = createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.field("nextval('\"TASKS_ID_seq\"'::regclass)", SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, Long> colTaskingCapabilityId = createField(DSL.name("TASKINGCAPABILITY_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.TASKS</code> table reference
     */
    public TableLongTasks() {
        super();
    }

    /**
     * Create an aliased <code>public.TASKS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableLongTasks(Name alias) {
        this(alias, TASKS);
    }

    private TableLongTasks(Name alias, TableLongTasks aliased) {
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
    public TableField<Record, Long> getTaskingCapabilityId() {
        return colTaskingCapabilityId;
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
