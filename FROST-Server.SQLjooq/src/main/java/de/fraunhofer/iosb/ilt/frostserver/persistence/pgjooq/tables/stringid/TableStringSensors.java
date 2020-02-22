package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.stringid;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableSensors;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableStringSensors extends AbstractTableSensors<String> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The reference instance of <code>public.SENSORS</code>
     */
    public static final TableStringSensors SENSORS = new TableStringSensors();

    /**
     * The column <code>public.SENSORS.ID</code>.
     */
    public final TableField<Record, String> id = createField(DSL.name("ID"), SQLDataType.VARCHAR.nullable(false).defaultValue(DSL.field("uuid_generate_v1mc()", SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    public TableStringSensors() {
        super();
    }

    /**
     * Create an aliased <code>public.SENSORS</code> table reference
     *
     * @param alias The alias to use in queries.
     */
    public TableStringSensors(Name alias) {
        this(alias, SENSORS);
    }

    private TableStringSensors(Name alias, TableStringSensors aliased) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringSensors as(String alias) {
        return new TableStringSensors(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStringSensors as(Name alias) {
        return new TableStringSensors(alias, this);
    }

}
