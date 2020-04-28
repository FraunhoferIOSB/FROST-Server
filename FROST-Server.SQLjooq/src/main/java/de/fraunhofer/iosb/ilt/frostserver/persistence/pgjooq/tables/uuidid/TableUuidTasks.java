package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableUuidTasks extends AbstractTableTasks<UUID> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The reference instance of <code>public.TASKS</code>
     */
    public static final TableUuidTasks TASKS = new TableUuidTasks();

    /**
     * The column <code>public.TASKS.ID</code>.
     */
    public final TableField<Record, UUID> colId = createField(DSL.name("ID"), SQLDataType.UUID.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, UUID> colTaskingCapabilityId = createField(DSL.name("TASKINGCAPABILITY_ID"), SQLDataType.UUID.nullable(false), this, "");

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

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return colId;
    }

    @Override
    public TableField<Record, UUID> getTaskingCapabilityId() {
        return colTaskingCapabilityId;
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
