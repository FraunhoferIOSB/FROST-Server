package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableStringTasks extends AbstractTableTasks<String> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.TASKS</code>
     */
    public static final TableStringTasks TASKS = new TableStringTasks();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, String> getId() {
        return id;
    }

    @Override
    public TableField<Record, String> getTaskingCapabilityId() {
        return taskingCapabilityId;
    }

    /**
     * The column <code>public.TASKS.ID</code>.
     */
    public final TableField<Record, String> id = createField("ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, String> taskingCapabilityId = createField("TASKINGCAPABILITY_ID", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.TASKS</code> table reference
     */
    public TableStringTasks() {
        super();
    }

    /**
     * Create an aliased <code>public.TASKS</code> table reference
     */
    public TableStringTasks(String alias) {
        this(DSL.name(alias), TASKS);
    }

    /**
     * Create an aliased <code>public.TASKS</code> table reference
     */
    public TableStringTasks(Name alias) {
        this(alias, TASKS);
    }

    private TableStringTasks(Name alias, TableStringTasks aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringTasks as(String alias) {
        return new TableStringTasks(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringTasks as(Name alias) {
        return new TableStringTasks(alias, this);
    }

}
