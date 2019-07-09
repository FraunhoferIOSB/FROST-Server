package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableUuidTaskingCapabilities extends AbstractTableTaskingCapabilities<UUID> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The reference instance of <code>public.TASKINGCAPABILITIES</code>
     */
    public static final TableUuidTaskingCapabilities TASKINGCAPABILITIES = new TableUuidTaskingCapabilities();

    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    @Override
    public TableField<Record, UUID> getActuatorId() {
        return actuatorId;
    }

    @Override
    public TableField<Record, UUID> getThingId() {
        return thingId;
    }

    /**
     * The column <code>public.TASKINGCAPABILITIES.ID</code>.
     */
    public final TableField<Record, UUID> id = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.UUID)), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.ACTUATOR_ID</code>.
     */
    public final TableField<Record, UUID> actuatorId = createField("ACTUATOR_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.TASKINGCAPABILITIES.THING_ID</code>.
     */
    public final TableField<Record, UUID> thingId = createField("THING_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * Create a <code>public.TASKINGCAPABILITIES</code> table reference
     */
    public TableUuidTaskingCapabilities() {
        super();
    }

    /**
     * Create an aliased <code>public.TASKINGCAPABILITIES</code> table reference
     *
     * @param alias The name to use for the alias.
     */
    public TableUuidTaskingCapabilities(Name alias) {
        this(alias, TASKINGCAPABILITIES);
    }

    private TableUuidTaskingCapabilities(Name alias, TableUuidTaskingCapabilities aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidTaskingCapabilities as(String alias) {
        return new TableUuidTaskingCapabilities(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidTaskingCapabilities as(Name alias) {
        return new TableUuidTaskingCapabilities(alias, this);
    }

}
