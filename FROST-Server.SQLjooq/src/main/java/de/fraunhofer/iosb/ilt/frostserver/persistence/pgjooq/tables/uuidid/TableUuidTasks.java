package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableUuidTasks extends AbstractTableTasks<UUID> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.TASKS</code>
     */
    public static final TableUuidTasks TASKS = new TableUuidTasks();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    @Override
    public TableField<Record, UUID> getTaskingCapabilityId() {
        return taskingCapabilityId;
    }

    /**
     * The column <code>public.TASKS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, UUID> taskingCapabilityId = createField("TASKINGCAPABILITY_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * Create a <code>public.TASKS</code> table reference
     */
    public TableUuidTasks() {
        super();
    }

    /**
     * Create an aliased <code>public.TASKS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidTasks(Name alias) {
        this(alias, TASKS);
    }

    private TableUuidTasks(Name alias, TableUuidTasks aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidTasks as(String alias) {
        return new TableUuidTasks(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidTasks as(Name alias) {
        return new TableUuidTasks(alias, this);
    }

}
