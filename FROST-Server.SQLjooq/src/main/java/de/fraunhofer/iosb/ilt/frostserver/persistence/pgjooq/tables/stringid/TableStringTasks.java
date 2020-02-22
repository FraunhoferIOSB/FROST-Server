package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringTasks extends AbstractTableTasks<String> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.TASKS</code>
     */
    public static final TableStringTasks TASKS = new TableStringTasks();

    /**
     * The column <code>public.TASKS.ID</code>.
     */
    public final TableField<Record, String> id = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, String> taskingCapabilityId = createField(DSL.name("TASKINGCAPABILITY_ID"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * Create a <code>public.TASKS</code> table reference
     */
    public TableStringTasks() {
        super();
    }

    /**
     * Create an aliased <code>public.TASKS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringTasks(Name alias) {
        this(alias, TASKS);
    }

    private TableStringTasks(Name alias, TableStringTasks aliased) {
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
    public TableField<Record, String> getTaskingCapabilityId() {
        return taskingCapabilityId;
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
