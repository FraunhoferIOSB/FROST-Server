package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.uuidid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableSensors;
import java.util.UUID;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

public class TableUuidSensors extends AbstractTableSensors<UUID> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.SENSORS</code>
     */
    public static final TableUuidSensors SENSORS = new TableUuidSensors();

    /**
     * @return The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    @Override
    public TableField<Record, UUID> getId() {
        return id;
    }

    /**
     * The column <code>public.SENSORS.ID</code>.
     */
    public final TableField<Record, UUID> id = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false).defaultValue(org.jooq.impl.DSL.field("uuid_generate_v1mc()", org.jooq.impl.SQLDataType.UUID)), this, "");

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    public TableUuidSensors() {
        super();
    }

    /**
     * Create an aliased <code>public.SENSORS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableUuidSensors(Name alias) {
        this(alias, SENSORS);
    }

    private TableUuidSensors(Name alias, TableUuidSensors aliased) {
        super(alias, aliased);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidSensors as(String alias) {
        return new TableUuidSensors(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableUuidSensors as(Name alias) {
        return new TableUuidSensors(alias, this);
    }

}
